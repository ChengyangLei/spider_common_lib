/**
 * @author Administrator
 * @created 2014 2014年12月1日 下午2:49:56
 * @version 1.0
 */
package org.epiclouds.handlers.util;

import io.netty.channel.Channel;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.epiclouds.handlers.AbstractNettyCrawlerHandler;

/**
 * @author Administrator
 *
 */
public class CrawlerEnvironment{
	public static Executor pool=Executors.newCachedThreadPool();
	public static void sleep(){
		long time=new Random().nextInt(20)+10;
		//System.err.println("wait secs:"+time);
		try {
			Thread.sleep(time*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void sleepMin(){
		long time=new Random().nextInt(2)+1;
		//System.err.println("wait secs:"+time);
		try {
			Thread.sleep(time*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * host the host you want to connect.
	 * 
	 * @param host,a ip or dns
	 * @param timeout,seconds
	 */
	
	public static void sleep(String host){
		long total=new Random().nextInt(20)+20;
		int size=ProxyManager.getSize(host);
		long time=0;
		if(size==0){
			time=20*1000;
		}else{
			time=total*1000/size;
		}
		
		//System.err.println("wait secs:"+time);
		if(time<=0){
			time=2000;
		}
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
		}
	}
}
