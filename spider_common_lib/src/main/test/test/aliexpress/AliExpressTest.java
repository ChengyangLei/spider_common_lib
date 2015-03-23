package test.aliexpress;

import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * A example that demonstrates how HttpClient APIs can be used to perform
 * form-based logon.
 */
public class AliExpressTest {

    public static void main(String[] args) throws Exception {
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

     /* {
            HttpUriRequest login = RequestBuilder.post()
                    .setUri(new URI("https://data.terapeak.cn/verify/"))
                    .addParameter("username", "vincentyunlou@foxmail.com")
                    .addParameter("password", "yuanshuju")
                    .build();
            CloseableHttpResponse response2 = httpclient.execute(login);
            try {
                HttpEntity entity = response2.getEntity();

                System.out.println("Login form get: " + response2.getStatusLine());
                EntityUtils.consume(entity);

                System.out.println("Post logon cookies:");
                List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i).toString());
                    }
                }
            } finally {
                response2.close();
            }
        }*/
            
/*            {
	            HttpGet httpget = new HttpGet("https://sell.terapeak.cn/?page=eBayProductResearch#productResearch/trend");
	            CloseableHttpResponse response1 = httpclient.execute(httpget);
	            try {
	                HttpEntity entity = response1.getEntity();
	
	                System.out.println("Login form get: " + response1.getStatusLine());
	            } finally {
	                response1.close();
	            }
            
            }*/
            
            
            
            
            
            
            
            {
            	
            	HttpUriRequest login = RequestBuilder.get()
                        .setUri(new URI("http://www.aliexpress.com/cross-domain/detailevaluationproduct/"
                        		+ "index.html?productId=1882101061&type=reverse&page=1149"))
                        .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                        .addHeader("Origin","http://www.aliexpress.com")
                        .addHeader("Referer","http://www.aliexpress.com/item/size-35-45-new-2014-high-quality-fashion-low-high-men-women-sneakers-and-lace-up/1882101061.html?spm=5261.7132366.1998156808.9")
                        .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36")
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        . build();
                CloseableHttpResponse response2 = httpclient.execute(login);
                try {
                    HttpEntity entity = response2.getEntity();

                    System.out.println("Login form get: " + response2.getStatusLine());
                    

                    String ss=EntityUtils.toString(entity);
                    AliexpressBean ab=JSONObject.parseObject(ss, AliexpressBean.class);
                    
                    System.err.println(JSONObject.toJSONString(ab));
                } finally {
                    response2.close();
                }

            }
            
    }
}