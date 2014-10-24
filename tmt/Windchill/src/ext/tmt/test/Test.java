package ext.tmt.test;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.VersionControlHelper;

public class Test {
	
	/**
	 * 根据部件编号查找部件
	 * @author blueswang
	 * @param number
	 * @return  select * from WTPart t where t.number ='' 
	 * @throws WTException
	 * @return WTPart
	 * @Description
	 */
	public static WTPart getPartByNumber(String number) throws WTException {
		WTPart Part = null;
		QuerySpec qs = new QuerySpec(WTPart.class);
		//SearchCondition sc = new SearchCondition(WTPart.class,WTPart.NUMBER,"=",number);
		qs.appendWhere(new SearchCondition(WTPart.class, WTPart.NUMBER,SearchCondition.EQUAL, number.trim().toUpperCase(), false));
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.size() > 0) {
			Part = (WTPart) qr.nextElement();
    			Part = (WTPart) VersionControlHelper.getLatestIteration(Part);
		}
		return Part;
	}
	
	

}
