package ext.tmt.integration.webservice.pm;

import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.ptc.jws.servlet.JaxWsWebService;

@WebService()
public class PMWebservice extends JaxWsWebService
{
	
	
	
	
	@WebMethod(operationName="addAllEntry")
	public int addAllEntry(@WebParam(name="containerName")String containerName,@WebParam(name="oid")String oid,@WebParam(name="dataMap")Map dataMap){
		return 0;
	}
	
	
    @WebMethod(operationName="addFolder")
    public int addFolder (@WebParam(name="containerName")String containerName,@WebParam(name="oid")String oid,@WebParam(name="dataMap")Map dataMap)
    {
    	
        return 0;
    }
    
    
    @WebMethod(operationName="editFolder")
    public int editFolder (int a, int b )
    {
      return a+b;
    }
    
    
    
    
    
}