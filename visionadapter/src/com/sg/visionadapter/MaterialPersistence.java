package com.sg.visionadapter;


public class MaterialPersistence extends PersistenceService<PMMaterial>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
