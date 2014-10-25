package com.sg.visionadapter.model;

/**
 * 结构化文档
 * @author Administrator
 *
 */
public abstract class BasicPart extends BasicDocument {
	
	protected static final String SPEC = "spec";
	
	protected static final String MODEL = "model";
	
	protected static final String WEIGHT = "weight";
	
	protected static final String MATERIAL = "material";
	

	/**
	 * 获得规格
	 * @return
	 */
	public String getSpec() {
		return (String) get(SPEC);
	}

	/**
	 * 设置规格
	 * @param spec
	 */
	public void setSpec(String spec) {
		put(SPEC,spec);
	}

	/**
	 * 型号
	 * @return
	 */
	public String getModel() {
		return (String) get(MODEL);
	}

	/**
	 * 型号
	 * @param model
	 */
	public void setModel(String model) {
		put(MODEL,model);
	}

	/**
	 * 取重量
	 * @return
	 */
	public Number getWeight() {
		return (Number) get(WEIGHT);
	}

	/**
	 * 设置重量
	 * @param weight
	 */
	public void setWeight(Number weight) {
		put(WEIGHT,weight);
	}

	/**
	 * 取材料
	 * @return
	 */
	public String getMaterial() {
		return (String) get(MATERIAL);
	}

	/**
	 * 设置材料
	 * @param material
	 */
	public void setMaterial(String material) {
		put(MATERIAL,material);
	}

	
}
