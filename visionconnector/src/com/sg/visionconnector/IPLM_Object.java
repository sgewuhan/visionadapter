package com.sg.visionconnector;

public interface IPLM_Object {
	/**
	 * PLM
	 */
	public static final String F_PLM_TYPE = "plmtype";

	public static final String TYPE_DOCUMENT = "pmdocument";

	public static final String TYPE_CAD = "pmcaddocument";
	
	public static final String TYPE_FOLDER = "pmfolder";
	
	public static final String TYPE_PART = "pmpart";
	
	public static final String TYPE_PRODUCT = "pmproduct";
	
	public static final String TYPE_MATERIAL = "pmmaterial";
	
	public static final String TYPE_SUPPLYMENT = "pmsupplyment";
	
	public static final String TYPE_JIGTOOL = "pmjigtools";
	
	public static final String TYPE_PACKAGE = "pmpackage";

	/**
	 * 零部件物资编号
	 */
	public static final String F_PARTNUMBER = "documentnumber";

	/**
	 * 文档的类型
	 */
	public static final String F_DOCUMENT_TYPE = "documenttype";

}
