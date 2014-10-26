package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.PMSupplyment;

public class SupplymentPersistence extends PersistenceService<PMSupplyment>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
