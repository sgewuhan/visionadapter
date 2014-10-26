package com.sg.visionadapter;

/**
 * 客供件
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
		put(MATERIAL_GROUP, materialGroup);
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
		put(CUSTOMER_NAME, customerName);
	}

}
