/**
 * @author Administrator
 * @created 2014 2014年12月10日 上午10:52:26
 * @version 1.0
 */
package org.epiclouds.handlers.util;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.util.ConcurrentHashSet;

/**
 * @author Administrator
 *
 */
public class ProxyManager{
	private static Map<String,Queue<SocketAddress>>   proxymap=new  ConcurrentHashMap<>();
	private static Map<String,Map<SocketAddress,Channel>>   proxyChannelMap=new  ConcurrentHashMap<>();

	public static synchronized void updateProxys(String host,Queue<SocketAddress> que){
		if(que==null||host==null){
			return;
		}
		proxymap.put(host, que);
	}
	public static synchronized void remove(String host){
		if(host==null){
			return;
		}
		proxymap.remove(host);
	}
	public static synchronized Channel getChannel(String host,SocketAddress addr){
		if(proxyChannelMap.get(host)==null){
			proxyChannelMap.put(host, new HashMap<SocketAddress,Channel>());
			return null;
		}
		Map<SocketAddress,Channel> channelMap=proxyChannelMap.get(host);
		return channelMap.get(addr);
	}
	/**
	 * put the channel into the host's queue
	 * @param host
	 * @param addr
	 * @return the old channel
	 */
	public static synchronized Channel putChannel(String host,SocketAddress addr,Channel ch){
		if(proxyChannelMap.get(host)==null){
			proxyChannelMap.put(host, new HashMap<SocketAddress,Channel>());
		}
		Map<SocketAddress,Channel> channelMap=proxyChannelMap.get(host);
		Channel oldch=channelMap.put(addr, ch);
		if(oldch!=null&&oldch.isActive()){
			oldch.close();
		}
		return oldch;
	}
	public synchronized static SocketAddress getProxy(String host){
		if(host==null)
			return null;
		Queue<SocketAddress> que=proxymap.get(host);
		if(que==null){
			que=new ConcurrentLinkedQueue<SocketAddress>();
			proxymap.put(host, que);
		}
		return que.poll();
	}
	public synchronized static int getSize(String host){
		if(host==null)
			return 0;
		Queue<SocketAddress> que=proxymap.get(host);
		if(que==null){
			return 0;
		}
		return que.size();
	}
	
	public synchronized static void putProxy(String host,SocketAddress addr){
		if(host==null||addr==null)
			return; 
		Queue<SocketAddress> que=proxymap.get(host);
		if(que==null){
			return;
		}
		que.offer(addr);
		return;
	}

	

}
