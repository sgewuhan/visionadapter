package com.sg.visionadapter;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientOptions.Builder;

public class ModelAdapterService {

	private static MongoClient mongo;

	private static DB db;
	
	public static void main(String[] args) {
		db = getDataBase(null);
		DBCollection doc = db.getCollection("document");
		System.out.println(doc.count());
	}
	
	public static DBCollection getCollection(String collectionName){
		return db.getCollection(collectionName);
	}

	public static DB getDataBase(String confPath) {
		if (db == null) {
			db = new ModelAdapterService().createDBFromProperties(confPath);
		}
		return db;
	}

	private DB createDBFromProperties(String confPath) {
		InputStream is = null;
		FileInputStream fis = null;
		try {
			if(confPath == null){
				URL url = getClass().getResource("vision.properties");
				fis = new FileInputStream(url.getPath()); //$NON-NLS-1$
			}else{
				fis = new FileInputStream(confPath); //$NON-NLS-1$
			}
			is = new BufferedInputStream(fis);
			Properties props = new Properties();
			props.load(is);
			mongo = createMongoClient(props);
			db = mongo.getDB(props.getProperty("db.name"));
			return db;
		} catch (Exception e) {
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
		return null;
	}

	public MongoClient createMongoClient(Properties props)
			throws UnknownHostException {
		String host = props.getProperty("db.host"); //$NON-NLS-1$
		String _port = props.getProperty("db.port");
		int port = _port == null ? 10001 : Integer.parseInt(_port); //$NON-NLS-1$
		ArrayList<ServerAddress> serverList = null;
		String replicaSet = props.getProperty("db.replicaSet"); //$NON-NLS-1$
		if (replicaSet != null && replicaSet.length() > 0) {
			serverList = new ArrayList<ServerAddress>();
			String[] arr = replicaSet.split(" ");
			for (int i = 0; i < arr.length; i++) {
				String[] ari = arr[i].split(":");
				ServerAddress address = new ServerAddress(ari[0],
						Integer.parseInt(ari[1]));
				serverList.add(address);
			}
		}

		Builder builder = MongoClientOptions.builder();
		builder.autoConnectRetry("true".equalsIgnoreCase(props //$NON-NLS-1$
				.getProperty("db.options.autoConnectRetry"))); //$NON-NLS-1$
		builder.connectionsPerHost(Integer.parseInt(props
				.getProperty("db.options.connectionsPerHost"))); //$NON-NLS-1$
		builder.maxWaitTime(Integer.parseInt(props
				.getProperty("db.options.maxWaitTime"))); //$NON-NLS-1$
		builder.socketTimeout(Integer.parseInt(props
				.getProperty("db.options.socketTimeout"))); //$NON-NLS-1$
		builder.connectTimeout(Integer.parseInt(props
				.getProperty("db.options.connectTimeout"))); //$NON-NLS-1$
		builder.threadsAllowedToBlockForConnectionMultiplier(Integer.parseInt(props
				.getProperty("db.options.threadsAllowedToBlockForConnectionMultiplier"))); //$NON-NLS-1$
		ServerAddress address = new ServerAddress(host, port);
		if (serverList != null) {
			return new MongoClient(serverList);
		} else {
			return new MongoClient(address, builder.build());
		}
	}


}
