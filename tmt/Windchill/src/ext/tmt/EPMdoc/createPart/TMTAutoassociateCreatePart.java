package ext.tmt.EPMdoc.createPart;


import wt.epm.EPMDocument;
import wt.epm.modelitems.ModelItem;
import wt.epm.workspaces.EPMWorkspace;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTKeyedMap;
import wt.part.WTPart;
import wt.pom.UniquenessException;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.VersionControlException;

import com.ptc.windchill.uwgm.common.associate.AssociatePartDescriptor;
import com.ptc.windchill.uwgm.common.autoassociate.DefaultAutoAssociatePartFinderCreator;

import ext.tmt.utils.Debug;

/**
 * 无用的程序
 * @author Administrator
 *
 */
@Deprecated
public class TMTAutoassociateCreatePart extends DefaultAutoAssociatePartFinderCreator{
	
	TMTAutoassociateCreatePart(){
		
	}
	@Override
	public void initSearchMatchingParts(WTCollection wtcollection,
			WTKeyedMap wtkeyedmap, EPMWorkspace epmworkspace)
			throws WTException {
		super.initSearchMatchingParts(wtcollection, wtkeyedmap, epmworkspace);
	}
	
	@Override
	public WTPart findOrCreateWTPart(EPMDocument epmdocument,
			ModelItem modelitem, EPMWorkspace epmworkspace) throws WTException,
			WTPropertyVetoException, VersionControlException,
			UniquenessException {
		return super.findOrCreateWTPart(epmdocument, modelitem, epmworkspace);
	}
	@Override
	public WTPart createNewWTPart(
			AssociatePartDescriptor associatepartdescriptor)
			throws WTException, WTPropertyVetoException {
		String number = associatepartdescriptor.getPartNumber();
		Debug.P("---->>TMT Auto part number -->"+number);
		if(number!=null&&"本图".equals(number)){
			return null;
		}else{
			return super.createNewWTPart(associatepartdescriptor);
		}
	}
	

}
