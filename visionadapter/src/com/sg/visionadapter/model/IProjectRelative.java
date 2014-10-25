package com.sg.visionadapter.model;

import org.bson.types.ObjectId;

public interface IProjectRelative {

	public static final String PROJECT_ID = "project_id";

	public static final String PROJECT_DESC = "projectdesc";

	public static final String PROJECT_NUMBER = "projectnumber";

	public static final String PROJECT_WORK_ORDER = "projectworkorder";
	
	public ObjectId getProjectId();

	public void setProjectId(ObjectId projectId);

	public String getProjectDesc();

	public void setProjectDesc(String projectDesc);

	public String getProjectNumber();

	public void setProjectNumber(String projectNumber);

	public String getProjectWorkOrder();

	public void setProjectWorkOrder(String projectWorkOrder);
	
}
