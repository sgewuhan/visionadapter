package com.sg.visionadapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

public class URLFileProvider implements IFileProvider {

	private String fileName;

	private String url;

	private Long fileSize;

	@Override
	public DBObject getFileData() {
		return new BasicDBObject().append("filename", fileName)
				.append("url", url).append("filesize", fileSize);
	}

	@Override
	public long write(OutputStream out) throws IOException {
		return 0;
	}

	@Override
	public long write(File file) throws IOException {
		return 0;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public ObjectId writeToGridFS(InputStream in, ObjectId gridfsObjectId,
			String fileName, String namespace, DB db, DBObject metadata)
			throws IOException {
		return null;
	}

}
