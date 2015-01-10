package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;


public class ProductPersistence extends PersistenceService<PMProduct>{

	@Override
	public String getCollectionName() {
		return "document";
	}

	
	/**
	 * ͨ��PLMId��PM�в�ѯProduct����
	 * @param PLMId
	 * @return
	 */
	public List<PMProduct> getPMObjectByPLMId(String PLMId) {
		List<PMProduct> dbos = new ArrayList<PMProduct>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMProduct.PLM_ID, PLMId));
		while(find.hasNext()) {
			PMProduct product = (PMProduct)find.next();
			product.setCollection(collection);
			dbos.add(product);
		}
		return dbos;
	}
}
