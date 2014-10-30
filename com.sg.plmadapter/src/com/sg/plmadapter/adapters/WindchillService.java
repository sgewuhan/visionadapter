package com.sg.plmadapter.adapters;

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

	private PMWebservice windchill;

	public WindchillService(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
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

	private void checkService() throws Exception {
		if (windchill == null) {
			throw new Exception("Windchill 同步服务不可用");
		}
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
	public void doInsertAfter(PrimaryObject po, boolean syncExcute)
			throws Exception {
		checkService();
		WindchillSyncJob job = null;
		if (po instanceof Folder) {
			job = new InsertFolder(windchill, po);
		} else if (po instanceof Document) {
			job = new InsertDocument(windchill, po);
		}
		if (job != null) {
			run(job, syncExcute);
		}
	}

	private void run(WindchillSyncJob job, boolean syncExcute) throws Exception {
		if (syncExcute) {
			job.run();
		} else {
			job.schedule();
		}
	}

	@Override
	public void doUpdateBefore(PrimaryObject po, String[] fields)
			throws Exception {
		po.setValue(F_SYNC_DATE, null);
	}

	@Override
	public void doUpdateAfter(PrimaryObject po, String[] fields,
			boolean syncExcute) throws Exception {
		checkService();

		WindchillSyncJob job = null;
		if (po instanceof Folder) {
			job = new RenameFolder(windchill, po);
		}
		
		if (job != null) {
			run(job, syncExcute);
		}
	}

	@Override
	public void doRemoveAfter(PrimaryObject po, boolean syncExcute)
			throws Exception {
		checkService();
		WindchillSyncJob job = null;

		if (po instanceof Folder) {
			checkService();
			job = new RemoveFolder(windchill, po);
		}
		
		if (job != null) {
			run(job, syncExcute);
		}
	}

	@Override
	public void doRemoveBefore(PrimaryObject po) throws Exception {
		po.setValue(F_SYNC_DATE, null);
	}

}
