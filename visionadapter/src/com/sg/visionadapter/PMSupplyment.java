package com.sg.visionadapter;

import com.mongodb.WriteResult;

/**
 * �͹���
 * 
 * @author zhonghua
 *
 */
public final class PMSupplyment extends BasicPart {

	private static final String MATERIAL_GROUP = "materialgroup";

	private static final String CUSTOMER_NAME = "customername";

	/**
	 * @return ������
	 */
	public String getMaterialGroup() {
		return (String) get(MATERIAL_GROUP);
	}

	/**
	 * @param materialGroup
	 *            ������
	 */
	public void setMaterialGroup(String materialGroup) {
		setValue(MATERIAL_GROUP, materialGroup);
	}

	/**
	 * @return �ͻ�����
	 */
	public String getCustomerName() {
		return (String) get(CUSTOMER_NAME);
	}

	/**
	 * @param customerName
	 *            ���ÿͻ�����
	 */
	public void setCustomerName(String customerName) {
		setValue(CUSTOMER_NAME, customerName);
	}

	@Override
	public WriteResult doInsert() throws Exception {
		put(EDITOR, "editor.document.supplyment");
		return super.doInsert();
	}

	@Override
	protected void setEditor() {
		put(EDITOR, "editor.document.supplyment");
	}
}
