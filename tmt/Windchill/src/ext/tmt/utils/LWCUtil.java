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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
}

