package com.sg.visionadapter;


public class PartPersistence extends PersistenceService<PMPart>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
