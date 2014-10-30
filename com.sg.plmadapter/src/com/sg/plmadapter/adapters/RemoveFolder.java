package com.sg.plmadapter.adapters;

import com.mobnut.db.model.PrimaryObject;
import com.sg.plmadapter.windchill.PMWebservice;

public class RemoveFolder extends WindchillSyncJob {

	public RemoveFolder(PMWebservice windchill, PrimaryObject po) {
		super(windchill, po);
	}

	@Override
	protected void run(PMWebservice windchill, PrimaryObject po)
			throws Exception {
		windchill.deleteFolder(po.get_id().toString());
	}

}
