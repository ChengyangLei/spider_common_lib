/**
 * @author Administrator
 * @created 2014 2014年12月16日 上午10:46:39
 * @version 1.0
 */
package org.epiclouds.handlers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.epiclouds.client.main.CrawlerClient;
import org.epiclouds.handlers.util.CrawlerEnvironment;
import org.epiclouds.handlers.util.MongoManager;
import org.epiclouds.handlers.util.MyHttpResponse;
import org.epiclouds.handlers.util.ProxyManager;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Administrator
 *
 */
public abstract class AbstractHtmlUnitCrawlerHandler extends
		AbstractHandler {

	private  ArrayBlockingQueue<WebResponse> que=new ArrayBlockingQueue<>(10);
	
	volatile private WebClient webClient;
	volatile private String host;
	//AtomicInteger windowindex=new AtomicInteger(0);
	volatile int errornum=0;
	boolean havechecked=false;
	
	protected String dbString;
	String source;
	protected DBObject condition;
	private String windowName;
	


	public WebClient getWebClient() {
		return webClient;
	}

	public void setWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	public void setSb(Bootstrap sb) {
		
	}
	private int errorlimit=Integer.MAX_VALUE;
	private boolean isclosed=true;
	public AbstractHtmlUnitCrawlerHandler(){
		super();
	}
	public DBObject getCondition() {
		return condition;
	}

	public void setCondition(DBObject condition) {
		this.condition = condition;
	}

	public AbstractHtmlUnitCrawlerHandler(boolean useproxy
			,String dbstring,long today,String source,String host,String url,int errorlimit
			){
		super();
		this.host=host;
		this.useproxy=useproxy;
		this.url=url;
		this.errorlimit=errorlimit;
		this.source=source;
		this.dbString=dbstring;
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
			   
			if(this.useproxy&&this.proxyaddr==null){
				this.proxyaddr=ProxyManager.getProxy(host);
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
				bb=Unpooled.wrappedBuffer(webResponse.getContentAsString().getBytes(webResponse.getContentCharset()));
				handle(bb);
			}catch(Exception e){
				FileOutputStream out=new FileOutputStream("a.htm");
				out.write(webResponse.getContentAsString().getBytes(webResponse.getContentCharset()));
				out.flush();
				out.close();
				throw e;
			}finally{
				if(bb!=null){
					bb.release();
				}
			}
			return;
		}
		System.err.println("error:"+webResponse.getStatusMessage()+":"+this.getUrl());
/*		if(status==3){
			this.setUrl(webResponse.getResponseHeaderValue("Location"));
		}*/
		close();
		
	}
	public void close(){
		if(this.useproxy){
			if(proxyaddr!=null){
				ProxyManager.putProxy(host, proxyaddr);
			}
			proxyaddr=null;
		}
		isclosed=true;
	}

	public Page requestPage(String url) throws FailingHttpStatusCodeException, IOException{
			Page p=webClient.getPage(new URL(url));
			return p;
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
	public void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata) throws Exception{
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
					if(!havechecked){
						if(source!=null){
							if(MongoManager.haveSpided(source,condition)){
								this.stop();
								continue;
							}else{
								MongoManager.clearCollection(this.dbString,this.source,condition);
							}
						}
						havechecked=true;
					}
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
						FileOutputStream out;
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

}
