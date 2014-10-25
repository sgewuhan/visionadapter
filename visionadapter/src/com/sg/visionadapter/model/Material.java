package com.sg.visionadapter.model;

public final class Material extends BasicPart {

	private final static String MATERIAL_GROUP = "materialGroup";

	/**
	 * @return the materialGroup
	 */
	public String getMaterialGroup() {
		return (String) get(MATERIAL_GROUP);
	}

	/**
	 * @param materialGroup the materialGroup to set
	 */
	public void setMaterialGroup(String materialGroup) {
		put(MATERIAL_GROUP,materialGroup);
	}
	
}
