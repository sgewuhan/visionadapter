package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.WriteResult;

/**
 * 成品对象
 * 
 * @author zhonghua
 *
 */
public final class PMProduct extends BasicPart {

	private static final String FORMULAR = "formularnumber";
	private static final String PRODUCT_NUMBER = "productnumber";

	@Override
	public ObjectId getProjectId() {
		return (ObjectId) get(PROJECT_ID);
	}

	@Override
	public void setProjectId(ObjectId projectId) {
		setValue(PROJECT_ID, projectId);
	}

	@Override
	public String getProjectName() {
		return (String) get(PROJECT_DESC);
	}

	@Override
	public void setProjectName(String projectDesc) {
		setValue(PROJECT_DESC, projectDesc);
	}

	@Override
	public String getProjectNumber() {
		return (String) get(PROJECT_NUMBER);
	}

	@Override
	public void setProjectNumber(String projectNumber) {
		setValue(PROJECT_NUMBER, projectNumber);
	}

	@Override
	public String getProjectWorkOrder() {
		return (String) get(PROJECT_WORK_ORDER);
	}

	/**
	 * @return 配方号
	 */
	public String getFormularNumber() {
		return (String) get(FORMULAR);
	}

	/**
	 * @param formularNumber
	 *            配方号
	 */
	public void setFormularNumber(String formularNumber) {
		setValue(FORMULAR, formularNumber);
	}

	@Override
	public WriteResult doInsert() throws Exception {
		setEditor();
		return super.doInsert();
	}

	@Override
	protected void setEditor() {
		put(EDITOR, "editor.document.product");
	}

	public void setProductNumber(String productNumber) {
		setValue(PRODUCT_NUMBER, productNumber);
	}
}
