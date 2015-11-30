package ext.tmt.integration.webservice.spm;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.rmi.RemoteException;

import org.dom4j.DocumentException;

import wt.method.RemoteMethodServer;
import wt.pom.Transaction;
import wt.query.QueryException;
import wt.util.WTException;

public class DocProcessor  {

    /**
     * 主逻辑程序
     * 
     * @param inputXml
     * @return
     */
    public static String processor(String inputXml) {
        String result = "";
        if (!RemoteMethodServer.ServerFlag) {
            String CLASSNAME = DocProcessor.class.getName();
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
            Object obj = XMLUtils.analysisDocXML(inputXml);
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

            DocInfo docInfo = (DocInfo) obj;
            
            docInfo.createDoc();

            tran.commit();
            tran = null;
        } catch (QueryException e) {
            e.printStackTrace();
            result = XMLUtils.creatResultXmlDoc(false,
                    "新材PLM系统API报错：" + e.getLocalizedMessage());
            return result;
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
        } catch (RemoteException e) {
            e.printStackTrace();
            result = XMLUtils.creatResultXmlDoc(false,
                    "新材PLM系统API报错：" + e.getLocalizedMessage());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            result = XMLUtils.creatResultXmlDoc(false,
                    "新材PLM系统API报错：" + e.getLocalizedMessage());
            return result;
        } catch (PropertyVetoException e) {
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
