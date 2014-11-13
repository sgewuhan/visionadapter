package ext.tmt.EPMdoc.createPart;


import wt.part.WTPart;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.windchill.uwgm.common.associate.AssociatePartDescriptor;
import com.ptc.windchill.uwgm.common.autoassociate.DefaultAutoAssociatePartFinderCreator;

import ext.tmt.utils.Debug;

public class TMTAutoassociateCreatePart extends DefaultAutoAssociatePartFinderCreator{
	
	void TMTAutoassociateCreatePart(){
		
	}
	@Override
	public WTPart createNewWTPart(
			AssociatePartDescriptor associatepartdescriptor)
			throws WTException, WTPropertyVetoException {
		// TODO Auto-generated method stub
		String number = associatepartdescriptor.getPartNumber();
		Debug.P("part number -->"+number);
		if(number!=null&&"本图".equals(number)){
			return null;
		}else{
			return super.createNewWTPart(associatepartdescriptor);
		}
	}
	

}
