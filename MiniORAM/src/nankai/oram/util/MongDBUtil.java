package nankai.oram.util;

import com.mongodb.*;
import com.mongodb.client.*;

public class MongDBUtil {
	MongoClient conn;
	MongoDatabase db;
	
	public void connect(String ip, int port, String dbName)
	{
		conn = new MongoClient(ip, port); 
		db = conn.getDatabase(dbName);
	}
	public void connect(String ip, int port )
	{
		conn = new MongoClient(ip, port);  
	} 
	public boolean createDB(String dbName)
	{
		if (conn==null)
		{
			System.out.println("no connection");
			return false;
		} 
		System.out.println("createDB "+dbName);
		//first, drop it if exists
		conn.dropDatabase(dbName);
		//then, create 
		db = conn.getDatabase(dbName);
		return true;
	}
	public boolean openDB(String dbName)
	{
		if (conn==null)
		{
			System.out.println("no connection");
			return false;
		}   
		System.out.println("openDB "+dbName);
		db = conn.getDatabase(dbName);
		return true;
	}
	/**
	 * create table / collection 
	 * use the default index '__id' as the unique index
	 * @param collectioName
	 * @return
	 */
	public boolean createCollection(String collectioName)
	{
		if (db==null)
		{
			System.out.println("no db");
			return false;
		}
		db.createCollection(collectioName); 
		return true;
	}
	
	public MongoCollection getCollection(String collectioName)
	{
		return db.getCollection(collectioName);
	}

}
