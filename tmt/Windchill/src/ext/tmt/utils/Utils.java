package ext.tmt.utils;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import wt.csm.navigation.litenavigation.ClassificationNodeDefaultView;
import wt.csm.navigation.service.ClassificationHelper;
import wt.doc.WTDocument;
import wt.doc.WTDocumentMasterIdentity;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.structure.EPMMemberLink;
import wt.epm.structure.EPMReferenceLink;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlHelper;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

import com.ibm.icu.util.Calendar;
import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;


/**
 * 
 * @author Tony 2013-12-25 修改了取分类码的方式，可向上查询父节点
 * 
 */
public class Utils {

	/**
	 * 根据文档编码查询文档对象
	 * 
	 * @param docNumber
	 *        文档编码
	 * @return WTDocument 文档对象
	 * @throws WTException
	 */
//	public static WTDocument getDocumentByNumber(String docNumber) throws WTException {
//		WTDocument document = null;
//		QuerySpec querySpec = new QuerySpec(WTDocument.class);
//		SearchCondition numberSC = new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, docNumber);
//		SearchCondition latestIteration = new SearchCondition(WTDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE);
//		querySpec.appendWhere(numberSC);
//		querySpec.appendAnd();
//		querySpec.appendWhere(latestIteration);
//		QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
//		queryResult = (new LatestConfigSpec()).process(queryResult);
//		if (queryResult.hasMoreElements()) {
//			document = (WTDocument) queryResult.nextElement();
//		}
//		return document;
//	}

	/**
	 * 更改对象编码
	 * 
	 * @param 待更待编码对象
	 * @param 新编码
	 */

//	public static void changeDcouementNumber(WTDocument document, String newNumber) {
//		Identified identified = (Identified) document.getMaster();
//		try {
//			WTDocumentMasterIdentity documentMasterIdentity = (WTDocumentMasterIdentity) identified.getIdentificationObject();
//			if (newNumber != null)
//				documentMasterIdentity.setNumber(newNumber);
//			identified = IdentityHelper.service.changeIdentity(identified, documentMasterIdentity);
//			document = (WTDocument) PersistenceHelper.manager.refresh(document);
//			// PersistenceHelper.manager.save(document);
//		} catch (WTException e) {
//			e.printStackTrace();
//		} catch (WTPropertyVetoException e) {
//			e.printStackTrace();
//		}
//	}

	// 取分类码，可向上查询父节点并取节点名进行拼装
	// mode：0：取本节点；1：取全路径各节点拼装；-1：取根节点
	// lengh：0：全名；N>0：指定前N位；-1：前面的全部数字
/*	public static String getClassficationNameofPart(WTPart partObj, int mode, int length) 
			throws WTException, RemoteException, Exception {
		String nodename = null;
		String str = null;
		try {
			GetClassificitionInfo info = new GetClassificitionInfo(partObj);
			ClassificationNodeDefaultView nodeInfo = info.getcNode();
			nodename = nodeInfo.getName();
			
			if (mode == 0) { //取本节点
				str = getPreSubstrfromString(nodename, length);
			}
			
			if (mode == 1 || mode == -1) {
				str = getPreSubstrfromString(nodename, length);
				
				ClassificationNodeDefaultView ParentNode = ClassificationHelper.service.getParentNodeDefaultView(nodeInfo);
				while (ParentNode != null) {
					nodename = ParentNode.getName();
					
					if (mode == 1)//取全路径各节点拼装
						str = getPreSubstrfromString(nodename, length) + str;
					if (mode == -1)//只取根节点
						str = getPreSubstrfromString(nodename, length);
					
					ParentNode = ClassificationHelper.service.getParentNodeDefaultView(ParentNode);
				}
			}		
			
		} catch (WTException e) {
			e.printStackTrace();
		}
		return str;
	}*/
	
	//字符串截取
	//lengh：0：全名；N>0：指定前N位；-1：前面的全部数字
	public static String getPreSubstrfromString(String originalString, int lengh) throws Exception {
		String result = null;
		
		if (lengh == 0) { //全部
			result = originalString;
		} else if (lengh < 0) {//前面的全部数字
			result="";
			String numChars = "0123456789";
			for (int i=0; i<originalString.length(); i++) {
				String ch = originalString.substring(i,i+1);
				if (numChars.indexOf(ch) >=0 )
					result = result + ch;
			}
		} else {//指定前N位
			if (originalString.length() > lengh) {
				result = originalString.substring(0, lengh);
			} else {
				result = originalString ;
			}
		}
		
		return result;
	}
	
//	public static ClassificationNodeDefaultView getClassficationNodeDefaultView(WTPart partObj) throws WTException {
//
//		GetClassificitionInfo info = new GetClassificitionInfo(partObj);
//		return info.getcNode();
//	}
//
//	public static WTPart setClassificationNodeDefaultView(WTPart partObj, ClassificationNodeDefaultView nodeView) throws WTException {
//		GetClassificitionInfo info = new GetClassificitionInfo(partObj);
//		info.setcNode(nodeView);
//		PersistenceHelper.manager.save(partObj);
//		return (WTPart) PersistenceHelper.manager.refresh(partObj);
//
//	}
	

//	/**
//	 * 查询CAD-BOM中EPMDocument及其link关系
//	 * 
//	 * @param partDocument
//	 * @return
//	 * @throws WTException
//	 */
//	public static List<EpmBOMElement> queryEPMDocumentBOM(EPMDocument partDocument) throws WTException {
//		EpmBOMElement root = new EpmBOMElement(partDocument);
//		List<EpmBOMElement> list = new ArrayList<EpmBOMElement>();
//		int index = 0;
//		list = queryEpmSubBomList(root, list, index);
//		return list;
//	}

//	private static List<EpmBOMElement> queryEpmSubBomList(EpmBOMElement root, List<EpmBOMElement> list, int index) throws WTException {
//		EPMDocument epm = null;
//		EPMMemberLink link = null;
//		epmDocList.add(root);
//		QueryResult result = PersistenceHelper.manager.navigate(root.getEpmDocument(), EPMMemberLink.USES_ROLE, EPMMemberLink.class, false);
//		while (result.hasMoreElements()) {
//			link = (EPMMemberLink) result.nextElement();
//			QueryResult qr = VersionControlHelper.service.allIterationsOf((EPMDocumentMaster )link.getRoleBObject());
//			epm = (EPMDocument) qr.nextElement();
//			index++;
//			EpmBOMElement subEpmElement = new EpmBOMElement(index, epm, link);
//			queryEpmSubBomList(subEpmElement, list, index);
//			index--;
//		}
//
//		return epmDocList;
//	}


	/**
	 * 根据Part查询3DEPMDocument
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument getEpmByPart(WTPart part) throws WTException {
		EPMDocument document = null;
		QueryResult result = PartDocServiceCommand.getAssociatedCADDocuments(part);
		if (result.hasMoreElements()) {
			document = (EPMDocument) result.nextElement();
			System.out.println(document.getCADName());
			return document;
		} else {
			return document;
		}
	}


	/**
	 * 根据3DEPMDocument查询对应的2维工程图
	 * 
	 * @param epm
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument get2DBy3D(EPMDocument epm) throws WTException {

		EPMDocument epm2 = null;
		QuerySpec qs = new QuerySpec(EPMReferenceLink.class);
		QueryResult qr = EPMStructureHelper.service.navigateReferencedBy((EPMDocumentMaster) epm.getMaster(), qs, false);
		while (qr != null && qr.hasMoreElements()) {
			EPMReferenceLink epmReferenceLink = (EPMReferenceLink) qr.nextElement();
			int dep_type = epmReferenceLink.getDepType();
			if (dep_type == 4) {
				epm2 = epmReferenceLink.getReferencedBy();
			}
		}
		return epm2;
	}

	public static EPMDocument getEPMDocumentByNumber(String epmNumber) throws WTException {
		EPMDocument document = null;
		QuerySpec querySpec = new QuerySpec(EPMDocument.class);
		SearchCondition numberSC = new SearchCondition(EPMDocument.class, EPMDocument.NUMBER, SearchCondition.EQUAL, epmNumber);
		SearchCondition latestIteration = new SearchCondition(EPMDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE);
		querySpec.appendWhere(numberSC);
		querySpec.appendAnd();
		querySpec.appendWhere(latestIteration);
		QueryResult queryResult = PersistenceHelper.manager.find(querySpec);
		queryResult = (new LatestConfigSpec()).process(queryResult);
		if (queryResult.hasMoreElements()) {
			document = (EPMDocument) queryResult.nextElement();
		}
		return document;
	}

	public static String getDate() {
		Calendar calendar = Calendar.getInstance();
		return String.valueOf(calendar.get(Calendar.YEAR)) + "-" + String.valueOf(calendar.get(Calendar.MONTH) + 1) + "-" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
	}

	public static String changeStrLowCase2UpperCase(String str) {
		StringBuffer sb = new StringBuffer();
		if (str != null) {
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (Character.isLowerCase(c)) {
					sb.append(Character.toUpperCase(c));
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 判断对象是否检出
	 * @author Eilaiwang
	 * @param workable
	 * @return
	 * @throws WTException
	 * @return boolean
	 * @Description
	 */
	public static boolean isCheckout(Workable workable) throws WTException {
		return WorkInProgressHelper.isCheckedOut(workable);
	}
	
	/**
	 * 非空判断
	 * @author Eilaiwang
	 * @param str
	 * @return boolean
	 */
	public static boolean isNull(String str){
		if(str ==null ||str.length() <0){
			return true;
		}else if(str ==""||str.length() <0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 判断一个字符是否为字母
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isLetter(String str) {
		Pattern pattern = Pattern.compile("[a-zA-Z]");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}

	/**
	 * 判断一个字符是否为汉字
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isChinese(String str) {
		Pattern pattern = Pattern.compile("[\u4E00-\u9FA5]");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}

	/**
	 * 判断一个字符是否为整数
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isNumber(String str) {
		Pattern pattern = Pattern.compile("[0-9]");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}
    
	/**
	 *提取字符串中的数字
	 * @author Eilaiwang
	 * @param str
	 * @return
	 * @return String
	 * @Description
	 */
	public static String getNumber(String str){
		String regEx="[^0-9]";     
		Pattern p = Pattern.compile(regEx);     
		Matcher m = p.matcher(str);     
		 return m.replaceAll("").trim();
	}
	
	/**
	 * 判断一个字符是否为指定的整数
	 * 
	 * @param str
	 * @param checkStr
	 * @return
	 */
	private static boolean checkNumberStr(String str, String checkStr) {
		Pattern pattern = Pattern.compile("[" + checkStr + "]");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}
	
}
