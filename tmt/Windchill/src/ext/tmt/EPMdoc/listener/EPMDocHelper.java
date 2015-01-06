package ext.tmt.EPMdoc.listener;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.WorkInProgressHelper;
import ext.tmt.WC2PM.WCToPMHelper;
import ext.tmt.utils.Debug;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.IBAUtils;

public class EPMDocHelper
  implements Serializable
{
  private static final long serialVersionUID = 2304592616754675533L;

  @SuppressWarnings("deprecation")
public static void listenerEPMDoc(EPMDocument epmdoc, String eventType)
    throws Exception
  {
    Debug.P("事件类型---->" + eventType);
    Debug.P("事件对象---->" + epmdoc.getNumber());
    IBAUtils iba = new IBAUtils(epmdoc);
    Debug.P("ibautils--->" + iba);
    String sync = iba.getIBAValue("CyncData");
    String pmoids = iba.getIBAValue("PMId");
    Debug.P("sync--->" + sync);
    Debug.P("pmoids--->" + pmoids);
    Folder docFolder = FolderHelper.service.getFolder(epmdoc);
    Debug.P("epmdocFolder---->" + docFolder);
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
      epmdoc= EPMUtil.getEPMDocument(epmdoc.getNumber(), null);
      Debug.P("PRE_DELETE-----------------pmoid----------->" + pmoid);
      if (!WorkInProgressHelper.isCheckedOut(epmdoc))
        WCToPMHelper.deletePMCADDoc(pmoid, epmdoc);
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