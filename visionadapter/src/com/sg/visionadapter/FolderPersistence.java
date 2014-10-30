package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
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

}
