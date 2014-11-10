package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.WriteResult;

/**
 * CAD图纸模型对象
 * 
 * @author zhonghua
 *
 */
public final class PMCADDocument extends BasicDocument implements
		IProductRelative {

	private static final String PART_TYPE0 = "parttype0";

	private static final String PART_NUMBER = "partnumber";

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
	 * @return the 成品编码
	 */
	@Override
	public String getProductNumber() {
		return (String) get(PRODUCT_NUMBER);
	}

	/**
	 * @param productNumber
	 *            设置成品编码
	 */
	@Override
	public void setProductNumber(String productNumber) {
		setValue(PRODUCT_NUMBER, productNumber);
	}


	/**
	 * 
	 * @return the 零部件类型0
	 */
	public String getPartType0() {
		return (String) get(PART_TYPE0);
	}

	/**
	 * @param partType0
	 *            零部件类型0
	 */
	public void setPartType0(String partType0) {
		setValue(PART_TYPE0, partType0);
	}

	/**
	 * @return the 图号（物资编号，也是零部件号）
	 */
	public String getPartNumber() {
		return (String) get(PART_NUMBER);
	}

	/**
	 * @param partNumber
	 *            要设置的图号partNumber
	 */
	public void setDrawingNumber(String partNumber) {
		setValue(PART_NUMBER, partNumber);
	}
	
	@Override
	public WriteResult doInsert() throws Exception {
		put(EDITOR, "editor.document.cad");
		return super.doInsert();
	}

}
