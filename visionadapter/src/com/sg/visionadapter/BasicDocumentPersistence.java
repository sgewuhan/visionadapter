package com.sg.visionadapter;

import com.mongodb.BasicDBObject;

public class BasicDocumentPersistence extends PersistenceService<BasicDocument> {

	@Override
	public String getCollectionName() {
		return "document";
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
}
