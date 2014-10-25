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
	 * 公共属性：获得pm中的编号
	 */
	public ObjectId get_id() {
		return (ObjectId) get(_ID);
	}

	/**
	 * 公共属性：设置pm中的编号
	 */
	public void set_id(ObjectId _id) {
		put(_ID, _id);
	}

	/**
	 * 公共属性：获得pm中的名称
	 */
	public String getDesc() {
		return (String) get(DESC);
	}

	/**
	 * 公共属性：设置pm中的名称
	 */
	public void setDesc(String desc) {
		put(DESC, desc);
	}

	/**
	 * 公共属性：获得pm中对应的plm系统的对象id
	 */
	public String getPlmId() {
		return (String) get(PLM_ID);
	}

	/**
	 * 公共属性：设置pm中对应的plm系统的对象id
	 */
	public void setPlmId(String plmId) {
		put(DESC, plmId);
	}

	/**
	 * 获得plm系统的数据
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
	 * 设置plm系统的数据
	 * 
	 * @param plmData
	 */
	public void setPlmData(Map<String, Object> plmData) {
		put(PLM_DATA, new BasicDBObject(plmData));
	}

	/**
	 * 获得创建者 userid
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
	 * 设置创建者 userid
	 * 
	 * @param createBy
	 */
	public void setCreateBy(String userId, String userName) {
		put(CREATE_BY,
				new BasicDBObject().append("userid", userId).append("username",
						userName));
	}

	/**
	 * 获得创建时间
	 * 
	 * @return
	 */
	public Date getCreateOn() {
		return (Date) get(CREATE_ON);
	}

	/**
	 * 获得创建者 userid
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
	 * 设置创建者 userid
	 * 
	 * @param createBy
	 */
	public void setModifiedBy(String userId, String userName) {
		put(MODIFIED_BY,
				new BasicDBObject().append("userid", userId).append("username",
						userName));
	}

	/**
	 * 获得创建时间
	 * 
	 * @return
	 */
	public Date getModifiedOn() {
		return (Date) get(MODIFIED_ON);
	}

	/**
	 * 获得所有者
	 * 
	 * @return
	 */
	public String getOwner() {
		return (String) get(OWNER);
	}

	/**
	 * 设置所有者
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
	 * 将PLM的元数据写入对象
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
