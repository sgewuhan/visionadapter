package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class PMOrganization extends BasicDBObject implements
		IPersistenceService {

	public static final String F_FACTORY = "factory";
	public static final String FACTORY_CODE = "code";
	public static final String FACTORY_NAME = "factoryname";
	public static final String collectionName = "organization";

	protected DBCollection collection;

	@SuppressWarnings("unchecked")
	public List<String> getAllFactoryList() {
		List<String> resule = new ArrayList<String>();
		List<DBObject> distinct = collection.distinct(
				F_FACTORY,
				new BasicDBObject().append(F_FACTORY,
						new BasicDBObject().append("$ne", null)));
		for (DBObject dbo : distinct) {
			resule.add((String) dbo.get(FACTORY_CODE));
		}
		return resule;
	}

	@Override
	public String getCollectionName() {
		return collectionName;
	}

	@Override
	public void setCollection(DBCollection col) {
		this.collection = col;
	}
}
