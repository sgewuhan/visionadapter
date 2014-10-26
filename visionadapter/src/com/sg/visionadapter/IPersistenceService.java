package com.sg.visionadapter;

import com.mongodb.DBCollection;

/**
 * 持久化服务接口
 * @author Administrator
 *
 */
public interface IPersistenceService {

	/**
	 * 设置集合
	 * @param col
	 */
	void setCollection(DBCollection col);

	/**
	 * 获得集合名称
	 * @return 集合名称
	 */
	String getCollectionName();
}
