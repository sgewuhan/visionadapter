package ext.tmt.integration.webservice.pm;


import javax.jws.WebMethod;
import javax.jws.WebService;

import com.ptc.jws.servlet.JaxWsWebService;

import ext.tmt.integration.webservice.spm.SPMWebserviceImpl;

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
    
    @WebMethod(operationName="changePhase")
    public void changePhase (String objectId)throws Exception
    {
         PMWebserviceImpl.changePhase(objectId);
    }
    

    @WebMethod(operationName="processorForSpm1")
    public String processorForSpm1 (String workflow, int times,
            String factory)throws Exception
    {
         return SPMWebserviceImpl.processorForSpm1(workflow, times, factory);
    }
    
    @WebMethod(operationName="processorForSpm2")
    public String processorForSpm2 (String partNumber, String workflow,
            String mark)throws Exception
    {
         return SPMWebserviceImpl.processorForSpm2(partNumber, workflow, mark);
    }
    
    @WebMethod(operationName="createDocForPart")
    public String createDocForPart(String partNumber, String workflow,
            String factory)throws Exception
    {
         return SPMWebserviceImpl.createDocForPart(partNumber, workflow, factory);
    }
    
    @WebMethod(operationName="updateDocForPart")
    public String updateDocForPart(String partNumber, String workflow)throws Exception
    {
         return SPMWebserviceImpl.updateDocForPart(partNumber, workflow);
    }
    
    @WebMethod(operationName="getJSGGSByPartNumber")
    public String getJSGGSByPartNumber(String partNumber)throws Exception
    {
         return SPMWebserviceImpl.getJSGGSByPartNumber(partNumber);
    }
    
    @WebMethod(operationName="getCPSCByPartNumber")
    public String getCPSCByPartNumber(String partNumber)throws Exception
    {
         return SPMWebserviceImpl.getCPSCByPartNumber(partNumber);
    }
    
    @WebMethod(operationName="checkPartFromLibrary")
    public String checkPartFromLibrary(String partNumber)throws Exception
    {
         return SPMWebserviceImpl.checkPartFromLibrary(partNumber);
    }
    
    
    
    
    
    
}