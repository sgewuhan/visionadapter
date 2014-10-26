package com.sg.visionadapter;

import org.bson.types.ObjectId;

/**
 * CADͼֽģ�Ͷ���
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
	 * @return the ��Ʒ����
	 */
	@Override
	public String getProductNumber() {
		return (String) get(PRODUCT_NUMBER);
	}

	/**
	 * @param productNumber
	 *            ���ó�Ʒ����
	 */
	@Override
	public void setProductNumber(String productNumber) {
		put(PRODUCT_NUMBER, productNumber);
	}

	/**
	 * 
	 * �㲿������1������ջɲ�������
	 * 
	 * @return the partType1
	 */
	public String getPartType1() {
		return (String) get(PART_TYPE1);
	}

	/**
	 * @param partType1
	 *            �㲿������1
	 */
	public void setPartType1(String partType1) {
		put(PART_TYPE1, partType1);
	}

	/**
	 * 
	 * @return the �㲿������0
	 */
	public String getPartType0() {
		return (String) get(PART_TYPE0);
	}

	/**
	 * @param partType0
	 *            �㲿������0
	 */
	public void setPartType0(String partType0) {
		put(PART_TYPE0, partType0);
	}

	/**
	 * @return the ͼ�ţ����ʱ�ţ�Ҳ���㲿���ţ�
	 */
	public String getPartNumber() {
		return (String) get(PART_NUMBER);
	}

	/**
	 * @param partNumber
	 *            Ҫ���õ�ͼ��partNumber
	 */
	public void setDrawingNumber(String partNumber) {
		put(PART_NUMBER, partNumber);
	}

}
