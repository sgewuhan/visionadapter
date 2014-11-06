package ext.tmt.EPMdoc.listener;

import java.io.Serializable;

import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.services.Manager;
import wt.services.ManagerServiceFactory;
import wt.util.WTException;

/**
 * 
 * @author Eilaiwang  2014-7-28
 * 
 * @Description
 */
public class EPMDocServiceFwd implements RemoteAccess, EPMDocService,
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9009992850558025689L;
	static final boolean SERVER;
	@SuppressWarnings("unused")
	private static final String FC_RESOURCE = "wt.fc.fcResource";
	@SuppressWarnings("unused")
	private static final String CLASSNAME = EPMDocServiceFwd.class
			.getName();

	static {
		SERVER = RemoteMethodServer.ServerFlag;
	}
	public EPMDocServiceFwd() {
	}

	@SuppressWarnings("unused")
	private static Manager getManager() throws WTException {
		Manager manager = ManagerServiceFactory.getDefault().getManager(
				EPMDocService.class);
		if (manager == null) {
			Object param[] = { EPMDocService.class.getName() };
			throw new WTException("wt.fc.fcResource", "40", param);
		} else {
			return manager;
		}
	}

}