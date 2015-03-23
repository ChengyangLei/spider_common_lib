/**
 * @author Administrator
 * @created 2014 2014年12月16日 下午12:08:15
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.bootstrap.Bootstrap;
import io.netty.handler.codec.http.HttpMethod;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.epiclouds.handlers.util.FinishCallBack;

/**
 * @author Administrator
 *
 */
public abstract class AbstractHandler implements CrawlerHandlerInterface{
	protected volatile boolean isrun=true;
	protected volatile String name;


	protected volatile FinishCallBack callback;
	protected volatile HandlerResultState state;
	
	protected volatile String schema;
	protected volatile String host;
	protected volatile String url;
	protected volatile HttpMethod md=HttpMethod.GET;
	protected volatile Map<String,String> headers=new HashMap<String, String>();
	protected volatile Map<String,String> postdata=new HashMap<String, String>();
	protected volatile String charset;
	
	protected volatile SocketAddress proxyaddr;
	
	public String getHost() {
		return host;
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

	public void setHost(String host) {
		this.host = host;
	}
	public HandlerResultState getState() {
		return state;
	}

	public void setState(HandlerResultState state) {
		this.state = state;
	}

	public boolean isIsrun() {
		return isrun;
	}

	public void setIsrun(boolean isrun) {
		this.isrun = isrun;
	}

	public FinishCallBack getCallback() {
		return callback;
	}

	public void setCallback(FinishCallBack callback) {
		this.callback = callback;
	}


	public SocketAddress getProxyaddr() {
		return proxyaddr;
	}

	public void setProxyaddr(SocketAddress proxyaddr) {
		this.proxyaddr = proxyaddr;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	

	
	public  enum HandlerResultState{
		SUCCESS,
		FAILED
	}
	
}
