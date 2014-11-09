package ext.tmt.EPMdoc.listener;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressServiceEvent;
import ext.tmt.WC2PM.WCToPMHelper;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.WindchillUtil;

public class EPMDocHelper implements Serializable {
	private static final long serialVersionUID = 2304592616754675533L;

	/**
	 * 监听EPMDocument创建事件
	 * @param target
	 * @param eventType
	 * @throws Exception
	 */
	public static void listenerEPMDoc(EPMDocument epmdoc, String eventType)
			throws Exception {
		Debug.P("事件类型---->" + eventType);
		Debug.P("事件对象---->" +epmdoc.getNumber());
		boolean flag = true;
		IBAUtils iba = new IBAUtils(epmdoc);
		Debug.P("ibautils--->"+iba);
		String sync=iba.getIBAValue(Contants.CYNCDATA);
		String pmoids = iba.getIBAValue(Contants.PMID);
		Debug.P("sync--->"+sync);
		Debug.P("pmoids--->"+pmoids);
        if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			try {
				flag = SessionServerHelper.manager.setAccessEnforced(false);
				// 如果是新建修订版本或者新建视图版本
				if (WindchillUtil.isReviseVersion(epmdoc)){
					return;
				}
				WCToPMHelper.CreateEPMDocToPM(epmdoc);
			}catch(Exception e){
				e.printStackTrace();
			} finally {
				SessionServerHelper.manager.setAccessEnforced(flag);
			}
		}else  if (StringUtils.isNotEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
            Debug.P("POST_STORE-------------pmoid----------->"+pmoid);
            Object object =GenericUtil.getObjectByNumber(epmdoc.getNumber());
			if(object !=null){
				epmdoc=(EPMDocument)object;
			}
            if(WorkInProgressHelper.isCheckedOut(epmdoc)){
			  WCToPMHelper.updatePMCADDoc(pmoid, epmdoc);
            }
		}else  if (StringUtils.isNotEmpty(sync)&&eventType.equals(WorkInProgressServiceEvent.POST_CHECKIN)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
			Object object =GenericUtil.getObjectByNumber(epmdoc.getNumber());
			if(object !=null){
				epmdoc=(EPMDocument)object;
			}
            Debug.P("POST_CHECKIN-----------pmoid----------->"+pmoid);
            WCToPMHelper.updatePMCADDoc(pmoid, epmdoc);
		}else  if (eventType.equals(PersistenceManagerEvent.PRE_DELETE)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
			Object object =GenericUtil.getObjectByNumber(epmdoc.getNumber());
			if(object !=null){
				epmdoc=(EPMDocument)object;
			}
			   Debug.P("PRE_DELETE-----------------pmoid----------->"+pmoid);
			   WCToPMHelper.deletePMCADDoc(pmoid, epmdoc);
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
}
