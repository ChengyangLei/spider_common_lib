/**
 * @author Administrator
 * @created 2014 2014年12月8日 下午1:30:30
 * @version 1.0
 */
package org.epiclouds.handlers.util;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;

/**
 * @author Administrator
 *
 */
public class MongoManager {
	private static MongoClient client;
	public static void init(String host,int port) throws UnknownHostException{
		if(client==null){
			client=new MongoClient(host,port);
			
		}
	}
	public static DB getDB(String name){
		return client.getDB(name);
	}
	
	public static void setSpiderState(String table,DBObject data){
		DB db=client.getDB("spider");
		DBCollection col=db.getCollection(table);
		col.update(data,data,true,false);
	}
	
	public static long getNum(String dbstr,String table,long date){
		DB db=client.getDB(dbstr);
		DBCollection col=db.getCollection(table);
		DBObject query=new BasicDBObject();
		query.put("date", date);
		return col.getCount(query);
	}
	
	public static long getNum(String dbstr,String table,DBObject query){
		DB db=client.getDB(dbstr);
		DBCollection col=db.getCollection(table);
		return col.getCount(query);
	}
	
	public static void putTaobaoZhishu(long today,String startdate,String enddate,String brand,String  word,
			String json){
		DB db=client.getDB("common");
		DBCollection col=db.getCollection("taobaozhishu");
		DBObject values=new BasicDBObject();
		values.put("startdate", startdate);
		values.put("enddate", enddate);
		values.put("date", today);
		values.put("brand", brand);
		values.put("data", json);
		values.put("word", word);
		DBObject condition=new BasicDBObject();
		condition.put("date", today);
		condition.put("word", word);
		col.update(condition, values,true,false);
	}
	
	public static void putBaiduShangQingQushi(long today,String brand,
			String model,String type,String area,String json){
		DB db=client.getDB("common");
		DBCollection col=db.getCollection("shangqingqushi");
		DBObject values=new BasicDBObject();
		values.put("date", today);
		values.put("brand", brand);
		values.put("model", model);
		values.put("type", type);
		values.put("area", area);
		values.put("data", json);
		DBObject condition=new BasicDBObject();
		condition.put("date", today);
		condition.put("brand", brand);
		condition.put("model", model);
		condition.put("type", type);
		condition.put("area", area);
		col.update(condition, values,true,false);
	}
	
	public static void putBaiduShangQingArea(long today,String brand,
			String model,String type,String area,String json){
		DB db=client.getDB("common");
		DBCollection col=db.getCollection("shangqingdiqu");
		DBObject values=new BasicDBObject();
		values.put("date", today);
		values.put("brand", brand);
		values.put("model", model);
		values.put("type", type);
		values.put("area", area);
		values.put("data", json);
		DBObject condition=new BasicDBObject();
		condition.put("date", today);
		condition.put("brand", brand);
		condition.put("model", model);
		condition.put("type", type);
		condition.put("area", area);
		col.update(condition, values,true,false);
	}
	
	
	/**
	 * get the models,key is model,value is brand
	 * @param dbStr
	 * @param tableStr
	 * @return
	 */
	public static Map<String,String> getAllModels(String dbStr,String tableStr){
		
		Map<String,String> re=new HashMap<String,String>();
		DB db=client.getDB(dbStr);
		DBCollection col=db.getCollection(tableStr);
		DBObject keys=new BasicDBObject();
		keys.put("model", 1);
		DBCursor cor=col.find(new BasicDBObject(), keys);
		while(cor.hasNext()){
			DBObject o=cor.next();
			String m=(String)o.get("model");
			if(m.indexOf("-")!=-1){
				m=m.substring(0,m.indexOf("-"));
			}
			re.put(m, tableStr);
		}
		return re;
	}
	
	
	
	public static void clearCollection(String dbString,String source,DBObject con){
		DB db=client.getDB(dbString);
		DBCollection col=db.getCollection(source);
		col.remove(con);
	}
	public static boolean haveSpided(String source,DBObject condition){
		DB db2=client.getDB("spider");
		DBCollection col2=db2.getCollection(source);
		DBObject cur=col2.findOne(condition);
		if(cur!=null){
			return true;
		}
		return false;
		
	}
	
}
