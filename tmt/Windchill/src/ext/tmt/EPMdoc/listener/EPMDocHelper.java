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
import ext.tmt.WC2PM.WCToPMHelper;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
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
		Debug.P(epmdoc.getNumber());
		String part_type="";     //部件分类
		String semiClasifination=""; //空簧部件分类
		WTPart part=null;        //文档关联的部件
		WTContainer container=null;  //文档所在的容器
		String docFolder =null;     //文档所在的文件夹
		String pPartType="";         //文档对应部件的部件类型
        if (eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			boolean flag = true;
			try {
				flag = SessionServerHelper.manager.setAccessEnforced(false);
				// 如果是检出再检入
				if (WorkInProgressHelper.isCheckedOut(epmdoc)) {
					return;
				}
				// 如果是新建修订版本或者新建视图版本
				if (WindchillUtil.isReviseVersion(epmdoc)){
					return;
				}
			IBAUtils iba = new IBAUtils(epmdoc);	
			part_type=iba.getIBAValue(Contants.PART_TYPE);
			container = epmdoc.getContainer();
			docFolder = epmdoc.getFolderPath();
			Debug.P(part_type);
			if(StringUtils.isNotEmpty(part_type)){
				if(part_type.contains("半成品")){
					pPartType="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
				}else if(part_type.contains("成品")){
					pPartType="wt.part.WTPart|"+Contants.PRODUCTPART;
				}
//				WTPart newPart = PartUtil.newPart(epmdoc.getNumber(),epmdoc.getName(),pPartType, container, docFolder);
//			    if(newPart !=null) 
//				Debug.P("part create succeed！--》"+newPart.getNumber());
			}
				WCToPMHelper.CreateEPMDocToPM(epmdoc);
			}catch(Exception e){
				e.printStackTrace();
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
}
