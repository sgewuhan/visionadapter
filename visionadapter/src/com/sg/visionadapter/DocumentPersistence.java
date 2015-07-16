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
		find.close();
		return ids;
	}

	/**
	 * ͨ��plmMasterId��PM�в�ѯPMDocument����
	 * 
	 * @param plmMasterId
	 * @return
	 */
	public List<PMDocument> getPMObjectByPLMMasterId(String plmMasterId) {
		List<PMDocument> dbos = new ArrayList<PMDocument>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMDocument.PLM_MASTER_ID, plmMasterId));
		while (find.hasNext()) {
			PMDocument doc = (PMDocument) find.next();
			doc.setCollection(collection);
			dbos.add(doc);
		}
		find.close();
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
	 * ͨ��PLMMasterId��PM�в�ѯBasicDocument����
	 * 
	 * @param plmMasterId
	 * @return
	 */
	public boolean hasPMObjectByPLMMasterId(String plmMasterId) {
		long count = collection.count(new BasicDBObject().append(
				PMPart.PLM_MASTER_ID, plmMasterId));
		return count > 0;
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
	public List<String> getDeletedDocumentList(String type) {
		List<String> result = new ArrayList<String>();
		if (type != null) {
			String[] pmtype = null;
			if (PMDocument.PLM_TYPE_EPMDOCUMENT.equals(type)) {
				pmtype = new String[] { PMDocument.PM_TYPE_CAD };
			} else if (PMDocument.PLM_TYPE_DOCUMENT.equals(type)) {
				pmtype = new String[] { PMDocument.PM_TYPE_DOCUMENT };
			} else if (PMDocument.PLM_TYPE_PART.equals(type)) {
				pmtype = new String[] { PMDocument.PM_TYPE_JIGTOOL,
						PMDocument.PM_TYPE_MATERIAL,
						PMDocument.PM_TYPE_PACKAGE, PMDocument.PM_TYPE_PART,
						PMDocument.PM_TYPE_PRODUCT,
						PMDocument.PM_TYPE_SUPPLYMENT };
			}
			if (pmtype != null) {
				List<?> docIdList = collection.distinct(
						PMDocument._ID,
						new BasicDBObject().append(PMDocument.F_DELETED,
								Boolean.TRUE).append(PMDocument.PLM_TYPE,
								new BasicDBObject().append("$in", pmtype)));
				for (Object obj : docIdList) {
					if (obj instanceof ObjectId) {
						result.add(obj.toString());
					}
				}
			}
		}
		return result;
	}

}
