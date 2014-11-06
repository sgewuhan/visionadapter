package com.sg.plmadapter.adapters;

import com.mobnut.db.DBActivator;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.sg.business.model.Document;
import com.sg.business.model.Folder;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.windchill.PMWebservice;

public class DocumentSync extends WindchillSyncJob {

	public DocumentSync(PMWebservice windchill, Document document) {
		super(windchill, document);
	}

	@Override
	protected void run(PMWebservice windchill, PrimaryObject po)
			throws Exception {
		Document document = (Document) po;

		String id = document.getValue(Folder.F__ID).toString();
		Object plmId = document.getValue(Document.F_PLM_ID);
		Object syncDate = document.getValue(Folder.F_SYNC_DATE);

		if (plmId == null) {// 未能同步
			if (syncDate != null) {
				cleanSyncDate(document);
			}
			windchill.createDocument(id);
		} else if (syncDate == null) {// 已经同步过，但是没有成功
			// 取出最后一次的同步请求
			Object req = document.getValue(IPLM_Object.F_SYNC_REQUEST);
			if (req == null) {// 最后一次的请求无法获取
				return;
			}
			if (IPLM_Object.REQUEST_CHANGE_REV.equals(req)) {
				windchill.changeLifecycleState(id);
			} else if (IPLM_Object.REQUEST_INSERT.equals(req)) {
				windchill.createDocument(id);
			} else if (IPLM_Object.REQUEST_MOVE.equals(req)) {
				windchill.moveDocument(id);
			} else if (IPLM_Object.REQUEST_REMOVE.equals(req)) {
				windchill.deleteDocument(id);
			} else if (IPLM_Object.REQUEST_SETLIFECYCLE.equals(req)) {
				windchill.changeLifecycleState(id);
			} else if (IPLM_Object.REQUEST_UPDATE.equals(req)) {
				windchill.updateDocument(id);
			}
		}
	}

	private void cleanSyncDate(Document document) {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_DOCUMENT);
		col.update(document.queryThis(),
				new BasicDBObject().append(Folder.F_SYNC_DATE, null));
	}

	@Override
	protected String getRequestCode() {
		return IPLM_Object.REQUEST_SYNC;
	}

}
