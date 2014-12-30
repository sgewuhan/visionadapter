package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class FolderPersistence extends PersistenceService<PMFolder> {

	@Override
	public String getCollectionName() {
		return "folder";
	}

	public PMFolder getFolderByPLMId(String plmFolderId) {
		PMFolder folder = (PMFolder) collection.findOne(new BasicDBObject()
				.append(PMFolder.PLM_ID, plmFolderId));
		if (folder != null) {
			folder.setCollection(collection);
		}
		return folder;
	}

	public ObjectId getFolderIdByPLMId(String plmFolderId) {
		DBObject data = collection.findOne(new BasicDBObject().append(
				PMFolder.PLM_ID, plmFolderId));
		if (data != null) {
			return (ObjectId) data.get(VisionObject._ID);
		}
		return null;
	}

	/**
	 * plm_id为空的文件夹_id
	 */
	public List<String> getNoSyncFolderId() {
		List<String> ids = new ArrayList<String>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMFolder.PLM_ID, null).append(PMFolder.F_IS_CONTAINER, new BasicDBObject("$ne",true)));
		while(find.hasNext()) {
			DBObject data = find.next();
			ObjectId id = (ObjectId) data.get(VisionObject._ID);
			String strId = id.toString();
			ids.add(strId);
		}
		return ids;
	}
	
	/**
	 * 通过pmId查询文件夹是否存在
	 * @param pmId
	 * @return
	 */
	public Boolean getFolderById(String pmId) {
		DBObject dbo = collection.findOne(new BasicDBObject().append(PMDocument._ID,
				new ObjectId(pmId)));
		if(dbo != null) {
			return true;
		}
		return false;
	}
}
