package com.sg.plmadapter.windchill;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.6.14
 * 2014-11-05T20:05:46.220+08:00
 * Generated source version: 2.6.14
 * 
 */
@WebService(targetNamespace = "http://pm.webservice.integration.tmt.ext/", name = "PMWebservice")
@XmlSeeAlso({ObjectFactory.class})
public interface PMWebservice {

    @WebResult(name = "return", targetNamespace = "")
    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/moveDocumentRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/moveDocumentResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/moveDocument/Fault/Exception")})
    @RequestWrapper(localName = "moveDocument", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.MoveDocument")
    @WebMethod
    @ResponseWrapper(localName = "moveDocumentResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.MoveDocumentResponse")
    public int moveDocument(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws Exception_Exception;

    @WebResult(name = "return", targetNamespace = "")
    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/deleteDocumentRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/deleteDocumentResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/deleteDocument/Fault/Exception")})
    @RequestWrapper(localName = "deleteDocument", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.DeleteDocument")
    @WebMethod
    @ResponseWrapper(localName = "deleteDocumentResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.DeleteDocumentResponse")
    public int deleteDocument(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws Exception_Exception;

    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/changeLifecycleStateRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/changeLifecycleStateResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/changeLifecycleState/Fault/Exception")})
    @RequestWrapper(localName = "changeLifecycleState", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.ChangeLifecycleState")
    @WebMethod
    @ResponseWrapper(localName = "changeLifecycleStateResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.ChangeLifecycleStateResponse")
    public void changeLifecycleState(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws Exception_Exception;

    @WebResult(name = "return", targetNamespace = "")
    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/createFolderRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/createFolderResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/createFolder/Fault/Exception")})
    @RequestWrapper(localName = "createFolder", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.CreateFolder")
    @WebMethod
    @ResponseWrapper(localName = "createFolderResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.CreateFolderResponse")
    public int createFolder(
        @WebParam(name = "arg0", targetNamespace = "")
        java.util.List<java.lang.String> arg0
    ) throws Exception_Exception;

    @WebResult(name = "return", targetNamespace = "")
    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/updateDocumentRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/updateDocumentResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/updateDocument/Fault/Exception")})
    @RequestWrapper(localName = "updateDocument", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.UpdateDocument")
    @WebMethod
    @ResponseWrapper(localName = "updateDocumentResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.UpdateDocumentResponse")
    public int updateDocument(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws Exception_Exception;

    @WebResult(name = "return", targetNamespace = "")
    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/deleteFolderRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/deleteFolderResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/deleteFolder/Fault/Exception")})
    @RequestWrapper(localName = "deleteFolder", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.DeleteFolder")
    @WebMethod
    @ResponseWrapper(localName = "deleteFolderResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.DeleteFolderResponse")
    public int deleteFolder(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws Exception_Exception;

    @WebResult(name = "return", targetNamespace = "")
    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/editFolderRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/editFolderResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/editFolder/Fault/Exception")})
    @RequestWrapper(localName = "editFolder", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.EditFolder")
    @WebMethod
    @ResponseWrapper(localName = "editFolderResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.EditFolderResponse")
    public int editFolder(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        java.lang.String arg1
    ) throws Exception_Exception;

    @WebResult(name = "return", targetNamespace = "")
    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/createDocumentRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/createDocumentResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/createDocument/Fault/Exception")})
    @RequestWrapper(localName = "createDocument", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.CreateDocument")
    @WebMethod
    @ResponseWrapper(localName = "createDocumentResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.CreateDocumentResponse")
    public int createDocument(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws Exception_Exception;

    @Action(input = "http://pm.webservice.integration.tmt.ext/PMWebservice/changeRevisionRequest", output = "http://pm.webservice.integration.tmt.ext/PMWebservice/changeRevisionResponse", fault = {@FaultAction(className = Exception_Exception.class, value = "http://pm.webservice.integration.tmt.ext/PMWebservice/changeRevision/Fault/Exception")})
    @RequestWrapper(localName = "changeRevision", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.ChangeRevision")
    @WebMethod
    @ResponseWrapper(localName = "changeRevisionResponse", targetNamespace = "http://pm.webservice.integration.tmt.ext/", className = "com.sg.plmadapter.windchill.ChangeRevisionResponse")
    public void changeRevision(
        @WebParam(name = "arg0", targetNamespace = "")
        java.lang.String arg0
    ) throws Exception_Exception;
}