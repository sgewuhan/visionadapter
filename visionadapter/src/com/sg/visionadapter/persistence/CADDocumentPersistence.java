package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.model.CADDocument;

public class CADDocumentPersistence extends PersistenceService<CADDocument>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
