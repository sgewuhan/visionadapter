package com.sg.visionadapter.model;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.sg.visionadapter.ModelServiceFactory;

public abstract class BasicDocument extends VisionObject {
	
	protected static final String FOLDER_ID = "folder_id";

	protected static final String DOCUMENTNUMBER = "documentnumber";

	protected static final String MAJOR_VID = "major_vid";

	protected static final String SECOND_VID = "svid";

	protected static final String STATUS = "status";

	protected static final String PHASE = "phase";

	protected static final String DESCRIPTION = "description";

	protected static final String SECURITY = "security";

	/**
	 * 获得所在的目录id
	 * 
	 * @return
	 */
	public ObjectId getFolderId() {
		return (ObjectId) get(FOLDER_ID);
	}

	public void setFolderId(ObjectId folderId) {
		put(FOLDER_ID, folderId);
	}

	public Folder getFolder() {
		DBCollection folderColletion = ModelServiceFactory.service
				.getCollection("folder");
		folderColletion.setObjectClass(Folder.class);
		return (Folder) collection.findOne(new BasicDBObject().append(_ID,
				getFolderId()));
	}

	public String getObjectNumber() {
		return (String) get(DOCUMENTNUMBER);

	}

	public void setObjectNumber(String documentNumber) {
		put(DOCUMENTNUMBER, documentNumber);
	}

	public String getMajorVid() {
		return (String) get(MAJOR_VID);
	}

	public void setMajorVid(String majorVid) {
		put(MAJOR_VID, majorVid);
	}

	public int getSecondVid() {
		Integer value = (Integer) get(SECOND_VID);
		if (value != null) {
			return value.intValue();
		} else {
			return 0x0;
		}
	}

	public void setSecondVid(int secondVid) {
		put(SECOND_VID, new Integer(secondVid));

	}

	public String getStatus() {
		return (String) get(STATUS);
	}

	public void setStatus(String status) {
		put(STATUS, status);
	}

	public String getPhase() {
		return (String) get(PHASE);
	}

	public void setPhase(String phase) {
		put(PHASE, phase);
	}

	public String getDescription() {
		return (String) get(DESCRIPTION);
	}

	public void setDescription(String description) {
		put(DESCRIPTION, description);
	}

	public String getSecurity() {
		return (String) get(SECURITY);
	}

	public void setSecurity(String security) {
		put(SECURITY, security);
	}

}
