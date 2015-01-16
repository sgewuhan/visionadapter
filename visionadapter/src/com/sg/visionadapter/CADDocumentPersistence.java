package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class CADDocumentPersistence extends PersistenceService<PMCADDocument> {

	@Override
	public String getCollectionName() {
		return "document";
	}

	/**
	 * 通过PLMId在PM中查询CAD对象
	 * 
	 * @param PLMId
	 * @return
	 */
	public List<PMCADDocument> getPMObjectByPLMId(String PLMId) {
		List<PMCADDocument> dbos = new ArrayList<PMCADDocument>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMCADDocument.PLM_ID, PLMId));
		while (find.hasNext()) {
			PMCADDocument cad = (PMCADDocument) find.next();
			cad.setCollection(collection);
			dbos.add(cad);
		}
		return dbos;
	}

	public Map<String, String> getInitDocumentList() {
		Map<String, String> result = new HashMap<String, String>();
		DBCursor dbCursor = collection
				.find(new BasicDBObject().append(
						"plmtype",
						new BasicDBObject().append("$ne",
								PMDocument.PM_TYPE_DOCUMENT)));
		while (dbCursor.hasNext()) {
			DBObject dbo = dbCursor.next();
			String plmid = (String) dbo.get("plmid");
			String plmtype = (String) dbo.get("plmtype");
			if (plmtype != null && plmid != null) {
				if (plmtype.equals(PMDocument.PM_TYPE_CAD)) {
					result.put(plmid, PMDocument.PLM_TYPE_EPMDOCUMENT);
				} else if (plmtype.equals(PMDocument.PM_TYPE_DOCUMENT)) {
					result.put(plmid, PMDocument.PLM_TYPE_DOCUMENT);
				} else if (plmtype.equals(PMDocument.PM_TYPE_JIGTOOL)) {
					result.put(plmid, PMDocument.PM_TYPE_JIGTOOL);
				} else if (plmtype.equals(PMDocument.PM_TYPE_MATERIAL)) {
					result.put(plmid, PMDocument.PM_TYPE_MATERIAL);
				} else if (plmtype.equals(PMDocument.PM_TYPE_PACKAGE)) {
					result.put(plmid, PMDocument.PM_TYPE_PACKAGE);
				} else if (plmtype.equals(PMDocument.PM_TYPE_PART)) {
					result.put(plmid, PMDocument.PM_TYPE_PART);
				} else if (plmtype.equals(PMDocument.PM_TYPE_PRODUCT)) {
					result.put(plmid, PMDocument.PM_TYPE_PRODUCT);
				} else if (plmtype.equals(PMDocument.PM_TYPE_SUPPLYMENT)) {
					result.put(plmid, PMDocument.PM_TYPE_SUPPLYMENT);
				}
			}
		}
		dbCursor.close();
		return result;

	}
}
