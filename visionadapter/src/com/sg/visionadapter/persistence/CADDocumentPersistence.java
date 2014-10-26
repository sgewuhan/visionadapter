package com.sg.visionadapter.persistence;

import com.sg.visionadapter.VTCADDocument;
import com.sg.visionadapter.PersistenceService;

public class CADDocumentPersistence extends PersistenceService<VTCADDocument>{


	@Override
	public String getCollectionName() {
		return "document";
	}

}
