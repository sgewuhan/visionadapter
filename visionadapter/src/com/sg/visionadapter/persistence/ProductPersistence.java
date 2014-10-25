package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.model.Product;

public class ProductPersistence extends PersistenceService<Product>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}