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
		// �����Ŀ¼�Ѿ�ͬ����Windchill
		// 1. plmid��Ϊ��
		// 2. syncdate ��Ϊ��
		String id = folder.getValue(Folder.F__ID).toString();
		String folderName = folder.getDesc();
		Object plmId = folder.getValue(Folder.F_PLM_ID);
		Object syncDate = folder.getValue(Folder.F_SYNC_DATE);
		// �����Ŀ¼plmid Ϊ�գ���Ŀ¼û�ж�Ӧ��windchill����Ӧ������windchill����
		if (plmId == null) {
			if(syncDate!=null){
				cleanSyncDate(folder);
			}
			
			ArrayList<String> list = new ArrayList<String>();
			list.add(id);
			windchill.createFolder(list);
		} else {// plmid��Ϊ�գ�windchill service���ж�Ӧ�Ķ���
			if (syncDate == null) {// �Ѿ�������ͬ��ʱ�䣬����ͬ�����ɹ���Ӧ����Windchill
									// service���޷�ͬ��
				windchill.editFolder(id, folderName);
			} else {//�Ѿ����¹��������Ʋ�һ�£���Windchill��ͬ���ɹ���������ȷ�����ֿ����Ժ�С��
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
