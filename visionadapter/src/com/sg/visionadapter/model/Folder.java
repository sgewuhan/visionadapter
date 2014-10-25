package com.sg.visionadapter.model;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

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

	public boolean isProjectContainer() {
		return Boolean.TRUE.equals(get(IS_PROJECT_ROOT_FOLDER));
	}

	public void setProjectContainer(boolean isProjectRoot) {
		put(IS_PROJECT_ROOT_FOLDER, isProjectRoot);
	}

	public ObjectId getRootId() {
		return (ObjectId) get(ROOT_ID);
	}

	public void setRootId(ObjectId rootId) {
		put(ROOT_ID, rootId);
	}

	public Folder getRoot() {
		return (Folder) collection.findOne(new BasicDBObject().append(_ID,
				getRootId()));
	}

}
