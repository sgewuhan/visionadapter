package com.sg.plmadapter.adapters;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.PLMAdapter;
import com.sg.plmadapter.windchill.PMWebservice;

public abstract class WindchillSyncJob extends Job {

	private PMWebservice windchill;
	private PrimaryObject po;

	public WindchillSyncJob(PMWebservice windchill, PrimaryObject po) {
		super("同步数据到Windchill");
		this.windchill = windchill;
		this.po = po;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			startRequest();
			run(windchill, po);
			finishRequest();
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(Status.ERROR, PLMAdapter.PLUGIN_ID,
					"同步到Windchill出现错误", e);
		}
	}

	public void run() throws Exception {
		run(windchill, po);
		finishRequest();
	}

	protected abstract void run(PMWebservice windchill, PrimaryObject po)
			throws Exception;

	protected void finishRequest() {
		DBCollection col = po.getCollection();
		col.update(po.queryThis(), new BasicDBObject().append("$set",
				new BasicDBObject().append(IPLM_Object.F_SYNC_REQUEST, null)));
	}

	
	protected void startRequest() {
		DBCollection col = po.getCollection();
		col.update(po.queryThis(), new BasicDBObject().append("$set",
				new BasicDBObject().append(IPLM_Object.F_SYNC_REQUEST, getRequestCode())));
	}

	protected abstract  String getRequestCode();

}
