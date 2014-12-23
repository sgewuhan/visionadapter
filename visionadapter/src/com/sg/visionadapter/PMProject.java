package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class PMProject extends BasicDBObject implements IPersistenceService {

	private static final String F_PROJECTNUMBER = "projectnumber";
	private static final String F_ID = "_id";
	private static final String PROJETC_ORG = "project";
	private DBCollection collection;

	public PMProject() {
	}

	public ObjectId getProjectIdByProjectNum(String projectNumber) {
		DBObject result = collection.findOne(new BasicDBObject().append(
				F_PROJECTNUMBER, projectNumber));
		if (result != null) {
			ObjectId id = (ObjectId) result.get(F_ID);
			return id;
		}
		return null;
	}

	@Override
	public void setCollection(DBCollection col) {
		this.collection = col;
	}

	@Override
	public String getCollectionName() {
		return PROJETC_ORG;
	}

}
