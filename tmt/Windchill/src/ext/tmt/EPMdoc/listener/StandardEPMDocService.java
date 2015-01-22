package ext.tmt.EPMdoc.listener;

import java.io.Serializable;

import com.ptc.windchill.cadx.common.util.WorkspaceUtilities;

import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;



import ext.tmt.utils.EPMDocUtil;
import ext.tmt.utils.EPMUtil;
import ext.tmt.utils.GenericUtil;
import ext.tmt.utils.IBAHelper;
import ext.tmt.utils.IBAUtils;
import ext.tmt.utils.Utils;
import wt.epm.EPMDocument;
import wt.epm.workspaces.EPMWorkspace;
import wt.epm.workspaces.EPMWorkspaceHelper;
import wt.epm.workspaces.EPMWorkspaceManagerEvent;
import wt.events.KeyedEvent;
import wt.events.KeyedEventListener;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.inf.container.WTContainer;
import wt.part.WTPart;
import wt.services.ManagerException;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.vc.VersionControlServiceEvent;
import wt.vc.wip.WorkInProgressServiceEvent;


/**
 * 
 * @author Eilaiwang  2014-7-28
 * 
 * @Description
 */
public class StandardEPMDocService extends StandardManager implements EPMDocService,
		Serializable {
	private static final long serialVersionUID = 3373744579193231186L;
	private static final String CLASSNAME = StandardEPMDocService.class.getName();

	// 定义事件监听器
	private KeyedEventListener listener;

	class EPMDocListener extends ServiceEventListenerAdapter {

		public void notifyVetoableEvent(Object event) throws WTException ,Exception{
			if (!(event instanceof KeyedEvent)) {
				return;
			}

			KeyedEvent keyedEvent = (KeyedEvent) event;
			// 获取事件目标对象
			Object target = keyedEvent.getEventTarget();
			// 获取事件类型
			String eventType = keyedEvent.getEventType();
			if (target instanceof EPMDocument) {
				EPMDocument epm=(EPMDocument)target;
				Debug.P("EPM eventType--->"+eventType+"---Object--->"+epm);
				if(!eventType.equals("POST_DELETE")){
//					epm=EPMDocUtil.getEPMDocByNumber(epm.getNumber());
					epm=(EPMDocument)Utils.getWCObject(EPMDocument.class, epm.getNumber());
				}
				EPMDocHelper.listenerEPMDoc(epm, eventType);
//				EPMDocHelper.listenerEPMDoc1(epm, eventType);
				//PersistenceHelper.manager.store(epm);
			} 
		}

		public EPMDocListener(String manager_name) {
			super(manager_name);
		}
	}
	/**
	 * @deprecated Method getConceptualClassname is deprecated
	 */

	public String getConceptualClassname() {
		return CLASSNAME;
	}
    
	public static StandardEPMDocService newStandardEPMDocService() throws WTException {
		StandardEPMDocService instance = new StandardEPMDocService();
		instance.initialize();
		return instance;
	}

	protected void performStartupProcess() throws ManagerException {
		listener = new EPMDocListener(getConceptualClassname());

		// 监听VersionControlServiceEvent
		String POST_STORE = PersistenceManagerEvent
				.generateEventKey(PersistenceManagerEvent.POST_STORE);
		getManagerService().addEventListener(listener, POST_STORE);
		
		String PRE_DELETE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.PRE_DELETE);
		getManagerService().addEventListener( listener, PRE_DELETE);
		
		String POST_DELETE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_DELETE);
		getManagerService().addEventListener( listener, POST_DELETE);
		
		String POST_MULTI_DELETE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_MULTI_DELETE);
		getManagerService().addEventListener( listener, POST_MULTI_DELETE);
		
        String POST_CHECKIN =WorkInProgressServiceEvent
		        .generateEventKey(WorkInProgressServiceEvent.POST_CHECKIN);
        getManagerService().addEventListener(listener, POST_CHECKIN);
        
        String POST_UPDATE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.UPDATE);
		getManagerService().addEventListener(listener, POST_UPDATE);
		
		 String POST_MODIFY=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_MODIFY);
			getManagerService().addEventListener(listener, POST_MODIFY);
		//add by qiaokai
//	    getManagerService().addEventListener(listener, EPMWorkspaceManagerEvent.generateEventKey(EPMWorkspaceManagerEvent.POST_WORKSPACE_CHECKIN));
 
//        String PER_POST = PersistenceManagerEvent
//                .generateEventKey(PersistenceManagerEvent.PRE_STORE);
//         getManagerService().addEventListener(listener, PER_POST);
		String NEW_ITERATION = VersionControlServiceEvent
				.generateEventKey(VersionControlServiceEvent.NEW_ITERATION);
		getManagerService().addEventListener(listener, NEW_ITERATION);
		
		String NEW_VERSION = VersionControlServiceEvent
				.generateEventKey(VersionControlServiceEvent.NEW_VERSION);
		getManagerService().addEventListener(listener, NEW_VERSION);

	}
}
