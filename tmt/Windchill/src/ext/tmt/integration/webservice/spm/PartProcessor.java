package ext.tmt.integration.webservice.spm;

import java.rmi.RemoteException;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import wt.method.RemoteMethodServer;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class PartProcessor {

	public static String processor(String inputXml) {
		String result = "";
		if (!RemoteMethodServer.ServerFlag) {
			String CLASSNAME = PartProcessor.class.getName();
			String method = "processor";
			Class<?>[] types = { String.class };
			Object[] vals = { inputXml };
			try {
				return (String) RemoteMethodServer.getDefault().invoke(method,
						CLASSNAME, null, types, vals);
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = "调用远程方法失败！";
			return XMLUtils.creatResultXmlDoc(false, result);
		}
		Transaction tran = null;
		try {
			Object obj = XMLUtils.analysisXML(inputXml);
			if (obj == null) {
				result = XMLUtils.creatResultXmlDoc(false, "新材PLM系统封装出错，请检查。");
				return result;
			}
			if (obj instanceof String) {
				result = (String) obj;
				return result;
			}
			tran = new Transaction();
			tran.start();

			PartInfo partInfo = (PartInfo) obj;

			String operation = partInfo.getOperation();
			if (StringUtils.equals(operation, PartInfo.OPERATION_CREATE)) {
				if (partInfo.checkTMTFactory()) {
					partInfo.doSaveWTPart();
				} else {
					result = XMLUtils.creatResultXmlDoc(false, "非新材物料，不进行创建！");
				}
			} else if (StringUtils.equals(operation, PartInfo.OPERATION_UPDATE)) {
				partInfo.doSaveWTPart();
			} else if (StringUtils.equals(operation, PartInfo.OPERATION_DELETE)) {
				partInfo.doRemoveWTPart();
			} else {

			}

			tran.commit();
			tran = null;
		} catch (DocumentException e) {
			e.printStackTrace();
			result = XMLUtils.creatResultXmlDoc(false,
					"新材PLM系统API报错：" + e.getLocalizedMessage());
			return result;
		} catch (WTException e) {
			e.printStackTrace();
			result = XMLUtils.creatResultXmlDoc(false,
					"新材PLM系统API报错：" + e.getLocalizedMessage());
			return result;
		} catch (WTPropertyVetoException e) {
			e.printStackTrace();
			result = XMLUtils.creatResultXmlDoc(false,
					"新材PLM系统API报错：" + e.getLocalizedMessage());
			return result;
		} catch (RemoteException e) {
			e.printStackTrace();
			result = XMLUtils.creatResultXmlDoc(false,
					"新材PLM系统API报错：" + e.getLocalizedMessage());
			return result;
		} catch (ParseException e) {
			e.printStackTrace();
			result = XMLUtils.creatResultXmlDoc(false,
					"新材PLM系统API报错：" + e.getLocalizedMessage());
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result = XMLUtils.creatResultXmlDoc(false,
					"新材PLM系统API报错：" + e.getLocalizedMessage());
			return result;
		} finally {
			if (tran != null) {
				tran.rollback();
			}
		}
		return result;
	}
}
