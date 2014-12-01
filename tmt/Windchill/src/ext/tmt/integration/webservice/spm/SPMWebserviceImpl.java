package ext.tmt.integration.webservice.spm;

import java.io.File;
import java.io.IOException;

import ext.tmt.utils.Debug;
import wt.util.WTProperties;

public class SPMWebserviceImpl {
	
	
	private static String EXPORT_TEMP="物料主数据 填写模板.xls";//导出成品,半成品的模板文件
	
	private static String codebase_path=null;
	static{
		WTProperties wtproperties;
		try {
			wtproperties = WTProperties.getLocalProperties();
			codebase_path= wtproperties.getProperty("wt.codebase.location");
			codebase_path=codebase_path+File.separator+"excel_template";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
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
    
	
    
     /**
       * 导出成品,半成品物料信息Excel文件
      */
      private static void exportProd2Excel(){
    	File file=getFileByName(EXPORT_TEMP);//获得Codebase下的模板文件
    	//获得所有的成品，半成品信息
    	
    	//将结果集回写到Excel中
    	
    }
	
      
      /**
       *获得Excel模板文件
       * @return File
       */
      private static File getFileByName(String fileName){
    	  if(codebase_path==null) return null;
    	   File file=new File(codebase_path);
    	   if(file.isDirectory()){
    		   String tempPath=codebase_path+File.separator+fileName;
    		   Debug.P("------>>>>Excel Model File Path:"+tempPath);
    		   file=new File(tempPath);
    	   }
    	    return file;
      }

}
