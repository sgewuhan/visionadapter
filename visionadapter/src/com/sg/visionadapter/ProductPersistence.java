package com.sg.visionadapter;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

public class ProductPersistence extends PersistenceService<PMProduct> {

	@Override
	public String getCollectionName() {
		return "document";
	}

	/**
	 * 通过PLMMasterId在PM中查询Product对象
	 * 
	 * @param plmMasterId
	 * @return
	 */
	public List<PMProduct> getPMObjectByPLMMasterId(String plmMasterId) {
		List<PMProduct> dbos = new ArrayList<PMProduct>();
		DBCursor find = collection.find(new BasicDBObject().append(
				PMProduct.PLM_MASTER_ID, plmMasterId));
		while (find.hasNext()) {
			PMProduct product = (PMProduct) find.next();
			product.setCollection(collection);
			dbos.add(product);
		}
		find.close();
		return dbos;
	}
}
