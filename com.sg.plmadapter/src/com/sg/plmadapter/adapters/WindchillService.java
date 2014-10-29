package com.sg.plmadapter.adapters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Document;
import com.sg.business.model.Folder;
import com.sg.business.model.IPDMServiceProvider;
import com.sg.plmadapter.windchill.PMWebservice;
import com.sg.plmadapter.windchill.PMWebserviceService;

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
		try {
			init();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private void init() throws MalformedURLException {
		PMWebserviceService service = new PMWebserviceService(new URL(url));
		windchill = service.getPort(PMWebservice.class);
		Client client = ClientProxy.getClient(windchill);
		HTTPConduit http = (HTTPConduit) client.getConduit();

		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(36000);
		httpClientPolicy.setAllowChunking(false);
		httpClientPolicy.setReceiveTimeout(32000);

		AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy();
		authorizationPolicy.setAuthorizationType("Basic");
		authorizationPolicy.setUserName(username);
		authorizationPolicy.setPassword(password);
		
		http.setAuthorization(authorizationPolicy);
		http.setClient(httpClientPolicy);
	}

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
	public void doInsertAfter(PrimaryObject po) throws Exception {
		if(po instanceof Folder){
			List<String> folderIds = new ArrayList<String>();
			folderIds.add(po.get_id().toString());
			windchill.createFolder(folderIds);
		}
	}

	@Override
	public void doUpdateBefore(PrimaryObject po, String[] fields)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void doUpdateAfter(PrimaryObject po, String[] fields)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
