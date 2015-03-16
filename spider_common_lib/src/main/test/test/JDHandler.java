/**
 * @author Administrator
 * @created 2014 2014年12月1日 下午5:06:40
 * @version 1.0
 */
package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

import org.epiclouds.handlers.AbstractNettyCrawlerHandler;


/**
 * @author Administrator
 *
 */
public class JDHandler extends AbstractNettyCrawlerHandler {

	static String source="jd";
	/**
	 * @param host
	 * @param url
	 * @param md
	 * @param headers
	 * @param postdata
	 */
	public JDHandler(boolean useproxy,String host, String url, HttpMethod md,
			Map<String, String> headers, Map<String, String> postdata) {
		super("notebook",source,null,null,0,useproxy,host, url, md, headers, postdata);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.epiclouds.handlers.AbstractCrawlerHandler#handle(io.netty.handler.codec.http.FullHttpResponse)
	 */
	@Override
	public void handle(ByteBuf content) {
		// TODO Auto-generated method stub
		System.err.println(content.toString(Charset.forName("utf-8")));
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		request(this.getUrl(), this.getMd(), this.getHeaders(), this.getPostdata());
	}

	/* (non-Javadoc)
	 * @see org.epiclouds.handlers.AbstractCrawlerHandler#getBrand()
	 */
	@Override
	public String getBrand() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
