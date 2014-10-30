package com.sg.plmadapter.adapters;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.mobnut.db.model.PrimaryObject;
import com.sg.plmadapter.PLMAdapter;
import com.sg.plmadapter.windchill.PMWebservice;

public abstract class WindchillSyncJob extends Job {

	private PMWebservice windchill;
	private PrimaryObject po;

	public WindchillSyncJob(PMWebservice windchill,PrimaryObject po) {
		super("同步数据到Windchill");
		this.windchill = windchill;
		this.po = po;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			run(windchill,po);			
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(Status.ERROR, PLMAdapter.PLUGIN_ID, "同步到Windchill出现错误", e);
		}
	}
	
	public void run()throws Exception{
		run(windchill,po);			
	}

	protected abstract void run(PMWebservice windchill, PrimaryObject po) throws Exception;


}
