package ext.tmt.EPMdoc.listener;

import java.io.Serializable;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.services.Manager;
import wt.services.ManagerService;
import wt.services.ManagerServiceFactory;
import wt.util.WTException;

/**
 * 远程调用接口,在Method Server中运行EPMDocService
 * @author Administrator
 *
 */
public class EPMDocServiceFwd
  implements RemoteAccess, EPMDocService, Serializable
{
  private static final long serialVersionUID = 9009992850558025689L;
  static final boolean SERVER;
  private static final String FC_RESOURCE = "wt.fc.fcResource";
  private static final String CLASSNAME = EPMDocServiceFwd.class
    .getName();

  static {
    SERVER = RemoteMethodServer.ServerFlag;
  }

  private static Manager getManager()
    throws WTException
  {
    Manager manager = ManagerServiceFactory.getDefault().getManager(
      EPMDocService.class);
    if (manager == null) {
      Object[] param = { EPMDocService.class.getName() };
      throw new WTException("wt.fc.fcResource", "40", param);
    }
    return manager;
  }
}