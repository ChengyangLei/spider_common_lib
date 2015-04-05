package test.tarapeak;

import io.netty.handler.codec.http.HttpMethod;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.epiclouds.handlers.AbstractHttpClientCrawlerHandler;
import org.epiclouds.handlers.AbstractNettyCrawlerHandler;
import org.epiclouds.handlers.util.ProxyStateBean;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONObject;

public class TerapeakSpider extends AbstractHttpClientCrawlerHandler {
	private int i=1;
	private String keyword;
	private int days=30;
	private int num=10;
	private String fileName="1.txt";
	private FileOutputStream out;
	private TerapeakBean tb=new TerapeakBean();
	public TerapeakSpider(ProxyStateBean proxyaddr, String host, String url,
			String schema, String charset,String keyword,int days,int num) {
		super(proxyaddr, host, url, schema, charset);
		this.keyword=keyword;
		this.days=days;
		this.num=num;
		try {
			out=new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





	@Override
	public void handle(String content) throws Exception {
		SearchBean sr=new SearchBean();
		sr.setCurrency("1");
		sr.setId("0");
		sr.setQuery(keyword);
		sr.setSiteID("0");
		DateTime dt = new DateTime().minusDays(10);// 取得当前时间  
        // 取得 90天前的时间  
        DateTime dt5 = dt.minusDays(days*num-days*i);  
        sr.setDate_range(days);
        sr.setDate(dt5.toString("yyyy-MM-dd"));
        HashMap<String,String> pd=new HashMap<String,String>();
        pd.put(null, JSONObject.toJSONString(sr));
       
        HashMap<String,String> hs=new HashMap<String,String>();
        //headers
        hs.put("Accept", "application/json, text/javascript, */*; q=0.01");
        hs.put("Content-Type", "application/json");

        hs.put("Origin","https://sell.terapeak.cn");
        hs.put("Referer","https://sell.terapeak.cn/?page=eBayProductResearch");
        hs.put("X-Requested-With", "XMLHttpRequest");
        if(i>1&&i<=num+1){
        	TerapeakBean tmp=JSONObject.parseObject(content,TerapeakBean.class);
        	System.err.println(tmp.getAverage_end_price().getData().size());
        	tb.addAverage_end_price(tmp.getAverage_end_price());
        	tb.addBids(tmp.getBids());
        	tb.addBids_per_listings(tmp.getBids_per_listings());
        	tb.addItems_sold(tmp.getItems_sold());
        	tb.addRevenue(tmp.getRevenue());
        	tb.addTotal_listings(tmp.getTotal_listings());
        	tb.addSell_through(tmp.getSell_through());
        }
       
		if(i>num+1){
			out.write(JSONObject.toJSONString(tb).getBytes(charset));
			out.close();
			stop();
			System.err.println("完成");
			return;
		}
		i++;
		 System.err.println(JSONObject.toJSONString(sr));
        System.err.println("sleeping");
		Thread.sleep(10*1000);
		request("/services/ebay/legacy/productresearch/researchtrends?token=4e5396e3fe80ee1249a0b8147c08c5636a95579b274624fc6ce568ef3d2cdde5", HttpMethod.POST, 
					hs, pd, "https");
		
	}

}
