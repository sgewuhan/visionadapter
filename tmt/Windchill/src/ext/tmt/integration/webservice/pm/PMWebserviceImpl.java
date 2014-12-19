package ext.tmt.integration.webservice.pm;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId; 

import com.mongodb.WriteResult;
import com.ptc.core.components.visualization.VisualizationDataUtility;
import com.ptc.wvs.common.ui.VisualizationHelper;
import com.ptc.wvs.server.ui.ThumbnailHelper;
import com.ptc.wvs.server.util.WVSContentHelper;
import com.sg.visionadapter.BasicDocument;
import com.sg.visionadapter.DocumentPersistence;
import com.sg.visionadapter.FolderPersistence;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMDocument;
import com.sg.visionadapter.PMFolder;

import ext.tmt.folder.api.FolderService;
import ext.tmt.folder.impl.FolderServiceImpl;


import ext.tmt.part.PartUtils;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import wt.content.ApplicationData;
import wt.content.ContentHolder;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.iba.value.IBAHolder;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleManaged;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.WorkInProgressHelper;
import wt.wvs.VisualizationHelperFactory;



/**
 * Webservice����ʵ����
 * @author public759
 *
 */
public class PMWebserviceImpl implements Serializable,RemoteAccess{
	

	
	private static final long serialVersionUID = -9012564223029784741L;


	/*�ļ��з���ӿ�*/
	 private  static FolderService folderService=new FolderServiceImpl();
	 
	 
	 private static String DEFAULT="/Default";
	 
	 
	 private static String VMUSER="PM-RW";
	 
	 
	 private static Map<String,String> stateMap=new HashMap<String,String>();
	 static{
		 stateMap.put(ConstanUtil.INWORK,ConstanUtil.WC_INWORK);
		 stateMap.put(ConstanUtil.APPROVE,ConstanUtil.WC_APPROVE);
		 stateMap.put(ConstanUtil.RELEASED,ConstanUtil.WC_RELEASED);
		 stateMap.put(ConstanUtil.DESPOSED,ConstanUtil.WC_DESPOSED);
	 }

	 private static String codebasePath=null;
	 static {
			try {
				WTProperties wtproperties= WTProperties.getLocalProperties();
				codebasePath= wtproperties.getProperty("wt.codebase.location");
				codebasePath=codebasePath+File.separator+"visionconf";
				Debug.P("----------->>>Codebase:"+codebasePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	 
	 
	 /**
	  * �����ļ��нṹ
	  * @param result
	  * @throws Exception
	  */
	 public static int  createFolderEntryList(String[] result) throws Exception{
		 int count=0;
		 if(result!=null){
			  Debug.P("----->>>>Length:"+result.length);
			 for (int i=0;i<result.length;i++) {
				 String str=result[i];
				 createFolderEntry(str);
				 count++;
			}
		 }
		    return count;
	 }

	 

	 /**
	  * �����ļ���
	  * @param pm_id
	 * @throws Exception 
	  */
	 private  static  void createFolderEntry(String objectId) throws Exception{
		    Folder folderResult=null;
		    //���ȵõ�PM Folder����
		    if(objectId==null) {throw new IllegalArgumentException("----Args PMID is Null");}
		    ModelServiceFactory factory= ModelServiceFactory.getInstance(codebasePath);
			FolderPersistence folderPersistence = factory.get(FolderPersistence.class);
			PMFolder pmfolder=folderPersistence.get(new ObjectId(objectId));//PM�ļ��ж���
			checkNull(pmfolder);
			boolean iscreate=pmfolder.getPLMId()==null?true:false;
			PMFolder parentFolder=pmfolder.getParentFolder();//���ļ���
			checkNull(parentFolder);
			String containerName=pmfolder.getContainerName();//ContainerName
			boolean  isContainer=parentFolder.isContainer();//�Ƿ�Ϊ����
			String parent_wcId=parentFolder.getPLMId();//��ø������Id
			String folderName=pmfolder.getCommonName().trim();
			Debug.P("------>>>Folder:"+folderName+"  ContainerName:"+containerName+"  isContainer="+isContainer+"  ParentFolderID="+parent_wcId);
			try{
		    	SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
		    	WTContainer container=checkWTContainerExist(containerName);
		    	if(iscreate){//�Ƿ�ͬ����ֹ�ظ�����
			    	 //����������������������´����ļ���
			    	 if(isContainer){
			    		  Debug.P("-----Container----->>>Ready Create FolderPath: "+(DEFAULT+"/"+folderName));
			    		  String folderPath=DEFAULT+"/"+folderName;
			    		  folderResult=FolderUtil.getFolderRef(folderPath,container,true);
			    	 }else{
			    		 //�����ø�����ļ��ж���
			    		 Persistable persistable=GenericUtil.getPersistableByOid(parent_wcId);
			    		 if(persistable!=null&&persistable instanceof Folder){
			 	             Folder parent_Folder=(Folder)persistable;
			 	             folderResult=FolderUtil.createSubFolder(folderName, null, parent_Folder, null);
			 	            }
			    	      }
			    	    if(folderResult!=null){
			                  //��дWindchill Folder Oid��PMϵͳ
			                  String wc_oid=folderResult.getPersistInfo().getObjectIdentifier().getStringValue();//OID
			                  Debug.P("------Windchill Folder_OID:"+wc_oid);
			                  pmfolder.setPLMId(wc_oid);
			                  pmfolder.setPLMData(getObjectInfo(folderResult));
			                  pmfolder.doUpdate();//�޸�
			                  Debug.P("----->>>����ͬ��Windchill�ļ���:("+folderName+")�ɹ�!");
			    	    }
			    	 }
		    }catch(Exception e){
		    	e.printStackTrace();
		    	throw new Exception("Windchill�����ļ���("+folderName+")ʧ��!");
		    }
	 }
	 
	 /**
	  * �޸��ļ�������
	  * @param objectId 
	  * @param newFolderName ���ļ�������
	  * @return
	  */
	 public static int modifyFolderEntry(String objectId,String newFolderName)throws Exception{
		  int count =0;
		  Debug.P("------->>>Modify Folder ObjectId:"+objectId);
		  checkNull(objectId);
		   if (!RemoteMethodServer.ServerFlag) {
	           String method = "modifyFolderEntry";
	           String klass = PMWebserviceImpl.class.getName();
	           Class[] types = { String.class,String.class};
	           Object[] vals = {objectId,newFolderName};
	           return (Integer) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
	       }else{
	  		 //��ѯPM�ļ��ж���
	  		 FolderPersistence folderPersistence =  ModelServiceFactory.getInstance(codebasePath).get(FolderPersistence.class);
	       	 PMFolder folder=folderPersistence.get(new ObjectId(objectId));//PM�ļ��ж���
	       	 String folderName=folder.getCommonName();
	       	 checkNull(folder);
	       	 //���Windchill �ļ��ж���
	       	 String foid=folder.getPLMId();
	       	 String containerName=folder.getContainerName();
	      	 Debug.P("------->>Modify PM Folder:"+folder.getCommonName()+" ;WC_PLMID="+foid+"   ;ContainerName="+containerName);
	      	 try{
//	      		 SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	      		 SessionHelper.manager.setAdministrator();
		       	 if(!StringUtils.isEmpty(foid)){
		       		Persistable persistable=GenericUtil.getPersistableByOid(foid);
		       		if(persistable!=null){
		       			if(persistable instanceof Folder){
		       				Folder folderObj=(Folder)persistable;
		       				String folderPath=folderObj.getFolderPath();
		       				String fName=folderObj.getName();
		       				Debug.P("----->>>OldFolderName:"+fName+"  NewFolderName="+folderName);
		       				if(!StringUtils.equals(fName, folderName)){//��һ�����޸�
		       					count=folderService.editFolder(folderPath, folderName, containerName);
			       				if(count>0){
			       					folder.doUpdate();
			       					Debug.P("------>>PM ���� OldFolderName("+fName+") ��NewFolderName("+folderName+")Success!");
			       				}
		       				}
		       			}
		       		}
		       	 }
	      	 }catch(Exception e){
	      		 e.printStackTrace();
	      		 throw new Exception("Windchill�޸��ļ���("+foid+")��Ϣʧ��!");
	      	 }finally{
	      		SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	      	 }
	       }
     	       return count;
	 }
	 
	 /**
	  * ɾ���ļ��а����ļ����µĶ���
	 * @throws IllegalAccessException 
	 * @throws Exception 
	  * 
	  */
	 
	 public static int deleteFolderEntry(String objectId) throws Exception{
		   int count=0;
		   Debug.P("-------->>>Delete Folder:"+objectId);
		   if (!RemoteMethodServer.ServerFlag) {
	            String method = "deleteFolderEntry";
	            String klass = PMWebserviceImpl.class.getName();
	            Class[] types = { String.class,};
	            Object[] vals = {objectId};
	            return (Integer) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
	        }else{
	             if(!StringUtils.isEmpty(objectId)){
	            	 FolderPersistence folderPersistence = ModelServiceFactory.getInstance(codebasePath).get(FolderPersistence.class);
		        	 PMFolder folder=folderPersistence.get(new ObjectId(objectId));//PM�ļ��ж���
		        	 checkNull(folder);
		        	 SessionHelper.manager.setAdministrator();
			         String containerName=folder.getContainerName();
			        //���Windchill��PLMID
			        String foid=folder.getPLMId();
		        	try{
			        	 if(StringUtils.isNotEmpty(foid)){
			        		Debug.P("------Ready Delete FolderName:"+folder.getCommonName()+"  Windchill FolderId:"+foid);
			          		Persistable persistable=GenericUtil.getPersistableByOid(foid);
			          		if(persistable!=null){
			          			if(persistable instanceof Folder){//�Ƿ�Ϊ�ļ�������
			          				Folder folderObj=(Folder)persistable;
			          				String folderPath=folderObj.getFolderPath();
			          				count=folderService.deleteFolder(folderPath, containerName);
			          				if(count>0){//���Windchillɾ���ɹ���ɾ��PMϵͳ����
			          					folder.doRemove();
			          					Debug.P("----Remove PM Folder:"+folder.getCommonName()+" Success!");
			          				}
			          				
			          			}
			          		}
			          	 }
	        	}catch(Exception e){
	        		 e.printStackTrace();
	        		 throw new Exception("Windchillɾ���ļ���("+foid+"ʧ��!");
	        	}finally{
	        		SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	        	}
	       }
	    }
     	            return count;
	 }
	 
	 
	

	private static void checkNull(Object object) {
		if(object==null){
			Debug.P("---->>Object:"+object);
			throw new IllegalArgumentException("object  is Null");
		}
	
	}
	
	/**
	 * ��������Ƿ����
	 * @param containerName
	 * @throws Exception
	 */
	private static WTContainer checkWTContainerExist(String containerName)throws Exception{
		WTContainer container=null;
	try{
		 container=GenericUtil.getWTContainerByName(containerName);
		if(container==null){
			throw new Exception("Windchill�в�����PM�е���������,����ϵ����Ա����!");
		}
	} catch (Exception e) {
		throw new Exception("Windchill��ѯ("+containerName+")�쳣!");
	  }
        return container;
} 
	
	/**
	 * ����WTDocument�ĵ�
	 * @param pm_docId
	 */
    public static int  createWTDocumentEntry(String pm_docId) throws Exception{
    	   int count=0;
    	   Debug.P("------>>>Create Windchill Doc:PMID("+pm_docId+")");
		 if (!RemoteMethodServer.ServerFlag) {
	            String method = "createWTDocumentEntry";
	            String klass = PMWebserviceImpl.class.getName();
	            Class[] types = { String.class,};
	            Object[] vals = {pm_docId};
	            return (Integer) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
	     }else{
	        	//���PM�ĵ�����
	    		SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	        	DocumentPersistence docPersistance=ModelServiceFactory.getInstance(codebasePath).get(DocumentPersistence.class);
	        	PMDocument pm_document=docPersistance.get(new ObjectId(pm_docId));
	        	PMFolder pmfolder=pm_document.getFolder();//����ĵ����ڵ�PM�ļ���
	        	String wc_foid=pmfolder.getPLMId();//Windchill �ļ��� Oid 
	        	boolean isContainer=pmfolder.isContainer();
	        	boolean iscreate=pm_document.getPLMId()==null?true:false;//�Ƿ���ͬ����Windchill
	        	WTDocument doc=checkWTDocumentWrite2PM(pm_docId);//��ͨ�Լ��
		    	if(doc!=null){
		    		String plmId=doc.getPersistInfo().getObjectIdentifier().getStringValue();
		    		pm_document.setPLMData(getObjectInfo(doc));
		    		pm_document.setPLMId(plmId);
		    		pm_document.setMajorVid(doc.getVersionIdentifier().getValue());
            		pm_document.setSecondVid(Integer.valueOf(doc.getIterationIdentifier().getValue()));
		    		pm_document.doUpdate();//�޸�
            		Debug.P("----->>>PM WCID:"+plmId+"  ;PM_Document:"+pm_docId);
		    		return 1;
		    	}
	        	String containerName=pmfolder.getContainerName();
	        	Debug.P("----->>>>WC   Folder ID:"+wc_foid+"  �Ƿ�ΪPM�������ļ���:"+isContainer +"  ;ContaienrName:"+containerName);
	        	try{
	        		Persistable persistable=null;
	        		  WTContainer container=null;
	        		  Debug.P("------>>>PM DOC_ID��"+pm_docId+"�Ƿ��½���Windchill="+iscreate);
	        		  if(iscreate){//�ж��Ƿ���ͬ����Windchill
	        			  //�ļ��ж���
	            		  if(!StringUtils.isEmpty(wc_foid)){
		        				persistable=GenericUtil.getPersistableByOid(wc_foid);
		        		  }else{//������������
		        			   container=GenericUtil.getWTContainerByName(containerName);
		        			   persistable=GenericUtil.createNewPath(container);
		        		  }
		        		if(persistable instanceof Folder){//�ļ���
		        			Folder folder=(Folder)persistable;
		                   //�ж��ĵ��Ƿ��Ѵ���
		       			   boolean isEmpty=StringUtils.isEmpty(pm_document.getPLMId());
		        			if(isEmpty){//�½�
		            			Map ibas=new HashMap();//�����Լ���
		            			setDocIBAValuesMap(ibas, pm_document);
		            			WTDocument document= DocUtils.createDocument(pm_document, null,VMUSER,ibas,folder);
		            			//��дWindchill��Ϣ��PM
				        		if(document!=null){
				        			if(isContainer){//����
			        					GenericUtil.moveObject2Container(document, container,folder);
			        				}else{//�ļ���
			        					FolderUtil.changeFolder(document,folder);
			        				}
				            		String wcId=document.getPersistInfo().getObjectIdentifier().getStringValue();
				            		pm_document.setPLMData(getObjectInfo(document));
				            		pm_document.setPLMId(wcId);
				            		pm_document.setMajorVid(document.getVersionIdentifier().getValue());
				            		pm_document.setSecondVid(Integer.valueOf(document.getIterationIdentifier().getValue()));
				            		WriteResult result=pm_document.doUpdate();//�޸�
				            		
				            		Debug.P("----->>>PM Return:("+result.getN()+")Create WCID:"+wcId+"  ;PM_Document:"+pm_docId);
				            		count=1;
				        		  }
		            		   }
		        			}
	        		  }
	        	}catch(Exception e){
	        		 e.printStackTrace();
	        		throw new Exception("Windchill ����("+pm_document.getCommonName()+")�ĵ�ʧ��!");
	        	}finally{
	        		 SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	        	}
	     }
         	     return count;
    }
    

    /**
     * �����ĵ���Ϣ
     * @param pm_id
     * @param newDocName
     * @return
     * @throws Exception
     */
    public static int updateWTDocumentEntry(String pm_id)throws Exception{
    	Debug.P("------>>>Update Windchill Doc:PMID("+pm_id+")");
    	int count=0;
    	checkNull(pm_id);
		if (!RemoteMethodServer.ServerFlag) {
	            String method = "updateWTDocumentEntry";
	            String klass = PMWebserviceImpl.class.getName();
	            Class[] types = { String.class,};
	            Object[] vals = {pm_id};
	            return (Integer) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
	     }else{
	    	DocumentPersistence docPersistance=ModelServiceFactory.getInstance(codebasePath).get(DocumentPersistence.class);
	     	PMDocument pm_document=docPersistance.get(new ObjectId(pm_id));
	     	checkNull(pm_document);
	     	String pm_docName=pm_document.getCommonName();
	     	//�ж��Ƿ��Ѿ�ͬ����Windchill
     		boolean isCreated=pm_document.getPLMId()==null?false:true;
     		String doc_id=pm_document.getPLMId();
     		Debug.P("------>>>>Windchill���Ƿ��Ѿ�����("+pm_docName+"):"+isCreated+"  Doc_Windchill:"+doc_id);
	     		try{
	     			SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
//	     			SessionHelper.manager.setAdministrator();
	     			if(isCreated){
	         			if(StringUtils.isEmpty(doc_id)) return 0;
	         			 Persistable object=GenericUtil.getPersistableByOid(doc_id);
	         			if(object!=null&&object instanceof WTDocument){
	         				WTDocument doc=(WTDocument)getLastObjectByNum(object);
	         				Map ibas=LWCUtil.getAllAttribute(doc);
	         			    setDocIBAValuesMap(ibas, pm_document);//����������
	         				doc=(WTDocument) GenericUtil.checkout(doc);
	         				doc=DocUtils.updateWTDocument(doc,pm_document, ibas);//�����ĵ�
	         				if (doc != null) {
	         					if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(doc, wt.session.SessionHelper.manager.getPrincipal()))
	         						doc = (WTDocument) WorkInProgressHelper.service.checkin(doc, "update document Info");
	         				   }
	         			
	             			//������ص�doUpdate()
	         				pm_document.setPLMData(getObjectInfo(doc));
	         				pm_document.setPLMId(doc.getPersistInfo().getObjectIdentifier().getStringValue());
	         				pm_document.setMajorVid(doc.getVersionIdentifier().getValue());
	         				pm_document.setSecondVid(Integer.valueOf(doc.getIterationIdentifier().getValue()));
	         				pm_document.doUpdate();
	         			    Debug.P("------>>>Update PM_DocumentName��"+pm_docName+" Success!");
	         			}
	         		}
	     		}catch(Exception e){
	     			e.printStackTrace();
	     			throw new Exception("Windchill����("+doc_id+")�ĵ�����ʧ��!");
	     		}finally{
	     			SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	     		}
    	}
    	  return count;
    }


    
	
	
    /**
     * ɾ���ĵ�����
     * @param pm_docId
     * @return
     * @throws Exception
     */
    public static int deleteWTDocumentEntry(String pm_docId)throws Exception{
    	
       Debug.P("------>>>Delete PM_DocumentID��"+pm_docId);
       
  	   if (!RemoteMethodServer.ServerFlag) {
           String method = "deleteWTDocumentEntry";
           String klass = PMWebserviceImpl.class.getName();
           Class[] types = { String.class,};
           Object[] vals = {pm_docId};
           return (Integer) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
       }else{
    	checkNull(pm_docId);
       	DocumentPersistence docPersistance=ModelServiceFactory.getInstance(codebasePath).get(DocumentPersistence.class);
       	PMDocument pm_document=docPersistance.get(new ObjectId(pm_docId));
       	checkNull(pm_document);
       	 //���PM �ĵ���Ӧ��Windchill�ĵ�ID
       	String wc_oid=pm_document.getPLMId();
       	Debug.P("------>>>PM("+pm_docId+")<--->Windchill("+wc_oid+")");
       	try {
//       		SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
       		SessionHelper.manager.setAdministrator();
       		if(!StringUtils.isEmpty(wc_oid)){
       			Persistable object=GenericUtil.getPersistableByOid(wc_oid);
       			if(object!=null){
       				WTDocument doc=(WTDocument)getLastObjectByNum(object);
            		 if(doc!=null){
               			 GenericUtil.deleteDoc(doc, null);
               			 return 1;
               		 }
       			 }
           	  }
   	    	} catch(Exception e){
   	    		e.printStackTrace();
   			   throw new Exception("Windchillɾ���ĵ�����("+wc_oid+")ʧ��!");
   		   }finally{
   			   SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
   		   }
       }
    	  return 0;
    }
    
    
    /**
     * ͬ���ƶ�PM�ĵ���·����Windchillϵͳ�и���
     * @param pm_docId
     * @return
     * @throws Exception
     */
    public static int moveWTDocumentEntry(String pm_docId)throws Exception{

   	 Debug.P("------>>>Move Path PM_DocumentID ��"+pm_docId);
	 checkNull(pm_docId);
	 if (!RemoteMethodServer.ServerFlag) {
           String method = "moveWTDocumentEntry";
           String klass = PMWebserviceImpl.class.getName();
           Class[] types = { String.class,};
           Object[] vals = {pm_docId};
           return (Integer) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
       }else{
    	     BasicDocument basic_object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_docId);
    	 	 checkNull(basic_object);
    	 	//����PM�ļ����ҵ���֮��Ӧ��Windchill�ļ���Oid
    	     PMFolder folder=basic_object.getFolder();
    	     checkNull(folder);
    	 	 //�ж��ļ����Ƿ�Ϊ����Root
    	 	 boolean isContainer=folder.isContainer();
    	 	 String containerName=folder.getContainerName();
    	 	 checkNull(folder);
    	 	 String wc_foid=folder.getPLMId();
    	 	   try {
    	 		 String doc_wcId=basic_object.getPLMId();
    	 		 Debug.P("---->>Windchill Folder ID:"+wc_foid+"  Windchill  ID:"+doc_wcId);
    			   if(!StringUtils.isEmpty(wc_foid)){
    				   SessionHelper.manager.setAdministrator();
    				   Persistable object=GenericUtil.getPersistableByOid(doc_wcId);
    				   Debug.P("---->>>ObjectType:"+object.getType());
	         			if(object!=null){
	         				object=getLastObjectByNum(object);
	    			    	Folder folderObj=null;
		         			if(isContainer){//����
		         				    Debug.P("----->>>IsContainer:"+isContainer);
		         					WTContainer container=GenericUtil.getWTContainerByName(containerName);
		         					folderObj=GenericUtil.createNewPath(container);
		         					GenericUtil.moveObject2Container(object, container, folderObj);//�ƶ�������Ŀ¼��
		         					 Debug.P("----->>>moveObject2Container:"+object);
		         				}else{//�ļ���
		         					if(StringUtils.isEmpty(wc_foid)){throw new Exception("�ĵ�("+basic_object.getCommonName()+")Ŀ���ļ���("+folder.getCommonName()+")��Windchillϵͳ������ ,�޷�ִ���ƶ�����!");}
		         					folderObj=(Folder) GenericUtil.getPersistableByOid(wc_foid);
		         					if(folderObj!=null){
		         						Debug.P("----->>>>Start Move Folder"+folderObj.getName());
		         						object=FolderHelper.service.changeFolder((FolderEntry) object, folderObj);
		         						PersistenceHelper.manager.refresh(object);
			         					Debug.P("----->>>>End Move("+object.getPersistInfo().getObjectIdentifier().getStringValue()+") Folder("+folderObj.getFolderPath()+") Success!!");
		         					}
		         				}
		         			       return 1;
	         			}
    			    }
    		    }catch(Exception e){
    		    	e.printStackTrace();
    		    	throw new Exception("Windchill�ƶ�����("+wc_foid+")��ʧ��!");
    		    }finally{
    		    	SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
    		    }
       }
	 
 	              return 0;
  }
    
    
    /**
     * ��ȡ�������°汾
     * @param object
     * @throws Exception
     */
    private static Persistable getLastObjectByNum(Persistable object)throws Exception{
    	if(object instanceof WTPart){
    		WTPart part=(WTPart)object;
    		object=PartUtils.getPartByNumber(part.getNumber());
    		Debug.P("---->>>getLastObjectByNum:WTPartNumber="+part.getNumber());
    	}else if(object instanceof EPMDocument){
    		EPMDocument epm=(EPMDocument)object;
    		object=EPMUtil.getEPMDocument(epm.getNumber(),null);
    		Debug.P("---->>>getLastObjectByNum:EPMDocument="+epm.getNumber());
    	}else if(object instanceof WTDocument){
    		WTDocument doc=(WTDocument)object;
    		object=DocUtils.getDocByNumber(doc.getNumber());
    		Debug.P("---->>>getLastObjectByNum:WTDocument="+doc.getNumber());
    	}
    	  return object;
    }
    
	/**
	 * ӳ���ĵ��������������
	 * @param ibas
	 * @param object
	 * @throws WTException
	 */
	private  static void setDocIBAValuesMap(Map ibas,PMDocument object )throws WTException{
		//���ö������
		if(!StringUtils.isEmpty(object.getPhase())){
			 ibas.put(ConstanUtil.PHASE, object.getPhase());//�׶�
		}
		if(!StringUtils.isEmpty(object.getProductNumber())){
			ibas.put(ConstanUtil.PROJECTNO,object.getProductNumber());//��Ŀ���
		}
		if(!StringUtils.isEmpty(object.getProjectWorkOrder())){
			ibas.put(ConstanUtil.WORKORDER, object.getProjectWorkOrder());//�������
		}
		if(!StringUtils.isEmpty(object.getProductNumber())){
			  ibas.put(ConstanUtil.PRODUCTNO, object.getProductNumber());//�����ĳ�Ʒ��
		}
		if(!StringUtils.isEmpty(object.getCreateByUserId())){
			  ibas.put(Contants.PMCREATOR, object.getCreateByUserId());//������
		}
		if(!StringUtils.isEmpty(object.getModifiedUserId())){
			  ibas.put(Contants.PMMODIFYEDBY, object.getModifiedUserId());//�޸���
		}
		if(StringUtils.isNotEmpty(object.get_id().toString())){
			ibas.put(ConstanUtil.PMID, object.get_id().toString());
		}
	}
	
	

	/**
	 * ��ȡ�����������Ϣ
	 * @param object
	 * @throws WTException
	 */
	 private static Map<String,Object> getObjectInfo(Persistable object)throws Exception{
		  Map<String,Object> result=new HashMap<String,Object>();
		  if(object!=null){
			   if(object instanceof WTPart){
				   WTPart part=(WTPart)object;
				   result.put(ConstanUtil.NAME, part.getName());
				   result.put(ConstanUtil.TYPE, part.getType());
				   result.put(ConstanUtil.NUMBER, part.getNumber());
				   result.put(ConstanUtil.CREATOR, part.getCreator().getName());
				   result.put(ConstanUtil.MODIFIER, part.getModifier().getName());
				   //��ȡ����������Լ���
				   Map<String,Object> ibas=LWCUtil.getAllAttribute(part);
				   result.putAll(ibas);
			   }else if(object instanceof EPMDocument){
				   EPMDocument epm=(EPMDocument)object;
				   result.put(ConstanUtil.NAME, epm.getName());
				   result.put(ConstanUtil.TYPE, epm.getType());
				   result.put(ConstanUtil.NUMBER, epm.getNumber());
//				   result.put(ConstanUtil.STATE, epm.getLifeCycleState().getDisplay());
				   result.put(ConstanUtil.CREATOR, epm.getCreator().getName());
				   result.put(ConstanUtil.MODIFIER, epm.getModifier().getName());
				   result.put(ConstanUtil.CREATEDATE, epm.getCreateTimestamp().toString());
				   result.put(ConstanUtil.MODIFYDATE, epm.getModifyTimestamp().toString());
				   result.put(ConstanUtil.DOWNLOAD_URL, GenericUtil.getPrimaryContentUrl(epm));
				   //��ȡ����������Լ���
				   Map<String,Object> ibas=LWCUtil.getAllAttribute(epm);
				   result.putAll(ibas);
			   }else if(object instanceof WTDocument){
				   WTDocument doc=(WTDocument)object;
				   Debug.P("----------->>>>Doc:"+doc.getName());
				   result.put(ConstanUtil.NAME, doc.getName());
				   result.put(ConstanUtil.TYPE, doc.getType());
				   result.put(ConstanUtil.NUMBER, doc.getNumber());
//                   result.put(ConstanUtil.STATE, doc.getLifeCycleState().getDisplay());
				   result.put(ConstanUtil.CREATOR, doc.getCreator().getName());
				   result.put(ConstanUtil.MODIFIER, doc.getModifier().getName());
//			   result.put(ConstanUtil.CREATEDATE, doc.getCreateTimestamp().toString());
//			   result.put(ConstanUtil.MODIFYDATE, doc.getModifyTimestamp().toString());
				   result.put(ConstanUtil.DOWNLOAD_URL, GenericUtil.getPrimaryContentUrl(doc));
				   //��ȡ����������Լ���
				   Map<String,Object> ibas=LWCUtil.getAllAttribute(doc);
				   result.putAll(ibas);
			   }else if(object instanceof Folder){
				   Folder folder=(Folder)object;
				   result.put(ConstanUtil.NAME, folder.getName());
			   }
			     //�������ӵ�ַ
			     result.put(ConstanUtil.OBJECT_URL, GenericUtil.getObjUrl(object));
		  }
		        return result;
	 }
	
	
	 /**
	  * ��ö���İ汾(A.3)
	  * @param object
	  * @return
	  */
	 public static String getObjectVersion(RevisionControlled object){
		  checkNull(object);
		  String result=null;
		 try {
			 String version =VersionControlHelper.getVersionIdentifier(object).getValue();
			 String iteration=VersionControlHelper.getIterationIdentifier(object).getValue();
	         result=version+"."+iteration;
		 } catch (VersionControlException e) {
			 Debug.P(e.getMessage());
			e.printStackTrace();
		}
		  return result;
	 }
	 
	 
	 
	 /**
	  * �޶��汾
	  * @param pm_oid
	  * @throws Exception
	  */
	 public static void changeRevision(String pm_id)throws Exception{
		   Debug.P("----->>Check Revision PM_DocID:"+pm_id);
		   checkNull(pm_id);
			if (!RemoteMethodServer.ServerFlag) {
		            String method = "changeRevision";
		            String klass = PMWebserviceImpl.class.getName();
		            Class[] types = { String.class,};
		            Object[] vals = {pm_id};
		            RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
		     }else{
		    	 //��ȡPM�ĵ�����
	             BasicDocument basic_object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
	             checkNull(basic_object);
			   //���WindChill ID
	        	String wc_id=basic_object.getPLMId();
	        	Debug.P("----------->>>Windchill PLMID:"+wc_id);
	        	try {
//	        		SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	        	    SessionHelper.manager.setAdministrator();
	        		if(!StringUtils.isEmpty(wc_id)){
					     Persistable object=GenericUtil.getPersistableByOid(wc_id);
					     if(object!=null){
					     object=getLastObjectByNum(object); 
						 Folder folder = FolderHelper.service.getFolder((FolderEntry) object);
						 Persistable newobject= VersionControlHelper.service.newVersion((Versioned) object);
						 FolderHelper.assignLocation((FolderEntry) newobject,folder);
						 PersistenceHelper.manager.save(newobject);
						 GenericUtil.changeState((LifeCycleManaged) newobject, ConstanUtil.WC_INWORK);//�޶�ʱ��������������״̬��Ϊ������
						 PersistenceHelper.manager.refresh(newobject);
						 if(newobject instanceof EPMDocument){
							 EPMDocument empdoc=(EPMDocument)newobject;
							 basic_object.setPLMId(empdoc.getPersistInfo().getObjectIdentifier().getStringValue());
							 basic_object.setMajorVid(empdoc.getVersionIdentifier().getValue());
							 basic_object.setSecondVid(Integer.valueOf(empdoc.getIterationIdentifier().getValue()));
						 }else if(newobject instanceof WTPart){
							 WTPart part=(WTPart)newobject;
							 basic_object.setPLMId(part.getPersistInfo().getObjectIdentifier().getStringValue());
							 basic_object.setMajorVid(part.getVersionIdentifier().getValue());
							 basic_object.setSecondVid(Integer.valueOf(part.getIterationIdentifier().getValue()));
						 }else if(newobject instanceof WTDocument){
							 WTDocument doc=(WTDocument)newobject;
							 basic_object.setPLMId(doc.getPersistInfo().getObjectIdentifier().getStringValue());
							 basic_object.setMajorVid(doc.getVersionIdentifier().getValue());
							 basic_object.setSecondVid(Integer.valueOf(doc.getIterationIdentifier().getValue()));
						 }
						     basic_object.doUpdate();
					     }
					}
				} catch (Exception e) {
					e.printStackTrace();
					basic_object.doSetErrorMessage(10, "PLM �޶�("+wc_id+")�����汾�쳣!");
				    throw new Exception(e.getMessage());
				}finally{
					SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
				}
		     }
		   
        	
	 }

	 /**
	  * �޸���������״̬
	  * @param pm_id
	  * @return
	  */
	 public static void changeLifecycleState(String pm_id)throws Exception{
		  Debug.P("----------->>>>Change Lifecycle  PMID:"+pm_id);
		  
		   if (!RemoteMethodServer.ServerFlag) {
	            String method = "changeLifecycleState";
	            String klass = PMWebserviceImpl.class.getName();
	            Class[] types = { String.class,};
	            Object[] vals = {pm_id};
	            RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
	     }else{
			 //��ȡPM�ĵ�����
			  if(!StringUtils.isEmpty(pm_id)){
				     BasicDocument  basic_object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
			      	 checkNull(basic_object);
			      	//���WindChill ID
			        String wc_id=basic_object.getPLMId();
			        Debug.P("----------->>>Windchill PLMID:"+wc_id);
			        try {
			        	  SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
					      Persistable object=GenericUtil.getPersistableByOid(wc_id);
					      if(object!=null){
					      object=getLastObjectByNum(object);
					      String stateName=basic_object.getStatus();
					      Debug.P("------->>>PM State:"+stateName);
					      object=GenericUtil.changeState((LifeCycleManaged) object, stateMap.get(stateName));
					      PersistenceHelper.manager.refresh(object);
					      basic_object.doUpdate();
					      }
			        } catch (Exception e) {
					    e.printStackTrace();
					    throw new Exception("Windchill �޸Ķ���("+wc_id+"��������״̬ʧ��!");
					}finally{
						SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
					}
			  }
	     }
	 }
	 
	 
	 /**
	  * ���Ķ���׶�
	  * @param pm_id
	  * @throws Exception
	  */
	 public static int  changePhase(String pm_id)throws Exception{
		  Debug.P("------->>>Change Phase PMID:"+pm_id);
		   int count=0;
		   if (!RemoteMethodServer.ServerFlag) {
		            String method = "changePhase";
		            String klass = PMWebserviceImpl.class.getName();
		            Class[] types = { String.class,};
		            Object[] vals = {pm_id};
		            return (Integer)RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
		     }else{
				  if(!StringUtils.isEmpty(pm_id)){
					  //����pm_id�޸Ľ׶���Ϣ
					  BasicDocument object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
		              String phase=object.getPhase();//�׶�
		           	  String wc_id=object.getPLMId();//PM��Ӧ��Windchill Number�ֶ�
		              Debug.P("----Phase--->>>Windchill ID:"+wc_id+"   ;Phase Value:"+phase);
		              if(!StringUtils.isEmpty(wc_id)){
		             try {
		            	 SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
		            	 Persistable persistable=GenericUtil.getPersistableByOid(wc_id);
		            	 if(StringUtils.isNotEmpty(phase)&&persistable!=null){
					    	persistable=getLastObjectByNum(persistable);
					        IBAUtils iba_values=new IBAUtils((IBAHolder)persistable);
					        iba_values.setIBAValue(ConstanUtil.PHASE, phase);
					        iba_values.updateIBAPart((IBAHolder)persistable);
					        object.doUpdate();
						    count=1;
					    }
					 } catch (Exception e) {
						 e.printStackTrace();
						 throw new Exception("Windchill ("+wc_id+") �޸Ľ׶�ʧ��!");
					}finally{
						SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
					}       
		        }    
			 }
		 }
		      return count;
	 }
	 
	 
	 
	 
	 /**
	  * ��ö���Ŀ��ӻ�����
	  * @param plmId
	  * @return
	  */
	 public static String getViewContentURL(String plmId)throws Exception{
		 Debug.P("--------->>>PLMID:"+plmId);
		 String result=null;
		 try {
			SessionHelper.manager.setAdministrator();
			if(!plmId.startsWith("VR")){plmId="VR:"+plmId;}
			Persistable object=GenericUtil.getPersistableByOid(plmId);
		    if(object!=null){
		    	object=getLastObjectByNum(object);
		    	Debug.P("------>>>EPM>>>>>>>");
		        String url=GenericUtil.getViewContentHrefUrl(object);
		        if(!StringUtils.isEmpty(url)){
		        	result=url.substring(url.indexOf("/"),url.indexOf(","));
		        }
		    }
		    Debug.P("------->>>>>View URL:"+result);
		} catch (Exception e) {
		   throw new Exception("��ȡ����Ŀ��ӻ�����ʧ��!");
		}finally{
			SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
		}
		  return result;
	 }

	 
	 /**
	  * �������Ƿ��д��PMϵͳ
	  * @return
	  */
	 private static WTDocument checkWTDocumentWrite2PM(String pmOid){
		 //У����ͨ��������Ϣ
		WTDocument doc=null;
	    Map<String,String> ibaValues=new HashMap<String,String>();
	    ibaValues.put(ConstanUtil.PMID, pmOid);
	    List<Persistable> list=LWCUtil.getObjectByIBA(ibaValues, ConstanUtil.WTDOCTYPE);
	    if(list!=null&&list.size()>0){
	    	doc=(WTDocument)list.get(0);
	    }
	      return doc;
	 }
	 
	 public static void main(String[] args) throws Exception {
		   String oid="VR:wt.epm.EPMDocument:4975395";
		  Persistable object=GenericUtil.getPersistableByOid(oid);
		  if(object!=null){
			  object=getLastObjectByNum(object);
			  VisualizationHelper visualizationHelper = VisualizationHelper.newVisualizationHelper();
			  String [] arr= visualizationHelper.getDefaultVisualizationStringsForSearch(object, Locale.US);
//			  String [] arr=visualizationHelper.getDefaultVisualizationData(object.toString(),true,Locale.US);
			  boolean flag=visualizationHelper.isWVSEnabled();
			  System.out.println("----flag:"+flag);
			  if(flag){
				  String fl=visualizationHelper.DEFAULT_THUMBNAILS_PROP_PAGE_PREF_VALUE;
                  System.out.println("->>>>>>"+fl);
			  }
//			  visualizationHelper.getppwAutoloadPref(arg0)
			  int pindx=visualizationHelper.productViewLinkIndex();
			  System.out.println("---->>>Link:"+arr[pindx]);
		  }
		
	 }
	 
	 
	 
}
