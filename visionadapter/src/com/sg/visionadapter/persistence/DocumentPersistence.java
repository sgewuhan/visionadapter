package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PMDocument;
import com.sg.visionadapter.PersistenceService;

public class DocumentPersistence extends PersistenceService<PMDocument>{

	@Override
	public String getCollectionName() {
		return "document";
	}

}
