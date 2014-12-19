package ext.tmt.utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import wt.auth.SimpleAuthenticator;
import wt.method.MethodContext;
import wt.method.MethodServerException;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.pom.DataServicesRegistry;
import wt.pom.Transaction;
import wt.pom.WTConnection;


/**
 * 用户自定义SQL语句查询
 * @author qwx80633
 *
 */
public class UserDefQueryUtil implements RemoteAccess,Serializable {

	private static final long serialVersionUID = 7908695588227993764L;
	
	
	
	/**
	 * 根据SQL语句查询结果集
	 * @author qwx80633
	 * @param sql 查询语句
	 * @param param 查询参数
	 * @return 数据库列对应的值
	 * @throws Exception
	 */
	public static List<Hashtable<String,String>> commonQuery(String  sql,String[] params) throws Exception{
		
		if(!RemoteMethodServer.ServerFlag){
			List list=null;
			Class[] cls={String.class,String[].class};
			Object[] obj={sql,params};
			RemoteMethodServer rms=RemoteMethodServer.getDefault();
			try {
				list=(List) rms.invoke("commonQuery", UserDefQueryUtil.class.getName(), null, cls, obj);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			  return list;
		}else{
			 List<Hashtable<String, String>> result=new ArrayList<Hashtable<String,String>>();
			 WTConnection connection=null;
			 PreparedStatement prement=null;
			 ResultSet rs=null;
			 //获取上下文对象
			 MethodContext context=getMethodContext();
			 try {
				 connection=(WTConnection) context.getConnection();
				 //拼接查询条件
				 prement=connection.prepareStatement(sql);
				 if(prement!=null){
					 for(int i=1;i<=params.length;i++){
						 prement.setString(i, params[i-1]);
					 }
				 }
				 rs=prement.executeQuery();
				 ResultSetMetaData metaData=rs.getMetaData();//元数据
				 int column=metaData.getColumnCount();
				 while(rs.next()){
					 Hashtable<String, String> htb=new Hashtable<String,String>();
					 for(int j=1;j<=column;j++){
						 String columnName=metaData.getColumnName(j);
						 String value=rs.getString(columnName);
						 htb.put(columnName, value==null?"":value);
					 }
					 if(htb.size()>0){
						 result.add(htb);
					 }
				 }
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(rs!=null){
					rs.close();
				}
				if(prement!=null){
					prement.close();
				}
				if(connection!=null){
					connection.release();
				}
			}
			    return result;
		}
		
	}
	
	
	/**
	 * 获取上下文对象
	 * @return 
	 * @throws Exception
	 */
	public static MethodContext getMethodContext()throws Exception{
		  MethodContext context=null;
		  try {
			  context=MethodContext.getContext();
		   } catch (MethodServerException e) {
			 RemoteMethodServer.ServerFlag=true;
			 InetAddress intAddress=InetAddress.getLocalHost();
			 String ads=intAddress.getHostName();
			 if(ads==null){
				 ads=intAddress.getHostAddress();
			 }
			 SimpleAuthenticator author=new SimpleAuthenticator();
			 context=new MethodContext(ads,author);
			 context.setThread(Thread.currentThread());
            wt.pds.PDSIfc pdsifc=DataServicesRegistry.getDefault().getPdsFor("DEFAULT");
		  }
		     return context;
	}
	
	
	/**
	 * 更新数据库信息操作
	 * @author qwx80633
	 * @param sql 数据库字符串
	 * @param params 数据库参数
	 * @return Integer 更新的结果条数
	 */
	public  static Integer commonUpdate(String sql,String[] params)throws Exception{
	     int  result=0;
		 if(!RemoteMethodServer.ServerFlag){
			 Class[] cls={sql.getClass(),params.getClass()};
			 Object[] obj={sql,params};
			 RemoteMethodServer rms=RemoteMethodServer.getDefault();
			 return (Integer)rms.invoke("commonUpdate", UserDefQueryUtil.class.getName(), null, cls, obj);
		 }else{
			 WTConnection wtconnection=null;
			 PreparedStatement ment=null;
			 MethodContext context=getMethodContext();
			 Transaction trx=null;
			 try{
				 trx=new Transaction();
				 trx.start();
				 wtconnection=(WTConnection) context.getConnection();
				 ment=wtconnection.prepareStatement(sql);
				 if(ment!=null){
					 for(int j=1;j<=params.length;j++){
						 ment.setString(j, params[j-1]);
					 }
				 }
				 result=ment.executeUpdate();//更新记录条数
				 trx.commit();//提交事务

			 }catch(Exception e){
				 e.printStackTrace();
			 }finally{
				  if(trx!=null){
					  ment.close();
				  }
				  if(wtconnection!=null){
					   wtconnection.release();
				  }
			 }
		 }
		    return result;
	}
	
	
	/**
	 * 向数据库中插入数据信息
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static void commonInsert(String sql,String[] params)throws Exception{
		if(!RemoteMethodServer.ServerFlag){
			 Class[] cls={sql.getClass(),params.getClass()};
			 Object[] obj={sql,params};
			 RemoteMethodServer rms=RemoteMethodServer.getDefault();
			 rms.invoke("commonInsert", UserDefQueryUtil.class.getName(), null, cls, obj);
		}else{
			 WTConnection wtconnection=null;
			 PreparedStatement ment=null;
			 MethodContext context=getMethodContext();
			 Transaction trx=null;
			 try {
				 trx=new Transaction();
				 trx.start();
				 wtconnection=(WTConnection) context.getConnection();
				 ment=wtconnection.prepareStatement(sql);
				 if(ment!=null){
					 for(int j=1;j<=params.length;j++){
						 ment.setString(j, params[j-1]);
					 }
				 }
				 ment.execute();
				 trx.commit();//提交事务
			} catch (Exception e) {
			    e.printStackTrace();
			}finally{
				if(trx!=null){
					  ment.close();
				  }
				  if(wtconnection!=null){
					   wtconnection.release();
				  }
			}
		}
		
	}
	
	

}
