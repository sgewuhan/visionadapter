package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

/**
 * 文件夹对象
 * @author zhonghua
 *
 */
public class Folder extends VisionObject {
	
	private final static String PROJECT_ID = "projectId";

	private final static String PARENT_FOLDER_ID = "parent_id";

	private final static String IS_PROJECT_ROOT_FOLDER = "isflderroot";

	private final static String ROOT_ID = "root_id";

	/**
	 * @return the projectId
	 */
	public ObjectId getProjectId() {
		return (ObjectId) get(PROJECT_ID);
	}

	/**
	 * @param projectId
	 *            the projectId to set
	 */
	public void setProjectId(Object projectId) {
		put(PROJECT_ID, projectId);
	}

	/**
	 * @return the parentFolderId
	 */
	public ObjectId getParentFolderId() {
		return (ObjectId) get(PARENT_FOLDER_ID);
	}

	public Folder getParentFolder() {
		return (Folder) collection.findOne(new BasicDBObject().append(_ID,
				getParentFolderId()));
	}

	/**
	 * @param parentFolderId
	 *            the parentFolderId to set
	 */
	public void setParentFolderId(ObjectId parentFolderId) {
		put(PARENT_FOLDER_ID, parentFolderId);
	}

	/**
	 * 
	 * @return 是否是项目的根文件夹
	 */
	public boolean isProjectContainer() {
		return Boolean.TRUE.equals(get(IS_PROJECT_ROOT_FOLDER));
	}

	/**
	 * 
	 * @param isProjectRoot 设置是否是项目的根文件夹
	 */
	public void setProjectContainer(boolean isProjectRoot) {
		put(IS_PROJECT_ROOT_FOLDER, isProjectRoot);
	}

	/**
	 * 
	 * @return 根文件夹id, （即文件柜/容器id）
	 */
	public ObjectId getRootId() {
		return (ObjectId) get(ROOT_ID);
	}

	/**
	 * @param rootId 根文件夹id, (即文件柜/容器id)
	 */
	public void setRootId(ObjectId rootId) {
		put(ROOT_ID, rootId);
	}

	/**
	 * 
	 * @return 根文件夹
	 */
	public Folder getRoot() {
		return (Folder) collection.findOne(new BasicDBObject().append(_ID,
				getRootId()));
	}

}
