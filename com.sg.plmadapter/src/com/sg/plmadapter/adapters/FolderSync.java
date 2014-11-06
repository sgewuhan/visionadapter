package com.sg.plmadapter.adapters;

import java.util.ArrayList;

import com.mobnut.db.DBActivator;
import com.mobnut.db.model.PrimaryObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.sg.business.model.Folder;
import com.sg.business.model.IModelConstants;
import com.sg.business.model.IPLM_Object;
import com.sg.plmadapter.windchill.PMWebservice;

public class FolderSync extends WindchillSyncJob {


	public FolderSync(PMWebservice windchill, Folder folder) {
		super(windchill, folder);
	}

	@Override
	protected void run(PMWebservice windchill, PrimaryObject po)
			throws Exception {
		
		Folder folder = (Folder) po;
		// 如果该目录已经同步到Windchill
		// 1. plmid不为空
		// 2. syncdate 不为空
		String id = folder.getValue(Folder.F__ID).toString();
		String folderName = folder.getDesc();
		Object plmId = folder.getValue(Folder.F_PLM_ID);
		Object syncDate = folder.getValue(Folder.F_SYNC_DATE);
		// 如果该目录plmid 为空，该目录没有对应的windchill对象，应当调用windchill创建
		if (plmId == null) {
			if(syncDate!=null){
				cleanSyncDate(folder);
			}
			
			ArrayList<String> list = new ArrayList<String>();
			list.add(id);
			windchill.createFolder(list);
		} else {// plmid不为空，windchill service端有对应的对象
			if (syncDate == null) {// 已经重设了同步时间，但是同步不成功，应当是Windchill
									// service端无法同步
				windchill.editFolder(id, folderName);
			} else {//已经更新过，但名称不一致，是Windchill端同步成功，但不正确（这种可能性很小）
				Object plmData = folder.getValue(Folder.F_PLM_DATA);
				if(plmData instanceof DBObject){
					Object plmName = ((DBObject) plmData).get("name");
					if(!folderName.equals(plmName)){
						folder.setValue(Folder.F_SYNC_DATE, null);
						cleanSyncDate(folder);
						windchill.editFolder(id, folderName);
					}
				}
				
			}
		}

	}

	private void cleanSyncDate(Folder folder) {
		DBCollection col = DBActivator.getCollection(IModelConstants.DB,
				IModelConstants.C_FOLDER);
		col.update(folder.queryThis(),
				new BasicDBObject().append(Folder.F_SYNC_DATE, null));
	}

	@Override
	protected String getRequestCode() {
		return IPLM_Object.REQUEST_SYNC;
	}
}
