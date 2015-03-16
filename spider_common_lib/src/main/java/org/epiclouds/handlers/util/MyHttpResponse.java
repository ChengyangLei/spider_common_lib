/**
 * @author Administrator
 * @created 2014 2014年12月1日 下午3:42:09
 * @version 1.0
 */
package org.epiclouds.handlers.util;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * @author Administrator
 *
 */
public class MyHttpResponse {
	private Channel channel;
	private FullHttpResponse response;
	public MyHttpResponse(Channel channel,FullHttpResponse response){
		this.channel=channel;
		this.response=response;
	}
	public Channel getChannel() {
		return channel;
	}
	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	public FullHttpResponse getResponse() {
		return response;
	}
	public void setResponse(FullHttpResponse response) {
		this.response = response;
	}
	
}
