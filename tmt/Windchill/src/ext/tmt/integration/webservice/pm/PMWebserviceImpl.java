package ext.tmt.integration.webservice.pm;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import wt.content.ApplicationData;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.iba.definition.StringDefinition;
import wt.iba.value.IBAHolder;
import wt.iba.value.StringValue;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleManaged;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.pds.StatementSpec;
import wt.query.ClassAttribute;
import wt.query.ConstantExpression;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.representation.Representable;
import wt.session.SessionHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.WorkInProgressHelper;

import com.mongodb.WriteResult;
import com.sg.visionadapter.BasicDocument;
import com.sg.visionadapter.DocumentPersistence;
import com.sg.visionadapter.FolderPersistence;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMDocument;
import com.sg.visionadapter.PMFolder;

import ext.tmt.folder.api.FolderService;
import ext.tmt.folder.impl.FolderServiceImpl;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;


/**
 * Webservice服务实现类
 * @author public759
 *
 */
@SuppressWarnings("all")
public class PMWebserviceImpl implements Serializable,RemoteAccess{
	

	
	private static final long serialVersionUID = -9012564223029784741L;


	/*文件夹服务接口*/
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
	  * 创建文件夹结构
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
	  * 创建文件夹
	  * @param pm_id
	 * @throws Exception 
	  */
	 private  static  void createFolderEntry(String objectId) throws Exception{
		    Folder folderResult=null;
		    //首先得到PM Folder对象
		    if(objectId==null) {throw new IllegalArgumentException("----Args PMID is Null");}
		    ModelServiceFactory factory= ModelServiceFactory.getInstance(codebasePath);
			FolderPersistence folderPersistence = factory.get(FolderPersistence.class);
			PMFolder pmfolder=folderPersistence.get(new ObjectId(objectId));//PM文件夹对象
			checkNull(pmfolder);
			boolean iscreate=pmfolder.getPLMId()==null?true:false;
			PMFolder parentFolder=pmfolder.getParentFolder();//父文件夹
			checkNull(parentFolder);
			String containerName=pmfolder.getContainerName();//ContainerName
			boolean  isContainer=parentFolder.isContainer();//是否为容器
			String parent_wcId=parentFolder.getPLMId();//获得父项对象Id
			String folderName=pmfolder.getCommonName().trim();
			Debug.P("------>>>Folder:"+folderName+"  ContainerName:"+containerName+"  isContainer="+isContainer+"  ParentFolderID="+parent_wcId);
			try{
		    	SessionHelper.manager.setAdministrator();
		    	WTContainer container=checkWTContainerExist(containerName);
		    	if(iscreate){//是否同步防止重复创建
			    	 //如果父项是容器则在容器下创建文件夹
			    	 if(isContainer){
			    		  Debug.P("-----Container----->>>Ready Create FolderPath: "+(DEFAULT+"/"+folderName));
			    		  String folderPath=DEFAULT+"/"+folderName;
			    		  folderResult=FolderUtil.getFolderRef(folderPath,container,true);
			    	 }else{
			    		 //否则获得父项的文件夹对象
			    		 Persistable persistable=GenericUtil.getPersistableByOid(parent_wcId);
			    		 if(persistable!=null&&persistable instanceof Folder){
			 	             Folder parent_Folder=(Folder)persistable;
			 	             folderResult=FolderUtil.createSubFolder(folderName, null, parent_Folder, null);
			 	            }
			    	      }
	                  //回写Windchill Folder Oid到PM系统
	                  String wc_oid=folderResult.getPersistInfo().getObjectIdentifier().getStringValue();//OID
	                  Debug.P("------Windchill Folder_OID:"+wc_oid);
	                  pmfolder.setPLMId(wc_oid);
	                  pmfolder.setPLMData(getObjectInfo(folderResult));
	                  pmfolder.doUpdate();//修改
	                  Debug.P("----->>>创建同步Windchill文件夹:("+folderName+")成功!");
			    	 }
		    }catch(Exception e){
		    	e.printStackTrace();
		    	throw new Exception("Windchill创建文件夹("+folderName+")失败!");
		    }finally{
		    	SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
		    }
	 }
	 
	 /**
	  * 修改文件夹名称
	  * @param objectId 
	  * @param newFolderName 新文件夹名称
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
	  		 //查询PM文件夹对象
	  		 FolderPersistence folderPersistence =  ModelServiceFactory.getInstance(codebasePath).get(FolderPersistence.class);
	       	 PMFolder folder=folderPersistence.get(new ObjectId(objectId));//PM文件夹对象
	       	 String oldFolderName=folder.getCommonName();
	       	 checkNull(folder);
	       	 //获得Windchill 文件夹对象
	       	 String foid=folder.getPLMId();
	       	 String containerName=folder.getContainerName();
	      	 Debug.P("------->>Modify Folder:"+folder.getCommonName()+" ;FOID="+foid+"   ;ContainerName="+containerName);
	      	 try{
	      		 SessionHelper.manager.setAdministrator();
	    		 Debug.P("----->>PM Edit Windchill Folder_OID:"+foid);
		       	 if(!StringUtils.isEmpty(foid)){
		       		Persistable persistable=GenericUtil.getPersistableByOid(foid);
		       		if(persistable!=null){
		       			if(persistable instanceof Folder){
		       				Folder folderObj=(Folder)persistable;
		       				String folderPath=folderObj.getFolderPath();
		       				count=folderService.editFolder(folderPath, newFolderName, containerName);
		       				if(count>0){
		       					folder.doUpdate();
		       					Debug.P("------>>PM 更新 OldFolderName("+oldFolderName+") 成NewFolderName("+newFolderName+")Success!");
		       				}
		       			}
		       		}
		       	 }
	      	 }catch(Exception e){
	      		 e.printStackTrace();
	      		 throw new Exception("Windchill修改文件夹("+foid+")信息失败!");
	      	 }finally{
	      		 SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	      	 }
	       }
     	       return count;
	 }
	 
	 /**
	  * 删除文件夹包含文件夹下的对象
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
		        	 PMFolder folder=folderPersistence.get(new ObjectId(objectId));//PM文件夹对象
		        	 checkNull(folder);
			         SessionHelper.manager.setAdministrator();
			         String containerName=folder.getContainerName();
			        //获得Windchill的PLMID
			        String foid=folder.getPLMId();
			        Debug.P("------Ready Delete FolderName:"+folder.getCommonName()+"  Windchill FolderId:"+foid);
		        	try{
			        	 if(!StringUtils.isEmpty(foid)){
			          		Persistable persistable=GenericUtil.getPersistableByOid(foid);
			          		if(persistable!=null){
			          			if(persistable instanceof Folder){//是否为文件夹类型
			          				Folder folderObj=(Folder)persistable;
			          				String folderPath=folderObj.getFolderPath();
			          				count=folderService.deleteFolder(folderPath, containerName);
			          				if(count>0){//如果Windchill删除成功则删除PM系统数据
			          					folder.doRemove();
			          					Debug.P("----Remove PM Folder:"+folder.getCommonName()+" Success!");
			          				}
			          				
			          			}
			          		}
			          	 }
	        	}catch(Exception e){
	        		 e.printStackTrace();
	        		 throw new Exception("Windchill删除文件夹("+foid+"失败!");
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
	 * 检查容器是否存在
	 * @param containerName
	 * @throws Exception
	 */
	private static WTContainer checkWTContainerExist(String containerName)throws Exception{
		WTContainer container=null;
	try{
		 container=GenericUtil.getWTContainerByName(containerName);
		if(container==null){
			throw new Exception("Windchill中不存在PM中的容器对象,请联系管理员配置!");
		}
	} catch (Exception e) {
		throw new Exception("Windchill查询("+containerName+")异常!");
	  }
        return container;
} 
	
	/**
	 * 创建WTDocument文档
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
	        	WTDocument document=WTDocument.newWTDocument();
	        	//获得PM文档对象
	        	DocumentPersistence docPersistance=ModelServiceFactory.getInstance(codebasePath).get(DocumentPersistence.class);
	        	PMDocument pm_document=docPersistance.get(new ObjectId(pm_docId));
	        	PMFolder pmfolder=pm_document.getFolder();//获得文档所在的PM文件夹
	        	String wc_foid=pmfolder.getPLMId();//Windchill 文件夹 Oid 
	        	boolean isContainer=pmfolder.isContainer();
	        	boolean iscreate=pm_document.getPLMId()==null?true:false;//是否已同步到Windchill
	        	String containerName=pmfolder.getContainerName();
	        	Debug.P("----->>>>WC   Folder ID:"+wc_foid+"  是否为PM的容器文件夹:"+isContainer +"  ;ContaienrName:"+containerName);
	        	try{
	        		SessionHelper.manager.setAdministrator();
	        		  Persistable persistable=null;
	        		  WTContainer container=null;
	        		  Debug.P("------>>>PM DOC_ID："+pm_docId+"是否新建到Windchill="+iscreate);
	        		  if(iscreate){//判断是否已同步到Windchill
	        			  //文件夹对象
	            		  if(!StringUtils.isEmpty(wc_foid)){
		        				persistable=GenericUtil.getPersistableByOid(wc_foid);
		        		  }else{//创建到容器下
		        			   container=GenericUtil.getWTContainerByName(containerName);
		        			   persistable=GenericUtil.createNewPath(container);
		        		  }
		        		if(persistable instanceof Folder){//文件夹
		        			Folder folder=(Folder)persistable;
		                   //判断文档是否已创建
		       			   boolean isEmpty=StringUtils.isEmpty(pm_document.getPLMId());
		        			if(isEmpty){//新建
		            			Map ibas=new HashMap();//软属性集合
		            			setDocIBAValuesMap(ibas, pm_document);
		        				document= DocUtils.createDocument(pm_document, null,VMUSER,ibas,folder);
		        				if(isContainer){//容器
		        					GenericUtil.moveObject2Container(document, container,folder);
		        				}else{//文件夹
		        					FolderUtil.changeFolder(document,folder);
		        				}
		            		   }
		        			}
		        		//回写Windchill信息到PM
	            		String wcId=document.getPersistInfo().getObjectIdentifier().getStringValue();
	            		pm_document.setPLMData(getObjectInfo(document));
	            		pm_document.setPLMId(wcId);
	            		pm_document.setMajorVid(document.getVersionIdentifier().getValue());
	            		pm_document.setSecondVid(Integer.valueOf(document.getIterationIdentifier().getValue()));
	            		WriteResult result=pm_document.doUpdate();//修改
	            		Debug.P("----->>>PM Return:("+result.getN()+")Create PMID:"+wcId+"  ;PM_Document:"+pm_document.getPLMId());
	            		count=1;
	        		  }
	        	}catch(Exception e){
	        		 e.printStackTrace();
	        		throw new Exception("Windchill 创建("+pm_document.getCommonName()+")文档失败!");
	        	}finally{
	        		SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	        	}
	     }
         	     return count;
    }
    

    /**
     * 更新文档信息
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
	     	//判断是否已经同步到Windchill
     		boolean isCreated=pm_document.getPLMId()==null?false:true;
     		String doc_id=pm_document.getPLMId();
     		Debug.P("------>>>>Windchill中是否已经创建("+pm_docName+"):"+isCreated+"  Doc_Windchill:"+doc_id);
	     		try{
	     			SessionHelper.manager.setAdministrator();
	         		if(isCreated){
	         			if(StringUtils.isEmpty(doc_id)) return 0;
	         			String doc_num=(String) pm_document.getPLMData().get(ConstanUtil.NUMBER);
	         			Persistable object= GenericUtil.getObjectByNumber(doc_num);
	         			if(object!=null&&object instanceof WTDocument){
	         				WTDocument doc=(WTDocument)object;
	         				Map ibas=LWCUtil.getAllAttribute(doc);
	         			    setDocIBAValuesMap(ibas, pm_document);//更新软属性
	         				doc=(WTDocument) GenericUtil.checkout(doc);
	         				doc=DocUtils.updateWTDocument(doc,pm_document, ibas);//更新文档
	         				if (doc != null) {
	         					if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(doc, wt.session.SessionHelper.manager.getPrincipal()))
	         						doc = (WTDocument) WorkInProgressHelper.service.checkin(doc, "update document Info");
	         				   }
	         			
	             			//操作完回调doUpdate()
	         				pm_document.setPLMData(getObjectInfo(doc));
	         				pm_document.setPLMId(doc.getPersistInfo().getObjectIdentifier().getStringValue());
	         				pm_document.setMajorVid(doc.getVersionIdentifier().getValue());
	         				pm_document.setSecondVid(Integer.valueOf(doc.getIterationIdentifier().getValue()));
	         				pm_document.doUpdate();
	         			    Debug.P("------>>>Update PM_DocumentName："+pm_docName+" Success!");
	         			}
	         		}
	     		}catch(Exception e){
	     			e.printStackTrace();
	     			throw new Exception("Windchill更新("+doc_id+")文档对象失败!");
	     		}finally{
	     			SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	     		}
    	}
    	  return count;
    }


    
	
	
    /**
     * 删除文档对象
     * @param pm_docId
     * @return
     * @throws Exception
     */
    public static int deleteWTDocumentEntry(String pm_docId)throws Exception{
    	
       Debug.P("------>>>Delete PM_DocumentID："+pm_docId);
       
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
       	 //获得PM 文档对应的Windchill文档ID
       	String wc_oid=pm_document.getPLMId();
       	Debug.P("------>>>PM("+pm_docId+")<--->Windchill("+wc_oid+")");
       	try {
       		SessionHelper.manager.setAdministrator();
       		if(!StringUtils.isEmpty(wc_oid)){
       			String doc_num=(String) pm_document.getPLMData().get(ConstanUtil.NUMBER);
       			WTDocument doc=(WTDocument) GenericUtil.getObjectByNumber(doc_num);
           		 if(doc!=null){
           			 GenericUtil.deleteDoc(doc, null);
           			 return 1;
           		 }
           	  }
   	    	} catch(Exception e){
   	    		e.printStackTrace();
   			   throw new Exception("Windchill删除文档对象("+wc_oid+")失败!");
   		   }finally{
   			SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
   		  }
       }
    	  return 0;
    }
    
    
    /**
     * 同步移动PM文档的路径到Windchill系统中更改
     * @param pm_docId
     * @return
     * @throws Exception
     */
    public static int moveWTDocumentEntry(String pm_docId)throws Exception{

   	 Debug.P("------>>>Move Path PM_DocumentID ："+pm_docId);
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
    	 	//根据PM文件夹找到与之对应的Windchill文件夹Oid
    	     PMFolder folder=basic_object.getFolder();
    	     checkNull(folder);
    	 	 //判断文件夹是否为容器Root
    	 	 boolean isContainer=folder.isContainer();
    	 	 String containerName=folder.getContainerName();
    	 	 checkNull(folder);
    	 	 String wc_foid=folder.getPLMId();
    		 Debug.P("---->>Windchill Folder ID:"+wc_foid+"  Windchill Doc ID:"+basic_object.getPLMId());
    	 	   try {
    	 		  String doc_num=(String) basic_object.getPLMData().get(ConstanUtil.NUMBER);
    			   if(!StringUtils.isEmpty(doc_num)){
    			    	SessionHelper.manager.setAdministrator();
    			    	Persistable object= GenericUtil.getObjectByNumber(doc_num);
	         			if(object!=null){
	    			    	Folder folderObj=null;
		         			if(isContainer){//容器
		         					WTContainer container=GenericUtil.getWTContainerByName(containerName);
		         					folderObj=GenericUtil.createNewPath(container);
		         					GenericUtil.moveObject2Container(object, container, folderObj);//移动到容器目录下
		         				}else{//文件夹
		         					if(StringUtils.isEmpty(wc_foid)){throw new Exception("文档("+basic_object.getCommonName()+")目标文件夹("+folder.getCommonName()+")在Windchill系统不存在 ,无法执行移动操作!");}
		         					folderObj=(Folder) GenericUtil.getPersistableByOid(wc_foid);
		         					FolderUtil.changeFolder((FolderEntry) object, folderObj);//移动文档位置
		         				}
		         			PersistenceHelper.manager.refresh(folderObj);
		         			return 1;
	         			}
    			    }
    		    }catch(Exception e){
    		    	e.printStackTrace();
    		    	throw new Exception("Windchill移动对象到("+wc_foid+")中失败!");
    		    } finally {
    	             SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
    		    }
       }
	 
 	              return 0;
  }
    
    
    
	/**
	 * 映射文档对象对象软属性
	 * @param ibas
	 * @param object
	 * @throws WTException
	 */
	private  static void setDocIBAValuesMap(Map ibas,PMDocument object )throws WTException{
		//设置对象参数
		if(!StringUtils.isEmpty(object.getPhase())){
			 ibas.put(ConstanUtil.PHASE, object.getPhase());//阶段
		}
		if(!StringUtils.isEmpty(object.getProductNumber())){
			ibas.put(ConstanUtil.PROJECTNO,object.getProductNumber());//项目编号
		}
		if(!StringUtils.isEmpty(object.getProjectWorkOrder())){
			ibas.put(ConstanUtil.WORKORDER, object.getProjectWorkOrder());//工作令号
		}
		if(!StringUtils.isEmpty(object.getProductNumber())){
			  ibas.put(ConstanUtil.PRODUCTNO, object.getProductNumber());//关联的成品号
		}
		if(!StringUtils.isEmpty(object.getCreateByUserId())){
			  ibas.put(Contants.PMCREATOR, object.getCreateByUserId());//创建者
		}
		if(!StringUtils.isEmpty(object.getModifiedUserId())){
			  ibas.put(Contants.PMMODIFYEDBY, object.getModifiedUserId());//修改者
		}
	}
	
	

	/**
	 * 获取对象的属性信息
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
				   //获取对象的软属性集合
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
				   //获取对象的软属性集合
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
				   //获取对象的软属性集合
				   Map<String,Object> ibas=LWCUtil.getAllAttribute(doc);
				   result.putAll(ibas);
			   }else if(object instanceof Folder){
				   Folder folder=(Folder)object;
				   result.put(ConstanUtil.NAME, folder.getName());
			   }
			     //对象链接地址
			     result.put(ConstanUtil.OBJECT_URL, GenericUtil.getObjUrl(object));
		  }
		        return result;
	 }
	
	
	 /**
	  * 获得对象的版本(A.3)
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
	  * 修订版本
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
		    	 //获取PM文档对象
	             BasicDocument basic_object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
	             checkNull(basic_object);
			   //获得WindChill ID
	        	String wc_id=basic_object.getPLMId();
	        	Debug.P("----------->>>Windchill PLMID:"+wc_id);
	        	try {
					SessionHelper.manager.setAdministrator();
					if(!StringUtils.isEmpty(wc_id)){
					     String doc_num=(String) basic_object.getPLMData().get(ConstanUtil.NUMBER);
					     Persistable object=GenericUtil.getObjectByNumber(doc_num);
					     if(object!=null){
						 Folder folder = FolderHelper.service.getFolder((FolderEntry) object);
						 Persistable newobject= VersionControlHelper.service.newVersion((Versioned) object);
						 FolderHelper.assignLocation((FolderEntry) newobject,folder);
						 PersistenceHelper.manager.save(newobject);
						 GenericUtil.changeState((LifeCycleManaged) newobject, ConstanUtil.WC_INWORK);//修订时将对象生命周期状态改为工作中
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
					basic_object.doSetErrorMessage(10, "PLM 修订("+wc_id+")升级版本异常!");
				    throw new Exception(e.getMessage());
				}finally{
					SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
				}
		     }
		   
        	
	 }

	 /**
	  * 修改生命周期状态
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
			 //获取PM文档对象
			  if(!StringUtils.isEmpty(pm_id)){
				     BasicDocument  basic_object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
			      	 checkNull(basic_object);
			      	//获得WindChill ID
			        String wc_id=basic_object.getPLMId();
			        Debug.P("----------->>>Windchill PLMID:"+wc_id);
			        try {
						  SessionHelper.manager.setAdministrator();
						  String doc_num=(String) basic_object.getPLMData().get(ConstanUtil.NUMBER);
					      Persistable object=GenericUtil.getObjectByNumber(doc_num);
					      if(object!=null){
					      String stateName=basic_object.getStatus();
					      Debug.P("------->>>PM State:"+stateName);
					      object=GenericUtil.changeState((LifeCycleManaged) object, stateMap.get(stateName));
					      PersistenceHelper.manager.refresh(object);
					      basic_object.doUpdate();
					      }
			        } catch (Exception e) {
					    e.printStackTrace();
					    throw new Exception("Windchill 修改对象("+wc_id+"生命周期状态失败!");
					}finally{
						SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
					}
			  }
	     }
	 }
	 
	 
	 /**
	  * 更改对象阶段
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
					  //根据pm_id修改阶段信息
					  BasicDocument object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
		              String phase=object.getPhase();//阶段
		           	   String plm_num=(String) object.getPLMData().get(ConstanUtil.NUMBER);//PM对应的Windchill Number字段
		              Debug.P("----Phase--->>>Windchill num:"+plm_num+"   ;Phase Value:"+phase);
		              if(!StringUtils.isEmpty(plm_num)){
		             try {
					    SessionHelper.manager.setAdministrator();
					    if(!StringUtils.isEmpty(phase)){
					    	Persistable persistable=GenericUtil.getObjectByNumber(plm_num);
					        IBAUtils iba_values=new IBAUtils((IBAHolder)persistable);
					        iba_values.setIBAValue(ConstanUtil.PHASE, phase);
					        iba_values.updateIBAPart((IBAHolder)persistable);
					        object.doUpdate();
						    count=1;
					    }
					 } catch (Exception e) {
						 e.printStackTrace();
						 throw new Exception("Windchill ("+plm_num+") 修改阶段失败!");
					}finally{
						 SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
					}       	        
		        }    
			 }
		 }
		      return count;
	 }
	 
	 
	 
	 
	 /**
	  * 获得对象的可视化链接
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
		    	Debug.P("------>>>EPM>>>>>>>");
		        String url=GenericUtil.getViewContentHrefUrl(object);
		        if(!StringUtils.isEmpty(url)){
		        	result=url.substring(url.indexOf("/"),url.indexOf(","));
		        }
		    }
		    Debug.P("------->>>>>View URL:"+result);
		} catch (Exception e) {
		   throw new Exception("获取对象的可视化链接失败!");
		}finally{
			SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
		}
		  return result;
	 }

	 /**
	  * 根据对象查询与该对象关联的对象
	  * @param pmid 对象的PMID
	  * @param class1 对象的类型，(wtpart,wtdocument,epmdocument)
	  * @param class2 要查询的对象类型，(wtpart,wtdocument,epmdocument)
	  * @param flag 如果查询对象结构(BOM结构)，true为向下查询，false为向上查询
	  * @return 返回关联对象的PMID集合
	  * @throws Exception
	  */
	 public static List<String> queryWCObjectByPM(String pmid ,String class1,String class2,boolean flag) throws Exception{
		 List<String> pmList = new ArrayList<String>();
		 WTPart part =null;
		 Debug.P("pmid-->"+pmid+"---class1--->"+class1+"----class2--->"+class2+"----flag--->"+flag);
		 if(StringUtils.isEmpty(class1)||StringUtils.isEmpty(class1)){
			 return null;
		 }
		 if(class1.equalsIgnoreCase("wtpart")){
			 part= (WTPart)searchWCObject(WTPart.class,pmid,Contants.PROJECTNO);
			 Debug.P(part);
			 if(part!=null){
				 part = PartUtil.getPartByNumber2(part.getNumber());
				 Debug.P(part.getNumber()+"-------"+part.toString()+"-------"+pmid);
				 //根据部件查部件
				 if(class2.equalsIgnoreCase("wtpart")){
					 if(flag){//向下查询子项
						 pmList=PartUtil.queryPartPMIDByBOM(part); 
					 }else{//向上查询父项
						 pmList=PartUtil.queryPrentPartsByParts(part);
					 }
				 }
				 //根据部件查询文档
				 else if(class2.equalsIgnoreCase("wtdocument")){
					 pmList=PartUtil.getDescriptDocPMIdBy(part);
				 }
				 //根据部件查询EPM文档
				 else if(class2.equalsIgnoreCase("epmdocument")){
					 Debug.P("根据部件查询EPM文档----->"+class2);
					 pmList=PartUtil.getEPMDocPMIDByPart(part);
				 }
			 }
		 }else if(class1.equalsIgnoreCase("wtdocument")){//根据文档查询该文档的说明部件
			 WTDocument doc = (WTDocument)searchWCObject(WTDocument.class,pmid,Contants.PMID);
			 if(doc!=null){
				 doc=DocUtils.getDocByNumber(doc.getNumber());
				 if(class2.equalsIgnoreCase("wtpart")){
					 pmList=DocUtils.getDescribePartsByDoc(doc);
				 }if(class2.equalsIgnoreCase("wtdocument")){
					 pmList=DocUtils.getWTDocPMIDByWTDocument(doc);
				 }
			 }
		 }else if(class1.equalsIgnoreCase("epmdocument")){//根据EPM文档查询该EPM文档的说明部件
			 EPMDocument epmdoc = (EPMDocument)searchWCObject(WTDocument.class,pmid,Contants.PMID);
			 if(epmdoc!=null){
				 epmdoc=EPMDocUtil.getEPMDocByNumber(epmdoc.getNumber());
				 if(class2.equalsIgnoreCase("wtpart")){
					 pmList=DocUtils.getDescribePartsByEPMDoc(epmdoc);
				 }
			 }
		 }
		 return pmList;
	 } 
	 
	 
	 /**
	  * 根据IBA属性查询对象
	  * @param class1
	  * @param ibavalue
	  * @param ibakey
	  * @throws WTException
	  */
	 public static Object searchWCObject(Class class1,String ibavalue,String ibakey)throws  WTException {
		 Object object = null;
		 QuerySpec qs = new QuerySpec();
		 qs.setAdvancedQueryEnabled(true);
		 int objindex = qs.appendClassList(class1, true);
		 
		 int pmSDIndex = 0;
		 int pmSVIndex = 0;
		 
		 if(StringUtils.isNotEmpty(ibavalue)){
			 pmSVIndex = qs.appendClassList(StringValue.class, false);
			 pmSDIndex = qs.appendClassList(StringDefinition.class, false);
			 ClassAttribute caValue = new ClassAttribute(StringValue.class,
						StringValue.VALUE2);
				WhereExpression we = new SearchCondition(
						SQLFunction.newSQLFunction(SQLFunction.UPPER, caValue),
						SearchCondition.EQUAL,
						ConstantExpression.newExpression(ibavalue.toUpperCase()));
				qs.appendWhere(we, new int[] { pmSVIndex });
				
				we = new SearchCondition(StringDefinition.class,
						StringDefinition.NAME, SearchCondition.EQUAL,ibakey);
				qs.appendAnd();
				qs.appendWhere(we, new int[] { pmSDIndex });

				we = new SearchCondition(StringValue.class,
						"definitionReference.key.id", StringDefinition.class,
						WTAttributeNameIfc.ID_NAME);
				qs.appendAnd();
				qs.appendWhere(we, new int[] { pmSVIndex,pmSDIndex });

				we = new SearchCondition(StringValue.class,
						"theIBAHolderReference.key.id", class1,
						WTAttributeNameIfc.ID_NAME);
				qs.appendAnd();
				qs.appendWhere(we, new int[] { pmSVIndex, objindex });
				Debug.P(qs);
				QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
				while (qr.hasMoreElements()) {
					Object[] objects = (Object[])qr.nextElement();
					object=objects[0];
				}
		 }
		 return object;
	 }
	 
	 public static ObjectInputStream getRepresentationByPM(String pmid) throws WTException, IOException{
		 ObjectInputStream ois = null;
		 InputStream is = null;
		 Object object = searchWCObject(EPMDocument.class,pmid,Contants.PMID);
		 if(object instanceof EPMDocument){
			 EPMDocument epm =(EPMDocument)object;
				ApplicationData ad = DocUtils.getPdfRep((Representable)epm); // 如果是表示法，则进行getPdfRep（）的取值
				Debug.P(ad);
				if (ad == null)
					return null;
				if (!RemoteMethodServer.ServerFlag) {
					try {
						Class aclass[] = { ApplicationData.class };
						Object aobj[] = { ad };
						RemoteMethodServer.getDefault().invoke("findContentStream",
								ContentServerHelper.service.getClass().getName(),
								ContentServerHelper.service, aclass, aobj);
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else{
					is= ContentServerHelper.service.findContentStream(ad);
					ois= new ObjectInputStream(is);
					return ois;
				}
		 }
		 return null;
	 }
	 
	 public static void main(String[] args) throws Exception {
		 RemoteMethodServer  rms = RemoteMethodServer.getDefault();
		 rms.setUserName("wcadmin");
		 rms.setPassword("wcadmin");
		 
		 String pid="VR:wt.epm.EPMDocument:174627";
		 String cid="VR:wt.epm.EPMDocument:96452";
		 String str="Project_NO";
		List<String> list=queryWCObjectByPM(args[0],args[1],args[2],true);
		for(String strs:list){
			Debug.P("strs---------->"+strs);
		}
//		Object obj= searchWCObject(WTDocument.class,args[0],Contants.PMID);
//		Debug.P(obj);
//		if(obj instanceof WTPart){
//			WTPart part =(WTPart)obj;
//			Debug.P(part.getNumber());
//		}else if(obj instanceof WTDocument){
//			WTDocument doc =(WTDocument)obj;
//			Debug.P(doc.getNumber());
//		}
	}
	 
}
