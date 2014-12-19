package com.sg.visionadapter;

import org.bson.types.ObjectId;

import com.mongodb.WriteResult;

public class PMProductItem extends VisionObject {
 
	@Override
	public PMFolder getParentFolder() {
		return null;
	}

	public void setProductNumber(String productNumber) {
		setValue(DESC, productNumber);
	}
	
	public void setProjectId(ObjectId projectId) {
		setValue(PROJECT_ID,projectId);
	}
	
	@Override
	public WriteResult doInsert() throws Exception {
		return super.doInsertSimple();
	}
}
