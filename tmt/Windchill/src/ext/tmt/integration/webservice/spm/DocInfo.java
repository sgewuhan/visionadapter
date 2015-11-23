package ext.tmt.integration.webservice.spm;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;

import com.mongodb.BasicDBObject;

import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;

public class DocInfo {

	public static final String F_BPS_WORKFLOW_ID = "BPS_WORKFLOW_ID";
	public static final String F_OPERATION = "OPERATION";
	public static final String F_PART_NUMBER = "PART_NUMBER";
	public static final String F_JSGGSNUMBER = "JSGGSNUMBER";
	public static final String F_PRODUCTNUMBER = "PRODUCTNUMBER";
	public static final String F_PRIMARY_LOCATION = "PRIMARY_LOCATION";
	public static final String F_ATT_LOCATION = "ATT_LOCATION";
	public static final String F_CREATOR = "CREATOR";
	public static final String F_TYPE = "TYPE";
	public static final String F_XIAFA = "XIAFA";
	private BasicDBObject _data;
	private PartInfo partInfo;
	private WTPart part = null;

	public DocInfo() {
		partInfo = new PartInfo();
	}

	public PartInfo getPartInfo() {
		return partInfo;
	}

	public void checkAndSetBpsWorkflowId(String value) throws Exception {
		partInfo.checkAndSetBpsWorkflowId(value);
	}

	public void checkAndSetOperation(String value) throws Exception {
		partInfo.checkAndSetOperation(value);

	}

	public void checkAndSetPartNumber(String value) throws Exception {
		partInfo.checkAndSetNumber(value);
	}

	public String getPartNumber() {
		return partInfo.getNumber();
	}

	public void setJsggsNumber(String value) {
		_data.put(F_JSGGSNUMBER, value);
	}

	public String getJsggsNumber() {
		return (String) _data.get(F_JSGGSNUMBER);
	}

	public void setProductNumber(String value) {
		_data.put(F_PRODUCTNUMBER, value);
	}

	public String getProductNumber() {
		return (String) _data.get(F_PRODUCTNUMBER);
	}

	public void setPrimaryLocation(String value) {
		_data.put(F_PRIMARY_LOCATION, value);
	}

	public String getPrimaryLocation() {
		return (String) _data.get(F_PRIMARY_LOCATION);
	}

	public void addAttLocations(String value) {
		List<String> deleteFactory = getAttLocations();
		deleteFactory.add(value);
		setAttLocations(deleteFactory);
	}

	public void setAttLocations(List<String> deleteFactory) {
		_data.put(F_ATT_LOCATION, deleteFactory);
	}

	@SuppressWarnings("unchecked")
	public List<String> getAttLocations() {
		return (List<String>) _data.get(F_ATT_LOCATION);
	}

	public void setCreator(String value) {
		_data.put(F_CREATOR, value);
	}

	public String getCreator() {
		return (String) _data.get(F_CREATOR);
	}

	public void setType(String value) {
		_data.put(F_TYPE, value);
	}

	public String getType() {
		return (String) _data.get(F_TYPE);
	}

	public void setXiafa(Boolean value) {
		_data.put(F_XIAFA, value);
	}

	public String getXiafa() {
		return (String) _data.get(F_XIAFA);
	}

	public void check() throws Exception {
		String jsggs_number = getJsggsNumber();
		String productNumber = getProductNumber();
		if (StringUtils.isNotEmpty(jsggs_number)
				&& StringUtils.isNotEmpty(productNumber)) {
		} else if (StringUtils.isNotEmpty(jsggs_number)
				&& StringUtils.isEmpty(productNumber)) {
			throw new Exception("创建物料相关文档时，传入技术规格书编号不为空，但关联产品编号为空，请检查。");
		} else if (StringUtils.isEmpty(jsggs_number)
				&& StringUtils.isNotEmpty(productNumber)) {
			throw new Exception("创建物料相关文档时，传入技术规格书编号为空，但关联产品编号不为空，请检查。");
		}

		if (StringUtils.isEmpty(getPrimaryLocation())) {
			throw new Exception("创建物料相关文档时，主内容不能为空，请检查。");
		}

		if (StringUtils.isEmpty(getType())) {
			throw new Exception("创建物料相关文档时，必须填写需创建的文档类型，请检查。");
		}

		part = partInfo.getWTPart();
		if (part == null) {
			throw new Exception("在新材PLM系统中未查询到编号为" + getPartNumber()
					+ "的部件，无法执行物料文档创建处理，请检查。");
		}
	}

	public void createDoc() throws Exception {
		if (part == null) {
			part = partInfo.getWTPart();
			if (part == null) {
				throw new Exception("在新材PLM系统中未查询到编号为" + getPartNumber()
						+ "的部件，无法执行物料文档创建处理，请检查。");
			}
		}
		Transaction tx = null;
		String folderPath = part.getFolderPath();
		folderPath = folderPath.substring(0, folderPath.lastIndexOf("/"));
		String documentType = SPMConsts.DEFAULT_DOC_TYPE;// 文档类型
		String number = genDocNumber(part, getType());
		String name = part.getName();
		String fileContent = getPrimaryLocation();// 主文档路径
		boolean flagAccess = SessionServerHelper.manager
				.setAccessEnforced(false);
		try {
			tx = new Transaction();
			tx.start();
			WTContainer container = GenericUtil
					.getWTContainerByName(part.getContainerName());
			if (StringUtils.isEmpty(documentType)) {
				documentType = SPMConsts.DEFAULT_DOC_TYPE;
			}
			TypeDefinitionReference tdr = TypedUtilityServiceHelper.service
					.getTypeDefinitionReference(documentType);
			WTDocument doc = WTDocument.newWTDocument();
			if (container != null) {
				doc.setContainer(container);
			}
			if (StringUtils.isNotEmpty(name)) {
				doc.setName(name);
			}
			// 技术规格书编号参照初始化规则
			if (StringUtils.isNotEmpty(number)) {
				doc.setNumber(number);
			}
			if (tdr != null) {
				doc.setTypeDefinitionReference(tdr);
			}

			// 文档移动到
			Folder folder = FolderUtil.getFolderRef(folderPath, container,
					false);
			String pmfoid = null;
			if (folder != null) {
				pmfoid = (String) LWCUtil.getValue(folder, SPMConsts.PMID);
				Debug.P("----->>>PM FOID:" + pmfoid);
				FolderHelper.assignLocation(doc, folder);

			}
			doc = (WTDocument) PersistenceHelper.manager.save(doc);
			Debug.P("--->>Doc:"
					+ doc.getPersistInfo().getObjectIdentifier()
							.getStringValue());
			Debug.P("---Create Doc Success--->>>Ready Link FileContentURL:"
					+ fileContent);
			// 关联主文档对象
			if (fileContent == null || !fileContent.contains("http://")) {
				throw new WTException(name + " " + number + "的文件地址不合法,请检查");
			}

			String fileName = fileContent.substring(fileContent
					.lastIndexOf("/") + 1);
			InputStream pins = CsrSpmUtil.saveUrlAsLocation(fileContent);
			// 添加主文档内容URL链接
			doc = DocUtils.linkDocument(doc, fileName, pins, "1", null);
			Debug.P("----->>>>Link Content URL Success!  =====>DocFolderpath:"
					+ doc.getFolderPath());

			// 向PM系统创建文档对象
			CsrSpmUtil.createDoc2PM(doc);
			tx.commit();
			tx = null;

			if (doc != null) {// 建立文档部件的描述关系
				PartUtil.createDescriptionLink(part, doc);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new WTException(e.getMessage());
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flagAccess);
			if (tx != null)
				tx.rollback();
		}

	}

	/**
	 * 生成物料类文档编号
	 * 
	 * @param part
	 * @param type
	 * @return
	 */
	public static String genDocNumber(WTPart part, String type) {
		String docNumber = "";

		if (StringUtils.isEmpty(type) || part == null) {
			return docNumber;
		}
		String partNumber = part.getNumber();
		if (StringUtils.equals("wgj_cpsc", type)) {
			docNumber = "C-" + partNumber;
		} else if (StringUtils.equals("wgj_jsrzbg", type)) {
			docNumber = "R-" + partNumber;
		} else if (StringUtils.equals("wgj_qt", type)) {
			docNumber = "Q-" + partNumber;
		} else {
			docNumber = "N-" + partNumber;
		}
		return docNumber;
	}
}
