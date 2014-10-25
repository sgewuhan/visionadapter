package com.sg.visionadapter.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class VisionObject extends BasicDBObject {

	public static final String _ID = "_id";

	protected static final String DESC = "desc";

	protected static final String PLM_ID = "plmId";

	protected static final String PLM_DATA = "plmData";

	protected static final String CREATE_BY = "_caccount";

	protected static final String CREATE_ON = "_cdate";

	protected static final String MODIFIED_BY = "_maccount";

	protected static final String MODIFIED_ON = "_mdate";

	protected static final String OWNER = "_owner";

	public static final String SYNC_DATE = "syncdate";

	protected static final String PLM_VAULT = "plmvault";

	protected static final String PLM_CONTENT = "plmcontentvault";

	protected DBCollection collection;

	protected Set<String> dirtyKeys;

	/**
	 * �������ԣ����pm�еı��
	 */
	public ObjectId get_id() {
		return (ObjectId) get(_ID);
	}

	/**
	 * �������ԣ�����pm�еı��
	 */
	public void set_id(ObjectId _id) {
		put(_ID, _id);
	}

	/**
	 * �������ԣ����pm�е�����
	 */
	public String getDesc() {
		return (String) get(DESC);
	}

	/**
	 * �������ԣ�����pm�е�����
	 */
	public void setDesc(String desc) {
		put(DESC, desc);
	}

	/**
	 * �������ԣ����pm�ж�Ӧ��plmϵͳ�Ķ���id
	 */
	public String getPlmId() {
		return (String) get(PLM_ID);
	}

	/**
	 * �������ԣ�����pm�ж�Ӧ��plmϵͳ�Ķ���id
	 */
	public void setPlmId(String plmId) {
		put(DESC, plmId);
	}

	/**
	 * ���plmϵͳ������
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> getPlmData() {
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
	public void setPlmData(Map<String, Object> plmData) {
		put(PLM_DATA, new BasicDBObject(plmData));
	}

	/**
	 * ��ô����� userid
	 * 
	 * @return
	 */
	public String getCreateByUserId() {
		Object value = get(CREATE_BY);
		if (value instanceof DBObject) {
			((DBObject) value).get("userid");
		}
		return null;
	}

	public String getCreateByUserName() {
		Object value = get(CREATE_BY);
		if (value instanceof DBObject) {
			((DBObject) value).get("username");
		}
		return null;
	}

	/**
	 * ���ô����� userid
	 * 
	 * @param createBy
	 */
	public void setCreateBy(String userId, String userName) {
		put(CREATE_BY,
				new BasicDBObject().append("userid", userId).append("username",
						userName));
	}

	/**
	 * ��ô���ʱ��
	 * 
	 * @return
	 */
	public Date getCreateOn() {
		return (Date) get(CREATE_ON);
	}

	/**
	 * ��ô����� userid
	 * 
	 * @return
	 */
	public String getModifiedUserId() {
		Object value = get(MODIFIED_BY);
		if (value instanceof DBObject) {
			((DBObject) value).get("userid");
		}
		return null;
	}

	public String getModifiedByUserName() {
		Object value = get(MODIFIED_BY);
		if (value instanceof DBObject) {
			((DBObject) value).get("username");
		}
		return null;
	}

	/**
	 * ���ô����� userid
	 * 
	 * @param createBy
	 */
	public void setModifiedBy(String userId, String userName) {
		put(MODIFIED_BY,
				new BasicDBObject().append("userid", userId).append("username",
						userName));
	}

	/**
	 * ��ô���ʱ��
	 * 
	 * @return
	 */
	public Date getModifiedOn() {
		return (Date) get(MODIFIED_ON);
	}

	/**
	 * ���������
	 * 
	 * @return
	 */
	public String getOwner() {
		return (String) get(OWNER);
	}

	/**
	 * ����������
	 * 
	 * @param owner
	 */
	public void setOwner(String owner) {
		put(OWNER, owner);
	}

	public void setPLMAttachments(List<IFileProvider> plmVault) {
		BasicBSONList list = new BasicDBList();
		if (plmVault != null) {
			for (int i = 0; i < plmVault.size(); i++) {
				list.add(((IFileProvider) plmVault.get(i)).getFileData());
			}
		}
		put(PLM_VAULT, list);
	}

	public void setPLMContent(IFileProvider fileProvider) {
		if (fileProvider != null) {
			put(PLM_CONTENT, fileProvider.getFileData());
		} else {
			put(PLM_CONTENT, null);
		}
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

	private void setSyncDate(Date date) {
		put(SYNC_DATE, date);
	}

	public void setCollection(DBCollection collection) {
		this.collection = collection;
	}

	public WriteResult doInsert() throws Exception {
		setSyncDate(new Date());
		extendPLMData();
		WriteResult wr = collection.insert(this);
		return wr;
	}

	/**
	 * ��PLM��Ԫ����д�����
	 */
	private void extendPLMData() {
		Map<String, Object> data = getPlmData();
		Iterator<java.util.Map.Entry<String, Object>> iter = data.entrySet()
				.iterator();
		while (iter.hasNext()) {
			java.util.Map.Entry<String, Object> entry = iter.next();
			put("_plm_" + entry.getKey(), entry.getValue());
		}
	}

	public WriteResult doUpdate() throws Exception {
		if (dirtyKeys == null || dirtyKeys.isEmpty()) {
			throw new Exception("no change");
		}
		setSyncDate(new Date());
		extendPLMData();
		BasicDBObject set = new BasicDBObject();
		Iterator<String> iter = dirtyKeys.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			set.put(key, get(key));
		}
		return collection.update(new BasicDBObject().append(_ID, get_id()),
				new BasicDBObject("$set", set));
	}

	public WriteResult doRemove() throws Exception {
		return collection.remove(new BasicDBObject().append(_ID, get_id()));
	}

	@Override
	public Object put(String key, Object val) {
		if (dirtyKeys == null) {
			dirtyKeys = new HashSet<String>();
		}
		dirtyKeys.add(key);
		return super.put(key, val);
	}

	protected IFileProvider getFileProvider(DBObject fileData) {
		return new GridFSFileProvider(fileData);
	}
}
