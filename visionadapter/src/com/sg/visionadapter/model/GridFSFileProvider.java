package com.sg.visionadapter.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.sg.visionadapter.ModelServiceFactory;

public class GridFSFileProvider implements IFileProvider {

	private DBObject fileData;

	private static final String F_FILENAME = "fileName";

	private static final String F_ID = "_id";

	private static final String F_NAMESPACE = "namespace";

	private static final String F_DB = "db";

	public GridFSFileProvider(DBObject fileData) {
		this.fileData = fileData;
	}

	@Override
	public DBObject getFileData() {
		return fileData;
	}

	public String getFileName() {
		return (String) fileData.get(F_FILENAME);
	}

	public InputStream getInputSteam() {
		checkFileData();
		String dbName = (String) fileData.get(F_DB);
		String namespace = (String) fileData.get(F_NAMESPACE);
		ObjectId oid = (ObjectId) fileData.get(F_ID);
		if (dbName == null || namespace == null || oid == null) {
			throw new IllegalArgumentException(
					"fileData does not contain valid file information");
		}
		DB db = ModelServiceFactory.service.getDB(dbName);
		GridFS gridfs = new GridFS(db, namespace);
		GridFSDBFile result = gridfs.find(oid);
		if (result == null) {
			throw new IllegalArgumentException("can not find file in GridFS");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			result.writeTo(out);
			return new ByteArrayInputStream(out.toByteArray());
		} catch (IOException e) {
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
		return null;
	}

	private void checkFileData() {
		if (fileData == null) {
			throw new IllegalArgumentException("fileData is null");
		}
	}

}
