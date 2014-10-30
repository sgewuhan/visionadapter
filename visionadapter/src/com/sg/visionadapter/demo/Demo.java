package com.sg.visionadapter.demo;

import java.util.List;

import org.bson.types.ObjectId;

import com.sg.visionadapter.DocumentPersistence;
import com.sg.visionadapter.FolderPersistence;
import com.sg.visionadapter.ModelServiceFactory;
import com.sg.visionadapter.PMDocument;
import com.sg.visionadapter.PMFolder;

public class Demo {

	public static void main(String[] args) throws InstantiationException,
			IllegalAccessException {
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

				new ObjectId("5282dacae0ccf8afc27a1a9f"),
				new ObjectId("5289bae4e0ccf7fae13fc532") };
		List<PMDocument> result = docService.getObjects(idArray);

		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i).getCommonName());
			PMFolder folder = result.get(i).getFolder();
			if (folder != null) {
				System.out.println(folder.getCommonName());
				ObjectId parentId = folder.getParentFolderId();
				PMFolder parentFolder = folderService.get(parentId);
				System.out.println(parentFolder.getCommonName());
			}
		}

		
		//create cad document
		
//		PMCADDocument cadDocument = new PMCADDocument();
//		cadDocument.setCommonName("Í¼Ö½ABC");
//		cadDocument.setCreateBy("20092055", "ÍõÑÇÖÐ");
//		PMFolder folder = folderService.getByPLMId(plmid);
//		ObjectId id = folder.get_id();
//		cadDocument.setFolderId(folderId);
		
	}

}
