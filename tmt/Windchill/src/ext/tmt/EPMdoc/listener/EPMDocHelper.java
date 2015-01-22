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
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.Utils;

public class EPMDocHelper implements Serializable {
  private static final long serialVersionUID = 2304592616754675533L;

  /**
	 * 监听EPMDocument创建事件
	 * @param target
	 * @param eventType
	 * @throws Exception
	 */
  @SuppressWarnings("deprecation")
public static void listenerEPMDoc(EPMDocument epmdoc, String eventType)
    throws Exception
  {
    Debug.P("事件类型---->" + eventType);
    Debug.P("事件对象---->" + epmdoc);
//    if(eventType.equals(PersistenceManagerEvent.POST_DELETE)){
//    	Debug.P("Post_DELETE-----------------epmdoc----------->" +epmdoc);
//    	if(epmdoc==null){
//    		WCToPMHelper.delatePMDoc("epmdocument", EPMDocument.class);
//    	}
//    }else{
//    Debug.P("事件对象---->" + epmdoc.getNumber());
    IBAUtils iba =null;
    if(epmdoc!=null)
      iba=new IBAUtils(epmdoc);
    Debug.P("ibautils--->" + iba);
    String sync = iba.getIBAValue("CyncData");
    String pmoids = iba.getIBAValue("PMId");
    Debug.P("sync--->" + sync);
    Debug.P("pmoids--->" + pmoids);
    Folder docFolder = FolderHelper.service.getFolder(epmdoc);
    Debug.P("epmdocFolder---->" + docFolder);
    boolean flag = true;
	flag = SessionServerHelper.manager.setAccessEnforced(false);
	try {
    if ((StringUtils.isEmpty(sync)) && (eventType.equals("POST_CHECKIN"))) {
      if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
        throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
      }
      if (!docFolder.getFolderPath().contains("工作区"))
        WCToPMHelper.CreateEPMDocToPM(epmdoc);
    } else if ((StringUtils.isEmpty(sync)) && (eventType.equals("UPDATE"))) {
      if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
        throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
      }
      if (!docFolder.getFolderPath().contains("工作区"))
        WCToPMHelper.CreateEPMDocToPM(epmdoc);
    }
    else if ((StringUtils.isNotEmpty(sync)) && ((eventType.equals("POST_STORE")||eventType.equals("POST_MODIFY")))) {
        if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
            throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
          }
      String pmoid = iba.getIBAValue("PMId");
      Debug.P("POST_STORE-------------pmoid----------->" + pmoid+"    version1s---->"+epmdoc.getVersionIdentifier().getValue()+"    version1---->"+epmdoc.getIterationIdentifier().getValue());
      epmdoc= EPMUtil.getEPMDocument(epmdoc.getNumber(), null);
      Debug.P("POST_STORE-------------pmoid----------->" + pmoid+"    version1s---->"+epmdoc.getVersionIdentifier().getValue()+"    version1---->"+epmdoc.getIterationIdentifier().getValue());
      WCToPMHelper.updatePMCADDoc(pmoid, epmdoc);
    }
    else if ((StringUtils.isNotEmpty(sync)) && (eventType.equals("POST_CHECKIN"))) {
        if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
            throw new Exception("不允许将EPM文档检入到容器根文件夹下！请重新指定文件夹");
          }
      String pmoid = iba.getIBAValue("PMId");
     Debug.P("POST_STORE-------------pmoid----------->" + pmoid+"    version1s---->"+epmdoc.getVersionIdentifier().getValue()+"    version1---->"+epmdoc.getIterationIdentifier().getValue());
      epmdoc= EPMUtil.getEPMDocument(epmdoc.getNumber(), null);
      Debug.P("POST_STORE-------------pmoid----------->" + pmoid+"    version1s---->"+epmdoc.getVersionIdentifier().getValue()+"    version1---->"+epmdoc.getIterationIdentifier().getValue());

      WCToPMHelper.updatePMCADDoc(pmoid, epmdoc);
    }
    else if (eventType.equals("PRE_DELETE")) {
      String pmoid = iba.getIBAValue("PMId");
      Debug.P("PRE_DELETE---------1--------pmoid----------->" + pmoid+"   EPMDocument---->"+WorkInProgressHelper.isCheckedOut(epmdoc));
      //epmdoc= EPMUtil.getEPMDocument(epmdoc.getNumber(), null); 
     // Debug.P("PRE_DELETE---------2--------pmoid----------->" + pmoid+"   EPMDocument---->"+WorkInProgressHelper.isCheckedOut(epmdoc));
      epmdoc=(EPMDocument)Utils.getWCObject(EPMDocument.class, epmdoc.getNumber());
      Debug.P("PRE_DELETE---------3--------pmoid----------->" + pmoid+"   EPMDocument---->"+WorkInProgressHelper.isCheckedOut(epmdoc));
      if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
          return;
        }
      if (docFolder.getFolderPath().contains("工作区")){
    	  return;
      }
      if(WorkInProgressHelper.isCheckedOut(epmdoc)){
    	  return;
      }
      if (!WorkInProgressHelper.isCheckedOut(epmdoc)){
    	  List partList =new ArrayList();
          String epmType=epmdoc.getCADName();
          if(epmType.endsWith(".drw")){
        	  epmdoc= (EPMDocument) DocUtils.getEPMReferences(epmdoc).get(0);
          }
          partList=DocUtils.getDescribePartsByEPMDoc(epmdoc);
          Debug.P("图纸："+epmdoc.getNumber()+"存在关联部件"+partList.size());
          if(partList.size()>0){
       	   throw new Exception("图纸："+epmdoc.getNumber()+"存在关联部件，不允许删除！");
          }
            WCToPMHelper.deletePMCADDoc(pmoid, epmdoc);
//    	  WCToPMHelper.updatePMDocument(pmoid);
      }
    }
	}catch(Exception e){
		throw new Exception("图纸创建/同步出错，请联系管理员"+e.getMessage());
	}finally {
		SessionServerHelper.manager.setAccessEnforced(flag);
	}
//    }
  }
  
  public static void listenerEPMDoc1(EPMDocument epmdoc, String eventType)
		    throws Exception  {
		    Debug.P("事件类型---->" + eventType);
		    Debug.P("事件对象---->" + epmdoc);
		    Debug.P("事件对象编码---->" + epmdoc.getNumber());
		    Folder docFolder = FolderHelper.service.getFolder(epmdoc);
		    Debug.P("事件对象文件夹------->"+docFolder.getFolderPath());
		    if ( eventType.equals("POST_CHECKIN")) {
		    	IBAUtils iba = new IBAUtils(epmdoc);
			    Debug.P("ibautils--->" + iba);
			    String sync = iba.getIBAValue("CyncData");
			    String pmoids = iba.getIBAValue("PMId");
			    Debug.P("sync--->" + sync);
			    Debug.P("pmoids--->" + pmoids);
		    } else if(eventType.equals("UPDATE")){
		    	IBAUtils iba = new IBAUtils(epmdoc);
			    Debug.P("ibautils--->" + iba);
			    String sync = iba.getIBAValue("CyncData");
			    String pmoids = iba.getIBAValue("PMId");
			    Debug.P("sync--->" + sync);
			    Debug.P("pmoids--->" + pmoids);
		    }else if(eventType.equals(PersistenceManagerEvent.POST_MODIFY)){
		    	IBAUtils iba = new IBAUtils(epmdoc);
			    Debug.P("ibautils--->" + iba);
			    String sync = iba.getIBAValue("CyncData");
			    String pmoids = iba.getIBAValue("PMId");
			    Debug.P("sync--->" + sync);
			    Debug.P("pmoids--->" + pmoids);
		    }
		    else if(eventType.equals("POST_STORE")){
		    	IBAUtils iba = new IBAUtils(epmdoc);
			    Debug.P("ibautils--->" + iba);
			    String sync = iba.getIBAValue("CyncData");
			    String pmoids = iba.getIBAValue("PMId");
			    Debug.P("sync--->" + sync);
			    Debug.P("pmoids--->" + pmoids);
		    }else if(eventType.equals("PRE_DELETE")){
		    	IBAUtils iba = new IBAUtils(epmdoc);
			    Debug.P("ibautils--->" + iba);
			    String sync = iba.getIBAValue("CyncData");
			    String pmoids = iba.getIBAValue("PMId");
			    Debug.P("sync--->" + sync);
			    Debug.P("pmoids--->" + pmoids);
		    }else if(eventType.equals("NEW_ITERATION")){
		    	IBAUtils iba = new IBAUtils(epmdoc);
			    Debug.P("ibautils--->" + iba);
			    String sync = iba.getIBAValue("CyncData");
			    String pmoids = iba.getIBAValue("PMId");
			    Debug.P("sync--->" + sync);
			    Debug.P("pmoids--->" + pmoids);
		    }
		  }

  public static boolean findNewPartNum(String newPartNum)
    throws WTException
  {
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