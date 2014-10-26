package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PMPart;
import com.sg.visionadapter.PersistenceService;

public class PartPersistence extends PersistenceService<PMPart>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
