package com.sg.visionadapter;

import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

/**
 * 文件夹对象
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

	public static final String F_IS_CONTAINER = "iscontainer";

	/**
	 * 是否提交，为PLM两段式提交打标记
	 */
	private static final String F_IS_COMMIT = "iscommit";

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
	 * @return 是否是项目的根文件夹
	 */
	public boolean isProjectContainer() {
		return Boolean.TRUE.equals(get(IS_PROJECT_ROOT_FOLDER));
	}

	/**
	 * 
	 * @param isProjectRoot
	 *            设置是否是项目的根文件夹
	 */
	public void setProjectContainer(boolean isProjectRoot) {
		setValue(IS_PROJECT_ROOT_FOLDER, isProjectRoot);
	}

	/**
	 * 
	 * @return 根文件夹id, （即资料库/容器id）
	 */
	public ObjectId getRootId() {
		return (ObjectId) get(ROOT_ID);
	}

	/**
	 * @param rootId
	 *            根文件夹id, (即资料库/容器id)
	 */
	public void setRootId(ObjectId rootId) {
		setValue(ROOT_ID, rootId);
	}

	/**
	 * 
	 * @return 根文件夹
	 */
	public PMFolder getRoot() {
		ObjectId rootId = getRootId();
		rootId = rootId == null ? get_id() : rootId;
		DBCollection folderColletion = ModelServiceFactory.service
				.getCollection("folder");
		folderColletion.setObjectClass(PMFolder.class);
		return (PMFolder) folderColletion.findOne(new BasicDBObject().append(
				_ID, rootId));
	}

	/**
	 * 获得容器名称
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

	@Override
	protected void setEditor() {

	}

	/**
	 * 是否为容器
	 * 
	 * @return
	 */
	public boolean isContainer() {
		return Boolean.TRUE.equals(get(F_IS_CONTAINER));
	}

	public Object getProjectWorkOrder() {
		return get(PROJECT_WORK_ORDER);
	}

	/**
	 * 设置PLM提交的状态
	 * 
	 * @param iscommit
	 *            true or false
	 */
	public void setCommit(boolean iscommit) {
		setValue(F_IS_COMMIT, iscommit);
	}
}
