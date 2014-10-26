package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PMFolder;
import com.sg.visionadapter.PersistenceService;

public class FolderPersistence extends PersistenceService<PMFolder>{

	@Override
	public String getCollectionName() {
		return "folder";
	}

}
