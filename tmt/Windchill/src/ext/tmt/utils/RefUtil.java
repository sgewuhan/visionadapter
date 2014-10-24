package ext.tmt.utils;

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

import wt.fc.ReferenceFactory;
import wt.fc.Persistable;
import wt.method.RemoteAccess;
import wt.util.WTException;
/**
 * 
 *获取对象ID,根据对象ID获取对象
 *@version 1.0 
 *
 *
 */
public class RefUtil  implements RemoteAccess{

    private static ReferenceFactory rf = new ReferenceFactory();
    
    /**
     * get Object By Oid,oid must start with VR: or OR: <br/>
     * eg:VR:wt.doc.WTDocument:28057
     * @param oid
     * @return
     * @throws WTException
     */
    public static Persistable getObjectByOid(String oid) throws WTException {
        return rf.getReference(oid).getObject();
    }
    

    /**
     * get Object Oid such as VR:wt.doc.WTDocument:28057
     * @param persistObj
     * @return
     * @throws WTException
     */
    public static String getObjectOid(Persistable persistObj) throws WTException {
        return rf.getReferenceString(persistObj);
    }
}