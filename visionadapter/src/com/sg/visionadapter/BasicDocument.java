package com.sg.visionadapter;

import java.util.List;

import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
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

	protected static final String CONTENTMD5 = "contentmd5";

	protected static final String FOLDER_ID = "folder_id";

	protected static final String DOCUMENTNUMBER = "documentnumber";

	protected static final String MAJOR_VID = "major_vid";

	protected static final String SECOND_VID = "svid";

	protected static final String STATUS = "status";

	protected static final String PHASE = "phase";

	protected static final String DESCRIPTION = "description";

	protected static final String SECURITY = "security";

	protected static final String PLM_VAULT = "plmvault";

	protected static final String PLM_CONTENT = "plmcontentvault";

	protected static final String PLM_MASTER_ID = "plmmasterid";

	// PM״̬
	protected static String STATUS_PM_WORKING = "working";// ���ڹ���
	protected static String STATUS_PM_APPROVE = "approving";// �����
	protected static String STATUS_PM_RELEASED = "released";// �ѷ���
	protected static String STATUS_PM_DESPOSED = "deposed";// �ѷ���

	// Windchill״ֵ̬
	protected static String STATUS_PLM_WORKING = "working";//
	protected static String STATUS_PLM_APPROVE = "auditing";
	protected static String STATUS_PLM_RELEASED = "released";
	protected static String STATUS_PLM_DESPOSED = "obsolete";

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
				new BasicDBObject().append(PMFolder.PLM_ID, folderPLMId),
				new BasicDBObject().append(_ID, 1));
		ObjectId folderId = (ObjectId) folder.get(_ID);
		setFolderId(folderId);
	}

	/**
	 * ��ö������ڵ�Ŀ¼����
	 * 
	 * @return �������ڵ�Ŀ¼����
	 */
	public PMFolder getFolder() {
		ObjectId folderId = getFolderId();
		if (folderId == null) {
			return null;
		}
		DBCollection folderColletion = ModelServiceFactory.service
				.getCollection("folder");
		folderColletion.setObjectClass(PMFolder.class);
		PMFolder folder = (PMFolder) folderColletion
				.findOne(new BasicDBObject().append(_ID, folderId));
		folder.setCollection(folderColletion);
		return folder;
	}

	public PMFolder getParentFolder() {
		return getFolder();
	}

	/**
	 * ��ö���ı�ţ���ͬ������ñ�ŵ����岻ͬ��
	 * 
	 * </br>���磺document���󣬱����������ĵ����
	 * 
	 * </br>part���󣬷������ʱ��
	 * 
	 * </br>PMCADDocument,����ͼֽ�����ţ�����ͼ�ţ�
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
		setValue(DOCUMENTNUMBER, documentNumber);
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
		setValue(MAJOR_VID, majorVid);
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
		setValue(SECOND_VID, new Integer(secondVid));
		put(SECOND_VID, new Integer(secondVid));

	}

	/**
	 * ��ȡ��������״̬����ͬ���͵Ķ�����������״̬��ѡ����ܲ�ͬ
	 * 
	 * @return ��������״̬
	 */
	public String getStatus() {
		String pmStatus = (String) get(STATUS);
		String plmStatus = getPLMStatus(pmStatus);
		return plmStatus;
	}

	/**
	 * ������������״̬
	 * 
	 * @param plmStatus
	 */
	public void setStatus(String plmStatus) {
		String pmStatus = getPMStatus(plmStatus);
		setValue(STATUS, pmStatus);
		put(STATUS, pmStatus);
	}

	/**
	 * ����������id
	 *
	 * @param masterid
	 */
	public void SetMasterId(String masterid) {
		setValue(PLM_MASTER_ID, masterid);
		put(PLM_MASTER_ID, masterid);
	}

	public String getMasterId() {
		return (String) get(PLM_MASTER_ID);
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
		setValue(PHASE, phase);
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
		setValue(DESCRIPTION, description);
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

	/**
	 * 
	 * @param plmVault
	 *            ����PLM�б���ĸ�����PMϵͳ��ʹ�������������أ�
	 */
	public void setPLMAttachments(List<IFileProvider> plmVault) {
		BasicBSONList list = new BasicDBList();
		if (plmVault != null) {
			for (int i = 0; i < plmVault.size(); i++) {
				list.add(((IFileProvider) plmVault.get(i)).getFileData());
			}
		}
		put(PLM_VAULT, list);
	}

	/**
	 * 
	 * @param fileProvider
	 *            plmϵͳ�еĶ������ļ�
	 */
	public void setPLMContent(IFileProvider fileProvider) {
		if (fileProvider != null) {
			put(PLM_CONTENT, fileProvider.getFileData());
		} else {
			put(PLM_CONTENT, null);
		}
	}

	@Override
	protected List<String> getMondatoryFields() {
		List<String> result = super.getMondatoryFields();
		result.add(DOCUMENTNUMBER);
		result.add(FOLDER_ID);
		result.add(MAJOR_VID);
		result.add(SECOND_VID);
		result.add(STATUS);
		return result;
	}

	/**
	 * �����������
	 * 
	 * @return
	 */
	public String getContainerName() {
		return getFolder().getRoot().getContainerName();
	}

	@Override
	void setCollection(DBCollection collection) {
		super.setCollection(collection);
	}

	public String getProjectWorkOrder() {
		Object object = get(PROJECT_WORK_ORDER);
		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			if (list.size() > 0) {
				return (String) list.get(0);
			}
		}
		return "";
	}

	public void setContentMD5(String contentMD5) {
		put(CONTENTMD5, contentMD5);
	}

	public String getContentMD5() {
		return (String) get(CONTENTMD5);
	}

	private String getPLMStatus(String pmStatus) {
		if (pmStatus != null) {
			pmStatus = pmStatus.toLowerCase();
			if (STATUS_PM_APPROVE.equals(pmStatus)) {
				return STATUS_PLM_APPROVE.toUpperCase();
			}
			if (STATUS_PM_RELEASED.equals(pmStatus)) {
				return STATUS_PLM_RELEASED.toUpperCase();
			}
			if (STATUS_PM_DESPOSED.equals(pmStatus)) {
				return STATUS_PLM_DESPOSED.toUpperCase();
			}
		}
		return STATUS_PLM_WORKING.toUpperCase();
	}

	private String getPMStatus(String plmStatus) {
		if (plmStatus != null) {
			plmStatus = plmStatus.toLowerCase();
			if (STATUS_PLM_APPROVE.equals(plmStatus)) {
				return STATUS_PM_APPROVE;
			}
			if (STATUS_PLM_RELEASED.equals(plmStatus)) {
				return STATUS_PM_RELEASED;
			}
			if (STATUS_PLM_DESPOSED.equals(plmStatus)) {
				return STATUS_PM_DESPOSED;
			}
		}
		return STATUS_PM_WORKING;
	}
}
