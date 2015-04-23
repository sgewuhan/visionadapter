package ext.tmt.part.listener;

import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;

import ext.tmt.WC2PM.WCToPMHelper;
import ext.tmt.part.PartUtils;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.StringUtil;
import ext.tmt.utils.Utils;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import wt.epm.EPMDocument;
import wt.fc.Identified;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManager;
import wt.fc.PersistenceManagerEvent;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.FolderService;
import wt.method.RemoteAccess;
import wt.org.WTOrganization;
import wt.org.WTUser;
import wt.part.PartType;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartService;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionManager;
import wt.session.SessionManagerSvr;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.WorkInProgressHelper;

public class PartHelper
  implements RemoteAccess, Serializable
{
  private static final long serialVersionUID = 2304592616754675533L;
  private static final String MATER_NO = "Material_NO";
  
  public static void listenerWTPart(WTPart wtPart, String eventType)
    throws Exception
  {
    Debug.P("事件类型---->" + eventType);
    Debug.P("wtPart----->" + wtPart.getNumber());
    String newNumber = "";
    String partNumber = "";
    String partType = "";
    String types = "";
    boolean flag = true;
    flag = SessionServerHelper.manager.setAccessEnforced(false);
    try
    {
      partType = DocUtils.getType(wtPart);
      IBAUtils iba = new IBAUtils(wtPart);
      IBAUtils epmIba = null;
     // Debug.P("ibautils--->" + iba);
      String sync = iba.getIBAValue("CyncData");
      String pmoids = iba.getIBAValue("PMId");
      Debug.P("sync--->" + sync);
      Debug.P("pmoids--->" + pmoids);
      Debug.P("eventType---------------->" + eventType);
      Folder docFolder = FolderHelper.service.getFolder(wtPart);
      Debug.P("partFolder---->" + docFolder);
      EPMDocument epmdoc=null;
      String epmPartType="";
      if ((StringUtils.isEmpty(sync)) && (eventType.equals("POST_STORE"))) {
        if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
          throw new Exception("不允许将零部件创建/移动到容器根文件夹下！请重新指定文件夹");
        }
        epmdoc = null;
        String productName = "";
        String prefix = "";
        partNumber = wtPart.getNumber();
        if(!Utils.checkNumberStr2(partNumber)){
    		throw new Exception("部件编码只能包含字母，数字，-；请改正！");
    	}
        
        if (StringUtils.isNotEmpty(partType)) {
          partType = partType.replaceAll(" ", "").trim();
        }
        Debug.P(partNumber + "------------------->" + partType + " event--->" + eventType);
        if (partType.equals("wt.part.WTPart")) {
          Debug.P("--888--->wt.part.WTPart-->>>PartType:" + partType);
          epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
          Debug.P("1-->" + epmdoc);
          if (epmdoc == null) {
            List persistables = EPMDocUtil.getEPMDocumentByIBA("Material_NO", wtPart.getNumber());
            epmdoc = getLastModifierObject(persistables);
          }

          Debug.P("211-->" + epmdoc);
          if (epmdoc != null) {   
            epmIba = new IBAUtils(epmdoc);
            epmPartType = epmIba.getIBAValue("Part_Type");
          }
          if (StringUtils.isNotEmpty(epmPartType)) {
            epmPartType = epmPartType.replaceAll(" ", "").trim();
          }
          Debug.P(epmPartType);
          if (StringUtils.isNotEmpty(epmPartType)) {
            if (epmPartType.equals("半成品")) {
              types = "wt.part.WTPart|com.plm.SemiFinishedProduct";
            }
            else if (epmPartType.equals("成品"))
            {
              types = "wt.part.WTPart|com.plm.Product";
            }
            else {
              throw new Exception("检入图纸时，只允许创建成品和半成品");
            }
            if (types.contains("Product")) {
              Debug.P(types);
              TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
              wtPart.setPartType(PartType.getPartTypeDefault());
              wtPart.setTypeDefinitionReference(typeDefinitionRef);

              if (!(PersistenceHelper.isPersistent(wtPart))) {
                wtPart = (WTPart)PersistenceHelper.manager.store(wtPart);
                wtPart = (WTPart)PersistenceHelper.manager.refresh(wtPart);
              }
            }
            setPartIBAValues(wtPart, epmdoc);
            if (types.contains("com.plm.SemiFinishedProduct"))
              WCToPMHelper.CreatePartToPM(wtPart);
            else if (types.contains("com.plm.Product"))
              WCToPMHelper.CreatePMProductToPM(wtPart);
          }
          else {
            throw new Exception("检入失败！图纸上：" + wtPart.getNumber() + " 的“部件类型”值为空");
          }
          partType = DocUtils.getType(wtPart);
          Debug.P(partNumber + "------------------->" + partType);
          if (partType.contains("com.plm.SemiFinishedProduct")) {
            WCToPMHelper.CreatePartToPM(wtPart);
          } else if (partType.contains("com.plm.Product")) {
            WCToPMHelper.CreatePMProductToPM(wtPart);
          } else if (partType.contains("Material")) {
            WCToPMHelper.CreatePMaterialToPM(wtPart);
          } else if (partType.contains("com.plm.GuestPart")) {
            WCToPMHelper.CreateSupplyToPM(wtPart);
          } else if (partType.contains("com.plm.PackingMaterialPart")) {
            WCToPMHelper.CreatePMPackageToPM(wtPart);
          } else if (partType.contains("com.plm.ToolPart")) {
            WCToPMHelper.CreateJigToolPartToPM(wtPart);
          }
        }
        else if (partType.contains("com.plm.Product"))
        {
        	//成品编码=TX+三位分类码+四位流水码。其中分类码为成品所在产品库容器名称的前三个字符，自动根据成品所在产品库获取。
          productName = wtPart.getContainerName();
          Debug.P("---999---Contants.PRODUCTPART-->>>PartType:" + partType + "   PartNum:" + wtPart.getNumber() + "   productName:" + productName);
        //批量导入部件时如果导入的部件编码含有TX则不修改部件编码
          if (wtPart.getNumber().toUpperCase().contains("TX")) {
            WCToPMHelper.CreatePMProductToPM(wtPart);
            return;
          }
          if (!(productName.toUpperCase().contains("TX"))) {
            throw new Exception("产品:" + productName + "  未添加  TX 前缀！");
          }
          prefix = productName.substring(0, productName.indexOf("-"));
          if ((prefix.toUpperCase().trim().contains("TX48")) || (prefix.toUpperCase().trim().contains("TX49")) || (prefix.toUpperCase().trim().contains("TX426")) || 
            (prefix.toUpperCase().trim().contains("TX113")) || (prefix.toUpperCase().trim().contains("TX114")) || (prefix.toUpperCase().trim().contains("TX115"))) {
            WCToPMHelper.CreatePMProductToPM(wtPart);
            return;
          }
          Debug.P("产品前缀----》" + prefix);
          if (prefix.toUpperCase().trim().contains("TX111")) {
            int i = 9000;
            do {
              partNumber = prefix + StringUtil.int2String(i, 4);
              if (PartUtil.getPartByNumber(partNumber) == null) {
                newNumber = partNumber;
                break;
              }
              i++;
            }  while (i < 9999);
            changePartNumber(wtPart, newNumber);
          } else {
            int i = 0;
            do {
              if ((prefix.toUpperCase().trim().equals("TXA6")) || (prefix.toUpperCase().trim().equals("TXA7")) || (prefix.toUpperCase().trim().equals("TXA8")))
                partNumber = prefix + StringUtil.int2String(i, 5);
              else {
                partNumber = prefix + StringUtil.int2String(i, 4);
              }
              if (PartUtil.getPartByNumber(partNumber) == null) {
                newNumber = partNumber;
                break;
              }
              i++;
            }
            while (
              i < 100000);
            changePartNumber(wtPart, newNumber);
          }
          wtPart = PartUtil.getPartByNumber(wtPart.getNumber());
          Debug.P("----999------>>part:" + wtPart.getName() + "  partNum" + wtPart.getNumber());
          WCToPMHelper.CreatePMProductToPM(wtPart);
          Debug.P("---999-->>>>CreatePMProductToPM  Success!!");
        }
        else if (partType.contains("com.plm.SemiFinishedProduct")) {
          Debug.P("--1000--Contants.SEMIFINISHEDPRODUCT-->>>PartType:" + partType);
          if (wtPart.isEndItem()) {
            throw new Exception("您创建的是半产品，请将“是否为成品”的值设置为“否”！");
          }
          String isKHpart = "";//空簧部件分类
          isKHpart = iba.getIBAValue("AirSpringClassification");
          Debug.P("WTPart -->" + isKHpart);
        //如果部件上的 空簧部件分类 值为空，则从部件关联的EPMDocument上获取
          if ((StringUtils.isEmpty(isKHpart)) && (epmdoc != null)) {
            IBAUtils epmIBA = new IBAUtils(epmdoc);
            isKHpart = epmIBA.getIBAValue("AirSpringClassification");
            Debug.P("EPMDocument -->" + isKHpart);
          }
          if (StringUtils.isNotEmpty(isKHpart)) {
            int i = 0;
            do {
              partNumber = isKHpart + StringUtil.int2String(i, 4);
              if (PartUtil.getPartByNumber(partNumber) == null) {
                newNumber = partNumber;
                break;
              }
              i++;
            }
            while (
              i < 100000);
            changePartNumber(wtPart, newNumber);
            wtPart = PartUtil.getPartByNumber(wtPart.getNumber());
          }
          Debug.P("CreatePartToPM-->");
          WCToPMHelper.CreatePartToPM(wtPart);
        } else if (partType.contains("Material")) {
          Debug.P("CreatePMaterialToPM-->");
          wtPart = PartUtil.getPartByNumber(wtPart.getNumber());
          WCToPMHelper.CreatePMaterialToPM(wtPart);
        } else if (partType.contains("com.plm.GuestPart")) {
          Debug.P("CreateSupplyToPM-->");
          wtPart = PartUtil.getPartByNumber(wtPart.getNumber());
          WCToPMHelper.CreateSupplyToPM(wtPart);
        } else if (partType.contains("com.plm.PackingMaterialPart")) {
          Debug.P("CreatePMPackageToPM-->");
          wtPart = PartUtil.getPartByNumber(wtPart.getNumber());
          WCToPMHelper.CreatePMPackageToPM(wtPart);
        } else if (partType.contains("com.plm.ToolPart")) {
          Debug.P("CreateJigToolPartToPM--->");
          wtPart = PartUtil.getPartByNumber(wtPart.getNumber());
          WCToPMHelper.CreateJigToolPartToPM(wtPart);
        }
      }
      else if ((StringUtils.isEmpty(sync)) && (eventType.equals("POST_MODIFY"))) {
        Debug.P("partType----->" + partType);
        if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
          throw new Exception("不允许将零部件创建/移动到容器根文件夹下！请重新指定文件夹");
        }
        if (partType.equals("wt.part.WTPart")) {
          epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
          epmPartType = "";
          Debug.P("1-->" + epmdoc);
          if (epmdoc == null) {
            epmdoc = EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
          }
          Debug.P("2-->" + epmdoc);
          if (epmdoc != null) {
            epmIba = new IBAUtils(epmdoc);
            epmPartType = epmIba.getIBAValue("Part_Type");
          }
          if (StringUtils.isNotEmpty(epmPartType)) {
            epmPartType = epmPartType.replaceAll(" ", "").trim();
          }
          Debug.P(epmPartType);
          Debug.P(Boolean.valueOf(StringUtils.isNotEmpty(epmPartType)));
          if (StringUtils.isNotEmpty(epmPartType)) {
            if (epmPartType.equals("半成品")) {
              types = "wt.part.WTPart|com.plm.SemiFinishedProduct";
            }
            else if (epmPartType.equals("成品")) {
              types = "wt.part.WTPart|com.plm.Product";
            }
            else {
              throw new Exception("检入图纸时，只允许自动创建半成品！");
            }
            Debug.P(types);
            if (types.contains("Product")) {
            	TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
              wtPart.setPartType(PartType.getPartTypeDefault());
              wtPart.setTypeDefinitionReference(typeDefinitionRef);
              if (!(PersistenceHelper.isPersistent(wtPart))) {
                wtPart = (WTPart)PersistenceHelper.manager.save(wtPart);
                wtPart = (WTPart)PersistenceHelper.manager.refresh(wtPart);
              }
            }
            setPartIBAValues(wtPart, epmdoc);
          }
          if (types.contains("com.plm.SemiFinishedProduct"))
            WCToPMHelper.CreatePartToPM(wtPart);
          else if (types.contains("com.plm.Product")) {
            WCToPMHelper.CreatePMProductToPM(wtPart);
          }
        }
        partType = DocUtils.getType(wtPart);
        Debug.P(types);
        Debug.P(partType);
        if (partType.contains("com.plm.SemiFinishedProduct"))
          WCToPMHelper.CreatePartToPM(wtPart);
        else if (partType.contains("com.plm.Product"))
          WCToPMHelper.CreatePMProductToPM(wtPart);
        else if (partType.contains("Material"))
          WCToPMHelper.CreatePMaterialToPM(wtPart);
        else if (partType.contains("com.plm.GuestPart"))
          WCToPMHelper.CreateSupplyToPM(wtPart);
        else if (partType.contains("com.plm.PackingMaterialPart"))
          WCToPMHelper.CreatePMPackageToPM(wtPart);
        else if (partType.contains("com.plm.ToolPart")) {
          WCToPMHelper.CreateJigToolPartToPM(wtPart);
        }
//        if (types.contains("com.plm.SemiFinishedProduct"))
//          WCToPMHelper.CreatePartToPM(wtPart);
//        else if (types.contains("com.plm.Product"))
//          WCToPMHelper.CreatePMProductToPM(wtPart);
//        else if (types.contains("Material"))
//          WCToPMHelper.CreatePMaterialToPM(wtPart);
//        else if (types.contains("com.plm.GuestPart"))
//          WCToPMHelper.CreateSupplyToPM(wtPart);
//        else if (types.contains("com.plm.PackingMaterialPart"))
//          WCToPMHelper.CreatePMPackageToPM(wtPart);
//        else if (types.contains("com.plm.ToolPart")) {
//          WCToPMHelper.CreateJigToolPartToPM(wtPart);
//        }
      }
      else if ((StringUtils.isEmpty(sync)) && (eventType.equals("UPDATE"))) {
        if (docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")) {
          throw new Exception("不允许将零部件创建/移动到容器根文件夹下！请重新指定文件夹");
        }
        Debug.P("StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)------------------");
        Debug.P("partType----->" + partType);
        if (partType.equals("wt.part.WTPart")) {
          epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
          epmPartType = "";
          Debug.P("1-->" + epmdoc);
          if (epmdoc == null) {
            epmdoc = EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
          }
          Debug.P("2-->" + epmdoc);
          if (epmdoc != null) {
            epmIba = new IBAUtils(epmdoc);
            epmPartType = epmIba.getIBAValue("Part_Type");
          }
          if (StringUtils.isNotEmpty(epmPartType)) {
            epmPartType = epmPartType.replaceAll(" ", "").trim();
          }
          Debug.P(epmPartType);
          Debug.P(Boolean.valueOf(StringUtils.isNotEmpty(epmPartType)));
          if (StringUtils.isNotEmpty(epmPartType)) {
            if (epmPartType.equals("半成品")) {
              types = "wt.part.WTPart|com.plm.SemiFinishedProduct";
            }
            else if (epmPartType.equals("成品")) {
              types = "wt.part.WTPart|com.plm.Product";
            }
            else {
              throw new Exception("检入图纸时，只允许自动创建半成品！");
            }
            Debug.P(types);
            if (types.contains("Product")) {
            	TypeDefinitionReference   typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
              wtPart.setPartType(PartType.getPartTypeDefault());
              wtPart.setTypeDefinitionReference(typeDefinitionRef);
              if (!(PersistenceHelper.isPersistent(wtPart))) {
                wtPart = (WTPart)PersistenceHelper.manager.save(wtPart);
                wtPart = (WTPart)PersistenceHelper.manager.refresh(wtPart);
              }
            }
            setPartIBAValues(wtPart, epmdoc);
          }
        }
        partType = DocUtils.getType(wtPart);
        Debug.P(partType);
        if (partType.contains("com.plm.SemiFinishedProduct")) {
          WCToPMHelper.CreatePartToPM(wtPart);
        } else if (partType.contains("com.plm.Product")) {
          WCToPMHelper.CreatePMProductToPM(wtPart);
        } else if (partType.contains("Material")) {
          WCToPMHelper.CreatePMaterialToPM(wtPart);
        } else if (partType.contains("com.plm.GuestPart")) {
          WCToPMHelper.CreateSupplyToPM(wtPart);
        } else if (partType.contains("com.plm.PackingMaterialPart")) {
          WCToPMHelper.CreatePMPackageToPM(wtPart);
        } else if (partType.contains("com.plm.ToolPart")) {
          WCToPMHelper.CreateJigToolPartToPM(wtPart);
        }
      }
      if ((StringUtils.isNotEmpty(sync)) && (eventType.equals("UPDATE"))) {
        if (partType.equals("wt.part.WTPart")) {
          epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
          epmPartType = "";
          Debug.P("1-->" + epmdoc);
          if (epmdoc == null) {
            epmdoc = EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
        }
          Debug.P("2-->" + epmdoc);
          if (epmdoc != null) {
            epmIba = new IBAUtils(epmdoc);
            epmPartType = epmIba.getIBAValue("Part_Type");
          }

          if (StringUtils.isNotEmpty(epmPartType)) {
            epmPartType = epmPartType.replaceAll(" ", "").trim();
          }
          Debug.P(epmPartType);
          if (StringUtils.isNotEmpty(epmPartType)) {
            if (epmPartType.equals("半成品")) {
              types = "wt.part.WTPart|com.plm.SemiFinishedProduct";
            }
            else if (epmPartType.equals("成品"))
            {
              types = "wt.part.WTPart|com.plm.Product";
            }
            else {
              throw new Exception("检入图纸时，只允许自动创建半成品！");
            }

            Debug.P(types);
            if (types.contains("Product")) {
              partType = DocUtils.getDfmDocumentType(types).getIntHid();
              Debug.P(partType);
              TypeDefinitionReference   typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
              wtPart.setPartType(PartType.getPartTypeDefault());
              wtPart.setTypeDefinitionReference(typeDefinitionRef);
              if (!(PersistenceHelper.isPersistent(wtPart))) {
                wtPart = (WTPart)PersistenceHelper.manager.save(wtPart);
                wtPart = (WTPart)PersistenceHelper.manager.refresh(wtPart);
              }
            }
            setPartIBAValues(wtPart, epmdoc);
          }
        }
      }
      if ((StringUtils.isNotEmpty(sync)) && (eventType.equals("POST_STORE"))) {
        pmoid = iba.getIBAValue("PMId");
        Debug.P("POST_STORE-------------pmoid----------->" + pmoid);

        wtPart = PartUtils.getPartByNumber(wtPart.getNumber());
        if (WorkInProgressHelper.isCheckedOut(wtPart))
        {
          if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.SemiFinishedProduct"))) {
            WCToPMHelper.updatePMPart(pmoid, wtPart);
          } else if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.Product"))) {
            WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
          } else if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("Material"))) {
            WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
          } else if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.GuestPart"))) {
            WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
          } else if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.PackingMaterialPart"))) {
            WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
          } else if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.ToolPart"))) {
            WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
          }
        }
      }
      if ((StringUtils.isNotEmpty(sync)) && (((eventType.equals("POST_CHECKIN")) || 
        (eventType.equals("POST_MODIFY"))))) {
        pmoid = (String)LWCUtil.getValue(wtPart, "PMId");
        wtPart = PartUtil.getPartByNumber(wtPart.getNumber());
        EPMDocument epmdoc_rel = EPMDocUtil.getActiveEPMDocument(wtPart);
        if (epmdoc_rel != null) {
          setPartIBAValues(wtPart, epmdoc_rel);
        }
        Debug.P("POST_CHECKIN-----------pmoid----------->" + pmoid);
        if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.SemiFinishedProduct"))) {
          WCToPMHelper.updatePMPart(pmoid, wtPart);
        }
        if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.Product"))) {
          WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
        }
        if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("Material"))) {
          WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
        }
        if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.GuestPart"))) {
          WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
        }
        if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.PackingMaterialPart"))) {
          WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
        }
        if ((StringUtils.isNotEmpty(pmoid)) && (partType.contains("com.plm.ToolPart"))) {
          WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
         }
          }else  if (eventType.equals(PersistenceManagerEvent.POST_DELETE)) {
  			Debug.P("WTPart-->"+wtPart);
  			if(wtPart !=null){
  				 pmoid = iba.getIBAValue(Contants.PMID);
  				if(StringUtils.isNotEmpty(pmoid)){
  					Debug.P("删除部件 "+wtPart.getNumber()+" 的最新小版本，重新以 -》"+pmoid+" 创建-------------");  
  				  	if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){//如果半是成品
  						WCToPMHelper.CreatePartToPM( wtPart);
  					  }else if(partType.contains(Contants.PRODUCTPART)){ //如果是成品
  						  WCToPMHelper.CreatePMProductToPM( wtPart);
  					  }else if(partType.contains(Contants.MATERIAL)){ //如果是原材料
  							WCToPMHelper.CreatePMaterialToPM(wtPart);
  					  }else if(partType.contains(Contants.SUPPLYMENT)){//如果是客供件
  							WCToPMHelper.CreateSupplyToPM(wtPart);
  					  }else if(partType.contains(Contants.PACKINGPART)){//如果是包装材料
  							WCToPMHelper.CreatePMPackageToPM(wtPart);
  					  }else if(partType.contains(Contants.TOOLPART)){//如果是备品备料
  							WCToPMHelper.CreateJigToolPartToPM( wtPart);
  					  }
  				}
  			}
  		}
    }
    catch (Exception e) {
    	throw new Exception("部件创建/同步出错，请联系管理员"+e.getMessage());
    }
    finally {
      SessionServerHelper.manager.setAccessEnforced(flag); 
      }
      SessionServerHelper.manager.setAccessEnforced(flag);
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

  private static void changePartNumber(WTPart part, String number)
    throws WTException
  {
    boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
    WTUser currentuser = (WTUser)SessionHelper.manager.getPrincipal();
    SessionHelper.manager.setAdministrator();
    Transaction tx = null;
    try {
      tx = new Transaction();
      tx.start();
      Identified identified = (Identified)part.getMaster();
      String name = part.getName();
      WTOrganization org = part.getOrganization();
      WTPartHelper.service.changeWTPartMasterIdentity(
        (WTPartMaster)identified, name, number, org);
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

  public static void setPartIBAValues(WTPart part, EPMDocument cad)
    throws Exception
  {
	   Debug.P("----->>>>setPartIBAValues:"+cad);
			 part=PartUtils.getPartByNumber(part.getNumber());
//			 part=(WTPart)Utils.getWCObject(WTPart.class, part.getNumber());
			 cad=EPMUtil.getEPMDocument(cad.getNumber(), null);
//			 cad=(EPMDocument)Utils.getWCObject(EPMDocument.class, cad.getNumber());
			 cad=(EPMDocument)PartUtils.getObj(cad.getNumber(), EPMDocument.class);
			 if(part==null||cad==null){
				 return;
			 }
			 IBAUtils partIBA = new IBAUtils(part);
			 IBAUtils cadIBA = new IBAUtils(cad);
			 
			 
			 String ProjectNo=cadIBA.getIBAValue(Contants.PROJECTNO);
			 String Part_Type=cadIBA.getIBAValue(Contants.PART_TYPE);
			 String Material=cadIBA.getIBAValue(Contants.MATERIAL);
			 String AirSpringClassification=cadIBA.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
			 String Weight=cadIBA.getIBAValue(Contants.WEIGHT);
			 String PHASE=cadIBA.getIBAValue(Contants.PHASE);
			 String Product_NO=cadIBA.getIBAValue(Contants.PRODUCTNO);
			 String Material_NO=cadIBA.getIBAValue(Contants.MATERIALNO);
			 String Material_Classification=cadIBA.getIBAValue(Contants.MATERIALGROUP);
			 String DRAWN_BY=cadIBA.getIBAValue(Contants.DRAWN_BY);
			 String DRAWN_DATE=cadIBA.getIBAValue(Contants.DRAWN_DATE);
			 String CORRECTOR=cadIBA.getIBAValue(Contants.CORRECTOR);
			 String CORRECT_DATE=cadIBA.getIBAValue(Contants.CORRECT_DATE);
			 String AUDITOR=cadIBA.getIBAValue(Contants.AUDITOR);
			 String AUDIT_DATE=cadIBA.getIBAValue(Contants.AUDIT_DATE);
			 String STANDARDIZE=cadIBA.getIBAValue(Contants.STANDARDIZE);
			 String STANDARDIZE_DATE=cadIBA.getIBAValue(Contants.STANDARDIZE_DATE);
			 String APPROVER=cadIBA.getIBAValue(Contants.APPROVER);
			 String APPROVE_DATE=cadIBA.getIBAValue(Contants.APPROVE_DATE);
			 String PROCESS_REVIEWER=cadIBA.getIBAValue(Contants.PROCESS_REVIEWER);
			 String PROCESS_REVIEW_DATE=cadIBA.getIBAValue(Contants.PROCESS_REVIEW_DATE);
			 
			 //设置签审信息
			 partIBA.setIBAValue(Contants.PROJECTNO, ProjectNo==null?"":ProjectNo);
			 partIBA.setIBAValue(Contants.DRAWN_BY, DRAWN_BY==null?"":DRAWN_BY);
			 partIBA.setIBAValue(Contants.DRAWN_DATE, DRAWN_DATE==null?"":DRAWN_DATE);
			 partIBA.setIBAValue(Contants.CORRECTOR, CORRECTOR==null?"":CORRECTOR);
			 partIBA.setIBAValue(Contants.CORRECT_DATE, CORRECT_DATE==null?"":CORRECT_DATE);
			 partIBA.setIBAValue(Contants.AUDIT_DATE, AUDIT_DATE==null?"":AUDIT_DATE);
			 partIBA.setIBAValue(Contants.AUDITOR, AUDITOR==null?"":AUDITOR);
			 partIBA.setIBAValue(Contants.STANDARDIZE, STANDARDIZE==null?"":STANDARDIZE);
			 partIBA.setIBAValue(Contants.STANDARDIZE_DATE, STANDARDIZE_DATE==null?"":STANDARDIZE_DATE);
			 partIBA.setIBAValue(Contants.APPROVE_DATE, APPROVE_DATE==null?"":APPROVE_DATE);
			 partIBA.setIBAValue(Contants.APPROVER, APPROVER==null?"":APPROVER);
			 partIBA.setIBAValue(Contants.PROCESS_REVIEWER, PROCESS_REVIEWER==null?"":PROCESS_REVIEWER);
			 partIBA.setIBAValue(Contants.PROCESS_REVIEW_DATE, PROCESS_REVIEW_DATE==null?"":PROCESS_REVIEW_DATE);
	         
			 
			 partIBA.setIBAValue(Contants.PART_TYPE, Part_Type);
			 if(StringUtils.isEmpty(Material)){
				 Material="";
			 }else{
				 if(Material.equals("Default")){
					 Material="";
				 }
			 }
			 partIBA.setIBAValue(Contants.MATERIAL, Material);
			 if(StringUtils.isEmpty(AirSpringClassification)){
				 AirSpringClassification="";
			 }else{
				 if(AirSpringClassification.equals("Default")){
					 AirSpringClassification="";
				 }
			 }
			 partIBA.setIBAValue(Contants.AIRSPRINGCLASSIFICATION, AirSpringClassification);
			 partIBA.setIBAValue(Contants.WEIGHT, Weight);
			 partIBA.setIBAValue(Contants.PHASE, PHASE);
			 if(StringUtils.isEmpty(Product_NO)){
				 Product_NO="";
			 }else{
				 if(Product_NO.equals("Default")){
					 Product_NO="";
				 }
			 }
			 partIBA.setIBAValue(Contants.PRODUCTNO, Product_NO);
			 if(StringUtils.isEmpty(Material_NO)){
				 Material_NO="";
			 }else{
				 if(Material_NO.equals("Default")){
					 Material_NO="";
				 }
			 }
			 partIBA.setIBAValue(Contants.MATERIALNO,Material_NO );
			 if(StringUtils.isEmpty(Material_Classification)){
				 Material_Classification="";
			 }else{
				 if(Material_Classification.equals("Default")){
					 Material_Classification="";
				 }
			 }
			 partIBA.setIBAValue(Contants.MATERIALGROUP, Material_Classification);
			 partIBA.updateIBAPart(part);
			 Debug.P("----------updateIBAPart-Success!!------------------");
  }

  private static EPMDocument getLastModifierObject(List<Persistable> objects) {
    EPMDocument result = null;
    if (objects != null) {
      Debug.P("-------->>>getLastModifierObject(Objects Size:)" + objects.size());
      long cpTime = 0L;
      for (int i = 0; i < objects.size(); ++i) {
        EPMDocument temp_epm = (EPMDocument)objects.get(i);
        if (result == null) {
          cpTime = temp_epm.getModifyTimestamp().getTime();
          result = temp_epm;
        } else {
          long temp_modTime = temp_epm.getModifyTimestamp().getTime();
          if (temp_modTime > cpTime) {
            cpTime = temp_modTime;
            result = temp_epm;
          }
        }
      }
    }
    return result;
  }
}