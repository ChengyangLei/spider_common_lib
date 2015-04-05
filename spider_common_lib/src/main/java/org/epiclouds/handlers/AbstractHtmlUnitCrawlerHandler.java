/**
 * @author Administrator
 * @created 2014 2014年12月16日 上午10:46:39
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.epiclouds.client.main.CrawlerClient;
import org.epiclouds.handlers.util.ProxyManger;
import org.epiclouds.handlers.util.ProxyStateBean;
import org.joda.time.DateTime;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * @author Administrator
 *
 */
public abstract class AbstractHtmlUnitCrawlerHandler extends
		AbstractHandler {

	private  ArrayBlockingQueue<WebResponse> que=new ArrayBlockingQueue<>(10);
	
	volatile private WebClient webClient;
	private String windowName;
	private boolean isclosed=true;

	

	public AbstractHtmlUnitCrawlerHandler(){
		super();
	}


	public AbstractHtmlUnitCrawlerHandler(ProxyStateBean proxyaddr
			,String host,String url,String charset
			){
		super();
		this.proxyaddr=proxyaddr;
		this.host=host;
		this.url=url;
		this.charset=charset;
	}
	
	public void stop(){
		this.isrun=false;
	}
	
	private void reconnect() throws FailingHttpStatusCodeException, IOException{
		if(isclosed){
			if(this.webClient==null){
				this.webClient=new WebClient(BrowserVersion.CHROME);
				webClient.getOptions().setJavaScriptEnabled(true);
			    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			    webClient.getOptions().setThrowExceptionOnScriptError(false);
			    webClient.getOptions().setActiveXNative(false);
			    webClient.getOptions().setCssEnabled(false);
			    webClient.getOptions().setRedirectEnabled(true);
			    webClient.getOptions().setUseInsecureSSL(true);
			    windowName=webClient.getCurrentWindow().getName();
			   
			}
			   
			if(this.proxyaddr!=null){
				webClient.getOptions().setProxyConfig(new ProxyConfig(
						((InetSocketAddress)proxyaddr.getAddr()).getHostString(),
						((InetSocketAddress)proxyaddr.getAddr()).getPort()));
			}
			requestSelf();
			isclosed=false;
		}
		
	}
	
	private void handleResponse2(WebResponse webResponse) throws Exception{
		int status=webResponse.getStatusCode()/100;
		if(status==2){
			ByteBuf bb=null;
			try{
				//bb=Unpooled.wrappedBuffer(webResponse.getContentAsString().getBytes(webResponse.getContentCharset()));
				handle(webResponse.getContentAsString(charset));
				ProxyManger.setAddrErrorInfo(host, proxyaddr,null);
			}catch(Exception e){
/*				FileOutputStream out=new FileOutputStream("a.htm");
				out.write(webResponse.getContentAsString().getBytes(webResponse.getContentCharset()));
				out.flush();
				out.close();*/
				throw e;
			}finally{
				if(bb!=null){
					bb.release();
				}
			}
			return;
		}
		errorNum++;
		System.err.println("error:"+webResponse.getStatusMessage()+":"+this.getUrl());
		ProxyManger.setAddrErrorInfo(host, proxyaddr,new DateTime().toString("yyyy-MM-dd hh:mm:ss")+"error:"+webResponse.getStatusMessage()+":"+this.getUrl());

		Thread.sleep(errorSleepTime);
		close();
		onError(webResponse);
	}
	public void onError(Object response){
		
	}
	public void close(){
		isclosed=true;
	}


	
	private void requestSelf() throws FailingHttpStatusCodeException, IOException{
		try {
			this.que.clear();
			WebRequest re=new WebRequest(new URL(url),this.md.compareTo(HttpMethod.GET)==0?com.gargoylesoftware.htmlunit.HttpMethod.GET:com.gargoylesoftware.htmlunit.HttpMethod.POST);
			re.setCharset(charset);
			if(this.getProxyaddr()!=null&&this.getProxyaddr().getAuthStr()!=null){
				re.setAdditionalHeader("Proxy-Authorization", "Basic "
					+new sun.misc.BASE64Encoder().encode(this.getProxyaddr().getAuthStr().getBytes()));
			}
			Page p=webClient.getPage(re);
			this.que.add(p.getWebResponse());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata,String schema) throws Exception{
		this.url=url;
		this.md=hm;
		requestSelf();
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		this.setState(HandlerResultState.SUCCESS);
		try{
			while(isrun){
				try{
					reconnect();
					WebResponse re=this.que.poll(30*1000,TimeUnit.MILLISECONDS);
					if(re!=null){
						handleResponse2(re);
					}else{
						errorNum++;
						close();
					}
				}catch(Exception e){
					CrawlerClient.mainlogger.error(this.url,e);
					ProxyManger.setAddrErrorInfo(host, proxyaddr,
							new DateTime().toString("yyyy-MM-dd hh:mm:ss")+e.toString());

					errorNum++;
					if(maxErrorNum!=0&&errorNum>=maxErrorNum){
						this.setState(HandlerResultState.ERROR);
						stop();
						break;
					}
				}
			}
		}finally{
			if(webClient!=null){
				webClient.closeAllWindows();
			}
		}
		if(this.callback!=null){
			this.callback.onfinished(this);
		}

	}

	public ArrayBlockingQueue<WebResponse> getQue() {
		return que;
	}

	public void setQue(ArrayBlockingQueue<WebResponse> que) {
		this.que = que;
	}

	public WebClient getWebClient() {
		return webClient;
	}

	public void setWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

}
