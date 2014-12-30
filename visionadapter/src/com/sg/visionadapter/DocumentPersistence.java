package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class DocumentPersistence extends PersistenceService<PMDocument> {

	@Override
	public String getCollectionName() {
		return "document";
	}

	/**
	 * plm_id为空的文档_id
	 */
	public List<String> getNoSyncdocumentId() {
		List<String> ids = new ArrayList<String>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMDocument.PLM_ID, null));
		while (find.hasNext()) {
			DBObject data = find.next();
			ObjectId id = (ObjectId) data.get(VisionObject._ID);
			String strId = id.toString();
			ids.add(strId);
		}
		return ids;
	}

	/**
	 * 通过pmId查询文档是否存在
	 * @param pmId
	 * @return
	 */
	public Boolean getDocumentById(String pmId) {
		DBObject dbo = collection.findOne(new BasicDBObject().append(PMDocument._ID,
				new ObjectId(pmId)));
		if(dbo != null) {
			return true;
		}
		return false;
	}
}
