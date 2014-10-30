package com.sg.visionadapter;

import java.util.List;

import org.bson.types.BasicBSONList;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * 抽象的文档对象
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

	protected static final String PLM_VAULT = "plmvault";

	protected static final String PLM_CONTENT = "plmcontentvault";

	/**
	 * 获得所在的目录id
	 * 
	 * @return 所在的目录id
	 */
	public ObjectId getFolderId() {
		return (ObjectId) get(FOLDER_ID);
	}

	/**
	 * 设置对象所在的目录的id
	 * 
	 * @param folderId
	 */
	public void setFolderId(ObjectId folderId) {
		put(FOLDER_ID, folderId);
	}

	/**
	 * 设置对象所在的目录id
	 * 
	 * @param folderPLMId
	 *            目录对象的PLM对象id
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
	 * 获得对象所在的目录对象
	 * 
	 * @return 对象所在的目录对象
	 */
	public PMFolder getFolder() {
		ObjectId folderId = getFolderId();
		if (folderId == null) {
			return null;
		}
		DBCollection folderColletion = ModelServiceFactory.service
				.getCollection("folder");
		folderColletion.setObjectClass(PMFolder.class);
		setCollection(folderColletion);
		return (PMFolder) collection.findOne(new BasicDBObject().append(_ID,
				folderId));
	}

	/**
	 * 获得对象的编号，不同的子类该编号的意义不同。
	 * 
	 * </br>比如：document对象，本方法返回文档编号
	 * 
	 * </br>part对象，返回物资编号
	 * 
	 * </br>PMCADDocument,返回图纸对象编号（并非图号）
	 * 
	 * @return 编号
	 */
	public String getObjectNumber() {
		return (String) get(DOCUMENTNUMBER);

	}

	/**
	 * 设置对象的编号
	 * 
	 * @param documentNumber
	 */
	public void setObjectNumber(String documentNumber) {
		put(DOCUMENTNUMBER, documentNumber);
	}

	/**
	 * 获得主要版本号，例如A,B,C
	 * 
	 * @return 主要版本号
	 */
	public String getMajorVid() {
		return (String) get(MAJOR_VID);
	}

	/**
	 * 设置主要版本号
	 * 
	 * @param majorVid
	 *            主要版本号
	 */
	public void setMajorVid(String majorVid) {
		put(MAJOR_VID, majorVid);
	}

	/**
	 * 
	 * @return 次要版本号，例如 0,1,2
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
	 * 设置次要版本号
	 * 
	 * @param secondVid
	 *            次要版本号
	 */
	public void setSecondVid(int secondVid) {
		put(SECOND_VID, new Integer(secondVid));

	}

	/**
	 * 获取生命周期状态，不同类型的对象生命周期状态的选项可能不同
	 * 
	 * @return 生命周期状态
	 */
	public String getStatus() {
		return (String) get(STATUS);
	}

	/**
	 * 设置生命周期状态
	 * 
	 * @param status
	 */
	public void setStatus(String status) {
		put(STATUS, status);
	}

	/**
	 * 获取阶段标记
	 * 
	 * @return 阶段标记
	 */
	public String getPhase() {
		return (String) get(PHASE);
	}

	/**
	 * 设置阶段标记
	 * 
	 * @param phase
	 */
	public void setPhase(String phase) {
		put(PHASE, phase);
	}

	/**
	 * 获得长文本的描述
	 * 
	 * @return 描述
	 */
	public String getDescription() {
		return (String) get(DESCRIPTION);
	}

	/**
	 * 设置长文本的描述
	 * 
	 * @param description
	 *            描述
	 */
	public void setDescription(String description) {
		put(DESCRIPTION, description);
	}

	/**
	 * 获得对象的密级
	 * 
	 * @return 密级
	 */
	public String getSecurity() {
		return (String) get(SECURITY);
	}

	/**
	 * 设置对象的密级
	 * 
	 * @param security
	 */
	public void setSecurity(String security) {
		put(SECURITY, security);
	}

	/**
	 * 
	 * @param plmVault
	 *            设置PLM中保存的附件（PM系统将使用它来进行下载）
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
	 *            plm系统中的对象主文件
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
	 * 获得容器名称
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
}
