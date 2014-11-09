package com.sg.visionadapter;


public class PackagePersistence extends PersistenceService<PMPackage>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
