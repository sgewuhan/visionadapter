package com.sg.visionadapter.persistence;

import com.sg.visionadapter.VTDocument;
import com.sg.visionadapter.PersistenceService;

public class DocumentPersistence extends PersistenceService<VTDocument>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
