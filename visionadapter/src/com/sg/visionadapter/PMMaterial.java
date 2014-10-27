package com.sg.visionadapter;

import com.mongodb.WriteResult;

/**
 * ԭ���϶���
 * @author zhonghua
 *
 */
public final class PMMaterial extends BasicPart {

	private final static String MATERIAL_GROUP = "materialgroup";

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
		setValue(MATERIAL_GROUP,materialGroup);
	}
	
	@Override
	public WriteResult doInsert() throws Exception {
		put(EDITOR, "editor.document.material");
		return super.doInsert();
	}
	
}
