package com.sg.visionadapter;


public class CADDocumentPersistence extends PersistenceService<PMCADDocument>{


	@Override
	public String getCollectionName() {
		return "document";
	}

}
