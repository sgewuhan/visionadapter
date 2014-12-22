package ext.tmt.integration.webservice.spm;


import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import com.infoengine.util.UrlEncoder;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.sg.visionadapter.BasicDocument;
import com.sg.visionadapter.DocumentPersistence;
import com.sg.visionadapter.FolderPersistence;
import com.sg.visionadapter.GridFSFileProvider;
import com.sg.visionadapter.IFileProvider;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMDocument;
import com.sg.visionadapter.PMOrganization;

import wt.doc.Document;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.Identified;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.part.QuantityUnit;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.pom.Transaction;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
import wt.vc.wip.WorkInProgressHelper;



import ext.tmt.WC2PM.WCToPMHelper;
import ext.tmt.integration.webservice.pm.ConstanUtil;
import ext.tmt.integration.webservice.pm.PMWebserviceImpl;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;



@SuppressWarnings("deprecation")
public class CsrSpmUtil implements RemoteAccess, Serializable {

    private static final long serialVersionUID = 1L;
    
    private static String CONTENTVAULT_FILE="contentvault_file";

	private static String CODEBASE_=null;
	 static {
			try {
				WTProperties wtproperties= WTProperties.getLocalProperties();
				CODEBASE_= wtproperties.getProperty("wt.codebase.location");
				CODEBASE_=CODEBASE_+File.separator+"visionconf";
				Debug.P("----------->>>codebasePath_:"+CODEBASE_);
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }

/**
 * 
 * @param hashmap 基础属性集合
 * @param ibaMap 软属性集合 
 * @return
 * @throws Exception
 */
    public static String createNewPart(HashMap<String,String> baseMap,Map<String,Object> ibaMap,Map<String,String> configMap)  throws Exception {
        if (baseMap == null||baseMap.size()==0)   throw new WTException("物料基础属性参数集合为空!");
        Debug.P("---createNewPart-----> " + baseMap.toString() + " ibaMaps:-- -> "+ ibaMap);
        Transaction tx = null;
        // 创建部件
        try {
            tx = new Transaction();
            tx.start();
            //获取集合中的基础属性参数
            String strNumber =  baseMap.get(SPMConsts.KEY_NUMBER);
            String strName = baseMap.get(SPMConsts.KEY_NAME);
            String containerName =  baseMap.get(SPMConsts.KEY_CONTAINERNAME);
            String  partType=baseMap.get(SPMConsts.PART_TYPE);
            String partCategory=baseMap.get(SPMConsts.KEY_MATER_CATEGORY);
            Debug.P("---->>Part Category:"+partCategory);
            if(StringUtils.isNotEmpty(partType)&&!partType.contains(SPMConsts.ROOT)){
            	partType = SPMConsts.ROOT + ibaMap.get(SPMConsts.PART_TYPE);
            }
            String unit = (String) ibaMap.get(SPMConsts.KEY_UNIT);//软属性
            ibaMap.remove(SPMConsts.KEY_UNIT);//默认单位从集合中移除
            
            Debug.P("------>ContainerName:"+containerName+" PartType: "+partType+";PartNum:"+strNumber);
            if (StringUtils.isEmpty(containerName)) {
                throw new WTException( "containerName is null or folderName is null");
            }

            if (StringUtils.isEmpty(strName)){
            	 throw new WTException("缺少物料名称，无法创建部件");
            }
            if (StringUtils.isEmpty(strNumber)){
            	  throw new WTException("缺少物料编号，无法创建部件");
            }
              
            //初始化部件实例
            WTPart wtpart = WTPart.newWTPart(strNumber, strName);
            TypeDefinitionReference tdr = TypedUtilityServiceHelper.service .getTypeDefinitionReference(partType);
            if (tdr != null){//设置部件类型
               wtpart.setTypeDefinitionReference(tdr);
            }
            //根据物料类型获得文件夹对象
            Folder folder = getFolderByType(partType,containerName,strNumber,configMap);
            if (folder != null){
            	Debug.P("----->>>Folder  ID:"+folder.getPersistInfo().getObjectIdentifier().getStringValue()+"  ;FolderPath:"+folder.getFolderPath());
                FolderHelper.assignLocation(wtpart, folder);
            }
            //视图
            View view = ViewHelper.service .getView(SPMConsts.DESIGN_VIEW);
            ViewReference viewReference = ViewReference.newViewReference(view);
            if (viewReference != null){
            	wtpart.setView(viewReference);
            }
            //来源
            Source resource = Source.toSource("buy");
            if (resource != null){
               wtpart.setSource(resource);
            }
            // 设置单位，如果没有就用ea
            QuantityUnit qu = null;
            try {
                if (StringUtils.isEmpty(unit)) { unit = "ea";}
                qu = QuantityUnit.toQuantityUnit(unit.toLowerCase());
            } catch (WTInvalidParameterException e) {
                throw new WTException(e.getMessage());
            }
            if (qu != null){
               wtpart.setDefaultUnit(qu);
            }
            
            // 设置IBA属性
            LWCUtil.setValueBeforeStore(wtpart, ibaMap);
            wtpart = (WTPart) PersistenceHelper.manager.save(wtpart);

            Debug.P("------>>>Create WTPart:"+wtpart.getPersistInfo().getObjectIdentifier().getStringValue()+" Part FolderPath:("+wtpart.getFolderPath()+")Success!!!");
            
            wtpart = (WTPart) PersistenceHelper.manager.refresh(wtpart);
            //设置生命周期状态为"已发布"(与株洲所一致??)
            GenericUtil.changeState((LifeCycleManaged) wtpart, SPMConsts.RELEASED);
            tx.commit();
            tx = null;
        } catch(IOException e){
        	e.printStackTrace();
        }catch (Exception e) {
        	e.printStackTrace();
            Debug.P("CsrSpmUtil.createNewPart" + e.getMessage());
            throw new WTException(e.getMessage());
        } finally {
            if (tx != null)
                tx.rollback();
        }
          return "创建成功";
    }

    
    /**
     * 更新部件信息
     * @param baseMap
     * @param ibaMap
     */
    public static void updatePartInfo(WTPart part,Map<String,String> baseMap,Map<String,Object> ibaMap)throws Exception{
    	Debug.P("---updatePartInfo--->>>baseMap:"+baseMap+"  --->ibaMap"+ibaMap);
    	Transaction tx = null;
    	try {
    		  tx = new Transaction();
              tx.start();
              String strName = baseMap.get(SPMConsts.KEY_NAME);
              ibaMap.remove(SPMConsts.KEY_UNIT);//默认单位从集合中移除
              String partName=part.getName();
              if(!StringUtils.equals(partName, strName)){//名称不等则修改
            	  PartUtil.rename(part,strName);
              }
              
              //修改IBA属性
              Debug.P("---->>>IBA Map:"+ibaMap);
              if(ibaMap!=null&&ibaMap.size()>0){
            	  part=(WTPart) GenericUtil.checkout(part);
            	  LWCUtil.setValue(part, ibaMap);
               	if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(part, wt.session.SessionHelper.manager.getPrincipal()))
               		part = (WTPart) WorkInProgressHelper.service.checkin(part, "update WTPart IBA");
                   Debug.P("---->>>IBA Update Success");
              }
              tx.commit();
              tx=null;
              Debug.P("------>>>>更新WTPart("+part.getNumber()+") Success!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}finally{
			if(tx!=null){
				tx.rollback();
			}
		}
    }
    
    
    
    /**
     * 根据物料类型筛选出系统中对应的文件夹对象
     * @param partType 物料类型
     * @param containerName 容器名称
     * @param partCategory 物料小类
     * @param configMap Folder的配置信息
     * @return
     */
    private static Folder getFolderByType(String partType,String containerName,String partNum,Map<String,String> configMap) throws Exception{
          Debug.P("-------------->>>GetFolderInfo<->PartType:"+partType+"  partNum="+partNum);
          String folderRoute=null;
          Folder folder=null;
    	  if(StringUtils.isNotEmpty(partType)){
    		 if(partType.contains(SPMConsts.RAWMATERIAL)){//原材料
    			 folderRoute=getFolderPathByNum(partNum,configMap);
    		 }else if(partType.contains(SPMConsts.SUPPLYMENT)){//客供料
    			 folderRoute=SPMConsts.CUSTOM_MATER_FOLDER;
    		 }else if(partType.contains(SPMConsts.PACKINGPART)){//包装材料
    			 folderRoute=SPMConsts.PACKING_MATER_FOLDER;
    		 }else if(partType.contains(SPMConsts.TOOLPART)){//备品备件
    			 folderRoute=SPMConsts.SPARE_MATER_FOLDER;
    		 }
    	  }
    	try {
    		Debug.P("------>>>FolderRoute:"+folderRoute);
    		if(folderRoute!=null){
    			WTContainer wtcontainer=GenericUtil.getWTContainerByName(containerName);
    			folder=FolderUtil.getFolderRef(folderRoute, wtcontainer, false);
    		}
		} catch (Exception e) {
		   e.printStackTrace();
		   throw new Exception("根据物料类型获取对应的文件夹对象失败,请检查容器("+containerName+")是否Windchill中存在指定的文件夹对象.");
		}
    	   return folder;
    }
    

    
    
    /**
     * 根据编码规则获得文件夹路径
     * @param configMap
     * @return
     */
    private  static String getFolderPathByNum(String partNum,Map<String,String> configMap){
    	String path=null;
    	Debug.P("---->>>Match PartNum:"+partNum);
    	if(configMap!=null&&configMap.size()>0){
 		   for(Iterator<?> ite=configMap.keySet().iterator();ite.hasNext();){
 			   String key=(String) ite.next();
 			   if(key.startsWith(SPMConsts.FOLDER_SPLIT)){
 					String temp_key=key.substring(key.indexOf("_")+1).toUpperCase();
 					 Debug.P("---Key:"+temp_key);
 					 if(partNum.startsWith(temp_key.trim())){
 						  path=configMap.get(key);
 						  break;
 					 }
 			   }
 		   }
 	   }
 	     Debug.P("--->>>getFolderPath:"+path);
 	     return path;
    }
    


    

    
    

    /**
     * 更新部件即为已存在的部件设置IBA属性值
     * 
     * @throws Exception
     * 
     */
    public static String updatePartForIba(String partNo, Map<String,Object> ibaMap,
            String introduct) throws Exception {
        try {
        	 WTPart wtpart=(WTPart) GenericUtil.getObjectByNumber(partNo);
              if(wtpart!=null){
            	  wtpart=(WTPart) GenericUtil.checkout(wtpart);
            	  LWCUtil.setValue(wtpart, ibaMap);
            	  if(wtpart!=null){
            		   	if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(wtpart, wt.session.SessionHelper.manager.getPrincipal()))
            		   		wtpart = (WTPart) WorkInProgressHelper.service.checkin(wtpart, "update WTPart IBA");
                           Debug.P("---->>>IBA Update Success");
                      }
            	  }
        	  return "操作成功";
		} catch (Exception e) {
			 e.printStackTrace();
			 return "更新IBA操作失败";
		}

    }

    
    /**
     * 为部件改变名称
     * @param wtpart 物料对象
     * @param name 名称
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
	public static String changePartName(WTPart wtpart, String name)
            throws Exception {
    	Debug.P("-changePartName-->>wtpart:"+wtpart+"  ;name:"+name);
        if (!RemoteMethodServer.ServerFlag) {
            String method = "changePartName";
            String klass = CsrSpmUtil.class.getName();
            Class[] types = { WTPart.class, String.class };
            Object[] values = { wtpart, name };
            return (String) RemoteMethodServer.getDefault().invoke(method,klass, null, types, values);
        }
        SessionServerHelper.manager.setAccessEnforced(false);
        Transaction tx = null;
        try {
        	tx = new Transaction();
            tx.start();
            if(wtpart!=null&&!StringUtils.equals(wtpart.getName(), name)){//名称不一致才修改
            	Debug.P("---->>>changePartName OldName:"+wtpart.getName()+" ;NewName:"+name);
                Identified identified = (Identified) wtpart.getMaster();
                WTOrganization org = wtpart.getOrganization();
                String number = wtpart.getNumber();
                WTPartHelper.service .changeWTPartMasterIdentity((WTPartMaster) identified,
                                name, number, org);
                tx.commit();
                Debug.P("--->>ChangePartName:("+wtpart.getPersistInfo().getObjectIdentifier().getStringValue()+")名称为("+name+")Success!");
                tx = null;
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            if (tx != null)
                tx.rollback();
            SessionServerHelper.manager.setAccessEnforced(true);
        }
        return "操作成功";
    }
    
    

    /**
     * 创建文档
     * @param baseMap
     * @param ibaMap
     * @return
     * @throws WTException
     */
    public static WTDocument createNewDocument(HashMap<String,String> baseMap,Map<String,Object> ibaMap) throws WTException {
        Debug.P("createNewDocument--> " + baseMap + " ibaMap is -> " + ibaMap);
        Transaction tx = null;
        InputStream pins=null;
        String wtcontainer =  baseMap.get(SPMConsts.KEY_CONTAINERNAME);//容器
        String foldePath = baseMap.get(SPMConsts.KEY_FOLDER);//文件夹路径
        String documentType =baseMap.get(SPMConsts.KEY_DOCTYPE);//文档类型
        String number = baseMap.get(SPMConsts.KEY_NUMBER);
        String name = baseMap.get(SPMConsts.KEY_NAME);
        String fileContent =baseMap.get(SPMConsts.KEY_LOCATION_PATH);//主文档路径
        boolean flagAccess = SessionServerHelper.manager.setAccessEnforced(false);
        try {
        	tx=new Transaction();
            tx.start();
            WTContainer container = GenericUtil.getWTContainerByName(wtcontainer);
            if (StringUtils.isEmpty(documentType)) {
                documentType = SPMConsts.DEFAULT_DOC_TYPE;
            }
            TypeDefinitionReference tdr = TypedUtilityServiceHelper.service.getTypeDefinitionReference(documentType);
            WTDocument doc = WTDocument.newWTDocument();
            if (container != null) {
                doc.setContainer(container);
            }
            if (StringUtils.isNotEmpty(name)){
                doc.setName(name);
            }
            //技术规格书编号参照初始化规则
            if (StringUtils.isNotEmpty(number)){
                 doc.setNumber(number);
            }
            if (tdr != null){
                doc.setTypeDefinitionReference(tdr);
            }
            
            // 设置IBA属性
            if(ibaMap!=null){
            	 LWCUtil.setValueBeforeStore(doc, ibaMap);
            }
            //文档移动到
            Debug.P("---->>>Create Doc FolderPath:"+foldePath);
            Folder folder = FolderUtil.getFolderRef(foldePath, container, false);
            String pmfoid=null;
            if(folder!=null){
               pmfoid=(String) LWCUtil.getValue(folder, SPMConsts.PMID);
               Debug.P("----->>>PM FOID:"+pmfoid);
               FolderHelper.assignLocation(doc, folder);
               
            }
            doc = (WTDocument) PersistenceHelper.manager.save(doc);
            Debug.P("--->>Doc:"+doc.getPersistInfo().getObjectIdentifier().getStringValue());
            Debug.P("---Create Doc Success--->>>Ready Link FileContentURL:"+fileContent);
            //关联主文档对象
            if (fileContent == null || !fileContent.contains("http://")) {
                throw new WTException(name + " " + number + "的文件地址不合法,请检查");
            }
            
           String fileName = fileContent.substring(fileContent.lastIndexOf("/")+1);
            pins=saveUrlAsLocation(fileContent);
            //添加主文档内容URL链接
            doc=DocUtils.linkDocument(doc, fileName, pins, "1", null);
            Debug.P("----->>>>Link Content URL Success!  =====>DocFolderpath:"+doc.getFolderPath());
            //向PM系统创建文档对象
            createDoc2PM(doc);	
            tx.commit();
            tx = null;
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getMessage());
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flagAccess);
            if(pins!=null){
            	try {
					pins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            if (tx != null)
                tx.rollback();
         }
       
    }
    
    
    
    
    
    
    private static void createDoc2PM(WTDocument doc)throws Exception{
    	if(doc!=null){
    		Debug.P("----->>>DOC:"+doc.getName());
    		ModelServiceFactory factory= ModelServiceFactory.getInstance(CODEBASE_);
    		DocumentPersistence docPersistence=factory.get(DocumentPersistence.class);
    		PMDocument  pmdoc=docPersistence.newInstance();
    		String docId=doc.getPersistInfo().getObjectIdentifier().getStringValue();
    		Folder folder=FolderHelper.getFolder(doc);
    		if(folder==null) throw new Exception("Windchill Doc"+docId+" 对应的文件夹为空不存在!");
    		String doc_foid=folder.getPersistInfo().getObjectIdentifier().getStringValue();
    		FolderPersistence folderPersistence=factory.get(FolderPersistence.class);
    		ObjectId foid=folderPersistence.getFolderIdByPLMId(doc_foid);
    		String state=doc.getLifeCycleState().getDisplay();
    		Debug.P("----WCFID:"+doc_foid+" 对应的PMId:"+foid.toString()+"  State:"+state);
    		ObjectId pmdocId=new ObjectId();
    		pmdoc.set_id(pmdocId);
    		pmdoc.setObjectNumber(doc.getNumber());
    		pmdoc.setCommonName(doc.getName());
    		pmdoc.setPLMId(docId);
    		pmdoc.setStatus(state);
    		pmdoc.setOwner(doc.getCreatorName());
    		pmdoc.setPLMData(getObjectInfo(doc));
    		pmdoc.setFolderId(foid);
    		pmdoc.setMajorVid(doc.getVersionIdentifier().getValue());
    		pmdoc.setSecondVid(Integer.valueOf(doc.getIterationIdentifier().getValue()));
    		//上传主内文档
    		try {
    			InputStream ins=DocUtils.doc2is(doc);
    			if(ins!=null){
    				String fileName=DocUtils.getPrimaryFileNameByDoc(doc);
    				pmdoc.setContentVault(new ObjectId(), CONTENTVAULT_FILE, fileName, factory.getDB("pm2"), ins);
    			}
    			 ins.close();
    		     pmdoc.doInsert();
                //加载文档权限
    			WCToPMHelper.reloadPermission(pmdocId.toString());
        		//回写PMID到Windchill
        		IBAUtils iba=new IBAUtils(doc);
        		iba.setIBAValue(SPMConsts.PMID, pmdocId==null?"":pmdocId.toString());
        		iba.updateIBAPart(doc);
        		Debug.P("--createDoc2PM--->>>Set PM ID2 Winchill:("+pmdocId+")Success!!!");
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception("回写PM文档信息失败!");
			}catch(Exception e){
				e.printStackTrace();
				throw new Exception(e.getMessage());
			}
    	} 
    }
    
    

    /**
     * 修订文档
     * @param doc
     * @param hashmap
     * @param properties
     * @param spm
     * @return
     * @throws WTException
     */
    public static WTDocument reviseDocument(WTDocument doc, HashMap<String,String> baseMap,
            Map<String,Object>  ibaMap) throws WTException {
        Debug.P("---reviseDocument-->>para hashmap is " + baseMap + " ibaMap is -> "+ ibaMap);
        Transaction tx=null;
        InputStream pins=null;
        String fileContent = baseMap.get(SPMConsts.KEY_LOCATION_PATH);
        String docName=baseMap.get(SPMConsts.KEY_NAME);
        boolean flagAccess = SessionServerHelper.manager .setAccessEnforced(false);
        try {
        	tx = new Transaction();
            tx.start();
            // 升大版本
            doc = (WTDocument) VersionControlHelper.service.newVersion(doc);
            // 设置IBA属性
            if(ibaMap!=null){
                LWCUtil.setValueBeforeStore(doc, ibaMap);
            }
            //是否更改文档的名称
        	if (!doc.getName().equals(docName)) {//如果名称不一致则改名称
				DocUtils.rename(doc, docName);
			}
            
            doc = (WTDocument) PersistenceHelper.manager.save(doc);

            // 更新主内容
            String fileName = fileContent.substring(fileContent .lastIndexOf("/"));
            DocUtils.clearAllContent(doc);//清除原有的历史信息
            //添加主文档内容
            pins=saveUrlAsLocation(fileContent);
            doc=DocUtils.linkDocument(doc, fileName, pins, "1", null);
            Debug.P("----->>>>Update Content URL Success!");
            tx.commit();
            tx = null;
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e.getMessage());
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flagAccess);
            if(pins!=null){
            	try {
					pins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            if (tx != null){
            	 tx.rollback();
            }
               
        }
    }
    
    /**
     * 通过Url读取文件流对象
     * ( 此方法只能用于HTTP协议)
     * @param fileUrl
     * @param fileName 本地文件路径
     * @return
     */
    private static  InputStream  saveUrlAsLocation(String fileUrl) {
    	DataInputStream ins = null;
    	try {
            String fileURL = dealURL(fileUrl);
            int lastIndex = fileURL.lastIndexOf("/");
            String fileURL_sub1 = fileURL.substring(0, lastIndex+1);
            String fileURL_sub2 = fileURL.substring(lastIndex+1, fileURL.length());
            fileURL_sub2 = UrlEncoder.encode(fileURL_sub2, "UTF-8");
            fileURL_sub2 = fileURL_sub2.replace("%25", "%");
//            fileUrl = UrlEncoder.decode(fileUrl);
            fileURL = fileURL_sub1+fileURL_sub2;
            Debug.P("解析前fileUrl------------>"+fileUrl);
            Debug.P("解析后fileURL------------>"+fileURL);
            URL url = new URL(fileURL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
             ins = new DataInputStream(connection .getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    	  return ins;
    }

     private  static String dealURL(String url) {
         String fileURL = url.replace(" ", "%20");
         return fileURL;
     }
    

     /**
      * 更新关联文档
      * 
      * @param part
      * @param tzNum
      * @throws Exception
      * @throws WTPropertyVetoException
      */
     @SuppressWarnings("unchecked")
     public static void updateDescribedDocument(WTPart part, String tzNum)throws WTPropertyVetoException, Exception {
        
     	Debug.P("-------->>updateDescribedDocument:"+part+"  ;tzNum(Doc):"+tzNum);
         if(part!=null){
         	 part=(WTPart) GenericUtil.checkout(part);
         	 //获得部件的描述文档
              QueryResult qrOldWTDocs = WTPartHelper.service .getDescribedByDocuments(part);
              Vector<Document> oldwtdocs = qrOldWTDocs.getObjectVectorIfc().getVector();
              if(StringUtils.isNotEmpty(tzNum)){
             	 WTDocument tzDoc=(WTDocument) GenericUtil.getObjectByNumber(tzNum);
             	// 删除旧关联
                  for(int i=0;i<oldwtdocs.size();i++){
                 	 Document doc = oldwtdocs.get(i);
                 	  if (doc instanceof WTDocument) {
                 		  WTDocument descDoc=(WTDocument)doc;
                 		  if(StringUtils.equals(tzNum, descDoc.getNumber())){//存在历史关系(保证编号唯一才可行)
                     		  QueryResult res = PersistenceHelper.manager.find(WTPartDescribeLink.class, part, "describes", (WTDocument) doc);
                               while (res.hasMoreElements()) {
                                   WTPartDescribeLink rule = (WTPartDescribeLink) res .nextElement();
                                   PersistenceHelper.manager.delete(rule);
                               }
                 		  }
                 	  }
                 }
                   //新建关联关系
                  PartUtil.createDescriptionLink(part, tzDoc);
              	if (tzDoc != null) {
  					if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(tzDoc, wt.session.SessionHelper.manager.getPrincipal()))
  						tzDoc = (WTDocument) WorkInProgressHelper.service.checkin(tzDoc, "update document Info");
  				   }
              }
         }
     }

     
     
     /**
      * 获得PM的扩建工厂
      * @return
      * @throws Exception
      */
     public static List<String> getAllPMFactory() throws Exception{
    	   ModelServiceFactory factory= ModelServiceFactory.getInstance(CODEBASE_);
    	   PMOrganization org=factory.get(PMOrganization.class);
    	   return  org.getAllFactoryList();
     }
     
     
     
     
     
//
//    /**
//     * 更新文档主内容并升版
//     * 
//     * @param doc
//     * @param location
//     * @return
//     * @throws FileNotFoundException
//     * @throws WTException
//     * @throws PropertyVetoException
//     * @throws IOException
//     */
//    public static WTDocument updatePrimaryForDoc(WTDocument doc, String location)
//            throws FileNotFoundException, WTException, PropertyVetoException,
//            IOException {
//        if (doc == null || location == null || location.equals(""))
//            throw new WTException("附件获取失败");
//        String fileName = location.substring(location.lastIndexOf("/"));
//        String filePath = WtUtil.getTempFilePath();
//        Debug.P("tempFolder is " + filePath);
//        boolean flag = saveUrlAs(location, filePath + fileName);
//        Debug.P("saveUrlAs flag is -> " + flag);
//
//        if (location != null && !location.equals("") && flag) {
//            boolean flagAccess = SessionServerHelper.manager
//                    .setAccessEnforced(false);
//            WTDocument newDoc = WtUtil.checkoutWTDocument(doc, "");
//            WtUtil.updateFiletoContent(newDoc, filePath + fileName, true);
//            newDoc = WtUtil.checkinWTDocument(newDoc, "");
//            WtUtil.deleteFile(filePath);
//            SessionServerHelper.manager.setAccessEnforced(flagAccess);
//            return newDoc;
//        } else {
//            throw new WTException("附件获取失败");
//        }
//
//    }
//
//    /**
//     * 得到容器
//     * 
//     * @param containerName
//     * @return
//     * @throws QueryException
//     * @throws WTException
//     */
//    public static WTContainer getContainer(String containerName)
//            throws QueryException, WTException {
//        WTContainer container = null;
//        QuerySpec qs = new QuerySpec(PDMLinkProduct.class);
//
//        SearchCondition sc = new SearchCondition(PDMLinkProduct.class,
//                PDMLinkProduct.NAME, SearchCondition.EQUAL, containerName);
//        qs.appendWhere(sc);
//
//        QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
//
//        if (qr.hasMoreElements()) {
//
//            container = (WTContainer) qr.nextElement();
//        } else {
//            qs = new QuerySpec(WTLibrary.class);
//
//            sc = new SearchCondition(WTLibrary.class, WTLibrary.NAME,
//
//            SearchCondition.EQUAL, containerName);
//
//            qs.appendWhere(sc, new int[] { 0 });
//
//            qr = PersistenceHelper.manager.find((StatementSpec) qs);
//
//            if (qr.hasMoreElements()) {
//
//                container = (WTContainer) qr.nextElement();
//            }
//        }
//        return container;
//    }
//
//    /**
//     * 得到folder
//     * 
//     * @param containerRef
//     * @param folder
//     * @return
//     * @throws WTException
//     */
//    public static Folder getFolder(WTContainerRef containerRef, String folder)
//            throws WTException {
//        return FolderHelper.service.getFolder(folder, containerRef);
//    }
//
//    /**
//     * 创建部件和文档的关系
//     * 
//     * @return false
//     * @throws WTException
//     */
//    public static String createPartDocLink(String partNo, WTDocument doc) {
//        WTPart part = null;
//        try {
//            QueryResult qrp = WtUtil.getPartByNumber(partNo, false);
//            if (qrp.hasMoreElements()) {
//                part = (WTPart) qrp.nextElement();
//            }
//            setPartDescribe(part, doc);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return e.getMessage();
//        }
//        return "操作成功";
//    }
//
//    /**
//     * 为part和document创建关联
//     * 
//     * @param prt
//     * @param doc
//     */
//    public static void setPartDescribe(wt.part.WTPart prt, WTDocument doc) {
//        boolean access = SessionServerHelper.manager.setAccessEnforced(false);
//        try {
//            WTPartDescribeLink linkObj = WTPartDescribeLink
//                    .newWTPartDescribeLink(prt, doc);
//            PersistenceServerHelper.manager.insert(linkObj);
//        } catch (wt.util.WTException wte) {
//            wte.printStackTrace();
//        } finally {
//            SessionServerHelper.manager.setAccessEnforced(access);
//        }
//    }
//
//    /**
//     * 取得文档的所有软类型
//     * 
//     * @return QueryResult
//     */
//    public static QueryResult getDocumentTypes(Object object) {
//        try {
//            QuerySpec queryspec = new QuerySpec();
//            int a = queryspec.appendClassList(WTTypeDefinitionMaster.class,
//                    true);
//            int b = queryspec.appendClassList(WTTypeDefinition.class, true);
//            String[] alias = new String[2];
//            alias[0] = queryspec.getFromClause().getAliasAt(a);
//            alias[1] = queryspec.getFromClause().getAliasAt(b);
//            ClassAttribute INTHD = new ClassAttribute(
//                    WTTypeDefinitionMaster.class, "intHid");
//            ClassAttribute DELETED_ID = new ClassAttribute(
//                    WTTypeDefinitionMaster.class, "deleted_id");
//            TableColumn LATEST = new TableColumn(alias[1],
//                    "LATESTITERATIONINFO");
//            TableColumn MASTER_ID = new TableColumn(alias[1],
//                    "IDA3MASTERREFERENCE");
//            TableColumn IDA2A2 = new TableColumn(alias[0], "IDA2A2");
//            queryspec.appendWhere(new SearchCondition(INTHD,
//                    SearchCondition.LIKE, new ConstantExpression(object)),
//                    new int[] { 0, 1 });
//            queryspec.appendAnd();
//            queryspec.appendWhere(new SearchCondition(DELETED_ID,
//                    SearchCondition.IS_NULL), new int[] { 0, 1 });
//            queryspec.appendAnd();
//            queryspec.appendWhere(new SearchCondition(LATEST, "=",
//                    new ConstantExpression(1)), new int[] { 0, 1 });
//            queryspec.appendAnd();
//            queryspec.appendWhere(new SearchCondition(IDA2A2, "=", MASTER_ID),
//                    new int[] { 0, 1 });
//            return PersistenceHelper.manager.find((StatementSpec) queryspec);
//        } catch (QueryException e) {
//            e.printStackTrace();
//        } catch (WTException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 将HashMap里面为null的值转换为空字符串
//     * 
//     * @param map
//     * @return
//     */
//    @SuppressWarnings( { "rawtypes", "unchecked" })
//    public static HashMap nullConvertEmptyForHashMap(HashMap map) {
//        HashMap convertedMap = null;
//        if (map != null) {
//            Set mapset = map.entrySet();
//            String EMPTYSTR = "";
//            convertedMap = new HashMap();
//            for (Iterator it = mapset.iterator(); it.hasNext();) {
//                Entry entry = (Entry) it.next();
//                if (entry.getValue() == null) {
//                    convertedMap.put(entry.getKey(), EMPTYSTR);
//                } else {
//                    convertedMap.put(entry.getKey(), entry.getValue());
//                }
//            }
//        }
//        return convertedMap;
//
//    }
//
    
    

    
    
//  
//  /**
//   * 创建资源
//   * 
//   * @param hashmap
//   * @param properties
//   * @return
//   * @throws Exception
//   * @author qiaokai
//   */
//  @SuppressWarnings( {"rawtypes"})
//  public static String createNewResource(HashMap hashmap,
//          Properties properties) throws Exception {
//      Debug.P("para hashmap is -> " + hashmap + " properties is -> "
//              + properties);
//      // Folder folder = null;
//      if (hashmap == null)
//          throw new WTException("参数不存在！");
//      WTContainerRef wtContainerRef = null;
//      Transaction tx = null;
//      boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
//
//      // 创建资源
//      try {
//
//          tx = new Transaction();
//          tx.start();
//          String strNumber = (String) hashmap.get("number");
//          String strName = (String) hashmap.get("name");
//          String partType = (String) hashmap.get("partType");
//          String containerName = (String) hashmap.get("containerName");
//          String folderName = (String) hashmap.get("folderName");
//          String unit = (String) hashmap.get("unit");
//          if (containerName == null || containerName.equals("")
//                  || folderName == null || folderName.equals("")) {
//              throw new WTException(
//                      "containerName is null or folderName is null");
//          } else {
//              WTContainer container = getContainer(containerName);
//              wtContainerRef = WTContainerRef.newWTContainerRef(container);
//              // folder = getFolder(wtContainerRef, folderName);
//          }
//          Debug.P("para partType is -> "
//                  + ((String) hashmap.get("partType")).equals(""));
//          if ((String) hashmap.get("partType") == null
//                  || ((String) hashmap.get("partType")).equals("")) {
//              partType = "com.ptc.windchill.mpml.resource.MPMTooling";
//          } else {
//              partType = SPMConsts.ROOTRESOURCE + partType;
//          }
//          Debug.P("partType is -> " + partType);
//          TypeDefinitionReference tdr = TypedUtilityServiceHelper.service
//                  .getTypeDefinitionReference(partType);
//          View view = ViewHelper.service .getView(SPMConsts.MANUFACTURE_VIEW);
//          ViewReference viewReference = ViewReference.newViewReference(view);
//          Source resource = Source.toSource("buy");
//          // 设置单位，如果没有就用ea
//          QuantityUnit qu = null;
//          try {
//              if (StringUtils.isEmpty(unit)) {
//                  unit = "ea";
//              }
//              qu = QuantityUnit.toQuantityUnit(unit.toLowerCase());
//          } catch (WTInvalidParameterException e) {
//              // qu = QuantityUnit.toQuantityUnit("ea");
//              throw new WTException(e.getMessage());
//          }
//          MPMTooling wtpart = null;
//          if (strName == null || strName.equals(""))
//              throw new WTException("缺少名称，无法创建资源");
//          if (strNumber == null || strNumber.equals(""))
//              throw new WTException("缺少编号，无法创建资源");
//          wtpart = MPMTooling.newMPMTooling(strNumber, strName);
//          if (tdr != null)
//              wtpart.setTypeDefinitionReference(tdr);
//          Folder folder = (Folder) InitRuleHelper.evaluator.getValue(
//                  "folder.id", wtpart, wtContainerRef);
//          if (folder != null)
//              FolderHelper.assignLocation(wtpart, folder);
//          if (viewReference != null)
//              wtpart.setView(viewReference);
//          if (resource != null)
//              wtpart.setSource(resource);
//          if (qu != null)
//              wtpart.setDefaultUnit(qu);
//
//          String imgPath = "";
//          String tzNum = "";
//          // 设置IBA属性
//          CSRIBAUtils iba = new CSRIBAUtils((IBAHolder) wtpart);
//          Iterator keySet = properties.keySet().iterator();
//          while (keySet.hasNext()) {
//              String name = (String) keySet.next();
//              Debug.P("name is -> " + name);
//              String value = properties.getProperty(name);
//              Debug.P("name is -> " + value);
//
//              if (StringUtils.equals(gongjutupian, name)) {
//                  imgPath = value;
//              } else if (StringUtils.equals(tuzhibianhao, name)) {
//                  tzNum = value;
//              } else {
//                  iba.setIBAValue(name, value);
//              }
//          }
//
//          // 部件必填属性添加
//          iba.setIBAValue("CSR_SHOUYONGCHANPINDAIHAO", "");
//          iba.setIBAValue("CSR_TUZHILEIXING", "");
//          iba.updateIBAPart(wtpart);
//
//          wtpart = (MPMTooling) PersistenceHelper.manager.save(wtpart);
//          wtpart = (MPMTooling) PersistenceHelper.manager.refresh(wtpart);
//          // setState((LifeCycleManaged) wtpart, LifecycleState.RELEASED);
//
//          // 工具类资源新建表示法
//          if (StringUtils.isNotEmpty(imgPath)) {
//              createRepresentation(wtpart, imgPath);
//          }
//          // 工装类资源关联图纸
//          else if (StringUtils.isNotEmpty(tzNum)) {
//              QueryResult qr = WtUtil.getDocumentByNumber(tzNum);
//              while (qr.hasMoreElements()) {
//                  WTDocument doc = (WTDocument) qr.nextElement();
//                  setPartDescribe(wtpart, doc);
//              }
//          }
//
//          tx.commit();
//          tx = null;
//
//      } catch (Exception e) {
//          Debug.P("CsrSpmUtil.createNewResource" + e.getMessage());
//          throw new WTException(e.getMessage());
//      } finally {
//          SessionServerHelper.manager.setAccessEnforced(flag);
//          if (tx != null)
//              tx.rollback();
//      }
//      return "创建成功";
//  }

    
    
    
//
//    /**
//     * 为对象设置生命周期状态
//     * 
//     * @param lifecyclemanaged
//     * @param s
//     * @throws WTInvalidParameterException
//     * @throws WTPropertyVetoException
//     * @throws WTException
//     */
//    public static void setState(LifeCycleManaged lifecyclemanaged, String state)
//            throws WTInvalidParameterException, WTPropertyVetoException,
//            WTException {
//        LifeCycleHelper.service.setLifeCycleState(lifecyclemanaged, State
//                .toState(state));
//    }
//
//    /**
//     * 设置关联文档的状态
//     * 
//     * @param wtobject
//     * @param state
//     *            (RELEASED)
//     * @throws WTException
//     */
//    public static void setDocState(WTPart part, String tarState)
//            throws WTException {
//        QueryResult tuYangQR = WTPartHelper.service
//                .getDescribedByWTDocuments(part);
//        if (tuYangQR.size() > 0) {
//            while (tuYangQR.hasMoreElements()) {
//                WTDocument doc = (WTDocument) tuYangQR.nextElement();
//                LifeCycleHelper.service.setLifeCycleState(
//                        (LifeCycleManaged) doc, State.toState(tarState));
//            }
//        }
//    }
//
//    /**
//     * 生成技术规格书名称
//     * 
//     * @param num
//     * @return
//     */
//    public static String genDocName(String num) {
//        String docName = "";
//        String partName = "";
//        String docType = "";
//        String partNumber = num.substring(2);
//        String type = num.substring(0, 1);
//        WTPart part = PartUtils.getWTPart(partNumber);
//        partName = part.getName();
//        // 获得文档类型
//        if (StringUtils.equals(type, "C")) {
//            docType = "产品手册";
//        } else if (StringUtils.equals(type, "J")) {
//            docType = "技术规格书";
//        } else if (StringUtils.equals(type, "Q")) {
//            docType = "其它文档";
//        } else if (StringUtils.equals(type, "R")) {
//            docType = "认证报告";
//        } else if (StringUtils.equals(type, "N")) {
//            docType = "文档";
//        }
//        docName = partName + docType;
//        String exp = "/\\:*\"<>|";
//        for (int i = 0; i < exp.length(); i++) {
//            char ca = exp.charAt(i);
//            docName = docName.replace(ca, "_".charAt(0));
//        }
//        return docName;
//    }
//
//    /**
//     * 判断集合中的部件的“审核人编号”属性是否一致
//     * 
//     * @param vect
//     * @return
//     */
//    public static boolean isSameChecker(Vector<WTPart> vect) {
//        boolean flag = true;
//        try {
//            String temp = "";
//            for (int i = 0; i < vect.size(); i++) {
//                WTPart part = vect.get(i);
//                String shrbh = WorkflowChangeHelper.getStringValue(part,
//                        "CSR_SHENHERENBIANHAO");
//                shrbh = shrbh == null ? "" : shrbh;
//                if (i == 0) {
//                    temp = shrbh;
//                } else if (!temp.equals(shrbh) || temp.equals("")) {
//                    flag = false;
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return flag;
//    }
//    
//    /**
//     * 判断技术规格书是否已关联下发单
//     * @param vectPart
//     * @param number
//     * @return
//     */
//    public static String isHaveXfd(Vector<WTPart> vectPart, String number){
//        StringBuffer buffer = new StringBuffer("");
//        try{
//            String docType = CSRUtils.getProperty("csr.jsggsxfd.doctype");
//            for(int j=0; j<vectPart.size(); j++){
//                //获取与part关联的技术规格书参考文档
//                Vector<WTDocument> vect = TechDocCommand.getJSGGSFromPart(vectPart.get(j));
//                for(int i=0; i< vect.size();i++){
//                    WTDocument doc = vect.get(i);
//                    //文档的所参考文档
//                    QueryResult docs = WTDocumentHelper.service
//                            .getHasDependentWTDocuments(doc);
//                    while (docs.hasMoreElements()) {
//                        WTDocument refDoc = (WTDocument) docs.nextElement();
//                        String softTypeName = CSRUtils.getExternalTypeIdentifier(refDoc);
//                        //技术规格书下发单
//                        if(softTypeName.indexOf(docType)>-1){
//                            if(StringUtils.equals(number, refDoc.getNumber())){
//                                continue;
//                            }
//                            //判断状态-已接收或废弃
//                            String state = refDoc.getLifeCycleState().getDisplay();
//                            if(!StringUtils.equals(state, "已接收") && 
//                                    !StringUtils.equals(state, "废弃")){
//                                buffer.append("技术规格书："+doc.getNumber()+"\n") ;
//                                break;
//                            }
//                        }
//                    }
//                }
//            }
//            
//            if(buffer.length()>0){
//                buffer.append("已关联技术规格书下发单，请检查");
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        
//        return buffer.toString();
//    }
//
//    /**
//     * 更新表示法
//     * 
//     * @param partNumber
//     * @param imgPath
//     * @throws Exception
//     */
//    public static void updateRepresentation(String partNumber, String imgPath)
//            throws Exception {
//        // 获取表示法
//        WTPart part = null;
//        QueryResult qr2 = WtUtil.getPartByNumber(partNumber, false);
//        if (qr2.hasMoreElements()) {
//            part = (WTPart) qr2.nextElement();
//        }
//        Representation representation = RepresentationHelper.service
//                .getDefaultRepresentation(part);
//        if (representation != null) {
//            if (representation instanceof DerivedImage) {
//                DerivedImage derivedImage = (DerivedImage) representation;
//                // 删除表示法
//                wt.viewmarkup.ViewMarkUpHelper.service
//                        .deleteDerivedImage(derivedImage);
//            }
//        }
//
//        // 新建表示法
//        createRepresentation(part, imgPath);
//    }
//
//    /**
//     * 新建表示法
//     * 
//     * @param part
//     * @param imgPath
//     * @throws Exception
//     * @throws WTException
//     */
//    public static void createRepresentation(WTPart part, String imgPath)
//            throws Exception, WTException {
//        String fileName = imgPath.substring(imgPath.lastIndexOf("/"));
//        String filePath = WtUtil.getTempFilePath();
//        fileName = part.getName()
//                + fileName.substring(fileName.lastIndexOf("."));
//        String exp = "/\\:*\"<>|";
//        for (int i = 0; i < exp.length(); i++) {
//            char ca = exp.charAt(i);
//            fileName = fileName.replace(ca, "_".charAt(0));
//        }
//        boolean isDone = CsrSpmUtil.saveUrlAs(imgPath, filePath + fileName);
//        if (isDone) {
//            WVSUtils.publish(part, filePath + fileName);
//        }
//    }
//
    
    
//
//    public static void main(String[] args) throws RemoteException, WTException {
//        // 测试修改部件名称
//        // HashMap nameMap = new HashMap();
//        // nameMap.put("name", "换个名字");
//        // changePartName("RUANJIAN",nameMap);
//        // 测试创建部件
//        // HashMap hashmap = new HashMap();
//        // hashmap.put("name", "物料");
//        // hashmap.put("number", "W0001");
//        // hashmap.put("containerName", "株电所");
//        // hashmap.put("folderName", "/Default");
//        // hashmap.put("partType", "wt.part.WTPart|com.ptc.www.WULIAO");
//        // Properties properties = new Properties();
//        // properties.setProperty("物料编号", "K1029");
//        // properties.setProperty("物料描述", "");
//        // Class[] cls = { HashMap.class, properties.getClass() };
//        // Object[] objs = { hashmap, properties };
//        // RemoteMethodServer rms = RemoteMethodServer.getDefault(); // 注册远程服务
//        // try {
//        // rms.invoke("createNewPart", "ext.csr.spm.CsrSpmUtil", null,
//        // cls, objs);// 注册服务器端的方法
//        // } catch (RemoteException e) { //
//        // e.printStackTrace();
//        // } catch (InvocationTargetException e) {
//        // e.printStackTrace();
//        // }
//        // 测试更新部件
//        // Properties properties = new Properties();
//        // properties.setProperty("JILIANGDANWEI", "cm");
//        // Class[] cls = { String.class, properties.getClass() };
//        // Object[] objs = { "00001", properties };
//        // RemoteMethodServer rms = RemoteMethodServer.getDefault(); // 注册远程服务
//        // try {
//        // rms.invoke("updatePartForIba", "ext.csr.spm.CsrSpmUtil", null,
//        // cls, objs);// 注册服务器端的方法
//        // } catch (RemoteException e) {
//        // e.printStackTrace();
//        // } catch (InvocationTargetException e) {
//        // e.printStackTrace();
//        // }
//        // 测试创建文档
//        // HashMap hashmap = new HashMap();
//        // hashmap.put("name", "九阴真经");
//        // hashmap.put("number", "F0001");
//        // hashmap.put("containerName", "株电所");
//        // hashmap.put("folderName", "/Default");
//        // hashmap.put("documentType", "wt.doc.WTDocument|com.ptc.www.SpmDoc");
//        // Properties properties = new Properties();
//        // properties.setProperty("CSRBGD", "u1234");
//        // properties.setProperty("CSRSHD", "U2468");
//        // Class[] cls = { HashMap.class, properties.getClass() };
//        // Object[] objs = { hashmap, properties };
//        // RemoteMethodServer rms = RemoteMethodServer.getDefault(); // 注册远程服务
//        // try {
//        // rms.invoke("createDocument", "ext.csr.spm.CsrSpmUtil", null,
//        // cls, objs);// 注册服务器端的方法
//        // } catch (RemoteException e) {
//        // e.printStackTrace();
//        // } catch (InvocationTargetException e) {
//        // e.printStackTrace();
//        // }
//
//        // 测试为部件和文档创建关联
//        // Class[] cls = { String.class, String.class };
//        // Object[] objs = { "W0001", "F0001" };
//        // RemoteMethodServer rms = RemoteMethodServer.getDefault(); // 注册远程服务
//        // try {
//        // rms.invoke("createPartDocLink", "ext.csr.spm.CsrSpmUtil", null,
//        // cls, objs);// 注册服务器端的方法
//        // } catch (RemoteException e) {
//        // e.printStackTrace();
//        // } catch (InvocationTargetException e) {
//        // e.printStackTrace();
//        // }

        // 测试更新部件分类属性
        // HashMap ibaMap = new HashMap();
        // ibaMap.put("CSR_BAOHOU", "80");
        // Class[] cls = { String.class, String.class, HashMap.class };
        // Object[] objs = { "00001","wt.part.WTPart|分类二/分类二一",ibaMap };
        // RemoteMethodServer rms = RemoteMethodServer.getDefault(); // 注册远程服务
        // try {
        // rms.invoke("updatePartClassificationIba", "ext.csr.spm.CsrSpmUtil",
        // null,
        // cls, objs);// 注册服务器端的方法
        // } catch (RemoteException e) {
        // e.printStackTrace();
        // } catch (InvocationTargetException e) {
        // e.printStackTrace();
        // }
//    }
     

     
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
// 				   result.put(ConstanUtil.STATE, epm.getLifeCycleState().getDisplay());
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
//                    result.put(ConstanUtil.STATE, doc.getLifeCycleState().getDisplay());
 				   result.put(ConstanUtil.CREATOR, doc.getCreator().getName());
 				   result.put(ConstanUtil.MODIFIER, doc.getModifier().getName());
// 			   result.put(ConstanUtil.CREATEDATE, doc.getCreateTimestamp().toString());
// 			   result.put(ConstanUtil.MODIFYDATE, doc.getModifyTimestamp().toString());
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
 	  * 更新物料信息
 	  * @param part
 	  * @param ibaMap
 	  */
     public static void updateWTPartIBA(WTPart part,Map<String ,Object> ibaMap)throws Exception{
    	 Debug.P("---->>>IBA Map:"+ibaMap); 
    	 if(ibaMap!=null){
    		 IBAUtils iba=new IBAUtils(part);
    		 for( Iterator<?> ite=ibaMap.keySet().iterator();ite.hasNext();){
    			   String key=(String) ite.next();
    			   String value=(String) ibaMap.get(key);
    			   Debug.P("--->>>Key:"+key+"   Value:"+value);
    			   iba.setIBAValue(key, value);
    		 }
    		    iba.updateIBAPart(part);
    	   }
     }
 	 
    
 	 public static String  testPM(String num) throws Exception{
 		 
 		 if(!RemoteMethodServer.ServerFlag){
 			   String method = "testPM";
	           String klass = CsrSpmUtil.class.getName();
	           Class[] types = { String.class};
	           Object[] vals = {num};
	           return (String)RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
 		 }else{
 			WTDocument doc=DocUtils.getDocByNumber(num);
 			Debug.P("---->>>>docNum:"+doc.getNumber());
 			createDoc2PM(doc);
 			System.out.println("---Success!!");
 			return "111111";
 		 }
 	 }
 	 

 	 
	 public static void main(String[] args) throws Exception {
		 String num="0000087145";
		 testPM(num);
	}
	 

}
