/**
 * @author Administrator
 * @created 2014 2014年8月27日 下午3:04:28
 * @version 1.0
 */
package org.epiclouds.client.main;

/**
 * @author Administrator
 *
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.handlers.AbstractNettyCrawlerHandler;
import org.epiclouds.handlers.util.CrawlerEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discards any incoming data.
 */
public class CrawlerClient {

	public  static Logger mainlogger = LoggerFactory.getLogger(CrawlerClient.class);

    private Bootstrap sb=new Bootstrap();
    private EventLoopGroup workers=new NioEventLoopGroup();

    public  CrawlerClient() throws Exception {   
	        sb.group(workers).channel(NioSocketChannel.class).
	        option(ChannelOption.SO_KEEPALIVE, true).handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					
					 ChannelPipeline pipeline = ch.pipeline();
				        /**
				         * http-response解码器
				         * http服务器端对response解码
				         */
				        pipeline.addLast(new HttpResponseDecoder());
				        /**
				         * http服务器端对request编码
				         */
				        pipeline.addLast( new HttpRequestEncoder());
				        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576*1024));

				        /**
				         * 压缩
				         * Compresses an HttpMessage and an HttpContent in gzip or deflate encoding
				         * while respecting the "Accept-Encoding" header.
				         * If there is no matching encoding, no compression is done.
				         */
				        //pipeline.addLast("deflater", new HttpContentCompressor());
				        //pipeline.addLast("redeflater", new HttpContentDecompressor());
				        
                	
				}	
	        });
	        System.out.println("client started");
    }
    
    public void execute(final AbstractHandler h) {
    	if(h instanceof AbstractNettyCrawlerHandler){
    		((AbstractNettyCrawlerHandler) h).setSb(sb);
    	}
    	//h.setChannel(new NioSocketChannel(workers.next()));
		CrawlerEnvironment.pool.execute(h);
    }
    public void execute(final Runnable h) {
		CrawlerEnvironment.pool.execute(h);
    }
    
    public void close(){
    	workers.shutdownGracefully();
    }


}
