package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.Supplyment;

public class SupplymentPersistence extends PersistenceService<Supplyment>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
