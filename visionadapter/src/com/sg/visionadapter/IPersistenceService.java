package com.sg.visionadapter;

import com.mongodb.DBCollection;

/**
 * �־û�����ӿ�
 * @author Administrator
 *
 */
public interface IPersistenceService {

	/**
	 * ���ü���
	 * @param col
	 */
	void setCollection(DBCollection col);

	/**
	 * ��ü�������
	 * @return ��������
	 */
	String getCollectionName();
}
