package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.PMProduct;

public class ProductPersistence extends PersistenceService<PMProduct>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
