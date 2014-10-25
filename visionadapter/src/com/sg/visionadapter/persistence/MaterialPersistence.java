package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.model.Material;

public class MaterialPersistence extends PersistenceService<Material>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
