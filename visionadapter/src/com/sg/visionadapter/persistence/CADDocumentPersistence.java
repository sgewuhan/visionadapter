package com.sg.visionadapter.persistence;

import com.sg.visionadapter.CADDocument;
import com.sg.visionadapter.PersistenceService;

public class CADDocumentPersistence extends PersistenceService<CADDocument>{


	@Override
	public String getCollectionName() {
		return "document";
	}

}
