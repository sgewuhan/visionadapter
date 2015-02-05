package ext.tmt.utils;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.conflict.ConflictResolution;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.content.StandardContentService;
import wt.content.URLData;
import wt.dataops.containermove.ContainerMoveHelper;
import wt.dataops.delete.DeleteTask;
import wt.doc.DocumentMaster;
import wt.doc.WTDocument;
import wt.doc.WTDocumentDependencyLink;
import wt.doc.WTDocumentHelper;
import wt.doc.WTDocumentMaster;
import wt.doc.WTDocumentMasterIdentity;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMApplicationType;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMContextHelper;
import wt.epm.EPMDocConfigSpec;
import wt.epm.EPMDocSubType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentHelper;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.epm.EPMDocumentType;
import wt.epm.build.EPMBuildHistory;
import wt.epm.build.EPMBuildLinksDelegate;
import wt.epm.build.EPMBuildRule;
import wt.epm.structure.EPMDescribeLink;
import wt.epm.structure.EPMMemberLink;
import wt.epm.structure.EPMReferenceLink;
import wt.epm.structure.EPMStructureHelper;
import wt.epm.util.EPMHelper;
import wt.epm.workspaces.EPMPopulateRule;
import wt.epm.workspaces.EPMWorkspace;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.facade.dataops.DeleteHelper;
import wt.fc.BinaryLink;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.ObjectIdentifier;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTKeyedHashMap;
import wt.fc.collections.WTKeyedMap;
import wt.fc.collections.WTSet;
import wt.fc.collections.WTValuedHashMap;
import wt.fc.collections.WTValuedMap;
import wt.folder.CabinetBased;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.FolderNotFoundException;
import wt.httpgw.GatewayServletHelper;
import wt.httpgw.GatewayURL;
import wt.httpgw.URLFactory;
import wt.inf.container.ExchangeContainer;
import wt.inf.container.LookupSpec;
import wt.inf.container.OrgContainer;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.container.WTContainerTemplate;
import wt.inf.library.WTLibrary;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.inf.template.ContainerTemplateHelper;
import wt.inf.template.DefaultWTContainerTemplate;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleServerHelper;
import wt.lifecycle.State;
import wt.method.MethodContext;
import wt.method.MethodServer;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.occurrence.OccurrenceHelper;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTOrganization;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.ownership.OwnershipHelper;
import wt.part.LineNumber;
import wt.part.PartUsesOccurrence;
import wt.part.Quantity;
import wt.part.QuantityUnit;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pdmlink.PDMLinkProduct;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.project.Role;
import wt.query.ConstantExpression;
import wt.query.KeywordExpression;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.representation.PublishedContentLink;
import wt.representation.Representable;
import wt.representation.Representation;
import wt.series.HarvardSeries;
import wt.series.MultilevelSeries;
import wt.series.Series;
import wt.series.SeriesException;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionMgr;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.IterationIdentifier;
import wt.vc.Mastered;
import wt.vc.VersionControlConflictType;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlResolutionType;
import wt.vc.VersionControlServerHelper;
import wt.vc.VersionIdentifier;
import wt.vc.Versioned;
import wt.vc._IterationInfo;
import wt.vc.baseline.Baseline;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.ManagedBaseline;
import wt.vc.struct.IteratedDescribeLink;
import wt.vc.struct.IteratedReferenceLink;
import wt.vc.struct.IteratedUsageLink;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressState;
import wt.vc.wip.Workable;
import wt.viewmarkup.DerivedImage;

import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;
import com.ptc.netmarkets.search.rendering.guicomponents.VersionIterationComponent;
import com.ptc.windchill.cadx.common.preference.EpdParams;
import com.ptc.windchill.cadx.common.util.WorkspaceConfigSpecUtilities;
import com.ptc.windchill.cadx.common.util.WorkspaceUtilities;
import com.ptc.windchill.enterprise.note.commands.NoteServiceCommand;
import com.ptc.windchill.wp.delivery.export.EPMBuildHistoryParentLinksDependencyProcessor;
import com.ptc.wvs.common.ui.VisualizationHelper;
import com.ptc.wvs.server.util.WVSContentHelper;
import com.sun.naming.internal.VersionHelper;

import ext.tmt.WC2PM.WCToPMHelper;

public class GenericUtil implements RemoteAccess {

	public static String DEFAULT_CHARSET = "UTF-8";
   
	public static ReferenceFactory REF_FACTORY = new ReferenceFactory();
	public static Locale LOCALE = null;
	// String oid = GenericUtil.REF_FACTORY.getReferenceString(obj);
	// Object obj = GenericUtil.REF_FACTORY.getReference(oid).getObject();
	public static String CONTAINER_ROOT_FOLDER = "/Default";

	public static String ROLE_LIBRARY_MANAGER = "LIBRARY MANAGER";

	public static String CADDOCTYPE_PUB_GRAPHIC = "PUB_GRAPHIC";

	public static String CODEBASE_LOCATION = null;
	
	private static String METHOD_CREATOR="setCreator";
	private static String METHOD_MODIFIER="setModifier";
	
	

	public static List BOOK_SUBFOLDERS = null;

	public static String HOSTNAME = null;

	public static URLFactory URL_FACTORY = null;

	static {
		try {
			WTProperties wtproperties = WTProperties.getLocalProperties();
			LOCALE = WTContext.getContext().getLocale();
			HOSTNAME = wtproperties.getProperty("java.rmi.server.hostname");
			CODEBASE_LOCATION = wtproperties
					.getProperty("wt.codebase.location");
			// String templateFolderName =
			// wtproperties.getProperty("ext.generic.folder.templateFolderName");
			// BOOK_SUBFOLDERS = getOptions(templateFolderName);
			URL_FACTORY = new URLFactory();
		} catch (Exception ex) {
			System.out.println("Error: GenericUtil initializing error!");
			ex.printStackTrace();
		}
	}

	/**
	 * create ReferenceLink between EPM Doc and WTDoc
	 * 
	 * @param doc1
	 * @param doc2
	 * @throws Exception
	 */
//	public static void createEPMReferenceLink(EPMDocument doc1, WTDocument doc2)
//			throws Exception {
//		if (doc1 == null || doc2 == null)
//			return;
//		EPMReferenceLink link = EPMReferenceLink.newEPMReferenceLink(doc1,
//				doc2);
//		link.setReferenceType(EPMReferenceType.toEPMReferenceType("INTERNAL"));
//		PersistenceServerHelper.manager.insert(link);
//	}

	/**
	 * check any Reference between EPMDoc and WTDoc
	 * 
	 * @param epmdoc
	 * @param wtdoc
	 * @return
	 * @throws Exception
	 */
	public static boolean isReference(EPMDocument epmdoc, WTDocument wtdoc)
			throws Exception {
		if (epmdoc == null || wtdoc == null) {
			return false;
		}
		
	
		wt.fc.QueryResult qr = wt.epm.structure.EPMStructureHelper.service
				.navigateReferences(epmdoc, null, true);
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof WTDocumentMaster) {
				WTDocumentMaster doc = (WTDocumentMaster) obj;
				if (wtdoc.getNumber().equals(doc.getNumber())) {
					return true;
				}
			}

		}
		return false;
	}
	/**
	 * get LangZip state by BookMap
	 * @param epmdoc BookMap 
	 * @param language_code language code
	 * @return
	 * @throws Exception
	 */
	public static String getReferenceLangZipStatus(EPMDocument epmdoc,
			String language_code) throws Exception {
		String rt = null;
		if (epmdoc == null) {
			return null;
		}
		String langDocNumber = epmdoc.getNumber() + "_" + language_code;
		System.out.println("begin Find Lang Doc For Lang:" + language_code
				+ " MapDoc:" + epmdoc);

		wt.fc.QueryResult qr = wt.epm.structure.EPMStructureHelper.service
				.navigateReferences(epmdoc, null, true);
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof WTDocumentMaster) {
				WTDocumentMaster doc = (WTDocumentMaster) obj;
				if (doc.getNumber().equalsIgnoreCase(langDocNumber)) {
					System.out.println("Find Lang Doc For Lang:"
							+ language_code + " Doc:" + doc);
					WTDocument document=(WTDocument)GenericUtil.getObjectByNumber(doc.getNumber());
					rt=document.getLifeCycleState().getDisplay();
					break;
				}
			}
		}
		return rt;
	}
	/**
	 * get Options from a string
	 * @param confFile
	 * @return
	 * @throws Exception
	 */
	public static List getOptions(String confFile) throws Exception {
		List options = new ArrayList();
		BufferedReader in = new BufferedReader(new FileReader(confFile));
		while (true) {
			String line = in.readLine();
			if (line == null)
				break;
			if (line.startsWith("#"))
				continue;
			options.add(line);
		}
		return options;
	}

	/**
	 * get properties from file
	 * @param propsFile file name
	 * @return
	 * @throws Exception
	 */
	public static Properties getProperties(String propsFile) throws Exception {
		Properties props = new Properties();
		FileReader fr = new FileReader(propsFile);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		while (true) {
			line = br.readLine();
			if (line == null)
				break;
			line = line.trim();
			if (line.startsWith("#"))
				continue;
			int p = line.indexOf("=");
			if (p > 0)
				props.setProperty(line.substring(0, p), line.substring(p + 1));
		}
		fr.close();
		br.close();
		return props;
	}
	/**
	 * get all Container,include WTLibrary and PDMLinkProduct
	 * @return
	 * @throws Exception
	 */
	public static Vector getWTContainer() throws Exception {
		Vector v = new Vector();
		WTContainer wtc = null;
		QuerySpec qs = new QuerySpec(WTLibrary.class);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements())
			v.addElement(qr.nextElement());
		qs = new QuerySpec(PDMLinkProduct.class);
		qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements())
			v.addElement(qr.nextElement());
		return v;
	}
	
	
	/**
	 *通过对象名 获得对象Masters
	 * @param objclass
	 * @param objectname
	 * @return Persistable
	 * @throws WTException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Persistable getObjectByName(Class objclass, String objectname)
			throws WTException, InstantiationException, IllegalAccessException {
		Persistable persistable = null;
		QuerySpec criteria = new QuerySpec(objclass);
		// caseSensitive
		criteria.appendSearchCondition(new SearchCondition(objclass, "name",
				SearchCondition.EQUAL, objectname, true)); 
		QueryResult queryresult = PersistenceHelper.manager.find(criteria);
		if (queryresult.hasMoreElements())
			persistable = (Persistable) queryresult.nextElement();
		    return persistable;
	}
	
	/**
	 * 设置对象生命周期状态
	 * 
	 * @param obj
	 * @param stateName
	 */

	public static void setLifeCycleState(LifeCycleManaged obj, String stateName) {
		if (obj == null || stateName == null) {
			return;
		}
		LookupSpec ls=null;
		State toState = State.toState(stateName);
		if (toState == null) {
			return;
		}
		WTPrincipal administrator = null;
		try {
			administrator = SessionHelper.manager.getAdministrator();
		} catch (WTException e) {
			e.printStackTrace();
		}
		WTPrincipal previous = SessionContext
				.setEffectivePrincipal(administrator);
		try {
			try {
				LifeCycleHelper.service.setLifeCycleState(obj, toState);
			} catch (WTException e) {
				e.printStackTrace();
			}
		} finally {
			SessionContext.setEffectivePrincipal(previous);
		}
	}

	
/**
 * 根据编号查询(包含WTDocument,WTPart,EPMDocument)
 * @param num 对象编号
 * @return
 * @throws WTException
 */
	public static Persistable getObjectByNumber(String num) throws WTException{
		
		
		Persistable persistable=null;
		Persistable master_persistable =null;
		if(StringUtils.isEmpty(num)) return persistable;
	     
		//EPM文档
		QuerySpec criteria = new QuerySpec(EPMDocumentMaster.class);
		criteria.setAdvancedQueryEnabled(true);
		criteria.appendSearchCondition(new SearchCondition(EPMDocumentMaster.class, "number",SearchCondition.EQUAL, num, true));
		QueryResult result_epm = PersistenceHelper.manager.find(criteria);
		if(result_epm!=null&&result_epm.size()>0){//图档对象
			master_persistable = (Persistable) result_epm.nextElement();
		}else {
				 criteria = new QuerySpec(WTDocumentMaster.class);
				criteria.setAdvancedQueryEnabled(true);
				criteria.appendSearchCondition(new SearchCondition(WTDocumentMaster.class, "number",SearchCondition.EQUAL, num, true));
				QueryResult result_doc = PersistenceHelper.manager.find(criteria);
				if(result_doc!=null&&result_doc.size()>0){//文档对象
					 master_persistable = (Persistable) result_doc.nextElement();
				}
				else{//部件对象
					criteria = new QuerySpec(WTPartMaster.class);
					criteria.setAdvancedQueryEnabled(true);
					criteria.appendSearchCondition(new SearchCondition(WTPartMaster.class, "number",SearchCondition.EQUAL, num, true));
					QueryResult result_part = PersistenceHelper.manager.find(criteria);
					if(result_part!=null&&result_part.size()>0){
						master_persistable = (Persistable) result_part.nextElement();
					}
				}
			}
			if(master_persistable!=null){
				
				persistable=getLatestObject((Master)master_persistable);
			}
	
	        	return persistable;
	}
	
	
	/**
	 * copy f1 to f2
	 * @param f1
	 * @param f2
	 * @throws Exception
	 */
	public static void copyFile(String f1, String f2) throws Exception {
		FileInputStream is = new FileInputStream(f1);
		FileOutputStream f = new FileOutputStream(f2);
		byte[] b = new byte[1024];
		int m = is.read(b);
		while (m != -1) {
			f.write(b, 0, m);
			m = is.read(b);
		}
		f.close();
	}
	
	

	/**
	 * 通过容器名查询容器对象(存储库对象,产品库对象)
	 * get Container by Name
	 * @param name
	 * @return WTContainer
	 * @throws Exception
	 */
	public static WTContainer getWTContainerByName(String containerName) throws Exception {
		WTContainer wtc = null;
		QuerySpec qs = new QuerySpec(WTLibrary.class);
		SearchCondition sc = new SearchCondition(WTLibrary.class,
				WTLibrary.NAME, SearchCondition.EQUAL, containerName);
		qs.appendWhere(sc);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.size() > 0){//存储库容器
			wtc = (WTContainer) qr.nextElement();
		}else {//产品库容器
			qs = new QuerySpec(PDMLinkProduct.class);
			sc = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME,
					SearchCondition.EQUAL, containerName);
			qs.appendWhere(sc);
			qr = PersistenceHelper.manager.find(qs);
			if (qr.size() > 0)
				wtc = (WTContainer) qr.nextElement();
		}
		    
		return wtc;
	}
	
	

	
	
    /**
     * 新建一个Library容器, 使用常规存储库模板, 如没有, 则创建默认存储库模板
     * @param orgName        组织名称
     * @param libName           容器名称
     * @param libDescription    容器描述
     * @return                  容器的reference
     * @throws Exception
     */
    public static WTLibrary createLibrary(String orgName,String libName, String libDescription)
            throws Exception {
        return createLibrary(orgName,libName, libDescription, "");
    }
    
    
    /**
     * 新建一个Library容器, 需指定容器模板名称
     * @param orgName       指定的组织名
     * @param libName           容器名称
     * @param libDescription    容器描述
     * @param templatename      容器模板名称
     * @return                  容器的reference
     * @throws Exception
     */
    public static WTLibrary createLibrary(String orgName,String libName, String libDescription, String templateName) throws Exception {
//        if (!RemoteMethodServer.ServerFlag) {
//            String method = "createLibrary";
//            String klass = GenericUtil.class.getName();
//            Class[] types = { String.class,String.class, String.class, String.class };
//            Object[] values = {orgName,libName, libDescription, templateName };
//            return (WTLibrary) RemoteMethodServer.getDefault().invoke(method, klass, null,types, values);
//        }else{
        	//获取组织容器
//          WTUser me = (WTUser) SessionHelper.manager.getPrincipal();
//          WTOrganization org = OrganizationServicesHelper.manager.getOrganization(me);
//          WTContainerRef orgContainerRef = WTContainerHelper.service.getOrgContainerRef(org);
          WTOrganization target_org=OrganizationServicesHelper.manager.getOrganization(orgName, ((ExchangeContainer) WTContainerHelper.getExchangeRef().getContainer()).getContextProvider());
          if(target_org==null){
         	 Debug.P("-------->>>组织("+orgName+")不存在,无法创建!");
         	 return null;
          }
     	    OrgContainer orgContainer=WTContainerHelper.service.getOrgContainer(target_org);
           
          WTLibrary lib = WTLibrary.newWTLibrary();
//          WTContainerRef libRef = null;
//          boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
//          Vector orgAdmins = findOrgAdmins(target_org);//获取这个用户所在组织的管理员
//          if (orgAdmins == null || orgAdmins.size() == 0)
//              SessionHelper.manager.setAdministrator();
//          else {
//              WTUser admin = (WTUser) orgAdmins.get(0);
//              SessionHelper.manager.setAuthenticatedPrincipal(admin.getAuthenticationName());
//          }
       
              lib.setName(libName);
              if (libDescription != null)
              lib.setDescription(libDescription);
              //定义容器模板
              WTContainerTemplate containerTemplate = ContainerTemplateHelper.service.getContainerTemplate(WTContainerHelper.getExchangeRef(), templateName, WTLibrary.class);
              if(containerTemplate==null){//如果为空则取默认的模板
              	containerTemplate=getEmptyTemplate(WTLibrary.class,orgContainer.getContainerReference());
              }
              //            WTContainerTemplateRef containerTemplateRef = ContainerTemplateHelper.service.getContainerTemplateRef(
//              orgContainerRef, templateName, WTLibrary.class);
//              if (containerTemplateRef == null) {
//                  WTContainerTemplate containerTemplate = DefaultWTContainerTemplate.newDefaultWTContainerTemplate(
//                          "默认存储库模板", WTLibrary.class.getName());//这里的存储库名称：“默认存储库模板”
//                  containerTemplate = ContainerTemplateHelper.service.createContainerTemplate(
//                          orgContainerRef, containerTemplate);
//                  containerTemplateRef = WTContainerTemplateRef.newWTContainerTemplateRef(containerTemplate);
//              }
              
              lib.setContainerTemplate(containerTemplate);
              WTContainerHelper.setPrivateAccess(lib, false);
              lib.setContainer(orgContainer);
              lib = (WTLibrary) WTContainerHelper.service.create(lib);
              lib = (WTLibrary) WTContainerHelper.service.makePublic(lib);
              return lib;
//        }
          
    }
    
    /**
     * 创建产品库
     * @param orgName 组织名称
     * @param productName 产品库名称
     * @param templateName 模板名称
     * @param wtproduct 产品
     * @return  PDMLinkProduct
     * @throws WTException
     * @throws WTPropertyVetoException
     */
    public static PDMLinkProduct createPDMLinkProduct(String orgName,String productName,String templateName,WTPart wtproduct)throws WTException,WTPropertyVetoException{
    	PDMLinkProduct result=null;
    	if(wtproduct==null){
    		result=PDMLinkProduct.newPDMLinkProduct();
    	}else{
    		result=PDMLinkProduct.newPDMLinkProduct(wtproduct);//初始化产品库名称
    	}
//    	Enumeration orgs = OrganizationServicesHelper.manager.findLikeOrganizations(WTOrganization.NAME, orgName, ((ExchangeContainer) WTContainerHelper.getExchangeRef().getContainer()).getContextProvider());
//    	WTUser me = (WTUser) SessionHelper.manager.getPrincipal();
//        WTOrganization org = OrganizationServicesHelper.manager.getOrganization(me);
    	 //通过名称获取组织容器
    	WTOrganization target_org=OrganizationServicesHelper.manager.getOrganization(orgName, ((ExchangeContainer) WTContainerHelper.getExchangeRef().getContainer()).getContextProvider());
         if(target_org==null){
        	 Debug.P("-------->>>组织("+orgName+")不存在,无法创建!");
        	 return null;
         }
    	OrgContainer orgContainer=WTContainerHelper.service.getOrgContainer(target_org);
        result.setContainer(orgContainer);
        result.setName(productName);
        
        //定义容器
        WTContainerTemplate template=ContainerTemplateHelper.service.getContainerTemplate(WTContainerHelper.getExchangeRef(), templateName, PDMLinkProduct.class);
        
        if(template==null){//如果为空则取默认的模板
        	template=getEmptyTemplate(PDMLinkProduct.class,orgContainer.getContainerReference());
        }
        
        result.setContainerTemplate(template);
        WTContainerHelper.setPrivateAccess(result, false);
        
        result=(PDMLinkProduct)ContainerTeamHelper.setSendInvitations(result, false);
        result=(PDMLinkProduct)WTContainerHelper.service.create(result);
        
        return result;
    }
    
    
    
    /**
     * 获取默认的模板容器
     * @param containerClass
     * @param containerRef
     * @return
     * @throws WTException 
     * @throws WTPropertyVetoException 
     */
    public static WTContainerTemplate getEmptyTemplate(Class<?> containerClass,WTContainerRef containerRef) throws WTException, WTPropertyVetoException{
    	if(containerRef==null){
    		containerRef=WTContainerHelper.getExchangeRef();
    	}
    	WTContainerTemplate  result=ContainerTemplateHelper.service.getContainerTemplate(containerRef,"Empty",containerClass);
    	if(result==null){
    		result=createTemplate("Empty",containerClass,containerRef,false);
    	}
    	   return result;
    }
    
    /**
     * 创建空模板
     * @param name
     * @param containerClass
     * @param parentRef
     * @param isEnable
     * @return
     * @throws WTException 
     * @throws WTPropertyVetoException 
     */
    private static  WTContainerTemplate createTemplate(String name,Class<?> containerClass,WTContainerRef parentRef,boolean isEnable) throws WTException, WTPropertyVetoException{
        
    	DefaultWTContainerTemplate result=DefaultWTContainerTemplate.newDefaultWTContainerTemplate();
    	result.setName(name);
    	result.setContainerClassName(containerClass.getName());
    	result.setEnabled(isEnable);
    	return ContainerTemplateHelper.service.createContainerTemplate(parentRef, result);
    }
    
    
	

	
	/**
	 * check the WTPrincipal is member of the role in WTContainer
	 * @param prin
	 * @param con
	 * @param role
	 * @return
	 * @throws WTException
	 */
	public static boolean isRoleHolder(WTPrincipal prin, WTContainer con,
			Role role) throws WTException {
		List list = ContainerTeamHelper.service.getContainerTeam(
				(ContainerTeamManaged) con).getAllPrincipalsForTarget(role);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				WTPrincipal wtp = ((WTPrincipalReference) list.get(i))
						.getPrincipal();
				if (prin.equals(wtp))
					return true;
			}
		}
		return false;
	}
	/**
	 * check the WTPrincipal is member of the role in Team
	 * @param prin
	 * @param team
	 * @param role
	 * @return
	 * @throws WTException
	 */
	public static boolean isRoleHolder(WTPrincipal prin, Team team, Role role)
			throws WTException {
		Enumeration enumer = team.getPrincipalTarget(role);
		while (enumer != null && enumer.hasMoreElements()) {
			WTPrincipal wtp = ((WTPrincipalReference) enumer.nextElement())
					.getPrincipal();
			if (prin.equals(wtp))
				return true;
		}
		return false;
	}
	/**
	 * check the WTPrincipal is member of the role in WTContainer
	 * @param con
	 * @param role
	 * @return
	 * @throws WTException
	 */
	public static boolean isRoleHolder(WTContainer con, String role)
			throws WTException {
		return isRoleHolder(SessionMgr.getPrincipal(), con, Role.toRole(role));
	}
	
	

	/**
	 * check is PartCentric Processing
	 * @return
	 */
	public static boolean isPartCentricProcessing() {
		boolean partCentricProcessing = false;
		String keyValue = EpdParams.getEpdParameter("partCentric",
				"cadxhtmlui", "newworkspace", "false");
		if (keyValue != null && keyValue.equalsIgnoreCase("true"))
			partCentricProcessing = true;
		return partCentricProcessing;
	}
	/**
	 * create a workspace in a continer
	 * @param wsname
	 * @param container
	 * @return
	 * @throws Exception
	 */
	public static EPMWorkspace createWorkspace(String wsname,
			WTContainer container) throws Exception {
		WTPartConfigSpec partConfigSpec = WorkspaceConfigSpecUtilities
				.createWTPartConfigSpec(container);
		EPMDocConfigSpec docConfigSpec = WorkspaceConfigSpecUtilities
				.createEPMDocConfigSpec(container);
		EPMWorkspace workspace = EPMWorkspace.newEPMWorkspace(wsname,
				SessionMgr.getPrincipal(), null, partConfigSpec, docConfigSpec,
				container);
		workspace.setContainer(container);
		workspace.setPopulateRule(EPMPopulateRule.NONE);
		EPMContextHelper.setApplication(EPMApplicationType
				.toEPMApplicationType("EPM"));
		workspace.setPartCentricProcessing(isPartCentricProcessing());
		workspace = (EPMWorkspace) PersistenceHelper.manager.save(workspace);
		return workspace;
	}
	/**
	 * get  a workspace in a continer
	 * @param con
	 * @return
	 * @throws Exception
	 */
	public static EPMWorkspace getWorkspace(WTContainer con) throws Exception {
		EPMWorkspace[] workspaces = WorkspaceUtilities
				.getWorkspacesForCurrentUser(con);
		EPMWorkspace ws = null;
		if (workspaces != null && workspaces.length > 0) {
			ws = workspaces[0];
		} else {
			int i = 0;
			String wsnamePrefix = con.getName() + "-"
					+ SessionMgr.getPrincipal().getName();
			String wsname = null;
			while (true) {
				i++;
				if (i == 1)
					wsname = wsnamePrefix;
				else
					wsname = wsnamePrefix + "-" + i;
				ws = WorkspaceUtilities.getWorkspace(wsname);
				if (ws == null) {
					ws = GenericUtil.createWorkspace(wsname, con);
					break;
				} else {
					continue;
				}
			}
		}
		return ws;
	}
	/**
	 * revise WTDocument
	 * @param doc
	 * @return
	 * @throws WTException
	 */
	public static WTDocument autoReviseWTDocument(WTDocument doc)
			throws WTException {
		boolean flag = false;
		WTDocument newDoc = null;
		try {
			WTDocument newVersionDoc = (WTDocument) wt.vc.VersionControlHelper.service
					.newVersion((wt.vc.Versioned) doc);
			String version = ((wt.enterprise.RevisionControlled) newVersionDoc)
					.getVersionIdentifier().getValue();
			newDoc = DocUtils.getWTDocument(doc.getNumber(), version);
			if (newDoc == null) {

				FolderHelper.assignLocation(newVersionDoc, FolderHelper.service
						.getFolder(doc));
				newDoc = (WTDocument) PersistenceHelper.manager
						.store(newVersionDoc);
			}
		} catch (Exception e) {
			System.out.println("autoReviseWTDocument Error:" + e.toString());
			e.printStackTrace();
		}
		return newDoc;
	}
	
	/**
	 * check in a object with comments
	 * @param w
	 * @param note 备注
	 * @param WTUser 用户对象
	 * @return
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	public static Workable checkin(Workable w, String note,String userName)
			throws WTPropertyVetoException, WTException {
		note = note + "," + (new Date()).toString() + "," + userName;
		w = (Workable) PersistenceHelper.manager.refresh(w);
		if (WorkInProgressHelper.isCheckedOut(w))
			w = WorkInProgressHelper.service.checkin(w, note);
		return w;
	}
	/**
	 * check in a object
	 * @param w
	 * @return
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	public static Workable checkin(Workable w,String username) throws WTPropertyVetoException,
			WTException {
		return checkin(w, "AutoCheckIn",username);
	}
	/**
	 * check out a object
	 * @param w
	 * @return
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	public static Workable checkout(Workable w) throws WTPropertyVetoException,
			WTException {
		return checkout(w, "AutoCheckOut");
	}
	/**
	 * check out a object with comments
	 * @param w
	 * @param note
	 * @return
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	public static Workable checkout(Workable w, String note)
			throws WTPropertyVetoException, WTException {
		if (w instanceof Iterated) {
			Iterated it = (Iterated) w;
			w = (Workable) wt.vc.VersionControlHelper.service
					.getLatestIteration(it, false);
		}
		Workable wk = null;
		boolean checkoutFlag = WorkInProgressHelper.isCheckedOut(w);
		if (checkoutFlag) {
			if (!WorkInProgressHelper.isWorkingCopy(w))
				wk = WorkInProgressHelper.service.workingCopyOf(w);
			else
				wk = w;
		} else {
			Folder myFolder = WorkInProgressHelper.service.getCheckoutFolder();
			CheckoutLink checkout_lnk = WorkInProgressHelper.service.checkout(
					w, myFolder, note);
			wk = checkout_lnk.getWorkingCopy();
		}
		return wk;
	}
	
	/**
	 * get a WTPart by numer,version,view
	 * @param num
	 * @param ver
	 * @param viewName
	 * @return
	 * @throws Exception
	 */
	public static WTPart getPart(String num, String ver, String viewName)
			throws Exception {
		if (viewName.equals(""))
			viewName = "Design";
		View view = ViewHelper.service.getView(viewName);
		WTPart part = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,
				SearchCondition.EQUAL, num);
		qs.appendWhere(sc);
		sc = new SearchCondition(WTPart.class, "view.key.id",
				SearchCondition.EQUAL, view.getPersistInfo()
						.getObjectIdentifier().getId());
		qs.appendAnd();
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
		qs.appendOrderBy(WTPart.class, "thePersistInfo.createStamp", true);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements())
			part = (WTPart) qr.nextElement();
		return part;
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
	
	/***
	 * 上传附件
	 * set a file as PRIMARY Content
	 * @param ch
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static ContentHolder attachAppData(ContentHolder ch, ApplicationData data,String category)
			throws Exception {
		Transaction trans = null;
		try {
			trans = new Transaction();
			trans.start();
			ch = (ContentHolder) PersistenceHelper.manager.refresh(ch);
			//query appdata
			QueryResult qr = ContentHelper.service.getContentsByRole(ch,ContentRoleType.SECONDARY);
			System.out.println("attachAppData.size:"+qr.size());
			while(qr.hasMoreElements()){
				wt.content.ApplicationData appData = (wt.content.ApplicationData) qr.nextElement();
//				System.out.println("category:"+category+" - appData.getCategory():"+appData.getCategory());
				if(appData.getCategory()!=null && appData.getCategory().equals( category )){
					ContentServerHelper.service.deleteContent(ch,appData);
					System.out.println("delete attachAppData ."+appData.getCategory());
				}
			}
			System.out.println("Create new APPData.");
			//create new appdata
			ApplicationData appData = ApplicationData.newApplicationData(ch);
			appData.setRole(ContentRoleType.SECONDARY);
			appData.setFileName(data.getFileName());
			appData.setCategory(category);
			appData.setUploadedFromPath("");
			InputStream is = ContentServerHelper.service.findContentStream(data);
			appData = ContentServerHelper.service.updateContent(
					(ContentHolder) ch, appData, is);
			is.close();
			trans.commit();
			System.out.println("create attachAppData end.");
		} catch (Exception e) {
			if (trans != null)
				trans.rollback();
			e.printStackTrace();
			throw e;
		}
		return ch;
	}
	
	/**
	 * 获取对象可视化的超链接地址
	 * @param object
	 * @return
	 * @throws Exception
	 */
	public static String getViewContentHrefUrl(Persistable object) throws Exception{
		  String result=null;
		try {
			 if(object!=null){
				  object = (ContentHolder) PersistenceHelper.manager.refresh(object);
				  VisualizationHelper visualizationHelper = new VisualizationHelper();
				  String [] arr= visualizationHelper.getDefaultVisualizationData(object.toString(),false,Locale.US);
				  if(!StringUtils.isEmpty(arr[0])){
					  result= getMatchHrefUrl(arr[0]);
				  }
			 }
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		     return result;
	}
	
	/**
	 * 获得对象的可视化文件下载地址
	 * @param ch
	 * @return
	 */
	public static  String  getViewContentDownloadURL(Persistable object)throws Exception{
		 String viewUrl=null;
		  if(object!=null){
		  try {
			  if(object instanceof EPMDocument||object instanceof WTDocument){
				  object = (ContentHolder) PersistenceHelper.manager.refresh(object);
				  VisualizationHelper visualizationHelper = new VisualizationHelper();
				  QueryResult epmReps = visualizationHelper.getRepresentations(object);
				  if (epmReps != null) {
			           while (epmReps.hasMoreElements()) {
			              Representation representation = (Representation) epmReps.nextElement();
			              representation = (Representation) ContentHelper.service.getContents(representation);
			              if (representation != null) {
			                  Enumeration e = ContentHelper.getApplicationData(representation).elements();
			                  while (e.hasMoreElements()) {
			                     Object app_object = e.nextElement();
			                     if (app_object instanceof ApplicationData) {
			                         ApplicationData app_data = (ApplicationData) app_object;
			                         viewUrl= app_data.getViewContentURL(representation).toString();
			                     }
			                  }
			 
			              }
			           }
				  }
			  }else {
				   throw new Exception("可视化不支持该对象("+object+")!");
			   }
			} catch (Exception e) {
			   e.printStackTrace();
			   throw new Exception("获取对象可视化下载链接失败!");
			}
		  }
		      return viewUrl;
	}
	
	
	
	

	
	
	
	
	/**
	 * set a file as PRIMARY Content
	 * @param document
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static WTDocument linkFile(WTDocument document, String filename)
			throws Exception {
		Transaction trans = null;
		try {
			trans = new Transaction();
			trans.start();
			document = (WTDocument) PersistenceHelper.manager.refresh(document);
			ApplicationData appData = ApplicationData
					.newApplicationData(document);
			appData.setRole(ContentRoleType.PRIMARY);
			appData.setFileName(filename.substring(filename
					.lastIndexOf(File.separator) + 1));
			appData.setUploadedFromPath("");
			File file = new File(filename);
			FileInputStream is = new FileInputStream(file);
			appData = ContentServerHelper.service.updateContent(
					(ContentHolder) document, appData, is);
			is.close();
			document = (WTDocument) ContentServerHelper.service
					.updateHolderFormat((FormatContentHolder) document);
			trans.commit();
		} catch (Exception e) {
			if (trans != null)
				trans.rollback();
			throw e;
		}
		return document;
	}
	
	
	
	/**
	 * create a usagelink between two WTPart
	 * @param parent
	 * @param child
	 * @param unit
	 * @param amount
	 * @param lineNo
	 * @param occurrence
	 * @return
	 * @throws WTException
	 */
	public static WTPartUsageLink createUsageLink(WTPart parent, WTPart child,
			String unit, double amount, String lineNo, String occurrence)
			throws WTException {
		// parent part need checking out before creating the link.
		parent = (WTPart) PersistenceHelper.manager
				.prepareForModification(parent);
		PersistenceServerHelper.manager.lock(parent, true);
		WTPartMaster master = (WTPartMaster) child.getMaster();
		QuantityUnit quantityUnit = QuantityUnit.toQuantityUnit(unit);
		Quantity quantity = Quantity.newQuantity(amount, quantityUnit);
		WTPartUsageLink usagelink = WTPartUsageLink.newWTPartUsageLink(parent,
				master);
		try {
			usagelink.setQuantity(quantity);
			if ((lineNo != null) && (lineNo.length() > 0)) {
				LineNumber lineNumber = LineNumber.newLineNumber(Long
						.parseLong(lineNo));
				usagelink.setLineNumber(lineNumber);
			}
			usagelink = (WTPartUsageLink) PersistenceHelper.manager
					.store(usagelink);
			// add new occurrence
			if ((occurrence != null) && (occurrence.length() > 0)) {
				PartUsesOccurrence newOccurrence = PartUsesOccurrence
						.newPartUsesOccurrence(usagelink);
				newOccurrence.setName(occurrence);
				OccurrenceHelper.service.saveUsesOccurrenceAndData(
						newOccurrence, null);

			}
		} catch (WTPropertyVetoException wpve) {
			throw new WTException(wpve);
		}
		return usagelink;
	}
	

	
	/**
	 * generate  Report URL
	 * @param rt
	 * @return
	 */
	public static String getReportURL(wt.query.template.ReportTemplate rt) {
		String theurl = null;
		try {
			wt.fc.ReferenceFactory rf = new wt.fc.ReferenceFactory();
			String oid = rf.getReferenceString(rt);
			java.util.Properties p = new java.util.Properties();
			p.put("action", "ProduceReport");
			p.put("oid", oid);
			theurl = GatewayURL.buildAuthenticatedURL(
					"wt.enterprise.URLProcessor", "generateForm", p)
					.toExternalForm();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return theurl;
	}
  

	/**
	 * get folder by name in Container
	 * @param path
	 * @param con
	 * @return
	 * @throws WTException
	 */
	public static Folder getFolder(String path, WTContainer con)
			throws WTException {
		// WTPrincipal curUser = SessionHelper.manager.getPrincipal();
		// WTPrincipal previous = SessionHelper.manager.setAdministrator();
		Folder folder = null;
		// try {
		StringTokenizer tokenizer = new StringTokenizer(path, "/");
		String subPath = "";
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			subPath = subPath + "/" + token;
			try {
				folder = FolderHelper.service.getFolder(subPath, WTContainerRef
						.newWTContainerRef(con));
			} catch (WTException e) {
//				e.printStackTrace();
				folder = FolderHelper.service.createSubFolder(subPath,
						WTContainerRef.newWTContainerRef(con));
			}
		}
		// } finally {
		// previous = SessionHelper.manager.setPrincipal(curUser.getName());
		// }
		return folder;
	}
	
	
	/**
	 * save wtdoc primary content to local
	 * @param doc
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public static String saveToDir(WTDocument doc, String dir) throws Exception {
		String filename = getFileName(doc);
		if (!dir.endsWith(File.separator))
			dir += File.separator;
		saveToFile(doc, dir + filename);
		return filename;
	}
	/**
	 * get name of primary content
	 * @param doc
	 * @return
	 */
	public static String getFileName(WTDocument doc) {
		return doc.getNumber() + "." + doc.getFormatName();
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
	 * get name by language
	 * @param doc
	 * @param lang
	 * @return
	 */
	public static String getFileNameByLang(EPMDocument doc, String lang) {
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
		filename += "_" + lang + extension;
		return filename;
	}
	
	/**
	 * get fullpath of file
	 * @param filepath
	 * @return
	 */
	public static String duplicatefilename(String filepath) {
		String rt = "";
		String filename = "";
		String extension = "";
		int dot = filepath.lastIndexOf(".");
		if (dot > 0) {
			filename = filepath.substring(0, dot);
			extension = filepath.substring(dot);
		}
		rt = filepath;
		int i = 1;
		while (rt != null) {
			
			File a = new File(rt);
			if (a.exists()) {
				rt = filename + "_" + String.valueOf(i) + extension;
			} else {
				break;
			}
			i++;
		}
		return rt;
	}
	


	/**
	 * format file name
	 * fName = Replace(fName, "/", "") fName = Replace(fName, "\", "") fName =
	 * Replace(fName, "/", "") fName = Replace(fName, ":", "") fName =
	 * Replace(fName, "*", "") fName = Replace(fName, "?", "") fName =
	 * Replace(fName, """", "") fName = Replace(fName, "<", "") fName =
	 * Replace(fName, ">", "") fName = Replace(fName, "!", "")
	 */
	public static String formatFileName(String filename) {
		String formatName = filename.replaceFirst("/", "");
		formatName = formatName.replaceFirst("\\\\", "");
		formatName = formatName.replaceFirst(":", "");
		formatName = formatName.replaceFirst("\\*", "");
		formatName = formatName.replaceFirst("\\?", "");
		formatName = formatName.replaceFirst("\"", "");
		formatName = formatName.replaceFirst("<", "");
		formatName = formatName.replaceFirst(">", "");
		formatName = formatName.replaceFirst("!", "");
		formatName = formatName.replace('&', '-');
		return formatName;
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

	public static void saveToDisk(String contents, String filename)
			throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filename);
			fos.write(contents.getBytes(DEFAULT_CHARSET));
			fos.flush();
		} finally {
			if (fos != null)
				fos.close();
		}
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
	 * revise a Versioned object
	 * @param currVer
	 * @return
	 * @throws WTException
	 */
	public static Versioned revise(Versioned currVer) throws WTException {
		Versioned newVer = null;
		try {
			Versioned newVersionDoc = VersionControlHelper.service
					.newVersion((Versioned) currVer,false);
			FolderHelper.assignLocation((FolderEntry) newVersionDoc,
					FolderHelper.service.getFolder((FolderEntry) currVer));
			newVer = (Versioned) PersistenceHelper.manager.store(newVersionDoc);
		} catch (Exception e) {
			System.out.println("Revise Error:" + e.toString());
			e.printStackTrace();
		}
		return newVer;
	}
	
	
	/**
	 * get soft type name
	 * @param obj
	 * @return
	 * @throws WTException
	 * @throws RemoteException
	 */
	public static String getTypeName(Object obj) throws WTException,
			RemoteException {
		String type = TypedUtilityServiceHelper.service.getLocalizedTypeName(
				obj).getLocalizedMessage(WTContext.getContext().getLocale());
		return type;
	}
	
  /**
   * 获得本地的codebase路径地址
   * @return
   * @throws IOException
   */
	public static String getCodebaseLocation() throws IOException{
		WTProperties wtproperties= WTProperties.getLocalProperties();
	    return wtproperties.getProperty("wt.codebase.location");
	}

	

	
	/**
	 * get PrimaryContent download url
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static String getPrimaryContentUrl(EPMDocument doc) throws Exception {
		ApplicationData app=getApplicationData(doc);
		if(app==null) return null;
		String url = "/Windchill/servlet/WindchillAuthGW/wt.fv.master.RedirectDownload/redirectDownload/"
				+ doc.getCADName()
				+ "?u8&HttpOperationItem="
				+ StringUtil.replaceAll(getApplicationData(doc).toString(),
						":", "%3A")
				+ "&ContentHolder="
				+ StringUtil.replaceAll(doc.toString(), ":", "%3A")
				+ "&forceDownload=true";
		return url;
	}

	


	/**
	 * get wtdoc primary content download url
	 * @author age
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static String getPrimaryContentUrl(WTDocument doc) throws Exception {
		StringBuffer surl = new StringBuffer(
				"/Windchill/servlet/WindchillAuthGW/wt.content.ContentHttp/viewContent/");
		ApplicationData appData = getApplicationData(doc);
        if(appData==null) return null;
		surl.append(appData.getFileName());
		surl.append("?u8&HttpOperationItem=");
		surl.append(StringUtil.replaceAll(getApplicationData(doc).toString(),
				":", "%3A"));
		surl.append("&ContentHolder=").append(
				StringUtil.replaceAll(doc.toString(), ":", "%3A"));
		surl.append("&originalFileName=").append(appData.getFileName());
		surl.append("&forceDownload=true");
		return surl.toString();
	}
	
	/**
	 * get ApplicationData
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static ApplicationData getApplicationData(ContentHolder doc)
			throws Exception {
		ContentHolder holder = ContentHelper.service.getContents(doc);
		// ContentHelper.service.getDownloadURL(arg0, arg1)
		ContentItem item = ContentHelper
				.getPrimary((FormatContentHolder) holder);
		// ContentServerHelper.service.findContentStream((ApplicationData)item);
		ApplicationData appData = (ApplicationData) item;

		return appData;
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
	 * @param state
	 * @return
	 * @throws Exception
	 */
	public static EPMDocument createEPMDocument(EPMApplicationType epmAppType,
			EPMAuthoringAppType epmAuthType, EPMDocumentType epmDocType,
			String number, String name, String cadName,
			TypeDefinitionReference softType, String filename, Folder folder,
			Map iba, wt.lifecycle.State state) throws Exception {
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
		
		// set state
		wt.lifecycle.LifeCycleState lc = wt.lifecycle.LifeCycleState
				.newLifeCycleState();
		lc.setState(state);
		doc.setState(lc);
		if (iba != null && !iba.isEmpty()) {
			LWCUtil.setValueBeforeStore(doc,iba);
		}		
		// wt.lifecycle.LifeCycleHelper.service.setLifeCycleState(doc,state);
		doc = (EPMDocument) PersistenceHelper.manager.store(doc);

		doc = (EPMDocument) GenericUtil.linkFile(doc, filename);
		return doc;
	}
	
	
	
	/**
	 * get a EPMDoc by CAD
	 * @param cadname
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument getEPMDocumentByCADName(String cadname)
			throws WTException {
		EPMDocument doc = null;
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				EPMDocument.CADNAME, SearchCondition.EQUAL, cadname);
		qs.appendWhere(sc);
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
	 * create a ReferenceLink between two EPMDocument
	 * @param cadname1
	 * @param cadname2
	 * @throws Exception
	 */
//	public static void createEPMReferenceLink(String cadname1, String cadname2)
//			throws Exception {
//		EPMDocument doc1 = getEPMDocumentByCADName(cadname1);
//		EPMDocument doc2 = getEPMDocumentByCADName(cadname2);
//		if (doc1 == null || doc2 == null)
//			return;
//		EPMReferenceLink link = EPMReferenceLink.newEPMReferenceLink(doc1,
//				(DocumentMaster) doc2.getMaster());
//		PersistenceServerHelper.manager.insert(link);
//	}
	
	/**
	 * create a ReferenceLink between two EPMDocument
	 * @param doc1
	 * @param doc2
	 * @throws Exception
	 */
//	public static void createEPMReferenceLink(EPMDocument doc1, EPMDocument doc2)
//			throws Exception {
//		if (doc1 == null || doc2 == null)
//			return;
//		EPMReferenceLink link = EPMReferenceLink.newEPMReferenceLink(doc1,
//				(DocumentMaster) doc2.getMaster());
//		link.setReferenceType(EPMReferenceType.toEPMReferenceType("INTERNAL"));
//		PersistenceServerHelper.manager.insert(link);
//	}
	

	
	

	
	/**
	 * get primary Content
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static String getTextContent(EPMDocument doc) throws Exception {
		System.out.println("GenericUtil: begin to get TextContent");
		ContentHolder holder = ContentHelper.service.getContents(doc);
		System.out.println("GenericUtil: 1");
		ContentItem item = ContentHelper
				.getPrimary((FormatContentHolder) holder);
		System.out.println("GenericUtil: 2");
		InputStream is = ContentServerHelper.service
				.findContentStream((ApplicationData) item);
		System.out.println("GenericUtil: end to get TextContent");
		return getString(is);
	}
	/**
	 * get foramted filename
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public static String getString(String filename) throws Exception {
		InputStream is = new FileInputStream(filename);
		String content = getString(is);
		is.close();
		return content;
	}
	
	/**
	 *  get foramted filename
	 * @param f
	 * @return
	 * @throws Exception
	 */
	public static String getString(File f) throws Exception {
		InputStream is = new FileInputStream(f);
		String content = getString(is);
		is.close();
		return content;
	}
	/**
	 *  get foramted filename
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public static String getString(InputStream is) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = is.read(buf)) > 0) {
			baos.write(buf, 0, len);
		}
		baos.flush();
		String content = baos.toString(DEFAULT_CHARSET);
		baos.close();
		return content;
	}
	

	
	
	/**
	 * 获得最新版本
	 * @param master
	 * @return
	 * @throws WTException
	 */
	public static RevisionControlled getLatestObject(Master master)
			throws WTException {
		QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
		if(queryResult.hasMoreElements()){
			return (RevisionControlled) queryResult.nextElement();
		}
		return null;
	}
	
	
	/**
	 * 获得WTDocument文档、EPM对象编码链接
	 * get a Object info URL
	 * @param objtype
	 * @param number
	 * @return
	 * @throws Exception
	 */
	public static String getObjUrl(String objtype, String number)
			throws Exception {
		if (EPMDocument.class.getName().equals(objtype)) {
			return getObjUrl(EPMUtil.getEPMDocument(number, ""));
		} else if (WTDocument.class.getName().equals(objtype)) {
			return getObjUrl(DocUtils.getWTDocument(number, ""));
		}
		return null;
	}
	/**
	 * get a Object info URL
	 * @param obj
	 * @return
	 */
	public static String getObjUrl(Object obj) {
		return getObjUrl(obj, "ObjProps");
	}
	/**
	 * get a Object info URL
	 * @param obj
	 * @param action
	 * @return
	 */
	public static String getObjUrl(Object obj, String action) {
		if (obj == null)
			return "";
		String urlLink = "";
		try {
			HashMap hashmap = new HashMap();
			hashmap.put("Action", action);
			hashmap.put("oid", (new ReferenceFactory())
					.getReferenceString((WTObject) obj));
			URLFactory urlfactory = null;
			try {
				urlfactory = (URLFactory) MethodContext.getContext().get(
						"URLFactory");
			} catch (Exception ex) {
			}
			if (urlfactory == null)
			urlfactory = new URLFactory();
			urlLink = GatewayServletHelper.buildAuthenticatedHREF(urlfactory,
					"wt.enterprise.URLProcessor", "generateForm", hashmap);
			urlLink = trimBaseHostUrl(urlLink);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urlLink;
	}
	
	/**
	 * 获得Windchill Host前缀链接字串，比如Http://pbi.huawei.com
	 * get a  URL  exclude host name 
	 * @param theUrl
	 * @return
	 */
	public static String trimBaseHostUrl(String theUrl) {
		if (theUrl == null || theUrl.length() == 0)
			return "";
		String windchill = "/WINDCHILL/";
		int nIndex = theUrl.toUpperCase().indexOf(windchill);
		if (nIndex > 0)
			theUrl = theUrl.substring(nIndex);
		return theUrl;
	}
	
	/**
	 * 得到版本信息
	 * @param wto
	 * @return
	 */
	public static String getVersionInfo(WTObject wto) {
        String versionInfo = "";
        if(wto instanceof Versioned) {
            Versioned ved = (Versioned)wto;
            String version = ved.getVersionIdentifier().getValue();
            String iteration = ved.getIterationIdentifier().getValue();
            if(version != null && iteration != null) {
                versionInfo = version + "." + iteration;
            }
        } else {
            Debug.P("the wto is not versioned, return null ");
        }
        return versionInfo;
    }
	
	/**
	 * 文件夹移动
	 * move a object to other foler
	 * @param obj
	 * @param folder
	 * @return
	 * @throws Exception
	 */
	public static FolderEntry changeFolder(FolderEntry obj, Folder folder)
			throws Exception {
		if (FolderHelper.service.getFolder(obj).equals(folder))
			return obj;
		obj = FolderHelper.service.changeFolder(obj, folder);
		return (FolderEntry) PersistenceHelper.manager.refresh(obj);
	}
	
	
	
	/**
	 * 持久化之前更改对象的承担者(创建者)
	 * @param userName 用户名
	 * @param objNum 对象编号
	 * @throws WTException
	 */
	public static void changeCreator(String userName,String objNum)throws WTException{
		if(!StringUtils.isEmpty(userName)){
			WTUser user = OrganizationServicesHelper.manager.getUser(userName);
			 Persistable object=GenericUtil.getObjectByNumber(objNum);
			 if(object!=null){
				 try {
					VersionControlHelper.assignIterationCreator((Iterated)object, WTPrincipalReference.newWTPrincipalReference(user));
				} catch (WTPropertyVetoException e) {
					e.printStackTrace();
				}
			 }
		}
		
		
	}
	

	/**
	 * get soft type id
	 * @param obj
	 * @return
	 * @throws WTException
	 * @throws RemoteException
	 */
	public static String getTypeId(Object obj) throws WTException,
			RemoteException {
		return TypedUtilityServiceHelper.service.getExternalTypeIdentifier(obj)
				.toString();
	}
	
	
	/**
	 * create a baseline
	 * @param baselineName
	 * @param description
	 * @param folder
	 * @return
	 * @throws Exception
	 */
	public static ManagedBaseline createBaseLine(String baselineName,
			String description, Folder folder) throws Exception {
		ManagedBaseline baseline = ManagedBaseline.newManagedBaseline();
		baseline.setName(baselineName);
		baseline.setDescription(description);
		FolderHelper.assignLocation(baseline, folder);
		baseline = (ManagedBaseline) PersistenceHelper.manager.save(baseline);
		return baseline;
	}
	
	/**
	 * put a object into a baseline
	 * @param baseline
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	public static Baseline addObjectToBaseline(Baseline baseline,
			EPMDocument doc) throws Exception {
		try {
			if (!BaselineHelper.service.isInBaseline(doc, baseline)) {
				baseline = BaselineHelper.service.addToBaseline(doc, baseline);
			}
			QueryResult qr = EPMStructureHelper.service.navigateUses(doc, null,
					true);
			while (qr.hasMoreElements()) {
				EPMDocumentMaster master = (EPMDocumentMaster) qr.nextElement();
				EPMDocument subDoc = EPMUtil.getEPMDocument(master.getNumber(), "");
				baseline = addObjectToBaseline(baseline, subDoc);
			}
		} catch (Exception wte) {
			wte.printStackTrace();
			throw wte;
		}
		return baseline;
	}
	
	
	


	/**
	 * 获得受影响对象
	 * get the Effect objects
	 * @param wtco
	 * @return
	 * @throws Exception
	 */
	public static List getEffect(WTChangeOrder2 wtco) throws Exception {
		List list = new ArrayList();
		QueryResult qr = ChangeHelper2.service.getChangeablesBefore(wtco);
		while (qr.hasMoreElements()) {
			// EPMDocument cab=(EPMDocument)qr.nextElement();
			list.add(qr.nextElement());
		}
		return list;
	}
	
	/**
	 * get the Effect objects
	 * @param wtco
	 * @return
	 * @throws Exception
	 */
	public static List getEffect(WTChangeActivity2 wtco) throws Exception {
		List list = new ArrayList();
		QueryResult qr = ChangeHelper2.service.getChangeablesBefore(wtco);
		while (qr.hasMoreElements()) {
			// EPMDocument cab=(EPMDocument)qr.nextElement();
			list.add(qr.nextElement());
		}
		return list;
	}

	
	/**
	 * get all file in a folder
	 * @param toDir
	 * @return
	 * @throws Exception
	 */
	public static List getFilesByPath(String toDir) throws Exception {
		List fileList = new ArrayList();
		File dir = new File(toDir);
		File[] files = dir.listFiles();
		String podOrderDir = toDir;
		System.out.println(podOrderDir);
		System.out.println("size:" + files.length);
		for (int i = 0; i < files.length; i++) {

			if (files[i].isDirectory()) {
				podOrderDir = files[i].getPath();
				fileList.addAll(getFilesByPath(podOrderDir));
			} else {
				fileList.add(toDir + File.separator + files[i].getName());
			}
		}

		return fileList;
	}
	

	
	  /**
     * 获取一个Organization的所有管理员
     * 
     * @param org   组织对象
     * @return      Vector of organization administrators
     * @throws Exception
     */
    public static Vector findOrgAdmins(WTOrganization org) throws Exception {
        if (!RemoteMethodServer.ServerFlag) {
            String method = "findOrgAdmins";
            String klass = GenericUtil.class.getName();
            Class[] types = { WTOrganization.class };
            Object[] vals = { org };
            return (Vector) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
        }

        Vector admins = new Vector();
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            if (org == null)
            org = SessionHelper.getPrincipal().getOrganization();
            OrgContainer orgCont = WTContainerHelper.service.getOrgContainer(org);
            if (orgCont != null) {
                WTGroup grp = orgCont.getAdministrators();
                for (Enumeration en = grp.members(); en.hasMoreElements(); ) {
                    WTPrincipal principal = (WTPrincipal) en.nextElement();
                    if (principal instanceof WTUser)
                        admins.add(principal);
                }
            }
        }
        finally {
            SessionServerHelper.manager.setAccessEnforced(flag);
        }
        
        return admins;
    }
    
    
    /**
	 *  根据oid找到Persistable对象
	 *  OR开头的oid可以省略OR:,VR开头的oid必须以VR：开头
	 * @param oid  对象的oid
	 * @return  Persistable
	 */
	public static Persistable getPersistableByOid(String oid) {
		Persistable p = null;
		if (!RemoteMethodServer.ServerFlag) {
            try {
                Class aclass[] = { String.class};
                Object aobj[] = {oid};
                p = (Persistable)RemoteMethodServer.getDefault().invoke("getPersistableByOid", GenericUtil.class.getName(), null, aclass, aobj);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else {
//        	   WTUser previous = null;
//                WTPrincipal wtadministrator = null;
        	try {
	    		// 切换session
//	        previous = (WTUser) SessionHelper.manager.getPrincipal();
//			wtadministrator = SessionHelper.manager.getAdministrator();
//			SessionContext.setEffectivePrincipal(wtadministrator);
//		    SessionHelper.manager.setAdministrator();
		        
				ReferenceFactory rf = new ReferenceFactory();
				WTReference wtr = rf.getReference(oid);
				if (rf != null)
					p = wtr.getObject();
			} catch (WTException e) {
			   Debug.P("getPersistableByOid2():" + e);
			}
	    }		
		return p;
	}
	
	
	/**
	 * 重新设置对象生命周期状态
	 * @param LifeCycleManaged 对象
	 * @param stateName String
	 * @return 修改后的对象
	 */
	public static LifeCycleManaged changeState(LifeCycleManaged lm, String stateName) {
		 Debug.P("chage lifecycle state---->"+stateName);
		State state = State.toState(stateName);
		Debug.P(state);
		wt.lifecycle.LifeCycleManaged lifeCycleManaged = (wt.lifecycle.LifeCycleManaged) lm;
		try {
			if(state!=null){
				Debug.P(state.getDisplay());
				lifeCycleManaged = wt.lifecycle.LifeCycleHelper.service.setLifeCycleState(lm,state);
				String str = lifeCycleManaged.getLifeCycleState().getStringValue();
				Debug.P(str);
				if (str.equals(stateName)) {
					Debug.P("change succeed!!");
				}
			}
		} catch (WTException el) {
			 el.printStackTrace();
		}
	    	return lm;
	}

	
	/**
	 * 版本修订 (A.1-->B.4) (方法需放在checkout和checkin之间)
	 * @param persistable
	 * @param version
	 * @param iteration
	 */
	public static Persistable changeVersion(Persistable persistable,String version,String iteration){
		
		    //例子:将版本由B.5变成C.1
			try {
			      String versionStr=getVersionInfo((WTObject) persistable);
			      String comVersion=version+"."+iteration;
			      Debug.P("------------>>>PM Version:"+comVersion+"   ;Windchill Version:"+versionStr);
			      if(!versionStr.equals(comVersion)){
			    		PersistenceHelper.manager.refresh(persistable);
						if(!StringUtils.isEmpty(iteration)&&!StringUtils.isEmpty(version)){
							Series series=Series.newSeries("wt.vc.IterationIdentifier",iteration);
							HarvardSeries  hs=HarvardSeries.newHarvardSeries();
							hs.setValue(version);
							
							persistable=VersionControlHelper.service.newVersion((Versioned) persistable, VersionIdentifier.newVersionIdentifier(hs),IterationIdentifier.newIterationIdentifier(series));
						 } 
                       		Debug.P("------>>>Change Version("+versionStr+")--->("+comVersion+") Success!");				
			      }else{
			    	  WorkInProgressHelper.service.undoCheckout((Workable) persistable);
			      }
			} catch (WTException e) {
				e.printStackTrace();
			}catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
			return persistable;
		}
		
	
	/**
	 * 只修订大版本号
	 * @param persistable
	 * @param revision
	 * @return
	 * @throws WTException
	 */
	public static Persistable changeRevision(Persistable persistable,String revision) throws WTException{
		 if(!StringUtils.isEmpty(revision)){
			VersionControlHelper.service.changeRevision((Versioned) persistable, revision);
		 }
		 return persistable;
	} 
	
	
	/**
	 * 删除文档(包含与之关联的关系)
	 * @param number 文档编码
	 * @param version 如果版本不指定则删除所有的历史版本
	 * @throws WTException
	 * @throws InvocationTargetException 
	 * @throws RemoteException 
	 */
	  public static void deleteDoc(WTDocument doc, String version) throws Exception{
		 
		        Transaction tx=null;
				try {
					tx=new Transaction();
					tx.start();
				 
				    deleteBizObj(doc, version == null ? true : false);
					tx.commit();
					tx=null;
				   Debug.P("---->>Delete WTDocument Sucess!!");
				} catch (PropertyVetoException e1) {
					e1.printStackTrace();
					throw new WTException("PropertyVetoException:删除主内容失败!");
				}finally{
					if(tx!=null){
						tx.rollback();
					}
			}
	    }
	  
	  

	  
	  /**
	   * 删除部件
	   * @param number
	   * @param version
	   * @throws WTException
	   */
	    public static void deletePart(WTPart part, String version) throws Exception {
	    	 if(!RemoteMethodServer.ServerFlag){
			       String method = "deletePart";
	               String klass = GenericUtil.class.getName();
		           Class[] types = { String.class,String.class};
		           Object[] values = {WTPart.class,version};
	               RemoteMethodServer.getDefault().invoke(method, klass, null,types, values);
			    }else{
			    	Transaction tx = null;
			      try {
			    	  tx=new Transaction();
			    	  tx.start();
			    	  Debug.P("---->>WTPart:"+part.getNumber());
			    	  deleteBizObj(part, version == null ? true : false);
			    	  tx.commit();
			    	  tx=null;
				} catch (Exception e) {
				    e.printStackTrace();
				}finally{
					if(tx!=null){
					    tx.rollback();
					}
				}
		 }
}
	    
	    
	    /**
	     * 删除图档对象
	     * @param number
	     * @param version
	     * @throws WTException 
	     */
	    public static void deleteEPM(EPMDocument epmdoc, String version) throws Exception {
	    	
	    	if(!RemoteMethodServer.ServerFlag){
	    		String method = "deleteEPM";
	               String klass = GenericUtil.class.getName();
		           Class[] types = { EPMDocument.class,String.class};
		           Object[] values = {epmdoc,version};
	               RemoteMethodServer.getDefault().invoke(method, klass, null,types, values);
	    	}else{
	    		Transaction tx = null;
	    		try {
		    		 tx=new Transaction();
		    		 tx.start();
			        Debug.P("---->>EPMDocument:"+epmdoc.getNumber());
			        deleteBizObj(epmdoc, version == null ? true : false);
			    	tx.commit();
			        tx=null;
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if(tx!=null){
						tx.rollback();
					}
				}
	    
	    	}

	    }
	    
	    public static void RemDeleteWTObject(RevisionControlled obj,String deleteAction) throws Exception{
	    	if(!RemoteMethodServer.ServerFlag){
	    	try {
   				Class aclass[] = {RevisionControlled.class, String.class };
   				Object aobj[] = { obj, deleteAction};
   				RemoteMethodServer.getDefault().invoke("RemDeleteWTObject",
   						GenericUtil.class.getName(), null, aclass, aobj);
   			} catch (Exception e) {
   				e.printStackTrace();
   			}
	    	}else{  
	    		Transaction tx = null;
	    		try {
		    		 tx=new Transaction();
		    		 tx.start();
	    		        deleteWTObject(obj,deleteAction);
	    		     tx.commit();
		             tx=null;
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(tx!=null){
					tx.rollback();
				}
			}
	    	}
	    }
	    
	    
	    
	    /**
	     * 删除Windchill中的WTPart，EPMDocument
	     * @param obj 
	     * @param deleteAction 删除动作，all【删除全部版本】，mv[删除最新大版本]，sv[删除最新小版本]
	     * @throws Exception
	     */
       public static int deleteWTObject(RevisionControlled obj,String deleteAction) throws Exception{
    	   Debug.P("deleteAction--->"+deleteAction);
    	   List deleteList = new ArrayList();
    	   List linkList = new ArrayList();//存储对象件的Link关系
    	   String epmoid="";
		   String partoid="";
           QueryResult qrIter;  
    	   try{
    		   if(obj instanceof EPMDocument){
    			   linkList.addAll(EPMDocUtil.searchAllEPMBuildRule((EPMDocument)obj));//获取该图纸与部件的link关系
    			   if(deleteAction.equals(Contants.DELETE_ALL)){//删除所有版本
        			   qrIter = VersionControlHelper.service.allIterationsOf(obj.getMaster());
        			   while (qrIter.hasMoreElements()) {//获取当前对象所有的版本对象和各个版本的说明Link关系
    		                Iterated ddd = (Iterated) qrIter.nextElement();
    		                linkList.addAll((PersistenceServerHelper.manager.expand(ddd,
    		                        IteratedDescribeLink.DESCRIBES_ROLE, IteratedDescribeLink.class,
    		                        false)).getObjectVectorIfc().getVector());
    		         	   EPMDocument epmdoc =(EPMDocument)ddd;
						   epmoid=epmdoc.toString();
						   epmoid=epmoid.substring(epmoid.lastIndexOf(":")+1, epmoid.length());
						   QueryResult qr=PersistenceHelper.manager.navigate(epmdoc,
									EPMBuildHistory.BUILT_ROLE, EPMBuildHistory.class, true);
						   while(qr.hasMoreElements()){
							   Object object =(Object)qr.nextElement();
							   Debug.P("object--->"+object);
							   if(object instanceof WTPart){
								   WTPart part = (WTPart)object;
								   partoid=part.toString();
	    						   partoid=partoid.substring(partoid.lastIndexOf(":")+1, partoid.length());
								   EPMBuildHistory EPMBuild = EPMDocUtil.getEPMBuildHistory(epmoid,partoid);
								   Debug.P("EPMBuild---->"+EPMBuild);
								   linkList.add(EPMBuild);
							   }
						   }
        			   }
        			      // 删除关联Link
        			   Debug.P("deleting relations: " + linkList.size());
        			   for (int i=0;i<linkList.size();i++) {
        				   Debug.P("it--->"+linkList.get(i));
        				   BinaryLink link = (BinaryLink) linkList.get(i);
        				   if(link !=null)
        					   PersistenceServerHelper.manager.remove(link);
   		            	}
                        //  挨个删除所有的版本对象
        			   QueryResult rs=VersionControlHelper.service.allVersionsOf(obj.getMaster());
    		           Debug.P("----->>Version Size:"+rs==null?"0":rs.size());
    		           while(rs.hasMoreElements()){
    		        	   Object object = rs.nextElement();
    		        	   PersistenceHelper.manager.delete((Persistable) object);
    		           }
    		           Debug.P("delete---------------删除对象成功>");
        			   return 1;
        		   }else if(deleteAction.equals(Contants.DELETE_MV)){//删除最新大版本
        			   String mainVersion="";
        			   String secondVersion="";
        			   mainVersion=obj.getVersionIdentifier().getValue();
        			   Debug.P("当前对象的大版本号----》"+mainVersion);
        			   qrIter = VersionControlHelper.service.allIterationsOf(obj.getMaster());
        			   Debug.P("当前对象的所有版本数量---->"+qrIter.size());
        			   while (qrIter.hasMoreElements()) {//获取当前对象所有的版本对象和各个版本的说明Link关系
        				   RevisionControlled ddd = (RevisionControlled) qrIter.nextElement();
        				   secondVersion=ddd.getVersionIdentifier().getValue();
        				   Debug.P("secondVersion---->"+secondVersion);
        				   if(secondVersion.equals(mainVersion)){
        						   EPMDocument epmdoc =(EPMDocument)ddd;
        						   epmoid=epmdoc.toString();
        						   epmoid=epmoid.substring(epmoid.lastIndexOf(":")+1, epmoid.length());
        						   QueryResult qr=PersistenceHelper.manager.navigate(epmdoc,
       									EPMBuildHistory.BUILT_ROLE, EPMBuildHistory.class, true);
        						   while(qr.hasMoreElements()){
        							   Object object =(Object)qr.nextElement();
        							   Debug.P("object--->"+object);
        							   if(object instanceof WTPart){
        								   WTPart part = (WTPart)object;
        								   partoid=part.toString();
        	    						   partoid=partoid.substring(partoid.lastIndexOf(":")+1, partoid.length());
        								   EPMBuildHistory EPMBuild = EPMDocUtil.getEPMBuildHistory(epmoid,partoid);
        								   Debug.P("EPMBuild---->"+EPMBuild);
        								   linkList.add(EPMBuild);
        							   }
        						   } 
        					   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
       			                        IteratedUsageLink.USED_BY_ROLE, IteratedUsageLink.class, false))
       			                        .getObjectVectorIfc().getVector());
       						   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
      			                        IteratedUsageLink.ALL_ROLES, IteratedUsageLink.class, false))
      			                        .getObjectVectorIfc().getVector());
       						   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
       			                        IteratedReferenceLink.REFERENCED_BY_ROLE,
       			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
       			                        .getVector());
       						   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
      			                        IteratedReferenceLink.ALL_ROLES,
      			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
      			                        .getVector());
        					   deleteList.add(ddd);
        				   }
    		           }
    			   
    			      // 删除关联Link
		             Debug.P("deleting relations: " + linkList.size());
		            for (int i=0;i<linkList.size();i++) {
//		            	 Debug.P("it--->"+linkList.get(i));
		                BinaryLink link = (BinaryLink) linkList.get(i);
		                if(link !=null)
		                   PersistenceServerHelper.manager.remove(link);
		            }
		            
     			    //挨个删除所有的版本对象
		            Debug.P("deleting wtobject: " + deleteList.size());
                     for(int i=0;i<deleteList.size();i++){
                    	 WTObject object =(WTObject)deleteList.get(i);
                    	 if(object instanceof RevisionControlled){
                    		 RevisionControlled epm = (RevisionControlled)object;
                    		 secondVersion=epm.getVersionIdentifier().getValue();
//                    		 Debug.P("delete epmd----->"+secondVersion+epm.getIterationIdentifier().getValue());
                    		 if(secondVersion.equals(mainVersion)){
                    			 PersistenceHelper.manager.delete(epm);
                                 break;                    			 
                    		 }
                    	 }
                     } 
                	 Debug.P("delete---------------删除最新大版本成功>");
    			   return 1;
    		   }else if(deleteAction.equals(Contants.DELETE_SV)){//删除最新小版本
    			  String objIterat="";
    			  String lasterIterat="";
    			  WTSet wtset = new WTHashSet();
    			  objIterat=obj.getVersionIdentifier().getValue()+"."+obj.getIterationIdentifier().getValue();
    			   Debug.P("objIterat--->"+objIterat);
    			   qrIter = VersionControlHelper.service.allIterationsOf(obj.getMaster());
    			   Debug.P("当前对象的所有版本数量---->"+qrIter.size());
    			   while (qrIter.hasMoreElements()) {//获取当前对象所有的版本对象和各个版本的说明Link关系
    				   RevisionControlled ddd = (RevisionControlled) qrIter.nextElement();
    				   lasterIterat=ddd.getVersionIdentifier().getValue()+"."+ddd.getIterationIdentifier().getValue();
    				   Debug.P("lasterIterat---->"+lasterIterat);
    				   if(lasterIterat.equals(objIterat)){
    						   EPMDocument epmdoc =(EPMDocument)ddd;
    						   epmoid=epmdoc.toString();
    						   epmoid=epmoid.substring(epmoid.lastIndexOf(":")+1, epmoid.length());
    						   QueryResult qr=PersistenceHelper.manager.navigate(epmdoc,
   									EPMBuildHistory.BUILT_ROLE, EPMBuildHistory.class, true);
    						   while(qr.hasMoreElements()){
    							   Object object =(Object)qr.nextElement();
//    							   Debug.P("object--->"+object);
    							   if(object instanceof WTPart){
    								   WTPart part = (WTPart)object;
    								   partoid=part.toString();
    	    						   partoid=partoid.substring(partoid.lastIndexOf(":")+1, partoid.length());
    								   EPMBuildHistory EPMBuild = EPMDocUtil.getEPMBuildHistory(epmoid,partoid);
    								   Debug.P("EPMBuild---->"+EPMBuild);
    								   linkList.add(EPMBuild);
    							   }
    						   } 
    					   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
   			                        IteratedUsageLink.USED_BY_ROLE, IteratedUsageLink.class, false))
   			                        .getObjectVectorIfc().getVector());
   						   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
  			                        IteratedUsageLink.ALL_ROLES, IteratedUsageLink.class, false))
  			                        .getObjectVectorIfc().getVector());
   						   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
   			                        IteratedReferenceLink.REFERENCED_BY_ROLE,
   			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
   			                        .getVector());
   						   linkList.addAll((PersistenceServerHelper.manager.expand(epmdoc,
  			                        IteratedReferenceLink.ALL_ROLES,
  			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
  			                        .getVector());
    				   }
		           }
    			   // 删除关联Link
		             Debug.P("deleting relations: " + linkList.size());
		            for (int i=0;i<linkList.size();i++) {
		                BinaryLink link = (BinaryLink) linkList.get(i);
		                if(link !=null)
		                   PersistenceServerHelper.manager.remove(link);
		            }
    			     
//  	              //删除当前最新小版本对象
    			   Iterated laster=VersionControlHelper.service.getLatestIteration(obj, false);
    				Debug.P("laster---->"+laster);
    				wtset.add(laster);
    				ConflictResolution[] localObject = getConflictResolutionsForDeleteLatest();
    				VersionControlHelper.service.deleteIterations(wtset,localObject, false);
  	             return 1;
    		    }
    		   }else if(obj instanceof WTPart){ //删除部件
    			   
    			   linkList.addAll(EPMDocUtil.searchAllEPMBuildRuleByPart((WTPart)obj));//获取该图纸与部件的link关系
    			   if(deleteAction.equals(Contants.DELETE_ALL)){//删除所有版本
        			   qrIter = VersionControlHelper.service.allIterationsOf(obj.getMaster());
        			   while (qrIter.hasMoreElements()) {//获取当前对象所有的版本对象和各个版本的说明Link关系
    		                Iterated ddd = (Iterated) qrIter.nextElement();
    		                linkList.addAll((PersistenceServerHelper.manager.expand(ddd,
    		                        IteratedDescribeLink.DESCRIBES_ROLE, IteratedDescribeLink.class,
    		                        false)).getObjectVectorIfc().getVector());
    		         	   WTPart part =(WTPart)ddd;
    		         	   partoid=part.toString();
    		         	  partoid=partoid.substring(partoid.lastIndexOf(":")+1, partoid.length());
						   QueryResult qr=PersistenceHelper.manager.navigate(part,
									EPMBuildHistory.BUILT_BY_ROLE, EPMBuildHistory.class, true);
						   while(qr.hasMoreElements()){
							   Object object =(Object)qr.nextElement();
							   Debug.P("object--->"+object);
							   if(object instanceof EPMDocument){
								   EPMDocument epmdoc = (EPMDocument)object;
								   epmoid=epmdoc.toString();
								   epmoid=epmoid.substring(epmoid.lastIndexOf(":")+1, epmoid.length());
								   EPMBuildHistory EPMBuild = EPMDocUtil.getEPMBuildHistory(epmoid,partoid);
								   Debug.P("EPMBuild---->"+EPMBuild);
								   linkList.add(EPMBuild);
							   }
						   }
        			   }
        			      // 删除关联Link
        			   Debug.P("deleting relations: " + linkList.size());
        			   for (int i=0;i<linkList.size();i++) {
        				   Debug.P("it--->"+linkList.get(i));
        				   BinaryLink link = (BinaryLink) linkList.get(i);
        				   if(link !=null)
        					   PersistenceServerHelper.manager.remove(link);
   		            	}
                        //  挨个删除所有的版本对象
        			   QueryResult rs=VersionControlHelper.service.allVersionsOf(obj.getMaster());
    		           Debug.P("----->>Version Size:"+rs==null?"0":rs.size());
    		           while(rs.hasMoreElements()){
    		        	   Object object = rs.nextElement();
    		        	   PersistenceHelper.manager.delete((Persistable) object);
    		           }
    		           Debug.P("delete---------------删除对象成功>");
        			   return 1;
        		   }else if(deleteAction.equals(Contants.DELETE_MV)){//删除最新大版本
        			   String mainVersion="";
        			   String secondVersion="";
        			   mainVersion=obj.getVersionIdentifier().getValue();
        			   Debug.P("当前对象的大版本号----》"+mainVersion);
        			   qrIter = VersionControlHelper.service.allIterationsOf(obj.getMaster());
        			   Debug.P("当前对象的所有版本数量---->"+qrIter.size());
        			   while (qrIter.hasMoreElements()) {//获取当前对象所有的版本对象和各个版本的说明Link关系
        				   RevisionControlled ddd = (RevisionControlled) qrIter.nextElement();
        				   secondVersion=ddd.getVersionIdentifier().getValue();
        				   Debug.P("secondVersion---->"+secondVersion);
        				   if(secondVersion.equals(mainVersion)){
        				   	   WTPart part =(WTPart)ddd;
        		         	   partoid=part.toString();
        		         	  partoid=partoid.substring(partoid.lastIndexOf(":")+1, partoid.length());
    						   QueryResult qr=PersistenceHelper.manager.navigate(part,
    									EPMBuildHistory.BUILT_BY_ROLE, EPMBuildHistory.class, true);
    						   while(qr.hasMoreElements()){
    							   Object object =(Object)qr.nextElement();
    							   Debug.P("object--->"+object);
    							   if(object instanceof EPMDocument){
    								   EPMDocument epmdoc = (EPMDocument)object;
    								   epmoid=epmdoc.toString();
    								   epmoid=epmoid.substring(epmoid.lastIndexOf(":")+1, epmoid.length());
    								   EPMBuildHistory EPMBuild = EPMDocUtil.getEPMBuildHistory(epmoid,partoid);
    								   Debug.P("EPMBuild---->"+EPMBuild);
    								   linkList.add(EPMBuild);
    							   }
    						   }
        					   linkList.addAll((PersistenceServerHelper.manager.expand(part,
       			                        IteratedUsageLink.USED_BY_ROLE, IteratedUsageLink.class, false))
       			                        .getObjectVectorIfc().getVector());
       						   linkList.addAll((PersistenceServerHelper.manager.expand(part,
      			                        IteratedUsageLink.ALL_ROLES, IteratedUsageLink.class, false))
      			                        .getObjectVectorIfc().getVector());
       						   linkList.addAll((PersistenceServerHelper.manager.expand(part,
       			                        IteratedReferenceLink.REFERENCED_BY_ROLE,
       			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
       			                        .getVector());
       						   linkList.addAll((PersistenceServerHelper.manager.expand(part,
      			                        IteratedReferenceLink.ALL_ROLES,
      			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
      			                        .getVector());
        					   deleteList.add(ddd);
        				   }
    		           }
    			   
    			      // 删除关联Link
		             Debug.P("deleting relations: " + linkList.size());
		            for (int i=0;i<linkList.size();i++) {
//		            	 Debug.P("it--->"+linkList.get(i));
		                BinaryLink link = (BinaryLink) linkList.get(i);
		                if(link !=null)
		                   PersistenceServerHelper.manager.remove(link);
		            }
		            
     			    //挨个删除所有的版本对象
		            Debug.P("deleting wtobject: " + deleteList.size());
                     for(int i=0;i<deleteList.size();i++){
                    	 WTObject object =(WTObject)deleteList.get(i);
                    	 if(object instanceof RevisionControlled){
                    		 RevisionControlled part = (RevisionControlled)object;
                    		 secondVersion=part.getVersionIdentifier().getValue();
//                    		 Debug.P("delete part----->"+secondVersion+part.getIterationIdentifier().getValue());
                    		 if(secondVersion.equals(mainVersion)){
                    			 PersistenceHelper.manager.delete(part);
                                 break;                    			 
                    		 }
                    	 }
                     } 
                	 Debug.P("delete---------------删除最新大版本成功>");
    			   return 1;
    		   }else if(deleteAction.equals(Contants.DELETE_SV)){//删除最新小版本
    			  String objIterat="";
    			  String lasterIterat="";
    			  WTSet wtset = new WTHashSet();
    			  objIterat=obj.getVersionIdentifier().getValue()+"."+obj.getIterationIdentifier().getValue();
    			   Debug.P("objIterat--->"+objIterat);
    			   qrIter = VersionControlHelper.service.allIterationsOf(obj.getMaster());
    			   Debug.P("当前对象的所有版本数量---->"+qrIter.size());
    			   while (qrIter.hasMoreElements()) {//获取当前对象所有的版本对象和各个版本的说明Link关系
    				   RevisionControlled ddd = (RevisionControlled) qrIter.nextElement();
    				   lasterIterat=ddd.getVersionIdentifier().getValue()+"."+ddd.getIterationIdentifier().getValue();
    				   Debug.P("lasterIterat---->"+lasterIterat);
    				   if(lasterIterat.equals(objIterat)){
    					   WTPart part =(WTPart)ddd;
    						   partoid=part.toString();
    						   partoid=partoid.substring(partoid.lastIndexOf(":")+1, partoid.length());
    						   QueryResult qr=PersistenceHelper.manager.navigate(part,
   									EPMBuildHistory.BUILT_BY_ROLE, EPMBuildHistory.class, true);
    						   while(qr.hasMoreElements()){
    							   Object object =(Object)qr.nextElement();
//    							   Debug.P("object--->"+object);
    							   if(object instanceof EPMDocument){
    								   EPMDocument epmdoc = (EPMDocument)object;
    								   epmoid=epmdoc.toString();
    								   epmoid=epmoid.substring(epmoid.lastIndexOf(":")+1, epmoid.length());
    								   EPMBuildHistory EPMBuild = EPMDocUtil.getEPMBuildHistory(epmoid,partoid);
//    								   Debug.P("EPMBuild---->"+EPMBuild);
    								   linkList.add(EPMBuild);
    							   }
    						   } 
    					   linkList.addAll((PersistenceServerHelper.manager.expand(part,
   			                        IteratedUsageLink.USED_BY_ROLE, IteratedUsageLink.class, false))
   			                        .getObjectVectorIfc().getVector());
   						   linkList.addAll((PersistenceServerHelper.manager.expand(part,
  			                        IteratedUsageLink.ALL_ROLES, IteratedUsageLink.class, false))
  			                        .getObjectVectorIfc().getVector());
   						   linkList.addAll((PersistenceServerHelper.manager.expand(part,
   			                        IteratedReferenceLink.REFERENCED_BY_ROLE,
   			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
   			                        .getVector());
   						   linkList.addAll((PersistenceServerHelper.manager.expand(part,
  			                        IteratedReferenceLink.ALL_ROLES,
  			                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
  			                        .getVector());
    				   }
		           }
    			   // 删除关联Link
		             Debug.P("deleting relations: " + linkList.size());
		            for (int i=0;i<linkList.size();i++) {
		                BinaryLink link = (BinaryLink) linkList.get(i);
		                if(link !=null)
		                   PersistenceServerHelper.manager.remove(link);
		            }
    			     
//  	              //删除当前最新小版本对象
    			   Iterated laster=VersionControlHelper.service.getLatestIteration(obj, false);
    				Debug.P("laster---->"+laster);
    				wtset.add(laster);
    				ConflictResolution[] localObject = getConflictResolutionsForDeleteLatest();
    				VersionControlHelper.service.deleteIterations(wtset,localObject, false);
  	             return 1;
    		    }
    		   }else if(obj instanceof WTDocument){
    			   WTDocument doc=(WTDocument)obj;
    			   if(deleteAction.equals(Contants.DELETE_ALL)){
    				   deleteBizObj(doc, true );
    			   }else{
    				   deleteBizObj(doc, false );
    			   }
    		   }
    	   }catch(Exception e){
    		   e.printStackTrace();
    		   return 0;
    	   }
    	   return 0;
       }
	    
       public static ConflictResolution[] getConflictResolutionsForDeleteLatest()
   			throws WTException {
   		ConflictResolution[] arrayOfConflictResolution = null;

   		MethodContext localMethodContext = MethodContext.getContext();
   		HashMap localHashMap = (HashMap) localMethodContext
   				.get("RESOLUTIONS_STORE");
   		if (localHashMap != null) {
   			ArrayList localArrayList = new ArrayList();
   			localArrayList.add(new ConflictResolution(
   					VersionControlConflictType.LATEST_ITERATION_DELETE,
   					VersionControlResolutionType.ALLOW_LATEST_ITERATION_DELETE));
   			Collection localCollection = localHashMap.values();
   			Iterator localIterator = localCollection.iterator();
   			while (localIterator.hasNext()) {
   				Vector localVector = (Vector) localIterator.next();
   				localArrayList.addAll(localVector);
   			}
   			arrayOfConflictResolution = (ConflictResolution[]) localArrayList
   					.toArray(new ConflictResolution[localArrayList.size()]);
   		} else {
   			arrayOfConflictResolution = new ConflictResolution[] {new ConflictResolution(
   					VersionControlConflictType.LATEST_ITERATION_DELETE,
   					VersionControlResolutionType.ALLOW_LATEST_ITERATION_DELETE)};
   		}
   		return arrayOfConflictResolution;
   	}
       
	    /**
	     * 删除对象(文档对象,部件对象,图档对象)
	     * 如果版本为空则删除所有的对象版本
	     * @param obj
	     * @param deleteAllVersion
	     */
	    private static void deleteBizObj(RevisionControlled obj, boolean deleteAllVersion) throws Exception {
//	        
//	    	if(!RemoteMethodServer.ServerFlag){
//	    		   String method = "deleteBizObj";
//	               String klass = GenericUtil.class.getName();
//		           Class[] types = {RevisionControlled.class,boolean.class};
//		           Object[] values = {obj,deleteAllVersion};
//	               RemoteMethodServer.getDefault().invoke(method, klass, null,types, values);
//	    	}else{
		        try {
		            // IteratedDescribeLink
		            HashSet linkSet = new HashSet();
		            QueryResult qrIter;
		            if (deleteAllVersion){
		                qrIter = VersionControlHelper.service.allIterationsOf(obj.getMaster());
		            }else{
		                qrIter = VersionControlHelper.service.iterationsOf(obj);
		            }
		            while (qrIter.hasMoreElements()) {
		                Iterated ddd = (Iterated) qrIter.nextElement();
		                linkSet.addAll((PersistenceServerHelper.manager.expand(ddd,
		                        IteratedDescribeLink.DESCRIBES_ROLE, IteratedDescribeLink.class,
		                        false)).getObjectVectorIfc().getVector());
		            }
		            
		            // IteratedUsageLink, IteratedReferenceLink
		            int verCount = VersionControlHelper.service.allVersionsOf(obj).size();
		            // Debug.P("version count: " + verCount);
		            if (verCount == 1) {
		                linkSet.addAll((PersistenceServerHelper.manager.expand(obj.getMaster(),
		                        IteratedUsageLink.USED_BY_ROLE, IteratedUsageLink.class, false))
		                        .getObjectVectorIfc().getVector());
		                linkSet.addAll((PersistenceServerHelper.manager.expand(obj.getMaster(),
		                        IteratedReferenceLink.REFERENCED_BY_ROLE,
		                        IteratedReferenceLink.class, false)).getObjectVectorIfc()
		                        .getVector());
		            }
		            
		            // 删除关联Link
		            // Debug.P("deleting relations: " + linkSet.size());
		            for (Iterator it = linkSet.iterator(); it.hasNext();) {
		                BinaryLink link = (BinaryLink) it.next();
		                // Debug.P("removing: ", link.getRoleAObjectRef(), ", "
		                // , link.getRoleBObjectRef());
		                PersistenceServerHelper.manager.remove(link);
		            }
		           QueryResult rs=VersionControlHelper.service.allVersionsOf(obj.getMaster());
		           Debug.P("----->>Version Size:"+rs==null?"0":rs.size());
		           while(rs.hasMoreElements()){
		        	   Object object = rs.nextElement();
		        	   PersistenceHelper.manager.delete((Persistable) object);
		           }
		           
		           
		            
		        } catch (PersistenceException e) {
		              Debug.P("PersistenceException : {}"+e.getMessage());
		        } catch (WTException e) {
		        	 Debug.P("WTException : {}"+e.getMessage());
		        }
	    	

	    }
	    
	    
		/**
		 * 通过对象类型的显示名称来获取文档的类型
		 * @param name					文档类型的显示名称
		 * @return
		 * @throws WTException
		 */
		public  static String getTypeByName(String name) throws WTException{
			QuerySpec qs = new QuerySpec();
			qs.setAdvancedQueryEnabled(true);
			qs.appendClassList(WTTypeDefinitionMaster.class, true);
			TableColumn column1 = new TableColumn("A0","DISPLAYNAMEKEY");
			SearchCondition sc2 = new SearchCondition(column1,SearchCondition.EQUAL,new ConstantExpression(name));
			qs.appendWhere(sc2);
			QueryResult qr1 = null;
			qr1 = PersistenceHelper.manager.find(qs);
			while(qr1.hasMoreElements()){
				Object obj[] = (Object[]) qr1.nextElement();
				WTTypeDefinitionMaster definition = (WTTypeDefinitionMaster) obj[0];
				return definition.getIntHid();
			}
			    return null;
		}
		
		/**
		 * 适用于持久化后修改对象的创建者/修改者(前提条件:对象需先检出)
		 *@param userName 用户对象
		 *@param persistable 持久化对象
		 * 
		 */
		public static void changeWTPrincipalField(String userName,Persistable persistable,String modifyType)throws WTException,Exception{
			
                    
			 try {
				  
				     //获得用户对象
				     WTUser user= OrganizationServicesHelper.manager.getUser(userName);
				     PersistenceHelper.manager.refresh(persistable,false,true);
				     WTPrincipalReference userRef= WTPrincipalReference.newWTPrincipalReference(user);
				     Debug.P("-------->>>Add Creator:"+user.getName());
				     Class[] principalRef=new Class[]{WTPrincipalReference.class};
				     Method methodRef=null;
				     if(METHOD_CREATOR.equals(modifyType)){
				    	 methodRef=_IterationInfo.class.getDeclaredMethod(METHOD_CREATOR, principalRef);
				     }else if(METHOD_MODIFIER.equals(modifyType)){
				    	 methodRef=_IterationInfo.class.getDeclaredMethod(METHOD_MODIFIER, principalRef);
				     }
				       
				     if(methodRef==null){ throw new WTException("--------->>>修改对象类型方法不存在:"+modifyType);}
				     methodRef.setAccessible(true);
                    
	                if(persistable instanceof EPMDocument){
	                	EPMDocument epmObject=(EPMDocument)persistable;
	                	methodRef.invoke(epmObject.getIterationInfo(), new Object[]{userRef});//创建者
	                	PersistenceHelper.manager.modify(epmObject);
	                    PersistenceHelper.manager.refresh(epmObject);
	                }else if(persistable instanceof WTDocument){
	                	WTDocument docObject=(WTDocument)persistable;
	                	methodRef.invoke(docObject.getIterationInfo(), new Object[]{userRef});//创建者
	                	SessionHelper.manager.setAuthenticatedPrincipal(userName);
	                    PersistenceHelper.manager.modify(docObject);
	                    PersistenceHelper.manager.refresh(docObject);
	                }else if(persistable instanceof WTPart){
	                	WTPart partObject=(WTPart)persistable;
	                	methodRef.invoke(partObject.getIterationInfo(), new Object[]{userRef});//创建者
	                	SessionHelper.manager.setAuthenticatedPrincipal(userName);
	                    PersistenceHelper.manager.modify(partObject);
	                    PersistenceHelper.manager.refresh(partObject);
	                }             
			} catch (Exception e) {
				e.printStackTrace();
			}
			   
		}
		
	    
		/**
		 * 检出对象
		 * @param wtobject
		 * @return
		 * @throws InterruptedException
		 * @throws WTPropertyVetoException
		 * @throws WorkInProgressException
		 * @throws WTException
		 */
		public static WTObject checkOut(WTObject wtobject)
				throws InterruptedException, WTPropertyVetoException,
				WorkInProgressException, WTException {
		
	       
			
			if (wtobject instanceof Workable) {
				Workable wtobject1 = (Workable) wtobject;
				
				if (wtobject1.getCheckoutInfo().getState().equals(
						WorkInProgressState.WORKING)) {
					return wtobject;
				} else if (wtobject1.getCheckoutInfo().getState().equals(
						WorkInProgressState.CHECKED_OUT)) {
					return (WTObject) WorkInProgressHelper.service
							.workingCopyOf(wtobject1);
				}
			}

			if (!FolderHelper.inPersonalCabinet((CabinetBased) wtobject)
					&& !WorkInProgressHelper.isWorkingCopy((Workable) wtobject)) {

				Thread.sleep(50);

				Folder myFolder;

				myFolder = WorkInProgressHelper.service.getCheckoutFolder();
				CheckoutLink checkout_link = WorkInProgressHelper.service.checkout(
						(Workable) wtobject, myFolder, "");
				wtobject = (WTObject) checkout_link.getWorkingCopy();
				return wtobject;
			}

			return wtobject;
		}

		
		/**
		 * 检入对象
		 * @param wtobject
		 * @return
		 * @throws WorkInProgressException
		 * @throws WTPropertyVetoException
		 * @throws PersistenceException
		 * @throws WTException
		 */
		public static WTObject checkIn(WTObject wtobject)
				throws WorkInProgressException, WTPropertyVetoException,
				PersistenceException, WTException {

			if (wt.vc.wip.WorkInProgressHelper.isCheckedOut((Workable) wtobject,
					wt.session.SessionHelper.manager.getPrincipal())) {
				wtobject = (WTObject) WorkInProgressHelper.service.checkin(
						(Workable) wtobject, "Automatically checked in");
				return wtobject;
			}

			return wtobject;
		}		
		
		
		public static boolean isCheckOut(Workable workable) {
			try {
				return WorkInProgressHelper.isCheckedOut(workable);
			} catch (WTException e) {
				 Debug.P("------>>>isCheckOut Error"+e.getMessage());
			}
			  return false;
		}
	    
		
		/**
		 * 检查是否存在该容器对象，没有就创建改容器对象
		 * @param wtContainerName
		 * @param containerType  创建容器对象的类型(1:产品库 2:存储库)
		 * @throws WTException
		 */
		public static WTContainer checkWTContainer(String orgName,String wtContainerName,String containerType)throws WTException{
			
			WTContainer container=null;
			try {
				container=getWTContainerByName(wtContainerName);
				if(container==null){//创建容器对象
					if("1".equals(containerType)){
						container=createPDMLinkProduct(orgName, wtContainerName, "", null);//TMT是组织名称
					}else{
						container=createLibrary(orgName, wtContainerName, "默认TMT存储库描述");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Debug.P(e.getMessage());
			}
			    return container;
		}
		
	
		/**
		 * 移动对象到容器下(包含产品库和存储库)
		 * @param object
		 * @param containerName
		 */
		public static void moveObject2Container(Persistable object,WTContainer container,Folder folder)throws Exception{
			 WTValuedHashMap map = new WTValuedHashMap();
			 if(container!=null){
				 if(object instanceof WTPart){
					 WTPart part=(WTPart)object;
					 part.setContainer(container);
					 map.put(part, folder);
				 }else if(object instanceof EPMDocument){
					 EPMDocument epm=(EPMDocument)object;
					 epm.setContainer(container);
					 map.put(epm, folder);
				 }else if(object instanceof WTDocument){
					 WTDocument doc=(WTDocument)object;
					 doc.setContainer(container);
					 map.put(doc, folder);
				 }
				 
				 FolderHelper.service.changeFolder(map);
				 Debug.P("-------->>>移动对象到容器成功!");
			 }

			
	  }
		
		
		
		/**
		 * 构造产品容器文件夹
		 * @param containerName
		 * @return
		 * @throws Exception
		 */
		public  static Folder createNewPath(WTContainer container) throws Exception {
			if(container==null) return null;
			WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);
			String folder_path="/Default";
			Debug.P("---------->>>Container Path:"+folder_path);
			Folder folder = null;
	        try{
	        	folder = FolderHelper.service.getFolder(folder_path, containerRef);
	        } catch (WTException e) {
	            try {
	                folder = FolderHelper.service.saveFolderPath(folder_path, containerRef);
	            } catch (WTException e1) {
	                e1.printStackTrace();
	            }
	        }
	        if (folder == null)
	        {
	        	throw new WTException("=--->>>create subfolder failed!");
	        }
	        return folder;
		}
		
		
		/**
		 * 设置大版本
		 * @param versioned
		 * @param s
		 */
	    public static void setVersion(Versioned versioned, String s) {
	        MultilevelSeries multilevelseries;
	        Mastered mastered;
	        VersionIdentifier versionidentifier;
	        try {
	            if (s == null || s.trim().length() == 0) {
	                s = null;
	                if (versioned.getVersionInfo() != null)
	                    return;
	            }
	        } catch (Exception exception) {
	          Debug.P(exception.getMessage());
	        }
	        multilevelseries = null;
	        mastered = versioned.getMaster();
	        if (mastered != null) {
	            String s1 = mastered.getSeries();
	            if (s1 == null) {
	                if ((versioned instanceof WTContained)
	                && ((WTContained) versioned).getContainer() != null) {
	                    try {
	                        multilevelseries = VersionControlHelper
	                                .getVersionIdentifierSeries(versioned);
	                        VersionControlServerHelper.changeSeries(mastered, multilevelseries
	                                .getUniqueSeriesName());
	                    } catch (VersionControlException e) {
	                    	Debug.P(e.getMessage());
	                    } catch (WTPropertyVetoException e) {
	                    	Debug.P(e.getMessage());
	                    } catch (WTException e) {
	                    	Debug.P(e.getMessage());
	                    }
	                }
	            } else {
	                try {
	                    multilevelseries = MultilevelSeries.newMultilevelSeries(s1);
	                } catch (SeriesException e) {
	                	Debug.P(e.getMessage());
	                }
	            }
	        }
	        if (multilevelseries == null) {
	            
	            try {
	                multilevelseries = MultilevelSeries.newMultilevelSeries(
	                        "wt.vc.VersionIdentifier", s);
	            } catch (Exception e) {
	            	Debug.P(e.getMessage());
	            }
	        }
	        if (s != null) {
	            try {
	                multilevelseries.setValueWithoutValidating(s.trim());
	            } catch (Exception e) {
	            	Debug.P(e.getMessage());
	            }
	        }
	        try {
	            versionidentifier = VersionIdentifier
	                    .newVersionIdentifier(multilevelseries);
	            VersionControlServerHelper.setVersionIdentifier(versioned,
	                    versionidentifier, false);
	        } catch (WTException e) {
	        	Debug.P(e.getMessage());
	        }
	    }
	    
	    /**
	     * 设置小版本
	     * @param iterated
	     * @param s
	     */
	    public static void setIteration(Iterated iterated, String s) {
	        try {
	            if (s != null) {
	                Series series = Series.newSeries("wt.vc.IterationIdentifier", s);
	                IterationIdentifier iterationidentifier = IterationIdentifier
	                        .newIterationIdentifier(series);
	                VersionControlHelper.setIterationIdentifier(iterated,
	                        iterationidentifier);
	            }
	        } catch (WTPropertyVetoException e) {
	        	Debug.P(e.getMessage());
	        } catch (SeriesException e) {
	        	Debug.P(e.getMessage());
	        } catch (WTException e) {
	        	Debug.P(e.getMessage());
	        }
	    }
	    
	    public static void setLifeCycle(WTContainerRef wtcontainerref,
	            LifeCycleManaged lifecyclemanaged, String s) {
	        if (s != null)
	            try {
	                LifeCycleHelper.setLifeCycle(lifecyclemanaged, LifeCycleHelper.service
	                        .getLifeCycleTemplate(s, wtcontainerref));
	            } catch (LifeCycleException e) {
	                Debug.P(e.getMessage());
	            } catch (WTPropertyVetoException e) {
	            	Debug.P(e.getMessage());
	            } catch (WTException e) {
	            	Debug.P(e.getMessage());
	            }
	    }
	    

		
public static void moveObject2OtherContainer(String containerOid,String targetFolderPath,String docOid){
	try{
	       ObjectIdentifier oid =  ObjectIdentifier.newObjectIdentifier(containerOid);
	       PDMLinkProduct product = ( PDMLinkProduct) wt.fc.PersistenceHelper.manager.refresh(oid);
	       WTContainerRef c_ref = WTContainerRef.newWTContainerRef(product);
	      
	       //Original folder
	       Folder folder1 = product.getDefaultCabinet();
	      
	       //Target folder: Move the doc to AA folder in the product
	       Folder folder2 = FolderHelper.service.getFolder(targetFolderPath, c_ref);
	 
	       //Get moved doc
	       ObjectIdentifier oidDoc =  ObjectIdentifier.newObjectIdentifier(docOid);
	       WTDocument doc = (wt.doc.WTDocument)  PersistenceHelper.manager.refresh(oidDoc);
	       
	       WTValuedMap objFolderMap = new WTValuedHashMap(1);
	       objFolderMap.put(doc, folder2);
	       WTCollection col = ContainerMoveHelper.service.moveAllVersions(objFolderMap);
	      } catch(Exception e){
	         e.printStackTrace();
	      }
}
		

/**
 * 设置EPM工作区属性
 * @param ws
 * @param epm
 * @param attr_name
 * @param attr_value
 * @throws WTException
 */
private static void setStringAttribute(EPMWorkspace ws, EPMDocument epm, String attr_name, String attr_value) throws WTException
{
   Map<String, String> attr_value_map = new HashMap<String, String>(1);
   attr_value_map.put(attr_name, attr_value);
 
   WTKeyedMap keyed_map = new WTKeyedHashMap(1);
   keyed_map.put(epm, attr_value_map);
   EPMWorkspaceHelper.manager.setAttributes(ws, keyed_map);
}
		



/**
 * 获取URl字符串中的
 * @param url
 * @return
 */
 public static  Map<String, String> getURLParams(String url) {
	    Map<String, String> paramMap = new HashMap<String, String>();
	    if (!StringUtils.isEmpty(url)) {// 如果URL不是空字符串
	        url = url.substring(url.lastIndexOf('?') + 1);
	        String paramaters[] = url.split("&");
	        for (String param : paramaters) {
	            String values[] = param.split("=");
	            System.out.println(values[1]);
	            paramMap.put(values[0], values[1]);
	        }
	    }
	       return paramMap;
}
 
 /**
  * 解析<a href=*>里的链接地址
  * @param str
  * @return
  */
 private static String  getMatchHrefUrl(String str){
	String result=null; 
    String patternString = "\\s*(?i)[h|H][r|R][e|E][f|F]\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
 	Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
	Matcher matcher = pattern.matcher(str);
	while (matcher.find()) {
		String link=matcher.group();
		link=link.replaceAll("[h|H][r|R][e|E][f|F]\\s*=\\s*(['|\"]*)", "");
		result=link.replaceAll("['|\"]", "");
	   }
	     return result;
    }
 
 
    /**
     * 获取文件的MD5码
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws WTException 
     * @throws PropertyVetoException 
     */
	public static String getMd5ByFile(EPMDocument epm) throws FileNotFoundException, WTException, PropertyVetoException {
		String value = null;
		ContentHolder holder = ContentHelper.service.getContents(epm);
		ContentItem item = ContentHelper
				.getPrimary((FormatContentHolder) holder);
		// ContentServerHelper.service.findContentStream((ApplicationData)item);
		ApplicationData appData = (ApplicationData) item;
		if(appData==null){
			return null;
		}
		FileInputStream in = (FileInputStream) ContentServerHelper.service.findContentStream(appData);
		try {
			MappedByteBuffer byteBuffer = in.getChannel().map(
					FileChannel.MapMode.READ_ONLY, 0, in.read());
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(byteBuffer);
			BigInteger bi = new BigInteger(1, md5.digest());
			value = bi.toString(16);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return value;
	}  

 public static void main(String[] args) throws Exception {
	 
	// EPMDocument epm =(EPMDocument)Utils.getWCObject(EPMDocument.class,args[0]);
//	 Debug.P("EPMDocument---->"+epm.getNumber()+"  version--->"+epm.getVersionIdentifier().getValue()+"  iteration----->"+epm.getIterationIdentifier().getValue());
	 WTPart part =(WTPart)Utils.getWCObject(WTPart.class,args[0]);
	 Debug.P("WTPart---->"+part.getNumber()+"  version--->"+part.getVersionIdentifier().getValue()+"  iteration----->"+part.getIterationIdentifier().getValue());

	Debug.P("args[0]--->"+args[0]+" args[1]--->"+args[1]);
	 RemDeleteWTObject(part, args[1]);
	 
}
 
 
}
