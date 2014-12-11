package com.sg.visionadapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * 保存在GridFSFile中的文件
 * @author zhonghua
 *
 */
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

	@Override
	public String getFileName() {
		return (String) fileData.get(F_FILENAME);
	}


	@Override
	public long write(OutputStream out) throws IOException {
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
		try {
			return result.writeTo(out);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public long write(File file) throws IOException {
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
		return result.writeTo(file);
	}

	private void checkFileData() {
		if (fileData == null) {
			throw new IllegalArgumentException("fileData is null");
		}
	}

	@Override
	public ObjectId writeToGridFS(InputStream in, ObjectId gridfsObjectId,
			String fileName, String namespace, DB db, DBObject metadata)
			throws IOException {
		if (gridfsObjectId == null) {
			gridfsObjectId = new ObjectId();
		}
		GridFS gridfs = new GridFS(db, namespace);
		GridFSInputFile file = gridfs.createFile(in, true);
		file.put("_id", gridfsObjectId); //$NON-NLS-1$
		file.setFilename(fileName);

		if (metadata != null) {
			file.setMetaData(metadata);
		}
		file.save();
		return gridfsObjectId;
	}
}
