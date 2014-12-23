package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

public class PMProductItem extends BasicDBObject implements IPersistenceService {

	private static final String F_ID = "_id";
	private static final String F_DESC = "desc";
	private static final String F_PROJECTID = "project_id";
	private static final String PRODUCT_ITEM = "productitem";
	protected DBCollection collection;

	public PMProductItem() {
	}

	public WriteResult doInsertProductNumToProductItem(String prductNumber,
			ObjectId projectId) {
		WriteResult rs = collection.insert(new BasicDBObject()
				.append(F_ID, new ObjectId()).append(F_PROJECTID, projectId)
				.append(F_DESC, prductNumber));
		return rs;

	}

	@Override
	public void setCollection(DBCollection col) {
		this.collection = col;
	}

	@Override
	public String getCollectionName() {
		return PRODUCT_ITEM;
	}
}
