package com.sg.plmadapter.adapters;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.sg.plmadapter.PLMAdapter;
import com.sg.plmadapter.windchill.Exception_Exception;
import com.sg.plmadapter.windchill.PMWebservice;

public class RenameFolder extends Job {

	private PMWebservice windchill;
	private String newFolderName;
	private String id;

	public RenameFolder(PMWebservice windchill, String id,
			String newFolderName) {
		super("同步创建Windchill文件夹");
		this.windchill = windchill;
		this.id = id;
		this.newFolderName = newFolderName;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			windchill.editFolder(id, newFolderName);
			return Status.OK_STATUS;
		} catch (Exception_Exception e) {
			return new Status(Status.ERROR, PLMAdapter.PLUGIN_ID,
					"同步到Windchill出现错误", e);
		}
	}

}
