package ext.tmt.integration.webservice.spm;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.ptc.jws.servlet.JaxWsWebService;

/**
 * 时代新材Webservices发布给株洲所SPM系统调用的接口
 * 
 * @author Administrator
 *
 */
@WebService()
public class SPMWebservice extends JaxWsWebService {

	@WebMethod(operationName = "processorForSpm1")
	public String processorForSpm1(String workflow, int times, String factory)
			throws Exception {
		return SPMWebserviceImpl.processorForSpm1(workflow, times, factory);
	}

	@WebMethod(operationName = "processorForSpm2")
	public String processorForSpm2(String partNumber, String workflow,
			String mark) throws Exception {
		return SPMWebserviceImpl.processorForSpm2(partNumber, workflow, mark);
	}

	@WebMethod(operationName = "createDocForPart")
	public String createDocForPart(String partNumber, String workflow,
			String factory) throws Exception {
		return SPMWebserviceImpl
				.createDocForPart(partNumber, workflow, factory);
	}

	@WebMethod(operationName = "updateDocForPart")
	public String updateDocForPart(String partNumber, String workflow)
			throws Exception {
		return SPMWebserviceImpl.updateDocForPart(partNumber, workflow);
	}

	@WebMethod(operationName = "getJSGGSByPartNumber")
	public String getJSGGSByPartNumber(String partNumber) throws Exception {
		return SPMWebserviceImpl.getJSGGSByPartNumber(partNumber);
	}

	@WebMethod(operationName = "getCPSCByPartNumber")
	public String getCPSCByPartNumber(String partNumber) throws Exception {
		return SPMWebserviceImpl.getCPSCByPartNumber(partNumber);
	}

	@WebMethod(operationName = "checkPartFromLibrary")
	public String checkPartFromLibrary(String partNumber) throws Exception {
		return SPMWebserviceImpl.checkPartFromLibrary(partNumber);
	}

	@WebMethod(operationName = "PlmTechFileInfoToBPS")
	public String PlmTechFileInfoToBPS(String inputXml) throws Exception {
		return DocProcessor.processor(inputXml);
	}

	@WebMethod(operationName = "BacthMaterialApply")
	public String BacthMaterialApply(String inputXml) throws Exception {
		return PartProcessor.processor(inputXml);
	}

}
