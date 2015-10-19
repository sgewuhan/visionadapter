package ext.tmt.integration.webservice.spm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import ext.tmt.part.PartUtils;
import ext.tmt.utils.Debug;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.wip.WorkInProgressHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import ext.tmt.utils.Contants;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.Utils;

/**
 * SPM接口(ext.tmt.integration.webservice.spm.SPMWebservice)实现类
 * @author Administrator
 *
 */
public class SPMWebserviceImpl{
	


	
	
	 
    /**
     * 第一个流程的实现方法
     * @param workflow 流程ID
     * @param times 变更次数
     * @param factory 工厂
     * @return
     * @throws Exception
     */
    public static String processorForSpm1(String workflow, int times,String factory)throws Exception{
    	
    	Debug.P("processorForSpm1  paramaters -->"+ workflow + " --- " + times + " --- "+ factory);
    	// IBA属性集合
         Map<String,Object>  ibaMap = new HashMap<String,Object>();
   	
        //基础属性集合
        HashMap<String,String>  baseMap= new HashMap<String,String>();
        
        //Windchill字段映射集合
       Map<String,String> mapDatas= CsrSpmUtil. getWCMappingFieldBySPM();
       Debug.P("------->>>Mapping Collection:"+mapDatas.toString());
        
        ResultSet resultSet = null;
        String containerName = SPMConsts.SPMCONTAINER_NAME;//容器类型
        String result = "操作不成功";
        try {
            SessionHelper.manager.setAdministrator();
            //首次接口发布
            ConnectionPool connectionPool=DBFactory.getConnectionPool();//连接池
            if (workflow != null && times == 1) {
                String sql = "select * from PLM.CSR_WLSXSQ WL ,PLM.CSR_JSTZSX JS where JS.WORKFLOW = WL.WORKFLOW and JS.TIMES = '"
                        + 1 + "' AND WL.WORKFLOW = '" + workflow + "'";
                Debug.P("--->SQL:"+sql);
                resultSet =connectionPool.getConnection().executeQuery(sql);
                while (resultSet.next()) {
                    //基本属性
                    String material_num = resultSet.getString(SPMConsts.MATERIAL_NUM);//物料编码
                    String material_name = resultSet.getString(SPMConsts.MATERIAL_NAME);//物料名称 
                    if(StringUtils.isEmpty(material_name)||StringUtils.isEmpty(material_num)) continue;//过滤物料名称和编号为空的数据
                	String material_category = resultSet.getString(SPMConsts.MATERIAL_PATH);//物料小类路径
                    if(StringUtils.isNotEmpty(material_category)){
                    	material_category=SPMConsts.ROOT+material_category;
                    }
                	baseMap.put(SPMConsts.KEY_MATER_CATEGORY,material_category==null?"":material_category.trim());//物料小类全路径
                    baseMap.put(SPMConsts.KEY_CONTAINERNAME, containerName==null?"":containerName.trim());
                    baseMap.put(SPMConsts.KEY_NUMBER, material_num==null?"":material_num.trim());
                    baseMap.put(SPMConsts.KEY_NAME, material_name==null?"":material_name.trim());
                    
                    // Windchill 软属性
                	String att_key = resultSet.getString(SPMConsts.IBA_KEY);
                    String att_value = resultSet.getString(SPMConsts.IBA_VALUE);
                    if(StringUtils.isEmpty(att_key)) continue;
                    Debug.P("-->>IBA Key："+att_key+"="+att_value);
                    //过滤掉Windchill系统不存在的软属性
                    String contain_key=mapDatas.get(att_key);
                    if(StringUtils.isNotEmpty(contain_key)){//如果对应配置文件Key则存放
                    	if(SPMConsts.SPM_MATER_TYPE.equals(att_key)){//物料类型
                    		baseMap.put(mapDatas.get(att_key).trim(), mapDatas.get(att_value)==null?"":mapDatas.get(att_value).trim());
                    	}else{
                    		ibaMap.put(mapDatas.get(att_key).trim(),att_value==null?"":att_value.trim());//Windchill软属性
                    	}
                    }
                }
                 
                //判断物料的存在性 如果存在则更新新材物料,不存在则创建新材物料
                 String object_num=baseMap.get(SPMConsts.KEY_NUMBER);
                 WTPart object= hasExistObject(object_num);
                 String spm_fac=(String) ibaMap.get(SPMConsts.FACTORY);
                 Debug.P("---SPM Factory:"+spm_fac);
                 if(object!=null){
                	 String part_fac=(String) LWCUtil.getValue(object, SPMConsts.FACTORY);
                	 //过滤重复添加的工厂
                	 if(!part_fac.contains(spm_fac)){
                		 StringBuffer bf=new StringBuffer(spm_fac);
                    	 bf.append(",").append(part_fac);
                    	 ibaMap.put(SPMConsts.FACTORY, bf.toString());//扩建工厂属性
                	 }
                    CsrSpmUtil.updatePartInfo(object,baseMap, ibaMap);
                    Debug.P("--->>Update Part Factory:"+part_fac);
                    boolean isTMT=CsrSpmUtil.matchFactory(part_fac);
                    if(!isTMT){//非新材工厂(状态设置成已废弃)
                      GenericUtil.changeState(object, SPMConsts.DESPOSED);
                    }
                    Debug.P("----->>>Update WTPart("+object.getPersistInfo().getObjectIdentifier().getStringValue()+")Success!!");
                    result= "更新物料操作成功";
                 }else{
                	boolean iscreateTMT=CsrSpmUtil.matchFactory(spm_fac); 
                	Debug.P("---->>>创建的物料是否属于新材工厂:"+iscreateTMT);
                	if(iscreateTMT){//创建新材工厂
                	  String partType=baseMap.get(SPMConsts.PART_TYPE);
                	  Debug.P(">>>>partType:"+partType);
                       if(StringUtils.isNotEmpty(partType)){
                    	  Debug.P("---->>>Ready Create Part: Type="+partType);
                    	  CsrSpmUtil.createNewPart(baseMap, ibaMap,mapDatas);
                    	  result="创建物料成功";
                    	}else{
                    		result="创建物料(成品,半成品)成功";
                    	}
                	}else{
                		 result="非新材物料无法创建成功";
                	}
                 }
              } else if (workflow != null && (times == 2 || times == 3)) {
                String partNo = null;//需要更新的物料编号
                String sql = "select * from PLM.CSR_JSTZSX where TIMES = '" + times + "' AND WORKFLOW = '" + workflow + "'";
                Debug.P("--->>>SQL:"+sql);
                resultSet =connectionPool.getConnection().executeQuery(sql);
                while (resultSet.next()) {
                	//软属性,基本属性
                    String spm_category = resultSet.getString(SPMConsts.MATERIAL_PATH);//物料小类全路径
                    partNo = resultSet.getString(SPMConsts.MATERIAL_NUM);//物料编码
                   
                    String att_key = resultSet.getString(SPMConsts.IBA_KEY);//软属性Name
                    if(StringUtils.isEmpty(att_key)) continue;
                    String att_value = resultSet.getString(SPMConsts.IBA_VALUE);//软属性Value
    
                    Debug.P("IBA Key："+att_key+"    >>>IBA Value："+att_value);
                    //设置软属性集合(综合等级,物料类型,型号规格)
                    //过滤掉Windchill系统不存在的软属性
                    String contain_key=mapDatas.get(att_key);
                    if(StringUtils.isNotEmpty(contain_key)){//如果对应配置文件Key则存放
                         ibaMap.put(mapDatas.get(att_key).trim(), att_value==null?"":att_value.trim());
                      }
                      //分类属性
                    if(StringUtils.isNotEmpty(mapDatas.get(SPMConsts.MATERIAL_PATH))){
                    	ibaMap.put(mapDatas.get(SPMConsts.MATERIAL_PATH), spm_category);
                    }
                }//循环结束

                //更新软属性
                Debug.P("---->>>ibaMap is " + ibaMap.toString());
                if (!ibaMap.isEmpty()) {
                   CsrSpmUtil.updatePartForIba(partNo, ibaMap, "");
                }
                  result="二次更新物料成功!";
                  
                // 物料删除 Factory标识维护
                if (times == 3) {
                	Debug.P("------------>>>Times:"+times+"  PartNo: "+partNo);
                	if(StringUtils.isNotEmpty(partNo)){
//                        WTPart part = PartUtil.getLastPartbyNumViwe(partNo,Contants.DESIGN);
                        WTPart part = (WTPart)Utils.getWCObject(WTPart.class, partNo.toUpperCase().trim());
                       Debug.P("partNo-->"+part+"---factory--->"+factory);
                        if (part == null) {
                            result = "PLM不存在编号为(" + partNo + ")的部件。";
                            return result;
                        }
//                        if (StringUtils.isEmpty(factory)) {
//                            result = "工厂(factory)参数为空";
//                            return result;
//                        }

                        if (part != null) {
                        	 IBAUtils partIba=new IBAUtils(part);
                        	 String  _factory=partIba.getIBAValue(SPMConsts.FACTORY);//系统Factory软属性值
                            if (StringUtils.isEmpty(_factory)) {//如果物料不存在工厂,则设置为已废弃
                                GenericUtil.changeState((LifeCycleManaged)part, SPMConsts.DESPOSED);
                            } else {//存在新材工厂参数则删除指定参数信息
                            	Debug.P("------>>参数Factory:"+factory+"(用于删除)");
                            	boolean isTMT=CsrSpmUtil.matchFactory(_factory);
                                if(!isTMT){//如果不存在新材的工厂则修改状态为 已作废
                                  GenericUtil.changeState((LifeCycleManaged)part, SPMConsts.DESPOSED);
                                }
                            }
                               result = "删除工厂成功";
                        }
                	}
                }
            }
          } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } finally {
        	if (resultSet != null) {
                resultSet.close();
            }
            SessionHelper.manager.setAuthenticatedPrincipal("PM-RW");
        }
            Debug.P(">>>>Result:"+result);
            return result;
    }
    
    

    
    /**
     * 更新 扩建工厂物料
     * @param partNumber 部件编号
     * @param workflow 流程ID
     * @param mark 
     * @return
     */
    public static String processorForSpm2(String partNumber, String workflow,String mark)throws Exception{
    	Debug.P("---processorForSpm2--->>>partNumber:"+partNumber+"  ;workflow:"+workflow+"  ;mark:"+mark );
    	String result = "操作不成功";
        ResultSet resultSet = null;
    	// IBA属性集合
        Map<String,Object>  ibaMap = new HashMap<String,Object>();
        //基础属性集合
        HashMap<String,String>  baseMap= new HashMap<String,String>();
        //Windchill字段映射集合
        Map<String,String> mapDatas= CsrSpmUtil.getWCMappingFieldBySPM();
        Debug.P("------->>>Mapping Collection:"+mapDatas.toString());
   	    ConnectionPool connectionPool=DBFactory.getConnectionPool();//连接池
        try {
        	  SessionServerHelper.manager.setAccessEnforced(false);
        	  //先查询所属工厂是否为时代新材
                if(StringUtils.isNotEmpty(workflow)){
                    String sql = "select * from PLM.CSR_SXWH SX where SX.WORKFLOW = '" + workflow + "'";
                    Debug.P("--->>SQL:"+sql);
                    resultSet = connectionPool.getConnection().executeQuery( sql);
                    while (resultSet.next()) {//获取最新维护属性
                    	String spm_proNum=resultSet.getString(SPMConsts.MATERIAL_NUM);//物料编码
                    	baseMap.put(SPMConsts.KEY_NUMBER, spm_proNum);
                    	if(StringUtils.isEmpty(spm_proNum)){
                    		return "物料编码为空";
                    	}
                    	if(spm_proNum.equals(partNumber)){
                    		String classification=resultSet.getString(SPMConsts.MATERIAL_PATH);//物料小类
                    		String change_reason=resultSet.getString(SPMConsts.SPM_WLCONTENT);//变更原因
                    		String attr_key=resultSet.getString(SPMConsts.IBA_KEY);
                    		String attr_value=resultSet.getString(SPMConsts.IBA_VALUE);
                    		
                    		//物料名称
                    		if(SPMConsts.ATTKEY_NAME.equals(attr_key)){
                    			baseMap.put(SPMConsts.ATTKEY_NAME, attr_value==null?"":attr_value.trim());
                    		}
                    		
                    		//图纸编号
                    		if(SPMConsts.SPM_TUZHIBIANHAO.equals(attr_key)){
                    			baseMap.put(SPMConsts.SPM_TUZHIBIANHAO, attr_value==null?"":attr_value.trim());
                    		}
                    		
                    		String chek_key=mapDatas.get(attr_key);
                    		//其他配置的软属性
                    		if(StringUtils.isNotEmpty(chek_key)){
                    			ibaMap.put(mapDatas.get(attr_key).trim(), attr_value==null?"":attr_value.trim());
                    		}
                    		//物料分类属性
                    		if(StringUtils.isNotEmpty(mapDatas.get(SPMConsts.MATERIAL_PATH))){
                    			ibaMap.put(mapDatas.get(SPMConsts.MATERIAL_PATH), classification==null?"":classification.trim());
                    		}
                    	   
                    		//变更原因
                    		if(StringUtils.isNotEmpty(mapDatas.get(SPMConsts.SPM_WLCONTENT))){
                    			ibaMap.put(mapDatas.get(SPMConsts.SPM_WLCONTENT), change_reason==null?"":change_reason.trim());
                    		}
                    	}	
                    }//循环结束
                    
                    String materNo=baseMap.get(SPMConsts.KEY_NUMBER);
                    String spm_factory=(String) ibaMap.get(SPMConsts.FACTORY);//SPM Factory
                    Debug.P("---->>>materNo:"+materNo+"   SPM Factory："+spm_factory);
                 	WTPart part=PartUtils.getPartByNumber(materNo);
                 	Debug.P(">>>>Updata Part Info:"+part);
                 	if(part!=null){//历史物料
                        //默认单位
                        String defaultUnit=(String) ibaMap.get(SPMConsts.KEY_UNIT);
                        Debug.P("--->>>>defaultUnit:"+defaultUnit);
                        if(StringUtils.isNotEmpty(defaultUnit)){
                            if(defaultUnit.equalsIgnoreCase(SPMConsts.EA)){
                           	 defaultUnit=SPMConsts.EA;
                           	 ibaMap.put(SPMConsts.KEY_UNIT, defaultUnit);
                            }
                        }
                        
            		//如果变更Name则更新物料名称
             		String mater_name=baseMap.get(SPMConsts.ATTKEY_NAME);
             		if(StringUtils.isNotEmpty(mater_name)){//名称不一致则修改
             			CsrSpmUtil.changePartName(part, mater_name.trim());
             		}
             		
             		//更新描述文档信息(描述文档图纸编号)
                     if(StringUtils.isNotEmpty(baseMap.get(SPMConsts.SPM_TUZHIBIANHAO))){
                     	Debug.P("---->>Update TZ Described:"+baseMap.get(SPMConsts.SPM_TUZHIBIANHAO));
                     	CsrSpmUtil.updateDescribedDocument(part,baseMap.get(SPMConsts.SPM_TUZHIBIANHAO).trim());
                     }
                     
                     if(part!=null){
                 		if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(part, wt.session.SessionHelper.manager.getPrincipal()))
                 			part = (WTPart) WorkInProgressHelper.service.checkin(part, "update Part Info");
                       }
                 	 //更新物料信息
               	     Debug.P("--Mark:更新-->>>UpdateIBA:"+ibaMap);
               	     String org_fac=(String) LWCUtil.getValue(part, SPMConsts.FACTORY);
               	     //过滤重复添加的工厂
               	     if(StringUtils.isNotEmpty(org_fac)&&StringUtils.isNotEmpty(spm_factory)&&!org_fac.contains(spm_factory)){
               	    	 StringBuffer sbf=new StringBuffer(spm_factory);
                  	     sbf.append(",").append(org_fac);
                  	     ibaMap.put(SPMConsts.FACTORY, sbf.toString());
               	     }
               	     CsrSpmUtil.updateWTPartIBA(part,ibaMap);
            		 Debug.P("---Mark:更新-->>>UpdateIBA-->>>Success!");
                     
             		//更新生命周期状态(如果工厂变更为非时代新材物料则设置成已作废)
              		String partFactory=(String) LWCUtil.getValue(part, SPMConsts.FACTORY);
              		boolean isDel=CsrSpmUtil.matchFactory(partFactory);//与PM系统匹配
              		if(!isDel){
              			part=(WTPart) GenericUtil.changeState(part, SPMConsts.DESPOSED);
              		}
              		     result="更新物料操作成功";
                 	}else {//扩建物料
                 		Debug.P("----->>>>>扩建TMT工厂物料:"+materNo);
                 		 String mater_factory=(String) ibaMap.get(SPMConsts.FACTORY);
                         boolean isTmT=CsrSpmUtil.matchFactory(mater_factory);
                 		 if(isTmT){//包含时代工厂则创建
                 			Debug.P("--->>New001  扩建工厂:"+mater_factory+" 创建物料.");
                 			processorForSpm1(workflow, 1, mater_factory);
                 			result="扩建TMT物料操作成功";
                 			return result;
                 		}
                 	}
                }   
		} catch (Exception e) {
			 e.printStackTrace();
	         return e.getLocalizedMessage();
		}finally{
		   	if (resultSet != null) {
                resultSet.close();
            }
            SessionServerHelper.manager.setAccessEnforced(true);
		}
              Debug.P("--->>>>>result11:"+result);
    	      return result;
    }

    
    

//    
//    /**
//     * 获取创建该部件时的workflow
//     * 
//     * @return
//     * @throws SQLException
//     */
//    private static String getCreateWorkflow(String partNumber,ConnectionPool connectionPool)throws Exception {
//        String workflow = null;
//        if (StringUtils.isEmpty(partNumber)) {
//            return workflow;
//        }
//        try {
//            String sql = "select workflow from csr_wlsxsq where wlnumber='"+ partNumber + "' order by sqdate desc";
//            ResultSet resultSet = connectionPool.getConnection().executeQuery(sql);
//            while (resultSet.next()) {
//                workflow = resultSet.getString("workflow");
//                return workflow;
//            }
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//        return workflow;
//    }

    
//    /**
//     * 获取更新历史记录
//     * 
//     * @param partNumber
//     * @param conn
//     * @return
//     * @throws SQLException
//     */
//
//    private static List<String> getUpdateHistry(String partNumber,ConnectionPool connectionPool) throws SQLException {
//        List<String> workflows = new ArrayList<String>();
//        if (StringUtils.isEmpty(partNumber)) {
//            return workflows;
//        }
//        String sql = "select workflow from CSR_SXWH where wlnumber = '"+ partNumber + "' order by cdate";
//        ResultSet resultSet = connectionPool.getConnection().executeQuery(sql);
//        while (resultSet.next()) {
//            String workflow = resultSet.getString("workflow");
//            workflows.add(workflow);
//        }
//           return workflows;
//    }
    

    /**
     * 为部件创建关联文档
     * @param partNumber
     * @param workflow 流程ID
     * @param factory 工厂
     * @return
     * @throws Exception
     */
    public static String createDocForPart(String partNumber, String workflow,String factory) throws Exception{
       Debug.P("--createDocForPart-->>>为部件创建关联文档 partNumber=[" + partNumber + "] workflow=["+ workflow + "] factory=[" + factory + "]");
       WTPart part = hasExistObject(partNumber);
       String resultInfo = "操作成功";
       if(part != null){
       ResultSet resultSet = null;
       try {
    	   SessionServerHelper.manager.setAccessEnforced(false);
    	   if (partNumber != null) {
    		   ConnectionPool connectionPool=DBFactory.getConnectionPool();//连接池
    		   String sql = "select WLNUMBER,DOCNAME,DOCTYPE,LOCATION,DOCNUMBER from PLM.CSR_FJJL where WLNUMBER = '"+ partNumber + "'";
    		   Debug.P("--->SQL:"+sql);
    		   resultSet = connectionPool.getConnection().executeQuery(sql);
    		   while (resultSet.next()) {
    			  String spm_num = resultSet.getString(SPMConsts.MATERIAL_NUM);
    			  if(StringUtils.isEmpty(spm_num)){
    				  return "物料编号为空";
    			  }
//    		      WTPart part=null;
			      try {
			    	  part= (WTPart) GenericUtil.getObjectByNumber(spm_num);
				     } catch (Exception e) {
					    Debug.P("---->>>Number为("+spm_num+")部件在Windchill中不存在!");
					    continue;
				   }
    			  Debug.P("---->>>SPM_NUM="+spm_num);
    			  //基本属性集合
    			  HashMap<String, String> baseMap = new HashMap<String, String>();
    	
    			  if(spm_num.equals(partNumber)){
    				  String docName=resultSet.getString(SPMConsts.SPM_DOCNAME);
    				  String docNumber=resultSet.getString(SPMConsts.SPM_DOCNUMBER);
    			      String location_path=resultSet.getString(SPMConsts.SPM_LOCATION);
    			
    			      if(part!=null){
    			    	   Debug.P("----->>>DOC_WTPart:"+part.getPersistInfo().getObjectIdentifier().getStringValue()+"   SPMDocNum:"+docNumber);
    			    	   //文档创建到部件同目录下
    			    	   String folderPath= part.getFolderPath();
    			    	   folderPath=folderPath.substring(0,folderPath.lastIndexOf("/"));
    			    	   String containerName=part.getContainerName();
    			    	   Debug.P("----->>Part("+partNumber+")的文件夹路径:"+folderPath+"  ;containerName:"+containerName);
    			    	   baseMap.put(SPMConsts.KEY_NUMBER, docNumber==null?"":docNumber.trim());
    			    	   baseMap.put(SPMConsts.KEY_FOLDER, folderPath==null?"":folderPath.trim()); 
    			    	   baseMap.put(SPMConsts.KEY_NAME, docName==null?"":docName.trim());//文档名称
    	    			   baseMap.put(SPMConsts.KEY_LOCATION_PATH, location_path==null?"":location_path.trim());//主文档路径
    	    			   baseMap.put(SPMConsts.KEY_CONTAINERNAME, containerName==null?"":containerName.trim());
    	    			   
    	    			   //创建文档对象信息
    	    			   WTDocument doc=CsrSpmUtil.createNewDocument(baseMap, null);
    	    			   if(doc!=null){//建立文档部件的描述关系
    	    				   PartUtil.createDescriptionLink(part, doc);
    	    			   }
    			      }
    			  }
    		   }//循环结束
    	   }
	     } catch (Exception e) {
		     e.printStackTrace();
		     resultInfo="操作失败";
	     }finally{
	    	  if (resultSet != null) {
	             resultSet.close();
	           }
	          SessionServerHelper.manager.setAccessEnforced(true);
	      }
       }
       return resultInfo;
    }

    


    /**
     * 为部件修改关联文档
     */
    public static String updateDocForPart(String partNumber, String workflow)throws Exception{
    	
    	Debug.P("---->>>updateDocForPart Number: " + partNumber + "workflow is " + workflow);
    	//基本属性集合
    	 HashMap<String, String> baseMap = new HashMap<String, String>();
    	 //IBA属性集合
//    	 HashMap<String, String> ibaMap = new HashMap<String, String>();
    	 ResultSet resultSet = null;
         String  resultInfo = "";
         try {
        	 SessionServerHelper.manager.setAccessEnforced(false);
        	 if (partNumber != null) {
        		 ConnectionPool connectionPool=DBFactory.getConnectionPool();//连接池
        		 String sql = "select WLNUMBER,DOCNAME,DOCTYPE,LOCATION,DOCNUMBER from PLM.CSR_FJJL where WLNUMBER = '" + partNumber + "'";
                 Debug.P("--->>SQL:"+sql);
                 resultSet = connectionPool.getConnection().executeQuery(sql);
                 while (resultSet.next()) {
                	 String spm_prodnum=resultSet.getString(SPMConsts.MATERIAL_NUM);
                	 String doc_number=resultSet.getString(SPMConsts.SPM_DOCNUMBER);
                	 if(StringUtils.isEmpty(spm_prodnum)){
                		 return "物料编码为空";
                	 }
                	
                	 Debug.P("-----SPM>>>WLNUMBER("+partNumber+")对应的属性字段DOCNUMBER为["+doc_number+"]");
                	 baseMap.put(SPMConsts.KEY_NUMBER, doc_number==null?"":doc_number.trim());
                	 if(partNumber.equals(spm_prodnum)){
                		 String docName=resultSet.getString(SPMConsts.SPM_DOCNAME);
       			         String location_path=resultSet.getString(SPMConsts.SPM_LOCATION);
       			         WTPart part=null;
    			         try {
    			    	    part= (WTPart) GenericUtil.getObjectByNumber(spm_prodnum);
					      } catch (Exception e) {
						     Debug.P("---->PART_ERROR:Num=("+spm_prodnum+")部件在Windchill中不存在!");
						     continue;
					     }
    			         
    				      if(part!=null){
       			    	   Debug.P("----->>>DOC_WTPart:"+part.getPersistInfo().getObjectIdentifier().getStringValue());
       			    	   //文档创建到部件同目录下
       			    	   String folderPath=FolderHelper.service.getFolderPath(part);
       			    	   String containerName=part.getContainerName();
       			    	   Debug.P("----->>Part("+partNumber+")的文件夹路径:"+folderPath+"  ;containerName:"+containerName);
       			    	   baseMap.put(SPMConsts.KEY_FOLDER, folderPath==null?"":folderPath.trim()); 
       			    	   baseMap.put(SPMConsts.KEY_NAME, docName==null?"":docName.trim());//文档名称
       	    			   baseMap.put(SPMConsts.KEY_LOCATION_PATH, location_path==null?"":location_path.trim());//主文档路径
       	    			   baseMap.put(SPMConsts.KEY_CONTAINERNAME, containerName==null?"":containerName.trim());
       	    		       
       	    			     
       	    			      //更新文档内容
       	    			      if(StringUtils.isNotEmpty(doc_number)){
       	    			       WTDocument doc=(WTDocument) GenericUtil.getObjectByNumber(doc_number);
         			             if(doc!=null){//文档对象为空
         			            	 CsrSpmUtil.reviseDocument(doc, baseMap, null);
         			             }
       	    			    	resultInfo="更新部件关联的文档成功";
       	    			      }else{
       	    			    	resultInfo="部件未更改";
       	    			      }
    				      }
                	 }
                 }
           }
		} catch(SQLException e){
			e.printStackTrace();
		}catch (Exception e) {
			 e.printStackTrace();
			 throw new Exception("Windchill更新("+partNumber+")部件关联的文档异常");
		}finally{
			  if (resultSet != null) {
		           resultSet.close();
		        }
		     SessionServerHelper.manager.setAccessEnforced(false);
		}
    	  return   resultInfo;
    }
    
    
    /**
     * （暫時未使用）
     * SPM调用接口判断物料在PLM系统是否存在技术规格书
     * @return 0. 表示不存在技术规格书 1.表示存在技术规格书
     * @throws Exception 
     */
    public static String getJSGGSByPartNumber(String partNumber) throws Exception {
    	 Debug.P("---getJSGGSByPartNumber--->>partNumber："+partNumber);
    	 String result = "0";//默认不存在
         if (StringUtils.isEmpty(partNumber)){
        	 return "物料编号参数为空";
         }
         try {
        	 SessionHelper.manager.setAdministrator();
             WTPart part =(WTPart) GenericUtil.getObjectByNumber(partNumber);
             if(part!=null){
            	  QueryResult qr = WTPartHelper.service .getDescribedByWTDocuments(part);
            	  while(qr.hasMoreElements()){//判断Windchill是否存在技术规格文档
            		  WTDocument doc=(WTDocument) qr.nextElement();
            		  String type=GenericUtil.getTypeName(doc);
            		  Debug.P("--->>DocType:"+type);
            		  if(StringUtils.equals(type, SPMConsts.TECHNICAL_SPEC)){//技术规格书
            			  result="1";
            		  }
            	  }
             }
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Windchill查询("+partNumber+")关联的技术规格书异常");
		}finally{
			 SessionServerHelper.manager.setAccessEnforced(true);
		}
    	  return  result;
    }

    /**
     * （暫時未使用）
     * SPM调用接口判断物料在PLM系统是否存在产品手册
     * @return 0. 表示不存在产品手册 1.表示存在产品手册
     */
    public static String getCPSCByPartNumber(String partNumber) throws Exception {
    	Debug.P("--getCPSCByPartNumber-->>>partNumber："+partNumber);
    	 String result = "0";//默认不存在
         if (StringUtils.isEmpty(partNumber)){
        	 return "物料编号参数为空";
         }
         try {
        	 SessionServerHelper.manager.setAccessEnforced(false);
        	   WTPart part =(WTPart) GenericUtil.getObjectByNumber(partNumber);
        	   QueryResult qr = WTPartHelper.service .getDescribedByWTDocuments(part);
         	  while(qr.hasMoreElements()){//判断Windchill是否存在技术规格文档
        		  WTDocument doc=(WTDocument) qr.nextElement();
        		  String type=GenericUtil.getTypeName(doc);
        		  Debug.P("--->>DocType:"+type);
        		  if(StringUtils.equals(type, SPMConsts.PRODUCT_SPEC)){//产品手册
        			  result="1";
        		  }
        	  }
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Windchill查询("+partNumber+")关联的技术规格书异常");
		}finally{
			 SessionServerHelper.manager.setAccessEnforced(true);
		}
    	    return result;
    }
    
	
    /**
     * （暫時未使用）
     * 检查物料是否在存储库中存在
     * 0. 表示不存在存储库 1.表示存在存储库
     */
    public static String checkPartFromLibrary(String partNumber)throws Exception {
     	Debug.P("--checkPartFromLibrary-->>>partNumber："+partNumber);
    	 String result = "0";//默认不存在
         if (StringUtils.isEmpty(partNumber)){
        	 return "物料编号参数为空";
         }
         try {
        	 SessionServerHelper.manager.setAccessEnforced(false);
        	 WTPart part =(WTPart) GenericUtil.getObjectByNumber(partNumber);
        	 String partContainer = part.getContainerName();
        	 Debug.P("-->Part state is " + part.getLifeCycleState().getDisplay().toString()+"  ;Part ContainerName:"+partContainer);
        	 if(StringUtils.equals(partContainer, SPMConsts.SPMCONTAINER_NAME)){
        	   	 result="1";
        	 }
         } catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Windchill查询("+partNumber+")是否存在存储库中异常");
		}finally{
			 SessionServerHelper.manager.setAccessEnforced(true);
		}
    	  return result;
    }

    
    
    
    /**
     * 判断物料对象的存在性
  * @throws WTException 
     */
    private static WTPart hasExistObject(String num) throws Exception{
 	    if(StringUtils.isEmpty(num)) throw new Exception("NUMBER属性字段参数值为空");
 	    try {
 	    	 WTPart object=PartUtils.getPartByNumber(num);
 	         return object;
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Windchill查询对象编号("+num+")异常!");
			}
    }
       
       
       
       
   
       

       
       
       

       
}
