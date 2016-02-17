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

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;

/**
 * 模型服务工厂，用于创建mongodb的客户端，第一次调用，需调用start方法以启动模型服务
 * 
 * @author zhonghua
 *
 */
public class ModelServiceFactory {

	private static ConcurrentHashMap<String, MongoClient> clients;

	private static DB defaultDB;

	public static String URL_PRODUCTNUM;

	public static String URL_REAUTH;

	public static String URL_LINKDELIVERY;

	public static String URL_DOCUMENTSERVICE;

	public static String URL_EPMDOCUMENTSERVICE;

	public static ModelServiceFactory service;

	public static ModelServiceFactory getInstance(String confFolder) {
		if (service != null) {
			return service;
		}
		service = new ModelServiceFactory();
		service.start(confFolder);
		return service;
	}

	/**
	 * 启动模型服务，传入配置文件所在的目录，start将读取配置文件中的[dbname].conf文件 并按照dbname获得数据库
	 * 
	 * @param confFolder
	 *            配置文件的目录名
	 */
	private void start(String confFolder) {
		clients = new ConcurrentHashMap<String, MongoClient>();
		loadDBConf(confFolder);
		loadPMConf(confFolder);
	}

	private void loadPMConf(String confFolder) {
		File file;
		if (confFolder == null) {
			String folderName = System.getProperty("user.dir") //$NON-NLS-1$
					+ File.separator
					+ "visionconf" + File.separator + "pm.conf";//$NON-NLS-1$
			file = new File(folderName);
		} else {
			file = new File(confFolder + File.separator + "pm.conf");
		}

		InputStream is = null;
		FileInputStream fis = null;
		try {
			is = new FileInputStream(file);
			Properties props = new Properties();
			props.load(is);
			URL_PRODUCTNUM = props.getProperty("pm.productnum");//$NON-NLS-1$
			URL_REAUTH = props.getProperty("pm.reauth"); //$NON-NLS-1$
			URL_LINKDELIVERY = props.getProperty("pm.linkdelivery"); //$NON-NLS-1$
			URL_DOCUMENTSERVICE = props.getProperty("pm.documentservice");
			URL_EPMDOCUMENTSERVICE = props.getProperty("pm.epmdocumentservice");
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
	}

	/**
	 * 获得名称为dbname的数据库
	 * 
	 * @param dbname
	 *            数据库名称
	 * @return 数据库
	 */
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

	/**
	 * 获得默认数据库的集合
	 * 
	 * @param collectionName
	 * @return 数据库的集合
	 */
	public DBCollection getCollection(String collectionName) {
		return defaultDB.getCollection(collectionName);
	}

	private void loadDBConf(String confFolder) {
		File folder;
		if (confFolder == null) {
			String folderName = System.getProperty("user.dir") //$NON-NLS-1$
					+ File.separator + "visionconf" + File.separator;//$NON-NLS-1$
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
		if (defaultDB == null) {
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
		//		builder.autoConnectRetry("true".equalsIgnoreCase(props //$NON-NLS-1$
		//				.getProperty("db.options.autoConnectRetry"))); //$NON-NLS-1$
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

	public BasicDocument getBasicDocumentById(String id) throws Exception {
		Class<? extends BasicDocument> clas = getClassBy(id);
		if (clas == null) {
			throw new Exception("无法确定对象的类型");
		}
		DBCollection col = getCollection("document");
		col.setObjectClass(clas);
		BasicDocument result = (BasicDocument) col.findOne(new BasicDBObject()
				.append(BasicDocument._ID, new ObjectId(id)));
		result.setCollection(col);
		return result;
	}

	private Class<? extends BasicDocument> getClassBy(String id) {
		DBCollection col = getCollection("document");
		DBObject data = col
				.findOne(new BasicDBObject().append(BasicDocument._ID,
						new ObjectId(id)), new BasicDBObject().append(
						BasicDocument.PLM_TYPE, 1));
		Object type = data.get(BasicDocument.PLM_TYPE);
		if (PMCADDocument.class.getSimpleName().toLowerCase().equals(type)) {
			return PMCADDocument.class;
		} else if (PMDocument.class.getSimpleName().toLowerCase().equals(type)) {
			return PMDocument.class;
		} else if (PMMaterial.class.getSimpleName().toLowerCase().equals(type)) {
			return PMMaterial.class;
		} else if (PMPart.class.getSimpleName().toLowerCase().equals(type)) {
			return PMPart.class;
		} else if (PMProduct.class.getSimpleName().toLowerCase().equals(type)) {
			return PMProduct.class;
		} else if (PMSupplyment.class.getSimpleName().toLowerCase()
				.equals(type)) {
			return PMSupplyment.class;
		} else if (PMJigTools.class.getSimpleName().toLowerCase().equals(type)) {
			return PMJigTools.class;
		} else if (PMPackage.class.getSimpleName().toLowerCase().equals(type)) {
			return PMPackage.class;
		} else if (PMSupplyment.class.getSimpleName().toLowerCase()
				.equals(type)) {
			return PMSupplyment.class;
		}
		return null;
	}

}
