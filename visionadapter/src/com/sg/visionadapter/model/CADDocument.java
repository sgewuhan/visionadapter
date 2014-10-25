package com.sg.visionadapter.model;

import org.bson.types.ObjectId;

public final class CADDocument extends BasicDocument implements
		IProjectRelative,IProductRelative {

	private static final String PART_TYPE0 = "parttype0";

	private static final String PART_TYPE1 = "parttype1";

	private static final String DRAWING_NUMBER = "drawingnumber";

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
	 * @return the productNumber
	 */
	@Override
	public String getProductNumber() {
		return (String) get(PRODUCT_NUMBER);
	}

	/**
	 * @param productNumber
	 *            the productNumber to set
	 */
	@Override
	public void setProductNumber(String productNumber) {
		put(PRODUCT_NUMBER, productNumber);
	}

	/**
	 * @return the partType1
	 */
	public String getPartType1() {
		return (String) get(PART_TYPE1);
	}

	/**
	 * @param partType1
	 *            the partType1 to set
	 */
	public void setPartType1(String partType1) {
		put(PART_TYPE1, partType1);
	}

	/**
	 * @return the partType0
	 */
	public String getPartType0() {
		return (String) get(PART_TYPE0);
	}

	/**
	 * @param partType0
	 *            the partType0 to set
	 */
	public void setPartType0(String partType0) {
		put(PART_TYPE0, partType0);
	}

	/**
	 * @return the drawingNumber
	 */
	public String getDrawingNumber() {
		return (String) get(DRAWING_NUMBER);
	}

	/**
	 * @param drawingNumber
	 *            the drawingNumber to set
	 */
	public void setDrawingNumber(String drawingNumber) {
		put(DRAWING_NUMBER, drawingNumber);
	}

}
