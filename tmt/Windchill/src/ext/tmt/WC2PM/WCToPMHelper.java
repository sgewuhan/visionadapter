package ext.tmt.WC2PM;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import wt.enterprise.Master;
import wt.epm.EPMDocument;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.Foldered;
import wt.iba.value.IBAHolder;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.util.WTProperties;

import com.mongodb.WriteResult;
import com.sg.visionadapter.BasicDocument;
import com.sg.visionadapter.BasicPart;
import com.sg.visionadapter.CADDocumentPersistence;
import com.sg.visionadapter.DocumentPersistence;
import com.sg.visionadapter.FolderPersistence;
import com.sg.visionadapter.IPersistenceService;
import com.sg.visionadapter.JigToolsPersistence;
import com.sg.visionadapter.MaterialPersistence;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMCADDocument;
import com.sg.visionadapter.PMFolder;
import com.sg.visionadapter.PMJigTools;
import com.sg.visionadapter.PMMaterial;
import com.sg.visionadapter.PMPackage;
import com.sg.visionadapter.PMPart;
import com.sg.visionadapter.PMProduct;
import com.sg.visionadapter.PMSupplyment;
import com.sg.visionadapter.PackagePersistence;
import com.sg.visionadapter.PartPersistence;
import com.sg.visionadapter.ProductPersistence;
import com.sg.visionadapter.SupplymentPersistence;
import com.sg.visionadapter.VisionObject;

import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.Utils;

/**
 * WC同步EPMDocument,wtpart到PM系统
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("all")
public class WCToPMHelper implements RemoteAccess, Serializable {
	private static final String CREATE = "create";
	private static final String UPDATE = "update";
	private static String codebasePath = null;
	private static ModelServiceFactory INSTANCE;

	static {
		try {
			WTProperties wtproperties = WTProperties.getLocalProperties();
			codebasePath = wtproperties.getProperty("wt.codebase.location");
			codebasePath = codebasePath + File.separator + "visionconf";
			Debug.P("----------->>>Codebase:" + codebasePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void CreatePMPart(WTPart wtPart) {
		Debug.P("将Windchill中的半成品插入PM系统的数据库中");
		try {
			PartPersistence partPersistence = getPersistence(PartPersistence.class);
			PMPart pmPart = partPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMPart(pmPart, wtPart, partiba, plmFolderId);
				syncPM(pmPart, wtPart, partiba, CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreatePMProduct(WTPart wtPart) {
		Debug.P("将Windchill中的成品插入PM系统的数据库中");
		try {
			ProductPersistence productPersistence = getPersistence(ProductPersistence.class);
			PMProduct pmProduct = productPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMProduct(pmProduct, wtPart, partiba, plmFolderId);
				syncPM(pmProduct, wtPart, partiba, CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreatePMMaterial(WTPart wtPart) {
		Debug.P("将Windchill中的原材料插入PM系统的数据库中");
		try {
			MaterialPersistence materialPersistence = getPersistence(MaterialPersistence.class);
			PMMaterial pmMaterial = materialPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMMaterial(pmMaterial, wtPart, partiba, plmFolderId);
				syncPM(pmMaterial, wtPart, partiba, CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreatePMSupply(WTPart wtPart) {
		Debug.P("将Windchill中的客供件插入PM系统的数据库中");

		try {
			SupplymentPersistence supplymentPersistence = getPersistence(SupplymentPersistence.class);
			PMSupplyment pmSupplyment = supplymentPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMSupplyment(pmSupplyment, wtPart, partiba, plmFolderId);
				syncPM(pmSupplyment, wtPart, partiba, CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreatePMPackage(WTPart wtPart) {
		Debug.P("将Windchill中的包装材料插入PM系统的数据库中");
		try {
			PackagePersistence packagePersistence = getPersistence(PackagePersistence.class);
			PMPackage pmPackage = packagePersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMPackage(pmPackage, wtPart, partiba, plmFolderId);
				syncPM(pmPackage, wtPart, partiba, CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreatePMJigToolPart(WTPart wtPart) {
		Debug.P("将Windchill中的备品备料插入PM系统的数据库中");
		try {
			JigToolsPersistence jigTollsPersistence = getPersistence(JigToolsPersistence.class);
			PMJigTools pmJigTools = jigTollsPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMJigTools(pmJigTools, wtPart, partiba, plmFolderId);
				syncPM(pmJigTools, wtPart, partiba, CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreateEPMDoc(EPMDocument epmdoc) {
		Debug.P("将Windchill中的EPMDocument插入PM系统的数据库中");
		try {
			CADDocumentPersistence cadDocPersistence = getPersistence(CADDocumentPersistence.class);
			PMCADDocument pmcad = cadDocPersistence.newInstance();
			IBAUtils epmiba = new IBAUtils(epmdoc);
			String plmFolderId = getPLMFolderId(epmdoc);

			if (checkFolder(plmFolderId)) {
				initEPMDocument(pmcad, epmdoc, epmiba, plmFolderId);
				syncPM(pmcad, epmdoc, epmiba, CREATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateEPMDoc(String pmoid, EPMDocument epmdoc) {
		Debug.P("更新Windchill中的EPMDocument-----------");
		try {
			CADDocumentPersistence cadDocPersistence = getPersistence(CADDocumentPersistence.class);
			PMCADDocument pmcad = cadDocPersistence.newInstance();
			IBAUtils epmiba = new IBAUtils(epmdoc);
			String plmFolderId = getPLMFolderId(epmdoc);

			if (checkFolder(plmFolderId)) {
				initEPMDocument(pmcad, epmdoc, epmiba, plmFolderId);
				syncPM(pmcad, epmdoc, epmiba, UPDATE);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMPart(String pmoid, WTPart wtPart) {
		Debug.P("更新Windchill中的半成品后至PM系统的数据库中");
		try {
			PartPersistence partPersistence = getPersistence(PartPersistence.class);
			PMPart pmPart = partPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMPart(pmPart, wtPart, partiba, plmFolderId);
				syncPM(pmPart, wtPart, partiba, UPDATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMProduct(String pmoid, WTPart wtPart) {
		Debug.P("更新PM系统的成品-----------》" + wtPart.getNumber());
		try {
			ProductPersistence productPersistence = getPersistence(ProductPersistence.class);
			PMProduct pmProduct = productPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMProduct(pmProduct, wtPart, partiba, plmFolderId);
				syncPM(pmProduct, wtPart, partiba, UPDATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMMaterial(String pmoid, WTPart wtPart) {
		Debug.P("更新Windchill中的原材料---------------" + wtPart.getNumber());

		try {
			MaterialPersistence materialPersistence = getPersistence(MaterialPersistence.class);
			PMMaterial pmMaterial = materialPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMMaterial(pmMaterial, wtPart, partiba, plmFolderId);
				syncPM(pmMaterial, wtPart, partiba, UPDATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMPackage(String pmoid, WTPart wtPart) {
		Debug.P("更新Windchill中的包装材料---------------" + wtPart.getNumber());
		try {
			PackagePersistence packagePersistence = getPersistence(PackagePersistence.class);
			PMPackage pmPackage = packagePersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMPackage(pmPackage, wtPart, partiba, plmFolderId);
				syncPM(pmPackage, wtPart, partiba, UPDATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void UpdatePMJigToolPart(String pmoid, WTPart wtPart) {
		Debug.P("将Windchill中的备品备料插入PM系统的数据库中");
		try {
			JigToolsPersistence jigTollsPersistence = getPersistence(JigToolsPersistence.class);
			PMJigTools pmJigTools = jigTollsPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMJigTools(pmJigTools, wtPart, partiba, plmFolderId);
				syncPM(pmJigTools, wtPart, partiba, UPDATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMSupply(String pmoid, WTPart wtPart) {
		Debug.P("将Windchill中的客供件更新PM系统的数据库中---------------》"
				+ wtPart.getNumber());
		try {
			SupplymentPersistence supplymentPersistence = getPersistence(SupplymentPersistence.class);
			PMSupplyment pmSupplyment = supplymentPersistence.newInstance();
			IBAUtils partiba = new IBAUtils(wtPart);
			String plmFolderId = getPLMFolderId(wtPart);

			if (checkFolder(plmFolderId)) {
				initPMSupplyment(pmSupplyment, wtPart, partiba, plmFolderId);
				syncPM(pmSupplyment, wtPart, partiba, UPDATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getObjectOid(WTObject object) {
		return object.getPersistInfo().getObjectIdentifier().getStringValue();
	}

	public static String getObjectMasterOid(WTObject object) {
		String oid = "";
		Master master = null;
		if ((object instanceof WTPart)) {
			WTPart part = (WTPart) object;
			master = (Master) part.getMaster();
			oid = master.getPersistInfo().getObjectIdentifier()
					.getStringValue();
		} else if ((object instanceof EPMDocument)) {
			EPMDocument epmdoc = (EPMDocument) object;
			master = (Master) epmdoc.getMaster();
			oid = master.getPersistInfo().getObjectIdentifier()
					.getStringValue();
		}
		Debug.P(oid);
		return oid;
	}

	private static ModelServiceFactory getModelServiceFactory() {
		if (INSTANCE == null) {
			INSTANCE = ModelServiceFactory.getInstance(codebasePath);
		}
		return INSTANCE;
	}

	private static <T extends IPersistenceService> T getPersistence(Class<T> t)
			throws Exception {
		return getModelServiceFactory().get(t);
	}

	private static String getPLMFolderId(Foldered foldered) throws Exception {
		String partFolderString = foldered.getFolderPath();
		Folder partFolder = FolderHelper.service.getFolder(foldered);
		Debug.P("Part Folder ----------->" + partFolder);
		String plmFolderId = partFolder.getPersistInfo().getObjectIdentifier()
				.getStringValue();
		Debug.P("Part Folder Id ----------->" + plmFolderId);
		return plmFolderId;
	}

	private static boolean checkFolder(String plmFolderId) throws Exception {
		try {
			PMFolder pmfolder = (getPersistence(FolderPersistence.class))
					.getByPLMId(plmFolderId);
			if (pmfolder == null) {
				return false;
			}
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	private static boolean checkDoc(String masterid) throws Exception {
		DocumentPersistence basicDocumentPersistence = getPersistence(DocumentPersistence.class);
		return basicDocumentPersistence.hasPMObjectByPLMMasterId(masterid);
	}

	private static void initVisionObject(VisionObject vo, String docOid,
			Map plmData, String commonName, String creatorName,
			String creatorFullName) {
		vo.setPLMId(docOid);
		vo.setPLMData(plmData);
		vo.setCommonName(commonName);
		vo.setCreateBy(creatorName, creatorFullName);
		vo.setOwner(creatorName);
		vo.setValue("IsSync", true);
		vo.initPlmData();
	}

	private static void initBasicDocument(BasicDocument basicDoc,
			String plmFolderId, String number, String status, String majorVid,
			int secondVid, String phase, String masterid) throws Exception {
		if (!checkDoc(masterid)) {
			basicDoc.setFolderIdByPLMId(plmFolderId);
		}
		basicDoc.setObjectNumber(number);
		basicDoc.setStatus(status);
		basicDoc.setMajorVid(majorVid);
		basicDoc.setSecondVid(secondVid);
		if (StringUtils.isNotEmpty(phase)) {
			basicDoc.setPhase(phase);
		}
		basicDoc.SetMasterId(masterid);
	}

	private static void initWTPart(BasicDocument basicDoc, WTPart wtPart,
			IBAUtils ibaUtils, String plmFolderId) throws Exception {
		Debug.P("initPart------> Start");
		String partOid = getObjectOid(wtPart);
		String number = wtPart.getNumber();
		String commonName = wtPart.getName();
		String creatorName = wtPart.getCreatorName();
		String creatorFullName = wtPart.getCreatorFullName();
		String status = wtPart.getState().toString().toLowerCase();
		String majorVid = wtPart.getVersionIdentifier().getValue();
		int secondVid = Integer.parseInt(wtPart.getIterationIdentifier()
				.getValue());
		String masterid = getObjectMasterOid(wtPart);

		String phase = ibaUtils.getIBAValue("PHASE");
		String partType = ibaUtils.getIBAValue("Part_Type");
		String airSpringClassification = ibaUtils
				.getIBAValue("AirSpringClassification");
		long branchId = wtPart.getIterationInfo().getBranchId();

		Map plmData = getPLMData(number, "wt.part.WTPart:" + branchId,
				airSpringClassification, partType);

		initVisionObject(basicDoc, partOid, plmData, commonName, creatorName,
				creatorFullName);
		initBasicDocument(basicDoc, plmFolderId, number, status, majorVid,
				secondVid, phase, masterid);
		Debug.P("initPart------> Finish");

	}

	private static void initEPMDocument(PMCADDocument pmcad,
			EPMDocument epmdoc, IBAUtils ibaUtils, String plmFolderId)
			throws Exception {
		String docOid = getObjectOid(epmdoc);
		String number = epmdoc.getNumber();
		String commonName = epmdoc.getName();
		String creatorName = epmdoc.getCreatorName();
		String creatorFullName = epmdoc.getCreatorFullName();
		String status = epmdoc.getState().toString().toLowerCase();
		String majorVid = epmdoc.getVersionIdentifier().getValue();
		int secondVid = Integer.parseInt(epmdoc.getIterationIdentifier()
				.getValue());
		String masterid = getObjectMasterOid(epmdoc);

		String phase = ibaUtils.getIBAValue("PHASE");
		String materialNo = ibaUtils.getIBAValue("Material_NO");
		String partType = ibaUtils.getIBAValue("Part_Type");
		String airSpringClassification = ibaUtils
				.getIBAValue("AirSpringClassification");
		long branchId = epmdoc.getIterationInfo().getBranchId();

		Map plmData = getPLMData(number, "wt.epm.EPMDocument:" + branchId,
				airSpringClassification, partType);

		initVisionObject(pmcad, docOid, plmData, commonName, creatorName,
				creatorFullName);
		initBasicDocument(pmcad, plmFolderId, number, status, majorVid,
				secondVid, phase, masterid);

		pmcad.setValue("cadName", epmdoc.getCADName());
		String contentMD5 = GenericUtil.getMd5ByFile(epmdoc);
		pmcad.setContentMD5(contentMD5);
		if (StringUtils.isNotEmpty(materialNo)) {
			pmcad.setDrawingNumber(materialNo);
		}
		if (StringUtils.isNotEmpty(partType)) {
			partType = partType.replaceAll(" ", "").trim();
			pmcad.setPartType0(partType);
		}

		pmcad.initPlmData();
	}

	private static Map getPLMData(String number, String branchId,
			String airSpringClassification, String partType) {
		Map plmData = new HashMap();
		plmData.put("number", number);
		plmData.put("plmmid", branchId);
		if (StringUtils.isNotEmpty(airSpringClassification)) {
			plmData.put("AirSpringClassification", airSpringClassification);
		}
		if (StringUtils.isNotEmpty(partType)) {
			plmData.put("Part_Type", partType);
		}
		return plmData;
	}

	private static void initBasicPart(BasicPart basicPart, WTPart wtPart,
			IBAUtils ibaUtils, String plmFolderId) throws Exception {
		Debug.P("initBasicPart------> Start");
		initWTPart(basicPart, wtPart, ibaUtils, plmFolderId);
		String specifications = ibaUtils.getIBAValue("Specifications");
		if (StringUtils.isNotEmpty(specifications)) {
			basicPart.setSpec(specifications);
		}
		String weight = ibaUtils.getIBAValue("Weight");
		if (StringUtils.isNotEmpty(weight)) {
			basicPart.setWeight(NumberFormat.getInstance().parse(weight));
		}
		String material = ibaUtils.getIBAValue("Material");
		if (StringUtils.isNotEmpty(material)) {
			basicPart.setMaterial(material);
		}
		Debug.P("initBasicPart------> Finish");
	}

	private static void initPMPart(PMPart pmPart, WTPart wtPart,
			IBAUtils ibaUtils, String plmFolderId) throws Exception {
		Debug.P("initPMPart------> Start");
		initBasicPart(pmPart, wtPart, ibaUtils, plmFolderId);
		String productNo = ibaUtils.getIBAValue("Product_NO");
		if (StringUtils.isNotEmpty(productNo)) {
			pmPart.setProductNumber(productNo);
		}
		Debug.P("initPMPart------> Finish");
	}

	private static void initPMProduct(PMProduct pmProduct, WTPart wtPart,
			IBAUtils ibaUtils, String plmFolderId) throws Exception {
		Debug.P("initPMProduct------> Start");
		initBasicPart(pmProduct, wtPart, ibaUtils, plmFolderId);
		pmProduct.setProductNumber(wtPart.getNumber());
		String formulaNo = ibaUtils.getIBAValue("Formula_NO");
		if (StringUtils.isNotEmpty(formulaNo)) {
			pmProduct.setFormularNumber(formulaNo);
		}
		Debug.P("initPMProduct------> Finish");
	}

	private static void initPMMaterial(PMMaterial pmMaterial, WTPart wtPart,
			IBAUtils ibaUtils, String plmFolderId) throws Exception {
		Debug.P("initPMMaterial------> Start");
		initBasicPart(pmMaterial, wtPart, ibaUtils, plmFolderId);
		setMaterialGroup(pmMaterial, ibaUtils);
		Debug.P("initPMMaterial------> Finish");
	}

	private static void initPMSupplyment(PMSupplyment pmSupplyment,
			WTPart wtPart, IBAUtils ibaUtils, String plmFolderId)
			throws Exception {
		Debug.P("initPMSupplyment------> Start");
		initBasicPart(pmSupplyment, wtPart, ibaUtils, plmFolderId);
		String clientName = ibaUtils.getIBAValue("Client_Name");
		if (StringUtils.isNotEmpty(clientName)) {
			pmSupplyment.setCustomerName(clientName);
		}
		setMaterialGroup(pmSupplyment, ibaUtils);
		Debug.P("initPMSupplyment------> Finish");
	}

	private static void initPMPackage(PMPackage pmPackage, WTPart wtPart,
			IBAUtils ibaUtils, String plmFolderId) throws Exception {
		Debug.P("initPMPackage------> Start");
		initBasicPart(pmPackage, wtPart, ibaUtils, plmFolderId);
		setMaterialGroup(pmPackage, ibaUtils);
		Debug.P("initPMPackage------> Finish");
	}

	private static void initPMJigTools(PMJigTools pmJigTools, WTPart wtPart,
			IBAUtils ibaUtils, String plmFolderId) throws Exception {
		Debug.P("initPMJigTools------> Start");
		initBasicPart(pmJigTools, wtPart, ibaUtils, plmFolderId);
		setMaterialGroup(pmJigTools, ibaUtils);
		Debug.P("initPMJigTools------> Finish");
	}

	private static void setMaterialGroup(BasicPart bp, IBAUtils ibaUtils) {
		String materialGroup = ibaUtils.getIBAValue("Material_Group");
		if (StringUtils.isNotEmpty(materialGroup)) {
			if (bp instanceof PMJigTools) {
				((PMJigTools) bp).setMaterialGroup(materialGroup);
			} else if (bp instanceof PMPackage) {
				((PMPackage) bp).setMaterialGroup(materialGroup);
			} else if (bp instanceof PMSupplyment) {
				((PMSupplyment) bp).setMaterialGroup(materialGroup);
			} else if (bp instanceof PMMaterial) {
				((PMMaterial) bp).setMaterialGroup(materialGroup);
			}
		}
	}

	private static void syncPM(VisionObject vo, IBAHolder ibaHolder,
			IBAUtils ibaUtils, String typle) throws Exception {
		Debug.P("syncPM------> Start");
		String docInfo = vo.serialize();
		String pmoid = InsterOrUpdatePMDoc(docInfo);
		if (!StringUtils.isEmpty(pmoid)) {
			ibaUtils.setIBAValue("PMId", pmoid);
			String data = Utils.getDate();
			ibaUtils.setIBAValue("CyncData", data);
			ibaUtils.setIBAValue("PMRequest", typle);
			ibaUtils.updateIBAPart(ibaHolder);
			reloadPermission(pmoid.toString());
			reloadDeliverable(pmoid.toString());

			if (vo instanceof PMProduct) {
				productNumToProductItem(pmoid.toString());
			}
			Debug.P("syncPM------> Finish success");
		} else {
			Debug.P("syncPM------> Finish error");
		}
	}

	/**
	 * 调用PM的sevlet进行插入和更新PM对象
	 * 
	 * @param docInfo
	 * @return
	 * @throws Exception
	 */
	public static String InsterOrUpdatePMDoc(String docInfo) throws Exception {
		String urls = ModelServiceFactory.URL_DOCUMENTSERVICE;
		Debug.P(urls);
		URL url = new URL(urls);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("POST");
		OutputStream os = connection.getOutputStream();
		docInfo = URLEncoder.encode(docInfo, "UTF-8");
		String param = "doc=" + docInfo;
		Debug.P(docInfo);
		os.write(param.getBytes());
		os.flush();
		os.close();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String result;
		System.out.println("InsterOrUpdatePMDoc----------start--- ");
		result = reader.readLine();
		Debug.P("result----->" + result);

		System.out.println(" InsterOrUpdatePMDoc--------ends ");
		reader.close();
		connection.disconnect();
		return result;
	}

	public static void reloadPermission(String objectId) throws Exception {
		String urls = ModelServiceFactory.URL_REAUTH + "?id=" + objectId;
		Debug.P(urls);
		URL url = new URL(urls);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		System.out.println("Reload Permission---------start---- ");
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		System.out.println(" Reload Permission--------ends ");
		reader.close();
		connection.disconnect();
	}

	public static void reloadDeliverable(String objectId) throws Exception {
		String urls = ModelServiceFactory.URL_LINKDELIVERY + "?id=" + objectId;
		Debug.P(urls);
		URL url = new URL(urls);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		System.out.println("Reload Deliverable----------start--- ");
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		System.out.println(" Reload Deliverable--------ends ");
		reader.close();
		connection.disconnect();
	}

	public static void productNumToProductItem(String objectId)
			throws Exception {
		String urls = ModelServiceFactory.URL_PRODUCTNUM + "?id=" + objectId;
		Debug.P(urls);
		URL url = new URL(urls);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String line;
		Debug.P("Product Number To ProductItem----------start--- ");
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		Debug.P("Product Number To ProductItem--------ends ");
		reader.close();
		connection.disconnect();
	}

	public static void main(String[] args) throws Exception {
		Debug.P("------>>>>Start Sysnch>>>>>>>>");
		syncMasterIdToDocument(Utils.getDate());
		// SynchEPMDocument2PM(Utils.getDate());// 同步图纸
		// SynchWTPart2PM(Utils.getDate());// 同步部件
		//
		// initPMDocument(Utils.getDate());
		// SynchDocument2PM(Utils.getDate());// 同步文档
		// PMDeliverable pmd = new PMDeliverable();
		// pmd.initDeli(codebasePath);
		Debug.P("------>>>>End Sysnch>>>>>>>>");
	}

	public static void syncMasterIdToDocument(String startTime)
			throws Exception {
		Debug.P("---------->>>>syncMasterIdToDocument : startTime=" + startTime);
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class aclass[] = { String.class };
				Object aobj[] = { startTime };
				RemoteMethodServer.getDefault().invoke(
						"syncMasterIdToDocument", WCToPMHelper.class.getName(),
						null, aclass, aobj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			PartPersistence partPersistence = getPersistence(PartPersistence.class);
			ProductPersistence productPersistence = getPersistence(ProductPersistence.class);
			MaterialPersistence materialPersistence = getPersistence(MaterialPersistence.class);
			SupplymentPersistence supplymentPersistence = getPersistence(SupplymentPersistence.class);
			PackagePersistence packagePersistence = getPersistence(PackagePersistence.class);
			JigToolsPersistence jigTollsPersistence = getPersistence(JigToolsPersistence.class);
			CADDocumentPersistence cadDocPersistence = getPersistence(CADDocumentPersistence.class);

			List<EPMDocument> epmList = new ArrayList<EPMDocument>();
			epmList = EPMDocUtil.getAllEPMDocument();
			for (EPMDocument epmdoc : epmList) {
				Debug.P("syncMasterIdToDocument sync EPMDocument finish >>>>>>>>"
						+ epmdoc.getNumber());
				try {
					PMCADDocument pmcad = cadDocPersistence.newInstance();
					IBAUtils epmiba = new IBAUtils(epmdoc);
					String plmFolderId = getPLMFolderId(epmdoc);
					String pmoid = epmiba.getIBAValue("PMId");
					WriteResult wresult;

					initEPMDocument(pmcad, epmdoc, epmiba, plmFolderId);
					if (StringUtils.isNotEmpty(pmoid)) {
						ObjectId objectId = new ObjectId();
						pmoid = objectId.toString();
						pmcad.set_id(objectId);
						wresult = pmcad.doInsert();
					} else {
						pmcad.set_id(new ObjectId(pmoid));
						wresult = pmcad.doUpdate();
					}
					String error = wresult.getError();
					if (StringUtils.isEmpty(error)) {
						epmiba.setIBAValue("PMId", pmoid);
						epmiba.setIBAValue("CyncData", Utils.getDate());
						epmiba.setIBAValue("PMRequest", "create");
						epmiba.updateIBAPart(epmdoc);
						Debug.P("syncMasterIdToDocument sync EPMDocument end >>>>>>>>"
								+ epmdoc.getNumber());
					}
				} catch (Exception e) {
					Debug.P("syncMasterIdToDocument sync EPMDocument error >>>>>>>>"
							+ epmdoc.getNumber());
				}
			}
			List<WTPart> partList = new ArrayList<WTPart>();
			String partType = "";
			partList = EPMDocUtil.getAllWTPart();
			for (WTPart wtPart : partList) {
				Debug.P("syncMasterIdToDocument sync WTPart finish >>>>>>>>"
						+ wtPart.getNumber());
				try {
					IBAUtils partiba = new IBAUtils(wtPart);
					String plmFolderId = getPLMFolderId(wtPart);
					partType = DocUtils.getType(wtPart);
					String pmoid = partiba.getIBAValue("PMId");
					WriteResult wresult = null;

					if (partType.contains(Contants.SEMIFINISHEDPRODUCT)) {// 如果半是成品
						PMPart pmPart = partPersistence.newInstance();

						initPMPart(pmPart, wtPart, partiba, plmFolderId);
						if (StringUtils.isNotEmpty(pmoid)) {
							ObjectId objectId = new ObjectId();
							pmoid = objectId.toString();
							pmPart.set_id(objectId);
							wresult = pmPart.doInsert();
						} else {
							pmPart.set_id(new ObjectId(pmoid));
							wresult = pmPart.doUpdate();
						}

					} else if (partType.contains(Contants.PRODUCTPART)) { // 如果是成品
						PMProduct pmProduct = productPersistence.newInstance();

						initPMProduct(pmProduct, wtPart, partiba, plmFolderId);
						if (StringUtils.isNotEmpty(pmoid)) {
							ObjectId objectId = new ObjectId();
							pmoid = objectId.toString();
							pmProduct.set_id(objectId);
							wresult = pmProduct.doInsert();
						} else {
							pmProduct.set_id(new ObjectId(pmoid));
							wresult = pmProduct.doUpdate();
						}

					} else if (partType.contains(Contants.MATERIAL)) { // 如果是原材料
						PMMaterial pmMaterial = materialPersistence
								.newInstance();

						initPMMaterial(pmMaterial, wtPart, partiba, plmFolderId);
						if (StringUtils.isNotEmpty(pmoid)) {
							ObjectId objectId = new ObjectId();
							pmoid = objectId.toString();
							pmMaterial.set_id(objectId);
							wresult = pmMaterial.doInsert();
						} else {
							pmMaterial.set_id(new ObjectId(pmoid));
							wresult = pmMaterial.doUpdate();
						}

					} else if (partType.contains(Contants.SUPPLYMENT)) {// 如果是客供件
						PMSupplyment pmSupplyment = supplymentPersistence
								.newInstance();

						initPMSupplyment(pmSupplyment, wtPart, partiba,
								plmFolderId);
						if (StringUtils.isNotEmpty(pmoid)) {
							ObjectId objectId = new ObjectId();
							pmoid = objectId.toString();
							pmSupplyment.set_id(objectId);
							wresult = pmSupplyment.doInsert();
						} else {
							pmSupplyment.set_id(new ObjectId(pmoid));
							wresult = pmSupplyment.doUpdate();
						}

					} else if (partType.contains(Contants.PACKINGPART)) {// 如果是包装材料
						PMPackage pmPackage = packagePersistence.newInstance();

						initPMPackage(pmPackage, wtPart, partiba, plmFolderId);
						if (StringUtils.isNotEmpty(pmoid)) {
							ObjectId objectId = new ObjectId();
							pmoid = objectId.toString();
							pmPackage.set_id(objectId);
							wresult = pmPackage.doInsert();
						} else {
							pmPackage.set_id(new ObjectId(pmoid));
							wresult = pmPackage.doUpdate();
						}

					} else if (partType.contains(Contants.TOOLPART)) {// 如果是备品备料
						PMJigTools pmJigTools = jigTollsPersistence
								.newInstance();

						initPMJigTools(pmJigTools, wtPart, partiba, plmFolderId);
						if (StringUtils.isNotEmpty(pmoid)) {
							ObjectId objectId = new ObjectId();
							pmoid = objectId.toString();
							pmJigTools.set_id(objectId);
							wresult = pmJigTools.doInsert();
						} else {
							pmJigTools.set_id(new ObjectId(pmoid));
							wresult = pmJigTools.doUpdate();
						}

					}
					if (wresult != null) {
						String error = wresult.getError();
						if (StringUtils.isEmpty(error)) {
							partiba.setIBAValue("PMId", pmoid);
							partiba.setIBAValue("CyncData", Utils.getDate());
							partiba.setIBAValue("PMRequest", "create");
							partiba.updateIBAPart(wtPart);
							Debug.P("syncMasterIdToDocument sync WTPart end >>>>>>>>"
									+ wtPart.getNumber());
						}
					}
				} catch (Exception e) {
					Debug.P("syncMasterIdToDocument sync WTPart error >>>>>>>>"
							+ wtPart.getNumber());
				}
			}
		}
	}
	
	// /**
	// * 同步WC中的EPMDocument对象，如果已同步则更新
	// *
	// * @throws Exception
	// */
	// public static void SynchEPMDocument2PM(String startTime) throws Exception
	// {
	// Debug.P("---------->>>>SynchEPMDocument2PM:startTime=" + startTime);
	// if (!RemoteMethodServer.ServerFlag) {
	// try {
	// Class aclass[] = { String.class };
	// Object aobj[] = { startTime };
	// RemoteMethodServer.getDefault().invoke("SynchEPMDocument2PM",
	// WCToPMHelper.class.getName(), null, aclass, aobj);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } else {
	// List<EPMDocument> epmList = new ArrayList<EPMDocument>();
	// epmList = EPMDocUtil.getAllEPMDocument();
	// for (EPMDocument epmdoc : epmList) {
	// CreateEPMDoc(epmdoc);
	// }
	// Debug.P("-----------------同步EPMDocument完毕！--------------");
	// }
	// }
	//
	// /**
	// * 同步WC中的WTPart对象，如果已同步则更新
	// *
	// * @throws Exception
	// */
	// public static void SynchWTPart2PM(String startTime) throws Exception {
	// Debug.P("---------->>>>SynchWTPart2PM:startTime=" + startTime);
	// if (!RemoteMethodServer.ServerFlag) {
	// try {
	// Class aclass[] = { String.class };
	// Object aobj[] = { startTime };
	// RemoteMethodServer.getDefault().invoke("SynchWTPart2PM",
	// WCToPMHelper.class.getName(), null, aclass, aobj);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } else {
	// List<WTPart> partList = new ArrayList<WTPart>();
	// String partType = "";
	// partList = EPMDocUtil.getAllWTPart();
	// int i = 0;
	// for (WTPart wtPart : partList) {
	// i++;
	// partType = DocUtils.getType(wtPart);
	// Debug.P(wtPart.getNumber() + "--------->" + partType
	// + "----------->" + i);
	// if (partType.contains(Contants.SEMIFINISHEDPRODUCT)) {// 如果半是成品
	// WCToPMHelper.CreatePMPart(wtPart);
	// } else if (partType.contains(Contants.PRODUCTPART)) { // 如果是成品
	// WCToPMHelper.CreatePMProduct(wtPart);
	// } else if (partType.contains(Contants.MATERIAL)) { // 如果是原材料
	// WCToPMHelper.CreatePMMaterial(wtPart);
	// } else if (partType.contains(Contants.SUPPLYMENT)) {// 如果是客供件
	// WCToPMHelper.CreatePMSupply(wtPart);
	// } else if (partType.contains(Contants.PACKINGPART)) {// 如果是包装材料
	// WCToPMHelper.CreatePMPackage(wtPart);
	// } else if (partType.contains(Contants.TOOLPART)) {// 如果是备品备料
	// WCToPMHelper.CreatePMJigToolPart(wtPart);
	// }
	// }
	// }
	// Debug.P("-----------------同步WTPart完毕！--------------");
	// }

	// public static void initPMDocument(String startTime) throws Exception {
	// Debug.P("---------->>>>initPMDocument:startTime=" + startTime);
	// if (!RemoteMethodServer.ServerFlag) {
	// try {
	// Class aclass[] = { String.class };
	// Object aobj[] = { startTime };
	// RemoteMethodServer.getDefault().invoke("initPMDocument",
	// WCToPMHelper.class.getName(), null, aclass, aobj);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } else {
	//
	// String lastOid = "";
	// WTPart part = null;
	// WTDocument document = null;
	// EPMDocument epmdoc = null;
	// PMDocument pmdoc = null;
	// PMCADDocument pmcad = null;
	// PMJigTools pmJigTools = null;
	// PMPart pmPart = null;
	// PMMaterial pmMaterial = null;
	// PMPackage pmPackage = null;
	// PMProduct pmProduct = null;
	// BasicDocument bd = null;
	// BasicPart bp = null;
	// PMSupplyment pmSupplyment = null;
	//
	// CADDocumentPersistence cadDoc =
	// getPersistence(CADDocumentPersistence.class);
	// DocumentPersistence docper = getPersistence(DocumentPersistence.class);
	// JigToolsPersistence jigTolls = getPersistence(JigToolsPersistence.class);
	// PartPersistence pmparts = getPersistence(PartPersistence.class);
	// MaterialPersistence material = getPersistence(MaterialPersistence.class);
	// PackagePersistence packages = getPersistence(PackagePersistence.class);
	// ProductPersistence pmproduct = getPersistence(ProductPersistence.class);
	// SupplymentPersistence supplyment =
	// getPersistence(SupplymentPersistence.class);
	//
	// // 获取PM中所有的document对象包含EPMdoc,Document,Part
	// Map<String, String> docMap = cadDoc.getInitDocumentList();
	// Debug.P("docMap.size--------------------->" + docMap.size());
	// String oid = "";
	// String pmType = "";
	// Iterator<?> iter = docMap.keySet().iterator();
	// int i = 0;
	// while (iter.hasNext()) {
	// i++;
	// oid = iter.next().toString();
	// pmType = docMap.get(oid);
	// Debug.P("oid----->" + oid + "----pmType---->" + pmType
	// + "------->" + i);
	// if (pmType.equals("epmdocument")) {
	// try {
	// epmdoc = (EPMDocument) GenericUtil.REF_FACTORY
	// .getReference(oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bd = cadDoc.getByPLMId(oid);
	// if (epmdoc == null) {
	// bd.setValue("IsSync", false);
	// bd.doUpdate();
	// } else {
	// epmdoc = (EPMDocument) Utils.getWCObject(
	// EPMDocument.class, epmdoc.getNumber());
	// lastOid = getObjectOid(epmdoc);
	// if (!oid.equals(lastOid)) {
	// bd.setValue("IsSync", lastOid);
	// bd.doUpdate();
	// }
	// }
	// } else if (pmType.equals("wtdocument")) {
	// try {
	// document = (WTDocument) GenericUtil.REF_FACTORY
	// .getReference(oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bd = docper.getByPLMId(oid);
	// if (document == null) {
	// bd.setValue("IsSync", false);
	// bd.doUpdate();
	// } else {
	// document = (WTDocument) Utils.getWCObject(
	// WTDocument.class, document.getNumber());
	// lastOid = getObjectOid(document);
	// if (!oid.equals(lastOid)) {
	// bd.setValue("IsSync", lastOid);
	// bd.doUpdate();
	// }
	// }
	// } else if (pmType.equals("pmjigtools")) {
	// try {
	// part = (WTPart) GenericUtil.REF_FACTORY.getReference(
	// oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bp = (BasicPart) jigTolls.getByPLMId(oid);
	// if (part == null) {
	// bp.setValue("IsSync", false);
	// bp.doUpdate();
	// } else {
	// part = (WTPart) Utils.getWCObject(WTPart.class,
	// part.getNumber());
	// lastOid = getObjectOid(part);
	// if (!oid.equals(lastOid)) {
	// bp.setValue("IsSync", lastOid);
	// bp.doUpdate();
	// }
	// }
	//
	// } else if (pmType.equals("pmmaterial")) {
	// try {
	// part = (WTPart) GenericUtil.REF_FACTORY.getReference(
	// oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bp = material.getByPLMId(oid);
	// if (part == null) {
	// bp.setValue("IsSync", false);
	// bp.doUpdate();
	// } else {
	// part = (WTPart) Utils.getWCObject(WTPart.class,
	// part.getNumber());
	// lastOid = getObjectOid(part);
	// if (!oid.equals(lastOid)) {
	// bp.setValue("IsSync", lastOid);
	// bp.doUpdate();
	// }
	// }
	// } else if (pmType.equals("pmpackage")) {
	// try {
	// part = (WTPart) GenericUtil.REF_FACTORY.getReference(
	// oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bp = packages.getByPLMId(oid);
	// if (part == null) {
	// bp.setValue("IsSync", false);
	// bp.doUpdate();
	// } else {
	// part = (WTPart) Utils.getWCObject(WTPart.class,
	// part.getNumber());
	// lastOid = getObjectOid(part);
	// if (!oid.equals(lastOid)) {
	// bp.setValue("IsSync", lastOid);
	// bp.doUpdate();
	// }
	// }
	// } else if (pmType.equals("pmpart")) {
	// try {
	// part = (WTPart) GenericUtil.REF_FACTORY.getReference(
	// oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bp = pmparts.getByPLMId(oid);
	// if (part == null) {
	// bp.setValue("IsSync", false);
	// bp.doUpdate();
	// } else {
	// part = (WTPart) Utils.getWCObject(WTPart.class,
	// part.getNumber());
	// lastOid = getObjectOid(part);
	// if (!oid.equals(lastOid)) {
	// bp.setValue("IsSync", lastOid);
	// bp.doUpdate();
	// }
	// }
	// } else if (pmType.equals("pmproduct")) {
	// try {
	// part = (WTPart) GenericUtil.REF_FACTORY.getReference(
	// oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bp = pmproduct.getByPLMId(oid);
	// if (part == null) {
	// bp.setValue("IsSync", false);
	// bp.doUpdate();
	// } else {
	// part = (WTPart) Utils.getWCObject(WTPart.class,
	// part.getNumber());
	// lastOid = getObjectOid(part);
	// if (!oid.equals(lastOid)) {
	// bp.setValue("IsSync", lastOid);
	// bp.doUpdate();
	// }
	// }
	// } else if (pmType.equals("pmsupplyment")) {
	// try {
	// part = (WTPart) GenericUtil.REF_FACTORY.getReference(
	// oid).getObject();
	// } catch (Exception e) {
	// part = null;
	// }
	// bp = supplyment.getByPLMId(oid);
	// if (part == null) {
	// bp.setValue("IsSync", false);
	// bp.doUpdate();
	// } else {
	// part = (WTPart) Utils.getWCObject(WTPart.class,
	// part.getNumber());
	// lastOid = getObjectOid(part);
	// if (!oid.equals(lastOid)) {
	// bp.setValue("IsSync", lastOid);
	// bp.doUpdate();
	// }
	// }
	// }
	// }
	// }
	// }
	//
	// public static void SynchDocument2PM(String startTime)
	// throws InstantiationException, IllegalAccessException, Exception {
	// Debug.P("---------->>>>SynchDocument2PM:startTime=" + startTime);
	// if (!RemoteMethodServer.ServerFlag) {
	// try {
	// Class aclass[] = { String.class };
	// Object aobj[] = { startTime };
	// RemoteMethodServer.getDefault().invoke("SynchDocument2PM",
	// WCToPMHelper.class.getName(), null, aclass, aobj);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } else {
	// List<WTDocument> docList = new ArrayList<WTDocument>();
	// docList = EPMDocUtil.getAllDocument();
	// int i = 0;
	// for (WTDocument document : docList) {
	// i++;
	// Debug.P("----------------index-----------》" + i);
	// getPMDocument(document);
	// }
	// Debug.P("-----------------同步WTDocument完毕！--------------");
	// }
	// }
	//
	// private static Map<String, String> stateMap = new HashMap<String,
	// String>();
	// static {
	// stateMap.put(ConstanUtil.INWORK, ConstanUtil.WC_INWORK);
	// stateMap.put(ConstanUtil.APPROVE, ConstanUtil.WC_APPROVE);
	// stateMap.put(ConstanUtil.RELEASED, ConstanUtil.WC_RELEASED);
	// stateMap.put(ConstanUtil.DESPOSED, ConstanUtil.WC_DESPOSED);
	// }
	//
	// /**
	// * 查询系统中所有的Document 以OID去PM系统查询，如果不存在，则删除WC端的Document,
	// * 如果存在，则修改WC端文档的状态，修改PM端文档的版本
	// *
	// * @param document
	// * @throws Exception
	// * @throws InstantiationException
	// * @throws IllegalAccessException
	// */
	// public static void getPMDocument(WTDocument document) throws Exception,
	// InstantiationException, IllegalAccessException {
	// PMDocument pmdoc = null;
	// DocumentPersistence docper = null;
	// String stateName = "";
	// String state = "";
	// docper = getPersistence(DocumentPersistence.class);
	// String plmId = document.getPersistInfo().getObjectIdentifier()
	// .getStringValue();
	// try {
	// pmdoc = docper.getByPLMId(plmId);
	// } catch (NullPointerException e) {
	// pmdoc = null;
	// }
	// state = document.getState().getState().getStringValue();
	// Debug.P("文档-----》" + document.getNumber() + "---状态-----》" + state
	// + "---pmdoc----------->" + pmdoc != null);
	// if (pmdoc == null) {
	// Debug.P("删除文档-------------》" + document.getNumber());
	// PersistenceHelper.manager.delete(document);
	// } else {
	// stateName = pmdoc.getStatus();
	// Debug.P("更新WC文档-----》" + document.getNumber() + "更新PM文档-----》"
	// + pmdoc.getObjectNumber() + "----stateName------->"
	// + stateName);
	// pmdoc.setMajorVid(document.getVersionIdentifier().getValue());
	// pmdoc.setSecondVid(Integer.valueOf(document
	// .getIterationIdentifier().getValue()));
	// pmdoc.setValue("IsSync", plmId);
	// WriteResult result = pmdoc.doUpdate();
	// document = (WTDocument) GenericUtil.changeState(document,
	// stateMap.get(stateName));
	// PersistenceHelper.manager.refresh(document);
	// }
	// }
	//
	// public static void deletePMPart(String pmoid, WTPart wtPart) {
	// PartPersistence partPersistence = null;
	// try {
	// PMPart pmPart = null;
	// partPersistence = getPersistence(PartPersistence.class);
	// boolean flag = false;
	// ObjectId objecdId = null;
	// try {
	// objecdId = new ObjectId(pmoid);
	// pmPart = partPersistence.get(objecdId);
	// if (pmPart != null)
	// flag = true;
	// } catch (NullPointerException e) {
	// flag = false;
	// }
	// if (flag) {
	// pmPart = partPersistence.get(objecdId);
	// Debug.P("将Windchill中的半成品从PM系统的数据库中删除------------->"
	// + pmPart.getCommonName());
	// WriteResult wresult = pmPart.doRemove();
	// String error = wresult.getError();
	// if (StringUtils.isEmpty(error))
	// Debug.P("delete PMPart success");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void deletePMProduct(String pmoid, WTPart wtPart) {
	// ProductPersistence productPersistence = null;
	// try {
	// PMProduct productPart = null;
	// productPersistence = getPersistence(ProductPersistence.class);
	// boolean flag = false;
	// ObjectId objecdId = null;
	// try {
	// objecdId = new ObjectId(pmoid);
	// productPart = productPersistence.get(objecdId);
	// if (productPart != null)
	// flag = true;
	// } catch (NullPointerException e) {
	// flag = false;
	// }
	// if (flag) {
	// productPart = productPersistence.get(objecdId);
	// Debug.P("将Windchill中的成品从PM系统的数据库中删除--->"
	// + productPart.getCommonName());
	// WriteResult wresult = productPart.doRemove();
	// String error = wresult.getError();
	// if (StringUtils.isEmpty(error))
	// Debug.P("delete PMProduct success");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void deletePMMaterial(String pmoid, WTPart wtPart) {
	// MaterialPersistence materialPersistence = null;
	// try {
	// PMMaterial pmMaterial = null;
	// materialPersistence = getPersistence(MaterialPersistence.class);
	// boolean flag = false;
	// ObjectId objecdId = null;
	// try {
	// objecdId = new ObjectId(pmoid);
	// pmMaterial = materialPersistence.get(objecdId);
	// if (pmMaterial != null)
	// flag = true;
	// } catch (NullPointerException e) {
	// flag = false;
	// }
	// if (flag) {
	// pmMaterial = materialPersistence.get(objecdId);
	// Debug.P("将Windchill中的原材料从PM系统的数据库中删除-----》"
	// + pmMaterial.getCommonName());
	// WriteResult wresult = pmMaterial.doRemove();
	// String error = wresult.getError();
	// if (StringUtils.isEmpty(error))
	// Debug.P("delete PMMaterial success");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void deletePMPackage(String pmoid, WTPart wtPart) {
	// PackagePersistence packagePersistence = null;
	// try {
	// PMPackage pmPackage = null;
	//
	// packagePersistence = getPersistence(PackagePersistence.class);
	// boolean flag = false;
	// ObjectId objecdId = null;
	// try {
	// objecdId = new ObjectId(pmoid);
	// pmPackage = packagePersistence.get(objecdId);
	// if (pmPackage != null)
	// flag = true;
	// } catch (NullPointerException e) {
	// flag = false;
	// }
	// if (flag) {
	// pmPackage = packagePersistence.get(objecdId);
	// Debug.P("将Windchill中的包装材料从PM系统的数据库中删除-----》"
	// + pmPackage.getCommonName());
	// WriteResult wresult = pmPackage.doRemove();
	// String error = wresult.getError();
	// if (StringUtils.isEmpty(error))
	// Debug.P("delete pmPackage success");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void deletePMCADDoc(String pmoid, EPMDocument epmdoc) {
	// CADDocumentPersistence cadDocPersistence = null;
	// try {
	// PMCADDocument pmcad = null;
	//
	// cadDocPersistence = getPersistence(CADDocumentPersistence.class);
	// boolean flag = false;
	// ObjectId objecdId = null;
	// try {
	// objecdId = new ObjectId(pmoid);
	// pmcad = cadDocPersistence.get(objecdId);
	// if (pmcad != null)
	// flag = true;
	// } catch (NullPointerException e) {
	// flag = false;
	// }
	// if (flag) {
	// Debug.P("将Windchill中的PMCADDocument从PM系统的数据库中删除-----》"
	// + pmcad.getCommonName());
	// WriteResult wresult = pmcad.doRemove();
	// String error = wresult.getError();
	// if (StringUtils.isEmpty(error))
	// Debug.P("delete PMCADDocument success");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void deletePMJigTools(String pmoid, WTPart wtPart) {
	// JigToolsPersistence jigToolsPersistence = null;
	// try {
	// PMJigTools pmJigTools = null;
	//
	// jigToolsPersistence = getPersistence(JigToolsPersistence.class);
	// boolean flag = false;
	// ObjectId objecdId = null;
	// try {
	// objecdId = new ObjectId(pmoid);
	// pmJigTools = jigToolsPersistence.get(objecdId);
	// if (pmJigTools != null)
	// flag = true;
	// } catch (NullPointerException e) {
	// flag = false;
	// }
	// if (flag) {
	// pmJigTools = jigToolsPersistence.get(objecdId);
	// Debug.P("将Windchill中的备品备料从PM系统的数据库中删除-----》"
	// + pmJigTools.getCommonName());
	// WriteResult wresult = pmJigTools.doRemove();
	// String error = wresult.getError();
	// if (StringUtils.isEmpty(error))
	// Debug.P("delete pmJigTools success");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void deleteSupplyment(String pmoid, WTPart wtPart) {
	// SupplymentPersistence supplymentPersistence = null;
	// try {
	// PMSupplyment pmSupplyment = null;
	// supplymentPersistence = getPersistence(SupplymentPersistence.class);
	// boolean flag = false;
	// ObjectId objecdId = null;
	// try {
	// objecdId = new ObjectId(pmoid);
	// pmSupplyment = supplymentPersistence.get(objecdId);
	// if (pmSupplyment != null)
	// flag = true;
	// } catch (NullPointerException e) {
	// flag = false;
	// }
	// if (flag) {
	// pmSupplyment = supplymentPersistence.get(objecdId);
	// Debug.P("将Windchill中的客供件从PM系统的数据库中删除---》"
	// + pmSupplyment.getCommonName());
	// WriteResult wresult = pmSupplyment.doRemove();
	// String error = wresult.getError();
	// if (StringUtils.isEmpty(error))
	// Debug.P("delete Supplyment success");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// /**
	// * 删除PM端预删除标记为ture的对象
	// *
	// * @param type
	// * @param class1
	// * @throws InstantiationException
	// * @throws Exception
	// */
	//
	// public static void delatePMDoc(String type, Class class1)
	// throws InstantiationException, Exception {
	// List<String> result = new ArrayList<String>();
	// PMDocument pmdoc = null;
	// DocumentPersistence docper = null;
	// docper = getPersistence(DocumentPersistence.class);
	// result = docper.getDeletedDocumentList(type);
	// Debug.P("result.size ---->" + result.size() + "   class--->" + class1
	// + "  type----->" + type);
	//
	// for (String pmid : result) {
	// Debug.P("需要删除的PM对象OID--------》" + pmid);
	// if (StringUtils.isNotEmpty(pmid)) {
	// Object object = PMWebserviceImpl.searchWCObject(class1, pmid,
	// Contants.PMID);
	// Debug.P(" object----->" + object);
	// if (object == null) {
	// pmdoc = docper.getDocumentByPMId(pmid);
	// pmdoc.doRemove();
	// }
	// }
	// }
	// }
	//
	// /**
	// * WC端删除前事件中调用，在PM端的对象上设置预删除标记，再在WC端删除后事件中执行删除PM中的该对象
	// *
	// * @param pmoid
	// * @throws Exception
	// */
	// public static void updatePMDocument(String pmoid) throws Exception {
	// PMDocument pmdoc = null;
	// DocumentPersistence docper = null;
	// docper = getPersistence(DocumentPersistence.class);
	// if (StringUtils.isNotEmpty(pmoid)) {
	// pmdoc = docper.getDocumentByPMId(pmoid);
	// Debug.P(">>>>pmdoc: " + pmdoc);
	// if (pmdoc != null) {
	// pmdoc.setValue("deleted", true);
	// pmdoc.doUpdate();
	// }
	// }
	// }
	//
	// public static void getPMDocument(String oid, String pmType)
	// throws Exception {
	// if (StringUtils.isNotEmpty(pmType)) {
	// if (pmType.equals("epmdocument")) {
	// EPMDocument epmdoc = EPMUtil.getEPMDocumentByOid(oid);
	// if (epmdoc != null) {
	//
	// } else {
	//
	// }
	// } else if (pmType.equals("wtdocument")) {
	// WTDocument document = (WTDocument) GenericUtil.REF_FACTORY
	// .getReference(oid).getObject();
	// } else if (pmType.equals("wtpart")) {
	// WTPart part = (WTPart) GenericUtil.REF_FACTORY
	// .getReference(oid).getObject();
	// }
	// }
	// }
	//
	// /**
	// * 自定义获取当前时间格式
	// *
	// * @param formate
	// * @return
	// */
	// private static String getCurrentDate(String formate) {
	// if (StringUtils.isEmpty(formate)) {
	// formate = "yyyy-MM-dd HH:mm:ss";
	// }
	// SimpleDateFormat sdf = new SimpleDateFormat(formate);
	// return sdf.format(new Date());
	// }
	//
	// /**
	// * 自动同步Windchill未同步的的数据到PM系统(临时补丁类)
	// *
	// * @param startTime
	// * 开始时间
	// * @param endTime
	// * 结束时间
	// */
	// public static void autoSynchPLM2PM(String startTime) throws Exception {
	// Debug.P("---------->>>>autoSynchPLM2PM:startTime=" + startTime);
	// if (!RemoteMethodServer.ServerFlag) {
	// try {
	// Class aclass[] = { String.class };
	// Object aobj[] = { startTime };
	// RemoteMethodServer.getDefault().invoke("autoSynchPLM2PM",
	// WCToPMHelper.class.getName(), null, aclass, aobj);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } else { // 全量同步
	// // 查询系统里所有EPM和Part PMID数据
	// DocumentPersistence docpersistence =
	// getPersistence(DocumentPersistence.class);
	// String sql =
	// "select CONCAT(to_char(V1.CLASSNAMEKEYA4)||':', to_char(V1.IDA3A4)) as OUID,V1.VALUE2 as PMID  FROM STRINGVALUE V1,STRINGDEFINITION D1 WHERE ( V1.CLASSNAMEKEYA4='wt.part.WTPart' OR  V1.CLASSNAMEKEYA4='wt.epm.EPMDocument') AND D1.IDA2A2=V1.ida3a6  AND D1.name='PMId'";
	// List<Hashtable<String, String>> result = UserDefQueryUtil
	// .commonQuery(sql, new String[] {});
	// // 过滤掉已经同步过
	// Set<String> noSynList = new HashSet<String>();
	// Debug.P("------>>>Get All EPM和Part Result Size:" + result.size());
	// for (int i = 0; i < result.size(); i++) {
	// Hashtable<String, String> data_rows = result.get(i);// 行数据
	// String oid = null;
	// boolean flag = true;// 默认存在
	// for (Iterator<?> ite = data_rows.keySet().iterator(); ite
	// .hasNext();) {
	// String key = (String) ite.next();// 列名
	// String value = data_rows.get(key);// OUID
	// if (key.equalsIgnoreCase("OUID")) {
	// oid = value;
	// }
	// if (key.equalsIgnoreCase("PMID")) {
	// flag = docpersistence
	// .getDocumentById(value == null ? "" : value);
	// }
	// if (!flag) {
	// if (StringUtils.isNotEmpty(oid)) {
	// noSynList.add(oid);
	// }
	// }
	// }
	// }
	//
	// Debug.P("--->>>>Windchill未同步到PM的数据量大小:" + noSynList.size());
	// // 开始为为未同步的执行同步操作
	// for (Iterator<?> ite = noSynList.iterator(); ite.hasNext();) {
	// String objectId = (String) ite.next();
	// if (objectId.startsWith("wt.epm.EPMDocument")) {// EPM文档对象
	// EPMDocument epmdoc = (EPMDocument) GenericUtil
	// .getPersistableByOid(objectId);
	// if (epmdoc == null)
	// continue;
	// Debug.P("--->>>SynEPM:" + epmdoc.getNumber());
	// CreateEPMDoc(epmdoc);// 向PM写EPM数据
	// } else {// WTPart对象
	// Persistable object = GenericUtil
	// .getPersistableByOid(objectId);
	// if (object != null && object instanceof WTPart) {
	// Debug.P("------>>>Ready Create WTPART："
	// + object.getPersistInfo().getObjectIdentifier()
	// .getStringValue());
	// TypeIdentifier typeIde = TypeIdentifierUtility
	// .getTypeIdentifier(object);
	// String typeName = typeIde.getTypename();
	// Debug.P("--->>>>Type:" + typeName);
	// if (typeName.contains(Contants.SEMIFINISHEDPRODUCT)) {
	// CreatePMPart((WTPart) object);
	// } else if (typeName.contains(Contants.PRODUCTPART)) {
	// CreatePMProduct((WTPart) object);
	// } else if (typeName.contains(Contants.MATERIAL)) {
	// CreatePMMaterial((WTPart) object);
	// } else if (typeName.contains(Contants.SUPPLYMENT)) {
	// CreatePMSupply((WTPart) object);
	// } else if (typeName.contains(Contants.PACKINGPART)) {
	// CreatePMPackage((WTPart) object);
	// } else if (typeName.contains(Contants.TOOLPART)) {
	// CreatePMJigToolPart((WTPart) object);
	// }
	// Debug.P("------>>>>>Create Type(" + typeName
	// + ")2PM Success!!!");
	// }
	// }
	// }
	// }
	// }
}
