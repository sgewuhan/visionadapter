package com.sg.plmadapter.adapters;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.windchill.PMWebservice;

public class UpdateDocumentVersion extends WindchillSyncJob {

	public UpdateDocumentVersion(PMWebservice windchill, PrimaryObject po) {
		super(windchill, po);
	}

	@Override
	protected void run(PMWebservice windchill, PrimaryObject po)
			throws Exception {
		windchill.changeRevision(po.get_id().toString());
	}
	
	@Override
	protected String getRequestCode() {
		return IPLM_Object.REQUEST_CHANGE_REV;
	}

}
