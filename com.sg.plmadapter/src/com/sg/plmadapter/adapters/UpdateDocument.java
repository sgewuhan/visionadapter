package com.sg.plmadapter.adapters;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.windchill.PMWebservice;

public class UpdateDocument extends WindchillSyncJob {

	public UpdateDocument(PMWebservice windchill, PrimaryObject po) {
		super(windchill, po);
	}

	@Override
	protected void run(PMWebservice windchill, PrimaryObject po)
			throws Exception {
		windchill.updateDocument(po.get_id().toString());
	}

	@Override
	protected String getRequestCode() {
		return IPLM_Object.REQUEST_UPDATE;
	}
}
