package ext.tmt.utils;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;  
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.DataFormat;
import wt.content.DataFormatReference;
import wt.content.FormatContentHolder;
import wt.content.URLData;
import wt.doc.DocumentType;
import wt.doc.WTDocument;
import wt.doc.WTDocumentDependencyLink;
import wt.doc.WTDocumentHelper;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.iba.value.IBAHolder;
import wt.iba.value.service.IBAValueHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleServerHelper;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.LifeCycleTemplateMaster;
import wt.lifecycle.LifeCycleTemplateReference;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartReferenceLink;
import wt.pom.Transaction;
import wt.query.ConstantExpression;
import wt.query.KeywordExpression;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.StructHelper;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;

import com.infoengine.util.UrlEncoder;
import com.mongodb.WriteResult;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;
import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.windchill.enterprise.copy.server.CoreMetaUtility;
import com.sg.visionadapter.IFileProvider;
import com.sg.visionadapter.PMDocument;
import com.sg.visionadapter.URLFileProvider;

import ext.tmt.integration.webservice.pm.ConstanUtil;




/**
 * �������ڴ����ĵ���һЩ����
 * 
 * @author Tony
 */
@SuppressWarnings("deprecation")
public class DocUtils implements RemoteAccess{
	
	
	
	private static final String SET_MODIFIER = "setModifier";



	private static final boolean SERVER=RemoteMethodServer.ServerFlag;
	
	
	
	
	

	
	
	/**
	 * ����WTDocument
	 * @param pmdoc  PM�ĵ����
	 * @param docType �ĵ�����
	 *@param  vmuser �����û�
	 *@param ibas �����Լ���
	 *@param lifecycleName ��������״̬ģ������
	 * @return
	 * @throws Exception
	 * @throws WTException
	 */
	public static WTDocument createDocument(PMDocument pmdoc,String docType,String vmUserName,Map ibas,Folder folder) throws 	WTException,Exception
		{

		
		WTDocument document =null;
		Transaction tx = null;
		try {
			  tx=new Transaction();
			  tx.start();
			  String doc_Number=getWTDocumentNumber();//Ĭ���������
			  document =WTDocument.newWTDocument();
			//�ĵ�����
			if (!StringUtils.isEmpty(docType)) {
			  String docTypeEnum = GenericUtil.getTypeByName(docType);//������ʵ���ƻ�ȡ������
		      if(StringUtils.isEmpty(docTypeEnum)){
		         throw new WTException("--->>��windchill���޷��ҵ�"+docTypeEnum+"����ĵ�����!");
		      } 
		      TypeDefinitionReference typeDefinitionRef   = TypedUtility.getTypeDefinitionReference(docTypeEnum);
		       if (typeDefinitionRef == null) {
					Debug.P("ERROR :typeDefinitionRef is null,docType="+ docType);
					return null;
				}
		        document.setTypeDefinitionReference(typeDefinitionRef);
			}else{//Ĭ�ϳ����ĵ�
				 DocumentType type=wt.doc.DocumentType.toDocumentType("$$Document");
				 document.setDocType(type);
			}
			
			String doc_Name=pmdoc.getCommonName();
			document.setName(doc_Name);
			document.setNumber(doc_Number);
			//��������
		    String containerName=pmdoc.getContainerName();
		    Debug.P("------>>>PM  ContainerName:"+containerName);
			WTContainer container=GenericUtil.getWTContainerByName(containerName);
			
//			EnumeratedType type[] = DocumentType.getDocumentTypeDefault().getSelectableValueSet();
//			document.setDocType((DocumentType) type[1]);
//			EnumeratedType aenumeratedtype[] = DepartmentList.getDepartmentListSet();
//			document.setDepartment((DepartmentList) aenumeratedtype[1]);
			document.setContainer(container);
			document.setOrganization(container.getOrganization());
			
			//ָ���ļ���λ��
			if(folder!=null){
				FolderHelper.assignLocation(document, folder);
			}
			String description=pmdoc.getDescription();
			if (StringUtils.isEmpty(description)){
				document.setDescription(description);
			}	
			
//			WTUser wtuser=null;
//			try {//��ȡ�û���Ϣ
//			    wtuser=OrganizationServicesHelper.manager.getAuthenticatedUser(username);
//			} catch (WTException e) {//��ȡ�����û�
//				 wtuser=OrganizationServicesHelper.manager.getAuthenticatedUser(vmUserName);
//			}
//		    Debug.P("---->VmUser:"+vmUserName+"   ;UserName="+wtuser);
//			VersionControlHelper.assignIterationCreator(doc, WTPrincipalReference.newWTPrincipalReference(wtuser));//������
//			VersionControlHelper.setIterationModifier(doc, WTPrincipalReference.newWTPrincipalReference(wtuser));//������
//			OwnershipHelper.setOwner(doc, wtuser); //������
			
		     
			if (ibas != null && !ibas.isEmpty()) {
				LWCUtil.setValueBeforeStore(document,ibas);
			}
	     
			 document = (WTDocument) PersistenceHelper.manager.save(document);
			 Debug.P("---New--->>WTDoc:"+document.getFolderPath() +"(LifecycleState:"+document.getLifeCycleName()+")   Success!!!");
		
			PersistenceHelper.manager.refresh(document);
			
			//�ĵ�
			ByteArrayInputStream pins=null;
			ByteArrayInputStream sins=null;
			String contentUrl=pmdoc.getUrl();//PM������·��
			try {
				IFileProvider content = pmdoc.getContent();//PM���ĵ���
				if(content!=null){
					   String pname=content.getFileName();//PM���ļ�����
				    	//�ϴ����ĵ���Ϣ
						ByteArrayOutputStream bout=new ByteArrayOutputStream();
		    			content.write(bout);
		    			pins=new ByteArrayInputStream(bout.toByteArray());
		    			linkDocument(document, pname, pins, "1", contentUrl);
				}

				
				//�ϴ�������Ϣ
				List<IFileProvider> attachments = pmdoc.getAttachments();
				if(attachments!=null&&attachments.size()>0){
					for (IFileProvider provider : attachments) {
						ByteArrayOutputStream bsout=new ByteArrayOutputStream();
							provider.write(bsout);
							sins=new ByteArrayInputStream(bsout.toByteArray());
							linkDocument(document, provider.getFileName(), sins, "0", null);
					}
				}
				
				
				
			      }catch (IOException e) {
				      e.printStackTrace();
				      throw new Exception("Windchillͬ����ȡPM�ĵ�("+doc_Name+")�ļ����쳣");
			    }
			
			if (document != null) {
			if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(document, wt.session.SessionHelper.manager.getPrincipal()))
				document = (WTDocument) WorkInProgressHelper.service.checkin(document, "add primary file");
		   }
			//����������ӵ�ַ
			if(document!=null){
				addDownloadURL2PM(pmdoc,document);
			}
			  tx.commit();
	    	  tx=null;
		} catch (Exception e) {
		     e.printStackTrace();
		}finally{
			if(tx!=null){
			    tx.rollback();
			}
		}
		 return document;
	}
	

	
	
	
	/**
	 * �ϴ����ĵ������Ǹ�������
	 * @param document �ĵ�����
	 * @param file �ļ���
	 * @param �ļ���
	 * @param type �ϴ����� (1�����ĵ����� 0:��������)
	 * @param isCreated �Ƿ��Ѵ����ĵ���
	 * @return  
	 * @throws Exception
	 */
	public static WTDocument linkDocument(WTDocument document, String fileName,InputStream ins,String type,String contentUrl)
			throws Exception {
		if (document == null)
			return null;
		Transaction tx = null;
		try {
			tx = new Transaction();
			tx.start();
			if(ins!=null){//�ļ���
				ApplicationData app = ApplicationData.newApplicationData(document);
				if("1".equals(type)){
					app.setRole(ContentRoleType.PRIMARY);
				}else if("0".equals(type)){
					app.setRole(ContentRoleType.SECONDARY);
				}
				app.setFileName(fileName);
				app.setUploadedFromPath("");
				app = ContentServerHelper.service.updateContent((ContentHolder) document, app, ins);
				ins.close();
			}else{//���ӵ�ַ
				if(!StringUtils.isEmpty(contentUrl)){
					URLData urldata=URLData.newURLData(document);
					urldata.setUrlLocation(contentUrl);
					urldata.setRole(ContentRoleType.PRIMARY);
					urldata.setDisplayName(fileName);
					ContentServerHelper.service.updateContent((ContentHolder) document,urldata);
				}
			}
			
			document = (WTDocument) ContentServerHelper.service.updateHolderFormat((FormatContentHolder) document);
			tx.commit();
			tx = null;

		} catch (WTException e) {
			throw e;
		} catch (Exception e) {
			throw new WTException(e);
		} finally {
			if (tx != null){
				tx.rollback();
			}
		}
		return document;
	}
	
	
	/**
	 * �����ĵ���IBA����
	 * @param doc						�ĵ�����
	 * @param table						�ĵ���IBA����
	 * @throws Exception
	 */
	public void setIbaAttribute(WTDocument doc, Hashtable<String, String> table) throws Exception{
		Enumeration<String> enum1 = table.keys();
		IBAHolder ibaholder = IBAValueHelper.service.refreshAttributeContainer(doc, null, null, null);
        IBAUtils ibaUtil = new IBAUtils(ibaholder);
        
		while(enum1.hasMoreElements()){
			String ibaKey = enum1.nextElement();
			String ibaValue = table.get(ibaKey);
			ibaUtil.setIBAValue(ibaKey, ibaValue);
		}
		  ibaUtil.updateIBAPart(ibaholder);
	}
	
	/**
	 *����ĵ���������ĵ��ļ�����
	 * @param doc
	 * @return
	 */
	public static String getPrimaryFileNameByDoc(WTDocument doc){
		String fileName=null;
		if(!SERVER){
			try {
				Class aclass[]={WTDocument.class};
				Object obj[]={doc};
				return (String)RemoteMethodServer.getDefault().invoke("getPrimaryFileNameByDoc", DocUtils.class.getName(), null, aclass, obj);
			} catch (Exception e) {
				e.printStackTrace();
				Debug.P(e);
			}
		}else{
			try {
				//��ȡ�ĵ�����
				ContentHolder contentHolder=ContentHelper.service.getContents((ContentHolder)doc);
				ContentItem item=ContentHelper.getPrimary((FormatContentHolder)contentHolder);
				ApplicationData appData=(ApplicationData)item;
				fileName=appData.getFileName();
			} catch (Exception e) {
				e.printStackTrace();
				Debug.P(e);
			}
		}
		      return fileName;
	}
	
	/**
	 * ����ͼ���ĵ���Ϣ���������룬�Լ��ͱ�����ͬ���㲿�����й���
	 * 
	 * @param nmCommandBean
	 * @throws WTPropertyVetoException
	 */
	public static void refreshDocument(NmCommandBean nmCommandBean) throws WTPropertyVetoException {
		try {
			NmOid nmOid = nmCommandBean.getPrimaryOid();
			Object obj = nmOid.getRef();
			if (obj instanceof WTDocument) {
				WTDocument document = (WTDocument) obj;
				String drwNumber = getDrwNumberByFileName(document);
				if (drwNumber != null && !"".equals(drwNumber)) {
					changeDcouementNumber(document, drwNumber);
					linkDocument2Part(document);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

//	/**
//	 * ���ݽ�ѹ��zip���е��ļ�������ͼֽ����
//	 * 
//	 * @param folderPath
//	 * @param wtContainerRef
//	 * @throws WTException
//	 * @throws WTPropertyVetoException
//	 */
//	private static boolean createDocumentByZipFile(String targetPath, WTContainerRef wtContainerRef, WTDocument zipDocument) throws WTException, WTPropertyVetoException {
//		File dir = new File(targetPath);
//		File[] files = dir.listFiles();
//		boolean result = false;
//		Transaction tran =new Transaction();// ��������
//		tran.start();
//		try{
//        Debug.P(files.length);
//		if (files == null)
//			return false;
//		for (int i = 0; i < files.length; i++) {
//			if (files[i].isDirectory()) {
//				createDocumentByZipFile(files[i].getAbsolutePath(), wtContainerRef, zipDocument);
//			} else {
//				String strFileName = files[i].getAbsolutePath().toUpperCase();
//				System.out.println("---" + files[i].getAbsolutePath());
//				String drwNumber = strFileName.substring(strFileName.lastIndexOf("\\") + 1, strFileName.lastIndexOf("."));
//				Debug.P(drwNumber);
//				if ("DWG".equals(strFileName.substring(strFileName.lastIndexOf(".")+1, strFileName.length()))) {
//					WTDocument document = getDocumentByNumber(drwNumber);
//					Debug.P(document);
//					if (document == null) {
//						document = createDoc(drwNumber, strFileName, wtContainerRef, zipDocument);
//						linkDocument2Part(document);
//					}
//				}
//
//			}
//		}
//		tran.commit();
//		tran = null;
//		result = true;
//		}catch (Exception e){
//			e.printStackTrace();
//		}finally{
//			if(tran !=null){
//				result = false;
//				tran.rollback();
//			}
//		}
//		return result;
//	}

//	/**
//	 * ����ͼֽ�ĵ�����
//	 * 
//	 * @param drwNumber
//	 * @param strFileName
//	 * @param wtContainerRef
//	 * @return
//	 * @throws WTException
//	 */
//	private static WTDocument createDoc(String drwNumber, String strFileName, WTContainerRef wtContainerRef, WTDocument zipDocument) {
//		WTDocument document = null;
//		try {
//			// ͼ���ĵ�����
//			String DrwDocType = "WCTYPE|wt.doc.WTDocument|com.SYFJ_windchill.DrawDoc";
//			document = WTDocument.newWTDocument();
//			String folderPath = zipDocument.getFolderPath();
//			// Folder folder = FolderHelper.service.getFolder(strFolder,
//			// WTContainerRef.newWTContainerRef(wtContainerRef));
//			Folder folder = FolderHelper.service.getFolder(folderPath.substring(0, folderPath.lastIndexOf("/")), wtContainerRef);
//
//			document.setName(drwNumber);
//			document.setNumber(drwNumber);
//			document.setContainerReference(wtContainerRef);
//			// �����ļ���
//			if (folder != null)
//				FolderHelper.assignLocation((FolderEntry) document, folder);
//			// �����ĵ�����
//			TypeIdentifier id = TypeHelper.getTypeIdentifier(DrwDocType);
//			document = (WTDocument) CoreMetaUtility.setType(document, id);
//			document = (WTDocument) PersistenceHelper.manager.save(document);
//			// �����ĵ�����Ϊ��е��
//			IBAUtils.setIBAStringValue(document, Contants.DOCTYPE, Contants.MACHINERYDOC);
//			IBAUtils.updateIBAHolder(document);
//			updateDocContent(document, strFileName);
//		} catch (WTException e) {
//			e.printStackTrace();
//		} catch (WTPropertyVetoException e) {
//			e.printStackTrace();
//		}
//		return document;
//	}

	/**
	 * �����ĵ����Ʋ��Ҷ�Ӧ��WTDocument������ĵ���Ӧ���ļ�ΪDBF�ļ����򷵻أ����򷵻ؿ�
	 * 
	 * @param docName
	 * @return
	 * @throws WTException
	 * @throws PropertyVetoException
	 */
	public static WTDocument getDBFByName(String docName) throws WTException, PropertyVetoException {
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		SearchCondition numberSC = new SearchCondition(WTDocument.class, WTDocument.NAME, SearchCondition.EQUAL, docName);
		SearchCondition latestIteration = new SearchCondition(WTDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE);
		querySpec.appendWhere(numberSC);
		querySpec.appendAnd();
		querySpec.appendWhere(latestIteration);
		QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
		queryResult = (new LatestConfigSpec()).process(queryResult);
		WTDocument document = null;
		while (queryResult.hasMoreElements()) {
			document = (WTDocument) queryResult.nextElement();
			FormatContentHolder formatContentHolder = (FormatContentHolder) ContentHelper.service.getContents(document);
			ContentItem contentItem = ContentHelper.getPrimary(formatContentHolder);
			ApplicationData applicationData = (ApplicationData) contentItem;
			if (applicationData == null) {
				continue;
			}
			String fileName = applicationData.getFileName();
			if (fileName.endsWith("DBF") || fileName.endsWith("dbf")) {
				return document;
			}

		}
		return null;
	}

	/**
	 * �����ĵ����������ͬ������㲿�������й��� ����㲿�������ڣ������ĵ����봴���µ��㲿�������й���
	 * ���Ϊϵ�м�����ֻ��ͱ�����ͬ���㲿�����й������������µ��㲿��
	 * 
	 * @param document
	 *            �ĵ�
	 * @throws WTPropertyVetoException
	 */
	public static void linkDocument2Part(WTDocument document) throws WTPropertyVetoException {

		try {
			String partNumber = null;
			String docNumber = document.getNumber();
			if (docNumber.contains("-W")) {
				partNumber = docNumber;
			}
			partNumber = generatePartNumberByDocNumber(docNumber);
			WTPart part = PartUtil.getPartByNumber(partNumber);
			if (part == null && !docNumber.contains("-W")) {
				//part = PartUtil.generateNewPartByDocument(document, partNumber);
			}
			if (part != null) {
				String partName = part.getName();
				updateDocumentName(document, partName);
				document = getDocumentByNumber(docNumber);
				WTPartDescribeLink describleLink = WTPartDescribeLink.newWTPartDescribeLink(part, document);
				PersistenceServerHelper.manager.insert(describleLink);
				PersistenceHelper.manager.refresh(describleLink);
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	/***
	 * �����ĵ��ı������ɶ�Ӧ�㲿���ı���
	 * 
	 * @param docNumber
	 * @return
	 */
	private static String generatePartNumberByDocNumber(String docNumber) {
		String partNumber = null;
		String str;
		if (docNumber.indexOf("[") != -1) {
			str = docNumber.substring(0, docNumber.indexOf("["));
		} else {
			str = docNumber;
		}
		String a = str.substring(str.length() - 1);
		if (isCheckLetter(a)) {
			partNumber = str.substring(0, str.length() - 1);
		} else {
			partNumber = str;
		}
		return partNumber;
	}

	/**
	 * �����ĵ������ѯ��װͼֽ�����ĵ�����
	 * 
	 * @param docNumber
	 *            �ĵ�����
	 * @return WTDocument �ĵ�����
	 * @throws WTException
	 */
	public static WTDocument getDocumentByNumber(String docNumber) throws WTException {
		WTDocument document = null;
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		SearchCondition numberSC = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, docNumber);
		SearchCondition latestIteration = new SearchCondition(WTDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE);
		querySpec.appendWhere(numberSC);
		querySpec.appendAnd();
		querySpec.appendWhere(latestIteration);
		QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
		// queryResult = (new LatestConfigSpec()).process(queryResult);
		while (queryResult.hasMoreElements()) {
			document = (WTDocument) queryResult.nextElement();
			String fileName = getDocFileName(document);  
			Debug.P(fileName);
			if (fileName != null) {
				if ("DWG".equals(fileName.substring(fileName.lastIndexOf(".") + 1)) || "dwg".equals(fileName.substring(fileName.lastIndexOf(".") + 1))) {
					return document;
				}
			}
		}
		return null;
	}
	
	
	/**
	 * get wtdocs by cadname,cadnumber
	 * @param cadname
	 * @param cadnumber
	 * @return
	 * @throws Exception
	 */
	public static ArrayList searchWTDocuments(String cadname, String cadnumber)
			throws Exception {

		if (StringUtil.isNullOrEmpty(cadname)
				&& StringUtil.isNullOrEmpty(cadnumber)) {
			return null;
		}

		QuerySpec qs = new QuerySpec(WTDocument.class);
		
		if (!StringUtil.isNullOrEmpty(cadnumber)) {
			SearchCondition sc = new SearchCondition(WTDocument.class,
					WTDocument.NUMBER, SearchCondition.LIKE, cadnumber+"%");
			qs.appendWhere(sc);
			qs.appendAnd();
		}

		if (!StringUtil.isNullOrEmpty(cadname)) {
			SearchCondition sc = new SearchCondition(WTDocument.class,
					WTDocument.NAME, SearchCondition.LIKE, cadname+"%");
			qs.appendWhere(sc);
			qs.appendAnd();
		}

		qs.appendWhere(new SearchCondition(new KeywordExpression(
				"A0.latestiterationinfo"), SearchCondition.EQUAL,
				new KeywordExpression("1")));
		qs.appendOrderBy(WTDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		ArrayList list = new ArrayList();
		List numbers = new ArrayList();
		while (qr.hasMoreElements()) {
			WTDocument doc = (WTDocument) qr.nextElement();
			if (!numbers.contains(doc.getNumber())) {
				numbers.add(doc.getNumber());
				list.add(doc);
			}
		}
		return list;
	}
	
	
	/**
	 * ��ȡ��������
	 * @author Eilaiwang
	 * @param object
	 * @return
	 * @return String
	 * @Description
	 */
	public static String getType(WTObject object){
		TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
		String type = ti.getTypename();
		return type;

		}
	
	
	/**
	 *��� �ο��ĵ�����
	 * get all DependsOn wtdoc of this
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static List getWTDocumentsByWTDocument(WTDocument doc)
			throws Exception {
		List docList = new ArrayList();
		QueryResult v = WTDocumentHelper.service.getDependsOnWTDocuments(doc,
				false);
		Vector vector = v.getObjectVector().getVector();
		for (int k = 0; k < vector.size(); k++) {
			WTDocumentDependencyLink docLink = (WTDocumentDependencyLink) vector
					.get(k);
			/*
			 * Object[] o = docLink.getAllObjects(); for(int m = 0;m <
			 * o.length;m++) { if(o[m] instanceof WTDocument) { WTDocument doc1
			 * = (WTDocument)o[m]; docList.add(doc1); } }
			 */
			docList.add(docLink.getDependsOn());
		}
		return docList;
	}
	
	
	/**
	 * get WTDoc by number and vesion
	 * @param num
	 * @param ver
	 * @return
	 * @throws Exception
	 */
	public static WTDocument getWTDocument(String num, String ver)
			throws Exception {
		WTDocument doc = null;
		QuerySpec qs = new QuerySpec(WTDocument.class);
		SearchCondition sc = new SearchCondition(WTDocument.class,
				WTDocument.NUMBER, SearchCondition.EQUAL, num);
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
		qs.appendOrderBy(WTDocument.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
			doc = (WTDocument) qr.nextElement();
		return doc;
	}
	
	/**
	 * �����ĵ������ѯ�ĵ�����
	 * 
	 * @param docNumber
	 *            �ĵ�����
	 * @return WTDocument �ĵ�����   
	 * @throws WTException
	 */
	public static WTDocument getDocByNumber(String docNumber) throws WTException {
		Debug.P(docNumber);
		WTDocument document = null;
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		SearchCondition numberSC = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, docNumber);
		SearchCondition latestIteration = new SearchCondition(WTDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE);
		querySpec.appendWhere(numberSC);
		querySpec.appendAnd();
		querySpec.appendWhere(latestIteration);
		QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
		// queryResult = (new LatestConfigSpec()).process(queryResult);
		Debug.P("Document----->"+queryResult.hasMoreElements());
		while (queryResult.hasMoreElements()) {
			document = (WTDocument) queryResult.nextElement();
			Debug.P(document);
			//String fileName = getDocFileName(document);  
			//if (fileName != null) {
				//if ("DWG".equals(fileName.substring(fileName.lastIndexOf(".") + 1)) || "dwg".equals(fileName.substring(fileName.lastIndexOf(".") + 1))) {
					return document;
				//}
			//}
		}
		return null;
	}

	/**
	 * �����ĵ��������
	 * 
	 * @param �������������
	 * @param �±���
	 * @throws WTException 
	 */

	public static void changeDcouementNumber(WTDocument document, String newNumber) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		WTUser currentuser = (WTUser) SessionHelper.manager.getPrincipal();
		SessionHelper.manager.setAdministrator();
		Identified identified = (Identified) document.getMaster();
		Transaction tx = null;
		try {
			tx = new Transaction();
			tx.start();
			WTDocumentMasterIdentity documentMasterIdentity = (WTDocumentMasterIdentity) identified.getIdentificationObject();
			if (newNumber != null)
				documentMasterIdentity.setNumber(newNumber);
			identified = IdentityHelper.service.changeIdentity(identified, documentMasterIdentity);
			document = (WTDocument) PersistenceHelper.manager.refresh(document);
		   tx.commit();
		   tx =null;
		} catch (WTException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} finally {
			if (tx != null)
				tx.rollback();
			SessionHelper.manager.setPrincipal(currentuser.getAuthenticationName());
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}

	/**
	 * ���ݶ����ȡ�ĵ��������ļ���
	 * 
	 * @param document
	 * @return
	 */

	public static String getDrwNumberByFileName(WTDocument document) {
		String drwNumber = null;
		try {
			FormatContentHolder formatContentHolder = (FormatContentHolder) ContentHelper.service.getContents(document);
			ContentItem contentItem = ContentHelper.getPrimary(formatContentHolder);
			ApplicationData applicationData = (ApplicationData) contentItem;
			if (applicationData == null) {
				return null;
			}
			String fileName = applicationData.getFileName();
			drwNumber = fileName.substring(0, fileName.indexOf("."));
		} catch (WTException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		return drwNumber;
	}

	public static WTDocument updateDocumentName(WTDocument document, String docName) throws ObjectNoLongerExistsException, WTException, WTPropertyVetoException {
		WTDocument doc = (WTDocument) PersistenceHelper.manager.refresh(document);
		WTDocumentMaster wtDocumentMaster = (WTDocumentMaster) doc.getMaster();
		WTDocumentMasterIdentity wtDocumentmasterIdentity = (WTDocumentMasterIdentity) wtDocumentMaster.getIdentificationObject();
		if (!wtDocumentmasterIdentity.getName().equals(docName)) {
			wtDocumentmasterIdentity.setName(docName);
			IdentityHelper.service.changeIdentity(wtDocumentMaster, wtDocumentmasterIdentity);
			doc = (WTDocument) PersistenceHelper.manager.refresh(doc);
		}
		return doc;
	}

	
	
	/**
	 * ������ĵ���������ص�ַ
	 * @param docNum �ĵ�����
	 * @return Map<String,ApplicationData> key:���ص�ַ value:�ĵ���
	 * @throws WTException
	 */
	public static Map<String,InputStream>  getPrimaryContentDownloadInfo(String docNum) throws WTException{

		Map<String,InputStream> result=new HashMap<String,InputStream>();
		 InputStream ins=null;
		 String primaryContentUrl=null;
		if(!StringUtils.isEmpty(docNum)){
	    	 Debug.P("----------->>Download Object_Num:"+docNum);
	    	 try {
	    		 Persistable object=GenericUtil.getObjectByNumber(docNum);
		    	 FormatContentHolder formatcontentholder = (FormatContentHolder) ContentHelper.service	.getContents((ContentHolder) object);
		    	 ContentItem contentitem = ContentHelper.getPrimary(formatcontentholder);  
	    	  	 if (contentitem != null && contentitem instanceof ApplicationData) {
						ApplicationData app = (ApplicationData) contentitem;
						 primaryContentUrl=ContentHelper.service.getDownloadURL(formatcontentholder, app).toExternalForm();
						 if(!StringUtils.isEmpty(primaryContentUrl)){
							 primaryContentUrl = UrlEncoder.encode(primaryContentUrl);
							 ins=ContentServerHelper.service.findContentStream(app);
						 }
					}
			} catch(Exception e){
				e.printStackTrace();
			}finally{
				if(ins!=null){
					try {
						ins.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	     }
		
   	   if(StringUtils.isEmpty(primaryContentUrl)){
   		    result.put(primaryContentUrl, ins);
   	   }
		     return result;
	}

	
	
	/**
	 * ���ݶ����ȡ�ĵ�������ȫ��
	 * 
	 * @param document
	 * @return
	 */

	public static String getDocFileName(WTDocument document) {
		String fileName = null;
		try {
			FormatContentHolder formatContentHolder = (FormatContentHolder) ContentHelper.service.getContents(document);
			ContentItem contentItem = ContentHelper.getPrimary(formatContentHolder);
			ApplicationData applicationData = (ApplicationData) contentItem;
			if (applicationData == null) {
				return null;
			}
			fileName = applicationData.getFileName();
		} catch (WTException e) {
			e.printStackTrace();
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	/**
	 * �ж�ͼֽ�Ƿ�Ϊ����ͼ
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isCheckLetter(String str) {
		boolean flag = false;
		List<String> strList = Arrays.asList("DRWCODE");
		if (strList.contains(str)) {
			flag = true;
		}
		return flag;
	}

	/**
	 * ����ͼֽ�ĵ���ָ��·��
	 * 
	 * @param doc
	 * @param path
	 * @param drwSize
	 * @throws WTException
	 * @throws PropertyVetoException
	 * @throws IOException
	 */
	public static void putDocTtoFolder(WTDocument doc, String path, String drwSize) throws WTException, PropertyVetoException, IOException {
		FormatContentHolder contentHolder = (FormatContentHolder) ContentHelper.service.getContents(doc);
		ContentItem contentItem = ContentHelper.getPrimary(contentHolder);
		if (contentItem instanceof ApplicationData) {
			ApplicationData applicationData = (ApplicationData) contentItem;
			String fileName = applicationData.getFileName();
			InputStream is = ContentServerHelper.service.findContentStream(applicationData);
		//	File file = new File(path + File.separator + drwSize + File.separator + fileName);
			File file = new File(path + File.separator + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int byteread = 0;
			while ((byteread = is.read(buffer)) != -1) {
				fos.write(buffer, 0, byteread);
			}
			is.close();
			fos.close();
		}
	}
	
	/**
	 * ����ĵ���������и�����Ϣ
	 * @param holder
	 * @throws WTException 
	 */
	public static List<ApplicationData> getAttachmentAppicationData(ContentHolder holder) throws WTException{
		List<ApplicationData> attacheList=new ArrayList<ApplicationData>();
		if(holder==null) return attacheList;
		QueryResult  result = ContentHelper.service.getContentsByRole(holder,ContentRoleType.SECONDARY);
	     if(result!=null&&result.size()>0){
	    	 Debug.P("------>>AttachementApp Size:"+result.size());
	    	 wt.content.ApplicationData appData = (wt.content.ApplicationData) result.nextElement();
	    	 attacheList.add(appData);
	     }
		
		return attacheList;
	}
	
	
	/**
	 * ������еĸ�������������
	 * @param holder
	 * @return Map (key:���������� ; value:����������)
	 * @throws Exception
	 */
	public static Map<String,String> getAllAttachementsDownloadURL(ContentHolder holder)throws Exception{
		Map<String,String> map=new HashMap<String,String>();
		if(holder!=null){
			FormatContentHolder formatcontentholder = (FormatContentHolder) ContentHelper.service.getContents(holder);
			List<ApplicationData> attachements=getAttachmentAppicationData(holder);//��ȡ�ĵ�����ĸ�����Ϣ
            for (ApplicationData app : attachements) {
            	  String  fileName=app.getFileName();
				 String app_url=ContentHelper.service.getDownloadURL(formatcontentholder, app).toExternalForm();
			      if(!StringUtils.isEmpty(app_url)){
			    	  app_url=UrlEncoder.encode(app_url);
			    	  map.put(fileName, app_url);
			      }
            }
		}
     		  return map;
	}
	
	

	/**
	 * ����dbf�ĵ���ָ��·��,�������ļ���
	 * 
	 * @param doc
	 * @param path
	 * @param drwSize
	 * @throws WTException
	 * @throws PropertyVetoException
	 * @throws IOException
	 */
	public static String putDocTtoFolder(WTDocument doc, String path) throws WTException, PropertyVetoException, IOException {
		FormatContentHolder contentHolder = (FormatContentHolder) ContentHelper.service.getContents(doc);
		ContentItem contentItem = ContentHelper.getPrimary(contentHolder);
		String fileName = null;
		if (contentItem instanceof ApplicationData) {
			ApplicationData applicationData = (ApplicationData) contentItem;
			fileName = applicationData.getFileName();
			InputStream is = ContentServerHelper.service.findContentStream(applicationData);
			File file = new File(path + File.separator + fileName);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[1024];
			int byteread = 0;
			while ((byteread = is.read(buffer)) != -1) {
				fos.write(buffer, 0, byteread);
			}
			is.close();
			fos.close();
		}
		return fileName;
	}

	/**
	 * �����ĵ�����
	 * 
	 * @param doc
	 * @param filepath
	 * @return
	 */
	public static WTDocument updateDocContent(WTDocument doc, String filepath) {
		try {
			if (doc == null)
				return null;
			
			doc = getWorkingCopyOfDoc(doc);

			 //������ĵ�����
			ContentHolder contentholder = (ContentHolder) doc;
			contentholder = ContentHelper.service.getContents(contentholder);
			List contentListForTarget = ContentHelper.getContentListAll(contentholder);
			for (int i = 0; i < contentListForTarget.size(); i++) {
				ContentItem contentItem = (ContentItem) contentListForTarget
						.get(i);
				if (contentItem.getRole().toString().equals("PRIMARY")) {
					ContentServerHelper.service.deleteContent(contentholder,contentItem);
					break;
				}
			}
			
			String fileformat = getFileFormat(filepath);
			ApplicationData applicationdata = ApplicationData.newApplicationData(doc);
			applicationdata.setRole(ContentRoleType.PRIMARY);
			applicationdata = ContentServerHelper.service.updateContent(doc, applicationdata, filepath);
			doc = (WTDocument) PersistenceServerHelper.manager.restore(doc);

			// set file format
			if (fileformat != null && !"".equals(fileformat)) {
				DataFormat df = ContentHelper.service.getFormatByName(fileformat);
				DataFormatReference dfr = DataFormatReference.newDataFormatReference(df);
				doc.setFormat(dfr);
			}
			if (doc != null) {
				if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(doc, wt.session.SessionHelper.manager.getPrincipal()))
					doc = (WTDocument) WorkInProgressHelper.service.checkin(doc, "add primary file");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

		return doc;
	}
	
	

	public static String getFileFormat(String filename) {
		String format = "";
		if (filename != null && !"".equals(filename)) {
			if (filename.endsWith(".xls"))
				format = "Microsoft Excel";
			else if (filename.endsWith(".doc"))
				format = "Microsoft Word";
			else if (filename.endsWith(".txt"))
				format = "Text File";
			else if (filename.endsWith(".gxk"))
				format = "GXK";
			else if (filename.endsWith(".pdf"))
				format = "PDF";
			else
				format = "DWG";
		}
		return format;
	}
	
	/**
	 *��ȡ��̨�ı������к�
	 * @return
	 * @throws NumberFormatException
	 * @throws WTException
	 */
	public static String getWTDocumentNumber() throws NumberFormatException, WTException{
		String bitFormat = "";
		for (int i = 0; i < 8; i++) {
			bitFormat = bitFormat + "0";
		}
		int seq = Integer.parseInt(wt.fc.PersistenceHelper.manager.getNextSequence("WTDOCUMENTID_seq"));
		java.text.DecimalFormat format = new java.text.DecimalFormat(bitFormat);
		String number = format.format(seq);
		return number;
	}
	

	public static WTDocument getWorkingCopyOfDoc(WTDocument doc) throws Exception {
		WTDocument workingdoc = null;

		if (!WorkInProgressHelper.isCheckedOut(doc)) {
			wt.folder.Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
			CheckoutLink checkoutlink = WorkInProgressHelper.service.checkout(doc, folder, "");
			workingdoc = (WTDocument) checkoutlink.getWorkingCopy();
		} else {
			if (!WorkInProgressHelper.isWorkingCopy(doc))
				workingdoc = (WTDocument) WorkInProgressHelper.service.workingCopyOf(doc);
			else
				workingdoc = doc;
		}
		return workingdoc;
	}
	
	
	
	//------------------------------Add by qiaokaikai----------------------------------------//
	
	/**
	 * ���ĵ�����ת���ĵ���
	 * @auth public759
	 * @Date 2014-10-17
	 */
	public static InputStream doc2is(WTDocument doc){
		InputStream ins=null;
		if(!SERVER){
			try {
				Class aclass[]={WTDocument.class};
				Object obj[]={doc};
				return (InputStream)RemoteMethodServer.getDefault().invoke("doc2is", DocUtils.class.getName(), null, aclass, obj);
			} catch (Exception e) {
				e.printStackTrace();
				Debug.P(e);
			}
		}else{
			try {
				//��ȡ�ĵ�����
				ContentHolder contentHolder=ContentHelper.service.getContents((ContentHolder)doc);
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
	 * ɾ���������ĵ��Ĺ�����ϵ
	 * @param part
	 * @param doc
	 * @throws Exception
	 */
	public static void deleteRelationLink(WTPart part,WTDocument doc)throws Exception{
		QueryResult qr=null;
		qr=StructHelper.service.navigateReferences(part,false);
		while(qr.hasMoreElements())
		{
			WTPartReferenceLink referenceLink=(WTPartReferenceLink)qr.nextElement();
			if(referenceLink.getRoleBObject().equals(doc.getMaster())){
				PersistenceServerHelper.manager.remove(referenceLink);
			}
		}
		qr=StructHelper.service.navigateDescribedBy(part, false);
		while(qr.hasMoreElements()){
			WTPartDescribeLink describeLink=(WTPartDescribeLink)qr.nextElement();
			if(describeLink.getRoleBObject().equals(doc)){
				PersistenceServerHelper.manager.remove(describeLink);
			}
		}
	}
	
	/**
	 * �����ĵ���Ų�ѯ�ĵ���Ϣ
	 * @param documentnumber
	 * @return
	 * @throws WTException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static WTDocument getWTDocumentByNumber(String documentnumber)
			throws WTException {
	   return  (WTDocument) DocUtils.getDocByNumber(documentnumber);
	}
	
	/**
	 * ������°汾
	 * @param master
	 * @return
	 * @throws WTException
	 */
	public static RevisionControlled getLatestObject(Master master)
			throws WTException {
		QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
		return (RevisionControlled) queryResult.nextElement();
	}
	

	
	
	
	/**
	 * �ĵ�������
	 * @param newDocName
	 * @param doc
	 * @return
	 */
	public static boolean documentRename(String newDocName, WTDocument doc) {
		Debug.P("newDocName = " + newDocName);
		boolean result = false;
		try {
			WTDocumentMaster docmaster = (WTDocumentMaster) doc.getMaster();
			WTDocumentMasterIdentity docmasteridentity = (WTDocumentMasterIdentity) docmaster
					.getIdentificationObject();
			docmasteridentity.setName(newDocName);
			docmaster = (WTDocumentMaster) IdentityHelper.service
					.changeIdentity(docmaster, docmasteridentity);
			String newName = docmaster.getName();
			if (newName.equals(newDocName)){
				result = true;
			}
		} catch (WTException e) {
			e.printStackTrace();
			Debug.P(e.toString());
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
			Debug.P(e.toString());
		}
		     return result;
	}


	
	
	/**
	 * ��������
	 * @param ch
	 * @param applicationdata
	 * @param fileName
	 * @return
	 * @throws WTException
	 * @throws java.beans.PropertyVetoException
	 * @throws java.io.IOException
	 */
	private static ApplicationData updateContent(ContentHolder ch, ApplicationData applicationdata, String fileName)
    		throws WTException, java.beans.PropertyVetoException, java.io.IOException {
        applicationdata = ContentServerHelper.service.updateContent(ch, applicationdata, fileName);
        return applicationdata;
    }
	
	
	/**
	 * ɾ������
	 * @param appDataPDF
	 * @throws WTException
	 */
	private static void deleteApplicationData(ApplicationData appDataPDF) throws WTException {
        PersistenceHelper.manager.delete(appDataPDF);
        return;
    }
	



	
	/**
	 * ɾ���ĵ��Լ��Ͳ����Ĳο���ϵ
	 * @param doc				�ĵ�����
	 * @throws WTException
	 */
	public static void deleteDependencyLink(WTDocument doc) throws WTException{
		QuerySpec qs = new QuerySpec();
		qs.setAdvancedQueryEnabled(true);
		qs.addClassList(WTDocumentDependencyLink.class, true);
		TableColumn column1 = new TableColumn("A0","IDA3B5");
		SearchCondition sc2 = new SearchCondition(column1,SearchCondition.EQUAL,new ConstantExpression(new Long(doc.getPersistInfo().getObjectIdentifier().getId())));
		qs.appendWhere(sc2);
		QueryResult qr1 = PersistenceHelper.manager.find(qs);
		while(qr1.hasMoreElements()){
			Object obj[] = (Object[]) qr1.nextElement();
			WTDocumentDependencyLink link = (WTDocumentDependencyLink) obj[0];
			WTDocument parentDoc = (WTDocument)link.getRoleAObject();
			PersistenceServerHelper.manager.remove(link);
			deleteDescribeLink(parentDoc);
			PersistenceHelper.manager.delete(parentDoc);
		}
		PersistenceHelper.manager.delete(doc);
	}
	
	/**
	 * ɾ���ĵ��Լ��Ͳ��������Ĺ�ϵ
	 * @param doc				�ĵ�����
	 * @throws WTException
	 */
	public static void deleteDescribeLink(WTDocument doc) throws WTException{
		QuerySpec qs = new QuerySpec();
		qs.setAdvancedQueryEnabled(true);
		qs.addClassList(WTPartDescribeLink.class, true);
		TableColumn column1 = new TableColumn("A0","IDA3B5");
		SearchCondition sc2 = new SearchCondition(column1,SearchCondition.EQUAL,new ConstantExpression(new Long(doc.getPersistInfo().getObjectIdentifier().getId())));
		qs.appendWhere(sc2);
		QueryResult qr1 = PersistenceHelper.manager.find(qs);
		while(qr1.hasMoreElements()){
			Object obj[] = (Object[]) qr1.nextElement();
			WTPartDescribeLink link = (WTPartDescribeLink) obj[0];
			PersistenceServerHelper.manager.remove(link);
		}
	}
	
	
	
	/**
	 * ���ĵ�������ظ���
	 * @param doc				WTDocument����
	 * @param fileRoute			�����ĵ�·��
	 * @throws Exception
	 */
	public void associateFile(WTDocument doc, String fileRoute) throws Exception{
		ContentHolder ch = (ContentHolder) doc;
		FormatContentHolder holder = (FormatContentHolder)ContentHelper.service.getContents(doc);
		ContentItem item = ContentHelper.getPrimary(holder);
		if(item!=null && item instanceof ApplicationData){
            ContentServerHelper.service.deleteContent(holder, item);
            holder = (WTDocument)ContentServerHelper.service.updateHolderFormat(holder);
        }
        ApplicationData ap = ApplicationData.newApplicationData(ch);
        ap.setRole(ContentRoleType.PRIMARY);
        ap = ContentServerHelper.service.updateContent(ch, ap, fileRoute);
        ap = (ApplicationData) PersistenceHelper.manager.save(ap);
        
	}
	
	
	/**
	 * �ĵ�������
	 * rename a wtdoc
	 * @param wtdoc
	 * @param newName
	 * @return
	 * @throws Exception
	 */
	public static WTDocument rename(WTDocument wtdoc, String newName)
			throws Exception {
		if (newName == null)
			return wtdoc;
		newName = newName.trim();
		if (!StringUtil.isNullOrEmpty(newName)
				&& !newName.equals(wtdoc.getName())) {
			Identified aIdentified = (Identified) wtdoc.getMaster();
			WTDocumentMasterIdentity aEPMDocumentMasterIdentity = (WTDocumentMasterIdentity) aIdentified
					.getIdentificationObject();
			aEPMDocumentMasterIdentity.setName(newName);
			IdentityHelper.service.changeIdentity(aIdentified,
					aEPMDocumentMasterIdentity);
			wtdoc = (WTDocument) PersistenceHelper.manager.refresh(wtdoc);
		}
		return wtdoc;
	}


	/**
	 *  �����ĵ���������
	 * @param doc  �ĵ�����
	 * @param  pmdoc PM�ĵ�����
	 * @param ibas �����Լ���
	 * @param lifecycleName ��������ģ������
	 * @return WTDocumnet
	 * @throws WTException
	 */
	public static WTDocument updateWTDocument(WTDocument doc,PMDocument pm_document,Map ibas) throws Exception{
		
		Transaction trans = null;
		String docName=pm_document.getCommonName();
		try {
			
			trans = new Transaction();
			trans.start();
			clearAllContent(doc);//�����ʷ�ĵ���Ϣ
			if (!doc.getName().equals(docName)) {//������Ʋ�һ���������
				rename(doc, docName);
			}
			
			if (ibas != null && !ibas.isEmpty()) {
				LWCUtil.setValue( doc,ibas);
			}

			//�޸Ķ����޸����ֶ�
          //GenericUtil.changeWTPrincipalField(modifyUser, doc, SET_MODIFIER);
		    PersistenceHelper.manager.refresh(doc);
			//�ϴ����ĵ��򸽼���Ϣ
		    String contentUrl=pm_document.getUrl();
			IFileProvider content = pm_document.getContent();//PM���ĵ���
			ByteArrayInputStream pins=null;
			ByteArrayInputStream sins=null;
			if(content!=null){
			String pname=content.getFileName();//PM���ļ�����
			//�ϴ����ĵ���Ϣ(ʵ����������ĵ�����)
				ByteArrayOutputStream bout=new ByteArrayOutputStream();
    			content.write(bout);
    			pins=new ByteArrayInputStream(bout.toByteArray());
    			doc=linkDocument(doc, pname, pins, "1", contentUrl);
			}
			//�ϴ�������Ϣ(Ŀǰδʵ��ɾ��������Ϣ)
			List<IFileProvider> attachments = pm_document.getAttachments();
			Debug.P("---->>>Attachement:"+attachments==null?"0":attachments.size());
			if(attachments!=null&&attachments.size()>0){
				for (IFileProvider provider : attachments) {
					ByteArrayOutputStream bsout=new ByteArrayOutputStream();
					provider.write(bsout);
					sins=new ByteArrayInputStream(bsout.toByteArray());
					doc=linkDocument(doc, provider.getFileName(), sins, "0", null);
				}
			}
			
			
			   
           //�����������
			addDownloadURL2PM(pm_document,doc);
			trans.commit();
			trans=null;
			return doc;
		}catch(IOException e){
			e.printStackTrace();
			throw new Exception("Windchill��ȡPMϵͳ�ĵ�("+docName+")�ļ����쳣!");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (trans != null){
				trans.rollback();
			}
		}
		     return doc;
	}
	   
	
	/**
	 * ��������ݺ͸������������ӵ�ַ
	 * @param pm_document PM�ĵ�����
	 * @param doc Windchill�ĵ�����
	 * @throws Exception
	 */
	  private static void addDownloadURL2PM(PMDocument pm_document, WTDocument doc)
				throws Exception {
			if(pm_document!=null&&doc!=null){
				Debug.P("------->>>>Add Dwn_URL2PM>>>>>>PM Doc:"+pm_document.get_id()+"   ;WC_DOCNum:"+doc.getNumber());
				IFileProvider content = pm_document.getContent();
				URLFileProvider fp = new URLFileProvider();
				if(content!=null){
					//д�����ļ�����
					String fileName  =pm_document.getContent().getFileName();
					String url = GenericUtil.getPrimaryContentUrl(doc);
					fp.setFileName(fileName);
					fp.setUrl(url);
					pm_document.setPLMContent(fp);
				}
					//д����
					List<IFileProvider> attachement = new ArrayList<IFileProvider>();
					Map<String, String> map = DocUtils.getAllAttachementsDownloadURL(doc);
					Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
					while(iterator.hasNext()){
						Entry<String, String> entry = iterator.next();
						String file_Name = entry.getKey();
						String url = entry.getValue();
						fp = new URLFileProvider();
						fp.setFileName(file_Name);
						fp.setUrl(url);
						attachement.add(fp);
					}
					pm_document.setPLMAttachments(attachement);
					Debug.P("----->>>>>Add DownLoadURL End");
				}

		}
	
	
	
	/**
	 * ����������ĵ�������������ĵ�����
	 * @param doc
	 * @throws PropertyVetoException 
	 */
	public static void clearContent(WTDocument doc)throws PropertyVetoException,WTException {
		 //������ĵ�����
		ContentHolder contentholder = (ContentHolder) doc;
		contentholder = ContentHelper.service.getContents(contentholder);
       if(contentholder!=null){
   		List contentListForTarget = ContentHelper.getContentListAll(contentholder);
   		if(contentListForTarget!=null){
   	   		for (int i = 0; i < contentListForTarget.size(); i++) {
   	   			ContentItem contentItem = (ContentItem) contentListForTarget.get(i);
   	   			if (contentItem.getRole().toString().equals("PRIMARY")) {
   	   				ContentServerHelper.service.deleteContent(contentholder,contentItem);
   	   				break;
   	   			}
   	   		 }
   		 }
       }
	}
	

	/**
	 * ������ĵ�����������Ϣ
	 * @param doc
	 * @throws Exception
	 */
	public static void clearAllContent(WTDocument doc)throws Exception{
		//�������(��������)
		FormatContentHolder holder = (FormatContentHolder) ContentHelper.service.getContents(doc);
		if(holder!=null){
	        //����������е�
	        Vector items = ContentHelper.getContentListAll(holder);
	        if(items!=null && items.size()>0) {
	        	Debug.P("-------->>>>Vector-Contents:"+items.size());
	            for (int i = 0; i < items.size(); i++) {
	                ContentItem item = (ContentItem) items.get(i);
	                if(item==null) continue;
//	                ApplicationData appData = (ApplicationData) item;
//					ContentServerHelper.service.deleteContent(holder, appData);
	                  ContentServerHelper.service.deleteContent(holder,item);
	            }
	        }
		}
	}
	
	
	
	/**
	 * �����������Ʋ�ѯ���Ͷ���
	 * @author blueswang
	 * @param objType
	 * @return
	 * @throws QueryException
	 * @return WTTypeDefinitionMaster
	 * @Description
	 */
	public static WTTypeDefinitionMaster getDfmDocumentType(String objType) throws QueryException {	
		QueryResult qr = new QueryResult();
		QuerySpec queryspec = new QuerySpec(WTTypeDefinitionMaster.class);
		SearchCondition sc = new SearchCondition(WTTypeDefinitionMaster.class,WTTypeDefinitionMaster.DISPLAY_NAME_KEY,SearchCondition.NOT_EQUAL, " ");
		SearchCondition sc1 = new SearchCondition(WTTypeDefinitionMaster.class,WTTypeDefinitionMaster.DELETED_ID,SearchCondition.IS_NULL, true);
		WTTypeDefinitionMaster wtm =null;
		try {
			queryspec.appendSearchCondition(sc);
			queryspec.appendAnd();
			queryspec.appendSearchCondition(sc1);
			qr = PersistenceHelper.manager.find(queryspec);
			while (qr.hasMoreElements()){
				wtm= (WTTypeDefinitionMaster) qr.nextElement();//������������ĵ�����
				//System.out.println(wtm.getHierarchyDisplayNameKey());
				//System.out.println(wtm.getDisplayNameKey());
				if (objType.equals(wtm.getHierarchyDisplayNameKey())){//���˵õ�������Ҫ���ĵ�����DFM
					Debug.P(wtm.getDescriptionKey() + "   ---    "+ wtm.getIntHid());
					return wtm;
				}
			}
		}catch(QueryException e) {
			e.printStackTrace();
		}catch (WTException e){
			e.printStackTrace();
		}
		return wtm;
	}

	

	
}
