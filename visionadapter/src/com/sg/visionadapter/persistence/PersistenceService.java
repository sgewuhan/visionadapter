package com.sg.visionadapter.persistence;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class PersistenceService implements IPersistenceService {

	private DBCollection col;

	@Override
	public void setCollection(DBCollection col) {
		this.col = col;
	}

	public DBCollection getCollection() {
		return col;
	}

	public List<DBObject> getObjects(ObjectId[] idArray) {
		if (idArray == null || idArray.length == 0) {
			throw new IllegalArgumentException(
					"sidArray parameter must not empty");
		}

		DBCursor cur = col.find(new BasicDBObject().append(
				IPMModelConstants.F_ID,
				new BasicDBObject().append("$in", idArray)));
		return cur.toArray();
	}

	public int updateObject(String sid, DBObject data) {
		WriteResult ws = col.update(new BasicDBObject().append(IPMModelConstants.F_ID,
				new ObjectId(sid)), new BasicDBObject().append("$set", data));
		return ws.getN();
	}
}
