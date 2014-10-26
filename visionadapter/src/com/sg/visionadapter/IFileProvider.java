package com.sg.visionadapter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

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
	 * @return 文件名称
	 */
	String getFileName();
	


}
