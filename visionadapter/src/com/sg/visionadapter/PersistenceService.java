package com.sg.visionadapter;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.sg.visionadapter.model.VisionObject;

@SuppressWarnings("unchecked")
public abstract class PersistenceService<T extends VisionObject> implements IPersistenceService {

	private Class<T> entityClass;

	private DBCollection collection;

	public PersistenceService() {
		entityClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	public void setCollection(DBCollection col) {
		this.collection = col;
		this.collection.setObjectClass(entityClass);
	}

	/**
	 * 
	 * @param idArray
	 *            查询id的范围，为空时是全部
	 * @return
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
	 * @return
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
		List<T> result = (List<T>) cur.toArray();
		for (int i = 0; i < result.size(); i++) {
			result.get(i).setCollection(collection);
		}
		cur.close();
		return result;
	}

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
