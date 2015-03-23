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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;

/**
 * @author Administrator
 *
 */
public abstract class AbstractHtmlUnitCrawlerHandler extends
		AbstractHandler {

	private  ArrayBlockingQueue<WebResponse> que=new ArrayBlockingQueue<>(10);
	
	volatile private WebClient webClient;
	volatile int errornum=0;
	private String windowName;
	private int errorlimit=Integer.MAX_VALUE;
	private boolean isclosed=true;

	

	public AbstractHtmlUnitCrawlerHandler(){
		super();
	}


	public AbstractHtmlUnitCrawlerHandler(SocketAddress proxyaddr
			,String host,String url,int errorlimit,String charset
			){
		super();
		this.proxyaddr=proxyaddr;
		this.host=host;
		this.url=url;
		this.errorlimit=errorlimit;
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
						((InetSocketAddress)proxyaddr).getHostString(),
						((InetSocketAddress)proxyaddr).getPort()));
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
		System.err.println("error:"+webResponse.getStatusMessage()+":"+this.getUrl());
		close();
		
	}
	public void close(){
		isclosed=true;
	}


	
	private void requestSelf() throws FailingHttpStatusCodeException, IOException{
		try {
			this.que.clear();
			Page p=webClient.getPage(new URL(url));
			this.que.add(p.getWebResponse());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata,String schema) throws Exception{
		this.url=url;
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
						errornum++;
						if(errornum>=errorlimit){
							this.setState(HandlerResultState.FAILED);
							stop();
							break;
						}
						close();
					}
				}catch(Exception e){
					CrawlerClient.mainlogger.error(this.url,e);
					errornum++;
					if(errornum>=errorlimit){
						this.setState(HandlerResultState.FAILED);
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
