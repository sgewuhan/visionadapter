
package com.sg.plmadapter.windchill;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.sg.plmadapter.windchill package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _CreateFolderResponse_QNAME = new QName("http://pm.webservice.integration.tmt.ext/", "createFolderResponse");
    private final static QName _DeleteFolderResponse_QNAME = new QName("http://pm.webservice.integration.tmt.ext/", "deleteFolderResponse");
    private final static QName _CreateFolder_QNAME = new QName("http://pm.webservice.integration.tmt.ext/", "createFolder");
    private final static QName _EditFolderResponse_QNAME = new QName("http://pm.webservice.integration.tmt.ext/", "editFolderResponse");
    private final static QName _DeleteFolder_QNAME = new QName("http://pm.webservice.integration.tmt.ext/", "deleteFolder");
    private final static QName _EditFolder_QNAME = new QName("http://pm.webservice.integration.tmt.ext/", "editFolder");
    private final static QName _Exception_QNAME = new QName("http://pm.webservice.integration.tmt.ext/", "Exception");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.sg.plmadapter.windchill
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Exception }
     * 
     */
    public Exception createException() {
        return new Exception();
    }

    /**
     * Create an instance of {@link EditFolderResponse }
     * 
     */
    public EditFolderResponse createEditFolderResponse() {
        return new EditFolderResponse();
    }

    /**
     * Create an instance of {@link EditFolder }
     * 
     */
    public EditFolder createEditFolder() {
        return new EditFolder();
    }

    /**
     * Create an instance of {@link DeleteFolder }
     * 
     */
    public DeleteFolder createDeleteFolder() {
        return new DeleteFolder();
    }

    /**
     * Create an instance of {@link DeleteFolderResponse }
     * 
     */
    public DeleteFolderResponse createDeleteFolderResponse() {
        return new DeleteFolderResponse();
    }

    /**
     * Create an instance of {@link CreateFolder }
     * 
     */
    public CreateFolder createCreateFolder() {
        return new CreateFolder();
    }

    /**
     * Create an instance of {@link CreateFolderResponse }
     * 
     */
    public CreateFolderResponse createCreateFolderResponse() {
        return new CreateFolderResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateFolderResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pm.webservice.integration.tmt.ext/", name = "createFolderResponse")
    public JAXBElement<CreateFolderResponse> createCreateFolderResponse(CreateFolderResponse value) {
        return new JAXBElement<CreateFolderResponse>(_CreateFolderResponse_QNAME, CreateFolderResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteFolderResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pm.webservice.integration.tmt.ext/", name = "deleteFolderResponse")
    public JAXBElement<DeleteFolderResponse> createDeleteFolderResponse(DeleteFolderResponse value) {
        return new JAXBElement<DeleteFolderResponse>(_DeleteFolderResponse_QNAME, DeleteFolderResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateFolder }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pm.webservice.integration.tmt.ext/", name = "createFolder")
    public JAXBElement<CreateFolder> createCreateFolder(CreateFolder value) {
        return new JAXBElement<CreateFolder>(_CreateFolder_QNAME, CreateFolder.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EditFolderResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pm.webservice.integration.tmt.ext/", name = "editFolderResponse")
    public JAXBElement<EditFolderResponse> createEditFolderResponse(EditFolderResponse value) {
        return new JAXBElement<EditFolderResponse>(_EditFolderResponse_QNAME, EditFolderResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteFolder }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pm.webservice.integration.tmt.ext/", name = "deleteFolder")
    public JAXBElement<DeleteFolder> createDeleteFolder(DeleteFolder value) {
        return new JAXBElement<DeleteFolder>(_DeleteFolder_QNAME, DeleteFolder.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EditFolder }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pm.webservice.integration.tmt.ext/", name = "editFolder")
    public JAXBElement<EditFolder> createEditFolder(EditFolder value) {
        return new JAXBElement<EditFolder>(_EditFolder_QNAME, EditFolder.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Exception }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://pm.webservice.integration.tmt.ext/", name = "Exception")
    public JAXBElement<Exception> createException(Exception value) {
        return new JAXBElement<Exception>(_Exception_QNAME, Exception.class, null, value);
    }

}
