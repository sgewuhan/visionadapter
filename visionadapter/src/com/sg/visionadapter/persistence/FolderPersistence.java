package com.sg.visionadapter.persistence;

import com.sg.visionadapter.VTFolder;
import com.sg.visionadapter.PersistenceService;

public class FolderPersistence extends PersistenceService<VTFolder>{

	@Override
	public String getCollectionName() {
		return "folder";
	}

}
