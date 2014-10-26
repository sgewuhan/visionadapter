package com.sg.visionadapter.persistence;

import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.model.Folder;

public class FolderPersistence extends PersistenceService<Folder>{

	@Override
	public String getCollectionName() {
		return "folder";
	}

}
