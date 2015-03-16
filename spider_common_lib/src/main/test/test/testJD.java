/**
 * @author Administrator
 * @created 2014 2014年12月1日 下午5:16:42
 * @version 1.0
 */
package test;

import java.util.HashMap;
import java.util.Map;

import io.netty.handler.codec.http.HttpMethod;

import org.epiclouds.client.main.CrawlerClient;
import org.epiclouds.handlers.AbstractNettyCrawlerHandler;

/**
 * @author Administrator
 *
 */
public class testJD {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CrawlerClient c=new CrawlerClient();
		Map<String,String> hs=new HashMap<>();
		hs.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
		hs.put("Host", "search.jd.com");
/*		Map<String,String> data=new HashMap<>();
		data.put("keyword", "%E4%B8%89%E6%98%9F%E7%AC%94%E8%AE%B0%E6%9C%AC");
		data.put("enc", "utf-8");
		data.put("psort", "3");*/
		AbstractNettyCrawlerHandler h=new JDHandler(false,"search.jd.com", "/Search?keyword=%E4%B8%89%E6%98%9F%E7%AC%94%E8%AE%B0%E6%9C%AC"
				+ "&enc=utf-8&psort=3", HttpMethod.GET, hs, null);
		c.execute(h);
		
	}

}
