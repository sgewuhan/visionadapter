package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;


public class CADDocumentPersistence extends PersistenceService<PMCADDocument>{


	@Override
	public String getCollectionName() {
		return "document";
	}

	/**
	 * 通过PLMId在PM中查询CAD对象
	 * @param PLMId
	 * @return
	 */
	public List<PMCADDocument> getPMObjectByPLMId(String PLMId) {
		List<PMCADDocument> dbos = new ArrayList<PMCADDocument>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMCADDocument.PLM_ID, PLMId));
		while(find.hasNext()) {
			PMCADDocument cad = (PMCADDocument)find.next();
			cad.setCollection(collection);
			dbos.add(cad);
		}
		return dbos;
	}
}
