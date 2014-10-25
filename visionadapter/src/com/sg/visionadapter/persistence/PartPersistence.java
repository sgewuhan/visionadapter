package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.model.Part;

public class PartPersistence extends PersistenceService<Part>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
