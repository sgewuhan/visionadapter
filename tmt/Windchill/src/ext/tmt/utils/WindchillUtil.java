package ext.tmt.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import wt.csm.navigation.ClassificationNode;
import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.fc.Identified;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.WTReference;
import wt.folder.CabinetBased;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.iba.definition.StringDefinition;
import wt.iba.value.ReferenceValue;
import wt.iba.value.StringValue;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.org.WTOrganization;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressState;
import wt.vc.wip.Workable;

public class WindchillUtil {
	
	/**
	 * 根据编号规则,为零部件改变编号
	 * 
	 * @param part
	 * @param number
	 * @throws WTException
	 */
	public static void changePartNumber(WTPart part, String number)throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		WTUser currentuser = (WTUser) SessionHelper.manager.getPrincipal();
		SessionHelper.manager.setAdministrator();
		Transaction tx = null;
		try {
			tx = new Transaction();
			tx.start();
			Identified identified = (Identified) part.getMaster();
			String name = part.getName();
			WTOrganization org = part.getOrganization();
			WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) identified, name, number, org);
			tx.commit();
			tx = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tx != null)
				tx.rollback();
			SessionHelper.manager.setPrincipal(currentuser.getAuthenticationName());
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}
	/**
	 * 是否为修订版本或者新视图版本
	 * 
	 * @param wtdoc
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean isReviseVersion(Workable workable) {
		boolean revise = false;
		if (workable == null) {
			return revise;
		}

		boolean flag = true;
		Workable firstIterated = null;
		try {
			flag = SessionServerHelper.manager.setAccessEnforced(false);
			// 获取第一个版本对象
			QueryResult qr = VersionControlHelper.service
					.allIterationsFrom(workable);
			Vector<Workable> its = qr.getObjectVectorIfc().getVector();
			for (int i = 0; i < its.size(); i++) {
				Workable workableTemp = its.get(i);
				boolean hasPredecessor = VersionControlHelper
						.hasPredecessor(workableTemp);
				if (!hasPredecessor) {
					firstIterated = workableTemp;
					break;
				}
			}
			if (firstIterated != null) {
				revise = !VersionControlHelper.inSameBranch(workable,
						firstIterated);
			}
		} catch (PersistenceException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} finally {
			flag = SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return revise;
	}
	
    /**
     * 
     * @author Eilaiwang
     * @param name
     * @return
     * @return WTContainer
     * @Description
     */
    public static WTContainer getContainers(String name){
   	  WTContainer container = null;
   	 try {
			QuerySpec qs = new QuerySpec(WTContainer.class);
			SearchCondition sc = new SearchCondition(WTContainer.class,WTContainer.NAME,"=",name);
			qs.appendWhere(sc);
			QueryResult qr = PersistenceHelper.manager.find(qs);
           while(qr.hasMoreElements()){
           	 container = (WTContainer)qr.nextElement();
           }
		} catch (Exception e) {
			e.printStackTrace();
		}
   	 return container;
    }
	
	public static Folder createFolder(String location, WTContainerRef cref) {
		if (location == null)
			location = "/Default/";
		Folder folder = null;
		String[] locations = location.split("/");
		String alocation = "";
		for (int i = 0; i < locations.length; i++) {
			String str = locations[i];
			if (str == null || str.trim().length() == 0)
				continue;
			alocation += "/" + str;
			Debug.P("folders -->" + alocation);
			try {
				folder = FolderHelper.service.getFolder(alocation, cref);
			} catch (WTException e) {
				folder = null;
			}
			try {
				if (folder == null)
					folder = FolderHelper.service.createSubFolder(alocation,
							cref);
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return folder;
	}
	
	public static boolean isCheckout(Workable workable) throws WTException {
		return WorkInProgressHelper.isCheckedOut(workable);
	}
	
	/**
	 * 根据文件名查找最新版文件
	 * 
	 * @param docName
	 * @return
	 * @throws WTException
	 */
	public static WTDocument getDocByName(String docName) throws WTException {
		QuerySpec qs = new QuerySpec();
		int docIndex = qs.appendClassList(WTDocument.class, true);

		qs.appendWhere(new SearchCondition(WTDocument.class,
				WTAttributeNameIfc.LATEST_ITERATION, SearchCondition.IS_TRUE),
				new int[] { docIndex });

		qs.appendAnd();
		qs.appendWhere(new SearchCondition(WTDocument.class, "master>name",
				SearchCondition.EQUAL, docName), new int[] { docIndex });

		qs.appendOrderBy(new OrderBy(new ClassAttribute(WTDocument.class,
				"versionInfo.identifier.versionId"), true),
				new int[] { docIndex });

		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		if (qr != null && qr.hasMoreElements()) {
			Persistable[] p = (Persistable[]) qr.nextElement();
			WTDocument wtDoc = (WTDocument) p[0];
			return (WTDocument)getLatestObject((Master)wtDoc.getMaster());
		}
		return null;
	}
	
	private static RevisionControlled getLatestObject(Master master)
			throws WTException {
		RevisionControlled revisionControlled = null;
		if (master != null) {
			try {
				QueryResult queryResult = VersionControlHelper.service
						.allVersionsOf(master);
				return (RevisionControlled) queryResult.nextElement();
			} catch (WTException wte) {
				wte.printStackTrace();
			}
		}
		return revisionControlled;
	}
	
	/**
	 * 修订系统对象，常用系统类型如下 EPMDocument, RevisionControlled, WTDocument, WTPart,
	 * WTProduct, WTProductInstance2
	 * 
	 * @param versioned
	 * @return
	 */
	@SuppressWarnings("all")
	public static Versioned newVersion(Versioned versioned) {
		if (versioned == null) {
			return null;
		}
		Persistable persistable = null;
		try {
			versioned = VersionControlHelper.service.newVersion(versioned);
			if (versioned != null) {
				persistable = PersistenceHelper.manager.save(versioned);
			}
		} catch (VersionControlException e) {
			e.printStackTrace();
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		versioned = (Versioned) persistable;
		return versioned;
	}
	@SuppressWarnings("all")
	public static Folder getFolderByName(String subfolderName) throws WTException {
		QuerySpec qs = new QuerySpec(SubFolder.class);
		qs.appendWhere(new SearchCondition(SubFolder.class, SubFolder.NAME,
				SearchCondition.EQUAL, subfolderName), new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		if (qr.hasMoreElements()) {
			 return (SubFolder)qr.nextElement();
		}
		return null;
	}
	
	public static SubFolder getSubFolderByName(String subFolderName)throws WTException{
		QuerySpec qs = new QuerySpec(SubFolder.class);
		qs.appendWhere(new SearchCondition(SubFolder.class, SubFolder.NAME,
				SearchCondition.EQUAL, subFolderName));
		Debug.P(qs);
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		Debug.P(qr);
		if (qr.hasMoreElements()) {
			 return (SubFolder)qr.nextElement();
		}
		return null;
	}
	
	
	@SuppressWarnings("all")
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

	public static WTObject undoCheckOut(WTObject wtobject)
			throws WorkInProgressException, WTPropertyVetoException,
			PersistenceException, WTException {
		if (wt.vc.wip.WorkInProgressHelper.isCheckedOut((Workable) wtobject,
				wt.session.SessionHelper.manager.getPrincipal())) {
			wtobject = (WTObject) WorkInProgressHelper.service
					.undoCheckout((Workable) wtobject);
			return wtobject;
		}
		return wtobject;
	}
	
	/**
	 * 根据part查询其所属分类
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static List<ClassificationNode> getClassificationNodeByPart(WTPart part) throws WTException {
		List<ClassificationNode> cnList = new ArrayList<ClassificationNode>();
		QuerySpec qs = new QuerySpec();
		int wtpartIndex = qs.addClassList(WTPart.class, false);
		int referenceValueIndex = qs.appendClassList(ReferenceValue.class, false);
		int classificationNodeIndex = qs.appendClassList(ClassificationNode.class, true);
		
		WhereExpression we = new SearchCondition(ReferenceValue.class,
				ReferenceValue.IBAHOLDER_REFERENCE + ".key.id",WTPart.class,
				WTAttributeNameIfc.ID_NAME);
		qs.appendWhere(we, new int[] { referenceValueIndex, wtpartIndex });
		qs.appendAnd();
		
		we = new SearchCondition(ReferenceValue.class,
				ReferenceValue.IBAREFERENCEABLE_REFERENCE + ".key.id",
				ClassificationNode.class,
				WTAttributeNameIfc.ID_NAME);
		qs.appendWhere(we, new int[]{ referenceValueIndex, classificationNodeIndex });
		qs.appendAnd();
		
		we = new SearchCondition(WTPart.class,
				WTAttributeNameIfc.OBJECT_IDENTIFIER, SearchCondition.EQUAL,
				PersistenceHelper.getObjectIdentifier(part));
		qs.appendWhere(we, new int[]{ wtpartIndex });
		
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
		while(qr.hasMoreElements()) {
			Object[] obj = (Object[])qr.nextElement();
			cnList.add((ClassificationNode)obj[0]);
		}
		return cnList;
	}
	
	/**
	 * 根据部件获取其分类属性
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static List<String> getClassificationAttributesByPart(WTPart part)
			throws WTException {
		List<String> caList = new ArrayList<String>();
		QuerySpec qs = new QuerySpec();
		int wtpartIndex = qs.addClassList(WTPart.class, false);
		int referenceValueIndex = qs.appendClassList(ReferenceValue.class, false);
		int classificationNodeIndex = qs.appendClassList(ClassificationNode.class, false);
		int stringDefinitionIndex = qs.addClassList(StringDefinition.class, true);
		int stringValueIndex = qs.addClassList(StringValue.class, false);

		WhereExpression we = new SearchCondition(ReferenceValue.class,
				ReferenceValue.IBAHOLDER_REFERENCE + ".key.id", WTPart.class,
				WTAttributeNameIfc.ID_NAME);
		qs.appendWhere(we, new int[] { referenceValueIndex, wtpartIndex });
		qs.appendAnd();

		we = new SearchCondition(ReferenceValue.class,
				ReferenceValue.IBAREFERENCEABLE_REFERENCE + ".key.id",
				ClassificationNode.class, WTAttributeNameIfc.ID_NAME);
		qs.appendWhere(we, new int[] { referenceValueIndex,
				classificationNodeIndex });
		qs.appendAnd();

		we = new SearchCondition(StringValue.class,
				StringValue.DEFINITION_REFERENCE + ".key.id",
				StringDefinition.class, WTAttributeNameIfc.ID_NAME);
		qs.appendWhere(we,
				new int[] { stringValueIndex, stringDefinitionIndex });
		qs.appendAnd();

		we = new SearchCondition(StringValue.class,
				"theIBAHolderReference.key.id", ClassificationNode.class,
				WTAttributeNameIfc.ID_NAME);
		qs.appendWhere(we, new int[] { stringValueIndex,
				classificationNodeIndex });
		qs.appendAnd();

		we = new SearchCondition(WTPart.class,
				WTAttributeNameIfc.OBJECT_IDENTIFIER, SearchCondition.EQUAL,
				PersistenceHelper.getObjectIdentifier(part));
		qs.appendWhere(we, new int[] { wtpartIndex });

		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while (qr.hasMoreElements()) {
			Object[] obj = (Object[]) qr.nextElement();
			StringDefinition sd = (StringDefinition) obj[0];
			caList.add(sd.getName());
		}
		return caList;
	}
	
	/**根据对象获得其OID
	 * @param obj
	 * @return
	 * @throws WTException
	 */
	public static String getOidString(Persistable obj) throws WTException {
		ReferenceFactory rf = new ReferenceFactory();
		WTReference wtrf = rf.getReference(obj.toString());
		return rf.getReferenceString(wtrf);
	}
	
	/**根据oid获得该对象
	 * @param oid
	 * @return
	 * @throws WTRuntimeException
	 * @throws WTException
	 */
	public static Persistable getObjectByOid(String oid)
			throws WTRuntimeException, WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
	//	WTUser currentuser = (WTUser) SessionHelper.manager.getPrincipal();
	//	SessionHelper.manager.setAdministrator();
		Persistable p =null;
		try{
			ReferenceFactory rf = new ReferenceFactory();
			 p = rf.getReference(oid).getObject();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
		//	SessionHelper.manager.setPrincipal(currentuser.getAuthenticationName());
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return p;
	}
	
	public static String getContaintOidByFolder(SubFolder folder){
		String containerOid = "";
		WTContainer container = null;
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		//	WTUser currentuser = (WTUser) SessionHelper.manager.getPrincipal();
		//	SessionHelper.manager.setAdministrator();
			Persistable p =null;
		try {
			if (folder != null) {
				container = folder.getContainer();
				Debug.P("container------->" + container);
				containerOid = container.toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// SessionHelper.manager.setPrincipal(currentuser.getAuthenticationName());
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return containerOid;
	}
}
