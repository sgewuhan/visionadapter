package ext.tmt.integration.webservice.spm;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ext.tmt.utils.Debug;
import wt.query.QueryException;
import wt.util.WTException;

public class XMLUtils {
	/**
	 * 解析由BPS平台传入xml字符串
	 * 
	 * @param inputXml
	 * @return
	 * @throws DocumentException
	 * @throws WTException
	 * @throws QueryException
	 */
	@SuppressWarnings("unchecked")
	public static Object analysisXML(String inputXml) throws DocumentException,
			QueryException, WTException {
		String errorMsg = null;

		if (StringUtils.isEmpty(inputXml)) {
			errorMsg = "BPS传入参数为空，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}

		Document xmlDoc = DocumentHelper.parseText(inputXml);
		if (xmlDoc == null) {
			errorMsg = "获取XMLDocument对象失败，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		PartInfo partInfo = new PartInfo();
		Element root = xmlDoc.getRootElement();

		// BPS传入流程编号检查
		Element bps_workflow_id = root.element(PartInfo.F_BPS_WORKFLOW_ID);
		if (bps_workflow_id == null) {
			errorMsg = "传入BPS流程编号节点（BPS_WORKFLOW_ID）为空，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		try {
			partInfo.checkAndSetBpsWorkflowId(StringUtils.trimToEmpty(
					(String) bps_workflow_id.getData()).trim());
		} catch (Exception e) {
			return creatResultXmlDoc(false, e);
		}

		// 处理类型检查
		Element operation = root.element(PartInfo.F_OPERATION);
		if (operation == null) {
			errorMsg = "传入处理类型节点（OPERATION）为空，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		String operation_data = StringUtils.trimToEmpty((String) operation
				.getData());
		try {
			partInfo.checkAndSetOperation(operation_data.trim());
		} catch (Exception e) {
			return creatResultXmlDoc(false, e);
		}

		// 处理类型对应详细信息检查
		Element operatoin_Element = root.element(StringUtils
				.upperCase(operation_data));
		if (operatoin_Element == null) {
			errorMsg = "传入处理类型为" + StringUtils.upperCase(operation_data)
					+ "，但未查询到具体信息，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		try {
			partInfo.checkAndSetNumber(operatoin_Element.attributeValue(
					PartInfo.F_NUMBER).trim());
		} catch (Exception e) {
			return creatResultXmlDoc(false, e);
		}

		// 封装
		// String containerName =
		// CSRUtils.getProperty("csr.part.containername");
		List<Element> elements = operatoin_Element.elements();
		if (elements == null || elements.size() == 0) {
			errorMsg = "传入处理类型为" + StringUtils.upperCase(operation_data)
					+ "，但具体信息中的条目为空，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		// Iterator<Element> attElements = operatoin_Element.elementIterator();
		for (Element element : elements) {

			String element_Name = element.getName();
			if (StringUtils.equals(element_Name, PartInfo.F_DELETE_FACTORY)) {
				String delete_factory_data = (String) element.getData();
				if (StringUtils.isEmpty(delete_factory_data)) {
					errorMsg = "传入已删除工厂值为空，请检查。";
					return creatResultXmlDoc(false, errorMsg);
				}
				partInfo.addDeleteFactory(delete_factory_data.trim());
			} else {
				Element att_Name_Element = element.element("NAME");
				if (att_Name_Element == null) {
					errorMsg = "传入属性名称节点为空，请检查。";
					return creatResultXmlDoc(false, errorMsg);
				} else {
					String att_Name_Element_data = StringUtils
							.trimToEmpty((String) att_Name_Element.getData());
					if (StringUtils.isEmpty(att_Name_Element_data)) {
						errorMsg = "传入属性名称值为空，请检查。";
						return creatResultXmlDoc(false, errorMsg);
					}

					Element att_Value_Element = element.element("VALUE");
					if (att_Value_Element == null) {
						errorMsg = "传入属性值节点为空，请检查。";
						return creatResultXmlDoc(false, errorMsg);
					}
					String att_Value_Element_data = StringUtils
							.trimToEmpty((String) att_Value_Element.getData());
					if (StringUtils.isEmpty(att_Value_Element_data)) {
						continue;
					}

					if (StringUtils.equals(att_Name_Element_data,
							"CSR_ZIYUANLEIXING_ZZZY")) {
						partInfo.setType(att_Value_Element_data.trim());
					} else if (StringUtils.equals(att_Name_Element_data,
							"CSR_CLASSFICATION")) {
						partInfo.setType(att_Value_Element_data.trim());
					} else if (StringUtils.equals(att_Name_Element_data, "设备")
							|| StringUtils.equals(att_Name_Element_data, "工具")
							|| StringUtils.equals(att_Name_Element_data, "工装")
							|| StringUtils.equals(att_Value_Element_data, "成品")
							|| StringUtils
									.equals(att_Value_Element_data, "半成品")) {
						partInfo.setType(att_Value_Element_data.trim());
					} else {
						partInfo.setValue(att_Name_Element_data,
								att_Value_Element_data.trim());
					}
				}
			}
		}

		return partInfo;
	}

	private static String creatResultXmlDoc(boolean isSucussed, Exception e) {
		return creatResultXmlDoc(isSucussed, e.getMessage());
	}

	/**
	 * 创建返回Xml
	 * 
	 * @param isSucussed
	 * @param errorMsg
	 * @return
	 */
	public static String creatResultXmlDoc(boolean isSucussed, String errorMsg) {
		Document xmlDoc = DocumentHelper.createDocument();
		Element rootElement = xmlDoc.addElement("PART_INTERFACE");

		Element sucussed_element = rootElement.addElement("SUCUSSED");
		sucussed_element.setText(isSucussed ? "SUCUSSED" : "FAIL");

		Element description_element = rootElement.addElement("DESCRIPTION");
		description_element.setText(errorMsg);

		return xmlDoc.asXML();
	}

	@SuppressWarnings("unchecked")
	public static Object analysisDocXML(String inputXml)
			throws DocumentException {
		String errorMsg = null;

		if (StringUtils.isEmpty(inputXml)) {
			errorMsg = "BPS传入参数为空，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}

		Document xmlDoc = DocumentHelper.parseText(inputXml);
		if (xmlDoc == null) {
			errorMsg = "获取XMLDocument对象失败，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}

		DocInfo docInfo = new DocInfo();
		Element root = xmlDoc.getRootElement();

		// BPS传入流程编号检查
		Element bps_workflow_id = root.element(DocInfo.F_BPS_WORKFLOW_ID);
		if (bps_workflow_id == null) {
			errorMsg = "传入BPS流程编号节点（BPS_WORKFLOW_ID）为空，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		try {
			docInfo.checkAndSetBpsWorkflowId(StringUtils.trimToEmpty(
					(String) bps_workflow_id.getData()).trim());
		} catch (Exception e) {
			return creatResultXmlDoc(false, e);
		}

		// 处理类型检查
		Element operation = root.element(DocInfo.F_OPERATION);
		if (operation == null) {
			errorMsg = "传入处理类型节点（OPERATION）为空，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		String operation_data = StringUtils.trimToEmpty((String) operation
				.getData());
		try {
			docInfo.checkAndSetOperation(operation_data.trim());
		} catch (Exception e) {
			return creatResultXmlDoc(false, e);
		}

		// 处理类型对应详细信息检查
		Element operatoin_Element = root.element(StringUtils
				.upperCase(operation_data));
		if (operatoin_Element == null) {
			errorMsg = "传入处理类型为" + StringUtils.upperCase(operation_data)
					+ "，但未查询到具体信息，请检查。";
			return creatResultXmlDoc(false, errorMsg);
		}
		try {
			docInfo.checkAndSetPartNumber(operatoin_Element.attributeValue(
					DocInfo.F_PART_NUMBER).trim());
		} catch (Exception e) {
			return creatResultXmlDoc(false, e);
		}

		Iterator<Element> it = operatoin_Element.elementIterator();
		while (it.hasNext()) {
			Element element = it.next();
			String name = element.getName();
			if (StringUtils.equals(name, DocInfo.F_JSGGSNUMBER)) {
				docInfo.setJsggsNumber((String) element.getData());
			} else if (StringUtils.equals(name, DocInfo.F_PRODUCTNUMBER)) {
				docInfo.setProductNumber((String) element.getData());
			} else if (StringUtils.equals(name, DocInfo.F_PRIMARY_LOCATION)) {
				Debug.P((String) element.getData());
				docInfo.setPrimaryLocation((String) element.getData());
			} else if (StringUtils.equals(name, DocInfo.F_ATT_LOCATION)) {
				docInfo.addAttLocations((String) element.getData());
			} else if (StringUtils.equals(name, DocInfo.F_CREATOR)) {
				docInfo.setCreator((String) element.getData());
			} else if (StringUtils.equals(name, DocInfo.F_TYPE)) {
				Debug.P((String) element.getData());
				docInfo.setType((String) element.getData());
			} else if (StringUtils.equals(name, DocInfo.F_XIAFA)) {
				docInfo.setXiafa((Boolean) element.getData());
			} else {
				break;
			}
		}

		try {
			docInfo.check();
		} catch (Exception e) {
			return creatResultXmlDoc(false, e);
		}

		return docInfo;
	}
}
