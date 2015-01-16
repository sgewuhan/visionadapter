package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
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
		DBCollection docCol = ModelServiceFactory.getInstance(codebasePath)
				.getCollection("document");
		DBCollection deliCol = ModelServiceFactory.getInstance(codebasePath)
				.getCollection("deliverable");
		DBCursor docCursor = docCol.find(new BasicDBObject().append(
				"IsSync",
				new BasicDBObject().append("$nin", new Boolean[] {
						Boolean.TRUE, Boolean.FALSE, null })));
		while (docCursor.hasNext()) {
			DBObject dbo = docCursor.next();
			Object isSync = dbo.get("IsSync");
			Object id = dbo.get("_id");
			DBObject one = docCol.findOne(new BasicDBObject().append("plmid",
					isSync));
			Object idOld = one.get("_id");
			deliCol.update(new BasicDBObject().append("document_id", id),
					new BasicDBObject().append("$set",
							new BasicDBObject().append("document_id", idOld)
									.append("document_idold", id)), false, true);
		}
		docCursor.close();
	}
}
