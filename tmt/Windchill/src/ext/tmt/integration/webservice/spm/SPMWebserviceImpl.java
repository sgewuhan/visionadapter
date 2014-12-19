package ext.tmt.integration.webservice.spm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

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
import wt.part.WTPart;
import wt.part.WTPartHelper;
import ext.tmt.utils.Contants;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;


public class SPMWebserviceImpl{
	


	
	
	 
    /**
     * ��һ�����̵�ʵ�ַ���
     * @param workflow ����ID
     * @param times �������
     * @param factory ����
     * @return
     * @throws Exception
     */
    public static String processorForSpm1(String workflow, int times,String factory)throws Exception{
    	
    	Debug.P("processorForSpm1  paramaters -->"+ workflow + " --- " + times + " --- "+ factory);
    	// IBA���Լ���
         Map<String,Object>  ibaMap = new HashMap<String,Object>();
   	
        //�������Լ���
        HashMap<String,String>  baseMap= new HashMap<String,String>();
        
        //Windchill�ֶ�ӳ�伯��
       Map<String,String> mapDatas= getWCMappingFieldBySPM();
       Debug.P("------->>>Mapping Collection:"+mapDatas.toString());
        
        ResultSet resultSet = null;
        String containerName = SPMConsts.SPMCONTAINER_NAME;//��������
        String result = null;
        ConnectionPool connectionPool=DBFactory.getConnectionPool();//���ӳ�
        try {
            SessionHelper.manager.setAdministrator();
            //�״νӿڷ���
            if (workflow != null && times == 1) {
                String sql = "select * from CSR_WLSXSQ,CSR_JSTZSX where CSR_JSTZSX.WORKFLOW = CSR_WLSXSQ.WORKFLOW and CSR_JSTZSX.TIMES = '"
                        + 1 + "' AND CSR_WLSXSQ.WORKFLOW = '" + workflow + "'";
                Debug.P("--->SQL:"+sql);
                resultSet =connectionPool.getConnection().executeQuery(sql);
                while (resultSet.next()) {
                    
                    //��������
                    String material_num = resultSet.getString(SPMConsts.MATERIAL_NUM);//���ϱ���
                    String material_name = resultSet.getString(SPMConsts.MATERIAL_NAME);//�������� 
//                    Debug.P("---processorForSpm1-->>>>Material Number:"+material_num+"  MaterialName:"+material_name);
                    if(StringUtils.isEmpty(material_name)||StringUtils.isEmpty(material_num)) continue;//�����������ƺͱ��Ϊ�յ�����
                	String material_category = resultSet.getString(SPMConsts.MATERIAL_PATH);//����С��·��
                    if(StringUtils.isNotEmpty(material_category)){
                    	material_category=SPMConsts.ROOT+material_category;
                    }
                	baseMap.put(SPMConsts.KEY_MATER_CATEGORY,material_category==null?"":material_category.trim());//����С��ȫ·��
                    baseMap.put(SPMConsts.KEY_CONTAINERNAME, containerName==null?"":containerName.trim());
                    baseMap.put(SPMConsts.KEY_NUMBER, material_num==null?"":material_num.trim());
                    baseMap.put(SPMConsts.KEY_NAME, material_name==null?"":material_name.trim());
                    
                    // Windchill ������
                	String att_key = resultSet.getString(SPMConsts.IBA_KEY);
                    String att_value = resultSet.getString(SPMConsts.IBA_VALUE);
                    if(StringUtils.isEmpty(att_key)) continue;
                    Debug.P("-->>IBA Key��"+att_key+"="+att_value);
                    //���˵�Windchillϵͳ�����ڵ�������
                    String contain_key=mapDatas.get(att_key);
                    if(StringUtils.isNotEmpty(contain_key)){//�����Ӧ�����ļ�Key����
                    	if(SPMConsts.SPM_MATER_TYPE.equals(att_key)){//��������
                    		baseMap.put(mapDatas.get(att_key).trim(), mapDatas.get(att_value)==null?"":mapDatas.get(att_value).trim());
                    	}else{
                    		ibaMap.put(mapDatas.get(att_key).trim(),att_value==null?"":att_value.trim());//Windchill������
                    	}
                    }
                }
                    //�ж����ϵĴ�����
                   if(!baseMap.isEmpty()){
                       String object_num=baseMap.get(SPMConsts.KEY_NUMBER);
                       Persistable object= hasExistObject(object_num);
                       if (object!=null) {
                           Debug.P("------->>>>this WTPart("+object_num+") has exist!");
                           if(object instanceof WTPart){
                          	   WTPart partObject=(WTPart)object;
                               CsrSpmUtil.updatePartInfo(partObject,baseMap, ibaMap);
                               Debug.P("----->>>Update WTPart("+partObject.getPersistInfo().getObjectIdentifier().getStringValue()+")Success!!");
                               result= "�������ϳɹ�";
                           }
                      } else {
                        try {
                            // ���ݵ�һ�η����ҵ�������Ϣ����part
                        	//�����������δ��ӳ���򲻴���
                        	String partType=baseMap.get(SPMConsts.PART_TYPE);
                        	if(StringUtils.isNotEmpty(partType)){
                        	   	Debug.P("---->>>Ready Create Part: Type="+partType);
                        		CsrSpmUtil.createNewPart(baseMap, ibaMap,mapDatas);
                        		result="�������ϳɹ�";
                        	}
                        } catch (Exception e) {
                            return e.getLocalizedMessage();// ֱ�ӽ�������Ϣ���з���
                        }
                     }
                   }
              } else if (workflow != null && (times == 2 || times == 3)) {
                String partNo = null;//��Ҫ���µ����ϱ��
                String sql = "select * from CSR_JSTZSX where TIMES = '" + times + "' AND WORKFLOW = '" + workflow + "'";
                resultSet =connectionPool.getConnection().executeQuery(sql);
                while (resultSet.next()) {
                	//������,��������
//               String spm_workflow = resultSet.getString(SPMConsts.SPM_WORKFLOW);//��������ID
//               String spm_creator = resultSet.getString(SPMConsts.SPM_CREATOR);//���̴�����
                    String spm_category = resultSet.getString(SPMConsts.MATERIAL_PATH);//����С��ȫ·��
                    partNo = resultSet.getString(SPMConsts.MATERIAL_NUM);//���ϱ���
                    String spm_name=resultSet.getString(SPMConsts.MATERIAL_NAME);//��������
                    baseMap.put(SPMConsts.KEY_NAME, spm_name);
                    
                    String att_key = resultSet.getString(SPMConsts.IBA_KEY);//������Name
                    if(StringUtils.isEmpty(att_key)) continue;
                    String att_value = resultSet.getString(SPMConsts.IBA_VALUE);//������Value
    
                    //���������Լ���(�ۺϵȼ�,��������,�ͺŹ��)
                    //���˵�Windchillϵͳ�����ڵ�������
                    String contain_key=mapDatas.get(att_key);
                    if(StringUtils.isNotEmpty(contain_key)){//�����Ӧ�����ļ�Key����
                         ibaMap.put(mapDatas.get(att_key).trim(), att_value==null?"":att_value.trim());
                      }
                      //��������
                    if(StringUtils.isNotEmpty(mapDatas.get(SPMConsts.MATERIAL_PATH))){
                    	ibaMap.put(mapDatas.get(SPMConsts.MATERIAL_PATH), spm_category);
                    }
                }//ѭ������

                //����������
                Debug.P("---->>>ibaMap is " + ibaMap.toString());
                if (!ibaMap.isEmpty()) {
                   CsrSpmUtil.updatePartForIba(partNo, ibaMap, "");
                }

                // ����ɾ�� ��ʶά��
                if (times == 3) {
                	Debug.P("------------>>>Times:"+times+"  PartNo:"+partNo);
                	if(StringUtils.isNotEmpty(partNo)){
                        WTPart part = PartUtil.getLastPartbyNumViwe(partNo,Contants.DESIGN);
                        if (part == null) {
                            result = "PLM�����ڱ��Ϊ(" + partNo + ")Design��ͼ�Ĳ�����";
                            return result;
                        }
                        if (StringUtils.isEmpty(factory)) {
                            result = "����(factory)����Ϊ��";
                            return result;
                        }

                        if (part != null) {
                        	 IBAUtils partIba=new IBAUtils(part);
                        	 String  _factory=partIba.getIBAValue(SPMConsts.FACTORY);//ϵͳFactory������ֵ
                             Set<String> factory_set=splitStr2Set(_factory,",");
                            if (StringUtils.isEmpty(_factory)) {//������ϲ����ڹ���,������Ϊ�ѷ���
                                GenericUtil.changeState(part, SPMConsts.DESPOSED);
                            } else {//���ڹ���������ɾ��ָ��������Ϣ
                            	Debug.P("------>>����Factory:"+factory+"(����ɾ��)");
                                String[] factories = factory.split(",");
                                if (factories != null && factories.length > 0) {
                                    for (int i = 0; i < factories.length; i++) {
                                        String fac = factories[i];
                                        if(factory_set.contains(fac)){//���������ɾ��???(�Ƿ���������һ��)
                                        	factory_set.remove(fac);
                                        }
                                    }
                                }
                                
                                //��Set�����ת�����ַ���
                                String factory_iba=setCollection2Str(factory_set);
                                List<String> fac_res=CsrSpmUtil.getAllPMFactory();
                                if(!fac_res.contains(fac_res)){//����������²ĵĹ������޸�״̬Ϊ ������
                                	  GenericUtil.changeState(part, SPMConsts.DESPOSED);
                                }
                               //������������IBA����
                                IBAUtils iba=new IBAUtils(part);
                                iba.setIBAValue(SPMConsts.FACTORY, factory_iba==null?"":factory_iba);
                                iba.updateIBAPart(part);
                            }
                               result = "ɾ�������ɹ�";
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
            return result;
    }
    
    

    
    /**
     * ���� ������������
     * @param partNumber �������
     * @param workflow ����ID
     * @param mark 
     * @return
     */
    public static String processorForSpm2(String partNumber, String workflow,String mark)throws Exception{
    	Debug.P("---processorForSpm2--->>>partNumber:"+partNumber+"  ;workflow:"+workflow+"  ;mark:"+mark );
    	String result = null;
        ResultSet resultSet = null;
    	// IBA���Լ���
        Map<String,Object>  ibaMap = new HashMap<String,Object>();
        //�������Լ���
        HashMap<String,String>  baseMap= new HashMap<String,String>();
        //Windchill�ֶ�ӳ�伯��
        Map<String,String> mapDatas= getWCMappingFieldBySPM();
        Debug.P("------->>>Mapping Collection:"+mapDatas.toString());
   	    ConnectionPool connectionPool=DBFactory.getConnectionPool();//���ӳ�
        try {
        	  SessionServerHelper.manager.setAccessEnforced(false);
        	  //�Ȳ�ѯ���������Ƿ�Ϊʱ���²�
                if(StringUtils.isNotEmpty(workflow)){
                    String sql = "select * from CSR_SXWH where WORKFLOW = '" + workflow + "'";
                    Debug.P("--->>SQL:"+sql);
                    resultSet = connectionPool.getConnection().executeQuery( sql);
                    while (resultSet.next()) {//��ȡ����ά������
                    	String spm_proNum=resultSet.getString(SPMConsts.MATERIAL_NUM);//���ϱ���
                    	Debug.P("---processorForSpm2-->>>>Material Number:"+spm_proNum);
                    	baseMap.put(SPMConsts.KEY_NUMBER, spm_proNum);
                    	if(StringUtils.isEmpty(spm_proNum)){
                    		return "���ϱ���Ϊ��";
                    	}
                    	if(spm_proNum.equals(partNumber)){
                    		String classification=resultSet.getString(SPMConsts.MATERIAL_PATH);//����С��
                    		String change_reason=resultSet.getString(SPMConsts.SPM_WLCONTENT);//���ԭ��
                    		String attr_key=resultSet.getString(SPMConsts.IBA_KEY);
                    		String attr_value=resultSet.getString(SPMConsts.IBA_VALUE);
                    		
                    		//��������
                    		if(SPMConsts.ATTKEY_NAME.equals(attr_key)){
                    			baseMap.put(SPMConsts.ATTKEY_NAME, attr_value==null?"":attr_value.trim());
                    		}
                    		
                    		//ͼֽ���
                    		if(SPMConsts.SPM_TUZHIBIANHAO.equals(attr_key)){
                    			baseMap.put(SPMConsts.SPM_TUZHIBIANHAO, attr_value==null?"":attr_value.trim());
                    		}
                    		
                    		String chek_key=mapDatas.get(attr_key);
                    		//�������õ�������
                    		if(StringUtils.isNotEmpty(chek_key)){
                    			ibaMap.put(mapDatas.get(attr_key).trim(), attr_value==null?"":attr_value.trim());
                    		}
                    		//���Ϸ�������
                    		if(StringUtils.isNotEmpty(mapDatas.get(SPMConsts.MATERIAL_PATH))){
                    			ibaMap.put(mapDatas.get(SPMConsts.MATERIAL_PATH), classification==null?"":classification.trim());
                    		}
                    	   
                    		//���ԭ��
                    		if(StringUtils.isNotEmpty(mapDatas.get(SPMConsts.SPM_WLCONTENT))){
                    			ibaMap.put(mapDatas.get(SPMConsts.SPM_WLCONTENT), change_reason==null?"":change_reason.trim());
                    		}
                    	}	
                    }//ѭ������
                    
                    String materNo=baseMap.get(SPMConsts.KEY_NUMBER);
                 	WTPart part=(WTPart) GenericUtil.getObjectByNumber(materNo);
                 	if(part!=null){//��ʷ����
                        //Ĭ�ϵ�λ
                        String defaultUnit=(String) ibaMap.get(SPMConsts.KEY_UNIT);
                        Debug.P("--->>>>defaultUnit:"+defaultUnit);
                        if(StringUtils.isNotEmpty(defaultUnit)){
                            if(defaultUnit.equalsIgnoreCase(SPMConsts.EA)){
                           	 defaultUnit=SPMConsts.EA;
                           	 ibaMap.put(SPMConsts.KEY_UNIT, defaultUnit);
                            }
                        }
                 	 //����������Ϣ
               	     Debug.P("--Mark:����-->>>UpdateIBA:"+ibaMap);
                     part=(WTPart) GenericUtil.checkout(part);
                     LWCUtil.setValue(part, ibaMap);
            		 Debug.P("---Mark:����-->>>UpdateIBA-->>>Success!");
            			//������Name�������������
             		String mater_name=baseMap.get(SPMConsts.ATTKEY_NAME);
             		if(StringUtils.isNotEmpty(mater_name)){//���Ʋ�һ�����޸�
             			CsrSpmUtil.changePartName(part, mater_name.trim());
             		}
             		
             		//���������ĵ���Ϣ(�����ĵ�ͼֽ���)
                     if(StringUtils.isNotEmpty(baseMap.get(SPMConsts.SPM_TUZHIBIANHAO))){
                     	Debug.P("---->>Update TZ Described:"+baseMap.get(SPMConsts.SPM_TUZHIBIANHAO));
                     	CsrSpmUtil.updateDescribedDocument(part,baseMap.get(SPMConsts.SPM_TUZHIBIANHAO).trim());
                     }
                     
                     if(part!=null){
                 		if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(part, wt.session.SessionHelper.manager.getPrincipal()))
                 			part = (WTPart) WorkInProgressHelper.service.checkin(part, "update Part Info");
                       }
                     
             		//������������״̬(����������Ϊ��ʱ���²����������ó�������)
              		String partFactory=(String) LWCUtil.getValue(part, SPMConsts.FACTORY);
              		boolean isDel=matchFactory(partFactory);//��PMϵͳƥ��
              		if(!isDel){
              			part=(WTPart) GenericUtil.changeState(part, SPMConsts.DESPOSED);
              			PersistenceHelper.manager.refresh(part);
              		}
                 	}else {//��������
                 		 String mater_factory=(String) ibaMap.get(SPMConsts.FACTORY);
                         boolean hasexisted=matchFactory(mater_factory);
                 		 if(hasexisted){//����ʱ�������򴴽�
                 			Debug.P("--->>��������:"+mater_factory+" ��������.");
                 			processorForSpm1(workflow, 1, mater_factory);
                 		}
                 	}
                       result="�����ɹ�";
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
    	     return result;
    }

    
    

    /**
     * ƥ���²Ĺ���
     * @param factorys
     * @return
     * @throws Exception
     */
    private  static  boolean matchFactory(String factorys) throws Exception{
        //��ȡPM�Ĺ�����Ϣ
        List<String> fac_res=CsrSpmUtil.getAllPMFactory();
	    Debug.P("--matchFactory-->>>ʱ���²ĵĹ���:"+fac_res+"  SPM Factory:"+factorys);
       boolean flag=false;//Ĭ�ϲ�����ʱ���²�
       if(StringUtils.isNotEmpty(factorys)){
         List<String> maters_list=Arrays.asList(factorys.split(","));
         for (String str : maters_list) {
			   if(fac_res.contains(str)){
				   Debug.P("------>>>>TMT Factory:"+str);
				   flag=true;
				   break;
			   }
		   }
       }
         return flag;
    }
    
    
    /**
     * ��ȡ�����ò���ʱ��workflow
     * 
     * @return
     * @throws SQLException
     */
    private static String getCreateWorkflow(String partNumber,ConnectionPool connectionPool)throws Exception {
        String workflow = null;
        if (StringUtils.isEmpty(partNumber)) {
            return workflow;
        }
        try {
            String sql = "select workflow from csr_wlsxsq where wlnumber='"+ partNumber + "' order by sqdate desc";
            ResultSet resultSet = connectionPool.getConnection().executeQuery(sql);
            while (resultSet.next()) {
                workflow = resultSet.getString("workflow");
                return workflow;
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
        return workflow;
    }

    
    /**
     * ��ȡ������ʷ��¼
     * 
     * @param partNumber
     * @param conn
     * @return
     * @throws SQLException
     */

    private static List<String> getUpdateHistry(String partNumber,ConnectionPool connectionPool) throws SQLException {
        List<String> workflows = new ArrayList<String>();
        if (StringUtils.isEmpty(partNumber)) {
            return workflows;
        }
        String sql = "select workflow from CSR_SXWH where wlnumber = '"+ partNumber + "' order by cdate";
        ResultSet resultSet = connectionPool.getConnection().executeQuery(sql);
        while (resultSet.next()) {
            String workflow = resultSet.getString("workflow");
            workflows.add(workflow);
        }
           return workflows;
    }
    

    /**
     * Ϊ�������������ĵ�
     * @param partNumber
     * @param workflow ����ID
     * @param factory ����
     * @return
     * @throws Exception
     */
    public static String createDocForPart(String partNumber, String workflow,String factory) throws Exception{
       Debug.P("--createDocForPart-->>>Ϊ�������������ĵ� partNumber=[" + partNumber + "] workflow=["+ workflow + "] factory=[" + factory + "]");
       ResultSet resultSet = null;
       String resultInfo = "";
       try {
    	   SessionServerHelper.manager.setAccessEnforced(false);
    	   if (partNumber != null) {
    		   ConnectionPool connectionPool=DBFactory.getConnectionPool();//���ӳ�
    		   String sql = "select WLNUMBER,DOCNAME,DOCTYPE,LOCATION,DOCNUMBER from CSR_FJJL where WLNUMBER = '"+ partNumber + "'";
    		   Debug.P("--->SQL:"+sql);
    		   resultSet = connectionPool.getConnection().executeQuery(sql);
    		   while (resultSet.next()) {
    			  String spm_num = resultSet.getString(SPMConsts.MATERIAL_NUM);
    			  if(StringUtils.isEmpty(spm_num)){
    				  return "���ϱ��Ϊ��";
    			  }
    		      WTPart part=null;
			      try {
			    	  part= (WTPart) GenericUtil.getObjectByNumber(spm_num);
				     } catch (Exception e) {
					    Debug.P("---->>>NumberΪ("+spm_num+")������Windchill�в�����!");
					    continue;
				   }
    			  Debug.P("---->>>SPM_NUM="+spm_num);
    			  //�������Լ���
    			  HashMap<String, String> baseMap = new HashMap<String, String>();
    	
    			  if(spm_num.equals(partNumber)){
    				  String docName=resultSet.getString(SPMConsts.SPM_DOCNAME);
    				  String docNumber=resultSet.getString(SPMConsts.SPM_DOCNUMBER);
    			      String location_path=resultSet.getString(SPMConsts.SPM_LOCATION);
    			
    			      if(part!=null){
    			    	   Debug.P("----->>>DOC_WTPart:"+part.getPersistInfo().getObjectIdentifier().getStringValue()+"   SPMDocNum:"+docNumber);
    			    	   //�ĵ�����������ͬĿ¼��
    			    	   String folderPath= part.getFolderPath();
    			    	   folderPath=folderPath.substring(0,folderPath.lastIndexOf("/"));
    			    	   String containerName=part.getContainerName();
    			    	   Debug.P("----->>Part("+partNumber+")���ļ���·��:"+folderPath+"  ;containerName:"+containerName);
    			    	   baseMap.put(SPMConsts.KEY_NUMBER, docNumber==null?"":docNumber.trim());
    			    	   baseMap.put(SPMConsts.KEY_FOLDER, folderPath==null?"":folderPath.trim()); 
    			    	   baseMap.put(SPMConsts.KEY_NAME, docName==null?"":docName.trim());//�ĵ�����
    	    			   baseMap.put(SPMConsts.KEY_LOCATION_PATH, location_path==null?"":location_path.trim());//���ĵ�·��
    	    			   baseMap.put(SPMConsts.KEY_CONTAINERNAME, containerName==null?"":containerName.trim());
    	    			   
    	    			   //�����ĵ�������Ϣ
    	    			   WTDocument doc=CsrSpmUtil.createNewDocument(baseMap, null);
    	    			   if(doc!=null){//�����ĵ�������������ϵ
    	    				   PartUtil.createDescriptionLink(part, doc);
    	    			   }
    			      }
    			  }
    		   }//ѭ������
    		   resultInfo="�����ɹ�";
    	   }
	     } catch (Exception e) {
		     e.printStackTrace();
		     resultInfo="����ʧ��";
	     }finally{
	    	  if (resultSet != null) {
	             resultSet.close();
	           }
	          SessionServerHelper.manager.setAccessEnforced(true);
	      }
       
    	     return resultInfo;
    }

    


    /**
     * Ϊ�����޸Ĺ����ĵ�
     */
    public static String updateDocForPart(String partNumber, String workflow)throws Exception{
    	
    	Debug.P("---->>>updateDocForPart Number: " + partNumber + "workflow is " + workflow);
    	//�������Լ���
    	 HashMap<String, String> baseMap = new HashMap<String, String>();
    	 //IBA���Լ���
//    	 HashMap<String, String> ibaMap = new HashMap<String, String>();
    	 ResultSet resultSet = null;
         String  resultInfo = "";
         try {
        	 SessionServerHelper.manager.setAccessEnforced(false);
        	 if (partNumber != null) {
        		 ConnectionPool connectionPool=DBFactory.getConnectionPool();//���ӳ�
        		 String sql = "select WLNUMBER,DOCNAME,DOCTYPE,LOCATION,DOCNUMBER from CSR_FJJL where WLNUMBER = '" + partNumber + "'";
                 Debug.P("--->>SQL:"+sql);
                 resultSet = connectionPool.getConnection().executeQuery(sql);
                 while (resultSet.next()) {
                	 String spm_prodnum=resultSet.getString(SPMConsts.MATERIAL_NUM);
                	 String doc_number=resultSet.getString(SPMConsts.SPM_DOCNUMBER);
                	 if(StringUtils.isEmpty(spm_prodnum)){
                		 return "���ϱ���Ϊ��";
                	 }
                	
                	 Debug.P("-----SPM>>>WLNUMBER("+partNumber+")��Ӧ�������ֶ�DOCNUMBERΪ["+doc_number+"]");
                	 baseMap.put(SPMConsts.KEY_NUMBER, doc_number==null?"":doc_number.trim());
                	 if(partNumber.equals(spm_prodnum)){
                		 String docName=resultSet.getString(SPMConsts.SPM_DOCNAME);
       			         String location_path=resultSet.getString(SPMConsts.SPM_LOCATION);
       			         WTPart part=null;
    			         try {
    			    	    part= (WTPart) GenericUtil.getObjectByNumber(spm_prodnum);
					      } catch (Exception e) {
						     Debug.P("---->PART_ERROR:Num=("+spm_prodnum+")������Windchill�в�����!");
						     continue;
					     }
    			         
    				      if(part!=null){
       			    	   Debug.P("----->>>DOC_WTPart:"+part.getPersistInfo().getObjectIdentifier().getStringValue());
       			    	   //�ĵ�����������ͬĿ¼��
       			    	   String folderPath=FolderHelper.service.getFolderPath(part);
       			    	   String containerName=part.getContainerName();
       			    	   Debug.P("----->>Part("+partNumber+")���ļ���·��:"+folderPath+"  ;containerName:"+containerName);
       			    	   baseMap.put(SPMConsts.KEY_FOLDER, folderPath==null?"":folderPath.trim()); 
       			    	   baseMap.put(SPMConsts.KEY_NAME, docName==null?"":docName.trim());//�ĵ�����
       	    			   baseMap.put(SPMConsts.KEY_LOCATION_PATH, location_path==null?"":location_path.trim());//���ĵ�·��
       	    			   baseMap.put(SPMConsts.KEY_CONTAINERNAME, containerName==null?"":containerName.trim());
       	    		       
       	    			     
       	    			      //�����ĵ�����
       	    			      if(StringUtils.isNotEmpty(doc_number)){
       	    			       WTDocument doc=(WTDocument) GenericUtil.getObjectByNumber(doc_number);
         			             if(doc!=null){//�ĵ�����Ϊ��
         			            	 CsrSpmUtil.reviseDocument(doc, baseMap, null);
         			             }
       	    			    	resultInfo="���²����������ĵ��ɹ�";
       	    			      }else{
       	    			    	resultInfo="����δ����";
       	    			      }
    				      }
                	 }
                 }
           }
		} catch(SQLException e){
			e.printStackTrace();
		}catch (Exception e) {
			 e.printStackTrace();
			 throw new Exception("Windchill����("+partNumber+")�����������ĵ��쳣");
		}finally{
			  if (resultSet != null) {
		           resultSet.close();
		        }
		     SessionServerHelper.manager.setAccessEnforced(false);
		}
    	  return   resultInfo;
    }
    
    
    /**
     * SPM���ýӿ��ж�������PLMϵͳ�Ƿ���ڼ��������
     * @return 0. ��ʾ�����ڼ�������� 1.��ʾ���ڼ��������
     * @throws Exception 
     */
    public static String getJSGGSByPartNumber(String partNumber) throws Exception {
    	 Debug.P("---getJSGGSByPartNumber--->>partNumber��"+partNumber);
    	 String result = "0";//Ĭ�ϲ�����
         if (StringUtils.isEmpty(partNumber)){
        	 return "���ϱ�Ų���Ϊ��";
         }
         try {
        	 SessionHelper.manager.setAdministrator();
             WTPart part =(WTPart) GenericUtil.getObjectByNumber(partNumber);
             if(part!=null){
            	  QueryResult qr = WTPartHelper.service .getDescribedByWTDocuments(part);
            	  while(qr.hasMoreElements()){//�ж�Windchill�Ƿ���ڼ�������ĵ�
            		  WTDocument doc=(WTDocument) qr.nextElement();
            		  String type=GenericUtil.getTypeName(doc);
            		  Debug.P("--->>DocType:"+type);
            		  if(StringUtils.equals(type, SPMConsts.TECHNICAL_SPEC)){//���������
            			  result="1";
            		  }
            	  }
             }
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Windchill��ѯ("+partNumber+")�����ļ���������쳣");
		}finally{
			 SessionServerHelper.manager.setAccessEnforced(true);
		}
    	  return  result;
    }

    /**
     * SPM���ýӿ��ж�������PLMϵͳ�Ƿ���ڲ�Ʒ�ֲ�
     * @return 0. ��ʾ�����ڲ�Ʒ�ֲ� 1.��ʾ���ڲ�Ʒ�ֲ�
     */
    public static String getCPSCByPartNumber(String partNumber) throws Exception {
    	Debug.P("--getCPSCByPartNumber-->>>partNumber��"+partNumber);
    	 String result = "0";//Ĭ�ϲ�����
         if (StringUtils.isEmpty(partNumber)){
        	 return "���ϱ�Ų���Ϊ��";
         }
         try {
        	 SessionServerHelper.manager.setAccessEnforced(false);
        	   WTPart part =(WTPart) GenericUtil.getObjectByNumber(partNumber);
        	   QueryResult qr = WTPartHelper.service .getDescribedByWTDocuments(part);
         	  while(qr.hasMoreElements()){//�ж�Windchill�Ƿ���ڼ�������ĵ�
        		  WTDocument doc=(WTDocument) qr.nextElement();
        		  String type=GenericUtil.getTypeName(doc);
        		  Debug.P("--->>DocType:"+type);
        		  if(StringUtils.equals(type, SPMConsts.PRODUCT_SPEC)){//��Ʒ�ֲ�
        			  result="1";
        		  }
        	  }
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Windchill��ѯ("+partNumber+")�����ļ���������쳣");
		}finally{
			 SessionServerHelper.manager.setAccessEnforced(true);
		}
    	    return result;
    }
    
	
    /**
     * ��������Ƿ��ڴ洢���д���
     * 0. ��ʾ�����ڴ洢�� 1.��ʾ���ڴ洢��
     */
    public static String checkPartFromLibrary(String partNumber)throws Exception {
     	Debug.P("--checkPartFromLibrary-->>>partNumber��"+partNumber);
    	 String result = "0";//Ĭ�ϲ�����
         if (StringUtils.isEmpty(partNumber)){
        	 return "���ϱ�Ų���Ϊ��";
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
			throw new Exception("Windchill��ѯ("+partNumber+")�Ƿ���ڴ洢�����쳣");
		}finally{
			 SessionServerHelper.manager.setAccessEnforced(true);
		}
    	  return result;
    }

    
    
    
    /**
     * �ж϶���Ĵ�����
  * @throws WTException 
     */
    private static Persistable hasExistObject(String num) throws Exception{
 	    if(StringUtils.isEmpty(num)) throw new Exception("NUMBER�����ֶβ���ֵΪ��");
 	    try {
 	        Persistable object=GenericUtil.getObjectByNumber(num);
 	         return object;
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Windchill��ѯ������("+num+")�쳣!");
			}
    }
       
       
       /**
        * �����м�����Ϣ��ѯ�����ļ�����֮��Ӧ��Windchill��������
        * @param field
        * @return
        * @throws Exception
        */
       private static String MAPPINGWCCONFIG_PATH="codebase" + File.separator + "ext"
   			+ File.separator + "tmt" + File.separator + "integration"
   			+ File.separator + "webservice" + File.separator +"spm"+ File.separator+ "SPMMapping.properties";
       private static Map<String,String> getWCMappingFieldBySPM()throws Exception{
    		Properties prop=new Properties();
    		Map<String,String> mappingMap=new HashMap<String,String>();
    	    String wthome = (String) (WTProperties.getLocalProperties()).getProperty("wt.home", "");
			String tempPath = wthome + File.separator + MAPPINGWCCONFIG_PATH;
			Debug.P("-------->>Mapping FilePath:"+tempPath);
			FileInputStream fis = new FileInputStream(tempPath);
			prop.load(new InputStreamReader(fis, "UTF-8"));
			prop.load(fis);
			if(prop!=null){
			   Iterator<?> ite=prop.entrySet().iterator();
				while(ite.hasNext()){
					Entry entry=(Entry) ite.next();
					String proName = (String) entry.getKey();
					String value = (String) entry.getValue();
					mappingMap.put(proName, value);
				}
			}
			  return mappingMap;
       }

        /**
         * ���ַ�����ָ���ķָ����и��ŵ�������
         * @param target Ŀ���ַ���
         * @param �и����
         */
       private static Set<String> splitStr2Set(String target,String regex){
    	     Set<String>  result=new HashSet<String>();
    	     if(StringUtils.isEmpty(target)) return result;
    	     String[] strs=target.split(regex);
    	     for(int i=0;i<strs.length;i++){
    	    	 if(StringUtils.isEmpty(strs[i])) continue;
    	    	  result.add(strs[i]);
    	     }
    	      return result;   
       } 
       
       
       private static String setCollection2Str(Collection<?> collection){
    	   StringBuffer bf=new StringBuffer();
    	   String result=null;
    	   if(collection!=null){
    		   for(Iterator<?> ite=collection.iterator();ite.hasNext();){
    			    String value=(String) ite.next();
    			    if(StringUtils.isEmpty(value)) continue;
    			    bf.append(value).append(",");
    		   }
    		   if(bf.toString().contains(",")){
    			   result= bf.substring(0, bf.lastIndexOf(",")) ;
    		   }
    	   }
    	        return result;
       }
       
       

       
}
