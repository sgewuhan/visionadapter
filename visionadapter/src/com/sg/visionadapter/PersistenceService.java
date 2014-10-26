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
	 * PMDocument doc = new PMDocument();
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
		DBCursor cur = collection.find(obj.append(VisionObject.PLM_TYPE,
				obj.getClass().getSimpleName().toLowerCase()));
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
		return getObjects(idArray, VisionObject._ID, false);
	}

	public List<T> getObjectsByPLMId(String[] plmIdArray) {
		return getObjects(plmIdArray, VisionObject.PLM_ID, false);
	}

	/**
	 * 
	 * @param idArray
	 *            查询的id范围，为空时是全部
	 * @param asynchronous
	 *            是否只获取不同步的数据
	 * @return 符合条件的对象
	 */
	public List<T> getObjects(Object[] idArray, String idKey,
			boolean asynchronous) {
		BasicDBObject condition = new BasicDBObject();
		condition.put(VisionObject.PLM_TYPE, entityClass.getSimpleName()
				.toLowerCase());
		if (idArray != null) {
			condition.put(idKey, new BasicDBObject().append("$in", idArray));
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
	 * @param id
	 *            对象的id
	 * @return id为传入参数的对象
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	public T get(Object id, String fieldName) throws InstantiationException,
			IllegalAccessException {
		if (id == null) {
			throw new IllegalArgumentException("id must not empty");
		}
		T result = (T) collection.findOne(new BasicDBObject().append(fieldName,
				id).append(VisionObject.PLM_TYPE,
				entityClass.getSimpleName().toLowerCase()));
		result.setCollection(collection);
		return result;
	}

	/**
	 * 根据oid 获得对象
	 * 
	 * @param id
	 *            plm对象id
	 * @return PM对象
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public T get(ObjectId id) throws InstantiationException,
			IllegalAccessException {
		return get(id, VisionObject._ID);
	}

	/**
	 * 根据PLM系统的id获得对象
	 * 
	 * @param id
	 *            plm 对象id
	 * @return pm对象
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public T getByPLMId(String id) throws InstantiationException,
			IllegalAccessException {
		return get(id, VisionObject.PLM_ID);
	}

	/**
	 * 获得一个空的可持久化的对象
	 * @return 空的可持久化的对象
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public T newInstance() throws InstantiationException,
			IllegalAccessException {
		T instance = entityClass.newInstance();
		instance.setCollection(collection);
		return instance;
	}
}
