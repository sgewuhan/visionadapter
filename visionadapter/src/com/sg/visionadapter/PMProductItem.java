package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

public class PMProductItem extends BasicDBObject {
 
	private static final String F_ID = "_id";
	private static final String F_DESC = "desc";
	private static final String F_PROJECTID = "project_id";
	protected DBCollection collection;
	
	public PMProductItem() {
		collection = ModelServiceFactory.service.getCollection("productitem");
	}
	
	public WriteResult doInsertProductNumToProductItem(String prductNumber, ObjectId projectId) {
		WriteResult rs = collection.insert(new BasicDBObject().append(F_ID, new ObjectId()).append(F_PROJECTID, projectId).append(F_DESC, prductNumber));
		return rs;
		
	}
}
