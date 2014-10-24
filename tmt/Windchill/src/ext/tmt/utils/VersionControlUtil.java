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

import java.util.Date;

import wt.fc.PersistenceHelper;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;
/**
 * 版本检入检出
 *@author qiaokaikai
 * *@version 1.0  20141018

 */
public class VersionControlUtil {
    /**
     * revise a Versioned object
     * 
     * @param currVer
     * @return
     * @throws WTException
     * @throws WTPropertyVetoException
     */
    public static Versioned revise(Versioned currVer) throws WTException,
            WTPropertyVetoException {
        Versioned newVer = null;
        try {
            Versioned newVersionObj = VersionControlHelper.service
                    .newVersion(currVer);
            FolderHelper.assignLocation((FolderEntry) newVersionObj,
                    FolderHelper.service.getFolder((FolderEntry) currVer));
            newVer = (Versioned) PersistenceHelper.manager.store(newVersionObj);
        } catch (WTException e) {
            e.printStackTrace();
        }
        return newVer;
    }

    /**
     * check in a object with comments
     * 
     * @param w
     * @param note
     * @return
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static Workable checkin(Workable w, String userName,String note)
            throws WTPropertyVetoException, WTException {
//        WTUser user = (WTUser) wt.session.SessionHelper.manager.getPrincipal();

			note = note + "," + (new Date()).toString() + "," + userName;
           w = (Workable) PersistenceHelper.manager.refresh(w);
         if (WorkInProgressHelper.isCheckedOut(w))
             w = WorkInProgressHelper.service.checkin(w, note);
            return w;
    }

    /**
     * check in a object
     * 
     * @param w
     * @return
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static Workable checkin(Workable w,String userName) throws WTPropertyVetoException,
            WTException {
        return checkin(w, userName,"AutoCheckIn");
    }

    /**
     * check out a object
     * 
     * @param w
     * @return
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static Workable checkout(Workable w) throws WTPropertyVetoException,
            WTException {
        return checkout(w,"AutoCheckOut");
    }

    /**
     * check out a object with comments
     * 
     * @param w
     * @param note
     * @return
     * @throws WTPropertyVetoException
     * @throws WTException
     */
    public static Workable checkout(Workable w,String note)
            throws WTPropertyVetoException, WTException {
    	       Workable wk = null;
    	      if (w instanceof Iterated) {
    	            Iterated it = (Iterated) w;
    	            w = (Workable) VersionControlHelper.service.getLatestIteration(it,
    	                    false);
    	        }
    	        boolean checkoutFlag = WorkInProgressHelper.isCheckedOut(w);
    	        if (checkoutFlag) {
    	            if (!WorkInProgressHelper.isWorkingCopy(w))
    	                wk = WorkInProgressHelper.service.workingCopyOf(w);
    	            else
    	                wk = w;
    	        } else {
    	            Folder myFolder = WorkInProgressHelper.service.getCheckoutFolder();
    	            CheckoutLink checkoutLink = WorkInProgressHelper.service.checkout(
    	                    w, myFolder, note,true);
    	            wk = checkoutLink.getWorkingCopy();
    	        }
	
             return wk;
    }
    
    

    /**
     * 
     * @param it
     * @return
     * @throws VersionControlException
     * @throws WTException
     */
    public static Iterated getLatestIteration(Iterated it)
            throws VersionControlException, WTException {
        if (it.getIterationInfo().isLatest())
            return it;
        return VersionControlHelper.service.getLatestIteration(it, false);
    }
    
    /**
     * 
     * @param verObj
     * @return
     * @throws VersionControlException
     */
    public static String getMajorVersion(Versioned verObj)
            throws VersionControlException {
        return verObj.getVersionIdentifier().getSeries().toString();
    }
}
