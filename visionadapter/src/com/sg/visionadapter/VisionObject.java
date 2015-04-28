package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 * vision系统的可持久化对象
 * 
 * @author zhonghua
 *
 */
public abstract class VisionObject extends BasicDBObject implements
		IProjectRelative {

	protected static final String PLM_TYPE = "plmtype";

	protected static final String _ID = "_id";

	protected static final String DESC = "desc";

	public static final String PLM_ID = "plmid";

	protected static final String PLM_DATA = "plmdata";

	protected static final String CREATE_BY = "_caccount";

	protected static final String CREATE_ON = "_cdate";

	protected static final String MODIFIED_BY = "_maccount";

	protected static final String MODIFIED_ON = "_mdate";

	protected static final String OWNER = "owner";

	public static final String SYNC_DATE = "syncdate";

	public static final String SYNC_CODE = "synccode";

	public static final String SYNC_MSG = "syncmsg";

	public static final String EDITOR = "_editor";

	public static final int SUCCESS = 0;

	public static final int INSERT_ERR = 1;

	public static final int UPDATE_ERR = 2;

	public static final int DELETE_ERR = 3;

	protected DBCollection collection;

	protected Set<String> dirtyKeys;

	/**
	 * 公共属性：获得pm中的编号
	 */
	public ObjectId get_id() {
		return getObjectId(_ID);
	}

	/**
	 * 公共属性：设置pm中的编号
	 */
	public void set_id(ObjectId _id) {
		setValue(_ID, _id);
	}

	/**
	 * 公共属性：获得pm中的名称
	 */
	public String getCommonName() {
		return getString(DESC);
	}

	/**
	 * 公共属性：设置pm中的名称
	 */
	public void setCommonName(String desc) {
		setValue(DESC, desc);
	}

	/**
	 * 公共属性：获得pm中对应的plm系统的对象id
	 */
	public String getPLMId() {
		return getString(PLM_ID);
	}

	/**
	 * 公共属性：设置pm中对应的plm系统的对象id
	 */
	public void setPLMId(String plmId) {
		setValue(PLM_ID, plmId);
	}

	/**
	 * 获得plm系统的数据
	 * 
	 * @return plm系统的数据
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPLMData() {
		Object value = get(PLM_DATA);
		if (value instanceof DBObject) {
			return ((DBObject) value).toMap();
		}
		return null;
	}

	/**
	 * 设置plm系统的数据
	 * 
	 * @param plmData
	 */
	public void setPLMData(Map<String, Object> plmData) {
		setValue(PLM_DATA, new BasicDBObject(plmData));
	}

	/**
	 * 获得创建者 userid
	 * 
	 * @return 创建者 userid
	 */
	public String getCreateByUserId() {
		Object value = get(CREATE_BY);
		if (value instanceof DBObject) {
			return (String) ((DBObject) value).get("userid");
		}
		return null;
	}

	/**
	 * @return 创建者的姓名
	 */
	public String getCreateByUserName() {
		Object value = get(CREATE_BY);
		if (value instanceof DBObject) {
			return (String) ((DBObject) value).get("username");
		}
		return null;
	}

	/**
	 * 
	 * @param userId
	 *            创建者id
	 * @param userName
	 *            创建者姓名
	 */
	public void setCreateBy(String userId, String userName) {
		setValue(CREATE_BY, new BasicDBObject().append("userid", userId)
				.append("username", userName));
	}

	/**
	 * 获得创建时间
	 * 
	 * @return 创建时间
	 */
	public Date getCreateOn() {
		return getDate(CREATE_ON);
	}

	/**
	 * 获得修改者 userid
	 * 
	 * @return 修改者 userid
	 */
	public String getModifiedUserId() {
		Object value = get(MODIFIED_BY);
		if (value instanceof DBObject) {
			((DBObject) value).get("userid");
		}
		return null;
	}

	/**
	 * 
	 * @return 修改者的姓名
	 */
	public String getModifiedByUserName() {
		Object value = get(MODIFIED_BY);
		if (value instanceof DBObject) {
			((DBObject) value).get("username");
		}
		return null;
	}

	/**
	 * 
	 * @param userId
	 *            修改者id
	 * @param userName
	 *            修改者姓名
	 */
	public void setModifiedBy(String userId, String userName) {
		setValue(MODIFIED_BY, new BasicDBObject().append("userid", userId)
				.append("username", userName));
	}

	/**
	 * 获得修改时间
	 * 
	 * @return 修改时间
	 */
	public Date getModifiedOn() {
		return getDate(MODIFIED_ON);
	}

	/**
	 * 获得所有者
	 * 
	 * @return 所有者userid
	 */
	public String getOwner() {
		return getString(OWNER);
	}

	/**
	 * 设置所有者
	 * 
	 * @param userId
	 *            所有者 userid
	 */
	public void setOwner(String userId) {
		setValue(OWNER, userId);
	}

	/*
	 * 获得最后一次PM同步到PLM的时间，如果为空表示没有同步
	 */
	public Date getSyncDate() {
		return (Date) get(SYNC_DATE);
	}

	/*
	 * 是否已经同步到了PM系统
	 */
	public boolean isSync() {
		return getSyncDate() != null;
	}

	/**
	 * 设置为已同步
	 */
	public void setSync() {
		setValue(SYNC_DATE, new Date());
		setValue(SYNC_CODE, SUCCESS);
		setValue(SYNC_MSG, null);
	}

	/**
	 * @param collection
	 *            对象所在的集合，一般情况下无须调用
	 */
	void setCollection(DBCollection collection) {
		this.collection = collection;
	}

	public WriteResult doInsertSimple() throws Exception {
		WriteResult wr = collection.insert(this);
		return wr;
	}

	private void extendProjectData() throws Exception {
		PMFolder folder = getParentFolder();
		if (folder == null) {
			throw new Exception("缺少父目录");
		}
		setProjectId(folder.getProjectId());
		setProjectName(folder.getProjectName());
		setProjectNumber(folder.getProjectNumber());
		setProjectWorkOrder(folder.getProjectWorkOrder());
	}

	public WriteResult doSetErrorMessage(int code, String message)
			throws Exception {
		BasicDBObject set = new BasicDBObject();
		set.put(SYNC_DATE, null);
		set.put(SYNC_CODE, code);
		set.put(SYNC_MSG, message);

		return collection.update(new BasicDBObject().append(_ID, get_id()),
				new BasicDBObject("$set", set));
	}

	private void setPLMType() {
		setValue(PLM_TYPE, getClass().getSimpleName().toLowerCase());
	}

	public String getPLMType() {
		return getClass().getSimpleName().toLowerCase();
	}

	/**
	 * 将PLM的元数据写入对象
	 */
	private void extendPLMData() {
		Map<String, Object> data = getPLMData();
		Iterator<java.util.Map.Entry<String, Object>> iter = data.entrySet()
				.iterator();
		while (iter.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iter.next();
			put("_plm_" + entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 更新对象记录到数据库
	 * 
	 * @return 更新结果
	 * @throws Exception
	 */
	public WriteResult doUpdate() throws Exception {
		if (dirtyKeys == null || dirtyKeys.isEmpty()) {
			return null;
		}
		checkUpdate();
		setSync();
		extendPLMData();
		BasicDBObject set = new BasicDBObject();
		Iterator<String> iter = dirtyKeys.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (key.equals(PLM_TYPE)) {
				continue;
			}
			set.put(key, get(key));
		}
		return collection.update(new BasicDBObject().append(_ID, get_id()),
				new BasicDBObject("$set", set));
	}

	public void initPlmData() {
		setEditor();
		setPLMType();
		setSync();
		extendPLMData();
		extendProjectData();
	}

	protected abstract void setEditor();

	/**
	 * 插入到数据库
	 * 
	 * @return 结果
	 * @throws Exception
	 */
	public WriteResult doInsert() throws Exception {
		checkInsert();
		setPLMType();
		setSync();
		extendPLMData();
		extendProjectData();
		WriteResult wr = collection.insert(this);
		return wr;
	}

	/**
	 * 在数据库中删除该对象
	 * 
	 * @return 删除结果
	 * @throws Exception
	 */
	public WriteResult doRemove() throws Exception {
		checkRemove();
		return collection.remove(new BasicDBObject().append(_ID, get_id()));
	}

	protected void checkInsert() {
		checkMondatory();
	}

	private void checkMondatory() {
		List<String> fields = getMondatoryFields();
		for (int i = 0; i < fields.size(); i++) {
			String field = fields.get(i);
			Object value = get(field);
			if (value == null || value.toString().isEmpty()) {
				throw new IllegalArgumentException(
						field
								+ " must not null or empty before insert or update, call setXXX(xxx) before insert or update.");
			}
		}
	}

	protected List<String> getMondatoryFields() {
		List<String> fields = new ArrayList<String>();
		fields.add(_ID);
		fields.add(PLM_ID);
		return fields;
	}

	protected void checkUpdate() {
		checkInsert();
	}

	protected void checkRemove() {
		ObjectId id = get_id();
		if (id == null) {
			throw new IllegalArgumentException(
					"id must not null before remove, call set_id(xxx) before remove.");
		}
	}

	final public Object setValue(String key, Object val) {
		if (dirtyKeys == null) {
			dirtyKeys = new HashSet<String>();
		}
		dirtyKeys.add(key);
		return put(key, val);
	}

	/**
	 * 根据传入的文件记录返回文件提供者对象
	 * 
	 * @param fileData
	 * @return 文件提供者对象
	 */
	protected IFileProvider getGridFSFileProvider(DBObject fileData) {
		return new GridFSFileProvider(fileData);
	}

	public abstract PMFolder getParentFolder();

	@Override
	public ObjectId getProjectId() {
		return (ObjectId) get(PROJECT_ID);
	}

	@Override
	public void setProjectId(ObjectId projectId) {
		setValue(PROJECT_ID, projectId);
	}

	@Override
	public String getProjectName() {
		return (String) get(PROJECT_DESC);
	}

	@Override
	public void setProjectName(String projectDesc) {
		setValue(PROJECT_DESC, projectDesc);
	}

	@Override
	public String getProjectNumber() {
		return (String) get(PROJECT_NUMBER);
	}

	@Override
	public void setProjectNumber(String projectNumber) {
		setValue(PROJECT_NUMBER, projectNumber);
	}

	public void setProjectWorkOrder(Object projectWorkOrder) {
		setValue(PROJECT_WORK_ORDER, projectWorkOrder);
	}

	public String serialize() {
		return JSON.serialize(this);
	}
}
