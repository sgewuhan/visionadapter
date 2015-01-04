/* bcwti
 *
 * Copyright (c) 2011 Parametric Technology Corporation (PTC). All Rights
 * Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package ext.tmt.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.session.SessionHelper;
import wt.type.ClientTypedUtility;
import wt.util.WTException;

import com.ptc.core.htmlcomp.util.TypeHelper;
import com.ptc.core.logging.Log;
import com.ptc.core.logging.LogFactory;
import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.meta.common.IllegalFormatException;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.TypeInstanceIdentifier;
import com.ptc.core.meta.common.UpdateOperationIdentifier;
import com.ptc.core.meta.type.common.TypeInstance;
import com.ptc.core.meta.type.common.TypeInstanceFactory;

/**
 * replace IBAUtil
 * 
 * @version 1.0 $Created on 2011-8-25 $
 * @author <a href="mailto:age@ptc.com">Ge Aiping</a>
 * 
 */
public class LWCUtil {
    private static Log log = LogFactory.getLog(LWCUtil.class);
    
    /**
     * 
     * @param p
     * @return
     */
    public static Map<String, Object> getAllAttribute(Persistable p,Locale loc) {
        log.info("$$$$$$$$ getAllAttribute Begin.......");
        TypeInstance typeInstance;
        Map<String, Object> dataMap = new HashMap<String, Object>();
        try {
            LWCNormalizedObject lwcObject = new LWCNormalizedObject(p, null, loc, null);
            TypeInstanceIdentifier typeinstanceidentifier = ClientTypedUtility.getTypeInstanceIdentifier(p);
            typeInstance = TypeInstanceFactory.newTypeInstance(typeinstanceidentifier);
            
            TypeIdentifier typeidentifier = (TypeIdentifier)typeInstance.getIdentifier().getDefinitionIdentifier();
            Set attrs=TypeHelper.getSoftAttributes(typeidentifier);
            Iterator attIt=attrs.iterator();
            String attrFullName="";
            String attrName="";
            int idx=0;
            while(attIt.hasNext()){
                attrFullName=attIt.next().toString();
                idx=attrFullName.lastIndexOf("|");
                attrName=attrFullName.substring(idx+1);
                lwcObject.load(attrName);
                dataMap.put(attrName, lwcObject.get(attrName));
            }

        } catch (IllegalFormatException e) {

            e.printStackTrace();
        } catch (WTException e) {

            e.printStackTrace();
        }
        log.info("$$$$$$$$ getAllAttribute End.......>");
        return dataMap;
    }
    
    /**
     * 
     * @param p
     * @return
     */
    public static Map<String, Object> getAllAttribute(Persistable p)  {
        Locale loc=null;
        try {
            loc = SessionHelper.getLocale();
        } catch (WTException e) {
            e.printStackTrace();
            return null;
            
        }
        return getAllAttribute(p,loc);
    }
    /**
     * 
     * @param p
     * @param key
     * @return
     * @throws WTException
     */
    public static Object getValue(Persistable p, String key) throws WTException {
        Locale loc=null;
        try {
            loc = SessionHelper.getLocale();
        } catch (WTException e) {
            e.printStackTrace();
            return null;
            
        }
        return getValue(p,loc,key);
    }
    
    /**
     * 
     * @param p
     * @param key
     * @return
     * @throws WTException
     */
    public static Object getValue(Persistable p,Locale loc, String key) throws WTException {
        LWCNormalizedObject lwcObject = new LWCNormalizedObject(p, null, loc, null);
        lwcObject.load(key);
        return lwcObject.get(key);

    }
    /***
     * 
     * @param p
     * @param keys
     * @return
     * @throws WTException
     */
    public static Map<String, Object> getMutilValue(Persistable p, String[] keys) throws WTException{
        
        Locale loc=null;
        try {
            loc = SessionHelper.getLocale();
        } catch (WTException e) {
            e.printStackTrace();
            return null;
            
        }
        return getMutilValue(p,loc,keys);
    }
    
    /**
     * 
     * @param p
     * @param keys
     * @return
     * @throws WTException
     */
    public static Map<String, Object> getMutilValue(Persistable p,Locale loc, String[] keys) throws WTException {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        LWCNormalizedObject lwcObject = new LWCNormalizedObject(p, null, loc, null);
        for (int i = 0; i < keys.length; i++) {
            lwcObject.load(keys[i]);
            dataMap.put(keys[i], lwcObject.get(keys[i]));
        }

        return dataMap;

    }

    /**
     * 
     * @param p
     * @param dataMap
     * @return
     * @throws WTException
     */
    public static void setValueBeforeStore(Persistable p,  Map<String, Object> dataMap) throws WTException{
        
        Locale loc=null;
        try {
            loc = SessionHelper.getLocale();
        } catch (WTException e) {
            e.printStackTrace();
            return ;
            
        }
        setValueBeforeStore(p,loc,dataMap);
    }
    
    /**
     * 
     * @param p
     * @param loc
     * @param dataMap
     * @return
     * @throws WTException
     */
    public static void setValueBeforeStore(Persistable p, Locale loc, Map<String, Object> dataMap) throws WTException {
        LWCNormalizedObject lwcObject = new LWCNormalizedObject(p, null, loc, new UpdateOperationIdentifier());
        Iterator<String> keyIt = dataMap.keySet().iterator();
        String key = null;
        lwcObject.load(dataMap.keySet());
        while (keyIt.hasNext()) {
            key = keyIt.next();
            lwcObject.set(key, dataMap.get(key));
        }

        lwcObject.apply();
//        Persistable newP=PersistenceHelper.manager.modify(p);
//        return newP;
    }
    
    /**
     * 
     * @param p
     * @param dataMap
     * @return
     * @throws WTException
     */
    public static void setValue(Persistable p,  Map<String, Object> dataMap) throws WTException{
        
        Locale loc=null;
        try {
            loc = SessionHelper.getLocale();
        } catch (WTException e) {
            e.printStackTrace();
            return ;
            
        }
        setValue(p,loc,dataMap);
    }
    
    /**
     * 
     * @param p
     * @param loc
     * @param dataMap
     * @return
     * @throws WTException
     */
    public static Persistable setValue(Persistable p, Locale loc, Map<String, Object> dataMap) throws WTException {
        LWCNormalizedObject lwcObject = new LWCNormalizedObject(p, null, loc, new UpdateOperationIdentifier());
        Iterator<String> keyIt = dataMap.keySet().iterator();
        String key = null;
        lwcObject.load(dataMap.keySet());
        while (keyIt.hasNext()) {
            key = keyIt.next();
            lwcObject.set(key, dataMap.get(key));
        }

        lwcObject.apply();
        Persistable newP=PersistenceHelper.manager.modify(p);
        return newP;
    }
    /***
     * 
     * @param p
     * @param key
     * @param value
     * @return
     * @throws WTException
     */
    public static Persistable setValue(Persistable p,  String key, Object value) throws WTException {
        Locale loc=null;
        try {
            loc = SessionHelper.getLocale();
        } catch (WTException e) {
            e.printStackTrace();
            return null;
        }
        return setValue(p,loc,key,value);
        
    }
    
    /**
     * 
     * @param p
     * @param loc
     * @param key
     * @param value
     * @return
     * @throws WTException
     */
    public static Persistable setValue(Persistable p, Locale loc, String key, Object value) throws WTException {
        LWCNormalizedObject lwcObject = new LWCNormalizedObject(p, null, loc, new UpdateOperationIdentifier());
        lwcObject.load(key);
        lwcObject.set(key, value);
        lwcObject.apply();
        //PersistenceServerHelper.manager.update(p);
        Persistable newP= PersistenceHelper.manager.modify(p);
        
        return newP;
    }
    
    

    /**
     * 根据Key,Value获得对象信息
     * @param key IBA键
     * @param value IBA值
     * @param type 对象类型
     * @return
     */
    public static List<Persistable> getObjectByIBA(Map<String,String> ibaValues,String type){
    	  Debug.P("---getObjectByIBA: ibaValues:" +ibaValues);
    	  List<Persistable>  result=null;
    	  String sql=null;
    	  if(ibaValues!=null&&ibaValues.size()>0){
    		      StringBuffer bf=new StringBuffer();
    		      List<String> paramList=new ArrayList<String>();
    		      bf.append("and  (");
    		      //拼接IBA查询条件
    		      for(Iterator<?> ite=ibaValues.keySet().iterator();ite.hasNext();){
    		    	 String key=(String) ite.next();
    		    	 bf.append("d1.name=?");
    		    	 paramList.add(key);
    		    	 String value=ibaValues.get(key);
    		    	 if(StringUtils.isEmpty(value)){
    		    		 bf.append("and  ").append("v1.value is Null");
    		    	 }else{
    		    		 bf.append("and  ").append("v1.value=?");
    		    		 paramList.add(value);
    		    	 }
    		      }
    		      bf.append(")");
    		     //IBA查询条件
    		      String queryIBACond=bf.toString();
    		      Debug.P("--->>Query IBA Param:"+queryIBACond);
    		    if("wt.part.WTPart".contains(type)){//部件类型
    			   sql="select M1.NAME,M1.WTPARTNUMBER as OBJECTNUMBER  FROM  STRINGVALUE v1 ,STRINGDEFINITION d1,WTPART p1,WTPARTMASTER m1 where D1.IDA2A2=v1.ida3a6 and v1.IDA3A4=p1.IDA2A2 and p1.IDA3MASTERREFERENCE=M1.IDA2A2 and d1.name=?  and v1.value=?";
    		    }else if("wt.epm.EPMDocument".contains(type)){
    			   sql="select M1.NAME,M1.DOCUMENTNUMBER as OBJECTNUMBER  FROM  STRINGVALUE v1 ,STRINGDEFINITION d1,EPMDOCUMENT e1,EPMDOCUMENTMASTER m1 where D1.IDA2A2=v1.ida3a6 and v1.IDA3A4=e1.IDA2A2 and e1.IDA3MASTERREFERENCE=M1.IDA2A2 and d1.name=?  and v1.value=? ";
    		   }else if("wt.doc.WTDocument".contains(type)){
    			   sql="select M1.NAME,M1.WTDOCUMENTNUMBER as OBJECTNUMBER FROM  STRINGVALUE v1 ,STRINGDEFINITION d1,WTDOCUMENT t1,WTDOCUMENTMASTER m1 where D1.IDA2A2=v1.ida3a6 and v1.IDA3A4=t1.IDA2A2 and t1.IDA3MASTERREFERENCE=M1.IDA2A2 and d1.name=?  and v1.value=?";
    		   }
    		    String[] params=new String[paramList.size()];
    		    params=paramList.toArray(params); 
    		    Debug.P("---->>>SQL:"+sql);
    		    Debug.P("------>>>>SQL param:"+paramList);
    		    try {
					List<Hashtable<String,String>> datas   =UserDefQueryUtil.commonQuery(sql, params);
				if(datas!=null&&datas.size()>0){
				 	Debug.P("---->>getObjectIBA  Size:"+datas.size());
					result=new ArrayList<Persistable>();	
					for(int i=0;i<datas.size();i++){
						    Hashtable<String, String> data_rows=datas.get(i);
							for(Iterator<?> ite=data_rows.keySet().iterator();ite.hasNext();){
								 String keyStr=(String) ite.next();
								 if(keyStr.equalsIgnoreCase("OBJECTNUMBER")){
									 String valueStr=data_rows.get("OBJECTNUMBER");
									 Persistable object=GenericUtil.getObjectByNumber(valueStr);
									 result.add(object);
								 }
							}
						}
				    }
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}    		    
    	  }
    	       return result;
    }
}

