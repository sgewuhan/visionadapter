package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class PMOrganization extends BasicDBObject {

	public static final String F_FACTORY = "factory";
	public static final String FACTORY_CODE = "code";
	public static final String FACTORY_NAME = "factoryname";

	protected DBCollection collection;

	public PMOrganization() {
		collection = ModelServiceFactory.service.getCollection("organization");
	}

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
}
