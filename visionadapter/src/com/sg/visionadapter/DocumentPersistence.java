package com.sg.visionadapter;


public class DocumentPersistence extends PersistenceService<PMDocument>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
