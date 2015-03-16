/**
 * @author Administrator
 * @created 2014 2014年12月1日 下午2:35:09
 * @version 1.0
 */
package org.epiclouds.handlers;

import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.Finishings;

import org.epiclouds.client.main.CrawlerClient;
import org.epiclouds.client.netty.handler.CrawlerHttpClientHandler;
import org.epiclouds.handlers.util.CrawlerEnvironment;
import org.epiclouds.handlers.util.FinishCallBack;
import org.epiclouds.handlers.util.MongoManager;
import org.epiclouds.handlers.util.MyHttpResponse;
import org.epiclouds.handlers.util.ProxyManager;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder.ErrorDataEncoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.codec.http.multipart.MixedAttribute;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * @author Administrator
 *
 */
public abstract class AbstractNettyCrawlerHandler extends AbstractHandler{
	private volatile Channel channel;	
	private volatile Bootstrap sb;

	private  ArrayBlockingQueue<MyHttpResponse> que=new ArrayBlockingQueue<>(100);

	private volatile HttpMethod md=HttpMethod.GET;
	private volatile Map<String,String> headers=new HashMap<String, String>();
	private volatile Map<String,String> postdata=new HashMap<String, String>();
	
	
	
	volatile boolean havechecked=false;

	String source;
	protected String type;
	String dbStr;
	protected String model;

	protected long today;
	private DBObject clearCondition;
	
	public long getToday() {
		return today;
	}
	public void setToday(long today) {
		this.today = today;
	}
	public boolean isUseproxy() {
		return useproxy;
	}
	public void setUseproxy(boolean useproxy) {
		this.useproxy = useproxy;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDbStr() {
		return dbStr;
	}
	public void setDbStr(String dbStr) {
		this.dbStr = dbStr;
	}
	public AbstractNettyCrawlerHandler(String dbStr,String source,String model,String type,long today,
			boolean useproxy,String host,String url,HttpMethod md,Map<String,String> headers,
			Map<String,String> postdata){
		this.model=model;
		this.dbStr=dbStr;
		this.source=source;
		this.type=type;
		this.today=today;
		this.useproxy=useproxy;
		if(headers!=null)
			this.headers.putAll(headers);
		this.host=host;
		this.url=url;
		this.md=md;
		if(postdata!=null){
			this.postdata.putAll(postdata);;
		}
	}
	private void reconnect(){
		if(!this.useproxy&&channel!=null&&channel.isActive()){
			return;
		}
		this.que.clear();
		SocketAddress addr=null;
		if(this.useproxy==false){
			addr=new InetSocketAddress(host, 80);
		}else{
			try {
				ProxyManager.putProxy(host, proxyaddr);
			} catch (Exception e) {
				CrawlerClient.mainlogger.error(e.getLocalizedMessage(),e);
			}
			do{
				proxyaddr=ProxyManager.getProxy(host);
				if(proxyaddr==null){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}while(proxyaddr==null);
			addr=proxyaddr;
		}
		
		if(this.useproxy){
			this.channel=ProxyManager.getChannel(host, addr);
			if(this.channel!=null&&this.channel.isActive()){
				requestSelf();
				return;
			}
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
						if(AbstractNettyCrawlerHandler.this.useproxy){
							ProxyManager.putChannel(host, n.remoteAddress(), n);
						}
						requestSelf();
					}else{
						System.err.println(future.cause());
						proxyaddr=null;
					}
				}
			}).sync();
			future.sync();
		} catch (InterruptedException e) {
		}
	}
	public void stop(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.isrun=false;
		close();
		if(this.useproxy){
			try {
				ProxyManager.putProxy(host, proxyaddr);
			} catch (Exception e) {
				CrawlerClient.mainlogger.error(e.getLocalizedMessage(),e);
			}
		}
		if(this.source!=null&&this.type!=null){
			DBObject condition2=new BasicDBObject();
			condition2.put("brand", this.getBrand());
			condition2.put("type", type);
			condition2.put("date", today);
			condition2.put("model", model);
			MongoManager.setSpiderState( source, condition2);
		}
		
	}
	
	public void close(){
		try{
			if(!this.useproxy&&this.channel!=null){
				this.channel.close().syncUninterruptibly();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void requestSelf(){
		if(this.channel==null||!this.channel.isActive()){
			return;
		}
		FullHttpRequest req=null;
		if(!this.useproxy){
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
	public void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata){
		this.url=url;
		this.md=hm;
		this.headers=headers;
		this.postdata=postdata;
		if(!this.useproxy){
			requestSelf();
		}
	}
	
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public HttpMethod getMd() {
		return md;
	}
	public void setMd(HttpMethod md) {
		this.md = md;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Map<String, String> getPostdata() {
		return postdata;
	}
	public void setPostdata(Map<String, String> postdata) {
		this.postdata = postdata;
	}
	public ArrayBlockingQueue<MyHttpResponse> getQue() {
		return que;
	}
	public boolean isIsrun() {
		return isrun;
	}
	public void setIsrun(boolean isrun) {
		this.isrun = isrun;
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
			System.out.println(this.getBrand());
			while(isrun){
				try{
					this.reconnect();
					if(!havechecked){
						if(source!=null&&type!=null){
							DBObject condition=new BasicDBObject();
							condition.put("brand", getBrand());
							condition.put("type", type);
							condition.put("date", today);
							condition.put("model", model);
							if(MongoManager.haveSpided(source,condition)){
								this.stop();
								continue;
							}else{
								MongoManager.clearCollection(this.dbStr,this.source,clearCondition);
							}
						}
						havechecked=true;
					}
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
						if(!this.useproxy){
							requestSelf();
						}
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
				FileOutputStream out=new FileOutputStream("a.htm");
				out.write(fullHttpResponse.content().toString(Charset.forName("gbk")).getBytes("gbk"));
				out.flush();
				out.close();
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
				this.sleep();
				return;
			}
			this.setUrl(fullHttpResponse.headers().get("Location"));
			this.sleep();
			return;
		}
		if(status==4||status==5||status==1){
			if(this.useproxy&&fullHttpResponse.getStatus().code()==403){
				proxyaddr=null;
			}
			this.sleep();
			return;
		}
		
	}
	public Bootstrap getSb() {
		return sb;
	}
	public void setSb(Bootstrap sb) {
		this.sb = sb;
	}
	
	public SocketAddress getProxyaddr() {
		return proxyaddr;
	}
	public void setProxyaddr(SocketAddress proxyaddr) {
		this.proxyaddr = proxyaddr;
	}

	/**
	 * @return
	 */
	abstract public String getBrand() ;
	public DBObject getClearCondition() {
		return clearCondition;
	}
	public void setClearCondition(DBObject clearCondition) {
		this.clearCondition = clearCondition;
	}
	

}
