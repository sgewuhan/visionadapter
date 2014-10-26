package com.sg.visionadapter;

/**
 * 原材料对象
 * @author zhonghua
 *
 */
public final class Material extends BasicPart {

	private final static String MATERIAL_GROUP = "materialGroup";

	/**
	 * @return the 物料组
	 */
	public String getMaterialGroup() {
		return (String) get(MATERIAL_GROUP);
	}

	/**
	 * @param materialGroup 设置物料组
	 */
	public void setMaterialGroup(String materialGroup) {
		put(MATERIAL_GROUP,materialGroup);
	}
	
}
