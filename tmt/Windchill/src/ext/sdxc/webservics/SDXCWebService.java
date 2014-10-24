package ext.sdxc.webservics;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.ptc.jws.servlet.JaxWsWebService;

@WebService()
public class SDXCWebService extends JaxWsWebService
{
    @WebMethod(operationName="add")
    public int add ( int a, int b )
    {
      return a+b;
    }
    
    @WebMethod(operationName="CreateFolders")
    public String CreateFolders(String folderName,String containerName){
    	return ProssServices.CreateFolders(folderName, containerName);
    }
    
    @WebMethod(operationName="getWorkItems")
    public String getWorkItems(String userName){
    	return ProssServices.getWorkItems(userName);
    }
    
    @WebMethod(operationName="doLoggin")
    public String doLoggin(String userName,String password){
    	return ProssServices.doLoggin(userName, password);
    }
    
    @WebMethod(operationName="getUserId")
    public String getUserId(String userName){
    	return ProssServices.getUserId(userName);
    }
    
    @WebMethod(operationName="getAllContainers")
    public String getAllContainers(){
    	return ProssServices.getAllContainer();
    }

}