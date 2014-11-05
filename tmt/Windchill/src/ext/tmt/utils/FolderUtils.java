package ext.tmt.utils;

import java.io.File;

/**
 * 文件夹操作类
 * 
 * @author Tony
 * 
 */
public class FolderUtils {

	/**
	 * 创建文件夹，创建A0,A1,A2,A3,A4
	 * 
	 * @param filePath
	 * @param folderName
	 * @return
	 */
	public static String createFolder(String filePath, String folderName) {
		File dir = new File(filePath);
		if (!dir.exists())
			dir.mkdir();
		for (int i = 0; i <= 4; i++) {
			File subFolder = new File(filePath + File.separator + "A" + String.valueOf(i));
			if (!subFolder.exists())
				subFolder.mkdir();
		}
		return dir.getAbsolutePath();
	}
	
	/**
	 * 创建文件夹
	 * 
	 * @param filePath
	 * @param folderName
	 * @return
	 */
	public static String createFolder(String filePath) {
		File dir = new File(filePath);
		if (!dir.exists())
			dir.mkdir();
		return dir.getAbsolutePath();
	}

	/**
	 * 删除文件夹
	 * @param path
	 */
	public static void deleteFolder(String path) {
		File file = new File(path);
		if (file.exists()) {
			if (file.isFile()) {
				deleteFile(path);
			} else {
				deleteDirectory(path);
			}
		}
	}

	/**
	 * 删除一个目录
	 * @param filePath
	 */
	private static void deleteDirectory(String filePath) {
		if (!filePath.endsWith(File.separator)) {
			filePath = filePath + File.separator;
		}
		File file = new File(filePath);
		if (!file.exists() && !file.isDirectory()) {
			return;
		} else {

			File[] fl = file.listFiles();
			for (int i = 0; i < fl.length; i++) {
				if (fl[i].isFile()) {
					deleteFile(fl[i].getAbsolutePath());
				} else {
					deleteDirectory(fl[i].getAbsolutePath());
				}
			}
		}
		file.delete();

	}
  /**
   * 删除一个文件
   * @param filePath
   */
	private static void deleteFile(String filePath) {

		File file = new File(filePath);
		
		if (file.isFile() && file.exists())
			file.delete();
	}
	
	public static void main(String[] args) {
		deleteFolder("D:\\ptc\\Windchill_10.1\\Windchill\\temp\\SHV4506-0100-2");
	}
}
