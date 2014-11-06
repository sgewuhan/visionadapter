package com.sg.plmadapter.adapters;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Document;
import com.sg.business.model.Folder;
import com.sg.business.model.IPDMServiceProvider;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.windchill.PMWebservice;

public class WindchillService implements IPDMServiceProvider {

	private static final String F_PLM_TYPE = "plmtype";

	private static final String PLM_TYPE_FOLDER = "pmfolder";

	private static final String PLM_TYPE_DOCUMENT = "pmdocument";

	// private static final String F_PLM_CONTAINERNAME = "containername";

	private static final String F_PLM_ID = "plmid";

	private static final String F_SYNC_DATE = "syncdate";

	// private static final String F_PLM_DATA = "plmdata";

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
		setRequest(po, IPLM_Object.REQUEST_INSERT);
		
		Object type = po.getValue(F_PLM_TYPE);
		if (type == null) {
			if (po instanceof Folder) {
				// 如果插入前，父目录未能正确插入windchill，抛出错误
				Folder parent = ((Folder) po).getParentFolder();
				checkParentFolder(parent);
				po.setValue(F_PLM_TYPE, PLM_TYPE_FOLDER);
			} else if (po instanceof Document) {
				Folder parent = ((Document) po).getFolder();
				checkParentFolder(parent);
				po.setValue(F_PLM_TYPE, PLM_TYPE_DOCUMENT);
			}
		}
	}

	private void checkParentFolder(Folder parent) throws Exception {
		if (parent == null) {
			throw new Exception("无法获得父文件夹");
		}
		if (!parent.isContainer()) {
			Object plmid = parent.getValue(F_PLM_ID);
			if (plmid == null) {
				throw new Exception("无法获得在Windchill中的父文件夹");
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
		checkBeforeSync(po);
		if (po instanceof Folder) {
			po.setValue(F_SYNC_DATE, null);
			setRequest(po, IPLM_Object.REQUEST_UPDATE);
		} else if (po instanceof Document) {
			setRequest(po, IPLM_Object.REQUEST_UPDATE);
			po.setValue(F_SYNC_DATE, null);
		}
	}

	private void setRequest(PrimaryObject po, String requestCode) {
//		BasicBSONList request = (BasicBSONList) po
//				.getValue(IPLM_Object.F_SYNC_REQUEST);
//		if (request == null) {
//			request = new BasicDBList();
//		}
//		request.add(0, requestCode);
		po.setValue(IPLM_Object.F_SYNC_REQUEST, requestCode);
	}

	private void checkBeforeSync(PrimaryObject po) throws Exception {
		Object plmid = po.getValue(F_PLM_ID, true);
		if (plmid == null) {
			throw new Exception(po.getLabel() + "," + "无法获取Windchill对象");
		}
		Object syncDate = po.getValue(F_SYNC_DATE, true);
		if (po instanceof Document) {
			if (syncDate == null) {
				throw new Exception(po.getLabel() + "," + "未在Windchill中同步，无法更新");
			}
		}
	}

	@Override
	public void doUpdateAfter(PrimaryObject po, String[] fields,
			boolean syncExcute) throws Exception {
		checkService();

		WindchillSyncJob job = null;
		if (po instanceof Folder) {
			job = new RenameFolder(windchill, po);
		} else if (po instanceof Document) {
			job = new UpdateDocument(windchill, po);
		}
		if (job != null) {
			run(job, syncExcute);
		}
	}

	@Override
	public void doRemoveBefore(PrimaryObject po) throws Exception {
		checkBeforeSync(po);

		po.setValue(F_SYNC_DATE, null);
		setRequest(po, IPLM_Object.REQUEST_REMOVE);

		checkService();
		WindchillSyncJob job = null;
		if (po instanceof Folder) {
			job = new RemoveFolder(windchill, po);
		} else if (po instanceof Document) {
			job = new RemoveDocument(windchill, po);
		}

		if (job != null) {
			run(job, true);
		}
	}

	@Override
	public void doMoveBefore(Document document) throws Exception {
		checkBeforeSync(document);

		Folder parent = document.getFolder();
		checkParentFolder(parent);

		document.setValue(F_SYNC_DATE, null);
		setRequest(document, IPLM_Object.REQUEST_MOVE);
	}

	@Override
	public void doMoveAfter(Document document, boolean syncExcute)
			throws Exception {
		WindchillSyncJob job = null;
		checkService();
		job = new MoveDocument(windchill, document);
		if (job != null) {
			run(job, syncExcute);
		}
	}

	@Override
	public void doChangeRevisionBefore(Document document) throws Exception {
		checkBeforeSync(document);

		document.setValue(F_SYNC_DATE, null);
		setRequest(document, IPLM_Object.REQUEST_CHANGE_REV);
	}

	@Override
	public void doChangeRevisionAfter(Document document, boolean syncExcute)
			throws Exception {
		WindchillSyncJob job = null;
		checkService();
		job = new UpdateDocumentVersion(windchill, document);
		if (job != null) {
			run(job, syncExcute);
		}
	}

	@Override
	public void doSetLifeCycleStatusBefore(Document document) throws Exception {
		checkBeforeSync(document);
		document.setValue(F_SYNC_DATE, null);
		setRequest(document, IPLM_Object.REQUEST_SETLIFECYCLE);
	}

	@Override
	public void doSetLifeCycleStatusAfter(Document document, boolean syncExcute)
			throws Exception {
		WindchillSyncJob job = null;
		checkService();
		job = new SetLifeCycleStatus(windchill, document);
		if (job != null) {
			run(job, syncExcute);
		}
	}

	@Override
	public void forceSync(PrimaryObject po) throws Exception {
		WindchillSyncJob job = null;
		checkService();
		if (po instanceof Document) {
			job = new DocumentSync(windchill, (Document) po);
		} else {
			job = new FolderSync(windchill, (Folder) po);
		}
		if (job != null) {
			run(job, true);
		}
	}
}
