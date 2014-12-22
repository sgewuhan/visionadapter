package ext.tmt.utils;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.build.EPMBuildHistory;
import wt.epm.build.EPMBuildRule;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.iba.definition.StringDefinition;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;

public class EPMDocUtil {
	
	
	  //根据编号得到EPMmaster
    public 	static EPMDocumentMaster getEPMMasterByNumber(String number) throws WTException {
        EPMDocumentMaster epmmaster =null;
        QuerySpec qs=null;
        QueryResult qr=null;
        qs= new QuerySpec(EPMDocumentMaster.class);
        SearchCondition sc = new SearchCondition(wt.epm.EPMDocumentMaster.class,wt.epm.EPMDocumentMaster.NUMBER, SearchCondition.LIKE, number);
        qs.appendSearchCondition(sc);
        qr= PersistenceHelper.manager.find(qs);
        
        while(qr.hasMoreElements()) {
            epmmaster=(EPMDocumentMaster)qr.nextElement();
            break;
        }
        
        return epmmaster;
    }
    
    /**
     * 根据编号得到EPM
     * @author blueswang
     * @param number
     * @return
     * @throws WTException
     * @return EPMDocument
     * @Description
     */
    public static EPMDocument getEPMDoc(String number)throws WTException {
    	EPMDocument EPMDoc= null;
    	 QuerySpec qs=null;
         QueryResult qr=null;
         qs= new QuerySpec(EPMDocument.class);
         SearchCondition sc = new SearchCondition(EPMDocument.class,"master>number", SearchCondition.EQUAL, number);
         qs.appendSearchCondition(sc);
         qr= PersistenceHelper.manager.find(qs);
         
         while(qr.hasMoreElements()) {
        	 EPMDoc=(EPMDocument)qr.nextElement();
             break;
         }
         return EPMDoc;
    }
    
    /**
     * 根据名称得到EPM
     * @author blueswang
     * @param number
     * @return
     * @throws WTException
     * @return EPMDocument
     * @Description
     */
    public static EPMDocument getEPMDocByName(String name)throws WTException {
    	EPMDocument EPMDoc= null;
    	 QuerySpec qs=null;
         QueryResult qr=null;
         qs= new QuerySpec(EPMDocument.class);
         SearchCondition sc = new SearchCondition(EPMDocument.class,"master>name", SearchCondition.LIKE, name);
         SearchCondition latestIteration = new SearchCondition(EPMDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE);
         qs.appendWhere(sc);
         qs.appendAnd();
         qs.appendWhere(latestIteration);
         qr= PersistenceHelper.manager.find(qs);
         while(qr.hasMoreElements()) {
        	 EPMDoc=(EPMDocument)qr.nextElement();
             break;
         }
         return EPMDoc;
    }
    
    /**
     * 根据编码得到EPM
     * @author blueswang
     * @param number
     * @return
     * @throws WTException
     * @return EPMDocument
     * @Description
     */
    public static EPMDocument getEPMDocByNumber(String number)throws WTException {
    	EPMDocument EPMDoc= null;
    	 QuerySpec qs=null;
         QueryResult qr=null;
         qs= new QuerySpec(EPMDocument.class);
         SearchCondition sc = new SearchCondition(EPMDocument.class,"master>number", SearchCondition.LIKE, number);
         qs.appendSearchCondition(sc);
         qr= PersistenceHelper.manager.find(qs);
         
         while(qr.hasMoreElements()) {
        	 EPMDoc=(EPMDocument)qr.nextElement();
             break;
         }
         return EPMDoc;
    }
    
    /**
     * 根据软属性的名称查询出对应的EPMDocument对象
     * 
     * @param partNum
     * @return
     * @throws Exception 
     */
    public static List<Persistable> getEPMDocumentByIBA(String IbaKey,String IbaValue) throws Exception{
    	List<Persistable> objects=new ArrayList<Persistable>();
    	Debug.P("----->>>>>IbaKey:"+IbaKey+"  IbaValue:"+IbaValue);
    	if(StringUtils.isNotEmpty(IbaValue)&&StringUtils.isNotEmpty(IbaKey)){
    		String sql="select M1.NAME,M1.DOCUMENTNUMBER  FROM  STRINGVALUE v1 ,STRINGDEFINITION d1,EPMDOCUMENT e1,EPMDOCUMENTMASTER m1 where D1.IDA2A2=v1.ida3a6 and v1.IDA3A4=e1.IDA2A2 and e1.IDA3MASTERREFERENCE=M1.IDA2A2 and d1.name=?  and v1.value=? ";
    		String [] params={IbaKey,IbaValue.trim()};
    		Debug.P(">>>>>sql:"+sql);
    		List<Hashtable<String,String>> result   =UserDefQueryUtil.commonQuery(sql, params);
    	    if(result!=null&&result.size()>0){
    	    	Debug.P("------>>>Get EpmDocument Result Size:"+result.size());
    	    	for(int i=0;i<result.size();i++){
    	    		 Hashtable<String, String> data_rows=result.get(i);
    	    		 for(Iterator<?> ite=data_rows.keySet().iterator();ite.hasNext();){
    	    			 String key=(String) ite.next();
    	    			 if("DOCUMENTNUMBER".equals(key)){
    	    				 String epmdoc_Num=data_rows.get(key);
    	    				 Persistable object=GenericUtil.getObjectByNumber(epmdoc_Num);
    	    				 objects.add(object);
    	    			 }
    	    		 }
    	    	}
    	    }
    	}
    	        return objects;
    }
    
    
    
    
    
    
    
    /**
     * 得到part关联的图档
     * @author blueswang
     * @param part
     * @return
     * @throws WTException
     * @return EPMDocument
     * @Description
     */
    public static EPMDocument getActiveEPMDocument( WTPart part )
    throws WTException {
        EPMDocument cadDoc=null;
        QueryResult qr = PersistenceHelper.manager.navigate(part, "buildSource", EPMBuildRule.class, true);
        while ( qr.hasMoreElements() ) {
            Object wto = (Object)qr.nextElement();
            if(wto instanceof EPMDocument) {
                cadDoc = ( EPMDocument ) wto;
                break;
            }
        }
        if(cadDoc==null) {
            qr = PersistenceHelper.manager.navigate( part, EPMBuildHistory.BUILT_BY_ROLE, EPMBuildHistory.class, true );
            while ( qr.hasMoreElements() ) {
                Object wto = (Object)qr.nextElement();
                if(wto instanceof EPMDocument) {
                    cadDoc = ( EPMDocument ) wto;
                    break;
                }
            }
        }
        return cadDoc;
    }
    
    /**
     * 得到part的说明文档
     * @author blueswang
     * @param part
     * @return
     * @throws WTException
     * @return Vector
     * @Description
     */
    public static Vector getNoActiveEPMDocuments( WTPart part )
    throws WTException {
        Vector cadDocV=new Vector();
        EPMDocument cadDoc=null;
        
        QueryResult qr =WTPartHelper.service.getDescribedByDocuments(part);
        while ( qr.hasMoreElements() ) {
            Object wto = (Object)qr.nextElement();
            if(wto instanceof EPMDocument) {
                cadDoc = ( EPMDocument ) wto;
                cadDocV.addElement(cadDoc);
            }
        }
        
        return cadDocV;
    }
    
    public static boolean isLinkedEPM(EPMDocumentMaster epmmaster,ArrayList<EPMDocument> EPMList){
    	boolean result = false;
    	for(int i=0;i<EPMList.size();i++){
			 String empNum = EPMList.get(i).getNumber();
			 if(epmmaster.NUMBER.equals(empNum)){
				 result = true;
			 }
		 }
    	return result;
    	
    }
    
    
    /**
     * 获取部件关联的EPM文档
     * @author blueswang
     * @param part
     * @return
     * @throws WTException
     * @return ArrayList<EPMDocument>
     * @Description
     */
    public static ArrayList<EPMDocument> getEPMDocbyPart(WTPart part) throws WTException{
    	ArrayList<EPMDocument> result = new ArrayList<EPMDocument>();
    	
    	QueryResult epmdoc = WTPartHelper.service.getDescribedByDocuments(part);
    	while(epmdoc.hasMoreElements()){
    		Object obj = epmdoc.nextElement();
    		if(obj instanceof EPMDocument){
    			EPMDocument doc = (EPMDocument)obj;
    			result.add(doc);
    		}
    	}
    	return result;
    }
    
    
    /**
     * 上载主文档
     * @author Eilaiwang
     * @param doc
     * @param contName
     * @param PRIMARY_FILE
     * @throws Exception
     * @return void
     * @Description
     */
    public static void uploadEPMApplicationData(EPMDocument doc, String contName,String PRIMARY_FILE)
	 throws Exception {
			doc = (EPMDocument) PersistenceHelper.manager.refresh(doc);
			ContentItem ci = ContentHelper.service.getPrimary(doc);
			System.out.println("主文件是" + ci);
			if (ci != null) {
				PersistenceServerHelper.manager.remove(ci);
			}
			doc = (EPMDocument) PersistenceHelper.manager.refresh(doc);
			if (!PRIMARY_FILE.equals("")||PRIMARY_FILE != null) {
				System.out.println("PRIMARY_FILE--->" + PRIMARY_FILE);
				ApplicationData applicationdata = ApplicationData.newApplicationData(doc);
				applicationdata.setRole(ContentRoleType.PRIMARY);
				applicationdata.setFileName(contName);

				File file = new File(PRIMARY_FILE);
				FileInputStream primary = new FileInputStream(file);
				ContentServerHelper.service.updateContent(doc, applicationdata,primary);
			}
	   }
    

    

}
