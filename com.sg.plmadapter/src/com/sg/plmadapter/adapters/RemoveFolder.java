package com.sg.plmadapter.adapters;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.sg.plmadapter.PLMAdapter;
import com.sg.plmadapter.windchill.Exception_Exception;
import com.sg.plmadapter.windchill.PMWebservice;

public class RemoveFolder extends Job {

	private PMWebservice windchill;
	private String id;

	public RemoveFolder(PMWebservice windchill, String id) {
		super("ͬ��ɾ��Windchill�ļ���");
		this.windchill = windchill;
		this.id = id;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			windchill.deleteFolder(id);
			return Status.OK_STATUS;
		} catch (Exception_Exception e) {
			return new Status(Status.ERROR, PLMAdapter.PLUGIN_ID, "ͬ����Windchill���ִ���", e);
		}
	}


}
