
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;

import org.apache.axis.encoding.XMLType;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import ext.sdxc.webservics.SDXCWebService;

public class CallWs {
	
	/**
	 * 调用Webservice
	 * @author Eilaiwang
	 * @param methodname WebService方法名
	 * @param urlStr Webservice的rul
	 * @param targetNs WebService xml文件里的 namespace
	 * @param objs WebService方法对应的参数
	 * @throws Exception
	 * @return String
	 * @Description
	 */
	public static String call2(String methodname ,Object [] objs) throws Exception {
		String targetNs="http://webservics.sdxc.ext/";
		String urlStr="http://csr-tmt.com/Windchill/servlet/SDXCWebService?wsdl";
		Service  service = new Service();  
	    Call call = (Call) service.createCall();
			call.setTimeout(60000);		
		call.setTargetEndpointAddress( new URL(urlStr) ); 
		call.setUsername("wcadmin");
		call.setPassword("wcadmin");
	    call.setOperationName(new QName(targetNs,methodname));  
		for(int i=0;i<objs.length;i++){
		   call.addParameter("arg"+i, XMLType.XSD_STRING, ParameterMode.IN);
		}
		call.setReturnType(XMLType.XSD_STRING);
	    String ts = "";
		try {
			ts = (String) call.invoke(objs);
		} catch (RemoteException e) {
			e.printStackTrace();
		}  
		System.out.println(ts);
	    return ts;
	}
	
	public static void main(String[] args) throws Exception {
		String methodname="doLoggin";
		
		Object [] objs=new Object[]{"wcadmin","wcadmin"};
		call2(methodname,objs);
	}
	
}
