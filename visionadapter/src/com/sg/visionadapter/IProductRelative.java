package com.sg.visionadapter;

/**
 * 与产品有关的对象
 * @author zhonghua
 *
 */
public interface IProductRelative {
	
	public static final String PRODUCT_NUMBER = "productnumber";


	/**
	 * @return the productNumber
	 */
	public String getProductNumber();

	/**
	 * @param productNumber
	 *            the productNumber to set
	 */
	public void setProductNumber(String productNumber);
}
