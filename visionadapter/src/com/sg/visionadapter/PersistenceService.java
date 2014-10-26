package com.sg.visionadapter;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

/**
 * 基础的持久化服务,将根据T的类型来反射对象
 * 
 * @author zhonghua
 *
 * @param <T>
 *            可持久化的对象，必须继承于{@link VisionObject}
 */
public abstract class PersistenceService<T extends VisionObject> implements
		IPersistenceService {

	private Class<T> entityClass;

	private DBCollection collection;

	@SuppressWarnings("unchecked")
	protected PersistenceService() {
		entityClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public void setCollection(DBCollection col) {
		this.collection = col;
		this.collection.setObjectClass(entityClass);
	}

	/**
	 * 查询符合条件的对象，例如，需要查询名称为abc的Document对象 </br><code>
	 * Document doc = new Document();
	 * </br>
	 * doc.setCommonName("abc");
	 * </br>
	 * DocumentPersistence docService = ModelServiceFactory.get(DocumentPersistence.class);
	 * </br>
	 * List<document> result = docService.find(doc);
	 * </br>
	 * </br>
	 * </code>
	 * 
	 * 
	 * @param obj
	 *            条件对象
	 * @return 满足条件的 对象列表
	 */
	public List<T> find(T obj) {
		DBCursor cur = collection.find(obj);
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) cur.toArray();
		for (int i = 0; i < result.size(); i++) {
			result.get(i).setCollection(collection);
		}
		cur.close();
		return result;
	}

	/**
	 * 
	 * @param idArray
	 *            查询id的范围，为空时是全部
	 * @return 符合条件的对象
	 */
	public List<T> getObjects(ObjectId[] idArray) {
		return getObjects(idArray, false);
	}

	/**
	 * 
	 * @param idArray
	 *            查询的id范围，为空时是全部
	 * @param asynchronous
	 *            是否只获取不同步的数据
	 * @return 符合条件的对象
	 */
	public List<T> getObjects(ObjectId[] idArray, boolean asynchronous) {
		BasicDBObject condition = new BasicDBObject();
		if (idArray != null) {
			condition.put(VisionObject._ID,
					new BasicDBObject().append("$in", idArray));
		}
		if (asynchronous) {
			condition.put(VisionObject.SYNC_DATE, null);
		}

		DBCursor cur = collection.find(condition);
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) cur.toArray();
		for (int i = 0; i < result.size(); i++) {
			result.get(i).setCollection(collection);
		}
		cur.close();
		return result;
	}

	/**
	 * 
	 * @param id 对象的id
	 * @return id为传入参数的对象
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public T get(ObjectId id) throws InstantiationException,
			IllegalAccessException {
		if (id == null) {
			throw new IllegalArgumentException("id must not empty");
		}
		T result = (T) collection.findOne();
		result.setCollection(collection);
		return result;
	}

}
