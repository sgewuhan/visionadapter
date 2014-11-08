package ext.tmt.WC2PM;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import wt.epm.EPMDocument;
import wt.folder.Folder;
import wt.part.WTPart;
import wt.util.WTProperties;

import com.mongodb.WriteResult;
import com.sg.visionadapter.CADDocumentPersistence;
import com.sg.visionadapter.MaterialPersistence;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMCADDocument;
import com.sg.visionadapter.PMMaterial;
import com.sg.visionadapter.PMPart;
import com.sg.visionadapter.PMProduct;
import com.sg.visionadapter.PMSupplyment;
import com.sg.visionadapter.PartPersistence;
import com.sg.visionadapter.ProductPersistence;
import com.sg.visionadapter.SupplymentPersistence;

import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.Utils;

public class WCToPMHelper {
	
	

	//获得PM数据池
	 private static  ModelServiceFactory factory =new ModelServiceFactory();
	 static{
		    try {
				WTProperties wtproperties = WTProperties.getLocalProperties();
				String codebasePath= wtproperties.getProperty("wt.codebase.location");
				codebasePath=codebasePath+File.separator+"visionconf";
				factory.start(codebasePath);
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }    
	
	 /**
	  * Windchill 创建半成品后将半成品的基本属性写入PM的半成品对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
		public static void CreatePartToPM(WTPart wtPart){
		String partOid ="";    //WC部件Oid 
		String partFolderString="";
		Folder partFolder =null;
		String pFolderId="";
		PartPersistence partPersistence=null;         //PM系统中的半成品持久化对象
		String weight ="";
		Debug.P("将Windchill中的半成品插入PM系统的数据库中");
		partOid = wtPart.toString();
		try {
			PMPart pmPart = null;//PM中的半成品           
			partPersistence = factory.get(PartPersistence.class);
			pmPart = partPersistence.newInstance();
			IBAUtils  partiba = new IBAUtils(wtPart);
            Debug.P(partOid);
            partFolderString = wtPart.getFolderPath();
            partFolder=wtPart.getFolderingInfo().getFolder();
            Debug.P(partFolder.toString());
            Debug.P(wtPart.getContainer());
            Debug.P(partFolderString);
            
            wt.fc.ReferenceFactory rf = new wt.fc.ReferenceFactory();
            pFolderId = rf.getReferenceString(partFolder);
            pFolderId=pFolderId.substring(pFolderId.indexOf(":")+1, pFolderId.length());
 		    pmPart.setFolderIdByPLMId(pFolderId);
 		    ObjectId objectId = new ObjectId();
 		    pmPart.set_id(objectId);
            pmPart.setPLMId(partOid);
			pmPart.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmPart.setObjectNumber(wtPart.getNumber());
			pmPart.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmPart.setCreateBy(wtPart.getCreatorName(), wtPart.getCreatorFullName());			  //设置PM部件创建者
			pmPart.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmPart.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmPart.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			//pmPart.setModifiedBy("", wtPart.getModifierFullName());		//设置PM部件修改者
			pmPart.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			 pmPart.setWeight(NumberFormat.getInstance().parse(weight));    
			pmPart.setProductNumber(partiba.getIBAValue(Contants.PRODUCTNO)==null?"":partiba.getIBAValue(Contants.PRODUCTNO));
			Map<String,Object> plmData = new HashMap<String,Object>();
			pmPart.setPLMData(plmData);
			pmPart.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmPart.doInsert();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.PMID, objectId.toString());
				String data = Utils.getDate();
				Debug.P(data);
				partiba.setIBAValue(Contants.CYNCDATA, data);
				partiba.setIBAValue(Contants.PMREQUEST, "create");
				partiba.updateIBAPart(wtPart);
				reloadPermission(objectId.toString());
				Debug.P("create PMPart success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	  * Windchill 创建成品后将成品的基本属性写入PM的成品对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void CreatePMProductToPM(WTPart wtPart){
		String partType ="";   //Windchill中部件的类型
		String partOid ="";    //WC部件Oid 
		String partFolderString="";
		Folder partFolder =null;
		String pFolderId="";
		ProductPersistence productPersistence =null;  //PM系统中的成品持久化对象
		String weight ="";
		Debug.P("将Windchill中的成品插入PM系统的数据库中");
		partType = PartUtil.getType(wtPart);
		partOid = wtPart.toString();
		try {
			PMProduct pmProduct = null;//PM中的成品           
			productPersistence = factory.get(ProductPersistence.class);
			pmProduct = productPersistence.newInstance();
			IBAUtils  partiba = new IBAUtils(wtPart);
           Debug.P(partOid);
           partFolderString = wtPart.getFolderPath();
           partFolder=wtPart.getFolderingInfo().getFolder();
           Debug.P(partFolder.toString());
           Debug.P(wtPart.getContainer());
           Debug.P(partFolderString);
           Debug.P(pFolderId);
           ObjectId objectId =new ObjectId();
           pmProduct.set_id(objectId);
           wt.fc.ReferenceFactory rf = new wt.fc.ReferenceFactory();
           pFolderId = rf.getReferenceString(partFolder);
           pFolderId=pFolderId.substring(pFolderId.indexOf(":")+1, pFolderId.length());
		   Debug.P(pFolderId);
           pmProduct.setFolderIdByPLMId(pFolderId);
           pmProduct.setPLMId(partOid);
           Map<String,Object> plmData = new HashMap<String,Object>();
           pmProduct.setPLMData(plmData);
			pmProduct.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmProduct.setObjectNumber(wtPart.getNumber());
			pmProduct.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmProduct.setCreateBy(wtPart.getCreatorName(), wtPart.getCreatorFullName());			  //设置PM部件创建者
			pmProduct.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmProduct.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmProduct.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			pmProduct.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			pmProduct.setWeight(NumberFormat.getInstance().parse(weight));    
			pmProduct.setFormularNumber(partiba.getIBAValue(Contants.FORMULANO)==null?"":partiba.getIBAValue(Contants.FORMULANO) );
			pmProduct.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmProduct.doInsert();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.PMID, objectId.toString());
				partiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
				partiba.setIBAValue(Contants.PMREQUEST, "create");
				partiba.updateIBAPart(wtPart);
				reloadPermission(objectId.toString());
				Debug.P("create pmproduct success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	  * Windchill 创建原材料后将原材料的基本属性写入PM的半成品对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void CreatePMaterialToPM(WTPart wtPart){
		String partOid ="";    //WC部件Oid 
		String partFolderString="";
		Folder partFolder =null;
		String pFolderId="";
		MaterialPersistence materialPersistence=null;         //PM系统中的原材料持久化对象
		
		String weight ="";
		Debug.P("将Windchill中的半成品插入PM系统的数据库中");
		partOid = wtPart.toString();
		try {
			PMMaterial pmMaterial = null;//PM中的半成品           
			materialPersistence = factory.get(MaterialPersistence.class);
			pmMaterial = materialPersistence.newInstance();
			IBAUtils  partiba = new IBAUtils(wtPart);
           Debug.P(partOid);
           partFolderString = wtPart.getFolderPath();
           partFolder=wtPart.getFolderingInfo().getFolder();
           Debug.P(partFolder.toString());
           Debug.P(wtPart.getContainer());
           Debug.P(partFolderString);
           wt.fc.ReferenceFactory rf = new wt.fc.ReferenceFactory();
           pFolderId = rf.getReferenceString(partFolder);
           pFolderId=pFolderId.substring(pFolderId.indexOf(":")+1, pFolderId.length());
           pmMaterial.setFolderIdByPLMId(pFolderId);
           pmMaterial.setPLMId(partOid);
           Map<String,Object> plmData = new HashMap<String,Object>();
           pmMaterial.setPLMData(plmData);
			pmMaterial.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmMaterial.setObjectNumber(wtPart.getNumber());
			pmMaterial.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmMaterial.setCreateBy(wtPart.getCreatorName(), wtPart.getCreatorFullName());			  //设置PM部件创建者
			pmMaterial.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmMaterial.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmMaterial.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			
			//pmMaterial.setModifiedBy("", wtPart.getModifierFullName());		//设置PM部件修改者
			pmMaterial.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			 pmMaterial.setWeight(NumberFormat.getInstance().parse(weight));    
			pmMaterial.setMaterialGroup(partiba.getIBAValue(Contants.MATERIALGROUP)==null?"":partiba.getIBAValue(Contants.MATERIALGROUP));
            ObjectId objectId =new ObjectId();
			pmMaterial.set_id(objectId);
			pmMaterial.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmMaterial.doInsert();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.PMID, objectId.toString());
				partiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
				partiba.setIBAValue(Contants.PMREQUEST, "create");
				partiba.updateIBAPart(wtPart);
				reloadPermission(objectId.toString());
				Debug.P("create PMMaterial success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	  * Windchill 创建客供件后将客供件的基本属性写入PM的客供件对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void CreateSupplyToPM(WTPart wtPart){
		String partType ="";   //Windchill中部件的类型
		String partOid ="";    //WC部件Oid 
		String partFolderString="";
		Folder partFolder =null;
		String pFolderId="";
		SupplymentPersistence supplymentPersistence=null;         //PM系统中的客供件持久化对象
		
		String weight ="";
		Debug.P("将Windchill中的客供件插入PM系统的数据库中");
		partType = PartUtil.getType(wtPart);
		partOid = wtPart.getPersistInfo().getObjectIdentifier().toString();
		try {
			PMSupplyment pmSupplyment = null;//PM中的半成品           
			supplymentPersistence = factory.get(SupplymentPersistence.class);
			pmSupplyment = supplymentPersistence.newInstance();
			IBAUtils  partiba = new IBAUtils(wtPart);
			
           Debug.P(partOid);
           partFolderString = wtPart.getFolderPath();
           partFolder=wtPart.getFolderingInfo().getFolder();
           Debug.P(partFolder.toString());
           Debug.P(wtPart.getContainer());
           Debug.P(partFolderString);
           wt.fc.ReferenceFactory rf = new wt.fc.ReferenceFactory();
           pFolderId = rf.getReferenceString(partFolder);
           pFolderId=pFolderId.substring(pFolderId.indexOf(":")+1, pFolderId.length());
           pmSupplyment.setFolderIdByPLMId(pFolderId);
           pmSupplyment.setPLMId(partOid);
           Map<String,Object> plmData = new HashMap<String,Object>();
           pmSupplyment.setPLMData(plmData);
			pmSupplyment.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmSupplyment.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmSupplyment.setCreateBy(wtPart.getCreatorName(), wtPart.getCreatorFullName());			  //设置PM部件创建者
			pmSupplyment.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmSupplyment.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmSupplyment.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			
			pmSupplyment.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			 pmSupplyment.setWeight(NumberFormat.getInstance().parse(weight));    
			pmSupplyment.setCustomerName(partiba.getIBAValue(Contants.CLIENTNAME)==null?"":partiba.getIBAValue(Contants.CLIENTNAME));
			pmSupplyment.setMaterialGroup(partiba.getIBAValue(Contants.MATERIALGROUP)==null?"":partiba.getIBAValue(Contants.MATERIALGROUP));
			ObjectId objectId = new ObjectId();
			pmSupplyment.set_id(objectId);
			pmSupplyment.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmSupplyment.doInsert();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.PMID, objectId.toString());
				partiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
				partiba.setIBAValue(Contants.PMREQUEST, "create");
				partiba.updateIBAPart(wtPart);
				reloadPermission(objectId.toString());
				Debug.P("create PMSupplyment success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	  * Windchill 创建图纸后将图纸的基本属性写入PM的cad对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void CreateEPMDocToPM(EPMDocument epmdoc){
		String docOid ="";    //EPMDoc的Oid 
		String docFolderString="";
		Folder docFolder =null;
		String pFolderId="";
		CADDocumentPersistence cadDocPersistence =null;  //PM系统中的图纸持久化对象
		
		String weight ="";
		Debug.P("将Windchill中的EPMDocument插入PM系统的数据库中");
		docOid = epmdoc.toString();
		try {
			PMCADDocument pmcad = null;//PM中的半成品           
			cadDocPersistence = factory.get(CADDocumentPersistence.class);
			pmcad = cadDocPersistence.newInstance();
			IBAUtils  cadiba = new IBAUtils(epmdoc);
           Debug.P(docOid);
           docFolderString = epmdoc.getFolderPath();
           docFolder=epmdoc.getFolderingInfo().getFolder();
           Debug.P(docFolder.toString());
           Debug.P(epmdoc.getContainer());
           Debug.P(docFolderString);
           pFolderId = docFolder.toString();
           Debug.P(docFolder.toString());
           pmcad.setFolderId(new ObjectId(pFolderId));
           pmcad.setPLMId(docOid);
           Map<String,Object> plmData = new HashMap<String,Object>();
           plmData.put("AirSpringClassification", cadiba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION)==null?"":cadiba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION));
           plmData.put("Part_Type", cadiba.getIBAValue(Contants.PART_TYPE)==null?"":cadiba.getIBAValue(Contants.PART_TYPE));
           pmcad.setPLMData(plmData);
			pmcad.setCommonName(epmdoc.getName());                           //设置PM部件名称
			pmcad.setObjectNumber(epmdoc.getNumber());
			pmcad.setStatus(epmdoc.getState().toString().toLowerCase());                   //设置PM部件状态
			pmcad.setCreateBy(epmdoc.getCreatorName(), epmdoc.getCreatorFullName());			  //设置PM部件创建者
			pmcad.setMajorVid(epmdoc.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmcad.setSecondVid(Integer.parseInt(epmdoc.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmcad.setPhase(cadiba.getIBAValue(Contants.PHASE)==null?"":cadiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			
			//pmcad.setModifiedBy("", epmdoc.getModifierFullName());		//设置PM部件修改者
			pmcad.setDrawingNumber(cadiba.getIBAValue(Contants.MATERIALNO)==null?"":cadiba.getIBAValue(Contants.MATERIALNO));
			ObjectId objectId = new ObjectId();
			pmcad.set_id(objectId);
			pmcad.setOwner(epmdoc.getCreatorName());
			WriteResult wresult=pmcad.doInsert();   //执行插入数据库操作
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
                cadiba.setIBAValue(Contants.PMID, objectId.toString());
                cadiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
                cadiba.setIBAValue(Contants.PMREQUEST, "create");
                cadiba.updateIBAPart(epmdoc);
                reloadPermission(objectId.toString());
                Debug.P("create PMCADDocument success");
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updatePMPart(String pmoid,WTPart wtPart){
		String partOid ="";    //WC部件Oid 
		PartPersistence partPersistence=null;         //PM系统中的半成品持久化对象
		String weight ="";
		Debug.P("更新Windchill中的半成品后至PM系统的数据库中");
		partOid = wtPart.toString();
		try {
			PMPart pmPart = null;//PM中的半成品           
			partPersistence = factory.get(PartPersistence.class);
			pmPart = partPersistence.get(new ObjectId(pmoid));
			Debug.P("pmPart --->"+pmPart.getCommonName());
			IBAUtils  partiba = new IBAUtils(wtPart);
            Debug.P(partOid);
			pmPart.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmPart.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmPart.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmPart.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmPart.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			pmPart.setModifiedBy(wtPart.getModifierName(), wtPart.getModifierFullName());		//设置PM部件修改者
			pmPart.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			 pmPart.setWeight(NumberFormat.getInstance().parse(weight));    
			pmPart.setProductNumber(partiba.getIBAValue(Contants.PRODUCTNO)==null?"":partiba.getIBAValue(Contants.PRODUCTNO));
			Map<String,Object> plmData = new HashMap<String,Object>();
			plmData.put("AirSpringClassification", partiba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION)==null?"":partiba.getIBAValue(Contants.AIRSPRINGCLASSIFICATION));
			pmPart.setPLMData(plmData);
			pmPart.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmPart.doUpdate();   //
			String error = wresult.getError();   
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
				partiba.setIBAValue(Contants.PMREQUEST, "update");
				partiba.updateIBAPart(wtPart);
				//reloadPermission(pmoid);
				Debug.P("update PMPart success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	  * Windchill 修改成品后将成品的基本属性写入PM的成品对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void updatePMProductToPM(String pmoid,WTPart wtPart){
		String partOid ="";    //WC部件Oid 
		ProductPersistence productPersistence =null;  //PM系统中的成品持久化对象
		String weight ="";
		Debug.P("更新PM系统的数据库-----------》"+wtPart.getNumber());
		try {
			PMProduct pmProduct = null;//PM中的成品           
			productPersistence = factory.get(ProductPersistence.class);
			pmProduct = productPersistence.get(new ObjectId(pmoid));
			Debug.P(pmProduct.getSyncDate());
			IBAUtils  partiba = new IBAUtils(wtPart);
          Debug.P(partOid);
          Map<String,Object> plmData = new HashMap<String,Object>();
          pmProduct.setPLMData(plmData);
			pmProduct.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmProduct.setObjectNumber(wtPart.getNumber());
			pmProduct.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmProduct.setCreateBy(wtPart.getCreatorName(), wtPart.getCreatorFullName());			  //设置PM部件创建者
			pmProduct.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmProduct.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmProduct.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			pmProduct.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			pmProduct.setWeight(NumberFormat.getInstance().parse(weight));    
			pmProduct.setFormularNumber(partiba.getIBAValue(Contants.FORMULANO)==null?"":partiba.getIBAValue(Contants.FORMULANO) );
			pmProduct.setModifiedBy(wtPart.getModifierName(), wtPart.getModifierFullName());		//设置PM部件修改者
			pmProduct.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmProduct.doUpdate();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
				partiba.setIBAValue(Contants.PMREQUEST, "update");
				partiba.updateIBAPart(wtPart);
//				reloadPermission(pmoid);
				Debug.P("update pmproduct success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	  * 更新Windchill原材料后将原材料的基本属性写入PM的半成品对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void updatePMaterialToPM(String pmoid,WTPart wtPart){
		String partOid ="";    //WC部件Oid 
		MaterialPersistence materialPersistence=null;         //PM系统中的原材料持久化对象
		String weight ="";
		Debug.P("更新Windchill中的半成品---------------"+wtPart.getNumber());
		partOid = wtPart.toString();
		try {
			PMMaterial pmMaterial = null;//PM中的原材料          
			materialPersistence = factory.get(MaterialPersistence.class);
			pmMaterial = materialPersistence.get(new ObjectId(pmoid));
			Debug.P("pmmaterial--->"+pmMaterial.getCommonName());
			IBAUtils  partiba = new IBAUtils(wtPart);
          Debug.P(partOid);
          pmMaterial.setPLMId(partOid);
          Map<String,Object> plmData = new HashMap<String,Object>();
          pmMaterial.setPLMData(plmData);
			pmMaterial.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmMaterial.setObjectNumber(wtPart.getNumber());
			pmMaterial.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmMaterial.setCreateBy(wtPart.getCreatorName(), wtPart.getCreatorFullName());			  //设置PM部件创建者
			pmMaterial.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmMaterial.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmMaterial.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			
			pmMaterial.setModifiedBy(wtPart.getModifierName(), wtPart.getModifierFullName());		//设置PM部件修改者
			pmMaterial.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			 pmMaterial.setWeight(NumberFormat.getInstance().parse(weight));    
			pmMaterial.setMaterialGroup(partiba.getIBAValue(Contants.MATERIALGROUP)==null?"":partiba.getIBAValue(Contants.MATERIALGROUP));
			pmMaterial.setOwner(wtPart.getCreatorName());
			WriteResult wresult = pmMaterial.doUpdate();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
				partiba.setIBAValue(Contants.PMREQUEST, "update");
				partiba.updateIBAPart(wtPart);
//				reloadPermission(pmoid);
				Debug.P("update PMMaterial success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	  * 更新Windchill客供件后将客供件的基本属性写入PM的客供件对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void updateSupplyToPM(String pmoid,WTPart wtPart){
		String pFolderId="";
		SupplymentPersistence supplymentPersistence=null;         //PM系统中的客供件持久化对象
		String weight ="";
		Debug.P("将Windchill中的客供件更新PM系统的数据库中---------------》"+wtPart.getNumber());
		try {
			PMSupplyment pmSupplyment = null;//PM中的客供件         
			supplymentPersistence = factory.get(SupplymentPersistence.class);
			pmSupplyment = supplymentPersistence.get(new ObjectId(pmoid));
			IBAUtils  partiba = new IBAUtils(wtPart);
          pmSupplyment.setFolderIdByPLMId(pFolderId);
          Map<String,Object> plmData = new HashMap<String,Object>();
          pmSupplyment.setPLMData(plmData);
			pmSupplyment.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmSupplyment.setStatus(wtPart.getState().toString().toLowerCase());                   //设置PM部件状态
			pmSupplyment.setCreateBy(wtPart.getCreatorName(), wtPart.getCreatorFullName());			  //设置PM部件创建者
			pmSupplyment.setMajorVid(wtPart.getVersionIdentifier().getValue());     //设置PM部件大版本
			pmSupplyment.setSecondVid(Integer.parseInt(wtPart.getIterationIdentifier().getValue())); //设置PM部件小版本
			pmSupplyment.setPhase(partiba.getIBAValue(Contants.PHASE)==null?"":partiba.getIBAValue(Contants.PHASE));             //设置PM部件的阶段标记
			
			pmSupplyment.setSpec(partiba.getIBAValue(Contants.SPECIFICATIONS)==null?"":partiba.getIBAValue(Contants.SPECIFICATIONS));   //设置pm部件型号规格
			 weight = partiba.getIBAValue(Contants.WEIGHT);
			if(StringUtils.isNotEmpty(weight))
			 pmSupplyment.setWeight(NumberFormat.getInstance().parse(weight));    
			pmSupplyment.setCustomerName(partiba.getIBAValue(Contants.CLIENTNAME)==null?"":partiba.getIBAValue(Contants.CLIENTNAME));
			pmSupplyment.setModifiedBy(wtPart.getModifierName(), wtPart.getModifierFullName());		//设置PM部件修改者
			pmSupplyment.setMaterialGroup(partiba.getIBAValue(Contants.MATERIALGROUP)==null?"":partiba.getIBAValue(Contants.MATERIALGROUP));
			pmSupplyment.setOwner(wtPart.getCreatorName());
			pmSupplyment.setModifiedBy(wtPart.getModifierName(), wtPart.getModifierFullName());		//设置PM部件修改者
			WriteResult wresult = pmSupplyment.doUpdate();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				partiba.setIBAValue(Contants.CYNCDATA,Utils.getDate() );
				partiba.setIBAValue(Contants.PMREQUEST, "update");
				partiba.updateIBAPart(wtPart);
//				reloadPermission(pmoid);
				Debug.P("update PMSupplyment success");
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();  
		}
	}
	
	public static void deletePMPart(String pmoid,WTPart wtPart){
		PartPersistence partPersistence=null;         //PM系统中的半成品持久化对象
		try {
			PMPart pmPart = null;//PM中的半成品           
			partPersistence = factory.get(PartPersistence.class);
			pmPart = partPersistence.get(new ObjectId(pmoid));
			Debug.P("将Windchill中的半成品从PM系统的数据库中删除------------->"+pmPart.getCommonName());
			WriteResult wresult = pmPart.doRemove();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				Debug.P("delete PMPart success");
			}
		} catch (Exception e) {    
			e.printStackTrace();
		}
	}
	
	public static void deletePMProduct(String pmoid,WTPart wtPart){
		ProductPersistence productPersistence=null;         //PM系统中的成品持久化对象
		try {
			PMProduct productPart = null;//PM中的成品           
			productPersistence = factory.get(ProductPersistence.class);
			productPart = productPersistence.get(new ObjectId(pmoid));
			Debug.P("将Windchill中的成品从PM系统的数据库中删除--->"+productPart.getCommonName());
			WriteResult wresult = productPart.doRemove();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				Debug.P("delete PMProduct success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deletePMMaterial(String pmoid,WTPart wtPart){
		MaterialPersistence materialPersistence=null;           //PM系统中的原材料持久化对象
		try {
			PMMaterial pmMaterial = null;//PM中的原材料
			materialPersistence = factory.get(MaterialPersistence.class);
			pmMaterial = materialPersistence.get(new ObjectId(pmoid));
			Debug.P("将Windchill中的半成品从PM系统的数据库中删除-----》"+pmMaterial.getCommonName());
			WriteResult wresult = pmMaterial.doRemove();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				Debug.P("delete PMMaterial success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteSupplyment(String pmoid,WTPart wtPart){
		SupplymentPersistence supplymentPersistence=null;         //PM系统中的客供件持久化对象
		try {
			PMSupplyment pmSupplyment = null;//PM中的客供件          
			supplymentPersistence = factory.get(SupplymentPersistence.class);
			pmSupplyment = supplymentPersistence.get(new ObjectId(pmoid));
			Debug.P("将Windchill中的客供件从PM系统的数据库中删除---》"+pmSupplyment.getCommonName());
			WriteResult wresult = pmSupplyment.doRemove();   //
			String error = wresult.getError();
			if(StringUtils.isEmpty(error)){
				Debug.P("delete Supplyment success");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void reloadPermission(String objectId) throws Exception{
		String urls = ModelServiceFactory.URL_REAUTH+"?id="+objectId;
		Debug.P(urls);
		URL url = new URL(urls);
		url.openConnection();
	}

}
