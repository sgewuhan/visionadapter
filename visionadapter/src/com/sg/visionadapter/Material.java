package com.sg.visionadapter;

/**
 * ԭ���϶���
 * @author zhonghua
 *
 */
public final class Material extends BasicPart {

	private final static String MATERIAL_GROUP = "materialGroup";

	/**
	 * @return the ������
	 */
	public String getMaterialGroup() {
		return (String) get(MATERIAL_GROUP);
	}

	/**
	 * @param materialGroup ����������
	 */
	public void setMaterialGroup(String materialGroup) {
		put(MATERIAL_GROUP,materialGroup);
	}
	
}
