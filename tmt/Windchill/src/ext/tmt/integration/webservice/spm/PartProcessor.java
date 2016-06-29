package ext.tmt.integration.webservice.spm;

import java.rmi.RemoteException;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;

import ext.tmt.utils.Debug;
import wt.method.RemoteMethodServer;
import wt.pom.Transaction;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class PartProcessor {

	public static String processor(String inputXml) {
		Debug.P("接受XML："+inputXml);
		String result = XMLUtils.creatResultXmlDoc(true, "创建成功");
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
			Debug.P("开始重构XML: " + inputXml);
			Object obj = XMLUtils.analysisXML(inputXml);
			Debug.P("重构XML完成");
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
			Debug.P("PartInfo number:" + partInfo.getNumber());
			// 判断是需要进入新材PLM
			if (partInfo.checkIsTMT()) {
				String operation = partInfo.getOperation();
				if (StringUtils.equals(operation, PartInfo.OPERATION_CREATE)) {
					if (partInfo.checkTMTFactory()) {
						partInfo.doSaveWTPart();
					}
				} else if (StringUtils.equals(operation,
						PartInfo.OPERATION_UPDATE)) {
					partInfo.doSaveWTPart();
				} else if (StringUtils.equals(operation,
						PartInfo.OPERATION_DELETE)) {
					partInfo.doRemoveWTPart();
				}
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
