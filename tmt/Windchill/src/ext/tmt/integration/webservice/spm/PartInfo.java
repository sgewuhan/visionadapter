package ext.tmt.integration.webservice.spm;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import wt.fc.Identified;
import wt.fc.PersistenceHelper;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.org.WTOrganization;
import wt.org.WTPrincipalReference;
import wt.part.QuantityUnit;
import wt.part.Source;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.pom.Transaction;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
import wt.vc.wip.WorkInProgressHelper;

import com.mongodb.BasicDBObject;

import ext.tmt.part.PartUtils;
import ext.tmt.utils.Debug;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;

public class PartInfo {

	public static final String F_CSR_WULIAOLEIXING = "CSR_WULIAOLEIXING";
	public static final String F_WLXIAOLEI = "WLXIAOLEI";
	public static final String OPERATION_CREATE = "CREATE";
	public static final String OPERATION_UPDATE = "UPDATE";
	public static final String OPERATION_DELETE = "DELETE";
	public static final String F_CSR_CLASSFICATION = "CSR_CLASSFICATION";
	public static final String F_NUMBER = "NUMBER";
	public static final String F_CSR_ZIYUANLEIXING_ZZZY = "CSR_ZIYUANLEIXING_ZZZY";
	public static final String F_CSR_XINGHAOGUIGE = "CSR_XINGHAOGUIGE";
	public static final String F_CSR_SUOSHUGONGCHANG = "CSR_SUOSHUGONGCHANG";
	public static final String F_CREATOR = "CREATOR";
	public static final String F_CSR_ZHONGLIANG = "CSR_ZHONGLIANG";
	public static final String F_UNIT = "UNIT";
	public static final String F_NAME = "NAME";
	public static final String F_OPERATION = "OPERATION";
	public static final String F_BPS_WORKFLOW_ID = "BPS_WORKFLOW_ID";
	public static final String F_DELETE_FACTORY = "DELETE_FACTORY";
	private BasicDBObject _data;
	private WTPart part = null;

	public PartInfo() {
		_data = new BasicDBObject();
	}

	public void setBpsWorkflowId(String value) {
		_data.put(F_BPS_WORKFLOW_ID, value);
	}

	public String getBpsWorkflowId() {
		return (String) _data.get(F_OPERATION);
	}

	public void setOperation(String value) {
		_data.put(F_OPERATION, value);
	}

	public String getOperation() {
		return (String) _data.get(F_OPERATION);
	}

	public void setName(String value) {
		_data.put(F_NAME, value);
	}

	public String getName() {
		return (String) _data.get(F_NAME);
	}

	public void setUnit(String value) {
		_data.put(F_UNIT, value);
	}

	public String getUnit() {
		return (String) _data.get(F_UNIT);
	}

	public void setWeight(String value) {
		_data.put(F_CSR_ZHONGLIANG, value);
	}

	public String getWeight() {
		return (String) _data.get(F_CSR_ZHONGLIANG);
	}

	public void setCreator(String value) {
		_data.put(F_CREATOR, value);
	}

	public String getCreator() {
		return (String) _data.get(F_CREATOR);
	}

	public void setFactory(String value) {
		_data.put(F_CSR_SUOSHUGONGCHANG, value);
	}

	public String getFactory() {
		return (String) _data.get(F_CSR_SUOSHUGONGCHANG);
	}

	public void setModel(String value) {
		_data.put(F_CSR_XINGHAOGUIGE, value);
	}

	public String getModel() {
		return (String) _data.get(F_CSR_XINGHAOGUIGE);
	}

	public void setType(String value) {
		_data.put(F_CSR_ZIYUANLEIXING_ZZZY, value);
	}

	public String getType() {
		return (String) _data.get(F_CSR_ZIYUANLEIXING_ZZZY);
	}

	public void setNumber(String value) {
		_data.put(F_NUMBER, value);
	}

	public String getNumber() {
		return (String) _data.get(F_NUMBER);
	}

	public void setClassType(String value) {
		_data.put(F_CSR_CLASSFICATION, value);
	}

	public String getClassType() {
		return (String) _data.get(F_CSR_CLASSFICATION);
	}

	public void addDeleteFactory(String value) {
		List<String> deleteFactory = getDeleteFactory();
		deleteFactory.add(value);
		setDeleteFactory(deleteFactory);
	}

	public void setDeleteFactory(List<String> value) {
		_data.put("DELETE_FACTORY", value);
	}

	@SuppressWarnings("unchecked")
	public List<String> getDeleteFactory() {
		return (List<String>) _data.get("DELETE_FACTORY");
	}

	public void setValue(String name, String value) {
		_data.put(name, value);
	}

	public String getValue(String name) {
		return (String) _data.get(name);
	}

	public boolean canCreate() throws Exception {
		String name = getName();
		if (StringUtils.isEmpty(name)) {
			throw new Exception("在PLM系统中创建部件时名称不能为空，请检查。");
		}
		String unit = getUnit();
		if (StringUtils.isEmpty(unit)) {
			throw new Exception("在PLM系统中创建部件时单位不能为空，请检查。");
		}
		String weight = getWeight();
		if (StringUtils.isEmpty(weight)) {
			throw new Exception("在PLM系统中创建部件时重量不能为空，请检查。");
		}
		String model = getModel();
		if (StringUtils.isEmpty(model)) {
			throw new Exception("在PLM系统中创建部件时型号规格不能为空，请检查。");
		}
		String factory = getFactory();
		if (StringUtils.isEmpty(factory)) {
			throw new Exception("在PLM系统中创建部件时所属工厂不能为空，请检查。");
		}
		return true;
	}

	public void checkAndSetBpsWorkflowId(String value) throws Exception {
		if (StringUtils.isEmpty(value)) {
			throw new Exception("传入BPS流程编号（BPS_WORKFLOW_ID）为空，请检查。");
		}
		setBpsWorkflowId(value);
	}

	public void checkAndSetOperation(String value) throws Exception {
		if (StringUtils.isEmpty(value)) {
			throw new Exception("传入处理类型（OPERATION）为空，请检查。");
		}
		setOperation(value);
	}

	public void checkAndSetNumber(String value) throws Exception {
		if (StringUtils.isEmpty(value)) {
			throw new Exception("传入处理类型为" + getOperation()
					+ "，但未查询到编号信息（NUMBER），请检查。");
		}
		setNumber(value);
	}

	public boolean checkTMTFactory() throws Exception {
		return CsrSpmUtil.matchFactory(getFactory());

	}

	public WTPart getWTPart() throws Exception {
		WTPart part = PartUtils.getPartByNumber(getNumber());
		if (part != null) {
			setOperation(OPERATION_UPDATE);
			this.part = part;
		}
		return this.part;
	}

	/**
	 * 创建Windchil部件信息
	 * 
	 * @param hashmap
	 *            基础属性集合
	 * @param ibaMap
	 *            软属性集合
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public WTPart createNewWTPart() throws Exception {
		canCreate();

		SessionHelper.manager.setAdministrator();

		Map<String, String> configMap = CsrSpmUtil.getWCMappingFieldBySPM();

		WTPart wtpart = WTPart.newWTPart(getNumber(), getName());
		Transaction tx = null;
		// 创建部件
		try {
			tx = new Transaction();
			tx.start();
			// 获取集合中的基础属性参数
			String partType = getType();
			Debug.P("---->>Part Type:" + partType);
			if (StringUtils.isNotEmpty(partType)
					&& !partType.contains(SPMConsts.ROOT)) {
				partType = SPMConsts.ROOT + getType();
			}

			String unit = getUnit();// 软属性

			// 初始化部件实例
			TypeDefinitionReference tdr = TypedUtilityServiceHelper.service
					.getTypeDefinitionReference(partType);
			if (tdr != null) {// 设置部件类型
				wtpart.setTypeDefinitionReference(tdr);
			}
			// 根据物料类型获得文件夹对象
			Folder folder = CsrSpmUtil.getFolderByType(partType,
					SPMConsts.SPMCONTAINER_NAME, getNumber(), configMap);
			if (folder != null) {
				Debug.P("----->>>Folder  ID:"
						+ folder.getPersistInfo().getObjectIdentifier()
								.getStringValue() + "  ;FolderPath:"
						+ folder.getFolderPath());
				FolderHelper.assignLocation(wtpart, folder);
			}
			// 视图
			View view = ViewHelper.service.getView(SPMConsts.DESIGN_VIEW);
			ViewReference viewReference = ViewReference.newViewReference(view);
			if (viewReference != null) {
				wtpart.setView(viewReference);
			}
			// 来源
			Source resource = Source.toSource("buy");
			if (resource != null) {
				wtpart.setSource(resource);
			}
			// 设置单位，如果没有就用ea
			QuantityUnit qu = null;
			try {
				Debug.P("----->>>unit  ID:" + unit + ";");
				if (StringUtils.isEmpty(unit)) {
					unit = "ea";
				}
				if (unit.equals("EA")) {
					unit = unit.toLowerCase();
				}
				if (unit.equals("AS_NEEDED")) {
					unit = unit.toLowerCase();
				}
				if (unit.equals("KG")) {
					unit = unit.toLowerCase();
				}
				if (unit.equals("M")) {
					unit = unit.toLowerCase();
				}
				if (unit.equals("L")) {
					unit = unit.toLowerCase();
				}
				if (unit.equals("SQ_M")) {
					unit = unit.toLowerCase();
				}
				if (unit.equals("CU_M")) {
					unit = unit.toLowerCase();
				}
				qu = QuantityUnit.toQuantityUnit(unit);
			} catch (WTInvalidParameterException e) {
				throw new WTException(e.getMessage());
			}
			if (qu != null) {
				wtpart.setDefaultUnit(qu);
			}

			Map<String, Object> ibaMap = getIBAMap(configMap);

			// 设置IBA属性
			LWCUtil.setValueBeforeStore(wtpart, ibaMap);
			tx.commit();
			tx = null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			Debug.P("CsrSpmUtil.createNewPart" + e.getMessage());
			throw new WTException(e.getMessage());
		} finally {
			if (tx != null)
				tx.rollback();
		}
		return wtpart;
	}

	private Map<String, Object> getIBAMap(Map<String, String> configMap) {
		Map<String, Object> ibaMap = new HashMap<String, Object>();
		Set<String> keySet = _data.keySet();
		for (String key : keySet) {
			String ibaName = configMap.get(key);
			if (!key.equals(F_CSR_WULIAOLEIXING)
					&& StringUtils.isNotEmpty(ibaName)) {
				String value = getValue(key);
				ibaMap.put(ibaName.trim(), value == null ? "" : value.trim());
			}
		}
		ibaMap.put(configMap.get(SPMConsts.MATERIAL_PATH),
				getValue(F_WLXIAOLEI));
		return ibaMap;
	}

	public void doSaveWTPart() throws Exception {
		getWTPart();

		if (getOperation().equals(OPERATION_CREATE)) {
			doInsertWTPart();
		} else if (getOperation().equals(OPERATION_UPDATE)) {
			doUpdateWTPart();
		}

	}

	public void doInsertWTPart() throws Exception {
		SessionHelper.manager.setAdministrator();
		if (part == null) {
			createNewWTPart();
		}

		part = (WTPart) PersistenceHelper.manager.save(part);

		Debug.P("------>>>Create WTPart:"
				+ part.getPersistInfo().getObjectIdentifier().getStringValue()
				+ " Part FolderPath:(" + part.getFolderPath() + ")Success!!!");

		part = (WTPart) PersistenceHelper.manager.refresh(part);

		GenericUtil.changeState((LifeCycleManaged) part, SPMConsts.RELEASED);
	}

	public void doUpdateWTPart() throws Exception {
		String number = getNumber();
		boolean isTMTFactory = checkTMTFactory();
		if (part == null) {
			if (isTMTFactory) {
				doInsertWTPart();
				return;
			}
			throw new WTException("在PLM系统中未查询到编号为" + number
					+ "的部件，无法执行更新处理，请检查。");
		}

		if (WorkInProgressHelper.isCheckedOut(part)) {
			WTPrincipalReference locker = part.getLocker();
			WTPrincipalReference principal = SessionHelper.manager
					.getPrincipalReference();
			if (!locker.equals(principal)) {
				throw new WTException("在PLM系统中部件[编号:" + number
						+ "]已经被其他人检出，无法执行更新处理，请检查。");
			}
		}

		SessionHelper.manager.setAdministrator();

		part = (WTPart) GenericUtil.checkout(part);

		String name = getName();
		if (StringUtils.isNotEmpty(name)) {
			Identified identified = (Identified) part.getMaster();
			WTOrganization org = part.getOrganization();
			WTPartHelper.service.changeWTPartMasterIdentity(
					(WTPartMaster) identified, name, number, org);
		}

		String unit = getUnit();
		if (StringUtils.isNotEmpty(unit)) {
			QuantityUnit quantityUnit = QuantityUnit.toQuantityUnit(unit
					.toLowerCase());
			part.setDefaultUnit(quantityUnit);
		}

		Map<String, String> configMap = CsrSpmUtil.getWCMappingFieldBySPM();
		Map<String, Object> ibaMap = getIBAMap(configMap);
		LWCUtil.setValue(part, ibaMap);

		if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(part,
				wt.session.SessionHelper.manager.getPrincipal())) {
			part = (WTPart) WorkInProgressHelper.service.checkin(part,
					"update WTPart IBA");
		}

		if (!isTMTFactory) {
			doRemoveWTPart();
		}
	}

	public void doRemoveWTPart() throws Exception {
		if (part == null) {
			throw new WTException("在PLM系统中未查询到编号为" + getNumber()
					+ "的部件，无法执行工厂删除处理，请检查。");
		}
		SessionHelper.manager.setAdministrator();
		String factory = null;
		List<String> deleteFactorys = getDeleteFactory();
		if (deleteFactorys != null && deleteFactorys.size() > 0) {
			Map<String, String> configMap = CsrSpmUtil.getWCMappingFieldBySPM();
			Map<String, Object> ibaMap = new HashMap<String, Object>();

			IBAUtils partIba = new IBAUtils(part);
			factory = partIba.getIBAValue(SPMConsts.FACTORY);

			for (String deleteFactory : deleteFactorys) {
				factory = factory.replace(deleteFactory, "");
				factory = factory.replace(",,", ",");
			}

			if (",".equals(factory)) {
				factory = "";
			}
			part = (WTPart) GenericUtil.checkout(part);
			ibaMap.put(configMap.get(F_CSR_SUOSHUGONGCHANG), factory);
			LWCUtil.setValue(part, ibaMap);

			if (wt.vc.wip.WorkInProgressHelper.isCheckedOut(part,
					wt.session.SessionHelper.manager.getPrincipal())) {
				part = (WTPart) WorkInProgressHelper.service.checkin(part,
						"update WTPart IBA");
			}

		} else {
			part = (WTPart) PersistenceHelper.manager.refresh(part);

			IBAUtils partIba = new IBAUtils(part);
			factory = partIba.getIBAValue(SPMConsts.FACTORY);// 系统Factory软属性值
		}
		if (factory == null || "".equals(factory)
				|| !CsrSpmUtil.matchFactory(factory)) {// 如果不存在新材的工厂则修改状态为 已作废
			GenericUtil
					.changeState((LifeCycleManaged) part, SPMConsts.DESPOSED);
		}
	}
}
