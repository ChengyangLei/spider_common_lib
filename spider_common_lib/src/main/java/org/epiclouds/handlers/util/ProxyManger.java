package org.epiclouds.handlers.util;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ProxyManger {

	private static Map<String,Map<SocketAddress, ProxyStateBean>>	proxyStatus=new ConcurrentHashMap<String,Map<SocketAddress,ProxyStateBean>>(); 
	private static Map<String,LinkedBlockingQueue<ProxyStateBean>>	proxies=new ConcurrentHashMap<String,LinkedBlockingQueue<ProxyStateBean>>(); 
	
	public static Map<String, Map<SocketAddress, ProxyStateBean>> getProxyStatus() {
		return proxyStatus;
	}

	public static void setProxyStatus(
			Map<String, Map<SocketAddress, ProxyStateBean>> proxyStatus) {
		ProxyManger.proxyStatus = proxyStatus;
	}

	public static void addProxy(String host,ProxyStateBean psb) throws InterruptedException{
		proxies.putIfAbsent(host, new LinkedBlockingQueue<ProxyStateBean>());
		LinkedBlockingQueue<ProxyStateBean> que=proxies.get(host);
		que.put(psb);
		proxyStatus.putIfAbsent(host, new ConcurrentHashMap<SocketAddress, ProxyStateBean>() );
		Map<SocketAddress, ProxyStateBean> mp=proxyStatus.get(host);
		mp.put(psb.getAddr(), psb);
	}
	
	public static ProxyStateBean getProxy(String host) throws InterruptedException {
		ProxyStateBean addr= proxies.get(host).take();
		proxyStatus.putIfAbsent(host, new ConcurrentHashMap<SocketAddress, ProxyStateBean>() );
		Map<SocketAddress, ProxyStateBean> mp=proxyStatus.get(host);
		ProxyStateBean psb=mp.get(addr.getAddr());
		psb.setUsing(true);
		psb.setErrorInfo(null);
		return addr;
	}
	public static void putProxy(String host,ProxyStateBean addr) throws InterruptedException {
		proxies.get(host).put(addr);
		proxyStatus.putIfAbsent(host, new ConcurrentHashMap<SocketAddress, ProxyStateBean>() );
		Map<SocketAddress, ProxyStateBean> mp=proxyStatus.get(host);
		ProxyStateBean psb=mp.get(addr.getAddr());
		psb.setUsing(false);
	}
	
	
	public static void setAddrErrorInfo(String host,ProxyStateBean addr,String errorInfo){
		proxyStatus.putIfAbsent(host, new ConcurrentHashMap<SocketAddress, ProxyStateBean>() );
		Map<SocketAddress, ProxyStateBean> mp=proxyStatus.get(host);
		ProxyStateBean psb=mp.get(addr.getAddr());
		psb.setErrorInfo(errorInfo);
		return;
	}
	
	
	
}
