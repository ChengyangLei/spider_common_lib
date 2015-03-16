/**
 * @author Administrator
 * @created 2014 2014年12月16日 下午12:08:15
 * @version 1.0
 */
package org.epiclouds.handlers;

import io.netty.bootstrap.Bootstrap;

import java.net.SocketAddress;

import org.epiclouds.handlers.util.CrawlerEnvironment;
import org.epiclouds.handlers.util.FinishCallBack;

/**
 * @author Administrator
 *
 */
public abstract class AbstractHandler implements CrawlerHandlerInterface{
	protected volatile boolean isrun=true;
	protected volatile String name;


	protected volatile FinishCallBack callback;
	protected volatile String host;
	
	protected volatile boolean useproxy;
	protected volatile SocketAddress proxyaddr;
	
	protected volatile String url;

	protected volatile HandlerResultState state;
	
	public volatile int  num=0;
	public volatile long time=System.currentTimeMillis();
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	public HandlerResultState getState() {
		return state;
	}

	public abstract String getBrand();
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

	public boolean isUseproxy() {
		return useproxy;
	}

	public void setUseproxy(boolean useproxy) {
		this.useproxy = useproxy;
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
	public abstract void setSb(Bootstrap sb);
	
	
	protected void sleep(){
		if(this.useproxy){
			CrawlerEnvironment.sleep(host);
		}else{
			CrawlerEnvironment.sleep();
		}
	}
	
	public  enum HandlerResultState{
		SUCCESS,
		FAILED
	}
	
}
