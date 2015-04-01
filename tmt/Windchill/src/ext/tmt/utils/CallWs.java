package ext.tmt.utils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.encoding.XMLType;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

@Deprecated
public class CallWs {
	
	
	@Deprecated
	public static void main(String[] args) throws Exception {
		String targetNs="http://pm.webservice.integration.tmt.ext/";
		String urlStr="http://lcy.yaozheng.com/Windchill/servlet/PMWebservice?wsdl";
		String userName="wcadmin";
		String pass="wcadmin";
		String methodName="editFolder";
//		String pid="526f302029f260085cc7491d";
//		String name="abcd";
		String[]   array={"526f302029f260085cc7491d","123"};
		
		callWebService(urlStr,methodName,userName,pass,targetNs,array);
	}
	

	
	 private static <T> Object[] ConvertToArray (List<T> tList)
     {
		 Object[] array = new Object[tList.size()];
         int i = 0;
     for (Object object : array) {
    	 array[i] = object;
         i++;
	}
         return array;
     }
	
	/**
	 * 
	 * @param wsdl 服务地址
	 * @param methodName 方法名称
	 * @param returnType 方法返回类型
	 * @param userName 认证用户
	 * @param password 认证密码
	 * @param nameSpace 名称空间
	 * @param params 参数
	 * @return 
	 * @throws Exception
	 */
	public static Object callWebService(String wsdl,String methodName,String userName,String password,String nameSpace,Object ... params)throws Exception{
		Object result=null;
		Service  service = new Service();
	    Call call = (Call) service.createCall();
	    call.setTimeout(60000);		
	    call.setTargetEndpointAddress( wsdl ); 
	    call.setUseSOAPAction(true);
	    if(userName!=null){
	    	call.setUsername(userName);
	    }
	    if(password!=null){
	    	call.setPassword(password);
	    }
	    call.setOperationName(new QName(nameSpace,methodName));  
	    call.setSOAPActionURI(nameSpace+methodName);
		for(int i=0;i<params.length;i++){
			 call.addParameter("arg"+i, XMLType.XSD_STRING, ParameterMode.IN);
		}
		 call.setReturnType(XMLType.XSD_INT);
		 try {
			 result = call.invoke(params);
			System.out.println(result);
		    } catch (RemoteException e) {
			    e.printStackTrace();
		}
		    return result;
	}
	
	
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
	public static int call2(String methodname ,  Object ... objs) throws Exception {
		String targetNs="http://pm.webservice.integration.tmt.ext/";
		String urlStr="http://lcy.yaozheng.com/Windchill/servlet/PMWebservice?wsdl";
		Service  service = new Service();
	    Call call = (Call) service.createCall();
		call.setTimeout(60000);		
		call.setTargetEndpointAddress( urlStr ); 
		call.setUseSOAPAction(true);
		call.setUsername("wcadmin");
		call.setPassword("wcadmin");
	    call.setOperationName(new QName(targetNs,methodname));  
	    String uri=targetNs+methodname;
	    call.setSOAPActionURI(uri);
		for(int i=0;i<objs.length;i++){
		   call.addParameter("arg"+i, XMLType.XSD_STRING, ParameterMode.IN);
		}
		call.setReturnType(XMLType.XSD_INT);
	    int count=0;
		try {
			count= (Integer) call.invoke(objs);
		} catch (RemoteException e) {
			e.printStackTrace();
		}  
		System.out.println(count+"");
	    return count;
	}



	
	
}
