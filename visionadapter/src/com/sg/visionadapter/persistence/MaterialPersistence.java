package com.sg.visionadapter.persistence;

import com.sg.visionadapter.Material;
import com.sg.visionadapter.PersistenceService;

public class MaterialPersistence extends PersistenceService<Material>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
