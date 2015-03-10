package com.sg.visionadapter;

import com.mongodb.WriteResult;

/**
 * 客供件
 * 
 * @author zhonghua
 *
 */
public final class PMSupplyment extends BasicPart {

	private static final String MATERIAL_GROUP = "materialgroup";

	private static final String CUSTOMER_NAME = "customername";

	/**
	 * @return 物料组
	 */
	public String getMaterialGroup() {
		return (String) get(MATERIAL_GROUP);
	}

	/**
	 * @param materialGroup
	 *            物料组
	 */
	public void setMaterialGroup(String materialGroup) {
		setValue(MATERIAL_GROUP, materialGroup);
	}

	/**
	 * @return 客户名称
	 */
	public String getCustomerName() {
		return (String) get(CUSTOMER_NAME);
	}

	/**
	 * @param customerName
	 *            设置客户名称
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
