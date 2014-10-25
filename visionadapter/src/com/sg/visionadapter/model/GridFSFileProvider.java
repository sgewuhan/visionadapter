package com.sg.visionadapter.model;

import com.mongodb.DBObject;

public class GridFSFileProvider implements IFileProvider {

	private DBObject fileData;

	public GridFSFileProvider(DBObject fileData) {
		this.fileData = fileData;
	}

	@Override
	public DBObject getFileData() {
		return fileData;
	}

}
