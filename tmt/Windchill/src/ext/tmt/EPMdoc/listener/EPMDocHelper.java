package ext.tmt.EPMdoc.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlServiceEvent;
import wt.vc.wip.WorkInProgressHelper;
import ext.tmt.WC2PM.WCToPMHelper;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.Utils;

public class EPMDocHelper implements Serializable {
	private static final long serialVersionUID = 2304592616754675533L;

	/**
	 * 监听EPMDocument的创建，更新，同步到PM系统
	 * 
	 * @param target
	 * @param eventType
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static void listenerEPMDoc(EPMDocument epmdoc, String eventType)
			throws Exception {
		Debug.P("事件类型---->" + eventType);
		Debug.P("事件对象---->" + epmdoc);
		// if(eventType.equals(PersistenceManagerEvent.POST_DELETE)){
		// Debug.P("Post_DELETE-----------------epmdoc----------->" +epmdoc);
		// if(epmdoc==null){
		// WCToPMHelper.delatePMDoc("epmdocument", EPMDocument.class);
		// }
		// }else{
		// Debug.P("事件对象---->" + epmdoc.getNumber());
		IBAUtils iba = null;
		if (epmdoc == null) {
			return;
		}
		iba = new IBAUtils(epmdoc);
		Debug.P("ibautils--->" + iba);
		String sync = iba.getIBAValue("CyncData");
		String pmoids = iba.getIBAValue("PMId");
		Debug.P("sync--->" + sync);
		Debug.P("pmoids--->" + pmoids);
		Folder docFolder = FolderHelper.service.getFolder(epmdoc);
		Debug.P("epmdocFolder---->" + docFolder);
		boolean flag = true;
		flag = SessionServerHelper.manager.setAccessEnforced(false);
		label784: try {
			if ((StringUtils.isEmpty(sync))
					&& (eventType.equals("POST_CHECKIN"))) {
				if (docFolder.getFolderPath().toUpperCase().trim()
						.endsWith("/DEFAULT")) {
					throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
				}
				if (!docFolder.getFolderPath().contains("工作区")) {
					WCToPMHelper.CreateEPMDoc(epmdoc);
					break label784;
				}
			} else if ((StringUtils.isEmpty(sync))
					&& (eventType.equals("UPDATE"))) {
				if (docFolder.getFolderPath().toUpperCase().trim()
						.endsWith("/DEFAULT")) {
					throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
				}
				if (!docFolder.getFolderPath().contains("工作区")) {
					WCToPMHelper.CreateEPMDoc(epmdoc);
					break label784;
				}
			} else if ((StringUtils.isNotEmpty(sync))
					&& ((eventType.equals("POST_STORE")) || (eventType
							.equals("POST_MODIFY")))) {
				if (docFolder.getFolderPath().toUpperCase().trim()
						.endsWith("/DEFAULT")) {
					throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
				}
				String pmoid = iba.getIBAValue("PMId");
				Debug.P("POST_STORE-------------pmoid----------->" + pmoid
						+ "    version1s---->"
						+ epmdoc.getVersionIdentifier().getValue()
						+ "    version1---->"
						+ epmdoc.getIterationIdentifier().getValue());
				epmdoc = EPMUtil.getEPMDocument(epmdoc.getNumber(), null);
				Debug.P("POST_STORE-------------pmoid----------->" + pmoid
						+ "    version1s---->"
						+ epmdoc.getVersionIdentifier().getValue()
						+ "    version1---->"
						+ epmdoc.getIterationIdentifier().getValue());
				WCToPMHelper.updateEPMDoc(pmoid, epmdoc);
			} else if ((StringUtils.isNotEmpty(sync))
					&& (eventType.equals("POST_CHECKIN"))) {
				if (docFolder.getFolderPath().toUpperCase().trim()
						.endsWith("/DEFAULT")) {
					throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
				}
				String pmoid = iba.getIBAValue("PMId");
				Debug.P("POST_STORE-------------pmoid----------->" + pmoid
						+ "    version1s---->"
						+ epmdoc.getVersionIdentifier().getValue()
						+ "    version1---->"
						+ epmdoc.getIterationIdentifier().getValue());
				epmdoc = EPMUtil.getEPMDocument(epmdoc.getNumber(), null);
				Debug.P("POST_STORE-------------pmoid----------->" + pmoid
						+ "    version1s---->"
						+ epmdoc.getVersionIdentifier().getValue()
						+ "    version1---->"
						+ epmdoc.getIterationIdentifier().getValue());

				WCToPMHelper.updateEPMDoc(pmoid, epmdoc);
			} else if ((eventType.equals("POST_DELETE")) && (epmdoc != null)) {
				String pmoid = iba.getIBAValue("PMId");
				if (StringUtils.isNotEmpty(pmoid)) {
					Debug.P("删除部件 " + epmdoc.getNumber() + " 的最新小版本，重新以 -》"
							+ pmoid + " 创建-------------");
					WCToPMHelper.CreateEPMDoc(epmdoc);
				}
			}
		} catch (Exception e) {
			throw new Exception("图纸创建/同步出错，请联系管理员" + e.getMessage());
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}

	public static void listenerEPMDoc1(EPMDocument epmdoc, String eventType)
			throws Exception {
		Debug.P("事件类型---->" + eventType);
		Debug.P("事件对象---->" + epmdoc);
		Debug.P("事件对象编码---->" + epmdoc.getNumber());
		Folder docFolder = FolderHelper.service.getFolder(epmdoc);
		Debug.P("事件对象文件夹------->" + docFolder.getFolderPath());
		if (eventType.equals("POST_CHECKIN")) {
			IBAUtils iba = new IBAUtils(epmdoc);
			Debug.P("ibautils--->" + iba);
			String sync = iba.getIBAValue("CyncData");
			String pmoids = iba.getIBAValue("PMId");
			Debug.P("sync--->" + sync);
			Debug.P("pmoids--->" + pmoids);
		} else if (eventType.equals("UPDATE")) {
			IBAUtils iba = new IBAUtils(epmdoc);
			Debug.P("ibautils--->" + iba);
			String sync = iba.getIBAValue("CyncData");
			String pmoids = iba.getIBAValue("PMId");
			Debug.P("sync--->" + sync);
			Debug.P("pmoids--->" + pmoids);
		} else if (eventType.equals("POST_MODIFY")) {
			IBAUtils iba = new IBAUtils(epmdoc);
			Debug.P("ibautils--->" + iba);
			String sync = iba.getIBAValue("CyncData");
			String pmoids = iba.getIBAValue("PMId");
			Debug.P("sync--->" + sync);
			Debug.P("pmoids--->" + pmoids);
		} else if (eventType.equals("POST_STORE")) {
			IBAUtils iba = new IBAUtils(epmdoc);
			Debug.P("ibautils--->" + iba);
			String sync = iba.getIBAValue("CyncData");
			String pmoids = iba.getIBAValue("PMId");
			Debug.P("sync--->" + sync);
			Debug.P("pmoids--->" + pmoids);
		} else if (eventType.equals("PRE_DELETE")) {
			IBAUtils iba = new IBAUtils(epmdoc);
			Debug.P("ibautils--->" + iba);
			String sync = iba.getIBAValue("CyncData");
			String pmoids = iba.getIBAValue("PMId");
			Debug.P("sync--->" + sync);
			Debug.P("pmoids--->" + pmoids);
		} else if (eventType.equals("NEW_ITERATION")) {
			IBAUtils iba = new IBAUtils(epmdoc);
			Debug.P("ibautils--->" + iba);
			String sync = iba.getIBAValue("CyncData");
			String pmoids = iba.getIBAValue("PMId");
			Debug.P("sync--->" + sync);
			Debug.P("pmoids--->" + pmoids);
		}
	}

	public static boolean findNewPartNum(String newPartNum) throws WTException {
		boolean result = false;
		QuerySpec qs = new QuerySpec(WTPart.class);
		SearchCondition sc = new SearchCondition(WTPart.class, "master>number",
				"=", newPartNum);
		qs.appendSearchCondition(sc);
		qs.appendAnd();
		SearchCondition sc1 = VersionControlHelper.getSearchCondition(
				WTPart.class, true);
		qs.appendSearchCondition(sc1);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements()) {
			result = true;
		}
		return result;
	}
}
