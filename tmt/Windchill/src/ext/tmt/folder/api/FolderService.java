package ext.tmt.folder.api;

import wt.folder.Folder;
import wt.util.WTException;

/**
 * 在PM中对PLM文件夹的操作接口
 * 创建、编辑、添加、删除、搜索
 * @author public759
 * @date 2014-10-17
 * @version V1.0
 *
 */
public interface FolderService {
	
	/**
	 * 创建文件夹(如果有多级目录则创建层级结构)
	 * @param parentFolderPath 父路径
	 * @param newFolderName 新文件夹名称
	 * @param container 容器名称
	 * @param containerType 容器类型
	 * @return int 0:创建失败 ;1：创建成功
	 * @throws WTException
	 */
	public Folder createFolder(String parentFolderPath,String newFolderName,String containerName,String containerType)throws WTException;
	
	
	
	/**
	 * 编辑文件夹
	 * @param targetPath   原文件夹全路径
	 * @param newFolderName 新文件夹名称
	 * @param container 容器名称
	 * @return int 0:更新失败 ;1：更新成功
	 * @throws WTException
	 */
	public int editFolder(String targetPath,String newFolderName,String containerName)throws WTException;
	
	
	
	/**
	 * 删除文件夹
	 * @param folderPath
	 * @param containerName
	 * @return
	 * @throws WTException
	 */
	public int deleteFolder(String folderPath,String containerName)throws WTException;
	
	
	/**
	 * 移动对象到另外的文件夹下
	 * @param newFolderPath 移动后的位置
	 * @param container 容器名称
	 * @param num 对象编号
	 * @return
	 * @throws WTException
	 */
	public int moveObjectFolder(String newFolderPath,String container,String num)throws WTException;
	
  

	
}
