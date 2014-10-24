package ext.sdxc.webservics;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import ext.tmt.utils.Base64;
import ext.tmt.utils.Debug;
import ext.tmt.utils.FolderUtil;
import ext.tmt.utils.PartUtil;


import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.SubFolder;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.inf.library.WTLibrary;
import wt.org.OrganizationServicesHelper;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;


public class ProssServices {
	
	public static final String USER_AUTHORIZATION_FAIL = "用户登录失败!";
	
	/**  
	 * 授权
	 * @author blueswang  2012-12-7
	 *   
	 * @Description    
	 */
	static class MyAuthenticator extends Authenticator {        
		  private String user = "";        
		  private String password = "";        
		  public MyAuthenticator(String user, String password) {            
		   this.user = user;            
		   this.password = password;           
		  }        
		  protected PasswordAuthentication getPasswordAuthentication() {           
		   return new PasswordAuthentication(user, password.toCharArray());           
		  }       
	}
	
	
	  /**
	   * 根据用户名获取用户当前的任务
	   * @author blueswang
	   * @param uName
	   * @return
	   * @return String
	   * @Description
	   */
	  public static String getWorkItems(String uName){
	    	String result="failed";
	    	WTUser user;
	    	
	    	ArrayList<WorkItem> itemList = new ArrayList<WorkItem>();
	    	
			try {
				user = wt.org.OrganizationServicesHelper.manager.getUser(uName);
	    	System.out.println(user.getFullName());
	    	QueryResult qr = WorkflowHelper.service.getUncompletedWorkItems(user);
	    	System.out.println(qr);
	    	while(qr.hasMoreElements()){
	    		 Object obj = (Object)qr.nextElement();
	    		WorkItem item =(WorkItem)obj;
	    		itemList.add(item);
	    		System.out.println(item.getIdentity());
	    		System.out.println(item.getType());
	    		result ="success";
	    	}
			} catch (WTException e) {
				e.printStackTrace();
			}
			getInfoByWrokItem(itemList);
	    	return result;
	    }
	  
	  public static String getInfoByWrokItem(ArrayList<WorkItem> items){
		  String result ="";
		  WfActivity wfAct = null;
		  for(WorkItem item :items){
			  wfAct=(WfActivity)item.getSource().getObject();
			  System.out.println("wfAct.getName() :"+wfAct.getName());
			  System.out.println("wfAct.getIdentity() :"+wfAct.getIdentity());
			  System.out.println("wfAct.getType() :"+wfAct.getType());
			  System.out.println(wfAct.getContainerName());
			  try {
				WfProcess proc = wfAct.getParentProcess();
				System.out.println("proc.getName():"+proc.getName());
				System.out.println("proc.getIdentity():"+proc.getIdentity());
			} catch (WTException e) {
				e.printStackTrace();
			}
			  
		  }
		  
		  return result;
	  }
	  
	  /**
		 * 设置Authorization信息
		 * @author blueswang
		 * @param httpConn
		 * @Description
		 */
		public static void authorization(HttpURLConnection httpConn,String userName,String password) {
			if (httpConn == null)
				return;
			// String userPass= "wcadmin:wcadmin";
			byte[] userPass = (userName + ":" + password).getBytes();
			String encoding = Base64.encodeBytes(userPass);
			httpConn.setRequestProperty("Authorization", "Basic " + encoding);
		}
	  
	  /**
	   * 验证登录信息
	   * @author blueswang
	   * @param userName
	   * @param passWord
	   * @return
	   * @return String
	   * @Description
	   */
	  public static String doLoggin(String userName,String passWord) {
		  String userId = "";
		  HttpURLConnection conn = null;
		  String serviceUrl="http://csr-tmt.com/Windchill/servlet/SDXCWebService?wsdl";
		  try {
			URL url = new URL(serviceUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(5 * 1000);// 设置超时的时间
			conn.setUseCaches(true);
			authorization(conn,userName,passWord);
			conn.setRequestProperty("Content-Type", "application/octet-stream");
		    System.out.println("url---->"+url);
		    System.out.println("userName-->"+userName+"   passWord: "+passWord );
		    try {
		    	System.out.println("conn.getResponseCode()--->"+conn.getResponseCode());
		    	if (conn.getResponseCode()==200){
		    		userId=getUserId(userName);
		    		System.out.println("userId---->"+userId);
		    	}else{
		    			throw new Exception(USER_AUTHORIZATION_FAIL);
		    	}
		    } catch (IOException e1) {
		    } catch (Exception e1) {
		    	e1.printStackTrace();
		    }
		  } catch (Exception e) {
		  } finally{
			  conn.disconnect();
		  }
		  return userId;
	  }
	  
	  public static String getUserId(String userName){
		  WTUser user;
		  String userId="";
		  try {
			user = OrganizationServicesHelper.manager.getUser(userName);
			if(user != null){
				userId=user.getFullName();
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return userId;
	  }
	  
	
	  /**
	   * 创建文件夹
	   * @author Eilaiwang
	   * @param folderName文件夹名称
	   * @param containerName容器名称
	   * @return
	   * @return String
	   * @Description
	   */
		public static String CreateFolders(String folderName, String containerName) {
				SubFolder subFolder = null;
				WTContainer container  =null;
				String path = FolderUtil.setFilePath(folderName);//增加默认域Default
				Debug.P(path);
				String nextfolder[]=path.split("/");
				ArrayList list = new ArrayList();
				for(int p=0;p<nextfolder.length;p++){
					if(nextfolder[p]!=null&&!nextfolder[p].trim().equals("") && 
							!nextfolder[p].trim().equals("Default")){
						list.add(nextfolder[p]);
					}
				}
				Debug.P(list.size());
				try {
					container=getContainer(containerName);
					Debug.P(container);
					subFolder=FolderUtil.createMultiLevelDirectory(list, WTContainerRef.newWTContainerRef(container));
				} catch (Exception e) {
					e.printStackTrace();
				}
				return subFolder.getFolderPath();
			}
	  
	  
		/**
		 * 根据容器名称查找容器
		 * @author Eilaiwang
		 * @param ContName
		 * @return
		 * @throws WTException
		 * @return WTContainer
		 * @Description
		 */
	  public static WTContainer getContainer(String ContName) throws WTException {
			WTContainer wtContainer = null;
			QuerySpec qs = new QuerySpec(WTContainer.class);
			SearchCondition sc = new SearchCondition(WTContainer.class,
					WTContainer.NAME, SearchCondition.EQUAL, ContName, true);
			qs.appendSearchCondition(sc);
			QueryResult result = PersistenceHelper.manager.find(qs);
			if (result.size() > 0) {
				wtContainer = (WTContainer) result.nextElement();
			}
			return wtContainer;
		}
	  
	
	  /**
	   * 根据部件名称获取部件
	   * @author Eilaiwang
	   * @param partNum
	   * @return
	   * @return WTPart
	   * @Description
	   */
	  public static WTPart getPartByNumber(String partNum){
		  WTPart part= null;
		  try {
			part= PartUtil.getPartByNumber(partNum);
		} catch (WTException e) {
			e.printStackTrace();
		}
		return part;
	  }
	  
		/**
		 * 获取系统所有存储库
		 * 
		 * @return
		 * @throws WTException
		 */
		public static List getAllLibrary() {
			List libaries = new ArrayList();
			WTLibrary library = null;
			QuerySpec qs = null;
			QueryResult qr = null;
			try {
				qs = new QuerySpec(WTLibrary.class);
				qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				library = (WTLibrary) qr.nextElement();
				libaries.add(library);
			}
			} catch (WTException e) {
				e.printStackTrace();
			}
			return libaries;
		}
		
		/*
		 * 获取系统里所有的产品
		 */
		public static String getAllContainer(){
			StringBuffer sb = new StringBuffer();
			List productes = new ArrayList();
			WTContainer contain=null;
			QuerySpec qs = null;
			QueryResult qr=null;
			try {
				qs=new QuerySpec(WTContainer.class);
				qr=PersistenceHelper.manager.find(qs);
				  while(qr.hasMoreElements()){
					  contain = (WTContainer)qr.nextElement();
//					  productes.add(contain);
					  sb.append(contain.getName()+",");
				  }
			} catch (Exception e) {
				e.printStackTrace();
			}
			Debug.P(sb);
			return sb.toString();
		}
}
