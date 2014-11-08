package com.sg.visionadapter;

/**
 * ������㲿������
 * @author zhonghua
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
		setValue(SPEC, spec);
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
		setValue(MODEL, model);
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
		setValue(WEIGHT, weight);
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
		setValue(MATERIAL, material);
		put(MATERIAL,material);
	}

	
}
