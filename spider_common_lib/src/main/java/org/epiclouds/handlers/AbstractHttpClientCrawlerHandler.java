/**
 * @author Administrator
 * @created 2014 2014年12月16日 上午10:46:39
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
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
public abstract class AbstractHttpClientCrawlerHandler extends
		AbstractHandler {

	private  ArrayBlockingQueue<CloseableHttpResponse> que=new ArrayBlockingQueue<>(10);
	
    private BasicCookieStore cookieStore;
    private CloseableHttpClient httpclient ;
	

	public AbstractHttpClientCrawlerHandler(){
		super();
	}


	public AbstractHttpClientCrawlerHandler(SocketAddress proxyaddr
			,String host,String url,String schema
			){
		super();
		this.proxyaddr=proxyaddr;
		this.host=host;
		this.url=url;
		this.schema=schema;
	}
	
	public void stop(){
		this.isrun=false;
	}
	
	private void reconnect() throws FailingHttpStatusCodeException, IOException{
		if(httpclient==null){
			 cookieStore = new BasicCookieStore();
			 httpclient = HttpClients.custom()
			            .setDefaultCookieStore(cookieStore)
			            .build();
			requestSelf();
		}
	}
	
	private void handleResponse2(CloseableHttpResponse webResponse) throws Exception{
		int status=webResponse.getStatusLine().getStatusCode()/100;
		if(status==2){
			ByteBuf bb=null;
			try{
				//bb=Unpooled.wrappedBuffer(EntityUtils.toByteArray(webResponse.getEntity()));
				handle(EntityUtils.toString(webResponse.getEntity(), charset));
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
		System.err.println("error:"+webResponse.getStatusLine()+":"+this.getUrl());
		Thread.sleep(20*1000);
		
	}
	public void close(){
		try {
			httpclient.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		httpclient=null;
	}


	
	private void requestSelf() throws ClientProtocolException, IOException {
			this.que.clear();
			HttpHost target = new HttpHost(host,schema.equals("https")?443:80, schema);
			org.apache.http.HttpRequest request = null;
			if(this.md.compareTo(HttpMethod.GET)==0){
				request = new HttpGet(url);
			}
			if(this.md.compareTo(HttpMethod.POST)==0){
				request = new HttpPost(url);
				if(this.postdata!=null){
					for(String k:this.getPostdata().keySet()){
						List<NameValuePair> nvps = new ArrayList <NameValuePair>();  
				          
				        Set<String> keySet = this.postdata.keySet();  
				        for(String key : keySet) {  
				            nvps.add(new BasicNameValuePair(key, this.postdata.get(key)));  
				        }  
				          
				        ((HttpPost)request).setEntity(new UrlEncodedFormEntity(nvps));  
					}
				}
				
			}
			if(this.getProxyaddr()!=null){
	            HttpHost proxy = new HttpHost(((InetSocketAddress)this.getProxyaddr()).getHostString(), 
	            		((InetSocketAddress)this.getProxyaddr()).getPort(),
	            		"http");

	            RequestConfig config = RequestConfig.custom()
	                    .setProxy(proxy)
	                    .build();
	            ((HttpRequestBase)request).setConfig(config);
			}
			request.addHeader("Connection","Keep-Alive");
			request.addHeader("Host",this.host);
			request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36");
			if(this.headers!=null){
				for(String k:headers.keySet()){
					request.addHeader(k,headers.get(k));
				}
			}
			
			CloseableHttpResponse response = httpclient.execute(target, request);
            this.que.add(response);
	}
	public String getSchema() {
		return schema;
	}


	public void setSchema(String schema) {
		this.schema = schema;
	}


	public void request(String url,HttpMethod hm,Map<String,String> headers,
			Map<String,String> postdata,String schema) throws Exception{
		this.url=url;
		this.md=hm;
		this.headers=headers;
		this.postdata=postdata;
		this.schema=schema;
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
					CloseableHttpResponse re=this.que.poll(30*1000,TimeUnit.MILLISECONDS);
					if(re!=null){
						handleResponse2(re);
					}else{
						requestSelf();
					}
				}catch(Exception e){
					CrawlerClient.mainlogger.error(this.url,e);
					try {
						Thread.sleep(10*1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						requestSelf();
					} catch (ClientProtocolException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}finally{
			if(this.httpclient!=null){
				try {
					httpclient.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(this.callback!=null){
			this.callback.onfinished(this);
		}

	}



}
