package ext.tmt.part.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.StringUtil;
import ext.tmt.utils.WindchillUtil;


import wt.change2.WTChangeRequest2;
import wt.csm.navigation.ClassificationNode;
import wt.fc.Identified;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.fc.QueryResult;
import wt.iba.definition.StringDefinition;
import wt.iba.value.ReferenceValue;
import wt.iba.value.StringValue;
import wt.org.WTOrganization;
import wt.org.WTUser;
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
        if (eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			boolean flag = true;
			try {
				flag = SessionServerHelper.manager.setAccessEnforced(false);

				// 如果是检出再检入
				if (WorkInProgressHelper.isCheckedOut(wtPart)) {
					return;
				}
				// 如果是新建修订版本或者新建视图版本
				if (WindchillUtil.isReviseVersion(wtPart)){
					IBAUtils ibaUtil = new IBAUtils(wtPart);
					ibaUtil.setIBAValue("IStoERP", "");
					ibaUtil.updateIBAPart(wtPart);
					return;
				}
			   String productName="";	
			   String prefix="";
			   
			   // 如果创建的部件类型是“1-成品” 且“isEndItem”为true
				if(DocUtils.getType(wtPart).contains("com.plm.Product")&&wtPart.isEndItem()){
					productName=wtPart.getContainerName();
					Debug.P("productName--------->"+productName);
					prefix=productName.substring(0, 5);
					if(!prefix.toUpperCase().contains("TX")){
						throw new Exception("产品:"+productName+"  未添加  TX 前缀！");
					}
					Debug.P("产品前缀----》"+prefix);
					int i= 0;
					do {
						partNumber=prefix+StringUtil.int2String(i,4);
						if(PartUtil.getPartByNumber(partNumber)==null){
							newNumber=partNumber;
							break;
						}
						i++;
					} while (i < 100000);
					
					changePartNumber(wtPart,newNumber);
				}
					
			} finally {
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
}
