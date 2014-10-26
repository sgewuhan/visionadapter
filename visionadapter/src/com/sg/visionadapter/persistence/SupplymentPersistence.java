package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.VTSupplyment;

public class SupplymentPersistence extends PersistenceService<VTSupplyment>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
