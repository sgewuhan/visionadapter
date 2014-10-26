package com.sg.visionadapter.persistence;

import com.sg.visionadapter.VTMaterial;
import com.sg.visionadapter.PersistenceService;

public class MaterialPersistence extends PersistenceService<VTMaterial>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
