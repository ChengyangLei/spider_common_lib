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
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.epiclouds.client.netty.handler.CrawlerHttpClientHandler;
import org.epiclouds.handlers.AbstractHandler;
import org.epiclouds.handlers.AbstractNettyCrawlerHandler;
import org.epiclouds.handlers.util.CrawlerEnvironment;
import org.epiclouds.handlers.util.ProxyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultChannelPromise;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.handler.codec.compression.SnappyFramedDecoder;
import io.netty.handler.codec.compression.SnappyFramedEncoder;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

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
					// TODO Auto-generated method stub
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
    	h.setSb(sb);
    	//h.setChannel(new NioSocketChannel(workers.next()));
		CrawlerEnvironment.pool.execute(h);
    }
    
    public void close(){
    	workers.shutdownGracefully();
    }


}
