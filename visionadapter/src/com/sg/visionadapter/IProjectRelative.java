package com.sg.visionadapter;

import org.bson.types.ObjectId;

/**
 * 与项目有关的对象
 * @author zhonghua
 *
 */
public interface IProjectRelative {

	public static final String PROJECT_ID = "project_id";

	public static final String PROJECT_DESC = "projectdesc";

	public static final String PROJECT_NUMBER = "projectnumber";

	public static final String PROJECT_WORK_ORDER = "projectworkorder";
	
	/**
	 * 获取项目id
	 * @return 项目id
	 */
	public ObjectId getProjectId();

	/**
	 * 
	 * @param projectId 项目id
	 */
	public void setProjectId(ObjectId projectId);

	/**
	 * 
	 * @return 项目名称
	 */
	public String getProjectName();

	/**
	 * @param projectName 项目名称
	 */
	public void setProjectName(String projectName);

	/**
	 * 
	 * @return 项目编号
	 */
	public String getProjectNumber();

	/**
	 * 
	 * @param projectNumber 项目编号
	 */
	public void setProjectNumber(String projectNumber);

//	/**
//	 * 
//	 * @return 项目工作令号
//	 */
//	public String getProjectWorkOrder();

//	/**
//	 * 
//	 * @param projectWorkOrder 项目工作令号
//	 */
//	public void setProjectWorkOrder(String projectWorkOrder);
	
}
