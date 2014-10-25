package com.sg.visionadapter.model;

public final class Supplyment extends BasicPart {

	private static final String MATERIAL_GROUP = "materialgroup";

	private static final String CUSTOMER_NAME = "customername";

	/**
	 * @return the materialGroup
	 */
	public String getMaterialGroup() {
		return (String) get(MATERIAL_GROUP);
	}

	/**
	 * @param materialGroup
	 *            the materialGroup to set
	 */
	public void setMaterialGroup(String materialGroup) {
		put(MATERIAL_GROUP, materialGroup);
	}

	/**
	 * @return the customerName
	 */
	public String getCustomerName() {
		return (String) get(CUSTOMER_NAME);
	}

	/**
	 * @param customerName
	 *            the customerName to set
	 */
	public void setCustomerName(String customerName) {
		put(CUSTOMER_NAME, customerName);
	}

}
