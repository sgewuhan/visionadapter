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
	 * ͨ��PLMId��PM�в�ѯCAD����
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
