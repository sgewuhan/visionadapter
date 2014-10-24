package ext.sdxc.webservics;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

public class WSTest {
	public static void main(String[] args) {

		 

        try {

               String endpoint = "http://csr-tmt.com/Windchill/servlet/SDXCWebService?wsdl";

               //直接引用远程的wsdl文件

              //以下都是套路
               Service service = new Service();

               Call call = (Call) service.createCall();

               call.setTargetEndpointAddress(endpoint);

               call.setOperationName("add");//WSDL里面描述的接口名称

               call.addParameter("a", org.apache.axis.encoding.XMLType.XSD_DATE,

               javax.xml.rpc.ParameterMode.IN);//接口的参数
               
               call.addParameter("b", org.apache.axis.encoding.XMLType.XSD_DATE,

                       javax.xml.rpc.ParameterMode.IN);//接口的参数

               call.setReturnType(org.apache.axis.encoding.XMLType.XSD_STRING);//设置返回类型 

               String a = "1";
                
               String b ="2";
               
               String result = (String)call.invoke(new Object[]{a,b});

               //给方法传递参数，并且调用方法

               System.out.println("result is "+result);

        }

        catch (Exception e) {

               System.err.println(e.toString());

        }

 }

}
