package com.sg.visionadapter;

public class ProductItemPersistence extends PersistenceService<PMProductItem> {

	@Override
	public String getCollectionName() {
		return "productitem";
	}

}
