package ext.tmt.part;

import ext.tmt.utils.Debug;
import ext.tmt.utils.IBAUtils;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.inf.container.WTContainer;
import wt.inf.library.WTLibrary;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.VersionControlHelper;

public class PartUtils {
	
	public static void main(String[] args) throws WTPropertyVetoException {
		try {
			WTPart part = getPartByNumber("0000000001");
			
			Debug.P(part.getName());
			Debug.P(part.getNumber());
			Debug.P(part.getContainer().getName());
			
			WTDocument doc =null;
			doc=getDocByPart(part);
			Debug.P(doc.getName());
			
			Debug.P(doc.getNumber());
			
			Debug.P(doc.getContainerName());
			System.out.println();
			
			WTContainer container=getContainers("测试产品");
			Debug.P(container.getName());
//			IBAUtils ibautil = new IBAUtils(part);
//			String size=ibautil.getIBAValue("plm.com.size");
//			ibautil.setIBAValue("plm.com.size", "99999");
//			ibautil.updateIBAPart(part);
			
			
			
		} catch (WTException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	
	
	
	public static WTPart getPartByNumber(String partNumber) throws WTException{
		WTPart Part = null;
		QuerySpec qs = new QuerySpec(WTPart.class);//构建器
		SearchCondition sc =new SearchCondition(WTPart.class, WTPart.NUMBER,SearchCondition.EQUAL, partNumber.trim().toUpperCase(), false);
		qs.appendWhere(sc);
		Debug.P("qs------>"+qs);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.size() > 0) {
			Part = (WTPart) qr.nextElement();
    			Part = (WTPart) VersionControlHelper.getLatestIteration(Part);
		}
		return Part;
	}
	
	public static WTDocument getDocByPart(WTPart part) throws WTException{
		WTDocument doc = null;
		QueryResult docs = WTPartHelper.service.getDescribedByWTDocuments(part);
		while(docs.hasMoreElements()){
			doc = (WTDocument)docs.nextElement();
		}
		return doc;
	}
	
	
	/**
	 * 根据OID获取对象
	 * @author Eilaiwang
	 * @param oid
	 * @return
	 * @throws WTException
	 * @return WTObject
	 * @Description
	 */
	public static WTObject getObjByOid(String oid) throws WTException{
		ReferenceFactory referencefactory = new ReferenceFactory();
	    WTObject obj = (WTObject) referencefactory.getReference(oid).getObject();
		return obj;
	}
	
	
	/**根据oid获得该对象
	 * @param oid
	 * @return
	 * @throws WTRuntimeException
	 * @throws WTException
	 */
	public static Persistable getObjectByOid(String oid)
			throws WTRuntimeException, WTException {
		ReferenceFactory rf = new ReferenceFactory();
		Persistable p = rf.getReference(oid).getObject();
		return p;
	}
	
	/**
	 * 获取对象的OID
	 * @author Eilaiwang
	 * @param obj
	 * @return
	 * @return String
	 * @Description
	 */
	public static String getOid(WTObject obj){
	    	String oid ="";
	    	wt.fc.ReferenceFactory rf = new ReferenceFactory();
	    	try {
				oid = rf.getReferenceString(obj);
			} catch (WTException e) {
				e.printStackTrace();
			}
	    	return oid;
	    }
	
      /* 万能查询
	 * @author blueswang
	 * @param number  对象编码
	 * @param masterClass   对象类
	 * @return
	 * @throws WTException
	 * @return Object
	 * @Description
	 */
	public static Object getObj(String number,Class masterClass) throws WTException{
		Object obj =null ;
		QuerySpec qs = new QuerySpec(masterClass);
		SearchCondition sc = null;
		try{
			sc = new SearchCondition(masterClass,
				"master>number", SearchCondition.EQUAL, number);
		}catch(wt.query.QueryException e){
			sc = new SearchCondition(masterClass,"number", SearchCondition.EQUAL, number);
		}
		qs.appendWhere(sc, new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find( qs);
		while(qr.hasMoreElements()){
			obj=qr.nextElement();	
		}
		return  (Object) obj;
	}
	/**
     * 
     * @author Eilaiwang
     * @param name
     * @return
     * @return WTContainer
     * @Description
     */
    public static WTContainer getContainers(String name){
   	  WTContainer container = null;
   	 try {
			QuerySpec qs = new QuerySpec(WTContainer.class);
			SearchCondition sc = new SearchCondition(WTContainer.class,WTContainer.NAME,"=",name);
			qs.appendWhere(sc);
			QueryResult qr = PersistenceHelper.manager.find(qs);
           while(qr.hasMoreElements()){
           	 container = (WTContainer)qr.nextElement();
           }
		} catch (Exception e) {
			e.printStackTrace();
		}
   	 return container;
    }

	/**
	 * 根据存储库文名称获得该上下文容器
	 * @author blueswang
	 * @param containerName
	 * @return
	 * @throws Exception
	 * @return WTContainer
	 * @Description
	 */
	public static WTContainer getContainer(String containerName) throws Exception {
		if ( containerName == null)
			return null;
		WTContainer ret = null;
		try {
			QuerySpec qs = new QuerySpec(WTLibrary.class);
			qs.appendWhere(new SearchCondition(WTLibrary.class, WTContainer.NAME,
					SearchCondition.EQUAL, containerName), new int[] { 0 });
			QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			Debug.P(qs);
			if (qr.hasMoreElements())
				ret = (WTContainer) qr.nextElement();
		}catch(Exception e){
			e.printStackTrace();
		} 
		return ret;
	}
	
	

}
