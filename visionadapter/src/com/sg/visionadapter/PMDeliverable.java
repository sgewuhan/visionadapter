package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

public class PMDeliverable extends BasicDBObject implements IPersistenceService {

	private static final String DELIVERY_NAME = "deliverable";

	private static final String F_DOCUMENT_ID = "document_id";

	protected DBCollection collection;

	public PMDeliverable() {
	}

	public WriteResult doUpdateDeliverable(String oldDocumentId,
			String newDocumentId) {
		WriteResult result = collection.update(new BasicDBObject().append(
				F_DOCUMENT_ID, oldDocumentId), new BasicDBObject().append(
				"$set", new BasicDBObject().append(F_DOCUMENT_ID, new ObjectId(
						newDocumentId))));
		return result;
	}

	@Override
	public void setCollection(DBCollection col) {
		this.collection = col;
	}

	@Override
	public String getCollectionName() {
		return DELIVERY_NAME;
	}

	public void initDeli(String codebasePath) {
		DBCollection col = ModelServiceFactory.getInstance(codebasePath)
				.getCollection("document");
		col.find(new BasicDBObject().append(
				"IsSync",
				new BasicDBObject().append("$nin", new Boolean[] {
						Boolean.TRUE, Boolean.FALSE, null })));
	}
}
