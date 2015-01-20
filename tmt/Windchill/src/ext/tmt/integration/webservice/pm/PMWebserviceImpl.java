package ext.tmt.integration.webservice.pm;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
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
import wt.pom.Transaction;
import wt.query.ClassAttribute;
import wt.query.ConstantExpression;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.representation.Representable;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
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


import ext.tmt.part.PartUtils;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.Utils;



/**
 * Webservice服务实现类
 * @author public759
 *
 */

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
	  *  创建文件夹
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
			Transaction tx = null;
			try{
				tx=new Transaction();
				tx.start();
		    	SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
		    	WTContainer container=checkWTContainerExist(containerName);
		    	if(iscreate){
		    		 //如果父项是容器则在容器下创建文件夹
			    	 if(isContainer){
			    		  Debug.P("-----Container----->>>Ready Create FolderPath: "+(DEFAULT+"/"+folderName));
			    		  String folderPath=DEFAULT+"/"+folderName;
			    		  folderResult=FolderUtil.getFolderRef(folderPath,container,true);
			    	 }else{
			    		 Persistable persistable=GenericUtil.getPersistableByOid(parent_wcId);
			    		 if(persistable!=null&&persistable instanceof Folder){
			 	             Folder parent_Folder=(Folder)persistable;
			 	             folderResult=FolderUtil.createSubFolder(folderName, null, parent_Folder, null);
			 	            }
			    	      }
			    	    if(folderResult!=null){
			                  String wc_oid=folderResult.getPersistInfo().getObjectIdentifier().getStringValue();//OID
			                  Debug.P("------Windchill Folder_OID:"+wc_oid);
			                  pmfolder.setPLMId(wc_oid);
			                  pmfolder.setPLMData(getObjectInfo(folderResult));
			                  pmfolder.doUpdate();
			                  Debug.P("----->>>创建同步Windchill文件夹:("+folderName+")成功!");
			    	    }
			    	 }
		    	tx.commit();
		    	tx=null;
		    }catch(Exception e){
		    	e.printStackTrace();
		    	throw new Exception("Windchill创建文件夹("+folderName+")失败!");
		    }finally{
		    	if(tx!=null){
				   tx.rollback();
				}
		    }
	 }
	 
	 /**
	  * 修改文件夹名称
	  * @param objectId 
	  * @param newFolderName 
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
	       	 PMFolder folder=folderPersistence.get(new ObjectId(objectId));//PM閿熶茎纭锋嫹閿熷彨璁规嫹閿熸枻鎷�
	       	 String folderName=folder.getCommonName();
	       	 checkNull(folder);
	       	 String foid=folder.getPLMId();
	       	 String containerName=folder.getContainerName();
	      	 Debug.P("------->>Modify PM Folder:"+folder.getCommonName()+" ;WC_PLMID="+foid+"   ;ContainerName="+containerName);
	      	 try{
	      		 SessionHelper.manager.setAdministrator();
		       	 if(!StringUtils.isEmpty(foid)){
		       		Persistable persistable=GenericUtil.getPersistableByOid(foid);
		       		if(persistable!=null){
		       			if(persistable instanceof Folder){
		       				Folder folderObj=(Folder)persistable;
		       				String folderPath=folderObj.getFolderPath();
		       				String fName=folderObj.getName();
		       				Debug.P("----->>>OldFolderName:"+fName+"  NewFolderName="+folderName);
		       				if(!StringUtils.equals(fName, folderName)){
		       					count=folderService.editFolder(folderPath, folderName, containerName);
			       				if(count>0){
			       					folder.doUpdate();
			       					Debug.P("------>>PM 更新 OldFolderName("+fName+") 成NewFolderName("+folderName+")Success!");
			       				}
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
	  ** 删除文件夹包含文件夹下的对象
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
		        	 PMFolder folder=folderPersistence.get(new ObjectId(objectId));
		        	 checkNull(folder);
		        	 SessionHelper.manager.setAdministrator();
			         String containerName=folder.getContainerName();
			        //Windchill ID
			        String foid=folder.getPLMId();
		        	try{
			        	 if(StringUtils.isNotEmpty(foid)){
			        		Debug.P("------Ready Delete FolderName:"+folder.getCommonName()+"  Windchill FolderId:"+foid);
			          		Persistable persistable=GenericUtil.getPersistableByOid(foid);
			          		if(persistable!=null){
			          			if(persistable instanceof Folder){
			          				Folder folderObj=(Folder)persistable;
			          				String folderPath=folderObj.getFolderPath();
			          				count=folderService.deleteFolder(folderPath, containerName);
			          				if(count>0){
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
	 *检查容器是否存在
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
	 * 閿熸枻鎷烽敓鏂ゆ嫹WTDocument閿熶茎纰夋嫹
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
	    		SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
	        	DocumentPersistence docPersistance=ModelServiceFactory.getInstance(codebasePath).get(DocumentPersistence.class);
	        	PMDocument pm_document=docPersistance.get(new ObjectId(pm_docId));
	        	PMFolder pmfolder=pm_document.getFolder();
	        	String wc_foid=pmfolder.getPLMId();
	        	boolean isContainer=pmfolder.isContainer();
	        	boolean iscreate=pm_document.getPLMId()==null?true:false;
	        	WTDocument doc=checkWTDocumentWrite2PM(pm_docId);
		    	Debug.P("----->>PM 是否存在WCDoc:"+doc);
	        	if(doc!=null){
		    		String plmId=doc.getPersistInfo().getObjectIdentifier().getStringValue();
		    		pm_document.setPLMData(getObjectInfo(doc));
		    		pm_document.setPLMId(plmId);
		    		pm_document.setMajorVid(doc.getVersionIdentifier().getValue());
            		pm_document.setSecondVid(Integer.valueOf(doc.getIterationIdentifier().getValue()));
		    		pm_document.doUpdate();//閿熺潾闈╂嫹
            		Debug.P("----->>>PM WCID:"+plmId+"  ;PM_Document:"+pm_docId);
		    		return 1;
		    	}
	        	String containerName=pmfolder.getContainerName();
	        	Debug.P("----->>>>WC   Folder ID:"+wc_foid+"  是否为PM的容器文件夹:"+isContainer +"  ;ContaienrName:"+containerName);
	        	try{
	        		Persistable persistable=null;
	        		  WTContainer container=null;
	        		  Debug.P("------>>>PM DOC_ID："+pm_docId+"是否新建到Windchill="+iscreate);
	        		  if(iscreate){
	            		  if(!StringUtils.isEmpty(wc_foid)){
		        				persistable=GenericUtil.getPersistableByOid(wc_foid);
		        		  }else{
		        			   container=GenericUtil.getWTContainerByName(containerName);
		        			   persistable=GenericUtil.createNewPath(container);
		        		  }
		        		if(persistable instanceof Folder){
		        			Folder folder=(Folder)persistable;
		       			   boolean isEmpty=StringUtils.isEmpty(pm_document.getPLMId());
		        			if(isEmpty){
		            			Map ibas=new HashMap();
		            			setDocIBAValuesMap(ibas, pm_document);
		            			WTDocument document= DocUtils.createDocument(pm_document, null,VMUSER,ibas,folder);
				        		if(document!=null){
				        			if(isContainer){
			        					GenericUtil.moveObject2Container(document, container,folder);
			        				}else{
			        					FolderUtil.changeFolder(document,folder);
			        				}
				            		String wcId=document.getPersistInfo().getObjectIdentifier().getStringValue();
				            		pm_document.setPLMData(getObjectInfo(document));
				            		pm_document.setPLMId(wcId);
				            		pm_document.setMajorVid(document.getVersionIdentifier().getValue());
				            		pm_document.setSecondVid(Integer.valueOf(document.getIterationIdentifier().getValue()));
				            		WriteResult result=pm_document.doUpdate();
				            		
				            		Debug.P("----->>>PM Return:("+result.getN()+")Create WCID:"+wcId+"  ;PM_Document:"+pm_docId);
				            		count=1;
				        		  }
		            		   }
		        			}
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
	     	//閿熷彨璁规嫹閿熻鍑ゆ嫹閿熺獤鎾呮嫹鍚岄敓鏂ゆ嫹閿熸枻鎷稺indchill
     		boolean isCreated=pm_document.getPLMId()==null?false:true;
     		String doc_id=pm_document.getPLMId();
     		Debug.P("------>>>>Windchill中是否已经创建("+pm_docName+"):"+isCreated+"  Doc_Windchill:"+doc_id);
	     		try{
	     			SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
//	     			SessionHelper.manager.setAdministrator();
	     			if(isCreated){
	         			if(StringUtils.isEmpty(doc_id)) return 0;
	         			 Persistable object=GenericUtil.getPersistableByOid(doc_id);
	         			if(object!=null&&object instanceof WTDocument){
	         				WTDocument doc = (WTDocument)object;
						//	WTDocument doc=(WTDocument)getLastObjectByNum(object);
	         				Map ibas=LWCUtil.getAllAttribute(doc);
	         			    setDocIBAValuesMap(ibas, pm_document);
	         				doc=(WTDocument) GenericUtil.checkout(doc);
	         				doc=DocUtils.updateWTDocument(doc,pm_document, ibas);
	         				if (doc != null) {
	         					if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(doc, wt.session.SessionHelper.manager.getPrincipal()))
	         						doc = (WTDocument) WorkInProgressHelper.service.checkin(doc, "update document Info");
	         				   }
	         			
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
     *  删除文档对象
     * @param pm_docId
     * @return
     * @throws Exception
     */
    public static int deleteWTDocumentEntry(String pm_docId,String deletAction)throws Exception{
    	
    	   Debug.P("------>>>Delete PM_DocumentID："+pm_docId);
       
  	   if (!RemoteMethodServer.ServerFlag) {
           String method = "deleteWTDocumentEntry";
           String klass = PMWebserviceImpl.class.getName();
           Class[] types = { String.class,String.class};
           Object[] vals = {pm_docId,deletAction};
           return (Integer) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
       }else{
    	checkNull(pm_docId);
       	DocumentPersistence docPersistance=ModelServiceFactory.getInstance(codebasePath).get(DocumentPersistence.class);
       	PMDocument pm_document=docPersistance.get(new ObjectId(pm_docId));
       	checkNull(pm_document);
       	String wc_oid=pm_document.getPLMId();
       	Debug.P("------>>>PM("+pm_docId+")<--->Windchill("+wc_oid+")");
       	try {
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
   	    	   throw new Exception("Windchill删除文档对象("+wc_oid+")失败!");
   		   }finally{
   			   SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
   		   }
       }
    	  return 0;
    }
    
    
    /**
     *  同步移动PM文档的路径到Windchill系统中更改
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
    	     PMFolder folder=basic_object.getFolder();
    	     checkNull(folder);
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
		         			if(isContainer){
		         				    Debug.P("----->>>IsContainer:"+isContainer);
		         					WTContainer container=GenericUtil.getWTContainerByName(containerName);
		         					folderObj=GenericUtil.createNewPath(container);
		         					GenericUtil.moveObject2Container(object, container, folderObj);//閿熺嫛璁规嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷风洰褰曢敓鏂ゆ嫹
		         					 Debug.P("----->>>moveObject2Container:"+object);
		         				}else{
		         					if(StringUtils.isEmpty(wc_foid)){throw new Exception("文档("+basic_object.getCommonName()+")目标文件夹("+folder.getCommonName()+")在Windchill系统不存在 ,无法执行移动操作!");}
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
    		    	throw new Exception("Windchill移动对象到("+wc_foid+")中失败!");
    		    }finally{
    		    	SessionHelper.manager.setAuthenticatedPrincipal(VMUSER);
    		    }
       }
	 
 	              return 0;
  }
    
    
    /**
     *  获取对象最新版本
     * @param object
     * @throws Exception
     */
    private static Persistable getLastObjectByNum(Persistable object)throws Exception{
    	if(object instanceof WTPart){
    		WTPart part=(WTPart)object;
    		//object=PartUtils.getPartByNumber(part.getNumber());
    		object=Utils.getWCObject(WTPart.class,part.getNumber());
    		Debug.P("---->>>getLastObjectByNum:WTPartNumber="+part.getNumber());
    	}else if(object instanceof EPMDocument){
    		EPMDocument epm=(EPMDocument)object;
//    		object=EPMUtil.getEPMDocument(epm.getNumber(),null);
    		object=Utils.getWCObject(EPMDocument.class,epm.getNumber());
    		Debug.P("---->>>getLastObjectByNum:EPMDocument="+epm.getNumber());
    	}else if(object instanceof WTDocument){
    		WTDocument doc=(WTDocument)object;
//    		object=DocUtils.getDocByNumber(doc.getNumber());
    		object=Utils.getWCObject(WTDocument.class,doc.getNumber());
    		doc=(WTDocument)Utils.getWCObject(WTDocument.class,doc.getNumber());
    		Debug.P("---->>>getLastObjectByNum:WTDocument="+doc.getNumber()+"---version-->"+doc.getVersionInfo().getIdentifier()+"-----oid----->"+doc.getIterationInfo().getIdentifier().getValue());
    	}
    	  return object;
    }
    
	/**
	 *  映射文档对象对象软属性
	 * @param ibas
	 * @param object
	 * @throws WTException
	 */
	private  static void setDocIBAValuesMap(Map ibas,PMDocument object )throws WTException{
		if(!StringUtils.isEmpty(object.getPhase())){
			 ibas.put(ConstanUtil.PHASE, object.getPhase());
		}
		if(!StringUtils.isEmpty(object.getProductNumber())){
			ibas.put(ConstanUtil.PROJECTNO,object.getProductNumber());
		}
		if(!StringUtils.isEmpty(object.getProjectWorkOrder())){
			ibas.put(ConstanUtil.WORKORDER, object.getProjectWorkOrder());
		}
		if(!StringUtils.isEmpty(object.getProductNumber())){
			  ibas.put(ConstanUtil.PRODUCTNO, object.getProductNumber());
		}
		if(!StringUtils.isEmpty(object.getCreateByUserId())){
			  ibas.put(Contants.PMCREATOR, object.getCreateByUserId());
		}
		if(!StringUtils.isEmpty(object.getModifiedUserId())){
			  ibas.put(Contants.PMMODIFYEDBY, object.getModifiedUserId());
		}
		if(StringUtils.isNotEmpty(object.get_id().toString())){
			ibas.put(ConstanUtil.PMID, object.get_id().toString());
		}
	}
	
	

	/**
	 *获取对象的属性信息
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
				   Map<String,Object> ibas=LWCUtil.getAllAttribute(doc);
				   result.putAll(ibas);
			   }else if(object instanceof Folder){
				   Folder folder=(Folder)object;
				   result.put(ConstanUtil.NAME, folder.getName());
			   }
			     result.put(ConstanUtil.OBJECT_URL, GenericUtil.getObjUrl(object));
		  }
		        return result;
	 }
	
	
	 /**
	  * 閿熸枻鎷锋灇閿熸枻鎷烽敓渚ョ増鏈�A.3)
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
	  *获得对象的版本(A.3)
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
	        	String wc_id=basic_object.getPLMId();
	        	Debug.P("----------->>>Windchill PLMID:"+wc_id);
	        	try {
	        	    SessionHelper.manager.setAdministrator();
	        		if(!StringUtils.isEmpty(wc_id)){
					     Persistable object=GenericUtil.getPersistableByOid(wc_id);
					     if(object!=null){
					     object=getLastObjectByNum(object); 
						 Folder folder = FolderHelper.service.getFolder((FolderEntry) object);
						 Persistable newobject= VersionControlHelper.service.newVersion((Versioned) object);
						 FolderHelper.assignLocation((FolderEntry) newobject,folder);
						 PersistenceHelper.manager.save(newobject);
						 GenericUtil.changeState((LifeCycleManaged) newobject, ConstanUtil.WC_INWORK);
						 PersistenceHelper.manager.refresh(newobject);
						 if(newobject instanceof EPMDocument){
							 EPMDocument empdoc=(EPMDocument)newobject;
							 Debug.P("EPMDocument ---->"+empdoc.getNumber()+"  new version--->"+empdoc.getVersionIdentifier().getValue());
							 basic_object.setPLMId(empdoc.getPersistInfo().getObjectIdentifier().getStringValue());
							 basic_object.setMajorVid(empdoc.getVersionIdentifier().getValue());
							 basic_object.setSecondVid(Integer.valueOf(empdoc.getIterationIdentifier().getValue()));
							 basic_object.setValue("syncdate", Utils.getDate());
						 }else if(newobject instanceof WTPart){
							 WTPart part=(WTPart)newobject;
							 Debug.P("WTPart ---->"+part.getNumber()+"  new version--->"+part.getVersionIdentifier().getValue());
							 basic_object.setPLMId(part.getPersistInfo().getObjectIdentifier().getStringValue());
							 basic_object.setMajorVid(part.getVersionIdentifier().getValue());
							 basic_object.setSecondVid(Integer.valueOf(part.getIterationIdentifier().getValue()));
							 basic_object.setValue("syncdate", Utils.getDate());
						 }else if(newobject instanceof WTDocument){
							 WTDocument doc=(WTDocument)newobject;
							 Debug.P("WTDocument ---->"+doc.getNumber()+"  new version--->"+doc.getVersionIdentifier().getValue());
							 basic_object.setPLMId(doc.getPersistInfo().getObjectIdentifier().getStringValue());
							 basic_object.setMajorVid(doc.getVersionIdentifier().getValue());
							 basic_object.setSecondVid(Integer.valueOf(doc.getIterationIdentifier().getValue()));
							 basic_object.setValue("syncdate", Utils.getDate());
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
			  if(!StringUtils.isEmpty(pm_id)){
				     BasicDocument  basic_object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
			      	 checkNull(basic_object);
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
					  BasicDocument object=ModelServiceFactory.getInstance(codebasePath).getBasicDocumentById(pm_id);
		              String phase=object.getPhase();
		           	  String wc_id=object.getPLMId();
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
						 throw new Exception("Windchill ("+wc_id+") 修改阶段失败!");
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
		    	object=getLastObjectByNum(object);
		    	Debug.P("------>>>EPM>>>>>>>");
		        String url=GenericUtil.getViewContentHrefUrl(object);
		        if(!StringUtils.isEmpty(url)){
		        	result=url.substring(url.indexOf("/"),url.indexOf(","));
		        }
		    }
		    Debug.P("------->>>>>View URL:"+result);
		} catch (Exception e) {
			e.printStackTrace();
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
			 part= (WTPart)searchWCObject(WTPart.class,pmid,Contants.PMID);
			 Debug.P("-searchWCObject-->>WTPart："+part+"   Num:"+part.getNumber());
			 if(part!=null){
				 Debug.P("----Search Part:"+part);
				 part =PartUtils.getPartByNumber(part.getNumber());
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
					 Debug.P("根据部件查询WTDocument文档----->"+class2);
					 pmList=PartUtil.getDescriptDocPMIdBy(part);
					 Debug.P("---GetDOC ByPart:"+pmList);
				 }
				 //根据部件查询EPM文档
				 else if(class2.equalsIgnoreCase("epmdocument")){
					 Debug.P("根据部件查询EPM文档----->"+class2);
					 pmList=PartUtil.getEPMDocPMIDByPart(part);
					 Debug.P("---->>GetEPM ByPart:"+pmList);
				 }
			 }
		 }else if(class1.equalsIgnoreCase("wtdocument")){//根据文档查询该文档的说明部件
			 WTDocument doc = (WTDocument)searchWCObject(WTDocument.class,pmid,Contants.PMID);
			 Debug.P("-searchWCObject-->>WTDoc："+doc);
			 Debug.P("----Search Part:"+doc);
			 if(doc!=null){
				 doc=DocUtils.getDocByNumber(doc.getNumber());
				 if(class2.equalsIgnoreCase("wtpart")){
					 Debug.P("根据WTDocument查询部件----->"+class2);
					 pmList=DocUtils.getDescribePartsByDoc(doc);
					 Debug.P("根据WTDocument查询部件----->Result:"+pmList.size());
				 }if(class2.equalsIgnoreCase("wtdocument")){
					 Debug.P("根据WTDocument查询WTDocument----->"+class2);
					 pmList=DocUtils.getWTDocPMIDByWTDocument(doc);
					 Debug.P("根据WTDocument查询WTDocument----->Result:"+pmList.size());
				 }
			 }
		 }else if(class1.equalsIgnoreCase("epmdocument")){//根据EPM文档查询该EPM文档的说明部件
			 EPMDocument epmdoc = (EPMDocument)searchWCObject(EPMDocument.class,pmid,Contants.PMID);
			 Debug.P("-searchWCObject-->>EPmDoc："+epmdoc);
				 if(epmdoc!=null){
					 epmdoc=EPMUtil.getEPMDocument(epmdoc.getNumber(),null);
					 Debug.P("----->>>EPM:"+epmdoc+"  Version:"+getObjectVersion(epmdoc));
					 if(class2.equalsIgnoreCase("wtpart")){
						 Debug.P("--GetPart ByEPM:"+class2);
						 String epmType=epmdoc.getCADName();
				          if(epmType.endsWith(".drw")){
				        	  epmdoc= (EPMDocument) DocUtils.getEPMReferences(epmdoc).get(0);
				          }
						 pmList=DocUtils.getDescribePartsByEPMDoc(epmdoc);
						 Debug.P("--getPartDescEPM->>>pmList:"+pmList.size());
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
	 * @throws InvocationTargetException 
	 * @throws RemoteException 
	 * @throws InterruptedException 
	  */
	 public static Object searchWCObject(Class class1,String ibavalue,String ibakey)throws  WTException, RemoteException, InvocationTargetException, InterruptedException {
		 Object object = null;
		 boolean flag =SessionServerHelper.manager.setAccessEnforced(false);
         try{
		 QuerySpec qs = new QuerySpec();
		 qs.setAdvancedQueryEnabled(true);
		 int objindex = qs.appendClassList(class1, true);
		 
		 int pmSDIndex = 0;
		 int pmSVIndex = 0;
		 
		 Debug.P("--->>>IBA Key："+ibakey+"  IBA Value:"+ibavalue);
		 if(StringUtils.isNotEmpty(ibavalue)){
			 pmSVIndex = qs.appendClassList(StringValue.class, false);
			 pmSDIndex = qs.appendClassList(StringDefinition.class, false);
			 ClassAttribute caValue = new ClassAttribute(StringValue.class,
						StringValue.VALUE2);
				WhereExpression we = new SearchCondition(
						SQLFunction.newSQLFunction(SQLFunction.LOWER, caValue),
						SearchCondition.EQUAL,
						ConstantExpression.newExpression(ibavalue));
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
				Debug.P("---->>SQL:"+qs);
				Thread.sleep(2000);
				Debug.P("sleep 10s");
				QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
				Debug.P("qr----->"+qr.size());
				if (qr.hasMoreElements()) {
					Object[] objects = (Object[])qr.nextElement();
					Debug.P("---->>SQL:"+objects[0]);
					object=objects[0];
				}
		     }
		 }finally{
					SessionServerHelper.manager.setAccessEnforced(flag);
		 }
		 return object;
	 }
         public static Object doSearch(Class class1,String ibavalue,String ibakey) {
        	 Object obj = null;
     		 String method = "searchWCObject";
	            String klass = PMWebserviceImpl.class.getName();
	            Class[] types = { Class.class,String.class,String.class};
	            Object[] vals = {class1,ibavalue,ibakey};

     		try {
     			obj = (Object)RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
     		} catch (RemoteException e) {
     			e.printStackTrace();
     		} catch (InvocationTargetException e) {
     			e.printStackTrace();
     		}
     		return obj;
     	}
	 public static ObjectInputStream getRepresentationByPM(String pmid) throws WTException, IOException, InvocationTargetException, InterruptedException{
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
//		 RemoteMethodServer  rms = RemoteMethodServer.getDefault();
//		 rms.setUserName("wcadmin");
//		 rms.setPassword("wcadmin");
//		 
//		 String pid="VR:wt.epm.EPMDocument:174627";
//		 String cid="VR:wt.epm.EPMDocument:96452";
//		 String str="Project_NO";
//		List<String> list=queryWCObjectByPM(args[0],args[1],args[2],true);
//		for(String strs:list){
//			Debug.P("strs---------->"+strs);
//		}
		Object obj= doSearch(EPMDocument.class,"54ae5a978a8775297f95150a",Contants.PMID);
		Debug.P(obj);
//		if(obj instanceof WTPart){
//			WTPart part =(WTPart)obj;
//			Debug.P(part.getNumber());
//		}else if(obj instanceof WTDocument){
//			WTDocument doc =(WTDocument)obj;
//			Debug.P(doc.getNumber());
//		}
	}
	 
	 /**
	  * 检查对象是否回写到PM系统
	  * @return
	  */
	 private static WTDocument checkWTDocumentWrite2PM(String pmOid){
		WTDocument doc=null;
	    Map<String,String> ibaValues=new HashMap<String,String>();
	    ibaValues.put(ConstanUtil.PMID, pmOid);
	    List<Persistable> list=LWCUtil.getObjectByIBA(ibaValues, ConstanUtil.WTDOCTYPE);
	    if(list!=null&&list.size()>0){
	    	doc=(WTDocument)list.get(0);
	    }
	      return doc;
	 }
	 
	 
	 
	 
	 
	 
	 

	 
	 
}
