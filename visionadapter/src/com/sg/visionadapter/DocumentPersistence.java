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
	 * plm_idΪ�յ��ĵ�_id
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
	 * ͨ��PLMId��PM�в�ѯPMDocument����
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
	 * ͨ��pmId��ѯ�ĵ��Ƿ����
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
	 * ͨ��PMId��ȡPMDocument
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
	 * ���Ԥɾ����document����
	 * 
	 * @return document ����ID����
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
