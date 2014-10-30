package com.sg.visionadapter;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

/**
 * �ļ��ж���
 * 
 * @author zhonghua
 *
 */
public class PMFolder extends VisionObject {

	private final static String PROJECT_ID = "projectId";

	private final static String PARENT_FOLDER_ID = "parent_id";

	private final static String IS_PROJECT_ROOT_FOLDER = "isflderroot";

	private final static String ROOT_ID = "root_id";

	private static final String CONTAINER_NAME = "containername";

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
		setValue(PROJECT_ID, projectId);
	}

	/**
	 * @return the parentFolderId
	 */
	public ObjectId getParentFolderId() {
		return (ObjectId) get(PARENT_FOLDER_ID);
	}

	public PMFolder getParentFolder() {
		return (PMFolder) collection.findOne(new BasicDBObject().append(_ID,
				getParentFolderId()));
	}

	/**
	 * @param parentFolderId
	 *            the parentFolderId to set
	 */
	public void setParentFolderId(ObjectId parentFolderId) {
		setValue(PARENT_FOLDER_ID, parentFolderId);
	}

	/**
	 * 
	 * @return �Ƿ�����Ŀ�ĸ��ļ���
	 */
	public boolean isProjectContainer() {
		return Boolean.TRUE.equals(get(IS_PROJECT_ROOT_FOLDER));
	}

	/**
	 * 
	 * @param isProjectRoot
	 *            �����Ƿ�����Ŀ�ĸ��ļ���
	 */
	public void setProjectContainer(boolean isProjectRoot) {
		setValue(IS_PROJECT_ROOT_FOLDER, isProjectRoot);
	}

	/**
	 * 
	 * @return ���ļ���id, �����ļ���/����id��
	 */
	public ObjectId getRootId() {
		return (ObjectId) get(ROOT_ID);
	}

	/**
	 * @param rootId
	 *            ���ļ���id, (���ļ���/����id)
	 */
	public void setRootId(ObjectId rootId) {
		setValue(ROOT_ID, rootId);
	}

	/**
	 * 
	 * @return ���ļ���
	 */
	public PMFolder getRoot() {
		ObjectId rootId = getRootId();
		rootId = rootId==null?get_id():rootId;
		DBCollection folderColletion = ModelServiceFactory.service
				.getCollection("folder");
		folderColletion.setObjectClass(PMFolder.class);
		setCollection(folderColletion);
		return (PMFolder) collection.findOne(new BasicDBObject().append(_ID,
				rootId));
	}

	/**
	 * �����������
	 * 
	 * @return
	 */
	public String getContainerName() {
		
		PMFolder root = getRoot();
		return root.getString(CONTAINER_NAME);
	}

	@Override
	protected List<String> getMondatoryFields() {
		List<String> result = super.getMondatoryFields();
		result.add(PARENT_FOLDER_ID);
		result.add(ROOT_ID);
		return result;
	}

	@Override
	public WriteResult doInsert() throws Exception {
		throw new Exception("PM Document can not insert by adapter.");
	}
}
