package com.sg.visionadapter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

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
	 * @return �ļ�����
	 */
	String getFileName();
	


}
