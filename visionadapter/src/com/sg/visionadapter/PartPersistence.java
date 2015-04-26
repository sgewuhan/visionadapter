package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;


public class PartPersistence extends PersistenceService<PMPart>{

	@Override
	public String getCollectionName() {
		return "document";
	}

	/**
	 * 通过PLMMasterId在PM中查询Part对象
	 * @param plmMasterId
	 * @return
	 */
	public List<PMPart> getPMObjectByPLMMasterId(String plmMasterId) {
		List<PMPart> dbos = new ArrayList<PMPart>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMPart.PLM_MASTER_ID, plmMasterId));
		while(find.hasNext()) {
			PMPart part = (PMPart)find.next();
			part.setCollection(collection);
			dbos.add(part);
		}
		return dbos;
	}
}
