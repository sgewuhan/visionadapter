package com.sg.visionadapter.persistence;

import com.mongodb.DBCollection;

public interface IPersistenceService {

	void setCollection(DBCollection col);

}
