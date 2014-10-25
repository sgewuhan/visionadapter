package com.sg.visionadapter.model;

import org.bson.types.ObjectId;

public final class Product extends BasicPart  implements IProjectRelative{

	private static final String FORMULAR = "formularnumber";
	
	
	@Override
	public ObjectId getProjectId() {
		return (ObjectId) get(PROJECT_ID);
	}

	@Override
	public void setProjectId(ObjectId projectId) {
		put(PROJECT_ID, projectId);
	}

	@Override
	public String getProjectDesc() {
		return (String) get(PROJECT_DESC);
	}

	@Override
	public void setProjectDesc(String projectDesc) {
		put(PROJECT_DESC, projectDesc);
	}

	@Override
	public String getProjectNumber() {
		return (String) get(PROJECT_NUMBER);
	}

	@Override
	public void setProjectNumber(String projectNumber) {
		put(PROJECT_NUMBER, projectNumber);
	}

	@Override
	public String getProjectWorkOrder() {
		return (String) get(PROJECT_WORK_ORDER);
	}

	@Override
	public void setProjectWorkOrder(String projectWorkOrder) {
		put(PROJECT_WORK_ORDER, projectWorkOrder);
	}

	/**
	 * @return the formularNumber
	 */
	public String getFormularNumber() {
		return (String) get(FORMULAR);
	}

	/**
	 * @param formularNumber the formularNumber to set
	 */
	public void setFormularNumber(String formularNumber) {
		put(FORMULAR,formularNumber);
	}

	
}