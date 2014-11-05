package ext.tmt.part.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.StringUtil;
import ext.tmt.utils.WindchillUtil;


import wt.change2.WTChangeRequest2;
import wt.csm.navigation.ClassificationNode;
import wt.epm.EPMDocument;
import wt.fc.Identified;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.iba.definition.StringDefinition;
import wt.iba.value.ReferenceValue;
import wt.iba.value.StringValue;
import wt.org.WTOrganization;
import wt.org.WTUser;
import wt.part.PartType;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.pds.StatementSpec;
import wt.pom.PersistenceException;
import wt.pom.Transaction;
import wt.query.ClassAttribute;
import wt.query.ConstantExpression;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SQLFunction;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.WorkInProgressHelper;

public class PartHelper implements Serializable {
	private static final long serialVersionUID = 2304592616754675533L;

	/**
	 * 监听WTPart事件
	 * @param target
	 * @param eventType
	 * @throws Exception
	 */
	public static void listenerWTPart(WTPart wtPart, String eventType)
			throws Exception {
		Debug.P("事件类型---->" + eventType);
		Debug.P(wtPart.getNumber());
		String newNumber="";
		String partNumber="";
		String partType="";
		String types="";
        if (eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			boolean flag = true;
			try {
				flag = SessionServerHelper.manager.setAccessEnforced(false);
				
				// 如果是检出再检入
				if (WorkInProgressHelper.isCheckedOut(wtPart)) {
					return;
				}
//				// 如果是新建修订版本或者新建视图版本
//				if (WindchillUtil.isReviseVersion(wtPart)){
//					IBAUtils ibaUtil = new IBAUtils(wtPart);
//					ibaUtil.setIBAValue("IStoERP", "");
//					ibaUtil.updateIBAPart(wtPart);
//					return;
//				}
			   EPMDocument epmdoc = null;
			   String productName="";	
			   String prefix="";
			   partType=DocUtils.getType(wtPart);
			   partNumber=wtPart.getNumber();
			   if(StringUtils.isNotEmpty(partType)){
				   partType=partType.replaceAll(" ", "").trim();
			   }
			   Debug.P(partNumber+"------------------->"+partType);
			   
			   //检入AutoCad图纸时，系统根据图纸明细栏自动创建部件，如果部件编码为“本图”则不创建
			    if(partNumber.equals("本图")){
			    Debug.P(partNumber+"------------------->"+partType);
			   // PersistenceHelper.manager.delete(wtPart);
			        wtPart =null;
			     //   throw new Exception("不创建编码为“本图”的部件");
			    	//return;
			    }
			    String epmPartType="";
			    if(partType.equals("wt.part.WTPart")){
			    	epmdoc=EPMDocUtil.getActiveEPMDocument(wtPart);
			    	Debug.P("1-->"+epmdoc);
			    	if(epmdoc==null){
			    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
			    	}
			    	Debug.P("2-->"+epmdoc);
			    	IBAUtils epmIba = new IBAUtils(epmdoc);
			    	epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
			    	if(StringUtils.isNotEmpty(epmPartType)){
			    		epmPartType=epmPartType.replaceAll(" ", "").trim();
			    	}
			    	Debug.P(epmPartType);
			    	if(StringUtils.isNotEmpty(epmPartType)){
						if(epmPartType.equals("半成品")){
							types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
						}else if(epmPartType.equals("成品")){
							wtPart.setEndItem(true);
							types="wt.part.WTPart|"+Contants.PRODUCTPART;
						}else{
							return;
						}
						Debug.P(types);
						if(types.contains("Product")){
							partType=DocUtils.getDfmDocumentType(types).getIntHid();
							Debug.P(partType);
							TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
							wtPart.setPartType(PartType.getPartTypeDefault());
							wtPart.setTypeDefinitionReference(typeDefinitionRef);
							if (!PersistenceHelper.isPersistent(wtPart)) {
								wtPart = (WTPart) PersistenceHelper.manager.save(wtPart);
								wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
							}else{
								PersistenceServerHelper.manager.update(wtPart);
							}
						}
					}
			    	partType=DocUtils.getType(wtPart);
			    	Debug.P(partNumber+"------------------->"+partType);
			    }else if(partType.contains(Contants.PRODUCTPART)){ //如果是成品
			    	wtPart.setEndItem(true);
			    	if (!PersistenceHelper.isPersistent(wtPart)) {
						wtPart = (WTPart) PersistenceHelper.manager.save(wtPart);
						wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
					}else{
						PersistenceServerHelper.manager.update(wtPart);
					}
			    	//					if(!wtPart.isEndItem()){
//						throw new Exception("您创建的是产品，请将“是否为成品”的值设置为“是”！");
//					}
				//成品编码=TX+三位分类码+四位流水码。其中分类码为成品所在产品库容器名称的前三个字符，自动根据成品所在产品库获取。
					productName=wtPart.getContainerName();
					//批量导入部件时如果导入的部件编码含有TX则不修改部件编码
					if(wtPart.getNumber().toUpperCase().contains("TX")){
						return;
					}
					Debug.P("productName--------->"+productName);
					prefix=productName.substring(0, prefix.indexOf("-"));
					if(!prefix.toUpperCase().contains("TX")){
						throw new Exception("产品:"+productName+"  未添加  TX 前缀！");
					}
					Debug.P("产品前缀----》"+prefix);
					int i= 0;
					do {
						if(prefix.toUpperCase().trim().equals("TXA6")||prefix.toUpperCase().trim().equals("TXA7")||prefix.toUpperCase().trim().equals("TXA8")){
							partNumber=prefix+StringUtil.int2String(i,5);
						}else{
							partNumber=prefix+StringUtil.int2String(i,4);
						}
						if(PartUtil.getPartByNumber(partNumber)==null){
							newNumber=partNumber;
							break;
						}
						i++;
					} while (i < 100000);
					changePartNumber(wtPart,newNumber);
				}
			    else //如果是半成品
				if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){
					if(wtPart.isEndItem()){
						throw new Exception("您创建的是半产品，请将“是否为成品”的值设置为“否”！");
					}
					String isKHpart="";//空簧部件分类
					IBAUtils iba = new IBAUtils(wtPart);
					isKHpart=iba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
					Debug.P("WTPart -->"+isKHpart);
					//如果部件上的 空簧部件分类 值为空，则从部件关联的EPMDocument上获取
					if(StringUtils.isEmpty(isKHpart)){
						IBAUtils epmIBA = new IBAUtils(epmdoc);
						isKHpart=epmIBA.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
						Debug.P("EPMDocument -->"+isKHpart);
					}
					if(StringUtils.isNotEmpty(isKHpart)){
						//prefix=productName.substring(0, 5);
						int i= 0;
						do {
							partNumber=isKHpart+StringUtil.int2String(i,4);
							if(PartUtil.getPartByNumber(partNumber)==null){
								newNumber=partNumber;
								break;
							}
							i++;
						} while (i < 100000);
						changePartNumber(wtPart,newNumber);
						
						//WCToPMHelper.CreatePartToPM(wtPart);
					}
				}
			}finally {
				SessionServerHelper.manager.setAccessEnforced(flag);
			}
		} 
	}

	

	/*
	 * 查询部件最新编码
	 */
	public static boolean findNewPartNum(String newPartNum) throws WTException {
		boolean result = false;
		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,
				"=", newPartNum);
		qs.appendSearchCondition(sc);
		qs.appendAnd();
		SearchCondition sc1 = VersionControlHelper.getSearchCondition(
				wt.part.WTPart.class, true);
		qs.appendSearchCondition(sc1);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements()) {
			result = true;
		}
		return result;
	}

	/**
	 * 根据编号规则,为成品改变编号
	 * 
	 * @param part
	 * @param number
	 * @throws WTException
	 */
	private static void changePartNumber(WTPart part, String number)
			throws WTException {
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
			WTPartHelper.service.changeWTPartMasterIdentity(
					(WTPartMaster) identified, name, number, org);
			tx.commit();
			tx = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (tx != null)
				tx.rollback();
			SessionHelper.manager.setPrincipal(currentuser
					.getAuthenticationName());
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}
	public static void main(String[] args) {
		String str="TXA6-半  成  品";
		System.out.println(str.replaceAll(" ", "").trim());
		System.out.println(str.substring(0,str.indexOf("-")));
	}
}
