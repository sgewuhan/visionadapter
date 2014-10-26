package com.sg.visionadapter;

import org.bson.types.ObjectId;

/**
 * CAD图纸模型对象
 * 
 * @author zhonghua
 *
 */
public final class VTCADDocument extends BasicDocument implements
		IProjectRelative, IProductRelative {

	private static final String PART_TYPE0 = "parttype0";

	private static final String PART_TYPE1 = "parttype1";

	private static final String PART_NUMBER = "partnumber";

	@Override
	public ObjectId getProjectId() {
		return (ObjectId) get(PROJECT_ID);
	}

	@Override
	public void setProjectId(ObjectId projectId) {
		put(PROJECT_ID, projectId);
	}

	@Override
	public String getProjectName() {
		return (String) get(PROJECT_DESC);
	}

	@Override
	public void setProjectName(String projectDesc) {
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
		put(PRODUCT_NUMBER, productNumber);
	}

	/**
	 * 
	 * 零部件类型1，比如空簧部件类型
	 * 
	 * @return the partType1
	 */
	public String getPartType1() {
		return (String) get(PART_TYPE1);
	}

	/**
	 * @param partType1
	 *            零部件类型1
	 */
	public void setPartType1(String partType1) {
		put(PART_TYPE1, partType1);
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
		put(PART_TYPE0, partType0);
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
		put(PART_NUMBER, partNumber);
	}

}
