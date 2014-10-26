package com.sg.visionadapter.persistence;

import com.sg.visionadapter.Part;
import com.sg.visionadapter.PersistenceService;

public class PartPersistence extends PersistenceService<Part>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
