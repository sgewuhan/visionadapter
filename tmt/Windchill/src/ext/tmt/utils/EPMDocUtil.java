package ext.tmt.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.build.EPMBuildHistory;
import wt.epm.build.EPMBuildRule;
import wt.epm.structure.EPMDescribeLink;
import wt.epm.structure.EPMReferenceLink;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.iba.definition.StringDefinition;
import wt.iba.value.StringValue;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.pds.StatementSpec;
import wt.query.ConstantExpression;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.query.WhereExpression;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.dataops.delete.processors.DeleteValidator;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.dataops.delete.DeleteTask;

@SuppressWarnings("all")
public class EPMDocUtil {

	public static void main(String[] args) throws WTException {
		getAllEPMDocument();
	}

	/**
	 * 获取系统内所有的最新版本(最新大版本+最新小版本)的的EPMDocument
	 * 
	 * @return
	 * @throws WTException
	 */
	public static List<EPMDocument> getAllEPMDocument() throws WTException {
		List<EPMDocument> epmList = new ArrayList<EPMDocument>();
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition latestIteration = new SearchCondition(
				EPMDocument.class, "iterationInfo.latest",
				SearchCondition.IS_TRUE);
		qs.appendWhere(latestIteration);
		Debug.P("sql---->" + qs);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		Debug.P("All EPMDocument size-------1------>" + qr.size());
		qr = new LatestConfigSpec().process(qr);
		Debug.P("All EPMDocument size-------2------>" + qr.size());
		while (qr.hasMoreElements()) {
			Object object = (Object) qr.nextElement();
			EPMDocument epmdoc = (EPMDocument) object;
			epmList.add(epmdoc);
		}
		return epmList;
	}

	/**
	 * 获取系统内所有的最新版本(最新大版本+最新小版本)的的WTDocument
	 * 
	 * @return
	 * @throws WTException
	 */
	public static List<WTDocument> getAllWTDocument() throws WTException {
		List<WTDocument> docList = new ArrayList<WTDocument>();
		QuerySpec qs = new QuerySpec(WTDocument.class);
		SearchCondition latestIteration = new SearchCondition(WTDocument.class,
				"iterationInfo.latest", SearchCondition.IS_TRUE);
		qs.appendWhere(latestIteration);
		Debug.P("sql---->" + qs);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		Debug.P("All WTDocument size-------1------>" + qr.size());
		qr = new LatestConfigSpec().process(qr);
		Debug.P("All WTDocument size-------2------>" + qr.size());
		while (qr.hasMoreElements()) {
			Object object = (Object) qr.nextElement();
			WTDocument doc = (WTDocument) object;
			docList.add(doc);
		}
		return docList;
	}

	/**
	 * 获取系统内所有的最新版本(最新大版本+最新小版本)的的WTPart
	 * 
	 * @return
	 * @throws WTException
	 */
	public static List<WTPart> getAllWTPart() throws WTException {
		List<WTPart> partList = new ArrayList<WTPart>();
		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition latestIteration = new SearchCondition(WTPart.class,
				"iterationInfo.latest", SearchCondition.IS_TRUE);
		qs.appendWhere(latestIteration);
		Debug.P("sql---->" + qs);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		Debug.P("All WTPart size-------1------>" + qr.size());
		qr = new LatestConfigSpec().process(qr);
		Debug.P("All WTPart size-------2------>" + qr.size());
		while (qr.hasMoreElements()) {
			Object object = (Object) qr.nextElement();
			WTPart part = (WTPart) object;
			partList.add(part);
		}
		return partList;
	}

	// 根据编号得到EPMmaster
	public static EPMDocumentMaster getEPMMasterByNumber(String number)
			throws WTException {
		EPMDocumentMaster epmmaster = null;
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(EPMDocumentMaster.class);
		SearchCondition sc = new SearchCondition(
				wt.epm.EPMDocumentMaster.class,
				wt.epm.EPMDocumentMaster.NUMBER, SearchCondition.LIKE, number);
		qs.appendSearchCondition(sc);
		qr = PersistenceHelper.manager.find(qs);

		while (qr.hasMoreElements()) {
			epmmaster = (EPMDocumentMaster) qr.nextElement();
			break;
		}

		return epmmaster;
	}

	/**
	 * 根据编号得到EPM
	 * 
	 * @author blueswang
	 * @param number
	 * @return
	 * @throws WTException
	 * @return EPMDocument
	 * @Description
	 */
	public static EPMDocument getEPMDoc(String number) throws WTException {
		EPMDocument EPMDoc = null;
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				"master>number", SearchCondition.EQUAL, number);
		qs.appendSearchCondition(sc);
		SearchCondition latestIteration = new SearchCondition(
				EPMDocument.class, "iterationInfo.latest",
				SearchCondition.IS_TRUE);
		qs.appendWhere(sc);
		qs.appendAnd();
		qs.appendWhere(latestIteration);
		qr = new LatestConfigSpec().process(qr);
		qr = PersistenceHelper.manager.find(qs);

		while (qr.hasMoreElements()) {
			EPMDoc = (EPMDocument) qr.nextElement();
			break;
		}
		return EPMDoc;
	}

	/**
	 * 根据名称得到EPM
	 * 
	 * @author blueswang
	 * @param number
	 * @return
	 * @throws WTException
	 * @return EPMDocument
	 * @Description
	 */
	public static EPMDocument getEPMDocByName(String name) throws WTException {
		EPMDocument EPMDoc = null;
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				"master>name", SearchCondition.LIKE, name);
		SearchCondition latestIteration = new SearchCondition(
				EPMDocument.class, "iterationInfo.latest",
				SearchCondition.IS_TRUE);
		qs.appendWhere(sc);
		qs.appendAnd();
		qs.appendWhere(latestIteration);

		qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements()) {
			EPMDoc = (EPMDocument) qr.nextElement();
			break;
		}
		return EPMDoc;
	}

	/**
	 * 根据编码得到EPM
	 * 
	 * @author blueswang
	 * @param number
	 * @return
	 * @throws WTException
	 * @return EPMDocument
	 * @Description
	 */
	public static EPMDocument getEPMDocByNumber(String number)
			throws WTException {
		EPMDocument EPMDoc = null;
		QuerySpec qs = null;
		QueryResult qr = null;
		qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class,
				"master>number", SearchCondition.LIKE, number);
		qs.appendSearchCondition(sc);
		qr = PersistenceHelper.manager.find(qs);

		while (qr.hasMoreElements()) {
			EPMDoc = (EPMDocument) qr.nextElement();
			break;
		}
		return EPMDoc;
	}

	/**
	 * 根据软属性的名称查询出对应的EPMDocument对象
	 * 
	 * @param partNum
	 * @return
	 * @throws Exception
	 */
	public static List<Persistable> getEPMDocumentByIBA(String IbaKey,
			String IbaValue) throws Exception {
		List<Persistable> objects = new ArrayList<Persistable>();
		Debug.P("----->>>>>IbaKey:" + IbaKey + "  IbaValue:" + IbaValue);
		if (StringUtils.isNotEmpty(IbaValue) && StringUtils.isNotEmpty(IbaKey)) {
			String sql = "select M1.NAME,M1.DOCUMENTNUMBER  FROM  STRINGVALUE v1 ,STRINGDEFINITION d1,EPMDOCUMENT e1,EPMDOCUMENTMASTER m1 where D1.IDA2A2=v1.ida3a6 and v1.IDA3A4=e1.IDA2A2 and e1.IDA3MASTERREFERENCE=M1.IDA2A2 and d1.name=?  and v1.value=? ";
			String[] params = { IbaKey, IbaValue.trim() };
			Debug.P(">>>>>sql:" + sql);
			List<Hashtable<String, String>> result = UserDefQueryUtil
					.commonQuery(sql, params);
			if (result != null && result.size() > 0) {
				Debug.P("------>>>Get EpmDocument Result Size:" + result.size());
				for (int i = 0; i < result.size(); i++) {
					Hashtable<String, String> data_rows = result.get(i);
					for (Iterator<?> ite = data_rows.keySet().iterator(); ite
							.hasNext();) {
						String key = (String) ite.next();
						if ("DOCUMENTNUMBER".equals(key)) {
							String epmdoc_Num = data_rows.get(key);
							Persistable object = GenericUtil
									.getObjectByNumber(epmdoc_Num);
							objects.add(object);
						}
					}
				}
			}
		}
		return objects;
	}

	/**
	 * 得到part关联的图档
	 * 
	 * @author blueswang
	 * @param part
	 * @return
	 * @throws WTException
	 * @return EPMDocument
	 * @Description
	 */
	public static EPMDocument getActiveEPMDocument(WTPart part)
			throws WTException {
		EPMDocument cadDoc = null;
		QueryResult qr = PersistenceHelper.manager.navigate(part,
				"buildSource", EPMBuildRule.class, true);
		while (qr.hasMoreElements()) {
			Object wto = (Object) qr.nextElement();
			if (wto instanceof EPMDocument) {
				cadDoc = (EPMDocument) wto;
				break;
			}
		}
		if (cadDoc == null) {
			qr = PersistenceHelper.manager.navigate(part,
					EPMBuildHistory.BUILT_BY_ROLE, EPMBuildHistory.class, true);
			while (qr.hasMoreElements()) {
				Object wto = (Object) qr.nextElement();
				if (wto instanceof EPMDocument) {
					cadDoc = (EPMDocument) wto;
					break;
				}
			}
		}
		return cadDoc;
	}

	/**
	 * 得到part的说明文档
	 * 
	 * @author blueswang
	 * @param part
	 * @return
	 * @throws WTException
	 * @return Vector
	 * @Description
	 */
	public static Vector getNoActiveEPMDocuments(WTPart part)
			throws WTException {
		Vector cadDocV = new Vector();
		EPMDocument cadDoc = null;

		QueryResult qr = WTPartHelper.service.getDescribedByDocuments(part);
		while (qr.hasMoreElements()) {
			Object wto = (Object) qr.nextElement();
			if (wto instanceof EPMDocument) {
				cadDoc = (EPMDocument) wto;
				cadDocV.addElement(cadDoc);
			}
		}

		return cadDocV;
	}

	public static boolean isLinkedEPM(EPMDocumentMaster epmmaster,
			ArrayList<EPMDocument> EPMList) {
		boolean result = false;
		for (int i = 0; i < EPMList.size(); i++) {
			String empNum = EPMList.get(i).getNumber();
			if (epmmaster.NUMBER.equals(empNum)) {
				result = true;
			}
		}
		return result;

	}

	/**
	 * 获取部件关联的EPM文档
	 * 
	 * @author blueswang
	 * @param part
	 * @return
	 * @throws WTException
	 * @return ArrayList<EPMDocument>
	 * @Description
	 */
	public static ArrayList<EPMDocument> getEPMDocbyPart(WTPart part)
			throws WTException {
		ArrayList<EPMDocument> result = new ArrayList<EPMDocument>();

		QueryResult epmdoc = WTPartHelper.service.getDescribedByDocuments(part);
		while (epmdoc.hasMoreElements()) {
			Object obj = epmdoc.nextElement();
			if (obj instanceof EPMDocument) {
				EPMDocument doc = (EPMDocument) obj;
				result.add(doc);
			}
		}
		return result;
	}

	/**
	 * 上载主文档
	 * 
	 * @author Eilaiwang
	 * @param doc
	 * @param contName
	 * @param PRIMARY_FILE
	 * @throws Exception
	 * @return void
	 * @Description
	 */
	public static void uploadEPMApplicationData(EPMDocument doc,
			String contName, String PRIMARY_FILE) throws Exception {
		doc = (EPMDocument) PersistenceHelper.manager.refresh(doc);
		ContentItem ci = ContentHelper.service.getPrimary(doc);
		System.out.println("主文件是" + ci);
		if (ci != null) {
			PersistenceServerHelper.manager.remove(ci);
		}
		doc = (EPMDocument) PersistenceHelper.manager.refresh(doc);
		if (!PRIMARY_FILE.equals("") || PRIMARY_FILE != null) {
			System.out.println("PRIMARY_FILE--->" + PRIMARY_FILE);
			ApplicationData applicationdata = ApplicationData
					.newApplicationData(doc);
			applicationdata.setRole(ContentRoleType.PRIMARY);
			applicationdata.setFileName(contName);

			File file = new File(PRIMARY_FILE);
			FileInputStream primary = new FileInputStream(file);
			ContentServerHelper.service.updateContent(doc, applicationdata,
					primary);
		}
	}

	/**
	 * 根据WTPart和EPMDocument得到EPMDescribeLink
	 * 
	 * @param part
	 * @param epmDoc
	 * @return EPMDescribeLink
	 * @throws WTException
	 */
	public static EPMDescribeLink getEPMDescribeLink(WTPart part,
			EPMDocument epmDoc) throws WTException {
		QueryResult qr = PersistenceHelper.manager.find(
				wt.epm.structure.EPMDescribeLink.class, part,
				EPMDescribeLink.DESCRIBES_ROLE, epmDoc);
		Debug.P(qr.size());
		if (qr == null || qr.size() == 0) {
			return null;
		} else {
			EPMDescribeLink epmdescribelink = (EPMDescribeLink) qr
					.nextElement();
			return epmdescribelink;
		}
	}

	public static EPMDescribeLink getAllEPMDescribeLink(WTPart part,
			EPMDocument epmDoc) throws WTException {
		QueryResult qr = PersistenceHelper.manager.find(
				wt.epm.structure.EPMDescribeLink.class, part,
				EPMDescribeLink.ALL_ROLES, epmDoc);
		Debug.P(qr.size());
		if (qr == null || qr.size() == 0) {
			return null;
		} else {
			EPMDescribeLink epmdescribelink = (EPMDescribeLink) qr
					.nextElement();
			return epmdescribelink;
		}
	}

	public static EPMReferenceLink getEPMReferenceLink(WTPart part,
			EPMDocument epmDoc) throws WTException {
		QueryResult qr = PersistenceHelper.manager.find(
				wt.epm.structure.EPMReferenceLink.class, part,
				EPMReferenceLink.REFERENCES_ROLE, epmDoc);
		Debug.P(qr.size());
		if (qr == null || qr.size() == 0) {
			return null;
		} else {
			EPMReferenceLink epmReferencelink = (EPMReferenceLink) qr
					.nextElement();
			return epmReferencelink;
		}
	}

	/**
	 * 判断WTPart和EPMDocument是否存在EPMDescribeLink关系
	 * 
	 * @param part
	 * @param epmDoc
	 * @return EPMDescribeLink
	 * @throws WTException
	 */
	public static boolean isDescribeLink(WTPart part, EPMDocument epmDoc)
			throws WTException {
		boolean isLink = true;
		QueryResult qr = PersistenceHelper.manager.find(
				wt.epm.structure.EPMDescribeLink.class, part,
				EPMDescribeLink.DESCRIBES_ROLE, epmDoc);
		if (qr == null || qr.size() == 0) {
			isLink = false;
		}
		return isLink;
	}

	/**
	 * 判断EPMDocument和EPMDocument是否存在EPMReferenceLink关系
	 * 
	 * @param epmDoc1
	 * @param epmDoc2
	 * @return EPMReferenceLink
	 * @throws WTException
	 */
	public static boolean isReferenceLink(EPMDocument epmdoc1,
			EPMDocument epmdoc2) throws Exception {
		if (epmdoc1 == null || epmdoc2 == null) {
			return false;
		}
		wt.fc.QueryResult qr = wt.epm.structure.EPMStructureHelper.service
				.navigateReferences(epmdoc1, null, true);
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof EPMDocumentMaster) {
				EPMDocumentMaster epmdoc = (EPMDocumentMaster) obj;
				if (epmdoc2.getNumber().equals(epmdoc.getNumber())) {
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * 根据WTPart和EPMDocument,创建EPMDescribeLink
	 * 
	 * @param part
	 * @param epmDoc
	 * @return EPMDescribeLink
	 * @throws WTException
	 */
	public static EPMDescribeLink createEPMDescribeLink(WTPart part,
			EPMDocument epmDoc) throws WTException {
		// Judge EPMDescribeLink has exist.
		EPMDescribeLink link_old = getEPMDescribeLink(part, epmDoc);
		if (link_old != null) {
			return link_old;
		} else {
			EPMDescribeLink link_new;
			try {
				link_new = EPMDescribeLink.newEPMDescribeLink(part, epmDoc);
				PersistenceServerHelper.manager.insert(link_new);
				link_new = (EPMDescribeLink) PersistenceHelper.manager
						.refresh(link_new);
			} catch (WTException e) {
				throw e;
			}
			Debug.P("Successfully created EPMDescribeLink with WTPart:"
					+ part.getNumber() + "and EPMDocument:"
					+ epmDoc.getNumber() + " .");
			return link_new;

		}
	}

	public static EPMBuildHistory getEPMBuildHistory(String epmoid,
			String partoid) throws WTException {
		EPMBuildHistory object = null;
		QuerySpec qs = new QuerySpec();
		qs.setAdvancedQueryEnabled(true);
		qs.appendClassList(EPMBuildHistory.class, true);
		TableColumn column1 = new TableColumn("A0", "idA3A5");
		SearchCondition sc1 = new SearchCondition(column1,
				SearchCondition.EQUAL, new ConstantExpression(epmoid));
		qs.appendWhere(sc1);
		qs.appendAnd();
		TableColumn column2 = new TableColumn("A0", "idA3B5");
		SearchCondition sc2 = new SearchCondition(column2,
				SearchCondition.EQUAL, new ConstantExpression(partoid));
		qs.appendWhere(sc2);

		// Debug.P("sql----->"+qs);
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		Debug.P("qr----->" + qr.size());
		if (qr.hasMoreElements()) {
			Object[] objects = (Object[]) qr.nextElement();
			object = (EPMBuildHistory) objects[0];
		}
		return object;

	}

	/**
	 * 根据图纸获取CAD图纸与部件的关联关系
	 * 
	 * @param epm
	 * @return
	 */
	public static ArrayList searchAllEPMBuildRule(EPMDocument epm) {
		ArrayList list = new ArrayList();
		try {
			QuerySpec qs = new QuerySpec(EPMBuildRule.class);
			qs.appendWhere(
					new SearchCondition(EPMBuildRule.class,
							"roleAObjectRef.key.branchId", "=",
							VersionControlHelper.getBranchIdentifier(epm)),
					new int[1]);

			qs.setAdvancedQueryEnabled(true);
			QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			if (qr.size() > 0)
				list.addAll(qr.getObjectVectorIfc().getVector());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 根据获取CAD图纸与部件的关联关系
	 * 
	 * @param epm
	 * @return
	 */
	public static ArrayList searchAllEPMBuildRuleByPart(WTPart part) {
		ArrayList list = new ArrayList();
		try {
			QuerySpec qs = new QuerySpec(EPMBuildRule.class);
			qs.appendWhere(
					new SearchCondition(EPMBuildRule.class,
							"roleBObjectRef.key.branchId", "=",
							VersionControlHelper.getBranchIdentifier(part)),
					new int[1]);

			qs.setAdvancedQueryEnabled(true);
			QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			if (qr.size() > 0)
				list.addAll(qr.getObjectVectorIfc().getVector());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

}
