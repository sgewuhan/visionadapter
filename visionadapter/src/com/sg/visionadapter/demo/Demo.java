package com.sg.visionadapter.demo;

import java.util.List;

import org.bson.types.ObjectId;

import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PersistenceService;
import com.sg.visionadapter.model.Document;
import com.sg.visionadapter.model.Folder;
import com.sg.visionadapter.persistence.DocumentPersistence;
import com.sg.visionadapter.persistence.FolderPersistence;

public class Demo {


	public static void main(String[] args) {
		// 1. start
		ModelServiceFactory factory = new ModelServiceFactory();
		factory.start(null);

		DocumentPersistence docService = null;
		FolderPersistence folderService = null;
		// 2. get document persistence service
		try {
			docService = factory.get(DocumentPersistence.class);
			folderService = factory.get(FolderPersistence.class);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		if (docService == null) {
			return;
		}

		// 3. test get document data by idarray
		ObjectId[] idArray = new ObjectId[] {
				new ObjectId("5282dacae0ccf8afc27a1a95"),
				new ObjectId("5282dacae0ccf8afc27a1a9f") };
		List<Document> result = docService.getObjects(idArray);
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i).getDesc());
		}
		
//		String path = folderService.getPath(new ObjectId("5444d6f7e5abe9723f5b1333"));
//		System.out.println(path);
		//4. test set data
//		docService.update(new ObjectId("5282dacae0ccf8afc27a1a95"), "desc", "SSSSSSSS");
		
		
		
	}

}
