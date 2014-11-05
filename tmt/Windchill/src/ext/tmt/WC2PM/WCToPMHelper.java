package ext.tmt.WC2PM;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import wt.folder.Folder;
import wt.part.WTPart;
import wt.util.WTProperties;

import com.google.javascript.jscomp.mozilla.rhino.tools.debugger.Main;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMPart;
import com.sg.visionadapter.PartPersistence;
import com.sg.visionadapter.ProductPersistence;

import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.PartUtil;

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
	  * Windchill 创建部件后将部件的基本属性写入PM的部件对象上并存入PM数据库中
	  * @author Eilaiwang
	  * @param wtPart
	  * @return void
	  * @Description
	  */
	public static void CreatePartToPM(WTPart wtPart){
		
		
		String partType ="";   //Windchill中部件的类型
		String partOid ="";    //WC部件Oid 
		String partFolderString="";
		Folder partFolder =null;
		String pFolderId="";
		PartPersistence partPersistence=null;         //PM系统中的半成品持久化对象
		ProductPersistence productPersistence =null;  //PM系统中的成品持久化对象
		
		String weight ="";
		Debug.P("将Windchill中的半成品插入PM系统的数据库中");
		
		partType = PartUtil.getType(wtPart);
		partOid = wtPart.toString();
		try {
			PMPart pmPart = null;//PM中的半成品           
			partPersistence = factory.get(PartPersistence.class);
			pmPart = partPersistence.newInstance();
			IBAUtils  partiba = new IBAUtils(wtPart);
            Debug.P(partOid);
           // partfolder
            partFolderString = wtPart.getFolderPath();
            partFolder=wtPart.getFolderingInfo().getFolder();
            Debug.P(partFolder.toString());
            Debug.P(wtPart.getContainer());
            Debug.P(partFolderString);
//            partFolderString = partFolderString.substring(0,partFolderString.lastIndexOf(File.separator));
//            partFolder = FolderUtil.getFolder(wtPart.getContainer(), partFolderString);
//            Debug.P(partFolder);
//            pFolderId = partFolder.toString();
            Debug.P(partFolder.toString());
			//pmPart.set_id(new ObjectId(partOid));
			pmPart.setFolderId(new ObjectId("5451d5f4c2d6d241000839ed"));
            pmPart.setPLMId(partOid);
            Map<String,Object> plmData = new HashMap<String,Object>();
            pmPart.setPLMData(plmData);
			pmPart.setCommonName(wtPart.getName());                           //设置PM部件名称
			pmPart.setObjectNumber(wtPart.getNumber());
			pmPart.setStatus(wtPart.getState().toString());                   //设置PM部件状态
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
			pmPart.setProjectName(partiba.getIBAValue(Contants.PROJECTNAME)==null?"":partiba.getIBAValue(Contants.PROJECTNAME));
			pmPart.setProjectNumber(partiba.getIBAValue(Contants.PRODUCTNO)==null?"":partiba.getIBAValue(Contants.PROJECTNO));
			pmPart.setProjectWorkOrder(partiba.getIBAValue(Contants.WOKRORDER)==null?"":partiba.getIBAValue(Contants.WOKRORDER));
			pmPart.setSync();   //设置同步
			pmPart.set_id(new ObjectId());
			pmPart.doInsert();   //
//			productPersistence = factory.get(ProductPersistence.class);
//			PMProduct pmProduct = productPersistence.newInstance();
			
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
	
//	public static void main(String[] args) {
//		String str="/Default/临时产品/wc2pm006";
//		System.out.println(str.substring(0, str.lastIndexOf(File.separator)));
//	}
	
	

}
