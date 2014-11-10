package ext.tmt.integration.webservice.spm;

public class SPMWebserviceImpl {
	
	
	/**
     * 第一个流程的实现方法
     */
    public static String processorForSpm1(String workflow, int times,String factory){
    	return "processorForSpm1";
    }

    /**
     * 第二个流程的实现方法 变更流程
     */
    public static String processorForSpm2(String partNumber, String workflow,String mark){
    	return "processorForSpm2";
    }

    /**
     * 为部件创建关联文档
     */
    public static String createDocForPart(String partNumber, String workflow,String factory){
    	return "createDocForPart";
    }

    /**
     * SPM调用接口判断物料在PLM系统是否存在技术规格书
     * @return 0. 表示存在技术规格书 1.表示不存在技术规格书
     */
    public static String getJSGGSByPartNumber(String partNumber) {
    	return "getJSGGSByPartNumber";
    }

    /**
     * SPM调用接口判断物料在PLM系统是否存在产品手册
     * @return 0. 表示存在产品手册 1.表示不存在产品手册
     */
    public static String getCPSCByPartNumber(String partNumber) {
    	return "getCPSCByPartNumber";
    }

    /**
     * 为部件修改关联文档
     */
    public static String updateDocForPart(String partNumber, String workflow){
    	return "updateDocForPart";
    }
	
    /**
     * 检查物料是否在存储库中存在
     */
    public static String checkPartFromLibrary(String partNumber) {
    	return "checkPartFromLibrary";
    }
    
	
	
	

}
