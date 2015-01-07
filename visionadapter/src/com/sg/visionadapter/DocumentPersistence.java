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
	 * @param pmId
	 * @return
	 */
	public PMDocument getDocumentByPMId(String pmId) {
		 PMDocument pmDocuemnt = (PMDocument) collection.findOne(new BasicDBObject()
				.append(PMDocument._ID, new ObjectId(pmId)));
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
