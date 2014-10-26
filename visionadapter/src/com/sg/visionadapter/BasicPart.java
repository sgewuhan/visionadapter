package com.sg.visionadapter;

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
	 * @return 规格
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
	 * @return 型号
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
	 * @return 重量
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
	 * @return 材料
	 */
	public String getMaterial() {
		return (String) get(MATERIAL);
	}

	/**
	 * 设置材料
	 * @param material 材料
	 */
	public void setMaterial(String material) {
		put(MATERIAL,material);
	}

	
}
