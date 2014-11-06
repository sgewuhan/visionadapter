package com.sg.plmadapter.adapters;

import java.util.ArrayList;
import java.util.List;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.windchill.PMWebservice;

public class InsertFolder extends WindchillSyncJob {

	public InsertFolder(PMWebservice windchill, PrimaryObject po) {
		super(windchill, po);
	}


	@Override
	protected void run(PMWebservice windchill, PrimaryObject po)
			throws Exception {
		List<String> folderIds = new ArrayList<String>();
		folderIds.add(po.get_id().toString());
		windchill.createFolder(folderIds);
	}

	@Override
	protected String getRequestCode() {
		return IPLM_Object.REQUEST_INSERT;
	}
}
