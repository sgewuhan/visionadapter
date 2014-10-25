package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.model.Document;

public class DocumentPersistence extends PersistenceService<Document>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
