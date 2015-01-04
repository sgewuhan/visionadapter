/* bcwti
 *
 * Copyright (c) 2011 Parametric Technology Corporation (PTC). All Rights
 * Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package ext.tmt.utils;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.content.StandardContentService;
import wt.doc.DocumentType;
import wt.doc.WTDocument;
import wt.epm.EPMApplicationType;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMContextHelper;
import wt.epm.EPMDocSubType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.epm.EPMDocumentType;
import wt.epm.structure.EPMMemberLink;
import wt.epm.structure.EPMStructureHelper;
import wt.epm.workspaces.EPMWorkspace;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.collections.WTKeyedHashMap;
import wt.fc.collections.WTKeyedMap;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.FolderNotFoundException;
import wt.iba.definition.StringDefinition;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.StringValue;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleServerHelper;
import wt.lifecycle.State;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.ownership.OwnershipHelper;
import wt.pom.Transaction;
import wt.query.KeywordExpression;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.representation.PublishedContentLink;
import wt.session.SessionMgr;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.baseline.ManagedBaseline;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.WorkInProgressHelper;

import com.ptc.core.logging.Log;
import com.ptc.core.logging.LogFactory;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.windchill.enterprise.note.commands.NoteServiceCommand;
import com.ptc.wvs.client.beans.PublishConfigSpec;
import com.ptc.wvs.common.ui.Publisher;
import com.ptc.core.foundation.type.server.impl.TypeHelper;
import com.ptc.windchill.cadx.common.util.WorkspaceConfigSpecUtilities;
import com.ptc.windchill.cadx.common.util.WorkspaceUtilities;


/**
 * 
 *
 */
public class EPMUtil {
    
	private static Log log = LogFactory.getLog(EPMUtil.class);
	
	private static String DEFAULT_CATEGORY="GENERAL";
	
    public static List<EPMDocument> getChildren(EPMDocument eo){
        List<EPMDocument>  result=new ArrayList<EPMDocument> ();
        return result;
    }
    

	
    /***
	 * set a file as PRIMARY Content
	 * @param ch
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static ContentHolder linkFile(ContentHolder ch, String filename)
			throws Exception {
		Transaction trans = null;
		try {
			trans = new Transaction();
			trans.start();
			ch = (ContentHolder) PersistenceHelper.manager.refresh(ch);
			ApplicationData appData = ApplicationData.newApplicationData(ch);
			appData.setRole(ContentRoleType.PRIMARY);
			appData.setFileName(filename.substring(filename
					.lastIndexOf(File.separator) + 1));
			appData.setUploadedFromPath("");
			File file = new File(filename);
			FileInputStream is = new FileInputStream(file);
			appData = ContentServerHelper.service.updateContent(
					(ContentHolder) ch, appData, is);
			is.close();
			ch = (ContentHolder) ContentServerHelper.service
					.updateHolderFormat((FormatContentHolder) ch);
			trans.commit();
		} catch (Exception e) {
			if (trans != null)
				trans.rollback();
			throw e;
		}
		return ch;
	}
	
    public static void autoPublishEPMDocument2PDF(EPMDocument epmDoc)throws WTException {
        
    }
    
    /**
	 * get the latest iterated epmdocument by the softtype
	 * the method will be run at server side
	 * @param softType
	 * @param ibaConditionMap
	 * @param isLatest
	 */
	public static Vector getEPMDocumentsByTypeAndIBA(String softType , Map ibaConditionMap , boolean isLatest){
		if(!RemoteMethodServer.ServerFlag){
			try {
				return (Vector)RemoteMethodServer.getDefault().invoke("getEPMDocumentsByTypeAndIBA", EPMUtil.class.getName(), null, new Class[]{String.class,Map.class,Boolean.class},new Object[]{softType,ibaConditionMap,isLatest});
			} catch (Exception e) {
				log.error( ExceptionUtils.getStackTrace(e));
			} 
		}
		try {
			Vector vec = new Vector();
			QuerySpec querySpec = new QuerySpec(EPMDocument.class);
			TypeDefinitionReference softTypeDefRef = TypedUtilityServiceHelper.service.getTypeDefinitionReference(softType);
			SearchCondition typeCondition = new SearchCondition(EPMDocument.class,EPMDocument.TYPE_DEFINITION_REFERENCE + ".key.branchId",SearchCondition.EQUAL, softTypeDefRef.getKey().getBranchId());
			querySpec.appendWhere(typeCondition);
			if(BooleanUtils.isTrue(isLatest)){
				querySpec = new LatestConfigSpec().appendSearchCriteria(querySpec);
			}
			if(!MapUtils.isEmpty(ibaConditionMap)){
				for(Iterator  iter =  ibaConditionMap.entrySet().iterator(); iter.hasNext();){
					Entry entry = (Entry)iter.next();
//			    appendStringIBACondition(querySpec, entry.getKey().toString(),SearchCondition.EQUAL, entry.getValue().toString());
					appendStringIBACondition(querySpec, entry.getKey().toString(),SearchCondition.EQUAL, entry.getValue().toString().toUpperCase());
				}	
			}
			querySpec.appendOrderBy(EPMDocument.class,EPMDocument.MODIFY_TIMESTAMP,true);
			log.debug("querySpec="+querySpec);
			QueryResult queryResult = PersistenceHelper.manager.find(querySpec); 
			while(queryResult.hasMoreElements()){
				Object obj = queryResult.nextElement();
				if(obj instanceof Persistable)
					vec.add(obj);
				else if(obj instanceof Persistable[])
					vec.add( ((Persistable[]) obj)[0] );
			}
			return vec;
		} catch (Exception e) {
			log.error(ExceptionUtils.getStackTrace(e));
		}	
		return null;
	}

	
    
    /**
     * 
     * @param oid
     * @param currentContainer
     * @param repName
     * @param repDesc
     * @param publishRuleName
     * @throws WTException
     */
    public static void autoPublishEPMDocument2PDF(String oid,
            String currentContainer, String repName, String repDesc,
            String publishRuleName) throws WTException {
        if (oid == null || oid == "")
            return;
        if (currentContainer == null || currentContainer == "")
            return;
        if (repName == null ) {
            repName = "";
        }
        if (repDesc == null ) {
            repDesc = "";
        }
        String actionString = "";
        if (publishRuleName != null) {
            actionString = "pubrulename=" + publishRuleName;
        } else {
            actionString = "";
        }
        PublishConfigSpec pcs = new PublishConfigSpec();
        pcs.setConfigSpec(0);

        Publisher publisher = new Publisher();
        //FIXME
        publisher.doPublish(false, true, oid, pcs.getEPMActiveNavigationCriteria(true, currentContainer), null, true, repName, repDesc, 1,
                actionString, 1);
    }
    
    
    /**
     * get pdf file by epmdoc
     * 
     * @param epmdocument
     * @return
     * @throws Exception
     */
    public static WTDocument getPublishedContent(EPMDocument epmdocument)
            throws Exception {
        QueryResult qr = PersistenceHelper.manager.navigate(epmdocument,
                PublishedContentLink.PUBLISHED_CONTENT_HOLDER_ROLE,
                PublishedContentLink.class, false);

        WTDocument pdfDoc = null;

        if (qr.size() > 0) {
            while (qr.hasMoreElements()) {
                wt.representation.PublishedContentLink pcl = (wt.representation.PublishedContentLink) qr
                        .nextElement();
                ContentHolder content = pcl
                        .getPublishedContentHolder();
                WTDocument tmpDoc = (WTDocument) content;
                if (pdfDoc == null
                        || pdfDoc.getModifyTimestamp().before(
                                tmpDoc.getModifyTimestamp())) {
                    pdfDoc = (WTDocument) content;
                }
            }
        }
        return pdfDoc;
    }
    
    /**
	 * 
	 * @param querySpec
	 * @param ibaPath
	 * @param operator
	 * @param ibaVal
	 * @return
	 * @throws WTException
	 */
	private static int appendStringIBACondition(QuerySpec querySpec,String ibaPath, String operator, String ibaVal) throws WTException {
		StringDefinition ibaDef = getStringDefByPath(ibaPath);
		int ibaIndex = querySpec.appendClassList(StringValue.class, false);
		querySpec.appendAnd();
		SearchCondition searchConditionHolder = new SearchCondition(querySpec.getPrimaryClass(), "thePersistInfo.theObjectIdentifier.id",StringValue.class, StringValue.IBAHOLDER_REFERENCE + ".key.id");
		querySpec.appendWhere(searchConditionHolder, new int[] { 0, ibaIndex });
		querySpec.appendAnd();
		SearchCondition searchConditionDef = new SearchCondition(StringValue.class,"definitionReference.key.id", SearchCondition.EQUAL, PersistenceHelper.getObjectIdentifier(ibaDef).getId());
		querySpec.appendWhere(searchConditionDef, new int[] { ibaIndex }); 
		querySpec.appendAnd();
		SearchCondition searchConditionVal = new SearchCondition(StringValue.class,StringValue.VALUE, operator, ibaVal);
		querySpec.appendWhere(searchConditionVal, new int[] { ibaIndex }); 
		return ibaIndex;
	}
	
	/**
	 * @param ibaKey
	 * @return
	 */
	public static StringDefinition getStringDefByPath(String ibaKey){
		if(!RemoteMethodServer.ServerFlag){
			try {
				return (StringDefinition) RemoteMethodServer.getDefault().invoke("getStringDefByPath", EPMUtil.class.getName(), null, new Class[]{String.class},new Object[]{ibaKey});
			} catch (Exception e) {
				log.error(ExceptionUtils.getStackTrace(e));
			} 
		}
		StringDefinition stringDef = null;
		try {
			AttributeDefDefaultView defView = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(ibaKey);
			log.debug("DEFView="+defView);
			if (defView instanceof StringDefView) {
				stringDef = (StringDefinition) ObjectReference.newObjectReference(((StringDefView) defView).getObjectID()).getObject();
			}
		} catch (Exception e) {
			log.error("Get IBA Definition [" + ibaKey + "] Error: " + ExceptionUtils.getStackTrace(e));
		}
		return stringDef;
	}

	
	/**
	 * get name of primary content
	 * @param doc
	 * @return
	 */
	public static String getFileName(EPMDocument doc) {
		String filename = doc.getNumber().toLowerCase();
		int dot = filename.indexOf(".");
		if (dot > 0)
			filename = filename.substring(0, dot);
		String extension = doc.getCADName().toLowerCase();
		dot = extension.lastIndexOf(".");
		if (dot > 0)
			extension = extension.substring(dot);
		else
			extension = ".xml";
		filename += extension;
		return filename;
	}
	
	/**
	 * get EPMDoc by UNIQUE_NDID
	 * @param uniqueId
	 * @return
	 * @throws Exception
	 */
	public static long getEPMMemberLinkId(String uniqueId) throws Exception {
		QuerySpec qs = new QuerySpec(EPMMemberLink.class);
		qs.appendWhere(new SearchCondition(EPMMemberLink.class,
				EPMMemberLink.UNIQUE_NDID, SearchCondition.EQUAL, uniqueId));
		QueryResult res = PersistenceHelper.manager.find(qs);
		if (res.hasMoreElements()) {
			EPMMemberLink link = (EPMMemberLink) res.nextElement();
			return link.getUniqueLinkID();
		}
		return Long.parseLong(PersistenceHelper.manager
				.getNextSequence("EPMLINK_SEQ"));
	}
	
	
	/**
	 * get all Descendant(uses) doc from EPMDoc
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static List getDescendants(EPMDocument doc) throws Exception {
		List list = new ArrayList();
		QueryResult qr = EPMStructureHelper.service.navigateUses(doc, null,
				true);
		while (qr.hasMoreElements()) {
			EPMDocumentMaster master = (EPMDocumentMaster) qr.nextElement();
			EPMDocument subDoc = getEPMDocument(master.getNumber(),"");
			list.add(subDoc);
			list.addAll(getDescendants(subDoc));
		}
		return list;
	}
	
	/**
	 * check has any Uses between EPMDoc
	 * @param doc1
	 * @param doc2
	 * @return
	 * @throws Exception
	 */
	public static boolean isDescendants(EPMDocument doc1, EPMDocument doc2)
			throws Exception {
		QueryResult qr = EPMStructureHelper.service.navigateUses(doc1, null,
				true);
		while (qr.hasMoreElements()) {
			EPMDocumentMaster master = (EPMDocumentMaster) qr.nextElement();
			if (master.getNumber().equals(doc2.getNumber())) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * save primary conetent to local
	 * @param contentHodler
	 * @param filename
	 * @throws Exception
	 */
	public static void saveToFile(FormatContentHolder contentHodler,
			String filename) throws Exception {
		ContentHolder holder = ContentHelper.service.getContents(contentHodler);
		ContentItem item = ContentHelper
				.getPrimary((FormatContentHolder) holder);
		InputStream is = ContentServerHelper.service
				.findContentStream((ApplicationData) item);
		saveToDisk(is, filename);
		is.close();
	}
	/**
	 * save primary conetent to local
	 * @param is
	 * @param filename
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void saveToDisk(InputStream is, String filename)
			throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = is.read(buf)) > 0) {
			fos.write(buf, 0, len);
		}
		fos.flush();
		fos.close();
	}
	
	/**
	 * create a DynamicDocument
	 * @param number
	 * @param name
	 * @param softType
	 * @param filename
	 * @param context
	 * @param folderPath
	 * @param iba
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createDynamicDocument(String number, String name,
			String softType, String filename, String context,
			String folderPath, Map iba) throws Exception {
		return createEPMDocument("EPM", "ARBORTEXT", "PUB_COMPOUNDTEXT",
				number, name, softType, filename, context, folderPath, iba);
	}	
	
	/**
	 * 
	 * @param number
	 * @param name
	 * @param softType
	 * @param filename
	 * @param context
	 * @param folderPath
	 * @param iba
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createDynamicDocument(String number, String name,
			String softType, String filename, WTContainer context,
			String folderPath, Map iba) throws Exception {
		return createEPMDocument("EPM", "ARBORTEXT", "PUB_COMPOUNDTEXT",
				number, name, softType, filename, context, folderPath, iba);
	}	
	
	/**
	 * create a EPMDoc
	 * @param epmAppType
	 * @param epmAuthType
	 * @param epmDocType
	 * @param number
	 * @param name
	 * @param softType
	 * @param filename
	 * @param context
	 * @param folderPath
	 * @param iba
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createEPMDocument(String epmAppType,
			String epmAuthType, String epmDocType, String number, String name,
			String softType, String filename, String context,
			String folderPath, Map iba) throws Exception {
		TypeDefinitionReference typeDefinitionRef = null;
		if (softType != null && !"".equals(softType)) {
			typeDefinitionRef = TypedUtility
					.getTypeDefinitionReference(softType);
			if (typeDefinitionRef == null)
				throw new Exception("Not found soft type:" + softType);
		}
		Folder folder = null;
		try {
			folder = GenericUtil.getFolder(folderPath, GenericUtil
					.getWTContainerByName(context));
		} catch (FolderNotFoundException fnfex) {
			throw new Exception("Error: Not found folder " + folderPath
					+ " in contianer " + context + ".");
		}
		return createEPMDocument(EPMApplicationType
				.toEPMApplicationType(epmAppType), EPMAuthoringAppType
				.toEPMAuthoringAppType(epmAuthType), EPMDocumentType
				.toEPMDocumentType(epmDocType), number, name, "",
				typeDefinitionRef, filename, folder, iba);
	}

	/**
	 * 
	 * @param epmAppType
	 * @param epmAuthType
	 * @param epmDocType
	 * @param number
	 * @param name
	 * @param softType
	 * @param filename
	 * @param context
	 * @param folderPath
	 * @param iba
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createEPMDocument(String epmAppType,
			String epmAuthType, String epmDocType, String number, String name,
			String softType, String filename, WTContainer context,
			String folderPath, Map iba) throws Exception {
		TypeDefinitionReference typeDefinitionRef = null;
		if (softType != null && !"".equals(softType)) {
			typeDefinitionRef = TypedUtility
					.getTypeDefinitionReference(softType);
			if (typeDefinitionRef == null)
				throw new Exception("Not found soft type:" + softType);
		}
		Folder folder = null;
		try {
			folder = GenericUtil.getFolder(folderPath, context);
		} catch (FolderNotFoundException fnfex) {
			throw new Exception("Error: Not found folder " + folderPath
					+ " in contianer " + context + ".");
		}
		return createEPMDocument(EPMApplicationType
				.toEPMApplicationType(epmAppType), EPMAuthoringAppType
				.toEPMAuthoringAppType(epmAuthType), EPMDocumentType
				.toEPMDocumentType(epmDocType), number, name, "",
				typeDefinitionRef, filename, folder, iba);
	}
	
	/**
	 * create a EPMDoc
	 * @param epmAppType
	 * @param epmAuthType
	 * @param epmDocType
	 * @param number
	 * @param name
	 * @param cadName
	 * @param softType
	 * @param filename
	 * @param folder
	 * @param iba
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createEPMDocument(EPMApplicationType epmAppType,
			EPMAuthoringAppType epmAuthType, EPMDocumentType epmDocType,
			String number, String name, String cadName,
			TypeDefinitionReference softType, String filename, Folder folder,
			Map iba) throws Exception {
		EPMContextHelper.setApplication(epmAppType);
		EPMDocument doc = EPMDocument.newEPMDocument(number, name, epmAuthType,
				epmDocType);
		if (softType != null) {
			doc.setTypeDefinitionReference(softType);
		}
		String cadNameTemp = cadName;
		if (cadName == null || cadName.equalsIgnoreCase("")) {
			cadNameTemp = filename.substring(filename
					.lastIndexOf(File.separator) + 1);
		}

		doc.setCADName(cadNameTemp);
		doc.setDocSubType(EPMDocSubType.getEPMDocSubTypeDefault());
		doc.setContainer(folder.getContainer());
		FolderHelper.assignLocation(doc, folder);
		
		doc = (EPMDocument) PersistenceHelper.manager.store(doc);
		
		if(iba !=null && !iba.isEmpty()){
			LWCUtil.setValue( doc,iba );
		}
		
		doc = (EPMDocument) GenericUtil.linkFile(doc, filename);
		return doc;
	}
	
    /**
     * 创建EPM文档对象
     * @param docName
     * @param filepath
     * @param containerName
     * @param CADDocType
     * @param CADAppType
     * @param lifecycleTemplate
     * @param lifecycleState
     * @return
     * @throws WTException 
     * @throws IOException 
     * @throws PropertyVetoException 
     * @throws FileNotFoundException 
     */
	public static EPMDocument createEPMDocument(String docName,String docnumber,String filepath,String containerName,String CADDocType,String CADAppType,
			String lifecycleTemplate,String lifecycleState,String folderStr,WTUser creator,String Version,String Revision) throws WTException, FileNotFoundException, PropertyVetoException, IOException{
		
		    EPMDocument doc = null;
		    Debug.P("------>>>>EPM容器名称:"+containerName);
		    if(StringUtils.isEmpty(containerName)) return doc;
			DocumentType[] types = DocumentType.getDocumentTypeSet();
			for(int i=0;i<types.length;i++){
				DocumentType type  = types[i];
				System.out.println(type.getStringValue());
			}

			TypeIdentifier typeidentifier = TypeHelper.getTypeIdentifier("WCTYPE|wt.epm.EPMDocument");
			doc = (EPMDocument)TypeHelper.newInstance(typeidentifier);
			
			WTContainer container=null;
			try {
				container = GenericUtil.getWTContainerByName(containerName);
				if(container==null) return doc;
			} catch (Exception e) {
				e.printStackTrace();
			}
			WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);
			
			doc.setName(docName);		
			doc.setCADName(docName);
			doc.setContainer(container);
			doc.setNumber(docnumber);
			
			EPMDocumentMaster master = (EPMDocumentMaster)doc.getMaster();
			//EPMAuthoringAppType epmauthoringapptype = EPMAuthoringAppType.toEPMAuthoringAppType("ACAD"); cad
			EPMAuthoringAppType epmauthoringapptype = EPMAuthoringAppType.toEPMAuthoringAppType(CADAppType);
			Debug.P("执行到这里了3:"+epmauthoringapptype);
			master.setAuthoringApplication(epmauthoringapptype);
			master.setOwnerApplication(EPMApplicationType.toEPMApplicationType("EPM"));
			
			Debug.P(">>>EPMApplicationType:"+EPMApplicationType.toEPMApplicationType("EPM"));
			//master.setOwnerApplication(EPMApplicationType.getEPMApplicationTypeDefault());
			//OWNERAPPLICATION
			doc.setMaster(master);
			
			LifeCycleHelper.setLifeCycle(doc, LifeCycleHelper.service.getLifeCycleTemplate(lifecycleTemplate, containerRef));
			LifeCycleServerHelper.setState(doc, State.toState(lifecycleState));
			Debug.P(doc+" set state:"+doc.getState().getState().toString());
			
			//EPMDocumentType type = EPMDocumentType.toEPMDocumentType("CADCOMPONENT");
			EPMDocumentType type = EPMDocumentType.toEPMDocumentType(CADDocType);
			doc.setDocType(type);
//			WTContainerRef wtcontainerref = WTContainerRef.newWTContainerRef(container);
			Folder folder = FolderUtil.getFolderRef("/Default/"+folderStr,container,true);
			FolderHelper.assignLocation(doc,folder);
			
			//版本
			/*MultilevelSeries multilevelseries = MultilevelSeries.newMultilevelSeries("wt.vc.VersionIdentifier", Version);
			VersionIdentifier ver = VersionIdentifier.newVersionIdentifier(multilevelseries);
			VersionControlHelper.setVersionIdentifier(doc, ver );//大版本
			Series series = Series.newSeries("wt.vc.IterationIdentifier", Revision);
			IterationIdentifier ide = IterationIdentifier.newIterationIdentifier(series);
			VersionControlHelper.setIterationIdentifier(doc, ide);//小版本	
*/			
			VersionControlHelper.assignIterationCreator(doc, WTPrincipalReference.newWTPrincipalReference(creator));//创建者
			VersionControlHelper.setIterationModifier(doc, WTPrincipalReference.newWTPrincipalReference(creator));//更新者
			OwnershipHelper.setOwner(doc, creator); //所有者
			doc = (EPMDocument) PersistenceHelper.manager.save(doc);

			ApplicationData applicationdata = ApplicationData.newApplicationData(doc);
			applicationdata.setRole(ContentRoleType.toContentRoleType("PRIMARY"));
			Debug.P("上传EPM主文件："+filepath);
			if(!"".equals(filepath)){
				ContentServerHelper.service.updateContent(doc,applicationdata,filepath);
			}
			
			//"C:\\Documents and Settings\\zhao\\桌面\\PROE图纸\\ac-40.asm"
		
		return doc;
	}
	
	/**
	 * 将EPMDoc主文档内容转化成流对象
	 * @param epdoc
	 * @return
	 */
	public static InputStream epmd2ins(EPMDocument epdoc){
		InputStream ins=null;
		if(!RemoteMethodServer.ServerFlag){
			try {
				Class aclass[]={EPMDocument.class};
				Object obj[]={epdoc};
				return (InputStream)RemoteMethodServer.getDefault().invoke("epmd2ins", EPMUtil.class.getName(), null, aclass, obj);
			} catch (Exception e) {
				e.printStackTrace();
				Debug.P(e);
			}
		}else{
			try {
				//获取文档对象
				ContentHolder contentHolder=ContentHelper.service.getContents((ContentHolder)epdoc);
				ContentItem item=ContentHelper.getPrimary((FormatContentHolder)contentHolder);
				ApplicationData appData=(ApplicationData)item;
		        ins=ContentServerHelper.service.findContentStream(appData);
			} catch (Exception e) {
				e.printStackTrace();
				Debug.P(e);
			}
		}
		    return ins;
	}
	
	
	/**
	 * get EPMDoc by number and version
	 * @param num
	 * @param ver
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument getEPMDocument(String num, String ver)
			throws WTException {
		EPMDocument doc = null;
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				EPMDocument.NUMBER, SearchCondition.EQUAL, num);
		qs.appendWhere(sc);
		if (StringUtils.isNotEmpty(ver)) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(new KeywordExpression(
					"A0.versionida2versioninfo"), SearchCondition.EQUAL,
					new KeywordExpression("'" + ver + "'")));
		}
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.latestiterationinfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendOrderBy(EPMDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
			doc = (EPMDocument) qr.nextElement();
		return doc;
	}
	/**
	 *  get EPMDoc by version and like number
	 * @param num
	 * @param ver
	 * @return
	 * @throws WTException
	 */
	public static List getEPMDocumentWithLike(String num, String ver)
			throws WTException {
		List list = new ArrayList();
		EPMDocument doc = null;
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				EPMDocument.NUMBER, SearchCondition.LIKE, num + "%");
		qs.appendWhere(sc);
		if (!ver.equals("")) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(new KeywordExpression(
					"A0.versionida2versioninfo"), SearchCondition.EQUAL,
					new KeywordExpression("'" + ver + "'")));
		}
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.latestiterationinfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendOrderBy(EPMDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		System.out.println(">>>>>>>>>qr = " + qr);
		while (qr.hasMoreElements()) {
			doc = (EPMDocument) qr.nextElement();
			list.add(doc);
		}
		return list;
	}
	
	
	/**
	 * get all EPM Reference object
	 * @param epmdoc
	 * @return
	 * @throws Exception
	 */
	public static List getEPMReferences(EPMDocument epmdoc) throws Exception {
		List numbers = new ArrayList();
		QueryResult qr = EPMStructureHelper.service.navigateReferences(epmdoc,
				null, true);
		while (qr.hasMoreElements()) {
			EPMDocumentMaster master = (EPMDocumentMaster) qr.nextElement();
			if (!numbers.contains(master.getNumber())) {
				numbers.add(master.getNumber());
			}
		}
		List references = new ArrayList();
		for (int i = 0; i < numbers.size(); i++) {
			references.add(getEPMDocument((String) numbers.get(i),""));
		}
		return references;
	}
	
	
	/**
	 * check any Child is check out
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static boolean hasCheckedOutChild(EPMDocument doc) throws Exception {
		QueryResult qr = EPMStructureHelper.service.navigateUses(doc, null,
				true);
		boolean flag = false;
		while (qr.hasMoreElements()) {
			EPMDocumentMaster master = (EPMDocumentMaster) qr.nextElement();
			EPMDocument subDoc = EPMUtil.getEPMDocument(master.getNumber(),"");
			flag = WorkInProgressHelper.isCheckedOut(subDoc);
			if (flag)
				break;
			flag = hasCheckedOutChild(subDoc);
		}
		return flag;
	}
	
	
	
	/**
	 * 获得EPM的软类型集合
	 * get objects by soft Type
	 * @param softtype
	 * @return
	 * @throws Exception
	 */
	public static List queryEPMDocumentBySofttype(String softtype)
			throws Exception {
		QuerySpec qs = new QuerySpec();
		int docIndex = qs.appendClassList(EPMDocument.class, true);
		int defIndex = qs.appendClassList(WTTypeDefinition.class, false);
		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.IDA2TYPEDEFINITIONREFERENCE"), SearchCondition.EQUAL,
				new KeywordExpression("A1.ida2a2")));
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(new KeywordExpression("A1.name"),
				SearchCondition.EQUAL, new KeywordExpression("'" + softtype
						+ "'")));
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.latestiterationinfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendOrderBy(new OrderBy(new KeywordExpression("A0.createstampa2"),
				true));
		QueryResult qr = PersistenceHelper.manager.find(qs);
		List list = new ArrayList();
		List numbers = new ArrayList();

		while (qr.hasMoreElements()) {
			Object[] obj = (Object[]) qr.nextElement();
			EPMDocument doc = (EPMDocument) obj[0];
			if (!numbers.contains(doc.getNumber())) {
				numbers.add(doc.getNumber());
				list.add(doc);
			}
		}
		return list;
	}
	
	
	/**
	 * 更新EPM主文档内容
	 * update the primary content
	 * @param newDoc
	 * @param filename
	 * @return
	 * @throws WTException
	 * @throws PropertyVetoException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static EPMDocument changeDocumentPrimary(EPMDocument newDoc,
			String filename) throws WTException, PropertyVetoException,
			FileNotFoundException, IOException {
		ContentHolder contentholder = (ContentHolder) newDoc;

		contentholder = ContentHelper.service.getContents(contentholder);
		Vector contentListForTarget = ContentHelper
				.getContentListAll(contentholder);
		for (int i = 0; i < contentListForTarget.size(); i++) {
			ContentItem contentItem = (ContentItem) contentListForTarget
					.elementAt(i);
			if (contentItem.getRole().toString().equals("PRIMARY")) {
				System.out.println("Delete Current Primary content!");
				ContentServerHelper.service.deleteContent(contentholder,
						contentItem);
				break;
			}
		}

		ApplicationData applicationdata = ApplicationData
				.newApplicationData(contentholder);
		applicationdata.setRole(ContentRoleType.toContentRoleType("PRIMARY"));
		applicationdata.setCategory(DEFAULT_CATEGORY);
		applicationdata = ContentServerHelper.service.updateContent(
				contentholder, applicationdata, filename);
		newDoc = (EPMDocument) PersistenceHelper.manager.refresh(newDoc);
		return newDoc;
	}
	
	
	/**
	 * get EPMDocument by oid
	 * @param oid
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument getEPMDocumentByOid(String oid) throws Exception {
		return (EPMDocument) GenericUtil.REF_FACTORY.getReference(oid)
				.getObject();
	}
	
	
	
	/**
	 * get all Released epmdoc by number
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public static List queryReleaseEPMDocumentsByNumber(String num)
			throws Exception {
		EPMDocument doc = null;
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				EPMDocument.NUMBER, SearchCondition.EQUAL, num);
		qs.appendWhere(sc);
		qs.appendAnd();
		qs
				.appendWhere(new SearchCondition(EPMDocument.class,
						EPMDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL,
						"RELEASED"));
		qs.appendOrderBy(EPMDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		List list= new ArrayList();
		while (qr.hasMoreElements()) {
			doc = (EPMDocument) qr.nextElement();
			list.add(doc);
		}
		  return list;
	}
	
	
	/**
	 * get all Released epmdoc by number,state
	 * @param num
	 * @param state
	 * @return
	 * @throws Exception
	 */
	public static List queryEPMDocumentsByNumberState(String num, String state)
			throws Exception {
		EPMDocument doc = null;
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				EPMDocument.NUMBER, SearchCondition.EQUAL, num);
		qs.appendWhere(sc);
		qs.appendAnd();

		qs.appendWhere(new SearchCondition(EPMDocument.class,
				EPMDocument.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state));
		qs.appendOrderBy(EPMDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		List l = new ArrayList();
		while (qr.hasMoreElements()) {
			doc = (EPMDocument) qr.nextElement();
			l.add(doc);
		}
		return l;
	}
	
	
	
	/**
	 * get the pre version by number
	 * @param num
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument getPreFinalEPMDocumentByNumber(String num)
			throws Exception {
		List l = queryEPMDocumentsByNumberState(num, "Final");
		if (l.size() > 0) {
			return (EPMDocument) l.get(0);
		} else {
			return null;
		}

	}
	
	/**
	 * get the last baseline
	 * @param doc
	 * @return
	 * @throws WTException
	 */
	public static ManagedBaseline getEPMLastedBaseLine(EPMDocument doc)
			throws WTException {
		ManagedBaseline baseline = null;
		wt.fc.QueryResult qr = wt.vc.baseline.BaselineHelper.service
				.getBaselines(doc);
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (!(obj instanceof wt.vc.baseline.ManagedBaseline))
				continue;
			wt.vc.baseline.ManagedBaseline b1 = (wt.vc.baseline.ManagedBaseline) obj;
			if (baseline != null) {
				if (baseline.getCreateTimestamp().before(
						b1.getCreateTimestamp())) {
					baseline = b1;
				}
			} else {
				baseline = b1;
			}
		}
		return baseline;
	}
	
	
	/**
	 * 通过CAD名称和编号搜索EPM文档
	 * get epmdocs by cadname,cadnumber
	 * @param cadname
	 * @param cadnumber
	 * @return
	 * @throws Exception
	 */
	public static ArrayList searchEPMDocuments(String cadname, String cadnumber)
			throws Exception {

		if (StringUtil.isNullOrEmpty(cadname)
				&& StringUtil.isNullOrEmpty(cadnumber)) {
			return null;
		}

		QuerySpec qs = new QuerySpec(EPMDocument.class);
		qs.appendOpenParen();
		if (!StringUtil.isNullOrEmpty(cadnumber)) {
			SearchCondition sc = new SearchCondition(EPMDocument.class,
					EPMDocument.NUMBER, SearchCondition.LIKE, cadnumber + "%");
			qs.appendWhere(sc);
			qs.appendAnd();
		}

		if (!StringUtil.isNullOrEmpty(cadname)) {
			SearchCondition sc = new SearchCondition(EPMDocument.class,
					EPMDocument.CADNAME, SearchCondition.LIKE, cadname + "%");
			qs.appendWhere(sc);
			qs.appendAnd();
		}

		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.latestiterationinfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendCloseParen();
		qs.appendOrderBy(EPMDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		ArrayList list = new ArrayList();
		List numbers = new ArrayList();

		while (qr.hasMoreElements()) {
			EPMDocument doc = (EPMDocument) qr.nextElement();
			if (!numbers.contains(doc.getNumber())) {
				numbers.add(doc.getNumber());
				list.add(doc);
			}
		}
		return list;
	}
	
	
	/**
	 * get the last reference epmdoc
	 * @param doc
	 * @return reference epmdocuments
	 * @throws WTException
	 */
	public static EPMDocument getLastedEPMReferenceDoc(EPMDocument doc)
			throws WTException {
		QueryResult res1 = PersistenceHelper.manager.navigate(doc,
				"references", wt.epm.structure.EPMReferenceLink.class);
		EPMDocumentMaster returnDocMaster = null;
		while (res1.hasMoreElements()) {
			Object obj = res1.nextElement();
			if (obj instanceof EPMDocumentMaster) {
				EPMDocumentMaster master = (EPMDocumentMaster) obj;
				if (returnDocMaster == null) {
					returnDocMaster = master;
				} else {
					if (returnDocMaster.getCreateTimestamp().before(
							master.getCreateTimestamp())) {
						returnDocMaster = master;
					}
				}
			}
		}
		if (returnDocMaster != null) {
			return getEPMDocument(returnDocMaster.getNumber(),"");
		} else {
			return null;
		}
	}
	
	
	/**
	 * get primary content file name from a EPMDoc
	 * @param doc
	 * @return
	 */
	public static String getFileNameByEPMDocName(EPMDocument doc) {
		String rt = "";
		String filename = doc.getName().toLowerCase().replaceAll(" ", "");
		int dot = filename.indexOf(".");
		if (dot > 0)
			filename = filename.substring(0, dot);
		String extension = doc.getCADName().toLowerCase();
		dot = extension.lastIndexOf(".");
		if (dot > 0)
			extension = extension.substring(dot);
		else
			extension = ".xml";

		rt = filename + extension;

		return rt;
	}
	
	
	/**
	 * get primary conetent file name from epmdoc
	 * @param doc
	 * @return
	 */
	public static String getOfflineFileName(EPMDocument doc) {
		String filename = doc.getName();
		filename = doc.getName() + "(" + doc.getNumber() + ")"
				+ getFileExt(doc);
		filename = GenericUtil.formatFileName(filename);
		return filename;
	}
	/**
	 * get primary conetent file extension name from epmdoc
	 * @param doc
	 * @return
	 */
	public static String getFileExt(EPMDocument doc) {
		String extension = doc.getCADName().toLowerCase();
		int dot = extension.lastIndexOf(".");
		if (dot > 0)
			extension = extension.substring(dot);
		else
			extension = ".xml";
		return extension;
	}
	/**
	 * save primary conetent to local
	 * @param doc
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static String saveToDir(EPMDocument doc, String dir)
			throws Exception {
		String filename = getFileName(doc);
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		saveToFile(doc, dir + filename);
		return filename;
	}

	/**
	 * save primary conetent to local
	 * @param doc
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static String saveToDirAsEPMName(EPMDocument doc, String dir)
			throws Exception {
		String filename = getFileNameByEPMDocName(doc).replaceAll(" ", "");
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		filename = GenericUtil.duplicatefilename(dir + filename);
		saveToFile(doc, filename);
		System.out.println("GenericUtil:filename=" + filename);
		int dot = filename.lastIndexOf(File.separator);
		return filename.substring(dot + 1);
		// return ;
	}
	
	
	/**
	 * save primary conetent to local
	 * @param doc
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static String saveToDirFormatName(EPMDocument doc, String dir)
			throws Exception {
		String filename = getFileName(doc);
		filename = GenericUtil.formatFileName(filename);
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		saveToFile(doc, dir + filename);
		System.out.println("GenericUtil:filename=" + filename);
		return filename;
	}
	
	
	/**
	 * get EPMDoc by pdf(PublishedContent)
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument getEPMDocumentByPublishedContent(WTDocument doc)
			throws Exception {
		QueryResult qrr = PersistenceHelper.manager.navigate(doc,
				"representable", wt.representation.PublishedContentLink.class);
		EPMDocument epmdoc = null;
		while (qrr.hasMoreElements()) {
			Object obj = qrr.nextElement();
			if (obj instanceof EPMDocument) {
				epmdoc = (EPMDocument) obj;
				break;
			}
		}
		return epmdoc;
	}
	
	
	/**
	 * get all pdf files by EPMDoc
	 * @param epmdoc
	 * @return
	 * @throws Exception
	 */
	public static List getPublishedContentObject(EPMDocument epmdoc) throws Exception {
		List numbers = new ArrayList();
		QueryResult qr = EPMStructureHelper.service.navigateBothRoles(epmdoc,
				PublishedContentLink.class);
		while (qr.hasMoreElements()) {
			PublishedContentLink link = (PublishedContentLink) qr.nextElement();
			WTDocument doc = (WTDocument) link.getRoleAObject();
			if (!numbers.contains(doc.getNumber())) {
				numbers.add(doc.getNumber());
			}
		}
		List docs = new ArrayList();
		for (int i = 0; i < numbers.size(); i++) {
			WTDocument doc=(WTDocument) GenericUtil.getObjectByNumber((String)numbers.get(i));
			docs.add(doc);
		}
		return docs;
	}
	
	/**
	 * get ApplicationData from EPMDoc
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static ApplicationData getApplicationData(EPMDocument doc)
			throws Exception {
		ContentHolder holder = ContentHelper.service.getContents(doc);
		ContentItem item = ContentHelper
				.getPrimary((FormatContentHolder) holder);
		// ContentServerHelper.service.findContentStream((ApplicationData)item);
		ApplicationData appData = (ApplicationData) item;
		return appData;
	}
	
	
	
	/**
	 * create a DynamicDocument
	 * @param epmDocType
	 * @param number
	 * @param name
	 * @param softType
	 * @param filename
	 * @param context
	 * @param folderPath
	 * @param iba
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createDynamicDocument(String epmDocType,
			String number, String name, String softType, String filename,
			String context, String folderPath, Map iba) throws Exception {
		return createEPMDocument("EPM", "ARBORTEXT", epmDocType, number, name,
				softType, filename, context, folderPath, iba);
	}

	
	
	/**
	 * create a EPMDoc
	 * @param epmAuthType
	 * @param epmDocType
	 * @param number
	 * @param name
	 * @param softType
	 * @param filename
	 * @param folder
	 * @param iba
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createEPMDocument(
			EPMAuthoringAppType epmAuthType, EPMDocumentType epmDocType,
			String number, String name, TypeDefinitionReference softType,
			String filename, Folder folder, Map iba) throws Exception {
		return createEPMDocument(
				EPMApplicationType.toEPMApplicationType("EPM"), epmAuthType,
				epmDocType, number, name, "", softType, filename, folder, iba);
	}
	
	
	


	/**
	 * rename a epmdoc
	 * @param epmdoc
	 * @param newName
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument rename(EPMDocument epmdoc, String newName)
			throws Exception {
		if (newName == null)
			return epmdoc;
		newName = newName.trim();
		if (!StringUtil.isNullOrEmpty(newName)
				&& !newName.equals(epmdoc.getName())) {
			Identified aIdentified = (Identified) epmdoc.getMaster();
			EPMDocumentMasterIdentity aEPMDocumentMasterIdentity = (EPMDocumentMasterIdentity) aIdentified
					.getIdentificationObject();
			aEPMDocumentMasterIdentity.setName(newName);
			IdentityHelper.service.changeIdentity(aIdentified,
					aEPMDocumentMasterIdentity);
			epmdoc = (EPMDocument) PersistenceHelper.manager.refresh(epmdoc);
		}
		return epmdoc;
	}
	
	
	/**
	 * get EPMDoc by name ,version
	 * @param name
	 * @param ver
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument getEPMDocumentByName(String name, String ver)
			throws Exception {
		EPMDocument doc = null;
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				EPMDocument.NAME, SearchCondition.EQUAL, name);
		qs.appendWhere(sc);
		if (!StringUtils.isEmpty(ver)) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(new KeywordExpression(
					"A0.versionida2versioninfo"), SearchCondition.EQUAL,
					new KeywordExpression("'" + ver + "'")));
		}
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.latestiterationinfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendOrderBy(EPMDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements()) {
			doc = (EPMDocument) qr.nextElement();
			if (!doc.getLocation()
					.startsWith(GenericUtil.CONTAINER_ROOT_FOLDER))
				continue;
			break;
		}
		return doc;
	}

	
	
	/**
	 * update a EPMDoc
	 * @param doc
	 * @param name
	 * @param filename
	 * @param category
	 * @param iba
	 * @param comments
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument updateEPM(EPMDocument doc, String name,
			String filename, String category, HashMap iba, String comments)
			throws Exception {
		Transaction trans = null;
		try {
			trans = new Transaction();
			trans.start();
			if (!doc.getName().equals(name)) {
				rename(doc, name);
			}
			doc = (EPMDocument) VersionControlUtil.checkout(doc);
			if (iba != null && !iba.isEmpty()) {
				LWCUtil.setValue( doc,iba);
			}
			
			doc = (EPMDocument) VersionControlUtil.checkin(doc, comments);
			doc = (EPMDocument) PersistenceHelper.manager.save(doc);
			
			ContentHolder contentholder = (ContentHolder) doc;
			contentholder = ContentHelper.service.getContents(contentholder);
			List contentListForTarget = ContentHelper
					.getContentListAll(contentholder);
			for (int i = 0; i < contentListForTarget.size(); i++) {
				ContentItem contentItem = (ContentItem) contentListForTarget
						.get(i);
				if (contentItem.getRole().toString().equals("PRIMARY")) {
					ContentServerHelper.service.deleteContent(contentholder,
							contentItem);
					break;
				}
			}
			ApplicationData appData = ApplicationData.newApplicationData(doc);
			appData.setRole(ContentRoleType.PRIMARY);
			appData.setFileName(doc.getCADName());
			StandardContentService.setFormat(filename, appData);
			if(StringUtils.isEmpty(category)){
				category=DEFAULT_CATEGORY;
			}
			appData.setCategory(category);
			FileInputStream is = new FileInputStream(filename);
			appData = ContentServerHelper.service.updateContent(doc, appData,
					is);
			is.close();
			doc = (EPMDocument) ContentServerHelper.service.updateHolderFormat(doc);
			trans.commit();
			return doc;
		} catch (Exception e) {
			if (trans != null)
				trans.rollback();
			e.printStackTrace();
			return doc;
		}
	}

	


	/**设置EPM工作区属性
	 * @param ws
	 * @param epm EPm
	 * @param attr_value_map
	 * @throws WTException
	 */
	public static void setStringAttributes(EPMWorkspace ws, EPMDocument epm,  Map<String, Object> attr_value_map)  throws WTException
	{
      
      if(attr_value_map!=null&&attr_value_map.size()>0){
    	  Debug.P(">>>>attr_value_map Size:"+attr_value_map.size()+"   info:"+attr_value_map);
    	  WTKeyedMap keyed_map = new WTKeyedHashMap();
    	  for(Iterator<?> ite=attr_value_map.keySet().iterator();ite.hasNext();){
    		  String key=(String) ite.next();
    		  String value=(String)attr_value_map.get(key);
    		  keyed_map.put(key,value);
    	  }
    	  if(keyed_map.size()>0){
    		  Debug.P(">>>>keyed_map:"+keyed_map.size());
    		  EPMWorkspaceHelper.manager.setAttributes(ws, keyed_map);
        	  Debug.P(">>>>Set attr_value_map to Workspace Success!");
    	  }
    	  
      }
	}
		
	
	
}
