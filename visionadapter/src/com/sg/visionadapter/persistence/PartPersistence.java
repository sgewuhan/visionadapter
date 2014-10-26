package com.sg.visionadapter.persistence;

import com.sg.visionadapter.VTPart;
import com.sg.visionadapter.PersistenceService;

public class PartPersistence extends PersistenceService<VTPart>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
