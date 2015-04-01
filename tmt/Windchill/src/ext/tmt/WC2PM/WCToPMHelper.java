package ext.tmt.WC2PM;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTProperties;

import com.mongodb.WriteResult;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.contextBuilder.util.PersistableHelper;
import com.sg.visionadapter.BasicDocument;
import com.sg.visionadapter.BasicPart;
import com.sg.visionadapter.CADDocumentPersistence;
import com.sg.visionadapter.DocumentPersistence;
import com.sg.visionadapter.FolderPersistence;
import com.sg.visionadapter.JigToolsPersistence;
import com.sg.visionadapter.MaterialPersistence;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMCADDocument;
import com.sg.visionadapter.PMDeliverable;
import com.sg.visionadapter.PMDocument;
import com.sg.visionadapter.PMFolder;
import com.sg.visionadapter.PMJigTools;
import com.sg.visionadapter.PMMaterial;
import com.sg.visionadapter.PMPackage;
import com.sg.visionadapter.PMPart;
import com.sg.visionadapter.PMProduct;
import com.sg.visionadapter.PMSupplyment;
import com.sg.visionadapter.PackagePersistence;
import com.sg.visionadapter.PartPersistence;
import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.ProductPersistence;
import com.sg.visionadapter.SupplymentPersistence;

import ext.tmt.integration.webservice.pm.ConstanUtil;
import ext.tmt.integration.webservice.pm.PMWebserviceImpl;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.UserDefQueryUtil;
import ext.tmt.utils.Utils;

/**
 * WC同步EPMDocument,wtpart到PM系统
 * 
 * @author Administrator
 *
 */
@SuppressWarnings("all")
public class WCToPMHelper implements RemoteAccess, Serializable {
	private static String codebasePath = null;

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

	public static void CreatePartToPM(WTPart wtPart) {
		String partOid = "";
		String partFolderString = "";
		Folder partFolder = null;
		String pFolderId = "";
		PartPersistence partPersistence = null;
		String weight = "";
		Debug.P("将Windchill中的半成品插入PM系统的数据库中");
		partOid = getObjectOid(wtPart);
		try {
			PMPart pmPart = null;
			partPersistence = (PartPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PartPersistence.class);

			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(partOid);
			Debug.P(wtPart.getContainerName());
			partFolderString = wtPart.getFolderPath();
			partFolder = FolderHelper.service.getFolder(wtPart);
			Debug.P(partFolder);
			pFolderId = partFolder.getPersistInfo().getObjectIdentifier()
					.getStringValue();
			Debug.P(pFolderId);
			boolean flag = true;
			try {
				pmPart = (PMPart) partPersistence.getByPLMId(partOid);
			} catch (NullPointerException e) {
				pmPart = null;
			}
			Debug.P("pmPart是否存在-->" + pmPart != null);
			try {
				PMFolder pmfolder = (PMFolder) ((FolderPersistence) ModelServiceFactory
						.getInstance(codebasePath).get(FolderPersistence.class))
						.getByPLMId(pFolderId);
				if (pmfolder == null)
					flag = false;
			} catch (NullPointerException e) {
				flag = false;
			}
			Debug.P("pm中是否存在文件夹OID为--》" + pFolderId + "----->" + flag);
			String pmoid = "";
			pmoid = partiba.getIBAValue("PMId");
			if (flag) {
				Debug.P("pmPart-->" + pmoid);
				if (pmPart != null) {
					Debug.P("update pmpart --->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						updatePMPart(pmoid, wtPart);
					}
				} else {
					pmPart = (PMPart) partPersistence.newInstance();
					pmPart.setFolderIdByPLMId(pFolderId);
					ObjectId objectId = null;
					Debug.P("半成品--》pmoid----->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						objectId = new ObjectId(pmoid);
					} else {
						objectId = new ObjectId();
					}
					pmPart.set_id(objectId);
					pmPart.setPLMId(partOid);
					pmPart.setCommonName(wtPart.getName());
					pmPart.setObjectNumber(wtPart.getNumber());
					pmPart.setStatus(wtPart.getState().toString().toLowerCase());
					pmPart.setCreateBy(wtPart.getCreatorName(),
							wtPart.getCreatorFullName());
					pmPart.setMajorVid(wtPart.getVersionIdentifier().getValue());
					pmPart.setSecondVid(Integer.parseInt(wtPart
							.getIterationIdentifier().getValue()));
					pmPart.setPhase(partiba.getIBAValue("PHASE") == null ? ""
							: partiba.getIBAValue("PHASE"));

					pmPart.setSpec(partiba.getIBAValue("Specifications") == null ? ""
							: partiba.getIBAValue("Specifications"));
					weight = partiba.getIBAValue("Weight");
					if (StringUtils.isNotEmpty(weight))
						pmPart.setWeight(NumberFormat.getInstance().parse(
								weight));
					pmPart.setProductNumber(partiba.getIBAValue("Product_NO") == null ? ""
							: partiba.getIBAValue("Product_NO"));
					String masterid = wtPart.getMaster().getPersistInfo()
							.toString();
					Debug.P("masterid------>" + masterid);
					Map plmData = new HashMap();
					pmPart.SetMasterId(masterid);
					plmData.put("number", wtPart.getNumber());
					plmData.put("plmmid", "wt.part.WTPart:"
							+ wtPart.getIterationInfo().getBranchId());
					pmPart.setPLMData(plmData);
					pmPart.setOwner(wtPart.getCreatorName());
					pmPart.setMaterial(partiba.getIBAValue("Material") == null ? ""
							: partiba.getIBAValue("Material"));
					pmPart.setValue("IsSync", true);
					pmPart.initPlmData();

					String docInfo = pmPart.serialize();
					pmoid = InsterOrUpdatePMDoc(docInfo);
					// WriteResult wresult = pmPart.doInsert();
					// String error = wresult.getError();
					if (StringUtils.isEmpty(pmoid)) {
						partiba.setIBAValue("PMId", pmoid);
						String data = Utils.getDate();
						Debug.P(data);
						partiba.setIBAValue("CyncData", data);
						partiba.setIBAValue("PMRequest", "create");
						partiba.updateIBAPart(wtPart);
						reloadPermission(objectId.toString());
						reloadDeliverable(objectId.toString());
						Debug.P("create PMPart success");
					}
				}
			} else {
				Debug.P();
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
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

	public static void CreatePMProductToPM(WTPart wtPart) {

		String partOid = "";
		String partFolderString = "";
		Folder partFolder = null;
		String pFolderId = "";
		ProductPersistence productPersistence = null;
		String weight = "";
		Debug.P("将Windchill中的成品插入PM系统的数据库中");
		Debug.P(wtPart.toString());
		partOid = getObjectOid(wtPart);
		Debug.P(wtPart.getPersistInfo().getObjectIdentifier().getId());
		Debug.P(partOid);
		ModelServiceFactory factory = ModelServiceFactory
				.getInstance(codebasePath);
		try {
			productPersistence = factory.get(ProductPersistence.class);
			PMProduct pmProduct = null;
			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(">>>>ProjectNo:" + partiba.getIBAValue(Contants.PROJECTNO));
			partFolderString = wtPart.getFolderPath();
			Debug.P(partFolderString);
			partFolder = FolderHelper.service.getFolder(wtPart);

			Debug.P(partFolder);
			Debug.P(pFolderId);
			pFolderId = partFolder.getPersistInfo().getObjectIdentifier()
					.getStringValue();
			Debug.P(pFolderId);
			boolean flag = true;
			try {
				pmProduct = (PMProduct) productPersistence.getByPLMId(partOid);
			} catch (NullPointerException e) {
				pmProduct = null;
			}
			Debug.P("pmProduct-->" + pmProduct != null);
			try {
				PMFolder pmfolder = (PMFolder) ((FolderPersistence) factory
						.get(FolderPersistence.class)).getByPLMId(pFolderId);
				if (pmfolder == null)
					flag = false;
			} catch (NullPointerException e) {
				flag = false;
			}
			Debug.P("pm中是否存在文件夹OID为--》" + pFolderId + "----->" + flag);
			String pmoid = partiba.getIBAValue("PMId");
			if (flag) {
				Debug.P("pmProduct-->" + pmProduct != null);
				if (pmProduct != null) {
					if (StringUtils.isNotEmpty(pmoid))
						updatePMProductToPM(pmoid, wtPart);
				} else {
					pmProduct = (PMProduct) productPersistence.newInstance();
					pmProduct.setFolderIdByPLMId(pFolderId);
					pmProduct.setPLMId(partOid);
					Map plmData = new HashMap();
					plmData.put("number", wtPart.getNumber());
					plmData.put("plmmid", "wt.part.WTPart:"
							+ wtPart.getIterationInfo().getBranchId());
					Debug.P(plmData);

					ObjectId objectId = null;
					Debug.P("成品--》pmoid----->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						objectId = new ObjectId(pmoid);
					} else {
						objectId = new ObjectId();
					}
					pmProduct.set_id(objectId);
					pmProduct.setPLMId(wtPart.getPersistInfo()
							.getObjectIdentifier().getStringValue());
					pmProduct.setPLMData(plmData);
					pmProduct.setCommonName(wtPart.getName());
					pmProduct.setObjectNumber(wtPart.getNumber());
					pmProduct.setProductNumber(wtPart.getNumber());
					pmProduct.setStatus(wtPart.getState().toString()
							.toLowerCase());
					pmProduct.setCreateBy(wtPart.getCreatorName(),
							wtPart.getCreatorFullName());
					pmProduct.setMajorVid(wtPart.getVersionIdentifier()
							.getValue());
					pmProduct.setSecondVid(Integer.parseInt(wtPart
							.getIterationIdentifier().getValue()));
					pmProduct
							.setPhase(partiba.getIBAValue("PHASE") == null ? ""
									: partiba.getIBAValue("PHASE"));
					pmProduct
							.setSpec(partiba.getIBAValue("Specifications") == null ? ""
									: partiba.getIBAValue("Specifications"));
					weight = partiba.getIBAValue("Weight");
					if (StringUtils.isNotEmpty(weight))
						pmProduct.setWeight(NumberFormat.getInstance().parse(
								weight));
					pmProduct.setFormularNumber(partiba
							.getIBAValue("Formula_NO") == null ? "" : partiba
							.getIBAValue("Formula_NO"));
					pmProduct
							.setMaterial(partiba.getIBAValue("Material") == null ? ""
									: partiba.getIBAValue("Material"));
					pmProduct.setOwner(wtPart.getCreatorName());
					pmProduct.setValue("IsSync", true);
					pmProduct.WriteResult wresult = pmProduct.doInsert();
					String error = wresult.getError();
					if (StringUtils.isEmpty(error)) {
						partiba.setIBAValue("PMId", objectId.toString());
						partiba.setIBAValue("CyncData", Utils.getDate());
						partiba.setIBAValue("PMRequest", "create");
						partiba.updateIBAPart(wtPart);
						reloadPermission(objectId.toString());
						reloadDeliverable(objectId.toString());
						Debug.P("create pmproduct success");
						productNumToProductItem(objectId.toString());
					}

					// 回写PM成品编号
					/*
					 * String projectNumber = partiba
					 * .getIBAValue(Contants.PROJECTNO); PMProject pmproject =
					 * factory.get(PMProject.class); Debug.P("----ProjectNo:" +
					 * projectNumber + "   PMProject:" + pmproject); ObjectId
					 * pmProjectId = factory.get(PMProject.class)
					 * .getProjectIdByProjectNum(projectNumber);
					 * Debug.P("--->>>>pmProjectId:" + pmProjectId); if
					 * (pmProjectId != null) { PMProductItem pmProd = new
					 * PMProductItem(); pmProd =
					 * factory.get(PMProductItem.class);
					 * pmProd.setProductNumber(wtPart.getNumber());
					 * pmProd.setProjectId(pmProjectId);
					 * pmProd.setUserId("PM-RW"); pmProd.setUserName("PLM系统");
					 * pmProd.setDate(getCurrentDate("yyyy/MM/dd HH:mm:ss"));
					 * pmProd.doInsertProductNumToProductItem(); }
					 */
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 自定义获取当前时间格式
	 * 
	 * @param formate
	 * @return
	 */
	private static String getCurrentDate(String formate) {
		if (StringUtils.isEmpty(formate)) {
			formate = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(formate);
		return sdf.format(new Date());
	}

	public static void CreatePMaterialToPM(WTPart wtPart) {
		String partOid = "";
		String partFolderString = "";
		Folder partFolder = null;
		String pFolderId = "";
		MaterialPersistence materialPersistence = null;

		String weight = "";
		Debug.P("将Windchill中的原材料插入PM系统的数据库中");
		partOid = getObjectOid(wtPart);
		try {
			PMMaterial pmMaterial = null;
			materialPersistence = (MaterialPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(MaterialPersistence.class);

			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(partOid);
			partFolderString = wtPart.getFolderPath();
			Debug.P(partFolderString);
			partFolder = FolderHelper.service.getFolder(wtPart);

			Debug.P(partFolder);
			pFolderId = partFolder.getPersistInfo().getObjectIdentifier()
					.getStringValue();
			Debug.P(pFolderId);
			boolean flag = true;
			try {
				pmMaterial = (PMMaterial) materialPersistence
						.getByPLMId(partOid);
			} catch (NullPointerException e) {
				pmMaterial = null;
			}
			Debug.P("pmMaterial-->" + pmMaterial != null);
			try {
				PMFolder pmfolder = (PMFolder) ((FolderPersistence) ModelServiceFactory
						.getInstance(codebasePath).get(FolderPersistence.class))
						.getByPLMId(pFolderId);
				if (pmfolder == null)
					flag = false;
			} catch (NullPointerException e) {
				flag = false;
			}
			Debug.P("pm中是否存在文件夹OID为--》" + pFolderId + "----->" + flag);
			String pmoid = partiba.getIBAValue("PMId");
			if (flag) {
				Debug.P("pmMaterial-->" + pmMaterial != null);
				if (pmMaterial != null) {
					if (StringUtils.isNotEmpty(pmoid))
						updatePMaterialToPM(pmoid, wtPart);
				} else {
					pmMaterial = (PMMaterial) materialPersistence.newInstance();
					pmMaterial.setFolderIdByPLMId(pFolderId);
					pmMaterial.setPLMId(partOid);
					ObjectId objectId = null;
					Debug.P("pmMaterial--》pmoid----->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						objectId = new ObjectId(pmoid);
					} else {
						objectId = new ObjectId();
					}
					Map plmData = new HashMap();
					plmData.put("number", wtPart.getNumber());
					plmData.put("plmmid", "wt.part.WTPart:"
							+ wtPart.getIterationInfo().getBranchId());
					pmMaterial.setPLMData(plmData);
					pmMaterial.setCommonName(wtPart.getName());
					pmMaterial.setObjectNumber(wtPart.getNumber());
					pmMaterial.setStatus(wtPart.getState().toString()
							.toLowerCase());
					pmMaterial.setCreateBy(wtPart.getCreatorName(),
							wtPart.getCreatorFullName());
					pmMaterial.setMajorVid(wtPart.getVersionIdentifier()
							.getValue());
					pmMaterial.setSecondVid(Integer.parseInt(wtPart
							.getIterationIdentifier().getValue()));
					pmMaterial
							.setPhase(partiba.getIBAValue("PHASE") == null ? ""
									: partiba.getIBAValue("PHASE"));

					pmMaterial
							.setSpec(partiba.getIBAValue("Specifications") == null ? ""
									: partiba.getIBAValue("Specifications"));
					weight = partiba.getIBAValue("Weight");
					if (StringUtils.isNotEmpty(weight))
						pmMaterial.setWeight(NumberFormat.getInstance().parse(
								weight));
					pmMaterial.setMaterialGroup(partiba
							.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
					pmMaterial.set_id(objectId);
					pmMaterial.setOwner(wtPart.getCreatorName());
					pmMaterial
							.setMaterial(partiba.getIBAValue("Material") == null ? ""
									: partiba.getIBAValue("Material"));
					pmMaterial.setValue("IsSync", true);
					WriteResult wresult = pmMaterial.doInsert();
					String error = wresult.getError();
					if (StringUtils.isEmpty(error)) {
						partiba.setIBAValue("PMId", objectId.toString());
						partiba.setIBAValue("CyncData", Utils.getDate());
						partiba.setIBAValue("PMRequest", "create");
						partiba.updateIBAPart(wtPart);
						reloadPermission(objectId.toString());
						reloadDeliverable(objectId.toString());
						Debug.P("create PMMaterial success");
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreateSupplyToPM(WTPart wtPart) {
		String partOid = "";
		String partFolderString = "";
		Folder partFolder = null;
		String pFolderId = "";
		SupplymentPersistence supplymentPersistence = null;

		String weight = "";
		Debug.P("将Windchill中的客供件插入PM系统的数据库中");
		partOid = getObjectOid(wtPart);
		try {
			PMSupplyment pmSupplyment = null;
			supplymentPersistence = (SupplymentPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(SupplymentPersistence.class);

			IBAUtils partiba = new IBAUtils(wtPart);

			Debug.P(partOid);
			partFolderString = wtPart.getFolderPath();
			Debug.P(partFolderString);
			partFolder = FolderHelper.service.getFolder(wtPart);

			Debug.P(partFolder);
			pFolderId = partFolder.getPersistInfo().getObjectIdentifier()
					.getStringValue();
			Debug.P(pFolderId);
			boolean flag = true;
			try {
				pmSupplyment = (PMSupplyment) supplymentPersistence
						.getByPLMId(partOid);
			} catch (NullPointerException e) {
				pmSupplyment = null;
			}
			try {
				PMFolder pmfolder = (PMFolder) ((FolderPersistence) ModelServiceFactory
						.getInstance(codebasePath).get(FolderPersistence.class))
						.getByPLMId(pFolderId);
				if (pmfolder == null)
					flag = false;
			} catch (NullPointerException e) {
				flag = false;
			}
			Debug.P("pm中是否存在文件夹OID为--》" + pFolderId + "----->" + flag);
			String pmoid = partiba.getIBAValue("PMId");
			if (flag) {
				Debug.P("pmSupplyment-->" + pmSupplyment != null);
				if (pmSupplyment != null) {
					if (StringUtils.isNotEmpty(pmoid))
						updateSupplyToPM(pmoid, wtPart);
				} else {
					pmSupplyment = (PMSupplyment) supplymentPersistence
							.newInstance();
					pmSupplyment.setFolderIdByPLMId(pFolderId);
					pmSupplyment.setPLMId(partOid);
					Map plmData = new HashMap();
					plmData.put("number", wtPart.getNumber());
					plmData.put("plmmid", "wt.part.WTPart:"
							+ wtPart.getIterationInfo().getBranchId());
					pmSupplyment.setPLMData(plmData);
					pmSupplyment.setObjectNumber(wtPart.getNumber());
					pmSupplyment.setCommonName(wtPart.getName());
					pmSupplyment.setStatus(wtPart.getState().toString()
							.toLowerCase());
					pmSupplyment.setCreateBy(wtPart.getCreatorName(),
							wtPart.getCreatorFullName());
					pmSupplyment.setMajorVid(wtPart.getVersionIdentifier()
							.getValue());
					pmSupplyment.setSecondVid(Integer.parseInt(wtPart
							.getIterationIdentifier().getValue()));
					pmSupplyment
							.setPhase(partiba.getIBAValue("PHASE") == null ? ""
									: partiba.getIBAValue("PHASE"));

					pmSupplyment
							.setSpec(partiba.getIBAValue("Specifications") == null ? ""
									: partiba.getIBAValue("Specifications"));
					weight = partiba.getIBAValue("Weight");
					if (StringUtils.isNotEmpty(weight))
						pmSupplyment.setWeight(NumberFormat.getInstance()
								.parse(weight));
					pmSupplyment.setCustomerName(partiba
							.getIBAValue("Client_Name") == null ? "" : partiba
							.getIBAValue("Client_Name"));
					pmSupplyment.setMaterialGroup(partiba
							.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
					ObjectId objectId = null;
					Debug.P("PMSupplyment--》pmoid----->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						objectId = new ObjectId(pmoid);
					} else {
						objectId = new ObjectId();
					}
					pmSupplyment.set_id(objectId);
					pmSupplyment
							.setMaterial(partiba.getIBAValue("Material") == null ? ""
									: partiba.getIBAValue("Material"));
					pmSupplyment.setOwner(wtPart.getCreatorName());
					pmSupplyment.setValue("IsSync", true);
					WriteResult wresult = pmSupplyment.doInsert();
					String error = wresult.getError();
					if (StringUtils.isEmpty(error)) {
						partiba.setIBAValue("PMId", objectId.toString());
						partiba.setIBAValue("CyncData", Utils.getDate());
						partiba.setIBAValue("PMRequest", "create");
						partiba.updateIBAPart(wtPart);
						reloadPermission(objectId.toString());
						reloadDeliverable(objectId.toString());
						Debug.P("create PMSupplyment success");
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreateEPMDocToPM(EPMDocument epmdoc) {
		String docOid = "";
		String docFolderString = "";
		Folder docFolder = null;
		String pFolderId = "";
		CADDocumentPersistence cadDocPersistence = null;
		Debug.P("将Windchill中的EPMDocument插入PM系统的数据库中");
		docOid = getObjectOid(epmdoc);
		try {
			PMCADDocument pmcad = null;
			cadDocPersistence = (CADDocumentPersistence) ModelServiceFactory
					.getInstance(codebasePath)
					.get(CADDocumentPersistence.class);

			IBAUtils cadiba = new IBAUtils(epmdoc);
			String pmid = cadiba.getIBAValue(Contants.PMID);
			Debug.P(">>>>WCOid：" + docOid + "  PMOID：" + pmid);
			docFolderString = epmdoc.getFolderPath();
			Debug.P(docFolderString);
			docFolder = FolderHelper.service.getFolder(epmdoc);

			Debug.P(docFolder);
			String part_type = cadiba.getIBAValue("Part_Type");
			if (StringUtils.isNotEmpty(part_type)) {
				part_type = part_type.replaceAll(" ", "").trim();
			}
			pFolderId = docFolder.getPersistInfo().getObjectIdentifier()
					.getStringValue();
			Debug.P(pFolderId);
			boolean flag = true;
			if (pmid != null) {
				try {
					pmcad = (PMCADDocument) cadDocPersistence.get(new ObjectId(
							pmid));
				} catch (NullPointerException e) {
					pmcad = null;
				}
			}
			// Debug.P("pmcad-->" + pmcad);
			try {
				PMFolder pmfolder = (PMFolder) ((FolderPersistence) ModelServiceFactory
						.getInstance(codebasePath).get(FolderPersistence.class))
						.getByPLMId(pFolderId);
				if (pmfolder == null)
					flag = false;
			} catch (NullPointerException e) {
				flag = false;
			}
			Debug.P("pm中是否存在文件夹OID为--》" + pFolderId + "----->" + flag);
			String pmoid = cadiba.getIBAValue("PMId");
			if (flag) {
				Debug.P("pmcad-->" + pmcad != null);
				if (pmcad != null) {
					if (StringUtils.isNotEmpty(pmoid))
						updatePMCADDoc(pmoid, epmdoc);
				} else {
					pmcad = (PMCADDocument) cadDocPersistence.newInstance();
					pmcad.setFolderIdByPLMId(pFolderId);
					pmcad.setPLMId(docOid);
					Map plmData = new HashMap();
					plmData.put("number", epmdoc.getNumber());
					plmData.put(
							"AirSpringClassification",
							cadiba.getIBAValue("AirSpringClassification") == null ? ""
									: cadiba.getIBAValue("AirSpringClassification"));
					plmData.put("Part_Type",
							cadiba.getIBAValue("Part_Type") == null ? ""
									: cadiba.getIBAValue("Part_Type"));
					plmData.put("plmmid", "wt.epm.EPMDocument:"
							+ epmdoc.getIterationInfo().getBranchId());
					pmcad.setPLMData(plmData);
					pmcad.setCommonName(epmdoc.getName());
					pmcad.setObjectNumber(epmdoc.getNumber());
					pmcad.setStatus(epmdoc.getState().toString().toLowerCase());
					pmcad.setCreateBy(epmdoc.getCreatorName(),
							epmdoc.getCreatorFullName());
					pmcad.setMajorVid(epmdoc.getVersionIdentifier().getValue());
					pmcad.setSecondVid(Integer.parseInt(epmdoc
							.getIterationIdentifier().getValue()));
					pmcad.setPhase(cadiba.getIBAValue("PHASE") == null ? ""
							: cadiba.getIBAValue("PHASE"));
<<<<<<< HEAD
                    String contentMD5=GenericUtil.getMd5ByFile(epmdoc);
                    Debug.P("contentMD5----->"+contentMD5);
=======
					String contentMD5 = GenericUtil.getMd5ByFile(epmdoc);
					Debug.P("contentMD5----->" + contentMD5);
>>>>>>> branch 'master' of https://github.com/sgewuhan/visionadapter.git
					pmcad.setContentMD5(contentMD5);
					pmcad.setDrawingNumber(cadiba.getIBAValue("Material_NO") == null ? ""
							: cadiba.getIBAValue("Material_NO"));
					pmcad.setPartType0(part_type == null ? "" : part_type);
					ObjectId objectId = null;
					Debug.P("PMCADDocument--》pmoid----->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						objectId = new ObjectId(pmoid);
					} else {
						objectId = new ObjectId();
					}
<<<<<<< HEAD
//					pmcad.set_id(objectId);
=======
					// pmcad.set_id(objectId);
>>>>>>> branch 'master' of https://github.com/sgewuhan/visionadapter.git
					pmcad.setValue("IsSync", true);
					pmcad.setOwner(epmdoc.getCreatorName());
					pmcad.setValue("cadName", epmdoc.getCADName());
<<<<<<< HEAD
					String masterid=epmdoc.getMaster().getPersistInfo().toString();
					pmcad.SetMasterId(masterid);
					 String docInfo=pmcad.serialize();
					  pmoid=InsterOrUpdatePMDoc(docInfo);	
//					WriteResult wresult = pmcad.doInsert();
//					String error = wresult.getError();
=======
					String masterid = epmdoc.getMaster().getPersistInfo()
							.toString();
					pmcad.SetMasterId(masterid);
					pmcad.initPlmData();
					String docInfo = pmcad.serialize();
					pmoid = InsterOrUpdatePMDoc(docInfo);
					// WriteResult wresult = pmcad.doInsert();
					// String error = wresult.getError();
>>>>>>> branch 'master' of https://github.com/sgewuhan/visionadapter.git
					if (StringUtils.isEmpty(pmoid)) {
						cadiba.setIBAValue("PMId", pmoid);
						cadiba.setIBAValue("CyncData", Utils.getDate());
						cadiba.setIBAValue("PMRequest", "create");
						cadiba.updateIBAPart(epmdoc);
						reloadPermission(objectId.toString());
						reloadDeliverable(objectId.toString());
						Debug.P("create PMCADDocument success");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreatePMPackageToPM(WTPart wtPart) {
		String partOid = "";
		String partFolderString = "";
		Folder partFolder = null;
		String pFolderId = "";
		PackagePersistence packagePersistence = null;

		String weight = "";
		Debug.P("将Windchill中的包装材料插入PM系统的数据库中");
		partOid = getObjectOid(wtPart);
		try {
			PMPackage pmPackage = null;
			packagePersistence = (PackagePersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PackagePersistence.class);

			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(partOid);
			partFolderString = wtPart.getFolderPath();
			Debug.P(partFolderString);
			partFolder = FolderHelper.service.getFolder(wtPart);

			Debug.P(partFolder);
			pFolderId = partFolder.getPersistInfo().getObjectIdentifier()
					.getStringValue();
			Debug.P(pFolderId);
			boolean flag = true;
			try {
				pmPackage = (PMPackage) packagePersistence.getByPLMId(partOid);
			} catch (NullPointerException e) {
				pmPackage = null;
			}
			try {
				PMFolder pmfolder = (PMFolder) ((FolderPersistence) ModelServiceFactory
						.getInstance(codebasePath).get(FolderPersistence.class))
						.getByPLMId(pFolderId);
				if (pmfolder == null)
					flag = false;
			} catch (NullPointerException e) {
				flag = false;
			}
			Debug.P("pm中是否存在文件夹OID为--》" + pFolderId + "----->" + flag);
			String pmoid = partiba.getIBAValue("PMId");
			if (flag) {
				Debug.P("pmPackage-->" + pmPackage != null);
				if (pmPackage != null) {
					if (StringUtils.isNotEmpty(pmoid))
						updatePMPackageToPM(pmoid, wtPart);
				} else {
					pmPackage = (PMPackage) packagePersistence.newInstance();
					pmPackage.setFolderIdByPLMId(pFolderId);
					pmPackage.setPLMId(partOid);
					Map plmData = new HashMap();
					plmData.put("number", wtPart.getNumber());
					plmData.put("plmmid", "wt.part.WTPart:"
							+ wtPart.getIterationInfo().getBranchId());
					pmPackage.setPLMData(plmData);
					pmPackage.setCommonName(wtPart.getName());
					pmPackage.setObjectNumber(wtPart.getNumber());
					pmPackage.setStatus(wtPart.getState().toString()
							.toLowerCase());
					pmPackage.setCreateBy(wtPart.getCreatorName(),
							wtPart.getCreatorFullName());
					pmPackage.setMajorVid(wtPart.getVersionIdentifier()
							.getValue());
					pmPackage.setSecondVid(Integer.parseInt(wtPart
							.getIterationIdentifier().getValue()));
					pmPackage
							.setPhase(partiba.getIBAValue("PHASE") == null ? ""
									: partiba.getIBAValue("PHASE"));

					pmPackage
							.setSpec(partiba.getIBAValue("Specifications") == null ? ""
									: partiba.getIBAValue("Specifications"));
					weight = partiba.getIBAValue("Weight");
					if (StringUtils.isNotEmpty(weight))
						pmPackage.setWeight(NumberFormat.getInstance().parse(
								weight));
					pmPackage.setMaterialGroup(partiba
							.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
					ObjectId objectId = null;
					Debug.P("pmPackage--》pmoid----->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						objectId = new ObjectId(pmoid);
					} else {
						objectId = new ObjectId();
					}
					pmPackage.set_id(objectId);
					pmPackage.setOwner(wtPart.getCreatorName());
					pmPackage
							.setMaterial(partiba.getIBAValue("Material") == null ? ""
									: partiba.getIBAValue("Material"));
					pmPackage.setValue("IsSync", true);
					WriteResult wresult = pmPackage.doInsert();
					String error = wresult.getError();
					if (StringUtils.isEmpty(error)) {
						partiba.setIBAValue("PMId", objectId.toString());
						partiba.setIBAValue("CyncData", Utils.getDate());
						partiba.setIBAValue("PMRequest", "create");
						partiba.updateIBAPart(wtPart);
						reloadPermission(objectId.toString());
						reloadDeliverable(objectId.toString());
						Debug.P("create pmPackage success");
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void CreateJigToolPartToPM(WTPart wtPart) {
		String partOid = "";
		String partFolderString = "";
		Folder partFolder = null;
		String pFolderId = "";
		JigToolsPersistence jigTollsPersistence = null;

		String weight = "";
		Debug.P("将Windchill中的备品备料插入PM系统的数据库中");
		partOid = getObjectOid(wtPart);
		try {
			PMJigTools pmJigTools = null;
			jigTollsPersistence = (JigToolsPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(JigToolsPersistence.class);

			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(partOid);
			partFolderString = wtPart.getFolderPath();
			Debug.P(partFolderString);
			partFolder = FolderHelper.service.getFolder(wtPart);
			Debug.P(partFolder);
			pFolderId = partFolder.getPersistInfo().getObjectIdentifier()
					.getStringValue();
			Debug.P(pFolderId);
			boolean flag = true;
			try {
				pmJigTools = (PMJigTools) jigTollsPersistence
						.getByPLMId(partOid);
			} catch (NullPointerException e) {
				pmJigTools = null;
			}
			try {
				PMFolder pmfolder = (PMFolder) ((FolderPersistence) ModelServiceFactory
						.getInstance(codebasePath).get(FolderPersistence.class))
						.getByPLMId(pFolderId);
				if (pmfolder == null)
					flag = false;
			} catch (NullPointerException e) {
				flag = false;
			}
			Debug.P("pm中是否存在文件夹OID为--》" + pFolderId + "----->" + flag);
			String pmoid = partiba.getIBAValue("PMId");
			if (flag) {
				Debug.P("pmJigTools-->" + pmJigTools != null);
				if (pmJigTools != null) {
					if (StringUtils.isNotEmpty(pmoid))
						UpdateJigToolPartToPM(pmoid, wtPart);
				} else {
					pmJigTools = (PMJigTools) jigTollsPersistence.newInstance();
					pmJigTools.setFolderIdByPLMId(pFolderId);
					pmJigTools.setPLMId(partOid);
					Map plmData = new HashMap();
					plmData.put("number", wtPart.getNumber());
					plmData.put("plmmid", "wt.part.WTPart:"
							+ wtPart.getIterationInfo().getBranchId());
					pmJigTools.setPLMData(plmData);
					pmJigTools.setCommonName(wtPart.getName());
					pmJigTools.setObjectNumber(wtPart.getNumber());
					pmJigTools.setStatus(wtPart.getState().toString()
							.toLowerCase());
					pmJigTools.setCreateBy(wtPart.getCreatorName(),
							wtPart.getCreatorFullName());
					pmJigTools.setMajorVid(wtPart.getVersionIdentifier()
							.getValue());
					pmJigTools.setSecondVid(Integer.parseInt(wtPart
							.getIterationIdentifier().getValue()));
					pmJigTools
							.setPhase(partiba.getIBAValue("PHASE") == null ? ""
									: partiba.getIBAValue("PHASE"));

					pmJigTools
							.setSpec(partiba.getIBAValue("Specifications") == null ? ""
									: partiba.getIBAValue("Specifications"));
					weight = partiba.getIBAValue("Weight");
					if (StringUtils.isNotEmpty(weight))
						pmJigTools.setWeight(NumberFormat.getInstance().parse(
								weight));
					pmJigTools.setMaterialGroup(partiba
							.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
					ObjectId objectId = null;
					Debug.P("pmJigTools--》pmoid----->" + pmoid);
					if (StringUtils.isNotEmpty(pmoid)) {
						objectId = new ObjectId(pmoid);
					} else {
						objectId = new ObjectId();
					}
					pmJigTools.set_id(objectId);
					pmJigTools.setValue("IsSync", true);
					pmJigTools.setOwner(wtPart.getCreatorName());
					pmJigTools
							.setMaterial(partiba.getIBAValue("Material") == null ? ""
									: partiba.getIBAValue("Material"));
					WriteResult wresult = pmJigTools.doInsert();
					String error = wresult.getError();
					if (StringUtils.isEmpty(error)) {
						partiba.setIBAValue("PMId", objectId.toString());
						partiba.setIBAValue("CyncData", Utils.getDate());
						partiba.setIBAValue("PMRequest", "create");
						partiba.updateIBAPart(wtPart);
						reloadPermission(objectId.toString());
						reloadDeliverable(objectId.toString());
						Debug.P("create pmJigTools success");
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMCADDoc(String pmoid, EPMDocument epmdoc) {
		String docOid = "";
		CADDocumentPersistence cadDocPersistence = null;
		Debug.P("更新Windchill中的EPMDocument-----------");
		docOid = epmdoc.toString();
		try {
			PMCADDocument pmcad = null;
			cadDocPersistence = (CADDocumentPersistence) ModelServiceFactory
					.getInstance(codebasePath)
					.get(CADDocumentPersistence.class);
			pmcad = (PMCADDocument) cadDocPersistence.get(new ObjectId(pmoid));
			IBAUtils cadiba = new IBAUtils(epmdoc);
			Debug.P(docOid);
			String part_type = cadiba.getIBAValue("Part_Type");
			if (StringUtils.isNotEmpty(part_type)) {
				part_type = part_type.replaceAll(" ", "").trim();
			}
			Debug.P(epmdoc.getContainer());
			pmcad.setPLMId(docOid);
			Map plmData = new HashMap();
			plmData.put("number", epmdoc.getNumber());
			plmData.put("AirSpringClassification", cadiba
					.getIBAValue("AirSpringClassification") == null ? ""
					: cadiba.getIBAValue("AirSpringClassification"));
			plmData.put(
					"Part_Type",
					cadiba.getIBAValue("Part_Type") == null ? "" : cadiba
							.getIBAValue("Part_Type"));
			plmData.put("plmmid", "wt.epm.EPMDocument:"
					+ epmdoc.getIterationInfo().getBranchId());
			pmcad.setPLMData(plmData);
			pmcad.setCommonName(epmdoc.getName());
			pmcad.setObjectNumber(epmdoc.getNumber());
			// pmcad.setStatus(epmdoc.getState().toString().toLowerCase());
			pmcad.setCreateBy(epmdoc.getCreatorName(),
					epmdoc.getCreatorFullName());
			Debug.P("version1--->" + epmdoc.getVersionIdentifier().getValue()
					+ "   version2----->"
					+ epmdoc.getIterationIdentifier().getValue());
			pmcad.setMajorVid(epmdoc.getVersionIdentifier().getValue());
			pmcad.setSecondVid(Integer.parseInt(epmdoc.getIterationIdentifier()
					.getValue()));
			pmcad.setPhase(cadiba.getIBAValue("PHASE") == null ? "" : cadiba
					.getIBAValue("PHASE"));
			pmcad.setModifiedBy(epmdoc.getCreatorName(),
					epmdoc.getModifierFullName());
			pmcad.setDrawingNumber(cadiba.getIBAValue("Material_NO") == null ? ""
					: cadiba.getIBAValue("Material_NO"));
			pmcad.setPartType0(part_type == null ? "" : part_type);
			pmcad.setValue("IsSync", true);
			pmcad.setValue("cadName", epmdoc.getCADName());
			WriteResult wresult = pmcad.doUpdate();
			String error = wresult.getError();
			if (StringUtils.isEmpty(error)) {
				cadiba.setIBAValue("CyncData", Utils.getDate());
				cadiba.setIBAValue("PMRequest", "update");
				cadiba.updateIBAPart(epmdoc);

				Debug.P("update PMCADDocument success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMPart(String pmoid, WTPart wtPart) {
		String partOid = "";
		PartPersistence partPersistence = null;
		String weight = "";
		Debug.P("更新Windchill中的半成品后至PM系统的数据库中");
		partOid = getObjectOid(wtPart);
		try {
			PMPart pmPart = null;
			partPersistence = (PartPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PartPersistence.class);
			pmPart = (PMPart) partPersistence.get(new ObjectId(pmoid));
			Debug.P("pmPart --->" + pmPart.getCommonName());
			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P("partiba----->" + partiba);
			Debug.P(partOid);
			pmPart.setCommonName(wtPart.getName());
			// pmPart.setStatus(wtPart.getState().toString().toLowerCase());
			pmPart.setMajorVid(wtPart.getVersionIdentifier().getValue());
			pmPart.setSecondVid(Integer.parseInt(wtPart
					.getIterationIdentifier().getValue()));
			pmPart.setPhase(partiba.getIBAValue("PHASE") == null ? "" : partiba
					.getIBAValue("PHASE"));
			pmPart.setModifiedBy(wtPart.getModifierName(),
					wtPart.getModifierFullName());
			pmPart.setSpec(partiba.getIBAValue("Specifications") == null ? ""
					: partiba.getIBAValue("Specifications"));
			weight = partiba.getIBAValue("Weight");
			if (StringUtils.isNotEmpty(weight))
				pmPart.setWeight(NumberFormat.getInstance().parse(weight));
			pmPart.setProductNumber(partiba.getIBAValue("Product_NO") == null ? ""
					: partiba.getIBAValue("Product_NO"));
			Map plmData = new HashMap();
			plmData.put("number", wtPart.getNumber());
			plmData.put("AirSpringClassification", partiba
					.getIBAValue("AirSpringClassification") == null ? ""
					: partiba.getIBAValue("AirSpringClassification"));
			plmData.put("plmmid", "wt.part.WTPart:"
					+ wtPart.getIterationInfo().getBranchId());
			pmPart.setPLMData(plmData);
			pmPart.setMaterial(partiba.getIBAValue("Material") == null ? ""
					: partiba.getIBAValue("Material"));
			pmPart.setOwner(wtPart.getCreatorName());
			pmPart.setValue("IsSync", true);
			WriteResult wresult = pmPart.doUpdate();
			String error = wresult.getError();
			if (StringUtils.isEmpty(error)) {
				partiba.setIBAValue("CyncData", Utils.getDate());
				partiba.setIBAValue("PMRequest", "update");
				partiba.updateIBAPart(wtPart);

				Debug.P("update PMPart success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMProductToPM(String pmoid, WTPart wtPart) {
		String partOid = "";
		ProductPersistence productPersistence = null;
		String weight = "";
		Debug.P("更新PM系统的成品-----------》" + wtPart.getNumber());
		try {
			partOid = getObjectOid(wtPart);
			PMProduct pmProduct = null;
			productPersistence = (ProductPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(ProductPersistence.class);
			try {
				pmProduct = (PMProduct) productPersistence.get(new ObjectId(
						pmoid));
			} catch (Exception e) {
				Debug.P(">>>Update PMProduct PMID:" + pmoid + "在PM中不存在!");
				return;
			}
			Debug.P(pmProduct.getSyncDate());
			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P("partiba----->" + partiba);
			Debug.P(partOid);
			Map plmData = new HashMap();
			plmData.put("number", wtPart.getNumber());
			plmData.put("plmmid", "wt.part.WTPart:"
					+ wtPart.getIterationInfo().getBranchId());
			pmProduct.setPLMData(plmData);
			pmProduct.setCommonName(wtPart.getName());
			pmProduct.setObjectNumber(wtPart.getNumber());
			// pmProduct.setStatus(wtPart.getState().toString().toLowerCase());
			pmProduct.setCreateBy(wtPart.getCreatorName(),
					wtPart.getCreatorFullName());
			pmProduct.setMajorVid(wtPart.getVersionIdentifier().getValue());
			pmProduct.setSecondVid(Integer.parseInt(wtPart
					.getIterationIdentifier().getValue()));
			pmProduct.setPhase(partiba.getIBAValue("PHASE") == null ? ""
					: partiba.getIBAValue("PHASE"));
			pmProduct
					.setSpec(partiba.getIBAValue("Specifications") == null ? ""
							: partiba.getIBAValue("Specifications"));
			weight = partiba.getIBAValue("Weight");
			if (StringUtils.isNotEmpty(weight))
				pmProduct.setWeight(NumberFormat.getInstance().parse(weight));
			pmProduct
					.setFormularNumber(partiba.getIBAValue("Formula_NO") == null ? ""
							: partiba.getIBAValue("Formula_NO"));
			pmProduct.setModifiedBy(wtPart.getModifierName(),
					wtPart.getModifierFullName());
			pmProduct.setOwner(wtPart.getCreatorName());
			pmProduct.setMaterial(partiba.getIBAValue("Material") == null ? ""
					: partiba.getIBAValue("Material"));
			pmProduct.setValue("IsSync", true);
			WriteResult wresult = pmProduct.doUpdate();
			String error = wresult.getError();
			if (StringUtils.isEmpty(error)) {
				partiba.setIBAValue("CyncData", Utils.getDate());
				partiba.setIBAValue("PMRequest", "update");
				partiba.updateIBAPart(wtPart);

				Debug.P("update pmproduct success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMaterialToPM(String pmoid, WTPart wtPart) {
		String partOid = "";
		MaterialPersistence materialPersistence = null;
		String weight = "";
		Debug.P("更新Windchill中的原材料---------------" + wtPart.getNumber());
		partOid = getObjectOid(wtPart);
		try {
			PMMaterial pmMaterial = null;
			materialPersistence = (MaterialPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(MaterialPersistence.class);
			pmMaterial = (PMMaterial) materialPersistence.get(new ObjectId(
					pmoid));
			Debug.P("pmmaterial--->" + pmMaterial.getCommonName());
			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(partOid);
			pmMaterial.setPLMId(partOid);
			Map plmData = new HashMap();
			plmData.put("number", wtPart.getNumber());
			plmData.put("plmmid", "wt.part.WTPart:"
					+ wtPart.getIterationInfo().getBranchId());
			pmMaterial.setPLMData(plmData);
			pmMaterial.setCommonName(wtPart.getName());
			pmMaterial.setObjectNumber(wtPart.getNumber());
			// pmMaterial.setStatus(wtPart.getState().toString().toLowerCase());
			pmMaterial.setCreateBy(wtPart.getCreatorName(),
					wtPart.getCreatorFullName());
			pmMaterial.setMajorVid(wtPart.getVersionIdentifier().getValue());
			pmMaterial.setSecondVid(Integer.parseInt(wtPart
					.getIterationIdentifier().getValue()));
			pmMaterial.setPhase(partiba.getIBAValue("PHASE") == null ? ""
					: partiba.getIBAValue("PHASE"));
			pmMaterial.setMaterial(partiba.getIBAValue("Material") == null ? ""
					: partiba.getIBAValue("Material"));
			pmMaterial.setModifiedBy(wtPart.getModifierName(),
					wtPart.getModifierFullName());
			pmMaterial
					.setSpec(partiba.getIBAValue("Specifications") == null ? ""
							: partiba.getIBAValue("Specifications"));
			weight = partiba.getIBAValue("Weight");
			if (StringUtils.isNotEmpty(weight))
				pmMaterial.setWeight(NumberFormat.getInstance().parse(weight));
			pmMaterial
					.setMaterialGroup(partiba.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
			pmMaterial.setOwner(wtPart.getCreatorName());
			pmMaterial.setValue("IsSync", true);
			WriteResult wresult = pmMaterial.doUpdate();
			String error = wresult.getError();
			if (StringUtils.isEmpty(error)) {
				partiba.setIBAValue("CyncData", Utils.getDate());
				partiba.setIBAValue("PMRequest", "update");
				partiba.updateIBAPart(wtPart);
				Debug.P("update PMMaterial success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updatePMPackageToPM(String pmoid, WTPart wtPart) {
		String partOid = "";
		PackagePersistence packagePersistence = null;
		String weight = "";
		Debug.P("更新Windchill中的包装材料---------------" + wtPart.getNumber());
		partOid = getObjectOid(wtPart);
		try {
			PMPackage pmPackage = null;

			packagePersistence = (PackagePersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PackagePersistence.class);
			pmPackage = (PMPackage) packagePersistence.get(new ObjectId(pmoid));
			Debug.P("pmPackage--->" + pmPackage.getCommonName());
			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(partOid);
			pmPackage.setPLMId(partOid);
			Map plmData = new HashMap();
			plmData.put("number", wtPart.getNumber());
			plmData.put("plmmid", "wt.part.WTPart:"
					+ wtPart.getIterationInfo().getBranchId());
			pmPackage.setPLMData(plmData);
			pmPackage.setCommonName(wtPart.getName());
			pmPackage.setObjectNumber(wtPart.getNumber());
			// pmPackage.setStatus(wtPart.getState().toString().toLowerCase());
			pmPackage.setCreateBy(wtPart.getCreatorName(),
					wtPart.getCreatorFullName());
			pmPackage.setMajorVid(wtPart.getVersionIdentifier().getValue());
			pmPackage.setSecondVid(Integer.parseInt(wtPart
					.getIterationIdentifier().getValue()));
			pmPackage.setPhase(partiba.getIBAValue("PHASE") == null ? ""
					: partiba.getIBAValue("PHASE"));
			pmPackage.setMaterial(partiba.getIBAValue("Material") == null ? ""
					: partiba.getIBAValue("Material"));
			pmPackage.setModifiedBy(wtPart.getModifierName(),
					wtPart.getModifierFullName());
			pmPackage
					.setSpec(partiba.getIBAValue("Specifications") == null ? ""
							: partiba.getIBAValue("Specifications"));
			weight = partiba.getIBAValue("Weight");
			if (StringUtils.isNotEmpty(weight))
				pmPackage.setWeight(NumberFormat.getInstance().parse(weight));
			pmPackage
					.setMaterialGroup(partiba.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
			pmPackage.setOwner(wtPart.getCreatorName());
			pmPackage.setValue("IsSync", true);
			WriteResult wresult = pmPackage.doUpdate();
			String error = wresult.getError();
			if (StringUtils.isEmpty(error)) {
				partiba.setIBAValue("CyncData", Utils.getDate());
				partiba.setIBAValue("PMRequest", "update");
				partiba.updateIBAPart(wtPart);

				Debug.P("update pmPackage success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void UpdateJigToolPartToPM(String pmoid, WTPart wtPart) {
		String partOid = "";
		String partFolderString = "";
		Folder partFolder = null;
		String pFolderId = "";
		JigToolsPersistence jigTollsPersistence = null;

		String weight = "";
		Debug.P("将Windchill中的备品备料插入PM系统的数据库中");
		partOid = getObjectOid(wtPart);
		try {
			PMJigTools pmJigTools = null;
			jigTollsPersistence = (JigToolsPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(JigToolsPersistence.class);
			pmJigTools = (PMJigTools) jigTollsPersistence.get(new ObjectId(
					pmoid));
			IBAUtils partiba = new IBAUtils(wtPart);
			Debug.P(partOid);
			partFolderString = wtPart.getFolderPath();
			Debug.P(partFolderString);
			partFolder = FolderHelper.service.getFolder(wtPart);

			Debug.P(partFolder);
			Debug.P(wtPart.getContainer());
			ReferenceFactory rf = new ReferenceFactory();
			pFolderId = rf.getReferenceString(partFolder);
			pFolderId = pFolderId.substring(pFolderId.indexOf(":") + 1,
					pFolderId.length());
			pmJigTools.setFolderIdByPLMId(pFolderId);
			pmJigTools.setPLMId(partOid);
			Map plmData = new HashMap();
			plmData.put("number", wtPart.getNumber());
			plmData.put("plmmid", "wt.part.WTPart:"
					+ wtPart.getIterationInfo().getBranchId());
			pmJigTools.setPLMData(plmData);
			pmJigTools.setCommonName(wtPart.getName());
			pmJigTools.setObjectNumber(wtPart.getNumber());
			// pmJigTools.setStatus(wtPart.getState().toString().toLowerCase());
			pmJigTools.setCreateBy(wtPart.getCreatorName(),
					wtPart.getCreatorFullName());
			pmJigTools.setMajorVid(wtPart.getVersionIdentifier().getValue());
			pmJigTools.setSecondVid(Integer.parseInt(wtPart
					.getIterationIdentifier().getValue()));
			pmJigTools.setPhase(partiba.getIBAValue("PHASE") == null ? ""
					: partiba.getIBAValue("PHASE"));

			pmJigTools
					.setSpec(partiba.getIBAValue("Specifications") == null ? ""
							: partiba.getIBAValue("Specifications"));
			weight = partiba.getIBAValue("Weight");
			if (StringUtils.isNotEmpty(weight))
				pmJigTools.setWeight(NumberFormat.getInstance().parse(weight));
			pmJigTools
					.setMaterialGroup(partiba.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
			pmJigTools.setOwner(wtPart.getCreatorName());
			pmJigTools.setMaterial(partiba.getIBAValue("Material") == null ? ""
					: partiba.getIBAValue("Material"));
			pmJigTools.setValue("IsSync", true);
			WriteResult wresult = pmJigTools.doUpdate();
			String error = wresult.getError();
			if (StringUtils.isEmpty(error)) {
				partiba.setIBAValue("CyncData", Utils.getDate());
				partiba.setIBAValue("PMRequest", "update");
				partiba.updateIBAPart(wtPart);

				Debug.P("update pmJigTools success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateSupplyToPM(String pmoid, WTPart wtPart) {
		String pFolderId = "";
		SupplymentPersistence supplymentPersistence = null;
		String weight = "";
		Debug.P("将Windchill中的客供件更新PM系统的数据库中---------------》"
				+ wtPart.getNumber());
		try {
			PMSupplyment pmSupplyment = null;
			supplymentPersistence = (SupplymentPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(SupplymentPersistence.class);
			pmSupplyment = (PMSupplyment) supplymentPersistence
					.get(new ObjectId(pmoid));
			IBAUtils partiba = new IBAUtils(wtPart);
			Map plmData = new HashMap();
			plmData.put("number", wtPart.getNumber());
			plmData.put("plmmid", "wt.part.WTPart:"
					+ wtPart.getIterationInfo().getBranchId());
			pmSupplyment.setPLMData(plmData);
			pmSupplyment.setCommonName(wtPart.getName());
			// pmSupplyment.setStatus(wtPart.getState().toString().toLowerCase());
			pmSupplyment.setCreateBy(wtPart.getCreatorName(),
					wtPart.getCreatorFullName());
			pmSupplyment.setMajorVid(wtPart.getVersionIdentifier().getValue());
			pmSupplyment.setSecondVid(Integer.parseInt(wtPart
					.getIterationIdentifier().getValue()));
			pmSupplyment.setPhase(partiba.getIBAValue("PHASE") == null ? ""
					: partiba.getIBAValue("PHASE"));
			pmSupplyment
					.setSpec(partiba.getIBAValue("Specifications") == null ? ""
							: partiba.getIBAValue("Specifications"));
			weight = partiba.getIBAValue("Weight");
			if (StringUtils.isNotEmpty(weight))
				pmSupplyment
						.setWeight(NumberFormat.getInstance().parse(weight));
			pmSupplyment
					.setCustomerName(partiba.getIBAValue("Client_Name") == null ? ""
							: partiba.getIBAValue("Client_Name"));
			pmSupplyment.setModifiedBy(wtPart.getModifierName(),
					wtPart.getModifierFullName());
			pmSupplyment
					.setMaterialGroup(partiba.getIBAValue("Material_Group") == null ? ""
							: partiba.getIBAValue("Material_Group"));
			pmSupplyment
					.setMaterial(partiba.getIBAValue("Material") == null ? ""
							: partiba.getIBAValue("Material"));
			pmSupplyment.setValue("IsSync", true);
			pmSupplyment.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmSupplyment.doUpdate();
			String error = wresult.getError();
			if (StringUtils.isEmpty(error)) {
				partiba.setIBAValue("CyncData", Utils.getDate());
				partiba.setIBAValue("PMRequest", "update");
				partiba.updateIBAPart(wtPart);

				Debug.P("update PMSupplyment success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deletePMPart(String pmoid, WTPart wtPart) {
		PartPersistence partPersistence = null;
		try {
			PMPart pmPart = null;
			partPersistence = (PartPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PartPersistence.class);
			boolean flag = false;
			ObjectId objecdId = null;
			try {
				objecdId = new ObjectId(pmoid);
				pmPart = (PMPart) partPersistence.get(objecdId);
				if (pmPart != null)
					flag = true;
			} catch (NullPointerException e) {
				flag = false;
			}
			if (flag) {
				pmPart = (PMPart) partPersistence.get(objecdId);
				Debug.P("将Windchill中的半成品从PM系统的数据库中删除------------->"
						+ pmPart.getCommonName());
				WriteResult wresult = pmPart.doRemove();
				String error = wresult.getError();
				if (StringUtils.isEmpty(error))
					Debug.P("delete PMPart success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deletePMProduct(String pmoid, WTPart wtPart) {
		ProductPersistence productPersistence = null;
		try {
			PMProduct productPart = null;
			productPersistence = (ProductPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(ProductPersistence.class);
			boolean flag = false;
			ObjectId objecdId = null;
			try {
				objecdId = new ObjectId(pmoid);
				productPart = (PMProduct) productPersistence.get(objecdId);
				if (productPart != null)
					flag = true;
			} catch (NullPointerException e) {
				flag = false;
			}
			if (flag) {
				productPart = (PMProduct) productPersistence.get(objecdId);
				Debug.P("将Windchill中的成品从PM系统的数据库中删除--->"
						+ productPart.getCommonName());
				WriteResult wresult = productPart.doRemove();
				String error = wresult.getError();
				if (StringUtils.isEmpty(error))
					Debug.P("delete PMProduct success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deletePMMaterial(String pmoid, WTPart wtPart) {
		MaterialPersistence materialPersistence = null;
		try {
			PMMaterial pmMaterial = null;
			materialPersistence = (MaterialPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(MaterialPersistence.class);
			boolean flag = false;
			ObjectId objecdId = null;
			try {
				objecdId = new ObjectId(pmoid);
				pmMaterial = (PMMaterial) materialPersistence.get(objecdId);
				if (pmMaterial != null)
					flag = true;
			} catch (NullPointerException e) {
				flag = false;
			}
			if (flag) {
				pmMaterial = (PMMaterial) materialPersistence.get(objecdId);
				Debug.P("将Windchill中的原材料从PM系统的数据库中删除-----》"
						+ pmMaterial.getCommonName());
				WriteResult wresult = pmMaterial.doRemove();
				String error = wresult.getError();
				if (StringUtils.isEmpty(error))
					Debug.P("delete PMMaterial success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deletePMPackage(String pmoid, WTPart wtPart) {
		PackagePersistence packagePersistence = null;
		try {
			PMPackage pmPackage = null;

			packagePersistence = (PackagePersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PackagePersistence.class);
			boolean flag = false;
			ObjectId objecdId = null;
			try {
				objecdId = new ObjectId(pmoid);
				pmPackage = (PMPackage) packagePersistence.get(objecdId);
				if (pmPackage != null)
					flag = true;
			} catch (NullPointerException e) {
				flag = false;
			}
			if (flag) {
				pmPackage = (PMPackage) packagePersistence.get(objecdId);
				Debug.P("将Windchill中的包装材料从PM系统的数据库中删除-----》"
						+ pmPackage.getCommonName());
				WriteResult wresult = pmPackage.doRemove();
				String error = wresult.getError();
				if (StringUtils.isEmpty(error))
					Debug.P("delete pmPackage success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deletePMCADDoc(String pmoid, EPMDocument epmdoc) {
		CADDocumentPersistence cadDocPersistence = null;
		try {
			PMCADDocument pmcad = null;

			cadDocPersistence = (CADDocumentPersistence) ModelServiceFactory
					.getInstance(codebasePath)
					.get(CADDocumentPersistence.class);
			boolean flag = false;
			ObjectId objecdId = null;
			try {
				objecdId = new ObjectId(pmoid);
				pmcad = (PMCADDocument) cadDocPersistence.get(objecdId);
				if (pmcad != null)
					flag = true;
			} catch (NullPointerException e) {
				flag = false;
			}
			if (flag) {
				Debug.P("将Windchill中的PMCADDocument从PM系统的数据库中删除-----》"
						+ pmcad.getCommonName());
				WriteResult wresult = pmcad.doRemove();
				String error = wresult.getError();
				if (StringUtils.isEmpty(error))
					Debug.P("delete PMCADDocument success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deletePMJigTools(String pmoid, WTPart wtPart) {
		JigToolsPersistence jigToolsPersistence = null;
		try {
			PMJigTools pmJigTools = null;

			jigToolsPersistence = (JigToolsPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(JigToolsPersistence.class);
			boolean flag = false;
			ObjectId objecdId = null;
			try {
				objecdId = new ObjectId(pmoid);
				pmJigTools = (PMJigTools) jigToolsPersistence.get(objecdId);
				if (pmJigTools != null)
					flag = true;
			} catch (NullPointerException e) {
				flag = false;
			}
			if (flag) {
				pmJigTools = (PMJigTools) jigToolsPersistence.get(objecdId);
				Debug.P("将Windchill中的备品备料从PM系统的数据库中删除-----》"
						+ pmJigTools.getCommonName());
				WriteResult wresult = pmJigTools.doRemove();
				String error = wresult.getError();
				if (StringUtils.isEmpty(error))
					Debug.P("delete pmJigTools success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteSupplyment(String pmoid, WTPart wtPart) {
		SupplymentPersistence supplymentPersistence = null;
		try {
			PMSupplyment pmSupplyment = null;
			supplymentPersistence = (SupplymentPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(SupplymentPersistence.class);
			boolean flag = false;
			ObjectId objecdId = null;
			try {
				objecdId = new ObjectId(pmoid);
				pmSupplyment = (PMSupplyment) supplymentPersistence
						.get(objecdId);
				if (pmSupplyment != null)
					flag = true;
			} catch (NullPointerException e) {
				flag = false;
			}
			if (flag) {
				pmSupplyment = (PMSupplyment) supplymentPersistence
						.get(objecdId);
				Debug.P("将Windchill中的客供件从PM系统的数据库中删除---》"
						+ pmSupplyment.getCommonName());
				WriteResult wresult = pmSupplyment.doRemove();
				String error = wresult.getError();
				if (StringUtils.isEmpty(error))
					Debug.P("delete Supplyment success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	/**
	 * 调用PM的sevlet进行插入和更新PM对象
	 * @param docInfo
	 * @return
	 * @throws Exception
	 */
	public static String InsterOrUpdatePMDoc(String docInfo)throws Exception{
		String urls = ModelServiceFactory.URL_DOCUMENTSERVICE + "?id=" + docInfo;
		Debug.P(urls);
		URL url = new URL(urls);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String result;
		System.out.println("InsterOrUpdatePMDoc----------start--- ");
		result = reader.readLine();
			Debug.P("result----->"+result);
		
		System.out.println(" InsterOrUpdatePMDoc--------ends ");
		reader.close();
		connection.disconnect();
		return result;
	}

	/**
	 * 调用PM的sevlet进行插入和更新PM对象
	 * 
	 * @param docInfo
	 * @return
	 * @throws Exception
	 */
	public static String InsterOrUpdatePMDoc(String docInfo) throws Exception {
		String urls = ModelServiceFactory.URL_DOCUMENTSERVICE + "?id="
				+ docInfo;
		Debug.P(urls);
		URL url = new URL(urls);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		String result;
		System.out.println("InsterOrUpdatePMDoc----------start--- ");
		result = reader.readLine();
		Debug.P("result----->" + result);

		System.out.println("InsterOrUpdatePMDoc--------ends ");
		reader.close();
		connection.disconnect();
		return result;
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

	/**
	 * 自动同步Windchill未同步的的数据到PM系统(临时补丁类)
	 * 
	 * @param startTime
	 *            开始时间
	 * @param endTime
	 *            结束时间
	 */
	public static void autoSynchPLM2PM(String startTime) throws Exception {
		Debug.P("---------->>>>autoSynchPLM2PM:startTime=" + startTime);
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class aclass[] = { String.class };
				Object aobj[] = { startTime };
				RemoteMethodServer.getDefault().invoke("autoSynchPLM2PM",
						WCToPMHelper.class.getName(), null, aclass, aobj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else { // 全量同步
					// 查询系统里所有EPM和Part PMID数据
			DocumentPersistence docpersistence = ModelServiceFactory
					.getInstance(codebasePath).get(DocumentPersistence.class);
			String sql = "select CONCAT(to_char(V1.CLASSNAMEKEYA4)||':', to_char(V1.IDA3A4)) as OUID,V1.VALUE2 as PMID  FROM STRINGVALUE V1,STRINGDEFINITION D1 WHERE ( V1.CLASSNAMEKEYA4='wt.part.WTPart' OR  V1.CLASSNAMEKEYA4='wt.epm.EPMDocument') AND D1.IDA2A2=V1.ida3a6  AND D1.name='PMId'";
			List<Hashtable<String, String>> result = UserDefQueryUtil
					.commonQuery(sql, new String[] {});
			// 过滤掉已经同步过
			Set<String> noSynList = new HashSet<String>();
			Debug.P("------>>>Get All EPM和Part Result Size:" + result.size());
			for (int i = 0; i < result.size(); i++) {
				Hashtable<String, String> data_rows = result.get(i);// 行数据
				String oid = null;
				boolean flag = true;// 默认存在
				for (Iterator<?> ite = data_rows.keySet().iterator(); ite
						.hasNext();) {
					String key = (String) ite.next();// 列名
					String value = data_rows.get(key);// OUID
					if (key.equalsIgnoreCase("OUID")) {
						oid = value;
					}
					if (key.equalsIgnoreCase("PMID")) {
						flag = docpersistence
								.getDocumentById(value == null ? "" : value);
					}
					if (!flag) {
						if (StringUtils.isNotEmpty(oid)) {
							noSynList.add(oid);
						}
					}
				}
			}

			Debug.P("--->>>>Windchill未同步到PM的数据量大小:" + noSynList.size());
			// 开始为为未同步的执行同步操作
			for (Iterator<?> ite = noSynList.iterator(); ite.hasNext();) {
				String objectId = (String) ite.next();
				if (objectId.startsWith("wt.epm.EPMDocument")) {// EPM文档对象
					EPMDocument epmdoc = (EPMDocument) GenericUtil
							.getPersistableByOid(objectId);
					if (epmdoc == null)
						continue;
					Debug.P("--->>>SynEPM:" + epmdoc.getNumber());
					CreateEPMDocToPM(epmdoc);// 向PM写EPM数据
				} else {// WTPart对象
					Persistable object = GenericUtil
							.getPersistableByOid(objectId);
					if (object != null && object instanceof WTPart) {
						Debug.P("------>>>Ready Create WTPART："
								+ object.getPersistInfo().getObjectIdentifier()
										.getStringValue());
						TypeIdentifier typeIde = TypeIdentifierUtility
								.getTypeIdentifier(object);
						String typeName = typeIde.getTypename();
						Debug.P("--->>>>Type:" + typeName);
						if (typeName.contains(Contants.SEMIFINISHEDPRODUCT)) {
							CreatePartToPM((WTPart) object);
						} else if (typeName.contains(Contants.PRODUCTPART)) {
							CreatePMProductToPM((WTPart) object);
						} else if (typeName.contains(Contants.MATERIAL)) {
							CreatePMaterialToPM((WTPart) object);
						} else if (typeName.contains(Contants.SUPPLYMENT)) {
							CreateSupplyToPM((WTPart) object);
						} else if (typeName.contains(Contants.PACKINGPART)) {
							CreatePMPackageToPM((WTPart) object);
						} else if (typeName.contains(Contants.TOOLPART)) {
							CreateJigToolPartToPM((WTPart) object);
						}
						Debug.P("------>>>>>Create Type(" + typeName
								+ ")2PM Success!!!");
					}
				}
			}
		}
	}

	/**
	 * 同步WC中的EPMDocument对象，如果已同步则更新
	 * 
	 * @throws Exception
	 */
	public static void SynchEPMDocument2PM(String startTime) throws Exception {
		Debug.P("---------->>>>SynchEPMDocument2PM:startTime=" + startTime);
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class aclass[] = { String.class };
				Object aobj[] = { startTime };
				RemoteMethodServer.getDefault().invoke("SynchEPMDocument2PM",
						WCToPMHelper.class.getName(), null, aclass, aobj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			List<EPMDocument> epmList = new ArrayList<EPMDocument>();
			epmList = EPMDocUtil.getAllEPMDocument();
			for (EPMDocument epmdoc : epmList) {
				CreateEPMDocToPM(epmdoc);
			}
			Debug.P("-----------------同步EPMDocument完毕！--------------");
		}
	}

	/**
	 * 同步WC中的WTPart对象，如果已同步则更新
	 * 
	 * @throws Exception
	 */
	public static void SynchWTPart2PM(String startTime) throws Exception {
		Debug.P("---------->>>>SynchWTPart2PM:startTime=" + startTime);
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class aclass[] = { String.class };
				Object aobj[] = { startTime };
				RemoteMethodServer.getDefault().invoke("SynchWTPart2PM",
						WCToPMHelper.class.getName(), null, aclass, aobj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			List<WTPart> partList = new ArrayList<WTPart>();
			String partType = "";
			partList = EPMDocUtil.getAllWTPart();
			int i = 0;
			for (WTPart wtPart : partList) {
				i++;
				partType = DocUtils.getType(wtPart);
				Debug.P(wtPart.getNumber() + "--------->" + partType
						+ "----------->" + i);
				if (partType.contains(Contants.SEMIFINISHEDPRODUCT)) {// 如果半是成品
					WCToPMHelper.CreatePartToPM(wtPart);
				} else if (partType.contains(Contants.PRODUCTPART)) { // 如果是成品
					WCToPMHelper.CreatePMProductToPM(wtPart);
				} else if (partType.contains(Contants.MATERIAL)) { // 如果是原材料
					WCToPMHelper.CreatePMaterialToPM(wtPart);
				} else if (partType.contains(Contants.SUPPLYMENT)) {// 如果是客供件
					WCToPMHelper.CreateSupplyToPM(wtPart);
				} else if (partType.contains(Contants.PACKINGPART)) {// 如果是包装材料
					WCToPMHelper.CreatePMPackageToPM(wtPart);
				} else if (partType.contains(Contants.TOOLPART)) {// 如果是备品备料
					WCToPMHelper.CreateJigToolPartToPM(wtPart);
				}
			}
		}
		Debug.P("-----------------同步WTPart完毕！--------------");
	}

	public static void SynchDocument2PM(String startTime)
			throws InstantiationException, IllegalAccessException, Exception {
		Debug.P("---------->>>>SynchDocument2PM:startTime=" + startTime);
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class aclass[] = { String.class };
				Object aobj[] = { startTime };
				RemoteMethodServer.getDefault().invoke("SynchDocument2PM",
						WCToPMHelper.class.getName(), null, aclass, aobj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			List<WTDocument> docList = new ArrayList<WTDocument>();
			docList = EPMDocUtil.getAllDocument();
			int i = 0;
			for (WTDocument document : docList) {
				i++;
				Debug.P("----------------index-----------》" + i);
				getPMDocument(document);
			}
			Debug.P("-----------------同步WTDocument完毕！--------------");
		}
	}

	/**
	 * 查询系统中所有的Document 以OID去PM系统查询，如果不存在，则删除WC端的Document,
	 * 如果存在，则修改WC端文档的状态，修改PM端文档的版本
	 * 
	 * @param document
	 * @throws Exception
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static void getPMDocument(WTDocument document) throws Exception,
			InstantiationException, IllegalAccessException {
		PMDocument pmdoc = null;
		DocumentPersistence docper = null;
		String stateName = "";
		String state = "";
		docper = ModelServiceFactory.getInstance(codebasePath).get(
				DocumentPersistence.class);
		String plmId = document.getPersistInfo().getObjectIdentifier()
				.getStringValue();
		try {
			pmdoc = docper.getByPLMId(plmId);
		} catch (NullPointerException e) {
			pmdoc = null;
		}
		state = document.getState().getState().getStringValue();
		Debug.P("文档-----》" + document.getNumber() + "---状态-----》" + state
				+ "---pmdoc----------->" + pmdoc != null);
		if (pmdoc == null) {
			Debug.P("删除文档-------------》" + document.getNumber());
			PersistenceHelper.manager.delete(document);
		} else {
			stateName = pmdoc.getStatus();
			Debug.P("更新WC文档-----》" + document.getNumber() + "更新PM文档-----》"
					+ pmdoc.getObjectNumber() + "----stateName------->"
					+ stateName);
			pmdoc.setMajorVid(document.getVersionIdentifier().getValue());
			pmdoc.setSecondVid(Integer.valueOf(document
					.getIterationIdentifier().getValue()));
			pmdoc.setValue("IsSync", plmId);
			WriteResult result = pmdoc.doUpdate();
			document = (WTDocument) GenericUtil.changeState(document,
					stateMap.get(stateName));
			PersistenceHelper.manager.refresh(document);
		}
	}

	public static void initPMDocument(String startTime) throws Exception {
		Debug.P("---------->>>>initPMDocument:startTime=" + startTime);
		if (!RemoteMethodServer.ServerFlag) {
			try {
				Class aclass[] = { String.class };
				Object aobj[] = { startTime };
				RemoteMethodServer.getDefault().invoke("initPMDocument",
						WCToPMHelper.class.getName(), null, aclass, aobj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			String lastOid = "";
			WTPart part = null;
			WTDocument document = null;
			EPMDocument epmdoc = null;
			PMDocument pmdoc = null;
			PMCADDocument pmcad = null;
			PMJigTools pmJigTools = null;
			PMPart pmPart = null;
			PMMaterial pmMaterial = null;
			PMPackage pmPackage = null;
			PMProduct pmProduct = null;
			BasicDocument bd = null;
			BasicPart bp = null;
			PMSupplyment pmSupplyment = null;

			CADDocumentPersistence cadDoc = (CADDocumentPersistence) ModelServiceFactory
					.getInstance(codebasePath)
					.get(CADDocumentPersistence.class);
			DocumentPersistence docper = (DocumentPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(DocumentPersistence.class);
			JigToolsPersistence jigTolls = (JigToolsPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(JigToolsPersistence.class);
			PartPersistence pmparts = (PartPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PartPersistence.class);
			MaterialPersistence material = (MaterialPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(MaterialPersistence.class);
			PackagePersistence packages = (PackagePersistence) ModelServiceFactory
					.getInstance(codebasePath).get(PackagePersistence.class);
			ProductPersistence pmproduct = (ProductPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(ProductPersistence.class);
			SupplymentPersistence supplyment = (SupplymentPersistence) ModelServiceFactory
					.getInstance(codebasePath).get(SupplymentPersistence.class);

			// 获取PM中所有的document对象包含EPMdoc,Document,Part
			Map<String, String> docMap = cadDoc.getInitDocumentList();
			Debug.P("docMap.size--------------------->" + docMap.size());
			String oid = "";
			String pmType = "";
			Iterator<?> iter = docMap.keySet().iterator();
			int i = 0;
			while (iter.hasNext()) {
				i++;
				oid = iter.next().toString();
				pmType = docMap.get(oid);
				Debug.P("oid----->" + oid + "----pmType---->" + pmType
						+ "------->" + i);
				if (pmType.equals("epmdocument")) {
					try {
						epmdoc = (EPMDocument) GenericUtil.REF_FACTORY
								.getReference(oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bd = cadDoc.getByPLMId(oid);
					if (epmdoc == null) {
						bd.setValue("IsSync", false);
						bd.doUpdate();
					} else {
						epmdoc = (EPMDocument) Utils.getWCObject(
								EPMDocument.class, epmdoc.getNumber());
						lastOid = getObjectOid(epmdoc);
						if (!oid.equals(lastOid)) {
							bd.setValue("IsSync", lastOid);
							bd.doUpdate();
						}
					}
				} else if (pmType.equals("wtdocument")) {
					try {
						document = (WTDocument) GenericUtil.REF_FACTORY
								.getReference(oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bd = docper.getByPLMId(oid);
					if (document == null) {
						bd.setValue("IsSync", false);
						bd.doUpdate();
					} else {
						document = (WTDocument) Utils.getWCObject(
								WTDocument.class, document.getNumber());
						lastOid = getObjectOid(document);
						if (!oid.equals(lastOid)) {
							bd.setValue("IsSync", lastOid);
							bd.doUpdate();
						}
					}
				} else if (pmType.equals("pmjigtools")) {
					try {
						part = (WTPart) GenericUtil.REF_FACTORY.getReference(
								oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bp = (BasicPart) jigTolls.getByPLMId(oid);
					if (part == null) {
						bp.setValue("IsSync", false);
						bp.doUpdate();
					} else {
						part = (WTPart) Utils.getWCObject(WTPart.class,
								part.getNumber());
						lastOid = getObjectOid(part);
						if (!oid.equals(lastOid)) {
							bp.setValue("IsSync", lastOid);
							bp.doUpdate();
						}
					}

				} else if (pmType.equals("pmmaterial")) {
					try {
						part = (WTPart) GenericUtil.REF_FACTORY.getReference(
								oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bp = material.getByPLMId(oid);
					if (part == null) {
						bp.setValue("IsSync", false);
						bp.doUpdate();
					} else {
						part = (WTPart) Utils.getWCObject(WTPart.class,
								part.getNumber());
						lastOid = getObjectOid(part);
						if (!oid.equals(lastOid)) {
							bp.setValue("IsSync", lastOid);
							bp.doUpdate();
						}
					}
				} else if (pmType.equals("pmpackage")) {
					try {
						part = (WTPart) GenericUtil.REF_FACTORY.getReference(
								oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bp = packages.getByPLMId(oid);
					if (part == null) {
						bp.setValue("IsSync", false);
						bp.doUpdate();
					} else {
						part = (WTPart) Utils.getWCObject(WTPart.class,
								part.getNumber());
						lastOid = getObjectOid(part);
						if (!oid.equals(lastOid)) {
							bp.setValue("IsSync", lastOid);
							bp.doUpdate();
						}
					}
				} else if (pmType.equals("pmpart")) {
					try {
						part = (WTPart) GenericUtil.REF_FACTORY.getReference(
								oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bp = pmparts.getByPLMId(oid);
					if (part == null) {
						bp.setValue("IsSync", false);
						bp.doUpdate();
					} else {
						part = (WTPart) Utils.getWCObject(WTPart.class,
								part.getNumber());
						lastOid = getObjectOid(part);
						if (!oid.equals(lastOid)) {
							bp.setValue("IsSync", lastOid);
							bp.doUpdate();
						}
					}
				} else if (pmType.equals("pmproduct")) {
					try {
						part = (WTPart) GenericUtil.REF_FACTORY.getReference(
								oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bp = pmproduct.getByPLMId(oid);
					if (part == null) {
						bp.setValue("IsSync", false);
						bp.doUpdate();
					} else {
						part = (WTPart) Utils.getWCObject(WTPart.class,
								part.getNumber());
						lastOid = getObjectOid(part);
						if (!oid.equals(lastOid)) {
							bp.setValue("IsSync", lastOid);
							bp.doUpdate();
						}
					}
				} else if (pmType.equals("pmsupplyment")) {
					try {
						part = (WTPart) GenericUtil.REF_FACTORY.getReference(
								oid).getObject();
					} catch (Exception e) {
						part = null;
					}
					bp = supplyment.getByPLMId(oid);
					if (part == null) {
						bp.setValue("IsSync", false);
						bp.doUpdate();
					} else {
						part = (WTPart) Utils.getWCObject(WTPart.class,
								part.getNumber());
						lastOid = getObjectOid(part);
						if (!oid.equals(lastOid)) {
							bp.setValue("IsSync", lastOid);
							bp.doUpdate();
						}
					}
				}
			}
		}
	}

	public static void getPMDocument(String oid, String pmType)
			throws Exception {
		if (StringUtils.isNotEmpty(pmType)) {
			if (pmType.equals("epmdocument")) {
				EPMDocument epmdoc = EPMUtil.getEPMDocumentByOid(oid);
				if (epmdoc != null) {

				} else {

				}
			} else if (pmType.equals("wtdocument")) {
				WTDocument document = (WTDocument) GenericUtil.REF_FACTORY
						.getReference(oid).getObject();
			} else if (pmType.equals("wtpart")) {
				WTPart part = (WTPart) GenericUtil.REF_FACTORY
						.getReference(oid).getObject();
			}
		}

	}

	private static Map<String, String> stateMap = new HashMap<String, String>();
	static {
		stateMap.put(ConstanUtil.INWORK, ConstanUtil.WC_INWORK);
		stateMap.put(ConstanUtil.APPROVE, ConstanUtil.WC_APPROVE);
		stateMap.put(ConstanUtil.RELEASED, ConstanUtil.WC_RELEASED);
		stateMap.put(ConstanUtil.DESPOSED, ConstanUtil.WC_DESPOSED);
	}

	/**
	 * WC端删除前事件中调用，在PM端的对象上设置预删除标记，再在WC端删除后事件中执行删除PM中的该对象
	 * 
	 * @param pmoid
	 * @param object
	 * @throws Exception
	 */
	public static void updatePMDocument(String pmoid) throws Exception {
		PMDocument pmdoc = null;
		DocumentPersistence docper = null;
		docper = ModelServiceFactory.getInstance(codebasePath).get(
				DocumentPersistence.class);
		if (StringUtils.isNotEmpty(pmoid)) {
			pmdoc = docper.getDocumentByPMId(pmoid);
			Debug.P(">>>>pmdoc: " + pmdoc);
			if (pmdoc != null) {
				pmdoc.setValue("deleted", true);
				pmdoc.doUpdate();
			}
		}
	}

	/**
	 * 删除PM端预删除标记为ture的对象
	 * 
	 * @throws Exception
	 * @throws InstantiationException
	 */
	public static void delatePMDoc(String type, Class class1)
			throws InstantiationException, Exception {
		List<String> result = new ArrayList<String>();
		PMDocument pmdoc = null;
		DocumentPersistence docper = null;
		docper = ModelServiceFactory.getInstance(codebasePath).get(
				DocumentPersistence.class);
		result = docper.getDeletedDocumentList(type);
		Debug.P("result.size ---->" + result.size() + "   class--->" + class1
				+ "  type----->" + type);

		for (String pmid : result) {
			Debug.P("需要删除的PM对象OID--------》" + pmid);
			if (StringUtils.isNotEmpty(pmid)) {
				Object object = PMWebserviceImpl.searchWCObject(class1, pmid,
						Contants.PMID);
				Debug.P(" object----->" + object);
				if (object == null) {
					pmdoc = docper.getDocumentByPMId(pmid);
					pmdoc.doRemove();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Debug.P("------>>>>Start Sysnch>>>>>>>>");
		SynchEPMDocument2PM(Utils.getDate());// 同步图纸
		// SynchWTPart2PM(Utils.getDate());//同步部件
		//
		// initPMDocument(Utils.getDate());
		// SynchDocument2PM(Utils.getDate());// 同步文档
		// PMDeliverable pmd = new PMDeliverable();
		// pmd.initDeli(codebasePath);
		Debug.P("------>>>>End Sysnch>>>>>>>>");
	}

}
