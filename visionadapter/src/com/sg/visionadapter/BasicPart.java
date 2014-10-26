package com.sg.visionadapter;

/**
 * �ṹ���ĵ�
 * @author Administrator
 *
 */
public abstract class BasicPart extends BasicDocument {
	
	protected static final String SPEC = "spec";
	
	protected static final String MODEL = "model";
	
	protected static final String WEIGHT = "weight";
	
	protected static final String MATERIAL = "material";
	

	/**
	 * ��ù��
	 * @return ���
	 */
	public String getSpec() {
		return (String) get(SPEC);
	}

	/**
	 * ���ù��
	 * @param spec
	 */
	public void setSpec(String spec) {
		put(SPEC,spec);
	}

	/**
	 * �ͺ�
	 * @return �ͺ�
	 */
	public String getModel() {
		return (String) get(MODEL);
	}

	/**
	 * �ͺ�
	 * @param model
	 */
	public void setModel(String model) {
		put(MODEL,model);
	}

	/**
	 * ȡ����
	 * @return ����
	 */
	public Number getWeight() {
		return (Number) get(WEIGHT);
	}

	/**
	 * ��������
	 * @param weight
	 */
	public void setWeight(Number weight) {
		put(WEIGHT,weight);
	}

	/**
	 * ȡ����
	 * @return ����
	 */
	public String getMaterial() {
		return (String) get(MATERIAL);
	}

	/**
	 * ���ò���
	 * @param material ����
	 */
	public void setMaterial(String material) {
		put(MATERIAL,material);
	}

	
}
