/**
 * @author Administrator
 * @created 2014 2014年12月1日 下午2:35:09
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.epiclouds.client.main.CrawlerClient;
import org.epiclouds.client.netty.handler.CrawlerHttpClientHandler;
import org.epiclouds.handlers.util.MyHttpResponse;

/**
 * @author Administrator
 *
 */
public abstract class AbstractNettyCrawlerHandler extends AbstractHandler{
	private volatile Channel channel;	
	private volatile Bootstrap sb;

	private  ArrayBlockingQueue<MyHttpResponse> que=new ArrayBlockingQueue<>(100);
	

	public AbstractNettyCrawlerHandler(
			SocketAddress proxyAddr,String schema,String host,String url,HttpMethod md,Map<String,String> headers,
			Map<String,String> postdata){
		this.proxyaddr=proxyAddr;
		if(headers!=null)
			this.headers.putAll(headers);
		this.host=host;
		this.url=url;
		this.md=md;
		this.schema=schema;
		if(postdata!=null){
			this.postdata.putAll(postdata);;
		}
	}


	private void reconnect(){
		if(channel!=null&&channel.isActive()){
			return;
		}
		this.que.clear();
		SocketAddress addr=null;
		if(proxyaddr==null){
			addr=new InetSocketAddress(host,schema.equals("http")?80:443 );
		}else{
			addr=proxyaddr;
		}
		try {
			ChannelFuture future=sb.connect(addr);
			future.addListener(new GenericFutureListener<Future<? super Void>>() {
				@Override
				public void operationComplete(Future<? super Void> future)
						throws Exception {
					if(future.isSuccess()){
						Channel n=((DefaultChannelPromise) future).channel();
						AbstractNettyCrawlerHandler.this.setChannel(n);
						
						if(channel.pipeline().get("crawlerhandler")==null){
							channel.pipeline().addLast("crawlerhandler",new CrawlerHttpClientHandler(AbstractNettyCrawlerHandler.this));
						}
						requestSelf();
					}
				}
			}).sync();
			future.sync();
		} catch (InterruptedException e) {
		}
	}
	public void stop(){
		this.isrun=false;
		close();
		
	}
	
	public void close(){
		try{
			this.channel.close().syncUninterruptibly();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void requestSelf(){
		if(this.channel==null||!this.channel.isActive()){
			return;
		}
		FullHttpRequest req=null;
		if(this.proxyaddr==null){
			req=new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, this.md, url);
			req.headers().add("Connection","Keep-Alive");
		}else{
			req=new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, this.md, "http://"+this.host+url);
			req.headers().add("Proxy-Connection","Keep-Alive");
		}
		req.headers().add("Host",this.host);
		req.headers().add("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36");
		if(headers!=null){
			for(String key:headers.keySet()){
				req.headers().add(key,headers.get(key));
			}
		}
		HttpPostRequestEncoder encoder=null;
		
		if(this.md.compareTo(HttpMethod.POST)==0&&postdata!=null){
			try {
				encoder = new HttpPostRequestEncoder(req, false);
				for(String key:postdata.keySet()){
					encoder.addBodyAttribute(key, postdata.get(key));
				}
				encoder.finalizeRequest();
				channel.writeAndFlush(req);
				//channel.writeAndFlush(encoder);
				//encoder.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
		}else{
			channel.writeAndFlush(req);
		}
	}
	public void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata,String schema){
		this.url=url;
		this.md=hm;
		this.headers=headers;
		this.postdata=postdata;
		this.schema=schema;
		requestSelf();
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	public ArrayBlockingQueue<MyHttpResponse> getQue() {
		return que;
	}
	public void setQue(ArrayBlockingQueue<MyHttpResponse> que) {
		this.que = que;
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			while(isrun){
				try{
					this.reconnect();
					MyHttpResponse r=que.poll(60000, TimeUnit.MILLISECONDS);
					if(r!=null){
						try{
							if(!r.getChannel().equals(channel)){
								continue;
							}
							this.handleResponse2(r.getResponse());
						}finally{
							r.getResponse().release();
						}
					}else{
						this.close();
					}
				}catch(Exception e){
					try{
						CrawlerClient.mainlogger.error(this.url,e);
						Thread.sleep(5000);
						requestSelf();
					}catch(Exception e1){
						
					}
				}
			}
		}finally{
			if(callback!=null){
				this.state=HandlerResultState.SUCCESS;
				callback.onfinished(this);
			}
		}
		
	}
	private void handleResponse2(FullHttpResponse fullHttpResponse) throws Exception{
		int status=fullHttpResponse.getStatus().code()/100;
		if(status==2){
			try{
				handle(fullHttpResponse.content());
			}catch(Exception e){
				CrawlerClient.mainlogger.error(this.url,e);
				/*FileOutputStream out=new FileOutputStream("a.htm");
				out.write(fullHttpResponse.content().toString(Charset.forName("gbk")).getBytes("gbk"));
				out.flush();
				out.close();*/
				throw e;
			}
			return;
		}
		System.err.println("error:"+fullHttpResponse.getStatus()+":"+this.getUrl());
		this.close();
		if(status==3){
			if(fullHttpResponse.headers().get("Location").contains("err")
					||fullHttpResponse.headers().get("Location").contains("error")
					||fullHttpResponse.headers().get("Location").contains("sec")
					||fullHttpResponse.headers().get("Location").contains("security")){
				return;
			}
			this.setUrl(fullHttpResponse.headers().get("Location"));
			return;
		}
		if(status==4||status==5||status==1){
			return;
		}
		
	}
	public Bootstrap getSb() {
		return sb;
	}
	public void setSb(Bootstrap sb) {
		this.sb = sb;
	}
	

}
