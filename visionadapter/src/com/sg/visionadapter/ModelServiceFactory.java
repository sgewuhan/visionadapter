package com.sg.visionadapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;

public class ModelServiceFactory {

	private static ConcurrentHashMap<String, MongoClient> clients;

	private DB defaultDB;

	public static ModelServiceFactory service;

	public ModelServiceFactory() {
	}

	/**
	 * 
	 * @param confFolder
	 *            配置文件的目录名
	 */
	public void start(String confFolder) {
		if (service == null) {
			service = this;
			clients = new ConcurrentHashMap<String, MongoClient>();
			loadDB(confFolder);
		}
	}

	public DB getDB(String dbname) {
		if (dbname == null) {
			throw new IllegalArgumentException("dbname cannot null");
		}
		MongoClient client = clients.get(dbname);
		if (client == null) {
			throw new IllegalArgumentException("db does not registed,name:"
					+ dbname);
		}
		return client.getDB(dbname);
	}

	public DBCollection getCollection(String collectionName) {
		return defaultDB.getCollection(collectionName);
	}

	private void loadDB(String confFolder) {
		File folder;
		if (confFolder == null) {
			String folderName = System.getProperty("user.dir") //$NON-NLS-1$
					+ File.separator + "conf" + File.separator;//$NON-NLS-1$
			folder = new File(folderName);
		} else {
			folder = new File(confFolder);
		}

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File file, String name) {
				return name.toLowerCase().endsWith(".dbconf");
			}
		};
		File[] files = folder.listFiles(filter);
		if (files == null) {
			throw new IllegalArgumentException(
					"does not contains .dbconf files in designated folder");
		}
		for (int i = 0; i < files.length; i++) {
			createClient(files[i]);
		}
		if(defaultDB == null){
			defaultDB = clients.get("pm2").getDB("pm2");
		}
	}

	private MongoClient createClient(File file) {
		InputStream is = null;
		FileInputStream fis = null;
		try {
			is = new FileInputStream(file);
			Properties props = new Properties();
			props.load(is);
			MongoClient mongo = createMongoClient(props);
			String name = props.getProperty("db.name"); //$NON-NLS-1$
			String dbname;
			if (name != null && !name.isEmpty()) {
				dbname = name;
				clients.put(name, mongo);
			} else {
				dbname = file.getName().substring(0,
						file.getName().lastIndexOf("."));
				clients.put(dbname, mongo);
			}
			boolean isDefault = "true".equals(props.getProperty("default")); //$NON-NLS-1$
			if (isDefault) {
				defaultDB = mongo.getDB(dbname);
			}
			return mongo;
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

	private MongoClient createMongoClient(Properties props)
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

	public <T extends IPersistenceService> T get(Class<T> t)
			throws InstantiationException, IllegalAccessException {
		T instance = t.newInstance();
		DBCollection col = getCollection(instance.getCollectionName());
		instance.setCollection(col);
		return instance;
	}

}
