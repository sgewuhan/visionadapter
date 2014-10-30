package com.sg.visionadapter;


public class SupplymentPersistence extends PersistenceService<PMSupplyment>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
