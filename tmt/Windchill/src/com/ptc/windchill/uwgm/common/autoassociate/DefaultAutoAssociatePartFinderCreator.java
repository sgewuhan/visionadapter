package com.ptc.windchill.uwgm.common.autoassociate;

import com.ptc.core.logging.Log;
import com.ptc.core.logging.LogFactory;
import com.ptc.windchill.cadx.common.util.WorkspaceUtilities;
import com.ptc.windchill.uwgm.common.associate.AssociatePartDescriptor;
import com.ptc.windchill.uwgm.common.util.PrintHelper;
import java.util.Map;
import wt.epm.EPMDocument;
import wt.epm.modelitems.ModelItem;
import wt.epm.workspaces.EPMWorkspace;
import wt.fc.Persistable;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.inf.container.WTContainer;
import wt.part.WTPart;
import wt.pom.UniquenessException;
import wt.util.*;
import wt.vc.VersionControlException;
import wt.vc.views.ViewReference;

// Referenced classes of package com.ptc.windchill.uwgm.common.autoassociate:
//            WTPartSearchHelper, AutoAssociatePartFinderCreator, AutoAssociateHelper, WTPartUtilities

public class DefaultAutoAssociatePartFinderCreator implements
		AutoAssociatePartFinderCreator {

	public DefaultAutoAssociatePartFinderCreator() {
	}

	/**
	 * @deprecated Method isIsNewPart is deprecated
	 */

	public boolean isIsNewPart() {
		return isNewPart;
	}

	/**
	 * @deprecated Method setIsNewPart is deprecated
	 */

	public void setIsNewPart(boolean flag) throws WTPropertyVetoException {
		isNewPart = flag;
	}

	public WTPart findOrCreateWTPart(EPMDocument epmdocument,
			EPMWorkspace epmworkspace) throws WTException,
			WTPropertyVetoException, VersionControlException,
			UniquenessException {
		String s = AutoAssociateHelper.getPartNumberToSearch(epmworkspace,
				epmdocument);
		return searchPart(s, epmdocument, epmworkspace, true);
	}

	public WTPart findOrCreateWTPart(EPMDocument epmdocument,
			ModelItem modelitem, EPMWorkspace epmworkspace) throws WTException,
			WTPropertyVetoException, VersionControlException,
			UniquenessException {
		String s = AutoAssociateHelper.getPartNumberToSearch(epmworkspace,
				epmdocument, modelitem);
		return searchPart(s, modelitem, epmworkspace, true);
	}

	public WTPart findWTPart(EPMDocument epmdocument) throws WTException {
		if (epmdocument == null)
			return null;
		if (docWorkspace == null)
			docWorkspace = WorkspaceUtilities
					.getAssociatedWorkspace(epmdocument);
		String s = AutoAssociateHelper.getPartNumberToSearch(docWorkspace,
				epmdocument);
		return searchPart(s, epmdocument, docWorkspace, false);
	}

	public WTPart findWTPart(EPMDocument epmdocument, ModelItem modelitem)
			throws WTException {
		if (modelitem == null)
			return null;
		if (docWorkspace == null)
			docWorkspace = WorkspaceUtilities
					.getAssociatedWorkspace(epmdocument);
		String s = AutoAssociateHelper.getPartNumberToSearch(docWorkspace,
				epmdocument, modelitem);
		return searchPart(s, modelitem, docWorkspace, false);
	}

	protected WTPart searchPart(String s, Persistable persistable,
			EPMWorkspace epmworkspace, boolean flag) throws WTException {
		if (searchHelper == null)
			throw new IllegalStateException("Missing initialization!");
		if (s == null || s.length() == 0)
			return null;
		if (log.isTraceEnabled())
			log.trace((new StringBuilder()).append("Searching part number: ")
					.append(s).append(" for : ")
					.append(PrintHelper.printPersistable(persistable))
					.toString());
		WTPart wtpart = searchHelper.getPartForDocorModelItem(persistable);
		if (log.isTraceEnabled())
			log.trace((new StringBuilder())
					.append(wtpart != null ? "" : "NOT ")
					.append("found in cache").toString());
		return wtpart;
	}

	public void initSearchSpecifiedParts(Map map, WTKeyedMap wtkeyedmap,
			EPMWorkspace epmworkspace) throws WTException {
		if (searchHelper != null) {
			throw new IllegalStateException("Repeated initialization!");
		} else {
			log.debug((new StringBuilder())
					.append("Initializing specified part search: ")
					.append(map.size()).toString());
			searchHelper = new WTPartSearchHelper(map, wtkeyedmap,
					epmworkspace, true);
			return;
		}
	}

	public void initSearchMatchingParts(WTCollection wtcollection,
			WTKeyedMap wtkeyedmap, EPMWorkspace epmworkspace)
			throws WTException {
		if (searchHelper != null) {
			throw new IllegalStateException("Repeated initialization!");
		} else {
			log.debug((new StringBuilder())
					.append("Initializing matching part search: ")
					.append(wtcollection.size()).toString());
			searchHelper = new WTPartSearchHelper(wtcollection, wtkeyedmap,
					epmworkspace, false);
			return;
		}
	}

	public Map getFilteredPartsMessageMap() {
		return searchHelper.getFilteredPartsMessageMap();
	}

	public WTPart createNewWTPart(
			AssociatePartDescriptor associatepartdescriptor)
			throws WTException, WTPropertyVetoException {
		if (searchHelper == null)
			throw new IllegalStateException("Missing initialization!");
		String s = associatepartdescriptor.getPartName();
		String s1 = associatepartdescriptor.getPartNumber();
		System.out.println("出现本图了-------------------------》"+s1);
		if(s1 !=null &&s1.equals("本图")){
			System.out.println("出现本图了-------------------------》"+s1);
			return null;
		}
		if (s1 != null && searchHelper.isExistingPart(s1)
				&& !searchHelper.isFilteredOutPart(s1)) {
			log.error((new StringBuilder())
					.append("Attempt to create an existing part: ").append(s1)
					.append(" - ").append(s).toString());
			WTMessage wtmessage = (WTMessage) searchHelper
					.getFilteredPartsMessageMap().get(s1);
			if (wtmessage == null)
				wtmessage = new WTMessage(
						"com.ptc.windchill.uwgm.common.autoassociate.autoassociateResource",
						"77", new Object[] { s1 });
			throw new WTException(wtmessage);
		} else if(s1.equals("本图")) {
			log.debug((new StringBuilder())
					.append("Create number is 本图  part!---------------"));
			return null;
		}else{
			return WTPartUtilities.createNewPart(associatepartdescriptor);
		}
	}

	private static final Log log = LogFactory.getLog(com.ptc.windchill.uwgm.common.autoassociate.DefaultAutoAssociatePartFinderCreator.class);
	private static final String RESOURCE = "com.ptc.windchill.uwgm.common.autoassociate.autoassociateResource";
	private boolean isNewPart;
	private EPMWorkspace docWorkspace;
	protected WTContainer wsContainer;
	private ViewReference viewReference;
	private WTPartSearchHelper searchHelper;

}
