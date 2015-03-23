/**
 * @author Administrator
 * @created 2014 2014年12月16日 上午10:13:25
 * @version 1.0
 */
package org.epiclouds.handlers;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;

/**
 * @author Administrator
 *
 */
public interface CrawlerHandlerInterface extends Runnable{
	public void handle(String content) throws Exception;
	public void request(String url,HttpMethod hm,Map<String,String> headers,Map<String,String> postdata,
			String schema) throws Exception;
	public void stop();
	public void close();
}
