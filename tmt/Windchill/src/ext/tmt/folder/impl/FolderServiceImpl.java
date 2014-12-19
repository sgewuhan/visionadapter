package ext.tmt.folder.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.util.WTException;
import ext.tmt.folder.api.FolderService;
import ext.tmt.utils.Debug;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.GenericUtil;

/**
 * �ļ��з���
 * @author public759
 * @date 2014-10-20
 * @version V1.0
 *
 */
public class FolderServiceImpl implements FolderService ,RemoteAccess{
	
	 private static String ORG_NAME="TMT";//TMTʱ���²�
	 


	@Override
	public  Folder createFolder(String parentFolderPath,String newFolderName, String containerName,String containerType) throws WTException {
		Folder folderResult=null;
		if(StringUtils.isEmpty(containerName)){
			Debug.P("--------->>����(containerName)Ϊ��ֵ----->>>");
			return folderResult;
		}
		try {
			//��ѯ�Ƿ���ڸ���������,û�оʹ�����������
			WTContainer container=GenericUtil.checkWTContainer(ORG_NAME,containerName,containerType);
			String folderPath=null;
			if(!StringUtils.isEmpty(newFolderName)){
				folderPath=parentFolderPath+"/"+newFolderName;
			}else{
				folderPath=parentFolderPath;
			}
			if(!folderPath.startsWith("/")){folderPath="/"+folderPath;}
			Folder folder=FolderUtil.getFolderRef(folderPath, container, true);//����ļ�Ŀ¼������ʱ�򴴽��ļ���
		    if(folder!=null){
		       Debug.P("------>>>>>�ļ���Ŀ¼·��="+folder.getFolderPath());
		       folderResult=folder;
		   }
		} catch (Exception e) {
			e.printStackTrace();
		}
		    return folderResult;
	}

	
	
	
	//�޸��ļ�������
	@Override
	public int editFolder(String targetPath, String newFolderName,
			String containerName) {
		 int flag=0;
		if(StringUtils.isEmpty(targetPath)||StringUtils.isEmpty(containerName)||StringUtils.isEmpty(newFolderName)) return flag;
		Debug.P("--------------->>FolderPath:"+targetPath);
		if(!targetPath.startsWith("/")){targetPath="/"+targetPath;}
		try {
			WTContainer  wtcontainer = GenericUtil.getWTContainerByName(containerName);
			 if(wtcontainer==null) return flag;
			 if(!targetPath.startsWith("/Default")){
				 targetPath="/Default"+targetPath;
			 }
		  SubFolder subFolder=FolderUtil.changeFolderName(targetPath, newFolderName,WTContainerRef.newWTContainerRef(wtcontainer));
		  if(subFolder!=null){
			  Debug.P("----------->>�޸ĳɹ�����ļ�������:"+subFolder.getName());
			  flag=1;
		  }
		} catch (Exception e) {
			e.printStackTrace();
			return flag;
		}
		  return flag;
	}



	
	//ɾ���ļ���
	@Override
	public int deleteFolder(String folderPath, String containerName)throws WTException {
	  int flag=0;
	  if(StringUtils.isEmpty(folderPath)||StringUtils.isEmpty(containerName)) return flag;
	  Transaction tx=null;
	  try {
		 //����ļ��ж����µ��������ļ���(��������ɾ��)
		 Debug.P("------>>>>�ļ�·��:"+folderPath+"  ;Container="+containerName);
		 tx = new Transaction();
		 tx.start();
		 WTContainer container=GenericUtil.getWTContainerByName(containerName);
		 if(container==null) return flag;
		 Folder folder=FolderUtil.getFolderRef(folderPath, container, false);
		  if(folder==null) return flag;
		 List<Folder> folderList=new ArrayList<Folder>();
		 folderList.add(folder);//��ӱ���
		 FolderUtil.getFolderTree(folder, folderList);//�����ļ����µ����ļ�����
		 int index=folderList.size();

		 for(int i=index-1;i>=0;i--){
			 Folder subfolder=folderList.get(i);//��������ļ���
			 Debug.P("---->>>FolderName:"+subfolder.getName());
			 //ɾ���ļ����µĶ���
			QueryResult result= FolderHelper.service.findFolderContents(subfolder);//�����ļ����ڵ�ʵ��FolderEntry
			if(result==null) continue;
			Debug.P("----->>>Delete Folder Entry  Start-----");
			while(result.hasMoreElements()){
				FolderEntry entry=(FolderEntry) result.nextElement();
				if(entry instanceof WTDocument){//������ĵ�����
					WTDocument wtdoc=(WTDocument)entry;
					Debug.P("------>>WTDocument_Number:"+wtdoc.getNumber());
					GenericUtil.deleteDoc(wtdoc, null);
				}else if(entry instanceof EPMDocument){
					EPMDocument epmdoc=(EPMDocument)entry;
					Debug.P("------>>EPMDocument_Number:"+epmdoc.getNumber());
					GenericUtil.deleteEPM(epmdoc, null);
				}else if(entry instanceof WTPart){
					WTPart part=(WTPart) entry;
					Debug.P("------>>WTPart_Number:"+part.getNumber());
					GenericUtil.deletePart(part, null);
				}
			}
			
			 PersistenceHelper.manager.refresh(subfolder);
			  //ɾ����ǰ�ļ���
			 PersistenceHelper.manager.delete(subfolder);
		 }
		    tx.commit();
		    tx=null;
		     flag=1;
	} catch (Exception e) {
		e.printStackTrace();
		Debug.P(e.getMessage());
		return flag;
	}finally{
		if(tx!=null){
			tx.rollback();
		}
	}
	       return flag;
	}
	
	
	//�ļ����еĶ����ƶ�
	@Override
	public int moveObjectFolder(String newFolderPath,String containerName,String num) throws WTException {
		int flag=0;
	    if(StringUtils.isEmpty(newFolderPath)||StringUtils.isEmpty(num)){
	    	throw new WTException("--->moveObjectFolder��������Ϊ��:newFolderPath="+newFolderPath+"  ;Num="+num+"  ��ContainerName="+containerName);
	    }
	    try {
	    	//Ŀ���ļ���
	    	WTContainer container=GenericUtil.getWTContainerByName(containerName);
	    	if(container==null) return flag;
			Folder folder=FolderUtil.getFolderRef(newFolderPath, container,false);
			if(folder==null)  return flag;
			//Ŀ�����
			Persistable persistable=GenericUtil.getObjectByNumber(num);
			if(persistable!=null){
				FolderUtil.changeFolder((FolderEntry)persistable, folder);
				flag=1;
			}
           
		} catch (InstantiationException e) {
			e.printStackTrace();
			Debug.P("-->InstantiationException:"+e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Debug.P("-->IllegalAccessException:"+e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			Debug.P(e);
		}
		    return flag;
	}
	
	
	


	

	

	
	
	public static void main(String[] args) throws RemoteException, WTException {
		//Create Folder
//		  String parentFolderPath="/TMT_Demo1_LB2";
//		  String newFolderName="TDE001_LB2";
//		  String containerName="TMT_201401_LB2";
//		  int flag= new FolderServiceImpl().createFolder(parentFolderPath,newFolderName,containerName,"0");
//		  System.out.println("----OutPut:"+flag);
		//Edit Folder
//		     String oldFolderPath="TMT_Demo1_LB2/TDE001_LB2";
//		     String newFolder="TDE001_LB201";
//		     String containerName="TMT_201401_LB2";
//		     new FolderServiceImpl().editFolder(oldFolderPath,newFolder,containerName);//�༭�ļ�������
		//�ƶ��ļ����µĶ���
//		     String newFolderPath="/TMT_Demo1_LB2/TDE001_LB201";
//		     String libName="TMT_201401_LB2";
//		     String objNum="0000000043";
//		     int flag=new FolderServiceImpl(). moveObjectFolder(newFolderPath,libName,objNum);   
//		     System.out.println("---->>Out:"+flag);
		//ɾ���ļ����µĶ��� ������֮�����Ĺ�ϵ
//		    RemoteMethodServer rms = RemoteMethodServer.getDefault();
//		    rms.setUserName("wcadmin");
//		    rms.setPassword("wcadmin");
		    String folderPath="/Default/TMT_Demo1_LB2/TDE001_LB201";
		    String containerName="TMT_201401_LB2";
		    new FolderServiceImpl().deleteFolder(folderPath, containerName);
		   
	}
	



	
	

}
