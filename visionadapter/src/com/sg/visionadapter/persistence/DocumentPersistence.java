package com.sg.visionadapter.persistence;

import com.sg.visionadapter.Document;
import com.sg.visionadapter.PersistenceService;

public class DocumentPersistence extends PersistenceService<Document>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
