package com.sg.visionadapter;


public class ProductPersistence extends PersistenceService<PMProduct>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
