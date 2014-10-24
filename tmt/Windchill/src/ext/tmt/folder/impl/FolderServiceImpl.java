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
import wt.util.WTException;
import ext.tmt.folder.api.FolderService;
import ext.tmt.utils.Debug;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.GenericUtil;

/**
 * 文件夹服务
 * @author public759
 * @date 2014-10-20
 * @version V1.0
 *
 */
public class FolderServiceImpl implements FolderService ,RemoteAccess{
	
	 private static String ORG_NAME="TMT";//TMT时代新材
	 


	@Override
	public  int createFolder(String parentFolderPath,String newFolderName, String containerName,String containerType) throws WTException {
		int flag=0;
		if(StringUtils.isEmpty(containerName)){
			Debug.P("--------->>参数(containerName)为空值----->>>");
			return flag;
		}
		try {
			//查询是否存在该容器对象,没有就创建容器对象
			WTContainer container=checkWTContainer(containerName,containerType);
			String folderPath=parentFolderPath+"/"+newFolderName;
			if(!folderPath.startsWith("/")){folderPath="/"+folderPath;}
			Folder folder=FolderUtil.getFolderRef(folderPath, container, true);//如果文件目录不存在时则创建文件夹
		    if(folder!=null){
		      Debug.P("------>>>>>文件夹目录路径="+folder.getFolderPath());
		      flag=1;
		   }
		} catch (Exception e) {
			e.printStackTrace();
		}
		    return flag;
	}

	
	
	
	//修改文件夹名称
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
			  Debug.P("----------->>修改成功后的文件夹名称:"+subFolder.getName());
			  flag=1;
		  }
		} catch (Exception e) {
			e.printStackTrace();
			return flag;
		}
		  return flag;
	}



	
	//删除文件夹
	@Override
	public int deleteFolder(String folderPath, String containerName)throws WTException {
	  int flag=0;
	  if(StringUtils.isEmpty(folderPath)||StringUtils.isEmpty(containerName)) return flag;
	  try {
		 //获得文件夹对象下的所有子文件夹(从里向外删除)
		 Debug.P("------>>>>文件路径:"+folderPath+"  ;Container="+containerName);
		 WTContainer container=GenericUtil.getWTContainerByName(containerName);
		 if(container==null) return flag;
		 Folder folder=FolderUtil.getFolderRef(folderPath, container, false);
		  if(folder==null) return flag;
		 List<Folder> folderList=new ArrayList<Folder>();
		 folderList.add(folder);//添加本身
		 FolderUtil.getFolderTree(folder, folderList);//返回文件夹下的子文件对象
		 int index=folderList.size();
		 for(int i=index-1;i>=0;i--){
			 Folder subfolder=folderList.get(i);//倒叙输出文件夹
			 //删除文件夹下的对象
			QueryResult result= FolderHelper.service.findFolderContents(subfolder);//返回文件夹内的实体FolderEntry
			if(result==null) continue;
			while(result.hasMoreElements()){
				FolderEntry entry=(FolderEntry) result.nextElement();
				if(entry instanceof WTDocument){//如果是文档对象
					WTDocument wtdoc=(WTDocument)entry;
					Debug.P("------>>WTDocument_Number:"+wtdoc.getNumber());
					GenericUtil.deleteDoc(wtdoc.getNumber(), null);
				}else if(entry instanceof EPMDocument){
					EPMDocument epmdoc=(EPMDocument)entry;
					Debug.P("------>>EPMDocument_Number:"+epmdoc.getNumber());
					GenericUtil.deleteEPM(epmdoc.getNumber(), null);
				}else if(entry instanceof WTPart){
					WTPart part=(WTPart) entry;
					Debug.P("------>>WTPart_Number:"+part.getNumber());
					GenericUtil.deletePart(part.getNumber(), null);
				}
			}
			
			  //删除当前文件夹
			 PersistenceHelper.manager.delete(subfolder);
		 }
		     flag=1;
	} catch (Exception e) {
		e.printStackTrace();
		Debug.P(e.getMessage());
		return flag;
	}
	       return flag;
	}
	
	
	//文件夹中的对象移动
	@Override
	public int moveObjectFolder(String newFolderPath,String containerName,String num) throws WTException {
		int flag=0;
	    if(StringUtils.isEmpty(newFolderPath)||StringUtils.isEmpty(num)){
	    	throw new WTException("--->moveObjectFolder参数不能为空:newFolderPath="+newFolderPath+"  ;Num="+num+"  ；ContainerName="+containerName);
	    }
	    try {
	    	//目标文件夹
	    	WTContainer container=GenericUtil.getWTContainerByName(containerName);
	    	if(container==null) return flag;
			Folder folder=FolderUtil.getFolderRef(newFolderPath, container,false);
			if(folder==null)  return flag;
			//目标对象
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
	
	
	
	/**
	 * 检查是否存在该容器对象，没有就创建改容器对象
	 * @param wtContainerName
	 * @param containerType  创建容器对象的类型(1:产品库 2:存储库)
	 * @throws WTException
	 */
	public static WTContainer checkWTContainer(String wtContainerName,String containerType)throws WTException{
		
		WTContainer container=null;
		try {
			container=GenericUtil.getWTContainerByName(wtContainerName);
			if(container==null){//创建容器对象
				if("1".equals(containerType)){
					container=GenericUtil.createPDMLinkProduct(ORG_NAME, wtContainerName, "", null);//TMT是组织名称
				}else{
					container=GenericUtil.createLibrary(ORG_NAME, wtContainerName, "默认TMT存储库描述");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Debug.P(e.getMessage());
		}
		    return container;
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
//		     new FolderServiceImpl().editFolder(oldFolderPath,newFolder,containerName);//编辑文件夹名称
		//移动文件夹下的对象
//		     String newFolderPath="/TMT_Demo1_LB2/TDE001_LB201";
//		     String libName="TMT_201401_LB2";
//		     String objNum="0000000043";
//		     int flag=new FolderServiceImpl(). moveObjectFolder(newFolderPath,libName,objNum);   
//		     System.out.println("---->>Out:"+flag);
		//删除文件下下的对象 包括与之关联的关系
//		    RemoteMethodServer rms = RemoteMethodServer.getDefault();
//		    rms.setUserName("wcadmin");
//		    rms.setPassword("wcadmin");
		    String folderPath="/Default/TMT_Demo1_LB2/TDE001_LB201";
		    String containerName="TMT_201401_LB2";
		    new FolderServiceImpl().deleteFolder(folderPath, containerName);
		   
	}
	



	
	

}
