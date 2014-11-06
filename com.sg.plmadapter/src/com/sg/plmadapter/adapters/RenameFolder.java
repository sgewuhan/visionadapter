package com.sg.plmadapter.adapters;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.windchill.PMWebservice;

public class RenameFolder extends WindchillSyncJob {


	public RenameFolder(PMWebservice windchill, PrimaryObject po) {
		super(windchill,po);
	}

	@Override
	protected void run(PMWebservice windchill, PrimaryObject po)
			throws Exception {
		windchill.editFolder(po.get_id().toString(), po.getDesc());
	}
	
	@Override
	protected String getRequestCode() {
		return IPLM_Object.REQUEST_UPDATE;
	}

}
