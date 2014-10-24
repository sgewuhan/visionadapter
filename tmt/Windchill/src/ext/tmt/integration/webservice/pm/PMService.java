package ext.tmt.integration.webservice.pm;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.ptc.jws.servlet.JaxWsWebService;

@WebService()
public class PMService extends JaxWsWebService
{
	
	
	
    @WebMethod(operationName="add")
    public int add ( int a, int b )
    {
      return a+b;
    }
}