package com.sg.visionadapter;

import com.mongodb.DBCollection;

public interface IPersistenceService {

	void setCollection(DBCollection col);

	String getCollectionName();
}
