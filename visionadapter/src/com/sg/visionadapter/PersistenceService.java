package com.sg.visionadapter;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

/**
 * �����ĳ־û�����,������T���������������
 * 
 * @author zhonghua
 *
 * @param <T>
 *            �ɳ־û��Ķ��󣬱���̳���{@link VisionObject}
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
	 * ��ѯ���������Ķ������磬��Ҫ��ѯ����Ϊabc��Document���� </br><code>
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
	 *            ��������
	 * @return ���������� �����б�
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
	 *            ��ѯid�ķ�Χ��Ϊ��ʱ��ȫ��
	 * @return ���������Ķ���
	 */
	public List<T> getObjects(ObjectId[] idArray) {
		return getObjects(idArray, false);
	}

	/**
	 * 
	 * @param idArray
	 *            ��ѯ��id��Χ��Ϊ��ʱ��ȫ��
	 * @param asynchronous
	 *            �Ƿ�ֻ��ȡ��ͬ��������
	 * @return ���������Ķ���
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
	 * @param id �����id
	 * @return idΪ��������Ķ���
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
