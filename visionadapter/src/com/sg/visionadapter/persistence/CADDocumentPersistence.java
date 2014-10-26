package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PMCADDocument;
import com.sg.visionadapter.PersistenceService;

public class CADDocumentPersistence extends PersistenceService<PMCADDocument>{


	@Override
	public String getCollectionName() {
		return "document";
	}

}
