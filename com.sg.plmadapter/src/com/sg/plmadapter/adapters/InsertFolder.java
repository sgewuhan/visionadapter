package com.sg.plmadapter.adapters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.sg.plmadapter.PLMAdapter;
import com.sg.plmadapter.windchill.Exception_Exception;
import com.sg.plmadapter.windchill.PMWebservice;

public class InsertFolder extends Job {

	private PMWebservice windchill;
	private String id;

	public InsertFolder(PMWebservice windchill, String id) {
		super("同步创建Windchill文件夹");
		this.windchill = windchill;
		this.id = id;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		List<String> folderIds = new ArrayList<String>();
		folderIds.add(id);
		try {
			windchill.createFolder(folderIds);
			return Status.OK_STATUS;
		} catch (Exception_Exception e) {
			return new Status(Status.ERROR, PLMAdapter.PLUGIN_ID, "同步到Windchill出现错误", e);
		}
	}


}
