package ext.tmt.integration.webservice.spm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ext.tmt.utils.Debug;
import wt.util.WTException;
import wt.util.WTProperties;

public class PropertiesUtil {

	private HashMap<String, String> hmConfig = new HashMap<String, String>();
	public static boolean VERBOSE = false;

	public PropertiesUtil(String configFilePath) throws WTException {
		readConfig(configFilePath);
	}

	public PropertiesUtil() {
	}

	public void readConfig(String configFilePath) throws WTException {
		Properties pro = new Properties();
		try {
			String wthome = (String) (WTProperties.getLocalProperties())
					.getProperty("wt.home", "");
			String tempPath = wthome + File.separator + configFilePath;
			System.out.println(tempPath);
			FileInputStream fis = new FileInputStream(tempPath);
			pro.load(new InputStreamReader(fis, "UTF-8"));
			pro.load(fis);
			Enumeration<?> e = pro.propertyNames();
			while (e.hasMoreElements()) {
				String proName = (String) e.nextElement();
				String value = (String) pro.getProperty(proName);
				Debug.P(proName);
				Debug.P(value);
				hmConfig.put(proName, value);
			}
			fis.close();
		} catch (FileNotFoundException ex) {
			throw new WTException("读取配置文件出错!");
		} catch (IOException ex) {
			throw new WTException("读取配置文件内容出错!");
		}
	}

	public String getValue(String key) {
		String strValue = (String) hmConfig.get(key);
		if (strValue == null || "".equals(strValue))
			return "";
		strValue = strValue.trim();
		return strValue;
	}

	public Map<String, String> getAllKeysValues() {
		Map<String, String> allKeysValues = new HashMap<String, String>();
		allKeysValues = hmConfig;
		return allKeysValues;
	}
}
