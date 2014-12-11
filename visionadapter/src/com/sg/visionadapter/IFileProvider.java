package com.sg.visionadapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBObject;

public interface IFileProvider {
	
	/**
	 * 
	 * @return �ļ�����
	 */
	DBObject getFileData();

	/**
	 * 
	 * ����: <code>
	 * 
	 * ...<br/>
	 * IFileProvider provider = document.getContent();
	 * <br/>
	 * ByteArrayOutputStream out = new ByteArrayOutputStream();
	 * <br/>
	 * provider.write(out);
	 * <br/>
	 * </code> out��д���ᱻ�Զ��ر�
	 * 
	 * @param out
	 * @return д����ֽ���
	 * @throws IOException
	 */
	long write(OutputStream out) throws IOException;

	/**
	 * 
	 * ����: <code>
	 * 
	 * ...<br/>
	 * IFileProvider provider = document.getContent();<br/>
	 * provider.write(new File("c:/1.pdf"));<br/>
	 * 
	 * </code> out��д���ᱻ�Զ��ر�
	 * 
	 * @param file
	 * @return д����ֽ���
	 * @throws IOException
	 */
	long write(File file) throws IOException;

	
	
	/**
	 * �ļ�д��GridFS
	 * @param in �ֽ���
	 * @param gridfsObjectId _id
	 * @param fileName �ļ���
	 * @param namespace �����ռ�
	 * @param db ���ݿ�
	 * @param metadata Ĭ��Ϊ�գ������Լ�����Ϊ����BSON����
	 * @return
	 * @throws IOException
	 */
	ObjectId writeToGridFS(InputStream in, ObjectId gridfsObjectId,
			String fileName, String namespace, DB db, DBObject metadata) throws IOException;
	
	
	/**
	 * @return �ļ�����
	 */
	String getFileName();
	


}
