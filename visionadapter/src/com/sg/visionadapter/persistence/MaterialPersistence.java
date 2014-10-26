package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PMMaterial;
import com.sg.visionadapter.PersistenceService;

public class MaterialPersistence extends PersistenceService<PMMaterial>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
