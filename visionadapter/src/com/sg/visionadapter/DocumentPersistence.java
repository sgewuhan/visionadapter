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
	 * 通过PLMId在PM中查询PMDocument对象
	 * @param PLMId
	 * @return
	 */
	public List<PMDocument> getPMObjectByPLMId(String PLMId) {
		List<PMDocument> dbos = new ArrayList<PMDocument>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMDocument.PLM_ID, PLMId));
		while(find.hasNext()) {
			PMDocument doc = (PMDocument)find.next();
			doc.setCollection(collection);
			dbos.add(doc);
		}
		return dbos;

	}

	/**
	 * 通过pmId查询文档是否存在
	 * 
	 * @param pmId
	 * @return
	 */
	public Boolean getDocumentById(String pmId) {
		DBObject dbo = collection.findOne(new BasicDBObject().append(
				PMDocument._ID, new ObjectId(pmId)));
		if (dbo != null) {
			return true;
		}
		return false;
	}

	/**
	 * 通过PMId获取PMDocument
	 * 
	 * @param pmId
	 * @return
	 */
	public PMDocument getDocumentByPMId(String pmId) {
		PMDocument pmDocuemnt = (PMDocument) collection
				.findOne(new BasicDBObject().append(PMDocument._ID,
						new ObjectId(pmId)));
		if (pmDocuemnt != null) {
			pmDocuemnt.setCollection(collection);
		}
		return pmDocuemnt;
	}

	/**
	 * 获得预删除的document对象
	 * 
	 * @return document 对象ID集合
	 */
	public List<String> getDeletedDocumentList() {
		List<String> result = new ArrayList<String>();
		List<?> docIdList = collection.distinct(PMDocument._ID,
				new BasicDBObject().append(PMDocument.F_DELETED, Boolean.TRUE));
		for (Object obj : docIdList) {
			if (obj instanceof ObjectId) {
				result.add(obj.toString());
			}
		}
		return result;
	}

}
