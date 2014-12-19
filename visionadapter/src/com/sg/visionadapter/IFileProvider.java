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
	 * @return 文件数据
	 */
	DBObject getFileData();

	/**
	 * 
	 * 例如: <code>
	 * 
	 * ...<br/>
	 * IFileProvider provider = document.getContent();
	 * <br/>
	 * ByteArrayOutputStream out = new ByteArrayOutputStream();
	 * <br/>
	 * provider.write(out);
	 * <br/>
	 * </code> out在写完后会被自动关闭
	 * 
	 * @param out
	 * @return 写入的字节数
	 * @throws IOException
	 */
	long write(OutputStream out) throws IOException;

	/**
	 * 
	 * 例如: <code>
	 * 
	 * ...<br/>
	 * IFileProvider provider = document.getContent();<br/>
	 * provider.write(new File("c:/1.pdf"));<br/>
	 * 
	 * </code> out在写完后会被自动关闭
	 * 
	 * @param file
	 * @return 写入的字节数
	 * @throws IOException
	 */
	long write(File file) throws IOException;

	
	
	/**
	 * 文件写到GridFS
	 * @param in 字节流
	 * @param gridfsObjectId _id
	 * @param fileName 文件名
	 * @param namespace 命名空间
	 * @param db 数据库
	 * @param metadata 默认为空，可以自己定义为任意BSON对象
	 * @return
	 * @throws IOException
	 */
	ObjectId writeToGridFS(InputStream in, ObjectId gridfsObjectId,
			String fileName, String namespace, DB db, DBObject metadata) throws IOException;
	
	
	/**
	 * @return 文件名称
	 */
	String getFileName();
	


}
