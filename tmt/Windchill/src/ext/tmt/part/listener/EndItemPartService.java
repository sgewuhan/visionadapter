package ext.tmt.part.listener;

import java.io.Serializable;

import ext.tmt.part.PartUtils;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;



import ext.tmt.utils.IBAHelper;
import ext.tmt.utils.PartUtil;
import wt.events.KeyedEvent;
import wt.events.KeyedEventListener;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceManagerEvent;
import wt.fc.delete.DeleteManagerEvent;
import wt.part.WTPart;
import wt.services.ManagerException;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.vc.wip.WorkInProgressServiceEvent;


/**
 * 
 * @author Eilaiwang  2014-7-28
 * 
 * @Description
 */
public class EndItemPartService extends StandardManager implements PartService,
		Serializable {
	private static final long serialVersionUID = 3373744579193231186L;
	private static final String CLASSNAME = EndItemPartService.class.getName();

	// 定义事件监听器
	private KeyedEventListener listener;

	class PartListener extends ServiceEventListenerAdapter {

		public void notifyVetoableEvent(Object event) throws WTException ,Exception{
			if (!(event instanceof KeyedEvent)) {
				return;
			}

			KeyedEvent keyedEvent = (KeyedEvent) event;
			// 获取事件目标对象
			Object target = keyedEvent.getEventTarget();
			// 获取事件类型
			String eventType = keyedEvent.getEventType();
			if(eventType.equals("POST_MULTI_DELETE")){
				return;
			}
			if (target instanceof WTPart) {
				WTPart part=(WTPart)target;
				Debug.P("WTPart eventType--->"+eventType);
			//if(!eventType.equals("POST_DELETE")){
				Debug.P("part1---->"+part);
				 Debug.P(part.getNumber()+" -------------version1s---->"+part.getVersionIdentifier().getValue()+"    version1---->"+part.getIterationIdentifier().getValue());
				 part=PartUtils.getPartByNumber(part.getNumber());
			//}
				 Debug.P("part2---->"+part);
				// Debug.P(part.getNumber()+" ------------- version1s---->"+part.getVersionIdentifier().getValue()+"    version1---->"+part.getIterationIdentifier().getValue());
				PartHelper.listenerWTPart((WTPart) target, eventType);
//				PartHelper.listenerWTPart1((WTPart) target, eventType);
			}

		}

		public PartListener(String manager_name) {
			super(manager_name);
		}
	}
	/**
	 * @deprecated Method getConceptualClassname is deprecated
	 */

	public String getConceptualClassname() {
		return CLASSNAME;
	}
    
	public static EndItemPartService newEndItemPartService() throws WTException {
		EndItemPartService instance = new EndItemPartService();
		instance.initialize();
		return instance;
	}

	protected void performStartupProcess() throws ManagerException {
		listener = new PartListener(getConceptualClassname());

		// 监听VersionControlServiceEvent
		String POST_STORE = PersistenceManagerEvent
				.generateEventKey(PersistenceManagerEvent.POST_STORE);
		getManagerService().addEventListener(listener, POST_STORE);
		
//		String INSERT = PersistenceManagerEvent
//				.generateEventKey(PersistenceManagerEvent.INSERT); 
//		getManagerService().addEventListener(listener, INSERT);
		String POST_UPDATE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.UPDATE);
		getManagerService().addEventListener(listener, POST_UPDATE);
		
		String DELETE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.PRE_DELETE);
		getManagerService().addEventListener( listener, DELETE);
		
		String POST_DELETE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_DELETE);
		getManagerService().addEventListener( listener, POST_DELETE);
		
		String POST_MULTI_DELETE=PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.POST_MULTI_DELETE);
		getManagerService().addEventListener( listener, POST_MULTI_DELETE);
		
		String PRE_CHECKIN = WorkInProgressServiceEvent
		        .generateEventKey(WorkInProgressServiceEvent.PRE_CHECKIN);
        getManagerService().addEventListener(listener, PRE_CHECKIN);
       
        String POST_CHECKIN =WorkInProgressServiceEvent
		        .generateEventKey(WorkInProgressServiceEvent.POST_CHECKIN);
        getManagerService().addEventListener(listener, POST_CHECKIN);
 
        
//        String PER_POST = PersistenceManagerEvent
//                .generateEventKey(PersistenceManagerEvent.PRE_STORE);
//         getManagerService().addEventListener(listener, PER_POST);
//		String NEW_ITERATION = VersionControlServiceEvent
//				.generateEventKey(VersionControlServiceEvent.NEW_ITERATION);
//		getManagerService().addEventListener(listener, NEW_ITERATION);
//
//		// 监听PersistenceManagerEvent
		String POST_MODIFY = PersistenceManagerEvent
				.generateEventKey(PersistenceManagerEvent.POST_MODIFY);
		getManagerService().addEventListener(listener, POST_MODIFY);
		
		String POST_MARK_FOR_DELETE =DeleteManagerEvent.generateEventKey(DeleteManagerEvent.POST_MARK_FOR_DELETE);
		getManagerService().addEventListener(listener, POST_MARK_FOR_DELETE);
		
		String PRE_MARK_FOR_DELETE =DeleteManagerEvent.generateEventKey(DeleteManagerEvent.PRE_MARK_FOR_DELETE);
		getManagerService().addEventListener(listener, PRE_MARK_FOR_DELETE);
		
		String PRE_MAKE_UNRESTORABLE =DeleteManagerEvent.generateEventKey(DeleteManagerEvent.PRE_MAKE_UNRESTORABLE);
		getManagerService().addEventListener(listener, PRE_MAKE_UNRESTORABLE);
		
		String POST_MAKE_UNRESTORABLE =DeleteManagerEvent.generateEventKey(DeleteManagerEvent.POST_MAKE_UNRESTORABLE);
		getManagerService().addEventListener(listener, POST_MAKE_UNRESTORABLE);
		
	}
}
