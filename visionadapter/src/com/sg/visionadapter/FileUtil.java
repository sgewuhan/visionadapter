package com.sg.visionadapter;

import java.io.File;
import java.util.Set;

public class FileUtil {

	private static final String[] OFFICE_FILE = new String[] { ".doc", ".docx", //$NON-NLS-1$ //$NON-NLS-2$
			".xls", ".xlsx", ".ppt", ".pptx" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	private static final String[] HTML_FILE = new String[] {
			".html", ".htm", ".xhtml" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String[] TEXT_FILE = new String[] { ".txt", ".log", //$NON-NLS-1$ //$NON-NLS-2$
			".xml" }; //$NON-NLS-1$
	private static final String[] PDF_FILE = new String[] { ".pdf" }; //$NON-NLS-1$
	private static final String[] IMAGE_FILE = new String[] { ".bmp", ".jpg", //$NON-NLS-1$ //$NON-NLS-2$
			".jpeg", ".png", ".tiff", ".gif", ".tif" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	private static final String[] DWG_FILE = new String[] { ".dwg" };
	public static final int FILETYPE_UNKONWN = 0;
	public static final int FILETYPE_OFFICE_FILE = 1;
	public static final int FILETYPE_HTML_FILE = 2;
	public static final int FILETYPE_PDF_FILE = 3;
	public static final int FILETYPE_IMAGE_FILE = 4;
	public static final int FILETYPE_TEXT_FILE = 5;
	public static final int FILETYPE_DWG_FILE = 6;

	public static int getFileType(File file) {
		return getFileType(file.getName());
	}

	public static int getFileType(String name) {
		String fileName = name.toLowerCase();
		int type = checkFileType(fileName, OFFICE_FILE, FILETYPE_OFFICE_FILE);
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(fileName, HTML_FILE, FILETYPE_HTML_FILE);
		} else {
			return type;
		}
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(fileName, DWG_FILE, FILETYPE_DWG_FILE);
		} else {
			return type;
		}
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(fileName, PDF_FILE, FILETYPE_PDF_FILE);
		} else {
			return type;
		}
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(fileName, IMAGE_FILE, FILETYPE_IMAGE_FILE);
		} else {
			return type;
		}
		if (type == FILETYPE_UNKONWN) {
			type = checkFileType(fileName, TEXT_FILE, FILETYPE_TEXT_FILE);
		} else {
		}
		return type;
	}

	private static int checkFileType(String fileName, String[] p, int result) {
		for (int i = 0; i < p.length; i++) {
			if (fileName.endsWith(p[i])) {
				return result;
			}
		}
		return FILETYPE_UNKONWN;
	}

	public static String checkName(String fileName, Set<String> fileNameSet) {
		fileName = fileName.replaceAll("/", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		int i = 1;
		while (fileNameSet.contains(fileName)) {
			fileName = fileName.substring(0, fileName.lastIndexOf(".")) + "(" //$NON-NLS-1$ //$NON-NLS-2$
					+ (i++) + ")" //$NON-NLS-1$
					+ fileName.substring(fileName.lastIndexOf(".")); //$NON-NLS-1$
		}
		fileNameSet.add(fileName);
		return fileName;
	}

	public static boolean canCreatePreview(String name) {
		if (name != null) {
			int fileType = getFileType(name);
			return FILETYPE_UNKONWN != fileType;
		}
		return false;
	}

}