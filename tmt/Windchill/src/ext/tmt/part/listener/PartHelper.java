package ext.tmt.part.listener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
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
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAHelper;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.LWCUtil;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.StringUtil;
import ext.tmt.utils.UserDefQueryUtil;
import ext.tmt.utils.WindchillUtil;


import wt.change2.WTChangeRequest2;
import wt.content.ApplicationData;
import wt.content.ContentServerHelper;
import wt.csm.navigation.ClassificationNode;
import wt.epm.EPMDocument;
import wt.fc.Identified;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.iba.definition.StringDefinition;
import wt.iba.value.ReferenceValue;
import wt.iba.value.StringValue;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
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

public class PartHelper implements RemoteAccess,Serializable {
	
	private static final long serialVersionUID = 2304592616754675533L; 
	
	private static final String MATER_NO="Material_NO";

	/** 
	 * ����WTPart�¼�
	 * @param target
	 * @param eventType
	 * @throws Exception
	 */
	public static void listenerWTPart(WTPart wtPart, String eventType)
			throws Exception {
		Debug.P("�¼�����---->" + eventType);
		Debug.P("wtPart----->"+wtPart.getNumber());
		String newNumber="";
		String partNumber="";
		String partType="";
		String types="";
//		 if (eventType.equals(PersistenceManagerEvent.POST_DELETE)) {
//			 //IBAUtils iba = new IBAUtils(wtPart);
//				Debug.P("Post_DELETE--------------wtPart----------->" +wtPart+"---iba---->");
//					WCToPMHelper.delatePMDoc("wtpart", WTPart.class);
//			}else{
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
		Folder docFolder=  wt.folder.FolderHelper.service.getFolder(wtPart);
		Debug.P("partFolder---->"+docFolder);
        if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
        	if(docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")){
        		throw new Exception("�������㲿������/�ƶ����������ļ����£�������ָ���ļ���");
        	}
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
			    	Debug.P("--888--->wt.part.WTPart-->>>PartType:"+partType);
			    	epmdoc=EPMDocUtil.getActiveEPMDocument(wtPart);
			    	Debug.P("1-->"+epmdoc);
			    	if(epmdoc==null){
			    		List<Persistable> persistables=EPMDocUtil.getEPMDocumentByIBA(MATER_NO, wtPart.getNumber());
			    		//��������������ʱ�������һ������
			    		epmdoc=getLastModifierObject(persistables);
			    		//epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
			    	}
			    	Debug.P("211-->"+epmdoc);
			    	if(epmdoc!=null){
			    		 epmIba = new IBAUtils(epmdoc);
			    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
			    	}
			    	if(StringUtils.isNotEmpty(epmPartType)){
			    		epmPartType=epmPartType.replaceAll(" ", "").trim();
			    	}
			    	Debug.P(epmPartType);
			    	if(StringUtils.isNotEmpty(epmPartType)){
						if(epmPartType.equals("���Ʒ")){
							types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
						}
						else if(epmPartType.equals("��Ʒ")){
							//wtPart.setEndItem(true);
							types="wt.part.WTPart|"+Contants.PRODUCTPART;
						}
						else{
							throw new Exception("����ͼֽʱ��ֻ��������Ʒ�Ͱ��Ʒ");
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
						if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
							WCToPMHelper.CreatePartToPM( wtPart);
						  }else if(types.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
							  WCToPMHelper.CreatePMProductToPM( wtPart);
						  }
					}else{
						throw new Exception("����ʧ�ܣ�ͼֽ�ϣ�"+wtPart.getNumber()+" �ġ��������͡�ֵΪ��");
					}
			    	partType=DocUtils.getType(wtPart);
			    	Debug.P(partNumber+"------------------->"+partType);
			    	if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
						WCToPMHelper.CreatePartToPM( wtPart);
					  }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
						  WCToPMHelper.CreatePMProductToPM( wtPart);
					  }else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
							WCToPMHelper.CreatePMaterialToPM(wtPart);
					  }else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
							WCToPMHelper.CreateSupplyToPM(wtPart);
					  }else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
							WCToPMHelper.CreatePMPackageToPM(wtPart);
					  }else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
							WCToPMHelper.CreateJigToolPartToPM( wtPart);
					  }
			    }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
				//��Ʒ����=TX+��λ������+��λ��ˮ�롣���з�����Ϊ��Ʒ���ڲ�Ʒ���������Ƶ�ǰ�����ַ����Զ����ݳ�Ʒ���ڲ�Ʒ���ȡ��
					productName=wtPart.getContainerName();
					Debug.P("---999---Contants.PRODUCTPART-->>>PartType:"+partType+"   PartNum:"+wtPart.getNumber()+"   productName:"+productName);
					//�������벿��ʱ�������Ĳ������뺬��TX���޸Ĳ�������
					if(wtPart.getNumber().toUpperCase().contains("TX")){
						WCToPMHelper.CreatePMProductToPM(wtPart);
						return;
					}
					if(!productName.toUpperCase().contains("TX")){
						throw new Exception("��Ʒ:"+productName+"  δ���  TX ǰ׺��");
					}
					prefix=productName.substring(0, productName.indexOf("-"));
					if(prefix.toUpperCase().trim().contains("TX48")||prefix.toUpperCase().trim().contains("TX49")||prefix.toUpperCase().trim().contains("TX426")
							||prefix.toUpperCase().trim().contains("TX113")||prefix.toUpperCase().trim().contains("TX114")||prefix.toUpperCase().trim().contains("TX115")){
						WCToPMHelper.CreatePMProductToPM(wtPart);
						return;
					}
					Debug.P("��Ʒǰ׺----��"+prefix);
					if(prefix.toUpperCase().trim().contains("TX111")){
						int i=9000;
						do{
							partNumber=prefix+StringUtil.int2String(i,4);
							if(PartUtil.getPartByNumber(partNumber)==null){
								newNumber=partNumber;
								break;
							}
							i++;
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
					Debug.P("----999------>>part:"+wtPart.getName()+"  partNum"+wtPart.getNumber());
					WCToPMHelper.CreatePMProductToPM(wtPart);
					Debug.P("---999-->>>>CreatePMProductToPM  Success!!");
					
				} else //����ǰ��Ʒ
				if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){
					Debug.P("--1000--Contants.SEMIFINISHEDPRODUCT-->>>PartType:"+partType);
					if(wtPart.isEndItem()){
						throw new Exception("���������ǰ��Ʒ���뽫���Ƿ�Ϊ��Ʒ����ֵ����Ϊ���񡱣�");
					} 
					String isKHpart="";//�ջɲ�������
					isKHpart=iba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
					Debug.P("WTPart -->"+isKHpart);
					//��������ϵ� �ջɲ������� ֵΪ�գ���Ӳ���������EPMDocument�ϻ�ȡ
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
				}else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
					 Debug.P("CreatePMaterialToPM-->");	
					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreatePMaterialToPM(wtPart);
				}else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
					 Debug.P("CreateSupplyToPM-->");	
					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreateSupplyToPM(wtPart);
				}else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
					 Debug.P("CreatePMPackageToPM-->");	
					 wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreatePMPackageToPM(wtPart);
				}else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
					Debug.P("CreateJigToolPartToPM--->");
					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
					WCToPMHelper.CreateJigToolPartToPM(wtPart);
				}
			 
		}else  if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_MODIFY)) {
			Debug.P("partType----->"+partType); 
			if(docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")){
        		throw new Exception("�������㲿������/�ƶ����������ļ����£�������ָ���ļ���");
        	}
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
				if(epmPartType.equals("���Ʒ")){
					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
				}
				else if(epmPartType.equals("��Ʒ")){
					types="wt.part.WTPart|"+Contants.PRODUCTPART;
				}
				else{
					throw new Exception("����ͼֽʱ��ֻ�����Զ��������Ʒ��");
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
	    	
	    	if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
				WCToPMHelper.CreatePartToPM( wtPart);
			  }else if(types.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
				  WCToPMHelper.CreatePMProductToPM( wtPart);
			  }
			 }
//	    	else{
//				throw new Exception("����ʧ�ܣ��Ҳ���������"+wtPart.getNumber()+" ��Ӧ��EPM�ĵ�");
//			}
			partType=DocUtils.getType(wtPart);
	    	Debug.P(types);
	    	Debug.P(partType);
			if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
				WCToPMHelper.CreatePartToPM( wtPart);
			  }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
				  WCToPMHelper.CreatePMProductToPM( wtPart);
			  }else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
					WCToPMHelper.CreatePMaterialToPM(wtPart);
			  }else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
					WCToPMHelper.CreateSupplyToPM(wtPart);
			  }else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
					WCToPMHelper.CreatePMPackageToPM(wtPart);
			  }else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
					WCToPMHelper.CreateJigToolPartToPM( wtPart);
			  }
			if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
				WCToPMHelper.CreatePartToPM( wtPart);
			  }else if(types.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
				  WCToPMHelper.CreatePMProductToPM( wtPart);
			  }else if(types.contains(Contants.MATERIAL)){ //�����ԭ����
					WCToPMHelper.CreatePMaterialToPM(wtPart);
			  }else if(types.contains(Contants.SUPPLYMENT)){//����ǿ͹���
					WCToPMHelper.CreateSupplyToPM(wtPart);
			  }else if(types.contains(Contants.PACKINGPART)){//����ǰ�װ����
					WCToPMHelper.CreatePMPackageToPM(wtPart);
			  }else if(types.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
					WCToPMHelper.CreateJigToolPartToPM( wtPart);
			  }
			
		}else  if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)) {
			if(docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")){
        		throw new Exception("�������㲿������/�ƶ����������ļ����£�������ָ���ļ���");
        	}
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
				if(epmPartType.equals("���Ʒ")){
					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
				}
				else if(epmPartType.equals("��Ʒ")){
					types="wt.part.WTPart|"+Contants.PRODUCTPART;
				}
				else{
					throw new Exception("����ͼֽʱ��ֻ�����Զ��������Ʒ��");
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
//				throw new Exception("����ʧ�ܣ��Ҳ���������"+wtPart.getNumber()+" ��Ӧ��EPM�ĵ�");
//			}
			partType=DocUtils.getType(wtPart);
	    	Debug.P(partType);
			if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
				WCToPMHelper.CreatePartToPM( wtPart);
			  }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
				  WCToPMHelper.CreatePMProductToPM( wtPart);
			  }else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
					WCToPMHelper.CreatePMaterialToPM(wtPart);
			  }else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
					WCToPMHelper.CreateSupplyToPM(wtPart);
			  }else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
					WCToPMHelper.CreatePMPackageToPM(wtPart);
			  }else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
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
				if(epmPartType.equals("���Ʒ")){
					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
				}
				else if(epmPartType.equals("��Ʒ")){
					//wtPart.setEndItem(true);
					types="wt.part.WTPart|"+Contants.PRODUCTPART;
				}
				else{
					throw new Exception("����ͼֽʱ��ֻ�����Զ��������Ʒ��");
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
//				throw new Exception("����ʧ�ܣ��Ҳ���������"+wtPart.getNumber()+" ��Ӧ��EPM�ĵ�");
//			}
	    	
		}else  if (StringUtils.isNotEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
            Debug.P("POST_STORE-------------pmoid----------->"+pmoid);
//            Object object =GenericUtil.getObjectByNumber(wtPart.getNumber());
            wtPart=   PartUtils.getPartByNumber(wtPart.getNumber());
            if(WorkInProgressHelper.isCheckedOut(wtPart)){
            	
			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
				   WCToPMHelper.updatePMPart(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //�����ԭ����
					WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //�����ԭ����
					WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
					WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
					WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
					WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
			  }
			
            }
		}else  if (StringUtils.isNotEmpty(sync)&&(eventType.equals(WorkInProgressServiceEvent.POST_CHECKIN)
				  ||eventType.equals(PersistenceManagerEvent.POST_MODIFY))) {
			  String pmoid = (String) LWCUtil.getValue(wtPart, Contants.PMID);
			  wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
			  EPMDocument epmdoc_rel=EPMDocUtil.getActiveEPMDocument(wtPart);
			  if(epmdoc_rel!=null){//��ֵ��������
				 setPartIBAValues(wtPart, epmdoc_rel);
			 }
            Debug.P("POST_CHECKIN-----------pmoid----------->"+pmoid);
			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
				   WCToPMHelper.updatePMPart(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //�����ԭ����
					WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //�����ԭ����
					WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
					WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
					WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
					WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
			  }
		}
		else  if (eventType.equals(PersistenceManagerEvent.PRE_DELETE)) {
			String pmoid = iba.getIBAValue(Contants.PMID);
			 wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
			   Debug.P("--2004--PRE_DELETE-----------------pmoid----------->"+pmoid);
               List bomList =new ArrayList();
               List docList =new ArrayList();
               List epmList =new ArrayList();
               bomList = PartUtil.queryPrentPartsByParts(wtPart);
               epmList=PartUtil.getEPMDocByPart(wtPart);
               if(bomList.size()>0){
            	   throw new Exception("������"+wtPart.getNumber()+"���ڹ���������������ɾ����");
               }
               if(epmList.size()>0){
            	   throw new Exception("������"+wtPart.getNumber()+"���ڹ���ͼֽ��������ɾ����");
               }
//			   WCToPMHelper.updatePMDocument(pmoid);
			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
				   WCToPMHelper.deletePMPart(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //�����ԭ����
					WCToPMHelper.deletePMProduct(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //�����ԭ����
					WCToPMHelper.deletePMMaterial(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
					WCToPMHelper.deleteSupplyment(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
					WCToPMHelper.deletePMPackage(pmoid, wtPart);
			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
					WCToPMHelper.deletePMJigTools(pmoid, wtPart);
			  }
		}   
		}catch(Exception e){
			throw new Exception("��������/ͬ����������ϵ����Ա"+e.getMessage());
		}finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
//	}
	}
	
	public static void listenerWTPart1(WTPart wtPart, String eventType)
			throws Exception {
		Debug.P("�¼�����---->" + eventType);
		Debug.P("wtPart----->"+wtPart.getNumber());
		String newNumber="";
		String partNumber="";
		String partType="";
		String types="";
		boolean flag = true;
		flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
		partType=DocUtils.getType(wtPart);
		Folder docFolder=  wt.folder.FolderHelper.service.getFolder(wtPart);
		Debug.P("partFolder---->"+docFolder);
		Debug.P("partType---->"+partType);
		Debug.P("partVersion---->"+PartUtil.getVersion(wtPart));
		if (eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			IBAUtils iba = new IBAUtils(wtPart);
			IBAUtils epmIba = null;
			Debug.P("ibautils--->"+iba);
			String sync=iba.getIBAValue(Contants.CYNCDATA);
			String pmoids = iba.getIBAValue(Contants.PMID);
			Debug.P("sync--->"+sync);
			Debug.P("pmoids--->"+pmoids);
			Debug.P("eventType---------------->"+eventType);
			
		}else  if (eventType.equals(PersistenceManagerEvent.POST_MODIFY)) {
			IBAUtils iba = new IBAUtils(wtPart);
			IBAUtils epmIba = null;
			Debug.P("ibautils--->"+iba);
			String sync=iba.getIBAValue(Contants.CYNCDATA);
			String pmoids = iba.getIBAValue(Contants.PMID);
			Debug.P("sync--->"+sync);
			Debug.P("pmoids--->"+pmoids);
			Debug.P("eventType---------------->"+eventType);
		}else  if (eventType.equals(PersistenceManagerEvent.UPDATE)) {
			
//			Debug.P("sync--->"+IBAHelper.getIBAValue(wtPart, Contants.MATERIALGROUP));
//			Debug.P("pmoids--->"+IBAHelper.getIBAValue(wtPart, Contants.PHASE));
			Debug.P("eventType---------------->"+eventType);
		}else  if (eventType.equals(PersistenceManagerEvent.POST_STORE)) {
			IBAUtils iba = new IBAUtils(wtPart);
			IBAUtils epmIba = null;
			Debug.P("ibautils--->"+iba);
			String sync=iba.getIBAValue(Contants.CYNCDATA);
			String pmoids = iba.getIBAValue(Contants.PMID);
			Debug.P("sync--->"+sync);
			Debug.P("pmoids--->"+pmoids);
			Debug.P("eventType---------------->"+eventType);
		}else  if ((eventType.equals(WorkInProgressServiceEvent.POST_CHECKIN)
				  ||eventType.equals(PersistenceManagerEvent.POST_MODIFY))) {
			IBAUtils iba = new IBAUtils(wtPart);
			IBAUtils epmIba = null;
			Debug.P("ibautils--->"+iba);
			String sync=iba.getIBAValue(Contants.CYNCDATA);
			String pmoids = iba.getIBAValue(Contants.PMID);
			Debug.P("sync--->"+sync);
			Debug.P("pmoids--->"+pmoids);
			Debug.P("eventType---------------->"+eventType);
		}else  if (eventType.equals(PersistenceManagerEvent.PRE_DELETE)) {
			IBAUtils iba = new IBAUtils(wtPart);
			IBAUtils epmIba = null;
			Debug.P("ibautils--->"+iba);
			String sync=iba.getIBAValue(Contants.CYNCDATA);
			String pmoids = iba.getIBAValue(Contants.PMID);
			Debug.P("sync--->"+sync);
			Debug.P("pmoids--->"+pmoids);
			WCToPMHelper.updatePMDocument(pmoids);
			Debug.P("eventType---------------->"+eventType);
		}else  if (eventType.equals(PersistenceManagerEvent.POST_DELETE)) {
			IBAUtils iba = new IBAUtils(wtPart);
			IBAUtils epmIba = null;
			Debug.P("ibautils--->"+iba);
			String sync=iba.getIBAValue(Contants.CYNCDATA);
			String pmoids = iba.getIBAValue(Contants.PMID);
			WCToPMHelper.delatePMDoc("wtpart", WTPart.class);
			Debug.P("sync--->"+sync);
			Debug.P("pmoids--->"+pmoids);
			Debug.P("eventType---------------->"+eventType);
		}
		
		
//		IBAUtils iba = new IBAUtils(wtPart);
//		IBAUtils epmIba = null;
//		Debug.P("ibautils--->"+iba);
//		String sync=iba.getIBAValue(Contants.CYNCDATA);
//		String pmoids = iba.getIBAValue(Contants.PMID);
//		Debug.P("sync--->"+sync);
//		Debug.P("pmoids--->"+pmoids);
//		Debug.P("eventType---------------->"+eventType);
//		Folder docFolder=  wt.folder.FolderHelper.service.getFolder(wtPart);
//		Debug.P("partFolder---->"+docFolder);
//        if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
//        	if(docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")){
//        		throw new Exception("�������㲿������/�ƶ����������ļ����£�������ָ���ļ���");
//        	}
//			   EPMDocument epmdoc = null;
//			   String productName="";	
//			   String prefix="";
//			   partNumber=wtPart.getNumber();
//			   if(StringUtils.isNotEmpty(partType)){
//				   partType=partType.replaceAll(" ", "").trim();
//			   }
//			   Debug.P(partNumber+"------------------->"+partType+" event--->"+eventType);
//			    String epmPartType="";
//			    if(partType.equals("wt.part.WTPart")){
//			    	Debug.P("--888--->wt.part.WTPart-->>>PartType:"+partType);
//			    	epmdoc=EPMDocUtil.getActiveEPMDocument(wtPart);
//			    	Debug.P("1-->"+epmdoc);
//			    	if(epmdoc==null){
//			    		List<Persistable> persistables=EPMDocUtil.getEPMDocumentByIBA(MATER_NO, wtPart.getNumber());
//			    		//��������������ʱ�������һ������
//			    		epmdoc=getLastModifierObject(persistables);
////			    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
//			    	}
//			    	Debug.P("211-->"+epmdoc);
//			    	if(epmdoc!=null){
//			    		 epmIba = new IBAUtils(epmdoc);
//			    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
//			    	}
//			    	if(StringUtils.isNotEmpty(epmPartType)){
//			    		epmPartType=epmPartType.replaceAll(" ", "").trim();
//			    	}
//			    	Debug.P(epmPartType);
//			    	if(StringUtils.isNotEmpty(epmPartType)){
//						if(epmPartType.equals("���Ʒ")){
//							types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
//						}
//						else if(epmPartType.equals("��Ʒ")){
//							//wtPart.setEndItem(true);
//							types="wt.part.WTPart|"+Contants.PRODUCTPART;
//						}
//						else{
//							throw new Exception("����ͼֽʱ��ֻ��������Ʒ�Ͱ��Ʒ");
//						}
//						if(types.contains("Product")){
//							Debug.P(types);
//							TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
//							wtPart.setPartType(PartType.getPartTypeDefault());
//							wtPart.setTypeDefinitionReference(typeDefinitionRef);
//							
//							if (!PersistenceHelper.isPersistent(wtPart)) {
//								wtPart = (WTPart) PersistenceHelper.manager.store(wtPart);
//								wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
//							}
//						}
//						setPartIBAValues(wtPart,epmdoc);
//						if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
//							WCToPMHelper.CreatePartToPM( wtPart);
//						  }else if(types.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
//							  WCToPMHelper.CreatePMProductToPM( wtPart);
//						  }
//					}else{
//						throw new Exception("����ʧ�ܣ�ͼֽ�ϣ�"+wtPart.getNumber()+" �ġ��������͡�ֵΪ��");
//					}
//			    	partType=DocUtils.getType(wtPart);
//			    	Debug.P(partNumber+"------------------->"+partType);
//			    	if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
//						WCToPMHelper.CreatePartToPM( wtPart);
//					  }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
//						  WCToPMHelper.CreatePMProductToPM( wtPart);
//					  }else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
//							WCToPMHelper.CreatePMaterialToPM(wtPart);
//					  }else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//							WCToPMHelper.CreateSupplyToPM(wtPart);
//					  }else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
//							WCToPMHelper.CreatePMPackageToPM(wtPart);
//					  }else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//							WCToPMHelper.CreateJigToolPartToPM( wtPart);
//					  }
//			    }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
//				//��Ʒ����=TX+��λ������+��λ��ˮ�롣���з�����Ϊ��Ʒ���ڲ�Ʒ���������Ƶ�ǰ�����ַ����Զ����ݳ�Ʒ���ڲ�Ʒ���ȡ��
//					productName=wtPart.getContainerName();
//					Debug.P("---999---Contants.PRODUCTPART-->>>PartType:"+partType+"   PartNum:"+wtPart.getNumber()+"   productName:"+productName);
//					//�������벿��ʱ�������Ĳ������뺬��TX���޸Ĳ�������
//					if(wtPart.getNumber().toUpperCase().contains("TX")){
//						WCToPMHelper.CreatePMProductToPM(wtPart);
//						return;
//					}
//					if(!productName.toUpperCase().contains("TX")){
//						throw new Exception("��Ʒ:"+productName+"  δ���  TX ǰ׺��");
//					}
//					prefix=productName.substring(0, productName.indexOf("-"));
//					if(prefix.toUpperCase().trim().contains("TX48")||prefix.toUpperCase().trim().contains("TX49")||prefix.toUpperCase().trim().contains("TX426")
//							||prefix.toUpperCase().trim().contains("TX113")||prefix.toUpperCase().trim().contains("TX114")||prefix.toUpperCase().trim().contains("TX115")){
//						WCToPMHelper.CreatePMProductToPM(wtPart);
//						return;
//					}
//					Debug.P("��Ʒǰ׺----��"+prefix);
//					if(prefix.toUpperCase().trim().contains("TX111")){
//						int i=9000;
//						do{
//							partNumber=prefix+StringUtil.int2String(i,4);
//							if(PartUtil.getPartByNumber(partNumber)==null){
//								newNumber=partNumber;
//								break;
//							}
//							i++;
//						}while(i<9999);
//						changePartNumber(wtPart,newNumber);
//					}else{
//						int i= 0;
//						do {  
//							if(prefix.toUpperCase().trim().equals("TXA6")||prefix.toUpperCase().trim().equals("TXA7")||prefix.toUpperCase().trim().equals("TXA8")){
//								partNumber=prefix+StringUtil.int2String(i,5);
//							}else {
//								partNumber=prefix+StringUtil.int2String(i,4);
//							}
//							if(PartUtil.getPartByNumber(partNumber)==null){
//								newNumber=partNumber;
//								break;
//							}
//							i++;
//						} while (i < 100000);
//						changePartNumber(wtPart,newNumber);
//					}
//					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//					Debug.P("----999------>>part:"+wtPart.getName()+"  partNum"+wtPart.getNumber());
//					WCToPMHelper.CreatePMProductToPM(wtPart);
//					Debug.P("---999-->>>>CreatePMProductToPM  Success!!");
//					
//				} else //����ǰ��Ʒ
//				if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){
//					Debug.P("--1000--Contants.SEMIFINISHEDPRODUCT-->>>PartType:"+partType);
//					if(wtPart.isEndItem()){
//						throw new Exception("���������ǰ��Ʒ���뽫���Ƿ�Ϊ��Ʒ����ֵ����Ϊ���񡱣�");
//					} 
//					String isKHpart="";//�ջɲ�������
//					isKHpart=iba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
//					Debug.P("WTPart -->"+isKHpart);
//					//��������ϵ� �ջɲ������� ֵΪ�գ���Ӳ���������EPMDocument�ϻ�ȡ
//					if(StringUtils.isEmpty(isKHpart)&&epmdoc!=null){
//						IBAUtils epmIBA = new IBAUtils(epmdoc);
//						isKHpart=epmIBA.getIBAValue(Contants.AIRSPRINGCLASSIFICATION);
//						Debug.P("EPMDocument -->"+isKHpart);
//					 }
//					if(StringUtils.isNotEmpty(isKHpart)){
//						int i= 0;
//						do {
//							partNumber=isKHpart+StringUtil.int2String(i,4);
//							if(PartUtil.getPartByNumber(partNumber)==null){
//								newNumber=partNumber;
//								break;
//							}
//							i++;
//						} while (i < 100000);
//						changePartNumber(wtPart,newNumber);
//						wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//					}
//				     Debug.P("CreatePartToPM-->");	
//					WCToPMHelper.CreatePartToPM(wtPart);
//				}else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
//					 Debug.P("CreatePMaterialToPM-->");	
//					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//					WCToPMHelper.CreatePMaterialToPM(wtPart);
//				}else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//					 Debug.P("CreateSupplyToPM-->");	
//					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//					WCToPMHelper.CreateSupplyToPM(wtPart);
//				}else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
//					 Debug.P("CreatePMPackageToPM-->");	
//					 wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//					WCToPMHelper.CreatePMPackageToPM(wtPart);
//				}else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//					Debug.P("CreateJigToolPartToPM--->");
//					wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//					WCToPMHelper.CreateJigToolPartToPM(wtPart);
//				}
//			 
//		}else  if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_MODIFY)) {
//			Debug.P("partType----->"+partType); 
//			if(docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")){
//        		throw new Exception("�������㲿������/�ƶ����������ļ����£�������ָ���ļ���");
//        	}
//			if(partType.equals("wt.part.WTPart")){
//			EPMDocument epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
//			String epmPartType="";
//	    	Debug.P("1-->"+epmdoc);
//	    	if(epmdoc==null){
//	    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
//	    	}
//	    	Debug.P("2-->"+epmdoc);
//			if(epmdoc!=null){
//	    		 epmIba = new IBAUtils(epmdoc);
//	    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
//	    	}
//	    	if(StringUtils.isNotEmpty(epmPartType)){
//	    		epmPartType=epmPartType.replaceAll(" ", "").trim();
//	    	}
//	    	Debug.P(epmPartType);
//	    	Debug.P(StringUtils.isNotEmpty(epmPartType));
//	    	if(StringUtils.isNotEmpty(epmPartType)){
//				if(epmPartType.equals("���Ʒ")){
//					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
//				}
//				else if(epmPartType.equals("��Ʒ")){
//					types="wt.part.WTPart|"+Contants.PRODUCTPART;
//				}
//				else{
//					throw new Exception("����ͼֽʱ��ֻ�����Զ��������Ʒ��");
//				}
//				Debug.P(types);
//				if(types.contains("Product")){
//					TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
//					wtPart.setPartType(PartType.getPartTypeDefault());
//					wtPart.setTypeDefinitionReference(typeDefinitionRef);
//					if (!PersistenceHelper.isPersistent(wtPart)) {
//						wtPart = (WTPart) PersistenceHelper.manager.save(wtPart);
//						wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
//					}
//				}
//				setPartIBAValues(wtPart,epmdoc);
//				
//			}
//	    	
//	    	if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
//				WCToPMHelper.CreatePartToPM( wtPart);
//			  }else if(types.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
//				  WCToPMHelper.CreatePMProductToPM( wtPart);
//			  }
//			 }
////	    	else{
////				throw new Exception("����ʧ�ܣ��Ҳ���������"+wtPart.getNumber()+" ��Ӧ��EPM�ĵ�");
////			}
//			partType=DocUtils.getType(wtPart);
//	    	Debug.P(types);
//	    	Debug.P(partType);
//			if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
//				WCToPMHelper.CreatePartToPM( wtPart);
//			  }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
//				  WCToPMHelper.CreatePMProductToPM( wtPart);
//			  }else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
//					WCToPMHelper.CreatePMaterialToPM(wtPart);
//			  }else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//					WCToPMHelper.CreateSupplyToPM(wtPart);
//			  }else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
//					WCToPMHelper.CreatePMPackageToPM(wtPart);
//			  }else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//					WCToPMHelper.CreateJigToolPartToPM( wtPart);
//			  }
//			if(types.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
//				WCToPMHelper.CreatePartToPM( wtPart);
//			  }else if(types.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
//				  WCToPMHelper.CreatePMProductToPM( wtPart);
//			  }else if(types.contains(Contants.MATERIAL)){ //�����ԭ����
//					WCToPMHelper.CreatePMaterialToPM(wtPart);
//			  }else if(types.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//					WCToPMHelper.CreateSupplyToPM(wtPart);
//			  }else if(types.contains(Contants.PACKINGPART)){//����ǰ�װ����
//					WCToPMHelper.CreatePMPackageToPM(wtPart);
//			  }else if(types.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//					WCToPMHelper.CreateJigToolPartToPM( wtPart);
//			  }
//			
//		}else  if (StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)) {
//			if(docFolder.getFolderPath().toUpperCase().trim().endsWith("/DEFAULT")){
//        		throw new Exception("�������㲿������/�ƶ����������ļ����£�������ָ���ļ���");
//        	}
//			Debug.P("StringUtils.isEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)------------------");
//			Debug.P("partType----->"+partType); 
//			if(partType.equals("wt.part.WTPart")){
//			EPMDocument epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
//			String epmPartType="";
//	    	Debug.P("1-->"+epmdoc);
//	    	if(epmdoc==null){
//	    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
//	    	}
//	    	Debug.P("2-->"+epmdoc);
//			if(epmdoc!=null){
//	    		 epmIba = new IBAUtils(epmdoc);
//	    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
//	    	}
//	    	if(StringUtils.isNotEmpty(epmPartType)){
//	    		epmPartType=epmPartType.replaceAll(" ", "").trim();
//	    	}
//	    	Debug.P(epmPartType);
//	    	Debug.P(StringUtils.isNotEmpty(epmPartType));
//	    	if(StringUtils.isNotEmpty(epmPartType)){
//				if(epmPartType.equals("���Ʒ")){
//					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
//				}
//				else if(epmPartType.equals("��Ʒ")){
//					types="wt.part.WTPart|"+Contants.PRODUCTPART;
//				}
//				else{
//					throw new Exception("����ͼֽʱ��ֻ�����Զ��������Ʒ��");
//				}
//				Debug.P(types);
//				if(types.contains("Product")){
//					TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
//					wtPart.setPartType(PartType.getPartTypeDefault());
//					wtPart.setTypeDefinitionReference(typeDefinitionRef);
//					if (!PersistenceHelper.isPersistent(wtPart)) {
//						wtPart = (WTPart) PersistenceHelper.manager.save(wtPart);
//						wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
//					}
//				}
//				setPartIBAValues(wtPart,epmdoc);
//			}
//			 }
////	    	else{
////				throw new Exception("����ʧ�ܣ��Ҳ���������"+wtPart.getNumber()+" ��Ӧ��EPM�ĵ�");
////			}
//			partType=DocUtils.getType(wtPart);
//	    	Debug.P(partType);
//			if(partType.contains(Contants.SEMIFINISHEDPRODUCT)){//������ǳ�Ʒ
//				WCToPMHelper.CreatePartToPM( wtPart);
//			  }else if(partType.contains(Contants.PRODUCTPART)){ //����ǳ�Ʒ
//				  WCToPMHelper.CreatePMProductToPM( wtPart);
//			  }else if(partType.contains(Contants.MATERIAL)){ //�����ԭ����
//					WCToPMHelper.CreatePMaterialToPM(wtPart);
//			  }else if(partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//					WCToPMHelper.CreateSupplyToPM(wtPart);
//			  }else if(partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
//					WCToPMHelper.CreatePMPackageToPM(wtPart);
//			  }else if(partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//					WCToPMHelper.CreateJigToolPartToPM( wtPart);
//			  }
//		} if (StringUtils.isNotEmpty(sync)&&eventType.equals(PersistenceManagerEvent.UPDATE)) {
//			if(partType.equals("wt.part.WTPart")){
//			EPMDocument epmdoc = EPMDocUtil.getActiveEPMDocument(wtPart);
//			String epmPartType="";
//	    	Debug.P("1-->"+epmdoc);
//	    	if(epmdoc==null){
//	    		epmdoc=EPMDocUtil.getEPMDocByNumber(wtPart.getNumber());
//	    	}
//	    	Debug.P("2-->"+epmdoc);
//			if(epmdoc!=null){
//	    		 epmIba = new IBAUtils(epmdoc);
//	    		epmPartType=epmIba.getIBAValue(Contants.PART_TYPE);
//	    	}
//	    	if(StringUtils.isNotEmpty(epmPartType)){
//	    		epmPartType=epmPartType.replaceAll(" ", "").trim();
//	    	}
//	    	Debug.P(epmPartType);
//	    	if(StringUtils.isNotEmpty(epmPartType)){
//				if(epmPartType.equals("���Ʒ")){
//					types="wt.part.WTPart|"+Contants.SEMIFINISHEDPRODUCT;
//				}
//				else if(epmPartType.equals("��Ʒ")){
//					//wtPart.setEndItem(true);
//					types="wt.part.WTPart|"+Contants.PRODUCTPART;
//				}
//				else{
//					throw new Exception("����ͼֽʱ��ֻ�����Զ��������Ʒ��");
//				}
//				Debug.P(types);
//				if(types.contains("Product")){
//					partType=DocUtils.getDfmDocumentType(types).getIntHid();
//					Debug.P(partType);
//					TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(types);
//					wtPart.setPartType(PartType.getPartTypeDefault());
//					wtPart.setTypeDefinitionReference(typeDefinitionRef);
//					if (!PersistenceHelper.isPersistent(wtPart)) {
//						wtPart = (WTPart) PersistenceHelper.manager.save(wtPart);
//						wtPart = (WTPart) PersistenceHelper.manager.refresh(wtPart);
//					}
//				}
//				setPartIBAValues(wtPart,epmdoc);
//			}
//			}
////	    	else{
////				throw new Exception("����ʧ�ܣ��Ҳ���������"+wtPart.getNumber()+" ��Ӧ��EPM�ĵ�");
////			}
//	    	
//		}else  if (StringUtils.isNotEmpty(sync)&&eventType.equals(PersistenceManagerEvent.POST_STORE)) {
//			String pmoid = iba.getIBAValue(Contants.PMID);
//            Debug.P("POST_STORE-------------pmoid----------->"+pmoid);
////            Object object =GenericUtil.getObjectByNumber(wtPart.getNumber());
//            wtPart=   PartUtils.getPartByNumber(wtPart.getNumber());
//            if(WorkInProgressHelper.isCheckedOut(wtPart)){
//            	
//			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
//				   WCToPMHelper.updatePMPart(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //�����ԭ����
//					WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //�����ԭ����
//					WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//					WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
//					WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//					WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
//			  }
//			
//            }
//		}else  if (StringUtils.isNotEmpty(sync)&&(eventType.equals(WorkInProgressServiceEvent.POST_CHECKIN)
//				  ||eventType.equals(PersistenceManagerEvent.POST_MODIFY))) {
//			  String pmoid = (String) LWCUtil.getValue(wtPart, Contants.PMID);
//			  wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//			  EPMDocument epmdoc_rel=EPMDocUtil.getActiveEPMDocument(wtPart);
//			  if(epmdoc_rel!=null){//��ֵ��������
//				 setPartIBAValues(wtPart, epmdoc_rel);
//			 }
//            Debug.P("POST_CHECKIN-----------pmoid----------->"+pmoid);
//			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
//				   WCToPMHelper.updatePMPart(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //�����ԭ����
//					WCToPMHelper.updatePMProductToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //�����ԭ����
//					WCToPMHelper.updatePMaterialToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//					WCToPMHelper.updateSupplyToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
//					WCToPMHelper.updatePMPackageToPM(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//					WCToPMHelper.UpdateJigToolPartToPM(pmoid, wtPart);
//			  }
//		}
//		else  if (eventType.equals(PersistenceManagerEvent.PRE_DELETE)) {
//			String pmoid = iba.getIBAValue(Contants.PMID);
//			 wtPart =PartUtil.getPartByNumber(wtPart.getNumber());
//			   Debug.P("--2004--PRE_DELETE-----------------pmoid----------->"+pmoid);
//			  if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SEMIFINISHEDPRODUCT)){
//				   WCToPMHelper.deletePMPart(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PRODUCTPART)){ //�����ԭ����
//					WCToPMHelper.deletePMProduct(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.MATERIAL)){ //�����ԭ����
//					WCToPMHelper.deletePMMaterial(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.SUPPLYMENT)){//����ǿ͹���
//					WCToPMHelper.deleteSupplyment(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.PACKINGPART)){//����ǰ�װ����
//					WCToPMHelper.deletePMPackage(pmoid, wtPart);
//			  }else if(StringUtils.isNotEmpty(pmoid)&&partType.contains(Contants.TOOLPART)){//����Ǳ�Ʒ����
//					WCToPMHelper.deletePMJigTools(pmoid, wtPart);
//			  }
//		}    
		}catch(Exception e){
			throw new Exception("��������/ͬ����������ϵ����Ա"+e.getMessage());
		}finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
	}

	

	/*
	 * ��ѯ�������±���
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
	 * ���ݱ�Ź���,Ϊ��Ʒ�ı���
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
	 * CORRECT_DATE=, AUDITOR=, Part_Type=���Ʒ, PROCESS_REVIEW_DATE=, Project_NO=, Material=10, DRAWN_BY=, 
	 * AirSpringClassification= , DRAWN_DATE=, Weight=0.0000000000000, CORRECTOR=, STANDARDIZE=, PROCESS_REVIEWER=,
	 * APPROVE_DATE=, ModifiedBy=null, PHASE=S, CyncData=null, CreatorBy=null, Material_Classification= , 
	 * PMRequest=null, STANDARDIZE_DATE=, PMId=null, Project_Name=null, AUDIT_DATE=, Product_NO=, APPROVER=, Material_NO=}
	 */
	public static void setPartIBAValues(WTPart part,EPMDocument cad) throws Exception{
         Debug.P("----->>>>setPartIBAValues:"+cad);
		 part=PartUtils.getPartByNumber(part.getNumber());
		 cad=EPMUtil.getEPMDocument(cad.getNumber(), null);
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
		 
		 //����ǩ����Ϣ
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
	
	private static EPMDocument getLastModifierObject(List<Persistable> objects){
		EPMDocument result=null;
		if(objects!=null){
			Debug.P("-------->>>getLastModifierObject(Objects Size:)"+objects.size());
			long cpTime=0L;
			for(int i=0;i<objects.size();i++){
				EPMDocument temp_epm=(EPMDocument) objects.get(i);
				if(result==null){
					cpTime=temp_epm.getModifyTimestamp().getTime();
					result=temp_epm;
				}else{
					long temp_modTime=temp_epm.getModifyTimestamp().getTime();
					if(temp_modTime>cpTime){
						cpTime=temp_modTime;
						result=temp_epm;
					}
				}
			}
		}
	          	return result;
	}
	
	   
   

}
