package com.sg.visionadapter.persistence;

import com.sg.visionadapter.Folder;
import com.sg.visionadapter.PersistenceService;

public class FolderPersistence extends PersistenceService<Folder>{

	@Override
	public String getCollectionName() {
		return "folder";
	}

}
