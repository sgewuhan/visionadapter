package ext.tmt.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import wt.csm.navigation.ClassificationNode;
import wt.doc.WTDocument;
import wt.doc.WTDocumentHelper;
import wt.doc.WTDocumentMaster;
import wt.epm.EPMDocument;
import wt.fc.ObjectVector;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleServerHelper;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.OrganizationServicesHelper;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.ownership.OwnershipHelper;
import wt.part.PartType;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartReferenceLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.VersionIdentifier;
import wt.vc.Versioned;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewReference;
import wt.vc.wip.CheckoutInfo;
import wt.vc.wip.WorkInProgressState;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;

import ext.tmt.part.PartUtils;
// part工具类
public class PartUtil implements RemoteAccess {
	
	
	private static String DEFAULT_VIEW="Design";//默认部件视图
	
	
	//创建部件的测试
	public static void main(String[] args) throws Exception {
		String partName="Car_20141022";
		String num="Car_NUM00000001";
		String containerName="TMT_201401";
		String part_folder="/TMT_Demo1/TDE001";
		String type="";
		String userName="zwx82599";
		String vmUser="PM-RW";
		Map ibas=new HashMap();
		createWTPart(partName,num,containerName,part_folder,null,null,null,null,userName,vmUser,null,null,ibas,true);
		
	}
	
	
	
	
	/**
	 * 创建部件
	 * @param partName 部件名称
	 * @param partNumber  部件编号
	 * @param containerName  容器名称
	 * @param part_folder 文件夹
	 * @param lifecycleTemplate 生命周期模板(未开放)
	 * @param lifecycleState 生命周期状态(未开放)
	 * @param viewstr 视图名称
	 * @param type 部件显示的软类型名称
	 * @param creator 创建人
	 * @param vmUser 虚名用户
	 * @param version 小版本  (未开放)
	 * @param revision 大版本 (未开放)
	 * @param isEndItem 是否创建成品
	 * @return
	 * @throws Exception
	 */
	public static WTPart createWTPart(String partName,String partNumber, String containerName, String part_folder,String lifecycleTemplate,
			String lifecycleState,String viewstr,String type,String creator,String vmUser,String version,String revision,Map ibas,boolean isEndItem) throws WTException,Exception {
		
		   if (!RemoteMethodServer.ServerFlag) {
	           String method = "createWTPart";
	           String klass = PartUtil.class.getName();
	           Class[] types = { String.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class,String.class,Map.class,boolean.class};
	           Object[] vals = {partName,partNumber,containerName,part_folder,lifecycleTemplate,lifecycleState,viewstr,type,creator,vmUser,version,revision,ibas,isEndItem};
	           return (WTPart) RemoteMethodServer.getDefault().invoke(method, klass, null, types, vals);
	       }
		
		WTPart part = WTPart.newWTPart();
		part.setName(partName);
		if(partNumber!=null){
			part.setNumber(partNumber);
		}	
		
		WTContainer container = GenericUtil.getWTContainerByName(containerName);
		part.setContainer(container);

		//定义视图
		if(StringUtils.isEmpty(viewstr)){viewstr=DEFAULT_VIEW;}//默认设计视图
		View view = ViewHelper.service.getView(viewstr);
		ViewReference viewRef = ViewReference.newViewReference(view);
		part.setView(viewRef);
		
		if(StringUtils.isEmpty(type)){//如果类型为空,则使用默认的WTPart类型
			PartType part_type=wt.part.PartType.getPartTypeDefault();
			part.setPartType(part_type);
		}else{
			String enumType=GenericUtil.getTypeByName(type);//软类型的名称
			TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(enumType);//type	
			part.setTypeDefinitionReference(typeDefinitionRef);
		}
		
        if(!StringUtils.isEmpty(lifecycleTemplate)){
        	WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);		
        	LifeCycleHelper.setLifeCycle(part, LifeCycleHelper.service.getLifeCycleTemplate(lifecycleTemplate, containerRef));
        }
	    if(!StringUtils.isEmpty(lifecycleState)){
	    	LifeCycleServerHelper.setState(part, State.toState(lifecycleState));
			Debug.P(part+" set state:"+part.getState().getState().toString());
	    }
	       //文件夹对象
	       Folder folder = FolderUtil.getFolderRef(part_folder,container,true);
		    FolderHelper.assignLocation((FolderEntry) part, folder);
		    
		//版本
		/*MultilevelSeries multilevelseries = MultilevelSeries.newMultilevelSeries("wt.vc.VersionIdentifier", Version);
		VersionIdentifier ver = VersionIdentifier.newVersionIdentifier(multilevelseries);
		VersionControlHelper.setVersionIdentifier(part, ver );//大版本
		Series series = Series.newSeries("wt.vc.IterationIdentifier", Revision);
		IterationIdentifier ide = IterationIdentifier.newIterationIdentifier(series);
		VersionControlHelper.setIterationIdentifier(part, ide);//小版本	
*/		
		
		Debug.P("---->VmUser:"+vmUser+"   ;UserName="+creator);
		WTUser wtuser=null;
		try {//获取用户信息
		    wtuser=OrganizationServicesHelper.manager.getUser(creator);
		} catch (WTException e) {//获取虚名用户
			 wtuser=OrganizationServicesHelper.manager.getUser(vmUser);
		}
		
		VersionControlHelper.assignIterationCreator(part, WTPrincipalReference.newWTPrincipalReference(wtuser));//创建者
		VersionControlHelper.setIterationModifier(part, WTPrincipalReference.newWTPrincipalReference(wtuser));//更新者
		OwnershipHelper.setOwner(part, wtuser); //所有者
		
		//设置软属性
		if (ibas != null && !ibas.isEmpty()) {
			LWCUtil.setValueBeforeStore(part,ibas);
		}
		
		//是否创建成品
		part.setEndItem(isEndItem);

		 part = (WTPart) PersistenceHelper.manager.save(part);
		 Debug.P("------->>>>WTPart::"+part);
		 
		 return part;
	}
	
	
	public static boolean VERBOSE = true;
	
	
	 public static String getVersion(WTObject obj) throws WTException {
	        String version = "";
	        String iterate = "";
	        String banbenhao = "";
	        version = ((Versioned)obj).getVersionIdentifier().getValue();
	        iterate = ((Iterated)obj).getIterationIdentifier().getValue();
	        banbenhao = version + "." + iterate;
	        return banbenhao;
	    }

	
	
	
	/**
	 * 获取部件的第一层分类码
	 * @author Eilaiwang
	 * @param wtPart
	 * @return
	 * @throws WTException
	 * @return String
	 * @Description
	 */
	public static String getNumberPrefix(WTPart wtPart) throws WTException {
		String numberPrefix = "";
		List<ClassificationNode> cnList = WindchillUtil.getClassificationNodeByPart(wtPart);
		if (cnList.size() > 0) {
			List<String> cpList = getClassficationPath(cnList.get(0));
			numberPrefix =cpList.get(cpList.size()-1);
		//	Debug.P("Prefix->>" + numberPrefix);
		}
		return numberPrefix;
	}
	
	/**
	 * 获取部件的第四层分类码
	 * @author Eilaiwang
	 * @param wtPart
	 * @return
	 * @throws WTException
	 * @return String
	 * @Description
	 */
	public static String getNumberPrefix4(WTPart wtPart) throws WTException {
		String numberPrefix = "";
		List<ClassificationNode> cnList = WindchillUtil.getClassificationNodeByPart(wtPart);
		List<String> cpList = getClassficationPath(cnList.get(0));
		if (cnList.size() > 0) {
			numberPrefix =cpList.get(0);
			//Debug.P("Prefix->>" + numberPrefix);
		}
		return numberPrefix;
	}
	
	public static String getNumberClassPath4(WTPart wtpart)throws WTException{
		String numberPrefix = "";
		String type ="";
		type = getType(wtpart);
		List<ClassificationNode> cnList = WindchillUtil.getClassificationNodeByPart(wtpart);
		if (cnList.size() > 0) {
			List<String> cpList = getClassficationPath(cnList.get(0));
			numberPrefix = cpList.get(0);
			//Debug.P("Prefix->>" + numberPrefix);
//			if (type.contains(Contants.NUMITPARTS)) {
//				numberPrefix = numberPrefix.substring(0, 6);
//			} else {
				numberPrefix = numberPrefix.substring(0, 7);
//			}
		}
		return numberPrefix;
	}
	
	
	public static List<String> getClassficationPath(ClassificationNode cn) {
		List<String> pathList = new ArrayList<String>();
		while (true) {
			if (cn == null) {
				break;
			} else {
				String displayString = cn.getIBAReferenceableDisplayString();
				//Debug.P("============= displayString11 " + displayString);
				pathList.add(displayString);
			}
			cn = cn.getParent();
		}
		return pathList;
	}
	

	/**
	 * 根据部件编号查找部件
	 * @author blueswang
	 * @param number
	 * @return
	 * @throws WTException
	 * @return WTPart
	 * @Description
	 */
	public static WTPart getPartByNumber(String number) throws WTException {

		WTPart Part = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER,
				SearchCondition.EQUAL, number.trim().toUpperCase(), false));
		
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.size() > 0) {
			Part = (WTPart) qr.nextElement();
     			Part = (WTPart) VersionControlHelper.getLatestIteration(Part);
		}
		return Part;
	}
	
	
	
	
	
	
	  
	
	/**
	 * 根据部件编号查找部件
	 * @author blueswang
	 * @param number
	 * @return
	 * @throws WTException
	 * @return WTPart
	 * @Description
	 */
	public static WTPart getPartByNumber2(String number) throws WTException {

		WTPart Part = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER,
				SearchCondition.EQUAL, number.trim().toUpperCase(), false));
		
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.size() > 0) {
			Part = (WTPart) qr.nextElement();
     			Part = (WTPart) VersionControlHelper.getLatestIteration(Part);
			if (Part != null)
				if (VERBOSE)
					System.out.println("the Part is ： "+ Part.getDisplayIdentity());
		}
		if (Part == null)
			if (VERBOSE)
				System.out.println("没有编号为:" + number + "的WTPart！");
		return Part;
	}
	
	/**
	 * 根据部件名称和视图获取部件
	 * @author blueswang
	 * @param number
	 * @param view
	 * @return
	 * @throws WTException
	 * @return WTPart
	 * @Description
	 */
	 public static WTPart getLastPartbyNumViwe(String number,String views)  throws WTException {
	    	WTPart part = null;
	    	QuerySpec qs = new QuerySpec(WTPart.class );
	    	qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER,
					SearchCondition.EQUAL, number.trim().toUpperCase(), false));
	    	View view = ViewHelper.service.getView(views.trim());
	    	view.getIdentity();
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class,"view.key",
					SearchCondition.EQUAL,PersistenceHelper.getObjectIdentifier(view)));
			QueryResult qr = PersistenceHelper.manager.find(qs);
			LatestConfigSpec  configSpec = new LatestConfigSpec();
			configSpec.process(qr);
			qr =conentOrder(qr);
			if (qr.size() > 0) {
				part = (WTPart) qr.nextElement();
				part = (WTPart) VersionControlHelper.getLatestIteration(part);
			 if (part != null)
						System.out.println("the Part is ： "+ part.getDisplayIdentity());
			}
			if (part == null)
					System.out.println("没有编号为:" + number + "视图为:"+views+"的WTPart！");
	     
	    	return part;
	    }   
	
//	
	    //排序
	    public static QueryResult conentOrder(QueryResult qr){
	    	int siez = qr.size();
	    	
	    	ObjectVector ov = new ObjectVector();
	    	
	    	while(qr.hasMoreElements()){
	    		Object obj = qr.nextElement();
	    		ov.addElement(obj);
	    		}
	    	Vector v= ov.getVector();
	    	ObjectVector lv = new ObjectVector();
	     	for(int i =v.size()-1;i>=0;i--){
	     		Object o = v.elementAt(i);
	     		lv.addElement(o);
	     	}
	     	QueryResult qrs = new QueryResult(lv);
	     	
	     	return qrs;
	    }
	

	public static WTPart getDocByName(String name) throws Exception {
		WTPart party = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NAME,
				SearchCondition.EQUAL, name.toUpperCase(), false));

		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.size() > 0) {
			party = (WTPart) qr.nextElement();
			party = (WTPart) VersionControlHelper.getLatestIteration(party);
			if (party != null)
				if (VERBOSE)
					System.out.println("the part is ： "
							+ party.getDisplayIdentity());
		}
		if (party == null)
			if (VERBOSE)
				System.out.println("没有编号为:" + name + "的WTPart！");

		return party;
	}
	
	/**
	 * 根据编号、版本查找part
	 * 
	 * @param RELATED_PART_NUMBER
	 * @param RELATED_PART_VERSION
	 * @return
	 * @throws Exception
	 */
	public static WTPart findPart(String RELATED_PART_NUMBER,
			String RELATED_PART_VERSION) throws Exception {
		WTPart part = null;
	//	Debug.P("-->查找编号为:" + RELATED_PART_NUMBER + "版本为:"
	//			+ RELATED_PART_VERSION + "的零部件");
		QuerySpec qs = new QuerySpec(WTPart.class);
		int index = 0;
		// 条件: 指定编号
		qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER,
				SearchCondition.EQUAL, RELATED_PART_NUMBER),
				new int[] { index });
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(WTPart.class, WTPart.CHECKOUT_INFO
				+ "." + CheckoutInfo.STATE, SearchCondition.NOT_EQUAL,
				WorkInProgressState.WORKING), new int[] { 0 });
		// 条件：指定大版本
		if (RELATED_PART_VERSION != null || !RELATED_PART_VERSION.trim().equals("")) {
			qs.appendAnd();
			qs.appendWhere(new SearchCondition(WTPart.class,
					WTPart.VERSION_IDENTIFIER + "."	+ VersionIdentifier.VERSIONID,
					SearchCondition.EQUAL, RELATED_PART_VERSION),
					new int[] { index });
		}
		qs = new LatestConfigSpec().appendSearchCriteria(qs);
		// 执行查询
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (RELATED_PART_VERSION == null|| RELATED_PART_VERSION.trim().equals("")) {
			// 过滤最新版本, 未指定版本时，只取最新大版本＋最新小版本
			qr = new LatestConfigSpec().process(qr);
		}
		// 处理查询结果
		if (qr.hasMoreElements()) {
			part = (WTPart) qr.nextElement();
			//Debug.P("-->查询完成,查找到编号为:" + part.getNumber() + "名称为:"
			//		+ part.getName() + "的零部件");
			return part;
		} else {
			throw new Exception("未找到指定编号、版本的零部件.");
		}
	}
	
	/**
	 * 创建部件与文档的说明关系
	 * @author Eilaiwang
	 * @param part
	 * @param doc
	 * @return
	 * @throws Exception
	 * @return boolean
	 * @Description
	 */
	public static boolean createDescriptionLink(WTPart part, WTDocument doc)
			throws Exception {
		boolean result = false;
		if (!getDescriptionLink(part, doc)) {
			WTPartDescribeLink partLink = WTPartDescribeLink
					.newWTPartDescribeLink(part, doc);
			PersistenceServerHelper.manager.insert(partLink);
			partLink = (WTPartDescribeLink) PersistenceHelper.manager
					.refresh(partLink);
			result = true;
		} else {
			throw new Exception("部件:" + part.getNumber() + " 和文档:"
					+ doc.getName() + "已有关联");
		}
		return result;
	}
	
	
	/**
	 * 创建部件和文档的参考关系
	 * @author blueswang
	 * @param part
	 * @param doc
	 * @return void
	 * @throws Exception 
	 * @Description
	 */
	public static boolean createReferentLink(WTPart part,WTDocument doc) throws Exception{
		 boolean result = false;
		WTDocumentMaster docMaster=(WTDocumentMaster)doc.getMaster();
			if(!getRelationLink(part,docMaster)){
				WTPartReferenceLink partRef = WTPartReferenceLink.newWTPartReferenceLink(part, docMaster);
				PersistenceServerHelper.manager.insert(partRef);
				partRef= (WTPartReferenceLink)PersistenceHelper.manager.refresh(partRef);
			result = true;
			}else{
				throw new Exception("部件:"+part.getNumber()+" 和文档:"+doc.getName()+"已有关联");
			}
			return result;
	}
	
	
	/**
	 * 判断部件和文档间是否存在说明关系
	 * @author Eilaiwang
	 * @param part
	 * @param doc
	 * @return
	 * @throws Exception
	 * @return boolean
	 * @Description
	 */
	public static boolean getDescriptionLink(WTPart part, WTDocument doc) throws Exception{
		boolean result = false;
		QueryResult qr = PersistenceHelper.manager.navigate(part, WTPartDescribeLink.DESCRIBES_ROLE,
				WTPartDescribeLink.class,true);
		while (qr.hasMoreElements()) {
			WTDocumentMaster docMaster = (WTDocumentMaster)qr.nextElement();
			System.out.println(" docMaster.num"+docMaster.getNumber());
			if(docMaster.getNumber().equals(doc.getNumber())){
				result = true;
			}
		}
		return result;
	}
	
	
	
	
	/**
	 * 获取部件的说明方文档的PMID
	 * @author Eilaiwang
	 * @param part
	 * @return
	 * @throws WTException
	 * @return WTDocument
	 * @Description
	 */
	public static List<String> getDescriptDocPMIdBy(WTPart part) throws WTException{
		WTDocument doc =null;
		List<String> docList = new ArrayList<String>();
//		List<WTDocument> docList = new ArrayList<WTDocument>();
		QueryResult qr =WTPartHelper.service.getDescribedByDocuments(part);
		Debug.P("部件："+part.getNumber()+"关联的说明方文档数量："+qr.size());
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if(obj instanceof WTDocument){
				doc=(WTDocument)obj;
				Debug.P("部件："+part.getNumber()+"关联的说明方文档---》"+doc.getNumber());
				IBAUtils iba = new IBAUtils(doc);
				docList.add(iba.getIBAValue(Contants.PROJECTNO));
			}
		}
		
		return docList;
	}
	
	/**
	 * 获取部件的参考文档
	 * @author Eilaiwang
	 * @param part
	 * @return
	 * @throws WTException
	 * @return List<WTDocument>
	 * @Description
	 */
	public static WTDocument getReferenceDocByPart(WTPart part) throws WTException{
		WTDocument doc =null;
		String documentType="";
		QueryResult qr = WTPartHelper.service.getReferencesWTDocumentMasters(part);
		while(qr.hasMoreElements()){
			Object object=qr.nextElement();
	    	if(object instanceof WTDocumentMaster){
			    WTDocumentMaster doct = (WTDocumentMaster)object;
		         doc = DocUtils.getDocByNumber(doct.getNumber());
		         IBAUtils iba = new IBAUtils(doc);
		         documentType=iba.getIBAValue("com.plm.hyth.documentType");
		         Debug.P(documentType);
		         if(StringUtils.isNotEmpty(documentType)&&documentType.equals("XH")){
		        	 return doc;
		         }
			}
		}
       return doc;
	}
	

	/**
	 * 查询BOM中零部件的单层子部件的pmid
	 * 
	 * @param parentPart
	 * @return
	 * @throws WTException
	 * @throws RemoteException
	 */
	public static List<String> queryPartPMIDByBOM(WTPart parentPart) throws WTException {
		List<String> list = new ArrayList<String>();
		ConfigSpec configSpec = WTPartHelper.service.findWTPartConfigSpec();
		int index = 0;
		list = querySubBOMList(parentPart,configSpec, list, index);
		return list;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private static List<String> querySubBOMList(WTPart parentPart, ConfigSpec configSpec, List<String> list, int index) throws WTException {
		QueryResult qr = WTPartHelper.service.getUsesWTParts(parentPart, configSpec);
		Vector<Object> vector = qr.getObjectVectorIfc().getVector();
		Debug.P("BOM数量----------》"+vector.size());
		for (int i = 0; i < vector.size(); i++) {
			Persistable[] persist = (Persistable[]) vector.get(i);
			WTPart part=(WTPart) persist[1];
			if(part!=null){
				IBAUtils iba = new IBAUtils(part);
				list.add(iba.getIBAValue(Contants.PMID));
			}
		}
		return list;
	}
	
	
	/**
	 * 根据子部件查询父部件
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static  List<String> queryPrentPartsByParts(WTPart part) throws WTException{
		List<String> list = new ArrayList<String>();
		WTPart parts =null;
		WTPartMaster partMaster= (WTPartMaster)part.getMaster();
		QueryResult qr = WTPartHelper.service.getUsedByWTParts(partMaster);
		while(qr.hasMoreElements()){
			Object obj = (Object)qr.nextElement();
			Debug.P(obj);
			if(obj instanceof WTPart){
				 parts =(WTPart)obj;
				 IBAUtils iba = new IBAUtils(parts);
				 list.add(iba.getIBAValue(Contants.PMID));
			}
		}
		return list;
	}
	
	
	/**
	 * 获取与部件管理的图样文档的编号
	 * @author Eilaiwang
	 * @param part
	 * @param docList
	 * @return
	 * @throws WTException
	 * @return WTDocument
	 * @Description
	 */
	public static List<String> getDescriptionDocByPart(WTPart part) throws WTException{
		List<String> list = new ArrayList<String>();
		String docType="";
		String docNumber="";
		String state ="";
		WTDocument doc =null;
		QueryResult qr = PersistenceHelper.manager.navigate(part, WTPartDescribeLink.DESCRIBES_ROLE,
				WTPartDescribeLink.class,true);
		while (qr.hasMoreElements()) {
			WTDocumentMaster docMaster = (WTDocumentMaster)qr.nextElement();
			docType=getType(docMaster);
			docNumber=docMaster.getNumber();
			doc=DocUtils.getDocByNumber(docNumber);
			state = doc.getLifeCycleState().toString();
			Debug.P(" docMastedocMaster.getNumber()--->"+docNumber+"   DocType====>"+docType);
			System.out.println();
//			if(docType.equals(Contants.DRWDOCUMENT)&& !state.equals(Contants.RELEASE)){
//				list.add(docNumber);
//			}
		}
		return list;
	}
	
	
	/**
	 * 获取与部件关联的EPMDocument
	 * @author Eilaiwang
	 * @param part
	 * @return
	 * @throws WTException
	 * @return List<EPMDocument>
	 * @Description
	 */
	public static List<String> getEPMDocByPart(WTPart part)throws WTException{
		List<String> list = new ArrayList<String>();
		String epmNumber ="";
		String state ="";
		QueryResult qr = WTPartHelper.service.getDescribedByDocuments(part);
	       while(qr.hasMoreElements()){
	    	   Object obj = qr.nextElement();
	    	   if(obj instanceof EPMDocument){
	    		   EPMDocument doc = (EPMDocument)obj;
	    		   state =doc.getLifeCycleState().toString();
	    		   if(!state.equals(Contants.RELEASE)){
	    		      epmNumber = doc.getNumber();
	    		      list.add(epmNumber);
	    		   }
	    	   }
	       }	
		return list;
	}
	
	/**
	 * 获取与部件关联的EPMDocument
	 * @author Eilaiwang
	 * @param part
	 * @return
	 * @throws WTException
	 * @return List<EPMDocument>
	 * @Description
	 */
	public static List<String> getEPMDocPMIDByPart(WTPart part)throws WTException{
		List<String> list = new ArrayList<String>();
	    		   //EPMDocument doc = EPMDocUtil.getActiveEPMDocument(part);
		QueryResult qr = WTPartHelper.service.getDescribedByDocuments(part);
	       while(qr.hasMoreElements()){
	    	   Object obj = qr.nextElement();
	    	   if(obj instanceof EPMDocument){
	    		   EPMDocument doc =(EPMDocument)obj;
	    			   Debug.P("EPMDocument---->"+doc.getCADName());
	    			   IBAUtils iba = new IBAUtils(doc);
	    			   list.add(iba.getIBAValue(Contants.PROJECTNO));
	    		   }
	       }
		return list;
	}
	
	
	
	/**
	 * 判断部件和文档是否已存在参考关系
	 * @author blueswang
	 * @param part
	 * @param doc
	 * @return
	 * @throws Exception
	 * @return boolean
	 * @Description
	 */
	public static boolean getRelationLink(WTPart part, WTDocumentMaster doc) throws Exception{
		boolean result = false;
		QueryResult qr = PersistenceHelper.manager.navigate(part, WTPartReferenceLink.REFERENCES_ROLE,
				 WTPartReferenceLink.class,true);
		while (qr.hasMoreElements()) {
			WTDocumentMaster docMaster = (WTDocumentMaster)qr.nextElement();
			System.out.println(" docMaster.num"+docMaster.getNumber());
			if(docMaster.getNumber().equals(doc.getNumber())){
				result = true;
			}
		}
		return result;
	}
	
//	/**
//	 * 查询BOM中零部件及其子件
//	 * 
//	 * @param parentPart
//	 * @return
//	 * @throws WTException
//	 * @throws RemoteException
//	 */
//	public static List<BOMElement> queryPartBOM(WTPart parentPart) throws WTException {
//		BOMElement root = new BOMElement(parentPart);
//		List<BOMElement> list = new ArrayList<BOMElement>();
//		ConfigSpec configSpec = WTPartHelper.service.findWTPartConfigSpec();
//		int index = 0;
//		list = querySubBOMList(root, configSpec, list, index);
//		return list;
//	}
//
//	@SuppressWarnings({ "deprecation", "unchecked" })
//	private static List<BOMElement> querySubBOMList(BOMElement element, ConfigSpec configSpec, List<BOMElement> list, int index) throws WTException {
//		list.add(element);
//		QueryResult qr = WTPartHelper.service.getUsesWTParts(element.getPart(), configSpec);
//		Vector<Object> vector = qr.getObjectVectorIfc().getVector();
//		for (int i = 0; i < vector.size(); i++) {
//			Persistable[] persist = (Persistable[]) vector.get(i);
//			index++;
//			BOMElement subElement = new BOMElement(index, (WTPart)persist[1], (WTPartUsageLink) persist[0]);
//			querySubBOMList(subElement, configSpec, list, index);
//			index--;
//		}
//		return list;
//	}
	
	
	
	
	
    public static WTDocument getDocumentByNum(String docNumber) throws WTException{
    	Debug.P(docNumber);
		WTDocument document = null;
		QuerySpec querySpec = new QuerySpec(WTDocument.class);
		SearchCondition numberSC = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, docNumber);
		SearchCondition latestIteration = new SearchCondition(WTDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE);
		querySpec.appendWhere(numberSC);
		querySpec.appendAnd();
		querySpec.appendWhere(latestIteration);
		QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
		// queryResult = (new LatestConfigSpec()).process(queryResult);
		Debug.P("Document----->"+queryResult.hasMoreElements());
		while (queryResult.hasMoreElements()) {
			document = (WTDocument) queryResult.nextElement();
			Debug.P(document);
			String dType=getType(document);
			   Debug.P(dType);
			  // if(dType.contains("ProcessDoc")){
					return document;
			   //}
		}
		return null;
    }
	
	/**
	 * 获取对象类型
	 * @author Eilaiwang
	 * @param object
	 * @return
	 * @return String
	 * @Description
	 */
	public static String getType(WTObject object){
		TypeIdentifier ti = TypeIdentifierUtility.getTypeIdentifier(object);
		String type = ti.getTypename();
		return type;

		}


	    
	    
	    
}

