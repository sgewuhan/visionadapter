package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.WriteResult;

/**
 * CADͼֽģ�Ͷ���
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
		setValue(PRODUCT_NUMBER, productNumber);
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
		setValue(PART_TYPE0, partType0);
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
		setValue(PART_NUMBER, partNumber);
	}
	
	@Override
	public WriteResult doInsert() throws Exception {
		put(EDITOR, "editor.document.cad");
		return super.doInsert();
	}

}
