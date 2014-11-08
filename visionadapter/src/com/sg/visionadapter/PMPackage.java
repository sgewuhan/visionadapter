package com.sg.visionadapter;

import com.mongodb.WriteResult;

/**
 * 包装材料
 * @author zhonghua
 *
 */
public final class PMPackage extends BasicPart {

	private final static String MATERIAL_GROUP = "materialgroup";

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
		setValue(MATERIAL_GROUP,materialGroup);
	}
	
	@Override
	public WriteResult doInsert() throws Exception {
		put(EDITOR, "editor.document.material");
		return super.doInsert();
	}
	
}
