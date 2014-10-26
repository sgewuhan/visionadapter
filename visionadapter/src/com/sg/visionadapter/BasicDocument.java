package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * ������ĵ�����
 * 
 * @author zhonghua
 *
 */
public abstract class BasicDocument extends VisionObject {

	protected static final String FOLDER_ID = "folder_id";

	protected static final String DOCUMENTNUMBER = "documentnumber";

	protected static final String MAJOR_VID = "major_vid";

	protected static final String SECOND_VID = "svid";

	protected static final String STATUS = "status";

	protected static final String PHASE = "phase";

	protected static final String DESCRIPTION = "description";

	protected static final String SECURITY = "security";

	/**
	 * ������ڵ�Ŀ¼id
	 * 
	 * @return ���ڵ�Ŀ¼id
	 */
	public ObjectId getFolderId() {
		return (ObjectId) get(FOLDER_ID);
	}

	/**
	 * ���ö������ڵ�Ŀ¼��id
	 * 
	 * @param folderId
	 */
	public void setFolderId(ObjectId folderId) {
		put(FOLDER_ID, folderId);
	}

	/**
	 * ���ö������ڵ�Ŀ¼id
	 * 
	 * @param folderPLMId
	 *            Ŀ¼�����PLM����id
	 */
	public void setFolderIdByPLMId(String folderPLMId) {
		DBCollection folderColletion = ModelServiceFactory.service
				.getCollection("folder");
		DBObject folder = folderColletion.findOne(
				new BasicDBObject().append(Folder.PLM_ID, folderPLMId),
				new BasicDBObject().append(_ID, 1));
		ObjectId folderId = (ObjectId) folder.get(_ID);
		setFolderId(folderId);
	}

	/**
	 * ��ö������ڵ�Ŀ¼����
	 * 
	 * @return �������ڵ�Ŀ¼����
	 */
	public Folder getFolder() {
		DBCollection folderColletion = ModelServiceFactory.service
				.getCollection("folder");
		folderColletion.setObjectClass(Folder.class);
		return (Folder) collection.findOne(new BasicDBObject().append(_ID,
				getFolderId()));
	}

	/**
	 * ��ö���ı�ţ���ͬ������ñ�ŵ����岻ͬ��
	 * 
	 * </br>���磺document���󣬱����������ĵ����
	 * 
	 * </br>part���󣬷������ʱ��
	 * 
	 * </br>CADDocument,����ͼֽ�����ţ�����ͼ�ţ�
	 * 
	 * @return ���
	 */
	public String getObjectNumber() {
		return (String) get(DOCUMENTNUMBER);

	}

	/**
	 * ���ö���ı��
	 * 
	 * @param documentNumber
	 */
	public void setObjectNumber(String documentNumber) {
		put(DOCUMENTNUMBER, documentNumber);
	}

	/**
	 * �����Ҫ�汾�ţ�����A,B,C
	 * 
	 * @return ��Ҫ�汾��
	 */
	public String getMajorVid() {
		return (String) get(MAJOR_VID);
	}

	/**
	 * ������Ҫ�汾��
	 * 
	 * @param majorVid
	 *            ��Ҫ�汾��
	 */
	public void setMajorVid(String majorVid) {
		put(MAJOR_VID, majorVid);
	}

	/**
	 * 
	 * @return ��Ҫ�汾�ţ����� 0,1,2
	 */
	public int getSecondVid() {
		Integer value = (Integer) get(SECOND_VID);
		if (value != null) {
			return value.intValue();
		} else {
			return 0x0;
		}
	}

	/**
	 * ���ô�Ҫ�汾��
	 * 
	 * @param secondVid
	 *            ��Ҫ�汾��
	 */
	public void setSecondVid(int secondVid) {
		put(SECOND_VID, new Integer(secondVid));

	}

	/**
	 * ��ȡ��������״̬����ͬ���͵Ķ�����������״̬��ѡ����ܲ�ͬ
	 * 
	 * @return ��������״̬
	 */
	public String getStatus() {
		return (String) get(STATUS);
	}

	/**
	 * ������������״̬
	 * 
	 * @param status
	 */
	public void setStatus(String status) {
		put(STATUS, status);
	}

	/**
	 * ��ȡ�׶α��
	 * 
	 * @return �׶α��
	 */
	public String getPhase() {
		return (String) get(PHASE);
	}

	/**
	 * ���ý׶α��
	 * 
	 * @param phase
	 */
	public void setPhase(String phase) {
		put(PHASE, phase);
	}

	/**
	 * ��ó��ı�������
	 * 
	 * @return ����
	 */
	public String getDescription() {
		return (String) get(DESCRIPTION);
	}

	/**
	 * ���ó��ı�������
	 * 
	 * @param description
	 *            ����
	 */
	public void setDescription(String description) {
		put(DESCRIPTION, description);
	}

	/**
	 * ��ö�����ܼ�
	 * 
	 * @return �ܼ�
	 */
	public String getSecurity() {
		return (String) get(SECURITY);
	}

	/**
	 * ���ö�����ܼ�
	 * 
	 * @param security
	 */
	public void setSecurity(String security) {
		put(SECURITY, security);
	}

}
