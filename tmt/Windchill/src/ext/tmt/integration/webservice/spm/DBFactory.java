package ext.tmt.integration.webservice.spm;

import java.io.File;
import java.sql.SQLException;

import wt.util.WTException;
import ext.tmt.integration.webservice.spm.ConnectionPool.PooledConnection;
import ext.tmt.utils.Debug;

/**
 * 数据库工厂
 * @author Administrator
 *
 */
public class DBFactory {
	
	
	
	private static String PERPERTIES_PATH = "codebase" + File.separator + "ext"
			+ File.separator + "tmt" + File.separator + "integration"
			+ File.separator + "webservice" + File.separator +"spm"+ File.separator+ "db.properties";
	  
	
	/**
	 * 获得数据池对象
	 * @return
	 * @throws WTException
	 */
	public static ConnectionPool  getConnectionPool() throws WTException{
		PropertiesUtil pu = null;
		ConnectionPool connectionPool=null;
		try {
			pu = new PropertiesUtil(PERPERTIES_PATH);
		} catch(WTException e) {
			Debug.P("无法获取DB配置文件!");
			throw new WTException("无法获取DB配置文件!");
		}
		
		String driver = pu.getValue("DRIVERNAME");
		String dbURL = pu.getValue("URL");
		String user = pu.getValue("USERNAME");
		String password = pu.getValue("PASSWORD");
	    try {
	    	connectionPool= new ConnectionPool(driver,dbURL,user,password);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
          return connectionPool;
	}
	

}
