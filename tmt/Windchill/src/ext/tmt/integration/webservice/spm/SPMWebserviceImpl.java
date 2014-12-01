package ext.tmt.integration.webservice.spm;

import java.io.File;
import java.io.IOException;

import ext.tmt.utils.Debug;
import wt.util.WTProperties;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import wt.iba.value.IBAHolder;
import wt.lifecycle.LifeCycleManaged;
import wt.part.WTPart;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.PartUtil;
>>>>>>> branch 'master' of https://github.com/sgewuhan/visionadapter.git

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
	
	 static Connection conn = null;
	/**
     * 第一个流程的实现方法
     */
    public static String processorForSpm1(String workflow, int times,String factory){
    	
    	Debug.P("processorForSpm1  paramaters -->"+ workflow + " --- " + times + " --- "
                + factory);
//    	
//    	  // IBA属性集合
//        Properties properties = new Properties();
//        HashMap hashmap = new HashMap();
//        HashMap para = new HashMap();
//        // 分类属性集合
//        HashMap<String, String> ibaMap = new HashMap<String, String>();
//        ResultSet resultSet = null;
//        String partType = Contants.PART_TYPE;
//
//        String containerName = Contants.PART_TYPE;
//        String folderName = Contants.PART_TYPE;
//        para.put("containerName", containerName);
//        para.put("folderName", folderName);
//        para.put("partType", partType);
//
//        String result = null;
//        try {
//            conn = DBUtils.getConnection();
//            if (workflow != null && times == 1) {
//                String sql = "select * from CSR_WLSXSQ,CSR_JSTZSX where CSR_JSTZSX.WORKFLOW = CSR_WLSXSQ.WORKFLOW and CSR_JSTZSX.TIMES = '"
//                        + 1 + "' AND CSR_WLSXSQ.WORKFLOW = '" + workflow + "'";
//                resultSet = DBUtils.executeQuery(conn, sql);
//                while (resultSet.next()) {
//                    // CSR_WLSXSQ
//                    String WLNUMBER = resultSet.getString("WLNUMBER");
//                    String WLNAME = resultSet.getString("WLNAME");
//                    String WLXIAOLEI = resultSet.getString("WLXIAOLEI");
//                    String ATTRKEY = resultSet.getString("ATTRKEY");
//                    String ATTRVALUE = resultSet.getString("ATTRVALUE");
//
//                    hashmap.put("ATTRKEY", ATTRKEY);
//                    hashmap.put("ATTRVALUE", ATTRVALUE);
//                    hashmap.put("WLNUMBER", WLNUMBER);
//                    hashmap.put("WLNAME", WLNAME);
//                    hashmap.put("WLXIAOLEI", WLXIAOLEI);
//
//                    //hashmap = CsrSpmUtil.nullConvertEmptyForHashMap(hashmap);
//                    Debug.P("hashmap is " + hashmap);
//
//                    para.put("number", (String) hashmap.get("WLNUMBER"));
//                    para.put("name", (String) hashmap.get("WLNAME"));
//                    para.put("WLXIAOLEI",
//                             (String) hashmap.get("WLXIAOLEI"));
//                    String zonghedengji = Contants.PART_TYPE;
//                    String unit = Contants.PART_TYPE;
//                    String xinghaoguige =Contants.PART_TYPE;
//                    String wuliaoleixing = Contants.PART_TYPE;
//
//                    // 综合等级
//                    if (((String) hashmap.get("ATTRKEY")).equals(zonghedengji)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY")
//                                + "2", (String) hashmap.get("ATTRVALUE"));
//                        ibaMap.put((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 基本计量单位
//                    else if (((String) hashmap.get("ATTRKEY")).equals(unit)) {
//                        para.put("unit", (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 物料类型
//                    else if (((String) hashmap.get("ATTRKEY")).equals(wuliaoleixing)) {
//                        Debug.P((String) hashmap.get("ATTRKEY") + "wuliaoleixing ============"
//                                + Contants.PART_TYPE);
//                        ibaMap.put((String) hashmap.get("ATTRKEY"), Contants.PART_TYPE);
//                        continue;
//                    }
//                    // 型号规格
//                    else if (((String) hashmap.get("ATTRKEY")).equals(xinghaoguige)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"), (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    } else {
//                        ibaMap.put((String) hashmap.get("ATTRKEY"),(String) hashmap.get("ATTRVALUE"));
//                    }
//                }
//
//                para.put("ibaMap", ibaMap);
//
//                Boolean isExist = false;//WtUtil.isExistPart((String) para.get("number"), false);
//                if (isExist) {
//                    Debug.P("this part is exist");
//                    return "此部件已经存在";
//                } else {
//                    try {
//                        // 根据第一次发布找到基本信息创建part
//                      //  CsrSpmUtil.createNewPart(para, properties);
//                    } catch (Exception e) {
//                        return e.getLocalizedMessage();// 直接将错误信息进行返回
//                    }
//
//                }
//            } else if (workflow != null && (times == 2 || times == 3)) {
//                String partNo = null;
//                String sql = "select * from CSR_JSTZSX where TIMES = '" + times
//                        + "' AND WORKFLOW = '" + workflow + "'";
//                resultSet = DBUtils.executeQuery(conn, sql);
//                while (resultSet.next()) {
//
//                    String WORKFLOW = resultSet.getString("WORKFLOW");
//                    String TIMES = resultSet.getString("TIMES");
//                    String CREATOR = resultSet.getString("CREATOR");
//                    String WLNUMBER = resultSet.getString("WLNUMBER");
//                    String WLXIAOLEI = resultSet.getString("WLXIAOLEI");
//                    String ATTRKEY = resultSet.getString("ATTRKEY");
//                    String ATTRVALUE = resultSet.getString("ATTRVALUE");
//
//                    hashmap.put("ATTRVALUE", ATTRVALUE);
//                    hashmap.put("TIMES", TIMES);
//                    hashmap.put("ATTRKEY", ATTRKEY);
//                    hashmap.put("WLXIAOLEI", WLXIAOLEI);
//                    hashmap.put("WLNUMBER", WLNUMBER);
//                    hashmap.put("CREATOR", CREATOR);
//                    hashmap.put("WORKFLOW", WORKFLOW);
//
//                    //hashmap = CsrSpmUtil.nullConvertEmptyForHashMap(hashmap);
//                    Debug.P("hashmap is " + hashmap);
//                    partNo = (String) hashmap.get("WLNUMBER");// 取得要更新的part的编号
//
//                    String zonghedengji =Contants.PART_TYPE;
//                    String xinghaoguige = Contants.PART_TYPE;
//                    String wuliaoleixing = Contants.PART_TYPE;
//
//                    if (((String) hashmap.get("ATTRKEY")).equals(zonghedengji)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY")+ "2", (String) hashmap.get("ATTRVALUE"));
//                        ibaMap.put((String) hashmap.get("ATTRKEY"), (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    } else if (((String) hashmap.get("ATTRKEY")).equals(wuliaoleixing)) {
//                        ibaMap.put((String) hashmap.get("ATTRKEY"), "ATTRVALUE");
//                        continue;
//                    } else if (((String) hashmap.get("ATTRKEY")).equals(xinghaoguige)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    } else {
//                        ibaMap.put((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                    }
//                }
//                if (!properties.isEmpty()) {
//                    //CsrSpmUtil.updatePartForIba(partNo, properties, "");
//                }
//                if (!ibaMap.isEmpty()) {
//                    String classificationPath = ROOT + (String) hashmap.get("WLXIAOLEI");// 取得分类path
//                    // 根据第二次发布找到属性更新part
//                    //CsrSpmUtil.updatePartClassificationIba(partNo,
//                    //        classificationPath, ibaMap, "");
//                }
//
//                // 物料删除标识维护
//                if (times == 3) {
//                    WTPart part = PartUtil.getLastPartbyNumViwe(partNo,
//                            Contants.DESIGN);
//                    if (part == null) {
//                        result = "PLM不存在编号为" + partNo + "的部件。";
//                        return result;
//                    }
//                    if (StringUtils.isEmpty(factory)) {
//                        result = "删除工厂为空";
//                        return result;
//                    }
//
//                    if (part != null) {
//                        IBAUtils utils = new IBAUtils((IBAHolder) part);
//                        String suoshugongchang = utils
//                                .getIBAValue("CSR_SUOSHUGONGCHANG");
//                        String yizuofeigongchang = utils
//                                .getIBAValue("CSR_YISHANCHUGONGCHANG");
//                        Debug.P("yizuofeigongchang>>>>>" + yizuofeigongchang);
//                        if (StringUtils.isEmpty(suoshugongchang)) {
//                            GenericUtil.setLifeCycleState((LifeCycleManaged) part,
//                                    "CSR_YIZUOFEI");
//                        } else {
//                            Vector<String> factoriesStrs = PrintSheet
//                                    .transforVec(suoshugongchang);
//                            Vector<String> yizuofeigongchangStrs = PrintSheet
//                                    .transforVec(yizuofeigongchang);
//
//                            String[] factories = factory.split(",");
//                            if (factories != null && factories.length > 0) {
//                                for (int i = 0; i < factories.length; i++) {
//                                    String fac = factories[i];
//                                    if (factoriesStrs.contains(fac)) {
//                                        factoriesStrs.remove(fac);
//                                    }
//                                    if (!yizuofeigongchangStrs.contains(fac)) {
//                                        yizuofeigongchangStrs.add(fac);
//                                    }
//                                }
//                            }
//                            String suoshugongchang_new = PrintSheet
//                                    .transforSTR2(factoriesStrs);
//                            Debug.P("suoshugongchang_new---->"
//                                    + suoshugongchang_new);
//                            String yizuofeigongchang_new = PrintSheet
//                                    .transforSTR2(yizuofeigongchangStrs);
//                            Debug.P("yizuofeigongchang_new---->"
//                                    + yizuofeigongchang_new);
//                            WtUtil.updateIBAValue(part, "CSR_SUOSHUGONGCHANG",
//                                    suoshugongchang_new);
//                            WtUtil.updateIBAValue(part,
//                                    "CSR_YISHANCHUGONGCHANG",
//                                    yizuofeigongchang_new);
//                            if (StringUtils.isEmpty(suoshugongchang)) {
//                                CsrSpmUtil.setState((LifeCycleManaged) part,
//                                        "CSR_YIZUOFEI");
//                            }
//                        }
//                        result = "删除成功";
//                    }
//                }
//            } else if (workflow != null && (times == 10 || times == 20)) {// 集成创建工具资源
//                partType = CSRUtils.getProperty("csr.spm.resourcetype");
//                containerName = CSRUtils
//                        .getProperty("csr.resource.containername");
//                folderName = CSRUtils.getProperty("csr.resource.foldername");
//                para.put("containerName", containerName);
//                para.put("folderName", folderName);
//                para.put("partType", partType);
//
//                String sql = "select * from CSR_WLSXSQ,CSR_JSTZSX where CSR_JSTZSX.WORKFLOW = CSR_WLSXSQ.WORKFLOW and CSR_JSTZSX.TIMES = '"
//                        + times
//                        + "' AND CSR_WLSXSQ.WORKFLOW = '"
//                        + workflow
//                        + "'";
//                resultSet = DBUtils.executeQuery(conn, sql);
//                while (resultSet.next()) {
//                    // CSR_WLSXSQ
//                    String WLNUMBER = resultSet.getString("WLNUMBER");
//                    String WLNAME = resultSet.getString("WLNAME");
//                    String WLXIAOLEI = resultSet.getString("WLXIAOLEI");
//                    String ATTRKEY = resultSet.getString("ATTRKEY");
//                    String ATTRVALUE = resultSet.getString("ATTRVALUE");
//
//                    hashmap.put("ATTRKEY", ATTRKEY);
//                    hashmap.put("ATTRVALUE", ATTRVALUE);
//                    hashmap.put("WLNUMBER", WLNUMBER);
//                    hashmap.put("WLNAME", WLNAME);
//                    hashmap.put("WLXIAOLEI", WLXIAOLEI);
//
//                    hashmap = CsrSpmUtil.nullConvertEmptyForHashMap(hashmap);
//                    Debug.P("hashmap is " + hashmap);
//
//                    para.put("number", (String) hashmap.get("WLNUMBER"));
//                    para.put("name", (String) hashmap.get("WLNAME"));
//
//                    String ziyuanleixing = CSRUtils
//                            .getProperty("csr.ziyuanleixing");
//                    String unit = CSRUtils
//                            .getProperty("csr.jibenjiliangdanwei");
//                    String pinpai = CSRUtils.getProperty("csr.pinpai");
//                    String pinpaiMust = CSRUtils.getProperty("csr.pinpai.must");
//                    String xinghaoguige = CSRUtils
//                            .getProperty("csr.xinghaoguige");
//                    String xinghaoMust = CSRUtils
//                            .getProperty("csr.xinghao.must");
//                    String gongjutupian = CSRUtils
//                            .getProperty("csr.gongjutupian");
//                    String tuzhibianhao = CSRUtils
//                            .getProperty("csr.tuzhibianhao");
//                    String cshsyfw = CSRUtils
//                            .getProperty("csr.canshuhuoshiyongfanwei");
//
//                    // 资源类型
//                    if (((String) hashmap.get("ATTRKEY")).equals(ziyuanleixing)) {
//                        String value = (String) hashmap.get("ATTRVALUE");
//                        if (StringUtils.equals(value, "GJ")) {
//                            value = "工具";
//                        } else if (StringUtils.equals(value, "GZ")) {
//                            value = "工装";
//                        } else if (StringUtils.equals(value, "SB")) {
//                            value = "设备";
//                        }
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                value);
//                        continue;
//                    }
//                    // 品牌
//                    else if (((String) hashmap.get("ATTRKEY")).equals(pinpai)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 品牌（必填）
//                    else if (((String) hashmap.get("ATTRKEY"))
//                            .equals(pinpaiMust)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 型号（必填）
//                    else if (((String) hashmap.get("ATTRKEY"))
//                            .equals(xinghaoMust)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 型号规格
//                    else if (((String) hashmap.get("ATTRKEY"))
//                            .equals(xinghaoguige)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 基本计量单位
//                    else if (((String) hashmap.get("ATTRKEY")).equals(unit)) {
//                        para.put("unit", (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 工具图片
//                    else if (((String) hashmap.get("ATTRKEY"))
//                            .equals(gongjutupian)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 图纸编号
//                    else if (((String) hashmap.get("ATTRKEY"))
//                            .equals(tuzhibianhao)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    }
//                    // 参数或使用范围
//                    else if (((String) hashmap.get("ATTRKEY")).equals(cshsyfw)) {
//                        properties.setProperty((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                        continue;
//                    } else {
//                        ibaMap.put((String) hashmap.get("ATTRKEY"),
//                                (String) hashmap.get("ATTRVALUE"));
//                    }
//                }
//
//                para.put("ibaMap", ibaMap);
//                Boolean isExist = WtUtil.isExistPart(
//                        (String) para.get("number"), false);
//                if (isExist) {
//                    Debug.P("this resource is exist");
//                    return "此资源已经存在";
//                } else {
//                    // 根据第一次发布找到基本信息创建resource
//                    CsrSpmUtil.createNewResource(para, properties);
//                }
//            }
//            if (resultSet != null) {
//                resultSet.close();
//            }
//            result = "操作成功";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return e.getLocalizedMessage();
//        } finally {
//            DBUtils.closeConnection();;
//        }
//        return result;
//    	
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
