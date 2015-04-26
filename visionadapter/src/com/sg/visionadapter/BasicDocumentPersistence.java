package com.sg.visionadapter;

import com.mongodb.BasicDBObject;

public class BasicDocumentPersistence extends PersistenceService<BasicDocument> {

	@Override
	public String getCollectionName() {
		return "document";
	}

	/**
	 * 通过PLMMasterId在PM中查询BasicDocument对象
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
