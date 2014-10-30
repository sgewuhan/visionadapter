package com.sg.plmadapter.adapters;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Document;
import com.sg.business.model.Folder;
import com.sg.business.model.IPDMServiceProvider;
import com.sg.plmadapter.windchill.PMWebservice;

public class WindchillService implements IPDMServiceProvider {

	private static final String F_PLM_TYPE = "plmtype";

	private static final String PLM_TYPE_FOLDER = "pmfolder";

	private static final String PLM_TYPE_DOCUMENT = "pmdocument";

	// private static final String F_PLM_CONTAINERNAME = "containername";

	private static final String F_PLM_ID = "plmid";

	private static final String F_SYNC_DATE = "syncdate";

	private static final String F_PLM_DATA = "plmdata";

	private String url;

	private String username;

	private String password;

	private PrimaryObject po;

	private PMWebservice windchill;

	public WindchillService(String url, String username, String password,
			PrimaryObject po) {
		this.url = url;
		this.username = username;
		this.password = password;
		this.po = po;
		// init();
		initService();
	}

	private void initService() {
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setAddress(url);
		factory.setUsername(username);
		factory.setPassword(password);
		factory.setServiceClass(PMWebservice.class);
		windchill = (PMWebservice) factory.create();
	}

	/*
	 * private void init() throws MalformedURLException { PMWebserviceService
	 * service = new PMWebserviceService(new URL(url)); windchill =
	 * service.getPort(PMWebservice.class); Client client =
	 * ClientProxy.getClient(windchill);
	 * 
	 * HTTPConduit http = (HTTPConduit) client.getConduit();
	 * 
	 * HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
	 * httpClientPolicy.setConnectionTimeout(36000);
	 * httpClientPolicy.setAllowChunking(false);
	 * httpClientPolicy.setReceiveTimeout(32000);
	 * 
	 * AuthorizationPolicy policy = new AuthorizationPolicy();
	 * policy.setAuthorizationType("Basic"); policy.setUserName(username);
	 * policy.setPassword(password);
	 * 
	 * http.setAuthorization(policy); http.setClient(httpClientPolicy);
	 * 
	 * }
	 */

	@Override
	public void doInsertBefore(PrimaryObject po) throws Exception {
		po.setValue(F_SYNC_DATE, null);
		Object type = po.getValue(F_PLM_TYPE);
		if (type == null) {
			if (po instanceof Folder) {
				po.setValue(F_PLM_TYPE, PLM_TYPE_FOLDER);
			} else if (po instanceof Document) {
				po.setValue(F_PLM_TYPE, PLM_TYPE_DOCUMENT);
			}
		}
	}

	@Override
	public int doInsertAfter(PrimaryObject po) throws Exception {
		if (po instanceof Folder) {
			List<String> folderIds = new ArrayList<String>();
			folderIds.add(po.get_id().toString());
			return windchill.createFolder(folderIds);
		} else if (po instanceof Document) {
			String documentId = po.get_id().toString();
			return windchill.createDocument(documentId);
		}
		return 0;
	}

	@Override
	public void doUpdateBefore(PrimaryObject po, String[] fields)
			throws Exception {
		po.setValue(F_SYNC_DATE, null);
	}

	@Override
	public int doUpdateAfter(PrimaryObject po, String[] fields)
			throws Exception {
		if (po instanceof Folder) {
			String id = po.get_id().toString();
			String newFolderName = (String) po.getValue(fields[0]);
			return windchill.editFolder(id, newFolderName);
		}else if(po instanceof Document) {
			String id = po.get_id().toString();
			windchill.updateDocument(id);
		}
		return 0;
	}

	@Override
	public int doRemove(PrimaryObject po) throws Exception {
		if(po instanceof Folder) {
			String folderId = po.get_id().toString();
			return windchill.deleteFolder(folderId);
		}else if(po instanceof Document) {
			String documentId = po.get_id().toString();
			return windchill.deleteDocument(documentId);
		}
		return 0;
	}
	
}
