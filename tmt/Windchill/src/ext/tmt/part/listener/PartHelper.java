package ext.tmt.part.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import ext.tmt.WC2PM.WCToPMHelper;
import ext.tmt.part.PartUtils;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.DocUtils;
import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAHelper;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
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
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressServiceEvent;

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
		Debug.P("wtPart----->"+wtPart.getNumber());
		String newNumber="";
		String partNumber="";
		String partType="";
		String types="";
		boolean flag = true;
		flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
		partType=DocUtils.getType(wtPart);
		IBAUtils iba = new IBAUtils(wtPart);
		IBAUtils epmIba = null;
		Debug.P("ibautils--->"+iba);
		String sync=iba.getIBAValue(Contants.CYNCDATA);
		String pmoids = iba.getIBAValue(Contants.PMID);
		Debug.P("sync--->"+sync);
		Debug.P("pmoids--->"+pmoids);
		Debug.P("eventType---------------->"+eventType);
        if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			   EPMDocument epmdoc = null;
			   String productName="";	
			   String prefix="";
			   partNumber=wtPart.getNumber();
			   if(StringUtils.isNotEmpty(partType)){
				   partType=partType.replaceAll(" ", "").trim();
			   }
			   Debug.P(partNumber+"------------------->"+partType+" event--->"+eventType);
			    String epmPartType="";
			    if(partType.equals("wt.part.WTPart")){
			    	epmdoc=EPMDocUtil.getActiveEPMDocument(wtPart);
			    	Debug.P("1-->"+epmdoc);
			    	if(epmdoc==null){
			    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
			    	}
			    	Debug.P("2-->"+epmdoc);
			    	if(epmdoc!=null){
			    		 epmIba = new IBAUtils(epmdoc);
			    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
			    	}
			    	if(StringUtils.isNotEmpty(epmPartType)){
			    		epmPartType=epmPartType.replaceAll(" ", "").trim();
			    	}
			    	Debug.P(epmPartType);
			    	if(StringUtils.isNotEmpty(epmPartType)){
						if(epmPartType.equals("半成品")){
							types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
						}
						else if(epmPartType.equals("成品")){
							//wtPart.setEndItem(true);
							types="wt.part.WTPart|"+Contants.PRODUCTPART;
						}
						else{
							throw new Exception("检入图纸时，只允许创建成品和半成品");
						}
						if(types.contains("Product")){
							Debug.P(types);
							TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
							wtPart.setPartType(PartType.getPartTypeDefault());
							wtPart.setTypeDefinitionReference(typeDefinitionRef);
							
							if (!PersistenceHelper.isPersistent(wtPart)) {
								wtPart = (WTPart) PersistenceHelper.manager.store(wtPart);
								wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
							}
						}
						setPartIBAValues(wtPart,epmdoc);
						if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//如果半是成品
							WCToPMHelper.CreatePartToPM( wtPart);
						  }else if(types.contains(Contants.PRODUCTPART)){ //如果是成品
							  WCToPMHelper.CreatePMProductToPM( wtPart);
						  }
					}else{
						throw new Exception("检入失败！图纸上："+wtPart.getNumber()+" 的“部件类型”值为空");
					}
			    	partType=DocUtils.getType(wtPart);
			    	Debug.P(partNumber+"------------------->"+partType);
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
			    }else if(partType.contains(Contants.PRODUCTPART)){ //如果是成品
				//成品编码=TX+三位分类码+四位流水码。其中分类码为成品所在产品库容器名称的前三个字符，自动根据成品所在产品库获取。
					productName=wtPart.getContainerName();
					//批量导入部件时如果导入的部件编码含有TX则不修改部件编码
					if(wtPart.getNumber().toUpperCase().contains("TX")){
						WCToPMHelper.CreatePMProductToPM(wtPart);
						return;
					}
					if(!productName.toUpperCase().contains("TX")){
						throw new Exception("产品:"+productName+"  未添加  TX 前缀！");
					}
					Debug.P("productName--------->"+productName);
					prefix=productName.substring(0, productName.indexOf("-"));
					if(prefix.toUpperCase().trim().contains("TX48")||prefix.toUpperCase().trim().contains("TX49")||prefix.toUpperCase().trim().contains("TX426")
							||prefix.toUpperCase().trim().contains("TX113")||prefix.toUpperCase().trim().contains("TX114")||prefix.toUpperCase().trim().contains("TX115")){
						WCToPMHelper.CreatePMProductToPM(wtPart);
						return;
					}
					Debug.P("产品前缀----》"+prefix);
					if(prefix.toUpperCase().trim().contains("TX111")){
						int i=9000;
						do{
							partNumber=prefix+StringUtil.int2String(i,4);
						}while(i<9999);
						changePartNumber(wtPart,newNumber);
					}else{
						int i= 0;
						do {  
							if(prefix.toUpperCase().trim().equals("TXA6")||prefix.toUpperCase().trim().equals("TXA7")||prefix.toUpperCase().trim().equals("TXA8")){
								partNumber=prefix+StringUtil.int2String(i,5);
							}else {
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
					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreatePMProductToPM(wtPart);
				} else //如果是半成品
				if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){
					if(wtPart.isEndItem()){
						throw new Exception("您创建的是半产品，请将“是否为成品”的值设置为“否”！");
					} 
					String isKHpart="";//空簧部件分类
					isKHpart=iba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
					Debug.P("WTPart -->"+isKHpart);
					//如果部件上的 空簧部件分类 值为空，则从部件关联的EPMDocument上获取
					if(StringUtils.isEmpty(isKHpart)&&epmdoc!=null){
						IBAUtils epmIBA = new IBAUtils(epmdoc);
						isKHpart=epmIBA.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
						Debug.P("EPMDocument -->"+isKHpart);
					 }
					if(StringUtils.isNotEmpty(isKHpart)){
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
						wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					}
				     Debug.P("CreatePartToPM-->");	
					WCToPMHelper.CreatePartToPM(wtPart);
				}else if(partType.contains(Contants.MATERIAL)){ //如果是原材料
					 Debug.P("CreatePMaterialToPM-->");	
					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreatePMaterialToPM(wtPart);
				}else if(partType.contains(Contants.SUPPLYMENT)){//如果是客供件
					 Debug.P("CreateSupplyToPM-->");	
					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreateSupplyToPM(wtPart);
				}else if(partType.contains(Contants.PACKINGPART)){//如果是包装材料
					 Debug.P("CreatePMPackageToPM-->");	
					 wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreatePMPackageToPM(wtPart);
				}else if(partType.contains(Contants.TOOLPART)){//如果是备品备料
					Debug.P("CreateJigToolPartToPM--->");
					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreateJigToolPartToPM(wtPart);
				}
			 
		}else  if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_MODIFY)) {
			Debug.P("partType----->"+partType); 
			if(partType.equals("wt.part.WTPart")){
			EPMDocument epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
			String epmPartType="";
	    	Debug.P("1-->"+epmdoc);
	    	if(epmdoc==null){
	    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
	    	}
	    	Debug.P("2-->"+epmdoc);
			if(epmdoc!=null){
	    		 epmIba = new IBAUtils(epmdoc);
	    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
	    	}
	    	if(StringUtils.isNotEmpty(epmPartType)){
	    		epmPartType=epmPartType.replaceAll(" ", "").trim();
	    	}
	    	Debug.P(epmPartType);
	    	Debug.P(StringUtils.isNotEmpty(epmPartType));
	    	if(StringUtils.isNotEmpty(epmPartType)){
				if(epmPartType.equals("半成品")){
					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
				}
				else if(epmPartType.equals("成品")){
					types="wt.part.WTPart|"+Contants.PRODUCTPART;
				}
				else{
					throw new Exception("检入图纸时，只允许自动创建半成品！");
				}
				Debug.P(types);
				if(types.contains("Product")){
					TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
					wtPart.setPartType(PartType.getPartTypeDefault());
					wtPart.setTypeDefinitionReference(typeDefinitionRef);
					if (!PersistenceHelper.isPersistent(wtPart)) {
						wtPart = (WTPart) PersistenceHelper.manager.save(wtPart);
						wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
					}
				}
				setPartIBAValues(wtPart,epmdoc);
				
			}
	    	
	    	if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//如果半是成品
				WCToPMHelper.CreatePartToPM( wtPart);
			  }else if(types.contains(Contants.PRODUCTPART)){ //如果是成品
				  WCToPMHelper.CreatePMProductToPM( wtPart);
			  }
			 }
//	    	else{
//				throw new Exception("检入失败！找不到部件："+wtPart.getNumber()+" 对应的EPM文档");
//			}
			partType=DocUtils.getType(wtPart);
	    	Debug.P(types);
	    	Debug.P(partType);
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
			if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//如果半是成品
				WCToPMHelper.CreatePartToPM( wtPart);
			  }else if(types.contains(Contants.PRODUCTPART)){ //如果是成品
				  WCToPMHelper.CreatePMProductToPM( wtPart);
			  }else if(types.contains(Contants.MATERIAL)){ //如果是原材料
					WCToPMHelper.CreatePMaterialToPM(wtPart);
			  }else if(types.contains(Contants.SUPPLYMENT)){//如果是客供件
					WCToPMHelper.CreateSupplyToPM(wtPart);
			  }else if(types.contains(Contants.PACKINGPART)){//如果是包装材料
					WCToPMHelper.CreatePMPackageToPM(wtPart);
			  }else if(types.contains(Contants.TOOLPART)){//如果是备品备料
					WCToPMHelper.CreateJigToolPartToPM( wtPart);
			  }
			
		}else  if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)) {
			Debug.P("StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)------------------");
			Debug.P("partType----->"+partType); 
			if(partType.equals("wt.part.WTPart")){
			EPMDocument epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
			String epmPartType="";
	    	Debug.P("1-->"+epmdoc);
	    	if(epmdoc==null){
	    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
	    	}
	    	Debug.P("2-->"+epmdoc);
			if(epmdoc!=null){
	    		 epmIba = new IBAUtils(epmdoc);
	    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
	    	}
	    	if(StringUtils.isNotEmpty(epmPartType)){
	    		epmPartType=epmPartType.replaceAll(" ", "").trim();
	    	}
	    	Debug.P(epmPartType);
	    	Debug.P(StringUtils.isNotEmpty(epmPartType));
	    	if(StringUtils.isNotEmpty(epmPartType)){
				if(epmPartType.equals("半成品")){
					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
				}
				else if(epmPartType.equals("成品")){
					types="wt.part.WTPart|"+Contants.PRODUCTPART;
				}
				else{
					throw new Exception("检入图纸时，只允许自动创建半成品！");
				}
				Debug.P(types);
				if(types.contains("Product")){
					TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
					wtPart.setPartType(PartType.getPartTypeDefault());
					wtPart.setTypeDefinitionReference(typeDefinitionRef);
					if (!PersistenceHelper.isPersistent(wtPart)) {
						wtPart = (WTPart) PersistenceHelper.manager.save(wtPart);
						wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
					}
				}
				setPartIBAValues(wtPart,epmdoc);
			}
			 }
//	    	else{
//				throw new Exception("检入失败！找不到部件："+wtPart.getNumber()+" 对应的EPM文档");
//			}
			partType=DocUtils.getType(wtPart);
	    	Debug.P(partType);
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
		} if (StringUtils.isNotEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)) {
			if(partType.equals("wt.part.WTPart")){
			EPMDocument epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
			String epmPartType="";
	    	Debug.P("1-->"+epmdoc);
	    	if(epmdoc==null){
	    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
	    	}
	    	Debug.P("2-->"+epmdoc);
			if(epmdoc!=null){
	    		 epmIba = new IBAUtils(epmdoc);
	    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
	    	}
	    	if(StringUtils.isNotEmpty(epmPartType)){
	    		epmPartType=epmPartType.replaceAll(" ", "").trim();
	    	}
	    	Debug.P(epmPartType);
	    	if(StringUtils.isNotEmpty(epmPartType)){
				if(epmPartType.equals("半成品")){
					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
				}
				else if(epmPartType.equals("成品")){
					//wtPart.setEndItem(true);
					types="wt.part.WTPart|"+Contants.PRODUCTPART;
				}
				else{
					throw new Exception("检入图纸时，只允许自动创建半成品！");
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
					}
				}
				setPartIBAValues(wtPart,epmdoc);
			}
			}
//	    	else{
//				throw new Exception("检入失败！找不到部件："+wtPart.getNumber()+" 对应的EPM文档");
//			}
	    	
		}else  if (StringUtils.isNotEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
            Debug.P("POST_STORE-------------pmoid----------->"+pmoid);
//            Object object =GenericUtil.getObjectByNumber(wtPart.getNumber());
            wtPart=   PartUtils.getPartByNumber(wtPart.getNumber());
            if(WorkInProgressHelper.isCheckedOut(wtPart)){
            	
			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
				   WCToPMHelper.updatePMPart(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //如果是原材料
					WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //如果是原材料
					WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//如果是客供件
					WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//如果是包装材料
					WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//如果是备品备料
					WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
			  }
			
            }
		}else  if (StringUtils.isNotEmpty(sync)&&eventType.equals(WorkInProgressServiceEvent.POST_CHECKIN)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
			wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
            Debug.P("POST_CHECKIN-----------pmoid----------->"+pmoid);
			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
				   WCToPMHelper.updatePMPart(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //如果是原材料
					WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //如果是原材料
					WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//如果是客供件
					WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//如果是包装材料
					WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//如果是备品备料
					WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
			  }
		}
		else  if (eventType.equals(PersistenceManagerEvent.PRE_DELETE)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
			 wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
			   Debug.P("PRE_DELETE-----------------pmoid----------->"+pmoid);
			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
				   WCToPMHelper.deletePMPart(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //如果是原材料
					WCToPMHelper.deletePMProduct(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //如果是原材料
					WCToPMHelper.deletePMMaterial(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//如果是客供件
					WCToPMHelper.deleteSupplyment(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//如果是包装材料
					WCToPMHelper.deletePMPackage(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//如果是备品备料
					WCToPMHelper.deletePMJigTools(pmoid, wtPart);
			  }
		}    
		}finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
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

	/*
	 * CORRECT_DATE=, AUDITOR=, Part_Type=半成品, PROCESS_REVIEW_DATE=, Project_NO=, Material=10, DRAWN_BY=, 
	 * AirSpringClassification= , DRAWN_DATE=, Weight=0.0000000000000, CORRECTOR=, STANDARDIZE=, PROCESS_REVIEWER=,
	 * APPROVE_DATE=, ModifiedBy=null, PHASE=S, CyncData=null, CreatorBy=null, Material_Classification= , 
	 * PMRequest=null, STANDARDIZE_DATE=, PMId=null, Project_Name=null, AUDIT_DATE=, Product_NO=, APPROVER=, Material_NO=}
	 */
	public static void setPartIBAValues(WTPart part,EPMDocument cad) throws Exception{

		 IBAUtils partIBA = new IBAUtils(part);
		 IBAUtils cadIBA = new IBAUtils(cad);
		 
		 String Part_Type=cadIBA.getIBAValue(Contants.PART_TYPE);
		 String Material=cadIBA.getIBAValue(Contants.MATERIAL);
		 String AirSpringClassification=cadIBA.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
		 String Weight=cadIBA.getIBAValue(Contants.WEIGHT);
		 String PHASE=cadIBA.getIBAValue(Contants.PHASE);
		 String Product_NO=cadIBA.getIBAValue(Contants.PRODUCTNO);
		 String Material_NO=cadIBA.getIBAValue(Contants.MATERIALNO);
		 String Material_Classification=cadIBA.getIBAValue(Contants.MATERIALGROUP);
		 
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
		 Debug.P("----------updateIBAPart-------------------");
	}
	
	
	
	public static void main(String[] args) {
		String str="TXA6-半  成  品";
		System.out.println(str.replaceAll(" ", "").trim());
		System.out.println(str.substring(0,str.indexOf("-")));
	}
}
