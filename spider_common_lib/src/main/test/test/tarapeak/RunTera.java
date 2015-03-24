package test.tarapeak;

import org.epiclouds.client.main.CrawlerClient;
import org.epiclouds.handlers.AbstractHandler;

public class RunTera {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		CrawlerClient cr=new CrawlerClient();
		AbstractHandler ab=new TerapeakSpider(null, "sell.terapeak.com", "/", "https", 
				"utf-8", "pants",30,10);
		cr.execute(ab);
	}

}
