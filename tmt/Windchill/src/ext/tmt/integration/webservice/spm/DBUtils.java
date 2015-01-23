package ext.tmt.integration.webservice.spm;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import ext.tmt.utils.Debug;
import wt.util.WTException;


/**
 * 废弃类
 * @author Administrator
 *
 */
@Deprecated
public class DBUtils {


	
	private static String PERPERTIES_PATH = "codebase" + File.separator + "ext"
			+ File.separator + "tmt" + File.separator + "integration"
			+ File.separator + "webservice" + File.separator +"spm"+ File.separator+ "db.properties";
	
	
	
	public static Connection conn;

	public static Connection getConnection() throws WTException {
		PropertiesUtil pu = null;
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
			Class.forName(driver);
			conn = DriverManager.getConnection(dbURL, user, password);
		} catch (ClassNotFoundException e ) {
			Debug.P("无效的驱动名"+driver);
			throw new WTException("无效的驱动名" + driver);
		} catch (SQLException e ) {
			Debug.P("连接数据库出错!");
			throw new WTException("连接数据库出错!");
		}
		return conn;
	}
	
	  public static int executeUpdate(Connection conn, String sql) {
	        int num = 0;
	        try {
	            Statement stmt = conn.createStatement();
	            num = stmt.executeUpdate(sql);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return num;
	    }

	    public static ResultSet executeQuery(Connection conn, String sql) {
	        ResultSet result = null;
	        try {
	            Statement stmt = conn.createStatement();
	            result = stmt.executeQuery(sql);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return result;
	    }

	
	
	
	public static void closeConnection() throws WTException {
		try {
			if(conn != null) {
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			Debug.P("无法关闭数据库连接!");
			throw new WTException("无法关闭数据库连接!");
		} finally {
			if(conn != null) {
				try {
					conn.close();
				} catch(SQLException e ) {
					Debug.P("无法关闭数据库连接!");
					throw new WTException("无法关闭数据库连接!");
				}
				conn = null;
			}
		}
	}
}
