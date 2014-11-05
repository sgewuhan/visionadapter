package ext.tmt.integration.webservice.pm;


import javax.jws.WebMethod;
import javax.jws.WebService;

import com.ptc.jws.servlet.JaxWsWebService;

@WebService()
public class PMWebservice extends JaxWsWebService
{
	
	
	
	
    @WebMethod(operationName="createFolder")
    public int createFolder(String[] result )throws Exception
    {
    	
         return PMWebserviceImpl.createFolderEntryList(result);
    }
    

    @WebMethod(operationName="editFolder")
    public int editFolder (String objectId,String newFolderName)throws Exception
    {
       return PMWebserviceImpl.modifyFolderEntry(objectId, newFolderName);
    }
    
   
    @WebMethod(operationName="deleteFolder")
    public int deleteFolder (String objectId)throws Exception
    {
       return PMWebserviceImpl.deleteFolderEntry(objectId);
    }
    
    
    @WebMethod(operationName="createDocument")
    public int createDocument (String objectId)throws Exception
    {
       return PMWebserviceImpl.createWTDocumentEntry(objectId);
    }
    
    
    @WebMethod(operationName="updateDocument")
    public int updateDocument (String objectId)throws Exception
    {
       return PMWebserviceImpl.updateWTDocumentEntry(objectId);
    }
    
    @WebMethod(operationName="moveDocument")
    public int moveDocument (String objectId)throws Exception
    {
       return PMWebserviceImpl.moveWTDocumentEntry(objectId);
    }
    
    
    @WebMethod(operationName="deleteDocument")
    public int deleteDocument (String objectId)throws Exception
    {
       return PMWebserviceImpl.deleteWTDocumentEntry(objectId);
    }
    
    
    
    @WebMethod(operationName="changeRevision")
    public void changeRevision (String objectId)throws Exception
    {
         PMWebserviceImpl.changeRevision(objectId);
    }
    
    
    @WebMethod(operationName="changeLifecycleState")
    public void changeLifecycleState (String objectId)throws Exception
    {
         PMWebserviceImpl.changeLifecycleState(objectId);
    }
    
    
    
    
    
    
}