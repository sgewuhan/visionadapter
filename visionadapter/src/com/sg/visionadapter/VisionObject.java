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
 * visionϵͳ�Ŀɳ־û�����
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
	 * �������ԣ����pm�еı��
	 */
	public ObjectId get_id() {
		return getObjectId(_ID);
	}

	/**
	 * �������ԣ�����pm�еı��
	 */
	public void set_id(ObjectId _id) {
		setValue(_ID, _id);
	}

	/**
	 * �������ԣ����pm�е�����
	 */
	public String getCommonName() {
		return getString(DESC);
	}

	/**
	 * �������ԣ�����pm�е�����
	 */
	public void setCommonName(String desc) {
		setValue(DESC, desc);
	}

	/**
	 * �������ԣ����pm�ж�Ӧ��plmϵͳ�Ķ���id
	 */
	public String getPLMId() {
		return getString(PLM_ID);
	}

	/**
	 * �������ԣ�����pm�ж�Ӧ��plmϵͳ�Ķ���id
	 */
	public void setPLMId(String plmId) {
		setValue(PLM_ID, plmId);
	}

	/**
	 * ���plmϵͳ������
	 * 
	 * @return plmϵͳ������
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
	 * ����plmϵͳ������
	 * 
	 * @param plmData
	 */
	public void setPLMData(Map<String, Object> plmData) {
		setValue(PLM_DATA, new BasicDBObject(plmData));
	}

	/**
	 * ��ô����� userid
	 * 
	 * @return ������ userid
	 */
	public String getCreateByUserId() {
		Object value = get(CREATE_BY);
		if (value instanceof DBObject) {
			return (String) ((DBObject) value).get("userid");
		}
		return null;
	}

	/**
	 * @return �����ߵ�����
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
	 *            ������id
	 * @param userName
	 *            ����������
	 */
	public void setCreateBy(String userId, String userName) {
		setValue(CREATE_BY, new BasicDBObject().append("userid", userId)
				.append("username", userName));
	}

	/**
	 * ��ô���ʱ��
	 * 
	 * @return ����ʱ��
	 */
	public Date getCreateOn() {
		return getDate(CREATE_ON);
	}

	/**
	 * ����޸��� userid
	 * 
	 * @return �޸��� userid
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
	 * @return �޸��ߵ�����
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
	 *            �޸���id
	 * @param userName
	 *            �޸�������
	 */
	public void setModifiedBy(String userId, String userName) {
		setValue(MODIFIED_BY, new BasicDBObject().append("userid", userId)
				.append("username", userName));
	}

	/**
	 * ����޸�ʱ��
	 * 
	 * @return �޸�ʱ��
	 */
	public Date getModifiedOn() {
		return getDate(MODIFIED_ON);
	}

	/**
	 * ���������
	 * 
	 * @return ������userid
	 */
	public String getOwner() {
		return getString(OWNER);
	}

	/**
	 * ����������
	 * 
	 * @param userId
	 *            ������ userid
	 */
	public void setOwner(String userId) {
		setValue(OWNER, userId);
	}

	/*
	 * ������һ��PMͬ����PLM��ʱ�䣬���Ϊ�ձ�ʾû��ͬ��
	 */
	public Date getSyncDate() {
		return (Date) get(SYNC_DATE);
	}

	/*
	 * �Ƿ��Ѿ�ͬ������PMϵͳ
	 */
	public boolean isSync() {
		return getSyncDate() != null;
	}

	/**
	 * ����Ϊ��ͬ��
	 */
	public void setSync() {
		setValue(SYNC_DATE, new Date());
		setValue(SYNC_CODE, SUCCESS);
		setValue(SYNC_MSG, null);
	}

	/**
	 * @param collection
	 *            �������ڵļ��ϣ�һ��������������
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
			throw new Exception("ȱ�ٸ�Ŀ¼");
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
	 * ��PLM��Ԫ����д�����
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
	 * ���¶����¼�����ݿ�
	 * 
	 * @return ���½��
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
	 * ���뵽���ݿ�
	 * 
	 * @return ���
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
	 * �����ݿ���ɾ���ö���
	 * 
	 * @return ɾ�����
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
	 * ���ݴ�����ļ���¼�����ļ��ṩ�߶���
	 * 
	 * @param fileData
	 * @return �ļ��ṩ�߶���
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
