package ext.tmt.integration.webservice.spm;

import java.io.Serializable;
/**
 * spm常量类
 * @author Administrator
 *
 */
public class SPMConsts implements Serializable{


	private static final long serialVersionUID = 1439520155658711040L;
	
	
    public static final String ROOT = "wt.part.WTPart|";
    
    public  static String ROOTDOC = "wt.doc.WTDocument|";
    
    public static String ROOTRESOURCE = "com.ptc.windchill.mpml.resource.MPMTooling|";
    
    public static String PARTCLASSIFIATION="PartClassification";//部件分类
    
    
    public static final String SPMCONTAINER_NAME="基础物料库";
    public static final String PACKING_MATER_FOLDER="/Default/包装材料";//物料类型:包装材料
    public static final String SPARE_MATER_FOLDER="/Default/备品备件";//物料类型:备品备件
    public static final String CUSTOM_MATER_FOLDER="/Default/客供料";//物料类型:客供料
    public static final String RAWMATERIAL_MATER_FOLDER="/Default/通用采购件/机电电子";//原材料
    
    //原材料
	public static final String RAWMATERIAL="com.plm.RawMaterial"; 
	
	// 客供件
	public static final String SUPPLYMENT="com.plm.GuestPart";
	
	
	// 包装材料
	public static final String PACKINGPART="com.plm.PackingMaterialPart";
	
	//备品备料
	public static final String TOOLPART="com.plm.ToolPart";
	
	//部件类型
	public static final String PART_TYPE="partTypeName";
	
	//工厂
	public static final String FACTORY="Factory";
	
	public static final String PMID="PMId";
	
	

    //数据库字段
	public static final  String MATERIAL_NUM="WLNUMBER";//物料编码
	public static final String MATERIAL_NAME="WLNAME";//物料名称
    public static final String MATERIAL_PATH="WLXIAOLEI";//物料小类(物料分类全路径)
    public static final String SPM_WORKFLOW="WORKFLOW";//工作流进程ID
    public static final  String SPM_TIMES="TIMES";//发布次数
    public static final String SPM_CREATOR="CREATOR";
    public static final String SPM_DOCNAME="DOCNAME";
    public static final String SPM_DOCTYPE="DOCTYPE";
    public static final String SPM_DOCNUMBER="DOCNUMBER";
    public static final String SPM_LOCATION="LOCATION";//主文档路径地址
    public static final String SPM_WLCONTENT="WLCONTENT";//变更原因
    public static final String SPM_TUZHIBIANHAO="TUZHIBIANHAO";
    public static final String IBA_KEY="ATTRKEY";//软属性名称
    public static final String IBA_VALUE="ATTRVALUE";
    public static final String SPM_FACTORY="CSR_SUOSHUGONGCHANG";
    
    public static final String ATTKEY_NAME="name";//物料维护表,物料名称
    
    
    
    //Map集合基本属性KEY(部分对应系统中的类型属性)
    public static final String KEY_CONTAINERNAME="containerName";
    public static final  String KEY_FOLDER="folderName";
    public static final String KEY_MATER_CATEGORY="materCategory";
    public static final String KEY_LOCATION_PATH="fileContent";
    public static final String KEY_CLASSIFICATION="WLXIAOLEI";//物料分类路径

    
    public static final  String KEY_PARTTYPE="partType";
    public static final String KEY_NUMBER="number";
    public static final String KEY_NAME="name";
    public static final String KEY_UNIT="defaultUnit";
    public static final String KEY_DOCTYPE="documentType";

    
   
    
    public static final String DEFAULT_DOC_TYPE="wt.doc.WTDocument";
    
   
   
   
   //物料类型
   public static final String SPM_MATER_TYPE="CSR_WULIAOLEIXING";//物料类型
   
   
   //文档类型
   public static final String TECHNICAL_SPEC="技术规格书";//Windchill技术规格书文档类型
   public static final String PRODUCT_SPEC="产品手册";//Windchill产品手册文档类型
   
   
   
   public static final String DESIGN_VIEW="Design";//设计视图
   public static final String MANUFACTURE_VIEW="Manufacture";
   
   
   //生命周期状态
   public static final String RELEASED="RELEASED";//已发布
   public static final String DESPOSED="OBSOLETE";//已废弃
   
   public static final String FOLDER_SPLIT="folder_w";
   
   public static final String EA="ea";
   
   

	
	

}
