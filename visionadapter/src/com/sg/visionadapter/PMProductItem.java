package com.sg.visionadapter;

import java.util.Date;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

public class PMProductItem extends BasicDBObject implements IPersistenceService {

	private static final String USERID = "userid";
	private static final String USERNAME = "username";
	private static final String F_CACCOUNT = "_caccount";
	private static final String F_CDATE = "_cdate";
	private static final String F_ID = "_id";
	private static final String F_DESC = "desc";
	private static final String F_PROJECTID = "project_id";
	private static final String PRODUCT_ITEM = "productitem";

	private String userId;
	private String userName;
	protected DBCollection collection;
	private Date date;

	public PMProductItem() {
	}

	public WriteResult doInsertProductNumToProductItem(String prductNumber,
			ObjectId projectId) {
		WriteResult rs = collection.insert(new BasicDBObject()
				.append(F_ID, new ObjectId())
				.append(F_PROJECTID, projectId)
				.append(F_DESC, prductNumber)
				.append(F_CDATE, getDate().toString())
				.append(F_CACCOUNT,
						new BasicDBObject().append(USERID, getUserId()).append(
								USERNAME, getUserName())));
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
