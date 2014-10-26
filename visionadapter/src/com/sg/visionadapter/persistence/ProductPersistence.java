package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.VTProduct;

public class ProductPersistence extends PersistenceService<VTProduct>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
