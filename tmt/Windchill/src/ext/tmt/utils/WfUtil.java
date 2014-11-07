package ext.tmt.utils;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import wt.access.AccessControlHelper;
import wt.access.AccessPermission;
import wt.access.AdHocAccessKey;
import wt.access.AdHocControlled;
import wt.change2.ChangeActivity2;
import wt.change2.ChangeException2;
import wt.change2.ChangeHelper2;
import wt.change2.ChangeOrder2;
import wt.change2.ChangeOrderIfc;
import wt.change2.ChangeRequestIfc;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeIssue;
import wt.change2.WTChangeOrder2;
import wt.change2.WTChangeRequest2;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.iba.value.IBAHolder;
import wt.inf.container.LookupSpec;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.maturity.MaturityBaseline;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.Promotable;
import wt.maturity.PromotionNotice;
import wt.maturity.PromotionTarget;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.pom.Transaction;
import wt.project.Role;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.baseline.BaselineHelper;
import wt.vc.wip.WorkInProgressHelper;
import wt.workflow.definer.WfDefinerHelper;
import wt.workflow.definer.WfProcessDefinition;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfConnector;
import wt.workflow.engine.WfContainer;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WorkItem;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.core.query.common.QueryException;


@SuppressWarnings("all")
// 获取文档的描述名称
public class WfUtil implements RemoteAccess {
	private static final String CLASSNAME = WfUtil.class.getName();
	
	/**
	 * 用代码来启动一个新的流程，
	 
	public static boolean launchPublishProcessPlanProcess(Object pbo,
			Object self) {
		WfProcess wfprocessParent = getProcess(self);
		boolean originalEnforce = SessionServerHelper.manager
				.isAccessEnforced();
		SessionServerHelper.manager.setAccessEnforced(false);
		try {
			WfProcessDefinition wfProcessDefinition = WfDefinerHelper.service
					.getProcessDefinition(ERPESIConfigUtil.getProperty(
							"com.hitachi.mpm.esi.publish.process.name",
							"PublishProcessPlanToERP"));//获取要启动的流程
			WfProcess wfprocess = wt.workflow.engine.WfEngineHelper.service
					.createProcess(wfProcessDefinition, null,
							wfprocessParent.getContainerReference());

			ProcessData processData = wfprocess.getContext();
			processData.setValue("primaryBusinessObject", pbo);

			wfprocess = WfEngineHelper.service.startProcessImmediate(wfprocess,
					processData, 1);
			if (wfprocess != null)
				return true;
		} catch (WTException e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(originalEnforce);
		}
		return false;
	}*/

	/**
	 * 流程提交环节，检查参与者角色中是否已设置承担者
	 * 
	 * @param self
	 * @param vector
	 *            流程中必填的角色key值
	 * @throws WTException
	 *             嵌套异常是: java.net.SocketException: Software caused connection
	 *             abort: socket write error
	 **/
	public static void checkPrincipalforRole(ObjectReference self,
			Vector<String> vector) throws WTException {
		if (self == null || vector == null || vector.size() <= 0)
			return;
		WfProcess wfprocess = getProcess(self);
		if (wfprocess != null) {
			Team team = (Team) wfprocess.getTeamId().getObject();
			List<Role> roleList = new ArrayList<Role>();
			
			for (int i = 0; i < vector.size(); i++) {
				Role role = Role.toRole((String) vector.get(i));
				
				if (role != null) {
					@SuppressWarnings("rawtypes")
					Enumeration enums = team.getPrincipalTarget(role);
					if (enums == null || !enums.hasMoreElements())
						roleList.add(role);
				}
			}
			if (roleList.size() > 0) {
				StringBuffer sb = new StringBuffer("\n以下角色....设置承担者:\n");
				for (int i = 0; i < roleList.size(); i++) {
					Role role = (Role) roleList.get(i);
					sb.append((i + 1) + "." + role.getDisplay(Locale.CHINA)
							+ "\n");
				}
				throw new WTException(sb.toString());
			}
		}
	}

	/**
	 * @author blueswang
	 * @param obj
	 * @return
	 * @return WfProcess
	 * @Description
	 */
	private static WfProcess getProcess(Object obj) {
		if (obj == null) {
			return null;
		}
		try {
			Persistable persistable = null;
			if (obj instanceof Persistable) {
				persistable = (Persistable) obj;
			} else if (obj instanceof ObjectIdentifier) {
				persistable = PersistenceHelper.manager
						.refresh((ObjectIdentifier) obj);
			} else if (obj instanceof ObjectReference) {
				persistable = ((ObjectReference) obj).getObject();
			}
			if (persistable == null) {
				return null;
			}
			if (persistable instanceof WorkItem) {
				persistable = ((WorkItem) persistable).getSource().getObject();
			}
			if (persistable instanceof WfActivity) {
				persistable = ((WfActivity) persistable).getParentProcess();
			}
			if (persistable instanceof WfConnector) {
				persistable = ((WfConnector) persistable).getParentProcessRef()
						.getObject();
			}
			if (persistable instanceof WfBlock) {
				persistable = ((WfBlock) persistable).getParentProcess();
			}
			if (persistable instanceof WfProcess) {
				return (WfProcess) persistable;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 流程提交环节，检查PBO是否处于检出状态
	 * 
	 * @param obj
	 * @throws WTException
	 */
	public static void isCheckout(WTObject obj) throws WTException {
		if (obj instanceof WTDocument) {
			WTDocument doc = (WTDocument) obj;
			if (WindchillUtil.isCheckout(doc)) {
				throw new WTException("文档(编号:" + doc.getNumber() + ",名称:"
						+ doc.getName() + ")处于检出状态!");
			}
		} else if (obj instanceof PromotionNotice) {
			// 升级请求，则获取升级对象
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			List<EPMDocument> checkoutEPMList = new ArrayList<EPMDocument>();
			List<WTPart> checkoutPartList = new ArrayList<WTPart>();
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof EPMDocument) {
					EPMDocument epmDoc = (EPMDocument) ptObj;
					if (WorkInProgressHelper.isCheckedOut(epmDoc))
						checkoutEPMList.add(epmDoc);
				} else if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					if (WorkInProgressHelper.isCheckedOut(wtPart))
						checkoutPartList.add(wtPart);
				}
			}
			StringBuffer sb = new StringBuffer();
			if (checkoutEPMList.size() > 0) {
				sb.append("\n以下CAD图纸处于检出状态:\n");
				for (int i = 0; i < checkoutEPMList.size(); i++) {
					EPMDocument epmDoc = (EPMDocument) checkoutEPMList.get(i);
					sb.append((i + 1) + ".编号:" + epmDoc.getNumber() + "->名称:"
							+ epmDoc.getName() + "\n");
				}
			}
			if (checkoutPartList.size() > 0) {
				sb.append("\n以下部件处于检出状态:\n");
				for (int i = 0; i < checkoutPartList.size(); i++) {
					WTPart wtPart = (WTPart) checkoutPartList.get(i);
					sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
							+ wtPart.getName() + "\n");
				}
			}
			if (sb.length() > 0)
				throw new WTException(sb.toString());
		}
	}

	/**
	 * 变更通告流程处理，判断变更通告中变更任务是否有产生的对象，有则返回true
	 * 
	 * @param obj
	 * @return
	 * @throws WTException
	 */
	public static boolean hasChangeablesAfter(WTObject obj) throws WTException {
		QueryResult qr = ChangeHelper2.service
				.getChangeActivities((ChangeOrder2) obj);
		while (qr.hasMoreElements()) {
			ChangeActivity2 qrv = (ChangeActivity2) qr.nextElement();
			QueryResult qr1 = ChangeHelper2.service.getChangeablesAfter(qrv);
			if (qr1.hasMoreElements()) {
				continue;
			}
			System.out.println("有变更任务的所得项为空！");
			return false;
		}
		return true;
	}

	/**
	 * 发布到ERP后,回写已发标志
	 * 
	 * @param obj
	 * @throws Exception
	 */ 
	public static void setSuccess2ERPFlag(Object obj) throws Exception {
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				setERPFlag(ptObj);
			}
		} else if (obj instanceof WTChangeOrder2) {
			QueryResult qr = ChangeHelper2.service
					.getChangeActivities((WTChangeOrder2) obj);
			while (qr.hasMoreElements()) {
				WTChangeActivity2 qrv = (WTChangeActivity2) qr.nextElement();
				QueryResult qr1 = ChangeHelper2.service
						.getChangeablesAfter(qrv);
				if (qr1.hasMoreElements()) {
					Object temp = qr1.nextElement();
					setERPFlag(temp);
				}
			}
		}
	}

	/**
	 * 已发送到ERP
	 * 
	 * @param obj
	 * @throws WTPropertyVetoException
	 * @throws Exception
	 */
	private static void setERPFlag(Object obj) throws WTPropertyVetoException,
			Exception {
		if (obj instanceof WTPart) {
			WTPart wtPart = (WTPart) obj;
			IBAUtils ibaUtils = new IBAUtils(wtPart);
			ibaUtils.setIBAValue("GENUINE_IMPORTED_K3_OR_NOT", "是");
			ibaUtils.updateIBAPart(wtPart);
			Debug.P("已将零部件" + wtPart.getNumber() + "发布到ERP");
		}
	}

	/**
	 * ECR流程启动后,初始化全局变量“checkOrNot”,该值来自ECR的软属性“验证与否”
	 * 
	 * @param obj
	 * @return
	 * @throws WTPropertyVetoException
	 * @throws Exception
	 */
	public static String getCheckOrNotAboutECR(Object obj)
			throws WTPropertyVetoException, Exception {
		String checkOrNot = "";
		if (obj instanceof WTChangeRequest2) {
			WTChangeRequest2 ecr = (WTChangeRequest2) obj;
			IBAUtils ibaUtils = new IBAUtils(ecr);
			checkOrNot = ibaUtils.getIBAValue("GENUINE_VERIFY_OR_NOT");
			return checkOrNot == null ? "" : checkOrNot;
		}
		return checkOrNot;
	}

	/**
	 * 设置升级对象生命周期状态
	 * 
	 * @param pn
	 * @param stateName
	 */
	public static void setPromotableLifeCycleState(PromotionNotice pn,
			String stateName) {
		QueryResult qr = null;
		try {
			qr = MaturityHelper.service.getPromotionTargets(pn);
		} catch (WTException e) {
			e.printStackTrace();
		}
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof LifeCycleManaged) {
				LifeCycleManaged obj1 = (LifeCycleManaged) obj;
				setLifeCycleState(obj1, stateName);
			}
		}
	}

	/**
	 * 设置升级对象（BOM）生命周期状态为制造
	 * 
	 * @param pn
	 * @param stateName
	 */
	public static void setPromotableLifeCycleState_1(PromotionNotice pn) {
		QueryResult qr = null;
		try {
			qr = MaturityHelper.service.getPromotionTargets(pn);
		} catch (WTException e) {
			e.printStackTrace();
		}
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof LifeCycleManaged) {
				LifeCycleManaged obj1 = (LifeCycleManaged) obj;
				if (obj1 instanceof WTPart) {
					WTPart wtPart = (WTPart) obj1;
					if (wtPart.isEndItem())// 设置成品BOM为“制造”状态
						setLifeCycleState(wtPart, "MANUFACTURING");
					else
						// 设置零部件BOM为“批量”状态
						setLifeCycleState(wtPart, "QUANTITIES");
				}
			}
		}
	}

	/**
	 * 设置升级对象（BOM）生命周期状态为正在工作
	 * 
	 * @param pn
	 * @param stateName
	 */
	public static void setPromotableLifeCycleState_2(PromotionNotice pn) {
		QueryResult qr = null;
		try {
			qr = MaturityHelper.service.getPromotionTargets(pn);
		} catch (WTException e) {
			e.printStackTrace();
		}
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof LifeCycleManaged) {
				LifeCycleManaged obj1 = (LifeCycleManaged) obj;
				if (obj1 instanceof WTPart) {
					WTPart wtPart = (WTPart) obj1;
					if (wtPart.isEndItem())
						setLifeCycleState(wtPart, "INWORK");
				}
			}
		}
	}

	/**
	 * 设置升级对象生命周期状态
	 * 
	 * @param pn
	 * @param stateName
	 */
	public static void setPromotableLifeCycleState(PromotionNotice pn) {
		QueryResult qr = null;
		try {
			qr = MaturityHelper.service.getPromotionTargets(pn);
		} catch (WTException e) {
			e.printStackTrace();
		}
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof LifeCycleManaged) {
				LifeCycleManaged obj1 = (LifeCycleManaged) obj;
				if (obj1 instanceof WTPart) {
					WTPart wtPart = (WTPart) obj1;
					if (wtPart.isEndItem())
						setLifeCycleState(obj1, "ENGINEERING");
					else
						setLifeCycleState(obj1, "SAMPLE");
				} else if (obj1 instanceof EPMDocument) {
					setLifeCycleState(obj1, "PUBLISHED");
				} else if (obj1 instanceof WTDocument) {
					setLifeCycleState(obj1, "PUBLISHED");
				}
			}
		}
	}

	public static void setPartLCStart(PromotionNotice pn) {
		QueryResult qr = null;
		pn.getMaturityState();
		Debug.P("pn.getLifeCycleState---->" + pn.getLifeCycleState().toString());
		try {
			qr = MaturityHelper.service.getPromotionTargets(pn);
		} catch (WTException e) {
			e.printStackTrace();
		}
		if (qr.size() > 1) {
			while (qr.hasMoreElements()) {
				Object obj = qr.nextElement();
				if (obj instanceof LifeCycleManaged) {
					LifeCycleManaged obj1 = (LifeCycleManaged) obj;

					Debug.P("obj1.LIFE_CYCLE_STATE---->"
							+ obj1.LIFE_CYCLE_STATE);
					Debug.P("obj1.getState()---->" + obj1.getState().toString());
					Debug.P("obj1.getLifeCycleState()---->"
							+ obj1.getLifeCycleState().toString());
					if (obj1 instanceof WTPart) {
						WTPart wtPart = (WTPart) obj1;
						String partState = wtPart.getLifeCycleState()
								.getDisplay().toString();
						Debug.P("OldPartState----->"
								+ wtPart.getLifeCycleState().getDisplay());
						// if ("样品".equals(partState)){
						// setLifeCycleState(obj1, "SMALL QUANTITIES");
						// Debug.P("NewPartState----->"+wtPart.getLifeCycleState().getDisplay());
						// }else if ("小批量".equals(partState)){
						setLifeCycleState(obj1, "QUANTITIES");
						// }
						// else if (wtPart.getLifeCycleState().equals("SAMPLE"))
						// setLifeCycleState(obj1, "QUANTITIES");

					}
				}
			}
		}
		while (qr.hasMoreElements()) {
			Object obj = qr.nextElement();
			if (obj instanceof LifeCycleManaged) {
				LifeCycleManaged obj1 = (LifeCycleManaged) obj;
				if (obj1 instanceof WTPart) {
					WTPart wtPart = (WTPart) obj1;
					String partState = wtPart.getLifeCycleState().getDisplay()
							.toString();
					Debug.P("obj1.LIFE_CYCLE_STATE---->"
							+ obj1.LIFE_CYCLE_STATE);
					Debug.P("obj1.getState()---->" + obj1.getState().toString());
					Debug.P("obj1.getLifeCycleState()---->"
							+ obj1.getLifeCycleState().toString());

					Debug.P("OldPartState----->"
							+ wtPart.getLifeCycleState().getDisplay());
					if ("样品".equals(partState)) {
						setLifeCycleState(obj1, "SMALL QUANTITIES");
						Debug.P("NewPartState----->"
								+ wtPart.getLifeCycleState().getDisplay());
					} else if ("小批量".equals(partState)) {
						setLifeCycleState(obj1, "QUANTITIES");
					}
					// else if (wtPart.getLifeCycleState().equals("SAMPLE"))
					// setLifeCycleState(obj1, "QUANTITIES");

				}
			}
		}
	}

	/**
	 * 设置对象生命周期状态
	 * 
	 * @param obj
	 * @param stateName
	 */

	public static void setLifeCycleState(LifeCycleManaged obj, String stateName) {
		if (obj == null || stateName == null) {
			return;
		}
		LookupSpec ls=null;
		State toState = State.toState(stateName);
		if (toState == null) {
			return;
		}
		WTPrincipal administrator = null;
		try {
			administrator = SessionHelper.manager.getAdministrator();
		} catch (WTException e) {
			e.printStackTrace();
		}
		WTPrincipal previous = SessionContext
				.setEffectivePrincipal(administrator);
		try {
			try {
				LifeCycleHelper.service.setLifeCycleState(obj, toState);
			} catch (WTException e) {
				e.printStackTrace();
			}
		} finally {
			SessionContext.setEffectivePrincipal(previous);
		}
	}

	/**
	 * 不需要设计图纸的部件走简单升级流程时，对升级对象做检查： 1.如果部件的需要设计图纸属性为“是”，则必须走复杂签审流程
	 * 2.如果升级对象中包含图纸，则必须走复杂签审流程
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	public static void checkNoCAD(Object obj) throws MaturityException,
			WTException {
		List<WTPart> needDesignPartList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					if (wtPart.isEndItem())
						throw new WTException("升级对象中包含成品，应使用成品(BOM)升级签审流程!");
					IBAUtils ibaUtils = new IBAUtils(wtPart);
					String design_or_not = ibaUtils
							.getIBAValue("GENUINE_DESIGN_OR_NOT");
					if (design_or_not != null && design_or_not.equals("是")) {
						// 需要设计图纸
						needDesignPartList.add(wtPart);
					}
				} else if (ptObj instanceof EPMDocument) {
					throw new WTException("升级对象中包含图纸，应使用复杂签审流程!");
				}
			}
		}
		if (needDesignPartList.size() > 0) {
			StringBuffer sb = new StringBuffer("\n以下部件需要设计图纸，应使用复杂签审流程:\n");
			for (int i = 0; i < needDesignPartList.size(); i++) {
				WTPart wtPart = (WTPart) needDesignPartList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 更新升级请求下的升级对象为
	 * 
	 * @param wtobj
	 * @throws WTException
	 */
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public static void refreshPromotionTargets(WTObject wtobj)
			throws WTException {
		if (wtobj instanceof PromotionNotice) {
			PromotionNotice pn = (PromotionNotice) wtobj;
			if (!RemoteMethodServer.ServerFlag) {
				String method = "refreshPromotionTargets";
				Class[] argTypes = new Class[] { PromotionNotice.class };
				Object[] argValues = new Object[] { pn };

				try {
					RemoteMethodServer.getDefault().invoke(method, CLASSNAME,
							null, argTypes, argValues);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				return;
			}
			Transaction transaction;
			transaction = new Transaction();
			try {
				QueryResult qr = MaturityHelper.service.getPromotionTargets(pn);
				transaction.start();
				MaturityBaseline baseline = pn.getConfiguration();

				WTSet old_set = new WTHashSet();
				WTSet new_set = new WTHashSet();
				while (qr.hasMoreElements()) {
					Object obj = qr.nextElement();
					Debug.P(obj);
					old_set.add(obj);
					if (!(obj instanceof WTDocument)) {
						if ((obj instanceof Iterated)
								&& (!VersionControlHelper
										.isLatestIteration((Iterated) obj))) {
							Object latestIt = (Object) VersionControlHelper
									.getLatestIteration((Iterated) obj);
							new_set.add(latestIt);
							Debug.P(latestIt);
						} else {
							new_set.add(obj);
						}
					}
				}

				MaturityHelper.service.deletePromotionTargets(pn, old_set);
				BaselineHelper.service.removeFromBaseline(old_set, baseline);
				old_set = null;

				MaturityHelper.service.savePromotionTargets(pn, new_set);
				BaselineHelper.service.addToBaseline(new_set, baseline);
				new_set = null;
				transaction.commit();
				transaction = null;
			} catch (Exception _wte) {
				_wte.printStackTrace();
			} finally {
				if (transaction != null)
					transaction.rollback();
			}
		}
	}

	/**
	 * 审签完成后，将签审信息返写到EPMDocument的IBA属性上，以便productview查看水印
	 * 
	 * @param obj
	 * @param roleName
	 * @param signDate
	 * @throws Exception
	 */
	public static void setSignInfo(Object obj, String roleName)
			throws Exception {
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			WTUser wtUser = (WTUser) SessionHelper.manager.getPrincipal();
			String userName = wtUser.getFullName();
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof EPMDocument) {
					EPMDocument epmDoc = (EPMDocument) ptObj;
					IBAUtils ibaUtils = new IBAUtils(epmDoc);
					ibaUtils.setIBAValue(roleName, userName);
					ibaUtils.updateIBAPart(epmDoc);
				}
			}
		}
	}

	public static void setSignInfo(Object obj, String roleName, String signDate)
			throws Exception {
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			WTUser wtUser = (WTUser) SessionHelper.manager.getPrincipal();
			String userName = wtUser.getFullName();
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof EPMDocument) {
					EPMDocument epmDoc = (EPMDocument) ptObj;
					IBAUtils ibaUtils = new IBAUtils(epmDoc);
					ibaUtils.setIBAValue(roleName, userName);
					ibaUtils.setIBAValue("ENUINE_APPROVED_DATE", signDate);
					ibaUtils.updateIBAPart(epmDoc);
				}
			}
		}
	}

	/**
	 * 部件和图纸升级流程设置参与者任务节点，提交时，判断是否包含已发布到ERP的部件
	 * 
	 * @param obj
	 * @throws Exception
	 */
	public static void isImport2ERP(Object obj) throws Exception {
		List<WTPart> partList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					IBAUtils ibaUtils = new IBAUtils(wtPart);
					String imported_k3 = ibaUtils
							.getIBAValue("GENUINE_IMPORTED_K3_OR_NOT");
					if ("是".equals(imported_k3))
						partList.add(wtPart);
				}
			}
		}
		if (partList.size() > 0) {
			StringBuffer sb = new StringBuffer("\n以下部件已发布到ERP:\n");
			for (int i = 0; i < partList.size(); i++) {
				WTPart wtPart = (WTPart) partList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	public static String setIBAByWorkItemOid(String workItemOid,
			Map<String, String> ibaMap) throws Exception {
		if (!RemoteMethodServer.ServerFlag) {
			return (String) RemoteMethodServer.getDefault().invoke(
					"setIBAByWorkItemOid", WfUtil.class.getName(), null,
					new Class[] { String.class, Map.class },
					new Object[] { workItemOid, ibaMap });
		}
		WfProcess process = getProcessByWorkItemOid(workItemOid);
		return setIBA(process, ibaMap);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getIBAByWorkItemOid(String workItemOid,
			List<String> ibaNamesList) throws Exception {
		if (!RemoteMethodServer.ServerFlag) {
			return (List<String>) RemoteMethodServer.getDefault().invoke(
					"getIBAByWorkItemOid", WfUtil.class.getName(), null,
					new Class[] { String.class, List.class },
					new Object[] { workItemOid, ibaNamesList });
		}
		WfProcess process = getProcessByWorkItemOid(workItemOid);
		return getIBA(process, ibaNamesList);
	}

	@SuppressWarnings("unchecked")
	public static List<String> getIBA(WfProcess process,
			List<String> ibaNamesList) throws Exception {
		List<String> list = new ArrayList<String>();
		if (!RemoteMethodServer.ServerFlag) {
			return (List<String>) RemoteMethodServer.getDefault().invoke(
					"getIBA", WfUtil.class.getName(), null,
					new Class[] { WfProcess.class, List.class },
					new Object[] { process, ibaNamesList });
		} else {
			ProcessData pd = process.getContext();
			Object obj = pd.getValue("primaryBusinessObject");
			if (obj instanceof WTDocument) {
				WTDocument wtDoc = (WTDocument) obj;
				IBAUtils ibaUtils = new IBAUtils(wtDoc);
				for (Iterator<String> it = ibaNamesList.iterator(); it
						.hasNext();) {
					String ibaName = it.next();
					String ibaValue = ibaUtils.getIBAValue(ibaName);
					list.add(ibaValue == null ? "" : ibaValue);
				}
			} else if (obj instanceof WTChangeRequest2) {
				WTChangeRequest2 ecr = (WTChangeRequest2) obj;
				IBAUtils ibaUtils = new IBAUtils(ecr);
				for (Iterator<String> it = ibaNamesList.iterator(); it
						.hasNext();) {
					String ibaName = it.next();
					String ibaValue = ibaUtils.getIBAValue(ibaName);
					list.add(ibaValue == null ? "" : ibaValue);
				}
			}
			return list;
		}
	}

	public static String setIBA(WfProcess process, Map<String, String> ibaMap)
			throws Exception {
		if (!RemoteMethodServer.ServerFlag) {
			return (String) RemoteMethodServer.getDefault().invoke("setIBA",
					WfUtil.class.getName(), null,
					new Class[] { WfProcess.class, Map.class },
					new Object[] { process, ibaMap });
		} else {
			ProcessData pd = process.getContext();
			Object obj = pd.getValue("primaryBusinessObject");
			if (obj instanceof WTDocument) {
				WTDocument wtDoc = (WTDocument) obj;
				Debug.P(wtDoc);
				IBAUtils ibaUtils = new IBAUtils(wtDoc);
				for (Iterator<String> it = ibaMap.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = ibaMap.get(key);
					Debug.P("key---> " + key + "   value--> " + value);
					ibaUtils.setIBAValue(key, value);
				}
				ibaUtils.updateIBAPart(wtDoc);
			} else if (obj instanceof WTChangeRequest2) {
				WTChangeRequest2 ecr = (WTChangeRequest2) obj;
				Debug.P(ecr);
				IBAUtils ibaUtils = new IBAUtils(ecr);
				for (Iterator<String> it = ibaMap.keySet().iterator(); it
						.hasNext();) {
					String key = it.next();
					String value = ibaMap.get(key);
					Debug.P("key---> " + key + "   value--> " + value);
					ibaUtils.setIBAValue(key, value);
				}
				ibaUtils.updateIBAPart(ecr);
			}
			return "PASS";
		}
	}

	public static WfProcess getProcessByWorkItemOid(String oid)
			throws WTRuntimeException, WTException {
		Debug.P("*******getProcessByWorkItemOid oid:" + oid);
		ReferenceFactory rf = new ReferenceFactory();
		Persistable p = rf.getReference(oid).getObject();
		WorkItem wi = null;
		WfActivity wfAct = null;
		if (p instanceof WorkItem) {
			wi = (WorkItem) p;
			wfAct = (WfActivity) wi.getSource().getObject();
			Debug.P("p instanceof WorkItem");
		} else if (p instanceof WfActivity) {
			wfAct = (WfActivity) rf.getReference(oid).getObject();
			Debug.P("p instanceof WfActivity");
		} else if (p instanceof WfProcess) {
			Debug.P("p instanceof WfProcess");
			return (WfProcess) p;
		}
		return getProcessByActivity(wfAct);
	}

	public static WfProcess getProcessByActivity(WfActivity wfAct) {
		WfContainer wfcont = (WfContainer) wfAct.getParentProcessRef()
				.getObject();
		if (wfcont instanceof WfBlock) {
			return null;
		}
		return (WfProcess) wfcont;
	}

	/**
	 * 授予流程角色审核权限
	 * 
	 * @param wtobj
	 * @param self
	 * @param roleName
	 * @param apVector
	 */
	public static void setObjectReviewAccess(WTObject wtobj,
			ObjectReference self, String roleName,
			Vector<AccessPermission> apVector) {
		
		
		Role role = Role.toRole(roleName);
		setObjectAccess(wtobj, self, role, apVector, null);
	}

	@SuppressWarnings("rawtypes")
	private static void setObjectAccess(Persistable wtobj,
			ObjectReference self, Role role,
			Vector<AccessPermission> accessPermissions, Team team) {

		if (wtobj == null || !(wtobj instanceof AdHocControlled)
				|| self == null) {
			return;
		}
		if (role == null) {
			return;
		}

		if (team == null) {
			WfProcess wfprocess = getProcess(self);
			team = (Team) wfprocess.getTeamId().getObject();
			// team.getRoles().size()
			// team.getRoles().elements()
		}
		if (team != null) {
			Enumeration enums = null;
			try {
				enums = team.getPrincipalTarget(role);
			} catch (WTException e) {
				e.printStackTrace();
			}
			while (enums.hasMoreElements()) {
				Object obj = enums.nextElement();
				if (obj instanceof WTPrincipalReference) {
					WTPrincipalReference principalref = (WTPrincipalReference) obj;
					try {
						setObjectAccess((Persistable) wtobj, principalref,
								accessPermissions);
					} catch (WTException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
    /**
     * 授予某对象某权限
     * @author blueswang
     * @param persistable 某对象 wtobj
     * @param wtPrincipalReference
     * @param accessPermissions 授予的权限
     * @return
     * @throws WTException
     * @return Persistable
     * @Description
     */
	@SuppressWarnings("deprecation")
	private static Persistable setObjectAccess(Persistable persistable,
			WTPrincipalReference wtPrincipalReference,
			Vector<AccessPermission> accessPermissions) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Transaction transaction = new Transaction();
		try {
			transaction.start();
			persistable = PersistenceHelper.manager.refresh(persistable);
			if (persistable instanceof AdHocControlled) {
				AdHocControlled adhoccontrolled = (AdHocControlled) persistable;
				try {
					adhoccontrolled = AccessControlHelper.manager
							.addPermissions(adhoccontrolled,
									wtPrincipalReference, accessPermissions,
									AdHocAccessKey.WNC_ACCESS_CONTROL);
					PersistenceServerHelper.manager.update(adhoccontrolled,
							false);
				} catch (Exception e) {
					Debug.P("-->setObjectAccess: " + e.getLocalizedMessage());
					if (adhoccontrolled != null) {
						Debug.P("-->setObjectAccess: Failed on object: "
								+ adhoccontrolled);
					}
				}
			}

			transaction.commit();
			transaction = null;
		} catch (WTException e) {
			Debug.P("-->setObjectAccess: " + e.getLocalizedMessage());
		} finally {
			if (transaction != null)
				transaction.rollback();
			SessionServerHelper.manager.setAccessEnforced(flag);
		}

		return persistable;
	}

	/**
	 * 移除流程角色的审核权限
	 * 
	 * @param wtobj
	 * @param self
	 * @param roleName
	 * @throws WTException
	 * @throws MaturityException
	 */
	public static void removeObjectReviewAccess(WTObject wtobj,
			ObjectReference self, String roleName,
			AccessPermission accessPermission) throws MaturityException,
			WTException {
		Debug.P(accessPermission.getDisplay());
		Vector<AccessPermission> accessPermissions = new Vector();
		accessPermissions.add(accessPermission);
		Debug.P("---->" + roleName);
		Debug.P("remove access --》" + accessPermissions.size());
		Role role = Role.toRole(roleName);
		// if (wtobj instanceof WTPart) {
		// WTPart part = (WTPart) wtobj;
		// if (part.isEndItem()) {
		// return;
		// }
		// Debug.P("开始移除权限"+accessPermissions.size());
		// removeObjectAccess(wtobj, self, role, accessPermissions);
		// }
	}

	// private static void removeObjectAccess(Persistable wtobj,
	// ObjectReference self, Role role,
	// Vector<AccessPermission> accessPermissions) throws TeamException,
	// WTException {
	// WTContainer wtContainer = ((WTPart) wtobj).getContainer();
	// ContainerTeam cTeam = ContainerTeamHelper.service
	// .getContainerTeam((ContainerTeamManaged) wtContainer);
	// if (wtobj == null || !(wtobj instanceof AdHocControlled)
	// || !(wtobj instanceof Persistable) || self == null) {
	// return;
	// }
	// // if (team == null) {
	// // WfProcess wfprocess = getProcess(self);
	// // team = (Team) wfprocess.getTeamId().getObject();
	// // }
	// Debug.P(cTeam);
	// if (cTeam != null) {
	// List list = null;
	// try {
	// list = cTeam.getAllPrincipalsForTarget(role);
	// } catch (WTException e) {
	// e.printStackTrace();
	// }
	// Debug.P(list);
	// for (Iterator it = list.iterator(); it.hasNext();) {
	// WTPrincipalReference principalref = (WTPrincipalReference) it
	// .next();
	// try {
	// Debug.P("1111111111111111111111");
	// for (int i = 0; i < accessPermissions.size(); i++) {
	// AccessPermission ap = accessPermissions.get(i);
	// String dddd = ap.getDisplay(Locale.SIMPLIFIED_CHINESE);
	// WTPrincipal user = principalref.getPrincipal();
	// Debug.P(user.getName()
	// + "->>>>"
	// + dddd
	// + "->>>>"
	// + AccessControlHelper.manager.hasAccess(user,
	// wtobj, accessPermissions.get(i)));
	// }
	// removeObjectAccess((Persistable) wtobj, principalref,
	// accessPermissions);
	// Debug.P("2222222222222222");
	// for (int i = 0; i < accessPermissions.size(); i++) {
	// AccessPermission ap = accessPermissions.get(i);
	// String dddd = ap.getDisplay(Locale.SIMPLIFIED_CHINESE);
	// WTPrincipal user = principalref.getPrincipal();
	// Debug.P(user.getName()
	// + "->>>>"
	// + dddd
	// + "->>>>"
	// + AccessControlHelper.manager.hasAccess(user,
	// wtobj, accessPermissions.get(i)));
	// }
	// } catch (WTException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	/**
	 * 移除权限
	 */
	public static void removeObjectAccess(WTPart part,
			WTPrincipalReference wtPrincipalReference,
			AccessPermission accessPermission) {
		Vector<AccessPermission> accessPermissions = new Vector();
		accessPermissions.add(accessPermission);
		System.out.println("accessPermissions.size()---->"
				+ accessPermissions.size());
		Debug.P("创建人 =----》" + wtPrincipalReference.getFullName());
		boolean result = false;
		try {
			result = AccessControlHelper.manager
					.hasAccess(wtPrincipalReference.getPrincipal(), part,
							accessPermission);
			System.out.println("创建人--》" + wtPrincipalReference.getFullName()
					+ "对" + part.getNumber() + "有"
					+ accessPermission.getFullDisplay() + "的权限  ==  " + result);
			removeObjectAccess((Persistable) part, wtPrincipalReference,
					accessPermissions);
			result = AccessControlHelper.manager
					.hasAccess(wtPrincipalReference.getPrincipal(), part,
							accessPermission);
			System.out.println("移除创建人--》" + wtPrincipalReference.getFullName()
					+ "对" + part.getNumber() + "有"
					+ accessPermission.getFullDisplay() + "的权限  ==  " + result);
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 移除访问控制权限
	 * 
	 * @param persistable
	 * @param wtPrincipalReference
	 * @param accessPermissions
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	private static Persistable removeObjectAccess1(Persistable persistable,
			WTPrincipalReference wtPrincipalReference,
			Vector<AccessPermission> accessPermissions) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Transaction transaction = new Transaction();
		try {
			transaction.start();
			Debug.P("remover  modify and modify_conter");
			persistable = PersistenceHelper.manager.refresh(persistable);
			if (persistable instanceof AdHocControlled) {
				AdHocControlled adhoccontrolled = (AdHocControlled) persistable;
				try {
					adhoccontrolled = AccessControlHelper.manager
							.removePermissions(adhoccontrolled,
									wtPrincipalReference, accessPermissions,
									AdHocAccessKey.WNC_ACCESS_CONTROL);
					PersistenceServerHelper.manager.update(adhoccontrolled,
							false);
					System.out.println("--->--"
							+ persistable.getConceptualClassname() + "--->"
							+ persistable.getType() + "-->"
							+ persistable.getIdentity());
				} catch (Exception e) {
					Debug.P("-->setObjectAccess: " + e.getLocalizedMessage());
					if (adhoccontrolled != null) {
						Debug.P("-->setObjectAccess: Failed on object: "
								+ adhoccontrolled);
					}
				}
			}
			transaction.commit();
			transaction = null;
			Persistable persistable1 = persistable;
			return persistable1;
		} catch (WTException e) {
			Debug.P("-->setObjectAccess: " + e.getLocalizedMessage());
		} finally {
			if (transaction != null)
				transaction.rollback();
			SessionServerHelper.manager.setAccessEnforced(flag);
		}

		return null;
	}

	/**
	 * 移除生命周期中设定的权限
	 * 
	 * @param persistable
	 * @param wtPrincipalReference
	 * @param accessPermissions
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	private static Persistable removeObjectAccess(Persistable persistable,
			WTPrincipalReference wtPrincipalReference,
			Vector<AccessPermission> accessPermissions) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Transaction transaction = new Transaction();
		try {
			transaction.start();
			Debug.P("remover  modify and modify_conter");
			persistable = PersistenceHelper.manager.refresh(persistable);
			if (persistable instanceof AdHocControlled) {
				AdHocControlled adhoccontrolled = (AdHocControlled) persistable;
				try {
					adhoccontrolled = AccessControlHelper.manager
							.removePermissions(adhoccontrolled,
									wtPrincipalReference, accessPermissions,
									AdHocAccessKey.WNC_LIFECYCLE);
					PersistenceServerHelper.manager.update(adhoccontrolled,
							false);
					System.out.println("--->--"
							+ persistable.getConceptualClassname() + "--->"
							+ persistable.getType() + "-->"
							+ persistable.getIdentity());
				} catch (Exception e) {
					Debug.P("-->setObjectAccess: " + e.getLocalizedMessage());
					if (adhoccontrolled != null) {
						Debug.P("-->setObjectAccess: Failed on object: "
								+ adhoccontrolled);
					}
				}
			}
			transaction.commit();
			transaction = null;
			Persistable persistable1 = persistable;
			return persistable1;
		} catch (WTException e) {
			Debug.P("-->setObjectAccess: " + e.getLocalizedMessage());
		} finally {
			if (transaction != null)
				transaction.rollback();
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return null;
	}

	/*
	 * 对part添加权限
	 */
	public static void setWTPartAccess(WTPart part,
			WTPrincipalReference wtPrincipalReference,
			AccessPermission accessPermission) {
		Vector<AccessPermission> accessPermissions = new Vector();
		accessPermissions.add(accessPermission);
		System.out.println("accessPermissions.size()---->"
				+ accessPermissions.size());
		Debug.P("创建人 =----》" + wtPrincipalReference.getFullName());
		boolean result = false;
		try {
			result = AccessControlHelper.manager
					.hasAccess(wtPrincipalReference.getPrincipal(), part,
							accessPermission);
			System.out.println("创建人--》" + wtPrincipalReference.getFullName()
					+ "对" + part.getNumber() + "有"
					+ accessPermission.getFullDisplay() + "的权限  ==  " + result);
			setWTPartAccess((Persistable) part, wtPrincipalReference,
					accessPermissions);
			result = AccessControlHelper.manager
					.hasAccess(wtPrincipalReference.getPrincipal(), part,
							accessPermission);
			System.out.println("恢复创建人--》" + wtPrincipalReference.getFullName()
					+ "对" + part.getNumber() + "有"
					+ accessPermission.getFullDisplay() + "的权限  ==  " + result);
		} catch (WTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 给（角色）wtPrincipalReference对（对象）persistable设置（权限）accessPermissions
	 * 
	 * @param persistable
	 * @param wtPrincipalReference
	 * @param accessPermissions
	 * @return
	 * @throws WTException
	 */
	@SuppressWarnings("deprecation")
	private static Persistable setWTPartAccess(Persistable persistable,
			WTPrincipalReference wtPrincipalReference,
			Vector<AccessPermission> accessPermissions) throws WTException {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		Transaction transaction = new Transaction();
		try {
			transaction.start();
			persistable = PersistenceHelper.manager.refresh(persistable);
			if (persistable instanceof AdHocControlled) {
				AdHocControlled adhoccontrolled = (AdHocControlled) persistable;
				try {
					adhoccontrolled = AccessControlHelper.manager
							.addPermissions(adhoccontrolled,
									wtPrincipalReference, accessPermissions,
									AdHocAccessKey.WNC_LIFECYCLE);
					PersistenceServerHelper.manager.update(adhoccontrolled,
							false);
					System.out.println("--->--"
							+ persistable.getConceptualClassname() + "--->"
							+ persistable.getType() + "-->"
							+ persistable.getIdentity());
				} catch (Exception e) {
					Debug.P("-->setWTPartAccess: " + e.getLocalizedMessage());
					if (adhoccontrolled != null) {
						Debug.P("-->setWTPartAccess: Failed on object: "
								+ adhoccontrolled);
					}
				}
			}
			transaction.commit();
			transaction = null;
			Persistable persistable1 = persistable;
			return persistable1;
		} catch (WTException e) {
			Debug.P("-->setObjectAccess: " + e.getLocalizedMessage());
		} finally {
			if (transaction != null)
				transaction.rollback();
			SessionServerHelper.manager.setAccessEnforced(flag);
		}

		return null;
	}

	/**
	 * 根据ECN获取与其关联的ECR
	 * 
	 * @param ecn
	 * @return
	 * @throws ChangeException2
	 * @throws WTException
	 */
	public static List<WTChangeRequest2> getECRsByECN(WTChangeOrder2 ecn)
			throws ChangeException2, WTException {
		List<WTChangeRequest2> ecrs = new ArrayList<WTChangeRequest2>();
		QueryResult qr = ChangeHelper2.service.getChangeRequest(
				(ChangeOrderIfc) ecn, true);
		while (qr.hasMoreElements()) {
			WTChangeRequest2 ecr = (WTChangeRequest2) qr.nextElement();
			ecrs.add(ecr);
		}
		return ecrs;
	}

	/**
	 * 根据ECR获取与其关联的ECN
	 * 
	 * @param ecr
	 * @return
	 * @throws ChangeException2
	 * @throws WTException
	 */
	public static List<WTChangeOrder2> getECNsByECR(WTChangeRequest2 ecr)
			throws ChangeException2, WTException {
		List<WTChangeOrder2> ecns = new ArrayList<WTChangeOrder2>();
		QueryResult qr = ChangeHelper2.service.getChangeOrders(
				(ChangeRequestIfc) ecr, true);
		while (qr.hasMoreElements()) {
			WTChangeOrder2 ecn = (WTChangeOrder2) qr.nextElement();
			ecns.add(ecn);
			
		}
		return ecns;
	}

	/**
	 * 根据ECR获取与其关联的PR
	 * 
	 * @param ecr
	 * @return
	 * @throws ChangeException2
	 * @throws WTException
	 */
	public static List<WTChangeIssue> getPRsByECR(WTChangeRequest2 ecr)
			throws ChangeException2, WTException {
		List<WTChangeIssue> prs = new ArrayList<WTChangeIssue>();
		QueryResult qr = ChangeHelper2.service.getChangeIssues(
				(ChangeRequestIfc) ecr, true);
		while (qr.hasMoreElements()) {
			WTChangeIssue pr = (WTChangeIssue) qr.nextElement();
			prs.add(pr);
		}
		return prs;
	}

	/**
	 * 在BOM转产时，判断顶层部件是否已发送到ERP
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static boolean checkEndItemPubishedOrNot(Object obj)
			throws Exception {
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					IBAUtils ibaUtils = new IBAUtils(wtPart);
					String importedK3 = ibaUtils
							.getIBAValue("GENUINE_IMPORTED_K3_OR_NOT");
					if ("是".equals(importedK3))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * 流程驱动改变物料编号任务之前，初始化升级对象oid与number的map
	 * 
	 * @param obj
	 * @return
	 * @throws MaturityException
	 * @throws WTException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map initialPromotableMap(Object obj)
			throws MaturityException, WTException {
		Map numberMap = new HashMap();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					numberMap.put(WindchillUtil.getOidString(wtPart),
							wtPart.getNumber());
				}
			}
		}
		return numberMap;
	}

	/**
	 * 改变物料编号任务提交时，对变更前后的物料编号进行对比，确认是否分配了K/3编号
	 * 
	 * @param numberMap
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	@SuppressWarnings("rawtypes")
	public static void checkRenameOrNot(Map numberMap, Object obj)
			throws MaturityException, WTException {
		List<WTPart> partList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					String oid = WindchillUtil.getOidString(wtPart);
					String beforeChangeNumber = (String) numberMap.get(oid);
					// 当前part未改变编号
					if (wtPart.getNumber().equals(beforeChangeNumber))
						partList.add(wtPart);
				}
			}
		}
		if (partList.size() > 0) {
			StringBuffer sb = new StringBuffer("\n以下部件未分配K/3编号，请按照工作指示处理:\n");
			for (int i = 0; i < partList.size(); i++) {
				WTPart wtPart = (WTPart) partList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 流程中上传附件前，保存PBO的附件列表
	 * 
	 * @param obj
	 * @return
	 * @throws MaturityException
	 * @throws WTException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List initialAttatchmentFileList(Object obj)
			throws MaturityException, WTException {
		List attachmentFileNameList = new ArrayList();
		// 根据PBO获取附件列表
		Debug.P(obj);
		QueryResult qr = ContentHelper.service.getContentsByRole(
				(ContentHolder) obj, ContentRoleType.SECONDARY);
		while (qr.hasMoreElements()) {
			ContentItem ci = (ContentItem) qr.nextElement();
			if (ci instanceof ApplicationData) {
				ApplicationData appData = (ApplicationData) ci;
				Debug.P(appData.getFileName());
				attachmentFileNameList.add(appData.getFileName());
			}
		}
		return attachmentFileNameList;
	}

	/**
	 * 需要上传附件的任务节点，在完成任务时，判断是否上传了附件
	 * 
	 * @param attachmentFileNameList
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	@SuppressWarnings("rawtypes")
	public static void checkUploadAttachmentOrNot(List attachmentFileNameList,
			Object obj) throws MaturityException, WTException {
		boolean flag = false;
		// 根据PBO获取附件列表
		QueryResult qr = ContentHelper.service.getContentsByRole(
				(ContentHolder) obj, ContentRoleType.SECONDARY);
		while (qr.hasMoreElements()) {
			ContentItem ci = (ContentItem) qr.nextElement();
			Debug.P(ci);
			if (ci instanceof ApplicationData) {
				ApplicationData appData = (ApplicationData) ci;
				if (!attachmentFileNameList.contains(appData.getFileName())) {
					Debug.P(appData.getFileName());
					flag = true;
					return;
				}
			}
		}
		if (!flag)
			throw new WTException("\n请根据工作指示，上传相应的附件!\n");
	}

	/**
	 * 零部件审批流程提交时，判断升级对象必须是单个部件，即非BOM，非成品
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	public static void checkNotExistBOM(Object obj) throws MaturityException,
			WTException {
		Map<String, String> map = new HashMap<String, String>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					if (wtPart.isEndItem())
						map.put(wtPart.getNumber(),
								"成品(编号:" + wtPart.getNumber()
										+ "),请使用BOM审批流程;\n");
					else {
						QueryResult qr1 = WTPartHelper.service
								.getUsesWTPartMasters(wtPart);
						if (qr1.size() > 0)
							map.put(wtPart.getNumber(),
									"零部件(编号:" + wtPart.getNumber()
											+ ")有BOM,请使用BOM审批流程;\n");
					}
				}
			}
		}
		if (map.keySet().size() > 0) {
			StringBuffer sb = new StringBuffer(
					"\n以下零部件或者成品不符合该升级流程要求，\n请终止或删除该升级流程:\n");
			int i = 1;
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = map.get(key);
				sb.append((i++) + "." + value);
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * BOM审批流程提交时，判断 1.必须有BOM， 2.BOM中子零件的生命周期状态必须为样品、小批量、批量、工程、制造
	 * 3.成品的生命周期状态不能为工程 4.普通零部件(顶层部件)的生命周期状态不能为样品
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	public static void checkMustBeBOM(Object obj) throws MaturityException,
			WTException {
		Map<String, String> map = new HashMap<String, String>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					String state = wtPart.getLifeCycleState().getDisplay(
							Locale.SIMPLIFIED_CHINESE);
					if (wtPart.isEndItem()) {
						if (state.equals("工程"))
							map.put(wtPart.getNumber(),
									"成品(编号:" + wtPart.getNumber()
											+ ")的生命周期状态为工程,请使用转产审批流程;\n");
						else {
							QueryResult qr1 = WTPartHelper.service
									.getUsesWTPartMasters(wtPart);
							if (qr1.size() == 0)
								map.put(wtPart.getNumber(),
										"成品(编号:" + wtPart.getNumber()
												+ ")没有BOM,请先搭建BOM;\n");
							else {
								while (qr1.hasMoreElements()) {
									WTPartUsageLink link = (WTPartUsageLink) qr1
											.nextElement();
									WTPartMaster master = (WTPartMaster) link
											.getUses();
									QueryResult qr2 = VersionControlHelper.service
											.allVersionsOf(master);
									WTPart child = (WTPart) qr2.nextElement();
									String childState = child
											.getLifeCycleState().getDisplay(
													Locale.SIMPLIFIED_CHINESE);
									if (!childState.equals("样品")
											&& !childState.equals("小批量")
											&& !childState.equals("批量")
											&& !childState.equals("工程")
											&& !childState.equals("制造")) {
										map.put(wtPart.getNumber(),
												"成品(编号:"
														+ wtPart.getNumber()
														+ ")的BOM中含有非样品、小批量、批量、工程、制造的零部件;\n");
									}
								}
							}
						}
					} else {
						if (state.equals("样品"))
							map.put(wtPart.getNumber(),
									"零部件(编号:" + wtPart.getNumber()
											+ ")的生命周期状态为样品,无需再审批;\n");
						else {
							QueryResult qr1 = WTPartHelper.service
									.getUsesWTPartMasters(wtPart);
							if (qr1.size() == 0)
								map.put(wtPart.getNumber(),
										"零部件(编号:"
												+ wtPart.getNumber()
												+ ")没有BOM,请使用零部件(含图纸、不含图纸)审批流程;\n");
							else {
								while (qr1.hasMoreElements()) {
									WTPartUsageLink link = (WTPartUsageLink) qr1
											.nextElement();
									WTPartMaster master = (WTPartMaster) link
											.getUses();
									QueryResult qr2 = VersionControlHelper.service
											.allVersionsOf(master);
									WTPart child = (WTPart) qr2.nextElement();
									String childState = child
											.getLifeCycleState().getDisplay(
													Locale.SIMPLIFIED_CHINESE);
									if (!childState.equals("样品")
											&& !childState.equals("小批量")
											&& !childState.equals("批量")
											&& !childState.equals("工程")
											&& !childState.equals("制造")) {
										map.put(wtPart.getNumber(),
												"零部件(编号:"
														+ wtPart.getNumber()
														+ ")的BOM中含有非样品、小批量、批量、工程、制造的零部件;\n");
									}
								}
							}
						}
					}
				}
			}
		}
		if (map.keySet().size() > 0) {
			StringBuffer sb = new StringBuffer(
					"\n以下零部件或者成品不符合该升级流程要求，\n请终止或删除该升级流程:\n");
			int i = 1;
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = map.get(key);
				sb.append((i++) + "." + value);
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 转产审批流程提交时，判断升级对象必须为工程状态的成品
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	public static void checkMustBeEndItem(Object obj) throws MaturityException,
			WTException {
		Map<String, String> map = new HashMap<String, String>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					QueryResult qr1 = WTPartHelper.service
							.getUsesWTPartMasters(wtPart);
					if (qr1.size() == 0) {
						map.put(wtPart.getNumber(),
								"零部件(编号:" + wtPart.getNumber() + ")无BOM结构;\n");
					} else {
						if (!wtPart.getLifeCycleState()
								.getDisplay(Locale.SIMPLIFIED_CHINESE)
								.equals("工程")
								|| !wtPart.getLifeCycleState()
										.getDisplay(Locale.SIMPLIFIED_CHINESE)
										.equals("小批量"))
							map.put(wtPart.getNumber(),
									"成品(编号:" + wtPart.getNumber()
											+ ")的生命周期状态不是工程或小批量,请使用BOM审批流程;\n");
					}
				}
			}
		}
		if (map.keySet().size() > 0) {
			StringBuffer sb = new StringBuffer(
					"\n以下零部件或者成品不符合该升级流程要求，\n请终止或删除该升级流程:\n");
			int i = 1;
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String key = it.next();
				String value = map.get(key);
				sb.append((i++) + "." + value);
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 零部件审批及BOM审批流程的提交任务节点检查升级对象必须经过编号申请流程。
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	@SuppressWarnings("rawtypes")
	public static void isAssociatedPromotionNotice_(Object obj)
			throws MaturityException, WTException {
		List<WTPart> partList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				Debug.P(ptObj);
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					WTCollection list = getAllParts(wtPart.getNumber());
					list.add(wtPart);
					Debug.P(list.size());
					WTHashSet pns = (WTHashSet) MaturityHelper.service
							.getPromotionNotices(list);
					Debug.P(pns.size());
					// 只关联了一个升级请求，及当前升级请求
					if (pns.size() == 1) {
						partList.add(wtPart);
						continue;
					} else {
						// 关联多个升级请求，在判断是有零部件编号申请流程
						boolean flag = false;
						for (Iterator it = pns.iterator(); it.hasNext();) {
							Object temp = it.next();
							Debug.P(temp);
							if (temp instanceof ObjectReference) {
								ObjectReference or = (ObjectReference) temp;
								Object ddd = or.getObject();
								PromotionNotice pn = (PromotionNotice) ddd;
								Debug.P(pn.getName());
								List<WfProcess> wflist = getAssociatedProcess(pn);
								for (Iterator<WfProcess> it1 = wflist
										.iterator(); it1.hasNext();) {
									WfProcess tempProcess = it1.next();
									String templateName = tempProcess
											.getTemplate().getName();
									String templateState = pn.getState()
											.toString();
									Debug.P(templateName);
									Debug.P(templateState);
									if (templateName
											.equals("Genuine_K3NumberApply_WF")
											&& templateState.equals("APPROVED")) {

										flag = true;
										break;
									}
								}
								if (flag)
									break;
							}
						}
						if (!flag) {
							// 关联的升级请求中没有零部件编号申请流程
							partList.add(wtPart);
						}
					}
				}
			}
		}

		if (partList.size() > 0) {
			StringBuffer sb = new StringBuffer("\n以下部件的编号申请流程申请未完成:\n");
			for (int i = 0; i < partList.size(); i++) {
				WTPart wtPart = (WTPart) partList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 零部件审批及BOM审批流程的提交任务节点检查升级对象必须经过编号申请流程。
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	@SuppressWarnings("rawtypes")
	public static void isAssociatedPromotionNotice(Object obj)
			throws MaturityException, WTException {
		List<WTPart> partList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				Debug.P(ptObj);
				boolean flag = false;
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					IBAUtils utils = new IBAUtils(wtPart);
					String isPassK3 = utils
							.getIBAValue("GENUINE_PASSK3_NUM_OR_NOT");
					Debug.P("isPassK3  --> " + isPassK3);
					if ("是".equals(isPassK3)) {
						flag = true;
						break;
					}
					if (!flag) {
						Debug.P("wtPart   -->" + wtPart);
						// 该部件没有经过“K3编码申请流程”
						partList.add(wtPart);
					}
				}
			}
		}

		if (partList.size() > 0) {
			StringBuffer sb = new StringBuffer(
					"\n以下部件需要先经过编号申请流程申请编号:\n 请选择“取消”路由并点击“完成任务”以终止该流程：\n");
			for (int i = 0; i < partList.size(); i++) {
				WTPart wtPart = (WTPart) partList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 判断是否已通过K3编码申请
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	public static void isAssociatedPromotionNotice2(Object obj)
			throws MaturityException, WTException {
		List<WTPart> partList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				boolean flag = false;
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				Debug.P(ptObj);
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					IBAUtils utils = new IBAUtils(wtPart);
					String isPassK3 = utils
							.getIBAValue("GENUINE_PASSK3_NUM_OR_NOT");
					if ("是".equals(isPassK3)) {
						// 该部件已经过“K3编码申请流程”
						partList.add(wtPart);
					}
				}
			}
		}

		if (partList.size() > 0) {
			StringBuffer sb = new StringBuffer("\n以下部件已经过编号申请流程申请编号:\n");
			for (int i = 0; i < partList.size(); i++) {
				WTPart wtPart = (WTPart) partList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 判断是否已提交K3编码申请
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	public static void isAssociatedPromotionNotice2_(Object obj)
			throws MaturityException, WTException {
		List<WTPart> partList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				boolean flag = false;
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				Debug.P(ptObj);
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					IBAUtils utils = new IBAUtils(wtPart);
					String isPassK3 = utils
							.getIBAValue("GENUINE_PASSK3_NUM_OR_NOT");
					if ("提交".equals(isPassK3) || "是".equals(isPassK3)) {
						// 该部件已经过“K3编码申请流程”
						partList.add(wtPart);
					}
				}
			}
		}

		if (partList.size() > 0) {
			StringBuffer sb = new StringBuffer(
					"\n以下部件已提交过编号申请流程申请编号，请取消本次流程:\n");
			for (int i = 0; i < partList.size(); i++) {
				WTPart wtPart = (WTPart) partList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 零部件审批及BOM审批流程的提交任务节点检查升级对象必须经过编号申请流程。
	 * 
	 * @param obj
	 * @throws MaturityException
	 * @throws WTException
	 */
	@SuppressWarnings("rawtypes")
	public static void isAssociatedPromotionNotice3(Object obj)
			throws MaturityException, WTException {
		List<WTPart> partList = new ArrayList<WTPart>();
		if (obj instanceof PromotionNotice) {
			QueryResult qr = MaturityHelper.service.getPromotionTargets(
					(PromotionNotice) obj, false);
			while (qr.hasMoreElements()) {
				PromotionTarget pt = (PromotionTarget) qr.nextElement();
				Promotable ptObj = pt.getPromotable();
				Debug.P(ptObj);
				if (ptObj instanceof WTPart) {
					WTPart wtPart = (WTPart) ptObj;
					WTCollection list = getAllParts(wtPart.getNumber());
					list.add(wtPart);
					Debug.P(list.size());
					WTHashSet pns = (WTHashSet) MaturityHelper.service
							.getPromotionNotices(list);
					Debug.P(pns.size());
					// 只关联了一个升级请求，及当前升级请求
					if (pns.size() == 1) {
						partList.add(wtPart);
						continue;
					} else {
						// 关联多个升级请求，在判断是有零部件编号申请流程
						boolean flag = false;
						for (Iterator it = pns.iterator(); it.hasNext();) {
							Object temp = it.next();
							Debug.P(temp);
							if (temp instanceof ObjectReference) {
								ObjectReference or = (ObjectReference) temp;
								Object ddd = or.getObject();
								PromotionNotice pn = (PromotionNotice) ddd;
								Debug.P(pn.getName());
								List<WfProcess> wflist = getAssociatedProcess(pn);
								for (Iterator<WfProcess> it1 = wflist
										.iterator(); it1.hasNext();) {
									WfProcess tempProcess = it1.next();
									String templateName = tempProcess
											.getTemplate().getName();
									Debug.P(templateName);
									if (templateName
											.equals("Genuine_NoDrawPartReview_WF")) {
										flag = true;
										break;
									}
								}
								if (flag)
									break;
							}
						}
						if (!flag) {
							// 关联的升级请求中已经过“零部件（不含图纸）升级审批流程”
							partList.add(wtPart);
						}
					}
				}
			}
		}

		if (partList.size() > 0) {
			StringBuffer sb = new StringBuffer("\n以下部件已申请了“零部件（不含图纸）升级审批流程”:\n");
			for (int i = 0; i < partList.size(); i++) {
				WTPart wtPart = (WTPart) partList.get(i);
				sb.append((i + 1) + ".编号:" + wtPart.getNumber() + "->名称:"
						+ wtPart.getName() + "\n");
			}
			throw new WTException(sb.toString());
		}
	}

	/**
	 * 得到同一编号部件的所有版本对像
	 * 
	 * @param Part编号
	 * @return Part对像
	 */
	@SuppressWarnings("deprecation")
	public static WTCollection getAllParts(String number) {
		List<WTPart> arrayList = new ArrayList<WTPart>();
		WTCollection list = new WTArrayList();
		try {
			QuerySpec qs = new QuerySpec(WTPart.class);
			WhereExpression we = new SearchCondition(WTPart.class,
					WTPart.NUMBER, "=", number);
			qs.appendWhere(we);
			QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);

			while (qr.hasMoreElements()) {
				WTPart part = (WTPart) qr.nextElement();
				list.add(part);
			}
			Debug.P(list);
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}
		return list;
	}

	public static List<WfProcess> getAssociatedProcess(PromotionNotice obj)
			throws WTException {
		List<WfProcess> list = new ArrayList<WfProcess>();
		Debug.P("############");
		QuerySpec qs = new QuerySpec();
		qs.setAdvancedQueryEnabled(true);
		int processIndex = qs.appendClassList(WfProcess.class, true);
		WhereExpression we = new SearchCondition(WfProcess.class,
				"businessObjReference", SearchCondition.EQUAL,
				"OR:wt.maturity.PromotionNotice:"
						+ obj.getPersistInfo().getObjectIdentifier().getId());
		qs.appendWhere(we, new int[] { processIndex });
		Debug.P(qs);
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		Debug.P(qr.size());
		while (qr.hasMoreElements()) {
			Persistable[] p = (Persistable[]) qr.nextElement();
			WfProcess wfprocess = (WfProcess) p[0];
			list.add(wfprocess);
		}
		return list;
	}
}