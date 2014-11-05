package ext.tmt.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import wt.fc.IdentityHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.folder.Cabinet;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.FolderNotFoundException;
import wt.folder.Shortcut;
import wt.folder.SubFolder;
import wt.folder.SubFolderIdentity;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.method.RemoteAccess;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;


public class FolderUtil implements RemoteAccess{

	
	
	/**
     * 得到系统文件夹结构，若系统中不存在此结构，而又不允许创建，则抛出错误，若允许则创建
     *
     * @param folderPath
     *            文件夹结构
     * @param wtcontainer
     *            容器对象
     * @param isCreate
     *            当系统不存在文件夹时，判断是否需要创建
     * @return String
     * @throws Exception
     */
    public static Folder getFolderRef(String folderPath, WTContainer wtcontainer, boolean isCreate) throws WTException {
        if (StringUtils.isEmpty(folderPath)) { return null; }
        
        Folder subfolder = null;
        String folderRef = "";
        String tempPath = folderPath;
        String splitMark = "/";
        String defaultMark = "/Default";
        if (!tempPath.startsWith(splitMark)) {
            tempPath = splitMark + tempPath;
        }

        if (!tempPath.equalsIgnoreCase(defaultMark) && !tempPath.startsWith(defaultMark)) {
            tempPath = defaultMark + tempPath;
        }
        
    	Debug.P("------  FolderPath:"+tempPath);
        String nextfolder[] = tempPath.split(splitMark);
        // System.out.println("nextfolder[]==="+nextfolder.length);
        ArrayList list = new ArrayList();
        for (int p = 0; p < nextfolder.length; p++) {
            if (nextfolder[p] != null && !nextfolder[p].trim().equals("") && !nextfolder[p].trim().equals("Default")) {
                list.add(nextfolder[p]);
            }
        }

        if (isCreate) {
            createMultiLevelDirectory(list, WTContainerRef.newWTContainerRef(wtcontainer));
        }
        
        //检查文件的存在性
        try {
        	 subfolder = FolderHelper.service.getFolder(tempPath, WTContainerRef.newWTContainerRef(wtcontainer));
		} catch (FolderNotFoundException e) {
			 Debug.P(e.getMessage());
			 throw new WTException("文件夹路径:"+tempPath+"不存在!");
			 
		}
        if (subfolder == null) {
            tempPath = "/Default";
            subfolder = FolderHelper.service.getFolder(tempPath, WTContainerRef.newWTContainerRef(wtcontainer));
        } else {
            ReferenceFactory rf = new ReferenceFactory();
            folderRef = rf.getReferenceString(ObjectReference.newObjectReference(((Persistable) subfolder)
                    .getPersistInfo().getObjectIdentifier()));
        }
        return subfolder;
    }
	
	
	/**
	 * 
	 * @param list 多级目录的各级名称集合
	 * @param wtContainerRef 对应的容器引用
	 * @return SubFolder
	 */
	public static SubFolder createMultiLevelDirectory(List<String> list,WTContainerRef wtContainerRef) {
		SubFolder subFolder = null;
		String path = ((WTContainer) wtContainerRef.getObject()).getDefaultCabinet()
				.getFolderPath();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			Folder folder = null;
			try {
				folder = FolderHelper.service.getFolder(path, wtContainerRef);
				path = path + "/" + list.get(i);
				QueryResult result = FolderHelper.service.findSubFolders(folder);
				if (!checkFolderExits(result, list.get(i))){
					subFolder = FolderHelper.service.createSubFolder(path, wtContainerRef);
				}
			} catch(FolderNotFoundException e1){
				  Debug.P(e1.getMessage());
				  e1.printStackTrace();
			 }catch (WTException e){
				e.printStackTrace();
			}
		}

		if (subFolder == null) {
			try {
				Folder folder = FolderHelper.service.getFolder(path, wtContainerRef);
				subFolder = (SubFolder) folder;
			} catch (WTException e) {
				e.printStackTrace();
			}
		}
		return subFolder;
	}

	/**
	 * @param result QueryResult
	 * @param str String
	 * @return boolean
	 */
	private static boolean checkFolderExits(QueryResult result, String str) {
		if (result == null){
			return false;
		}
		while (result.hasMoreElements()) {
			Object obj = result.nextElement();
			if (obj instanceof SubFolder) {
				SubFolder subFolder = (SubFolder) obj;
				if (subFolder.getName().equals(str)){
					return true;
				}
			}else{
				return false;
			}
		}
		return false;
	}
	
	/**
	 * @param folderPath List
	 * @return String
	 */
	public static String setFilePath(String folderPath){
		String result = folderPath;
		if (folderPath == null || folderPath.equals("")) {
			return "";
		}
		Folder subfolder = null;
		String folderRef = "";

		if (!result.startsWith("/")) {
			result = "/" + result;
		}

		if (!result.equalsIgnoreCase("/Default")
				&& !result.startsWith("/Default")) {
			result = "/Default" + result;
		}		
		return result;
	}

	
	/**
	 * 获取文件夹的对象
	 * @param folderRoute			文件夹的路径
	 * @param containerRef			容器的名称
	 * @return						文件夹对象
	 * @throws WTException
	 */
	public static Folder getFolder(String folderRoute, String containerName) throws WTException{
		Folder folder=null;
		if(StringUtils.isEmpty(containerName)) return folder;
		Debug.P("Folder容器名称-------->>>>containerName:"+containerName);
		WTContainer wtcontainer=null;
		try {
			wtcontainer = GenericUtil.getWTContainerByName(containerName);
			if(wtcontainer==null) return folder;
			folder=FolderHelper.service.getFolder(folderRoute, wtcontainer.getContainerReference());
		} catch (Exception e) {
			e.printStackTrace();
		}
		    return folder;
	}
	
	
	/**
	 * 保存一个对象到folder这个文件夹中
	 * @param entry			persistable对象
	 * @param folder			文件夹对象
	 * @throws WTException
	 */
	public static void assignLocation(FolderEntry entry, Folder folder) throws WTException{
		   FolderHelper.assignLocation(entry,folder);
	}
	
	/**
	 * 移动对象到一个文件夹下
	 * @param entry	persistable对象
	 * @param purposeFolder				
	 * @throws WTException
	 */
	public static void changeFolder(FolderEntry entry,Folder purposeFolder) throws WTException{
		if(entry!=null&&purposeFolder!=null) {
			Debug.P("--------->>>移动对象到文件:"+purposeFolder.getFolderPath());
			entry=FolderHelper.service.changeFolder(entry, purposeFolder);
			PersistenceHelper.manager.refresh(entry);
		}
		
	}

	/**
	 * 获取子文件夹
	 * @param folder
	 * @return
	 * @throws WTException
	 */
	public static Vector getSubFolder(Folder folder) throws WTException{
		Vector vec = new Vector();
		if(folder==null){
			return vec;
		}
		QueryResult qr = FolderHelper.service.findSubFolders(folder);
		while(qr.hasMoreElements()){
			Folder fold = (Folder)qr.nextElement();
			if(!vec.contains(fold)){
				vec.add(fold);
			}
		}
		return vec;
	}
	
	
	/**
	 * 修改文件夹
	 * @author qiaokaikai
	 * @param wtcontainer
	 * @param path
	 * @param s
	 * @throws WTException
	 * @throws Exception
	 * @return void
	 * @Description
	 */
	public static void editFolder(WTContainer wtcontainer,String path,String s) throws WTException, Exception{
		String folderRef = "";
		SubFolder subfolder =null;
		QueryResult qr=null;
		Folder folder = FolderHelper.service.getFolder(path, WTContainerRef.newWTContainerRef(wtcontainer));
			qr=FolderHelper.service.findSubFolders(folder) ;
			while(qr.hasMoreElements()){
				subfolder=(SubFolder)qr.nextElement();
			}
		FolderHelper.service.updateSubFolder(subfolder, s, wtcontainer.getDomainRef(), true);
	}
	
	
	
	/**
	 * 将文件名中带空格的文件复制，去掉空格
	 * @author Eilaiwang
	 * @param sourceFile
	 * @throws IOException
	 * @return void
	 * @Description
	 */
	public static void chageFile(String sourceFile) throws IOException{
	     String targetFile ="";
		if(sourceFile.contains(" ")){
			targetFile=sourceFile.replace(" ", "");
			File sFile = new File(sourceFile);
			File tFile = new File(targetFile);
			copyFile(sFile,tFile);
			sFile.delete();
		}
	}
	
	
	
	 /**
	  * 复制文件
	  * @author Eilaiwang
	  * @param sourceFile 原文件绝对路径
	  * @param targetFile 目标文件绝对路径
	  * @throws IOException
	  * @return void
	  * @Description
	  */
    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }
	
	

	/** * 修改文件夹名称 
	 *  @param oldFolderPath 需要修改名称的文件夹全路径
	 *   * @param newName 修改之后的文件夹名称 
	 *   * @return SubFolder 修改之后的文件夹 
	 *   * @throws WTException 
	 *   */
	public static SubFolder changeFolderName(String oldFolderPath, String newName,WTContainerRef containerRef) throws WTException { 
		try { 
			Debug.P("------>>原文件夹路径名称:" + oldFolderPath);
			Debug.P("------>>新文件夹名称:" + newName); 
			SubFolder subFolder = (SubFolder) FolderHelper.service.getFolder(oldFolderPath,containerRef); 
			SubFolderIdentity subFolderIdentity = (SubFolderIdentity) subFolder .getIdentificationObject(); 
			subFolderIdentity.setName(newName); 
			subFolder = (SubFolder) IdentityHelper.service.changeIdentity(subFolder, subFolderIdentity); 
			return subFolder; 
	      } catch (FolderNotFoundException e){
	            Debug.P(e.getMessage());
	            throw new WTException("以下文件夹路径在系统中不存在:" + oldFolderPath);
	        } catch (WTPropertyVetoException e) {
	        	Debug.P(e.getMessage());
	            throw new WTException("修改文件夹名称时出现错误,需要修改的文件夹路径名称为:" + oldFolderPath + " 修改后的名称为:"
	                    + newName);
	        } catch (WTException e){
	        	Debug.P(e.getMessage());
	            throw new WTException("修改文件夹名称时出现错误,需要修改的文件夹路径名称为:" + oldFolderPath + " 修改后的名称为:"+ newName);
	        }
	  }

	/**
	 * create a Folder
	 * @param name folder Name
	 * @param softType folder type
	 * @param parentFolder parent Folder
	 * @param iba  folder IBA value
	 * @return
	 * @throws Exception
	 */
	public static SubFolder createSubFolder(String name, String softType,
			Folder parentFolder, Map iba) throws Exception {
		
		SubFolder folder = null;
		try{
			
			folder = SubFolder.newSubFolder(name);
			if (softType != null && !"".equals(softType)) {
				TypeDefinitionReference typeDefinitionRef = TypedUtility
						.getTypeDefinitionReference(softType);
				if (typeDefinitionRef == null)
					throw new Exception("Not found soft type" + softType);
				folder.setTypeDefinitionReference(typeDefinitionRef);
			}
			folder.setContainer(parentFolder.getContainer());
			FolderHelper.assignLocation(folder, parentFolder);
			if (iba != null && !iba.isEmpty()) {
				LWCUtil.setValueBeforeStore(folder,iba);
			}		
			folder = (SubFolder) PersistenceHelper.manager.store(folder);
		}catch(WTException e){
			e.printStackTrace();
		}
		   return folder;
	}
	
	
	/**
	 *  删除文件夹
	 * @param  folderPath 父路径
	 * @param containerName 容器名称
	 * @return int
	 */
	public static int deleteFolder(String folderPath,String containerName){
		  int flag=0;
		 if(StringUtils.isEmpty(folderPath)||StringUtils.isEmpty(containerName)){
			 return flag;
		 }
		 //查找指定的容器名称
		 try {
			WTContainer container =GenericUtil.getWTContainerByName(containerName);
			if(container==null) return 0;
			//获得该路径下的文件夹对象
			Folder folder=FolderHelper.service.getFolder(folderPath, WTContainerRef.newWTContainerRef(container));
			//获得问价夹下的所有子文件夹
			List<Folder> list=new ArrayList<Folder>();
			getFolderTree(folder,list);
		   //反向由里到外删除文件夹和对象(包含关联关系)
		  //1.方案移动文件夹到另一个位置(建议)
			FolderEntry entry=FolderHelper.service.getFolderEntry(folderPath, WTContainerRef.newWTContainerRef(container));
		    Folder folderTarget=null;//移动的目标位置
			changeFolder(entry,folderTarget);
		//2.方案删除所属的关联关系
		 } catch (Exception e) {
			e.printStackTrace();
			Debug.P(e.getMessage());
			return flag;
		}
		   return flag;
	}
	
	
	/**
	 * 递归获取某个资料夹下的所有文件夹
	 * @param folder
	 * @param list
	 * @return
	 */
	public static List<Folder> getFolderTree(Folder folder,List<Folder> list){
		  if(folder==null){return list;}
		  try {
			   QueryResult result=FolderHelper.service.findSubFolders(folder);
			   while(result.hasMoreElements()){
				   Object obj=result.nextElement();
				   if(obj instanceof SubFolder){
					    SubFolder subFolder=(SubFolder)obj;
					    list.add(subFolder);
					    QueryResult  subresult=FolderHelper.service.findSubFolders(subFolder);
					    if(subresult.hasMoreElements()){
					    	getFolderTree(subFolder, list);
					    }
				   }
			   }
		} catch (Exception e) {
			e.printStackTrace();
		}
		       return list;
	}
	
	
	
	
}