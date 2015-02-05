package ext.tmt.part.listener;

import java.io.Serializable;

import ext.tmt.part.PartUtils;
import ext.tmt.utils.Contants;
import ext.tmt.utils.Debug;



import ext.tmt.utils.IBAHelper;
import ext.tmt.utils.PartUtil;
import ext.tmt.utils.Utils;
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
			if (target instanceof WTPart) {
				 WTPart part = (WTPart)target;
			        Debug.P("WTPart eventType--->" + eventType);
			        if(part!=null){
			        	part = (WTPart)Utils.getWCObject(WTPart.class,part.getNumber());
			        }
			        PartHelper.listenerWTPart((WTPart)target, eventType);
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
	    String POST_STORE = 
	      PersistenceManagerEvent.generateEventKey("POST_STORE");
	    getManagerService().addEventListener(this.listener, POST_STORE);

	    String POST_UPDATE = PersistenceManagerEvent.generateEventKey("UPDATE");
	    getManagerService().addEventListener(this.listener, POST_UPDATE);

	    String DELETE = PersistenceManagerEvent.generateEventKey("POST_DELETE");
	    getManagerService().addEventListener(this.listener, DELETE);

	    String PRE_CHECKIN = 
	      WorkInProgressServiceEvent.generateEventKey("PRE_CHECKIN");
	    getManagerService().addEventListener(this.listener, PRE_CHECKIN);

	    String POST_CHECKIN = 
	      WorkInProgressServiceEvent.generateEventKey("POST_CHECKIN");
	    getManagerService().addEventListener(this.listener, POST_CHECKIN);

	    String POST_MODIFY = 
	      PersistenceManagerEvent.generateEventKey("POST_MODIFY");
	    getManagerService().addEventListener(this.listener, POST_MODIFY);
	  }
}
