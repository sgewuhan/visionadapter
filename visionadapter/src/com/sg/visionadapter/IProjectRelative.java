package com.sg.visionadapter;

import org.bson.types.ObjectId;

/**
 * ����Ŀ�йصĶ���
 * @author zhonghua
 *
 */
public interface IProjectRelative {

	public static final String PROJECT_ID = "project_id";

	public static final String PROJECT_DESC = "projectdesc";

	public static final String PROJECT_NUMBER = "projectnumber";

	public static final String PROJECT_WORK_ORDER = "projectworkorder";
	
	/**
	 * ��ȡ��Ŀid
	 * @return ��Ŀid
	 */
	public ObjectId getProjectId();

	/**
	 * 
	 * @param projectId ��Ŀid
	 */
	public void setProjectId(ObjectId projectId);

	/**
	 * 
	 * @return ��Ŀ����
	 */
	public String getProjectName();

	/**
	 * @param projectName ��Ŀ����
	 */
	public void setProjectName(String projectName);

	/**
	 * 
	 * @return ��Ŀ���
	 */
	public String getProjectNumber();

	/**
	 * 
	 * @param projectNumber ��Ŀ���
	 */
	public void setProjectNumber(String projectNumber);

//	/**
//	 * 
//	 * @return ��Ŀ�������
//	 */
//	public String getProjectWorkOrder();

//	/**
//	 * 
//	 * @param projectWorkOrder ��Ŀ�������
//	 */
//	public void setProjectWorkOrder(String projectWorkOrder);
	
}
