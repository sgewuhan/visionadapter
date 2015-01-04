/**
 * Source File Name:IBAHelper.java
 * Description: ��IBA�����йصĴ�����
 */
package ext.tmt.utils;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import wt.clients.widgets.NumericToolkit;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.WTObject;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerNodeView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.AttributeOrgNodeView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.RatioDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.litedefinition.TimestampDefView;
import wt.iba.definition.litedefinition.URLDefView;
import wt.iba.definition.litedefinition.UnitDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.AttributeContainer;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueException;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.BooleanValueDefaultView;
import wt.iba.value.litevalue.FloatValueDefaultView;
import wt.iba.value.litevalue.IntegerValueDefaultView;
import wt.iba.value.litevalue.RatioValueDefaultView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.litevalue.TimestampValueDefaultView;
import wt.iba.value.litevalue.URLValueDefaultView;
import wt.iba.value.litevalue.UnitValueDefaultView;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueHelper;
import wt.introspection.WTIntrospectionException;
import wt.org.OrganizationOwned;
import wt.part.WTPart;
import wt.part.WTPartUsageLink;
import wt.part.WTProductInstance2;
import wt.query.DateHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.units.Unit;
import wt.util.WTException;
import wt.util.WTMessage;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTStandardDateFormat;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.server.TypeIdentifierUtility;

import wt.change2.WTChangeIssue;

public class IBAHelper implements Serializable, wt.method.RemoteAccess {

	private static Locale LOCALE = Locale.CHINA;
	private static final String CLASSNAME;
	private static boolean VERBOSE = true;
	private static String attrorg;

	public static TypeDefinitionReference getTypeRef(String softType) {
		TypeDefinitionReference	ref = TypedUtility.getTypeDefinitionReference(softType);
		return ref;
	}

	public static String getIBAValue(WTObject obj, String ibaName)
	throws WTException {

		String value = null;
		String ibaClass = "wt.iba.definition.StringDefinition";
	
		try {
			if (obj instanceof IBAHolder) {
				IBAHolder ibaholder = (IBAHolder) obj;
				DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
		
				if (defaultattributecontainer != null) {
					if (VERBOSE)
						System.out
								.println(" -- Get the values from the container");
					AbstractValueView avv = getIBAValueView(
							defaultattributecontainer, ibaName);
					if (avv != null) {
						value = IBAValueUtility.getLocalizedIBAValueDisplayString(avv,LOCALE);
						if (VERBOSE)
							System.out
									.println(" ** ---- " +avv.getDefinition().getClass().getName()+ " Value >>>" + value);
					} else {
//						if (VERBOSE)
//							Debug.P(" ** ---- NO VALUE ");
					}
				}
			}
		} catch (RemoteException rexp) {
//			Debug.P(" ** !!!!! ** ERROR Getting IBS");
			rexp.printStackTrace();
		}
		
//		if (VERBOSE)
//			Debug.P(" ** END " + CLASSNAME + ".getIBAStringValue()");
		return value;
	}
	
	public static double getIBAUnitValue(WTObject obj, String ibaName)
			throws WTException {

		double value =  0;
		String ibaClass = "wt.iba.definition.UnitDefinition";

		try {
			if (obj instanceof IBAHolder) {
				IBAHolder ibaholder = (IBAHolder) obj;
				DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);

				if (defaultattributecontainer != null) {
					if (VERBOSE)
						System.out
								.println(" -- Get the values from the container");
					AbstractValueView avv = getIBAValueView(
							defaultattributecontainer, ibaName, ibaClass);
					if (avv != null) {
						value = ((UnitValueDefaultView) avv).getValue();						
						if (VERBOSE)
							System.out
									.println(" ** ---- String Value " + value);
					} else {
//						if (VERBOSE)
//							Debug.P(" ** ---- NO VALUE ");
					}
				}
			}
		} catch (RemoteException rexp) {
//			Debug.P(" ** !!!!! ** ERROR Getting IBS");
			rexp.printStackTrace();
		}

//		if (VERBOSE)
//			Debug.P(" ** END " + CLASSNAME + ".getIBAStringValue()");
		return value;
	}

	public static String getIBAStringValue(WTObject obj, String ibaName)
	throws WTException {

String value = null;
String ibaClass = "wt.iba.definition.StringDefinition";

try {
	if (obj instanceof IBAHolder) {
		IBAHolder ibaholder = (IBAHolder) obj;
		DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);

		if (defaultattributecontainer != null) {
			if (VERBOSE)
				System.out
						.println(" -- Get the values from the container");
			AbstractValueView avv = getIBAValueView(
					defaultattributecontainer, ibaName, ibaClass);
			if (avv != null) {
				value = ((StringValueDefaultView) avv).getValue();
				wt.iba.value.litevalue.UnitValueDefaultView s;
				if (VERBOSE)
					System.out
							.println(" ** ---- String Value " + value);
			} else {
//				if (VERBOSE)
//					Debug.P(" ** ---- NO VALUE ");
			}
		}
	}
} catch (RemoteException rexp) {
//	Debug.P(" ** !!!!! ** ERROR Getting IBS");
	rexp.printStackTrace();
}

//if (VERBOSE)
//	Debug.P(" ** END " + CLASSNAME + ".getIBAStringValue()");
return value;
}
	public static double getIBAFloatValue(WTObject obj, String ibaName)
			throws WTException {

		double value = 0;
		String ibaClass = "wt.iba.definition.FloatDefinition";

		try {
			if (obj instanceof IBAHolder) {
				IBAHolder ibaholder = (IBAHolder) obj;
				DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);

				if (defaultattributecontainer != null) {
					if (VERBOSE)
						System.out
								.println(" -- Get the values from the container");
					AbstractValueView avv = getIBAValueView(
							defaultattributecontainer, ibaName, ibaClass);
					if (avv != null) {
						value = ((FloatValueDefaultView) avv).getValue();
						if (VERBOSE)
							System.out
									.println(" ** ---- String Value " + value);
					} else {
//						if (VERBOSE)
//							Debug.P(" ** ---- NO VALUE ");
					}
				}
			}
		} catch (RemoteException rexp) {
//			Debug.P(" ** !!!!! ** ERROR Getting IBS");
			rexp.printStackTrace();
		}

//		if (VERBOSE)
//			Debug.P(" ** END " + CLASSNAME + ".getIBAFloatValue()");

		return value;
	}

	/**
	 * Description: �õ����������IBA���� Created on 2005-09-22
	 * 
	 * @param wto
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type Hashtable
	 */
	public static Hashtable getIBAStringValue(WTObject wto, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		Hashtable hashTable = new Hashtable();
		if (wto == null)
			return hashTable;

		if (!(wto instanceof IBAHolder)) {
			return hashTable;
		}

		IBAHolder ibaHolder = (IBAHolder) wto;
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// ���IBA����
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;

		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder
				.getAttributeContainer());

		try {
			// ������֯������WTObject������
			if (!(wto instanceof OrganizationOwned)) {
				// ֻ��OrganizationOwned�����ܻ����֯������
				return hashTable;
			}

			// ������֯����
			WTProperties wtproperties = WTProperties.getLocalProperties();
			String organizationName = wtproperties
					.getProperty("property.orgcontainer");
			if (organizationName != null)
				organizationName = organizationName.trim();
			else
				organizationName = ((OrganizationOwned) wto)
						.getOrganizationName();

			ibaDefNode = getAttributeOrganizer(organizationName);

			ibaDefNodes = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);

			if (ibaDefNodes != null) {
				for (int i = 0; i < ibaDefNodes.length; i++) {
					if (ibaDefNodes[i] == null)
						continue;

					String propertyName = ibaDefNodes[i].getName();

					String propertyValue = getIBAStringValue(wto, propertyName);
					if (StringUtil.nullOrBlank(propertyValue))
						continue;

					hashTable.put(propertyName, propertyValue);
				}
			}
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return hashTable;
	}

	public static DefaultAttributeContainer getContainer(IBAHolder ibaholder)
			throws WTException, RemoteException {

//		if (VERBOSE)
//			Debug.P(" ** START " + CLASSNAME + ".getContainer()");

		ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder,
				null, LOCALE, null);
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();

//		if (VERBOSE)
//			Debug.P(" ** END " + CLASSNAME + ".getContainer()");

		return defaultattributecontainer;
	}

	public static AbstractValueView getIBAValueView(
			DefaultAttributeContainer dac, String ibaName, String ibaClass)
			throws WTException {

//		if (VERBOSE)
//			Debug.P(" ** START " + CLASSNAME + ".getIBAValueView()");

		AbstractValueView aabstractvalueview[] = null;
		AbstractValueView avv = null;

		aabstractvalueview = dac.getAttributeValues();
		for (int j = 0; j < aabstractvalueview.length; j++) {
			String thisIBAName = aabstractvalueview[j].getDefinition()
					.getName();
			String thisIBAValue = IBAValueUtility
					.getLocalizedIBAValueDisplayString(aabstractvalueview[j],
							LOCALE);
			String thisIBAClass = (aabstractvalueview[j].getDefinition())
					.getAttributeDefinitionClassName();
//			if (VERBOSE)
//				Debug.P(" ** -- IBA " + thisIBAName + " - "
//						+ thisIBAValue + " - " + thisIBAClass);
			if (thisIBAName.equals(ibaName) && thisIBAClass.equals(ibaClass)) {
				avv = aabstractvalueview[j];
//				if (VERBOSE)
//					Debug.P(" ** -- SET ");
				break;
			}
		}

//		if (VERBOSE)
//			Debug.P(" ** END " + CLASSNAME + ".getIBAValueView()");

		return avv;
	}

	public static AbstractValueView getIBAValueView(
			DefaultAttributeContainer dac, String ibaName)
			throws WTException {

//		if (VERBOSE)
//			Debug.P(" ** START " + CLASSNAME + ".getIBAValueView()");

		AbstractValueView aabstractvalueview[] = null;
		AbstractValueView avv = null;

		aabstractvalueview = dac.getAttributeValues();
		for (int j = 0; j < aabstractvalueview.length; j++) {
			String thisIBAName = aabstractvalueview[j].getDefinition()
					.getName();
			String thisIBAValue = IBAValueUtility
					.getLocalizedIBAValueDisplayString(aabstractvalueview[j],
							LOCALE);
			String thisIBAClass = (aabstractvalueview[j].getDefinition())
					.getAttributeDefinitionClassName();
//			if (VERBOSE)
//				Debug.P(" ** -- IBA " + thisIBAName + " - "
//						+ thisIBAValue + " - " + thisIBAClass);
			if (thisIBAName.equals(ibaName)) {
				avv = aabstractvalueview[j];
//				if (VERBOSE)
//					Debug.P(" ** -- SET ");
				break;
			}
		}

//		if (VERBOSE)
//			Debug.P(" ** END " + CLASSNAME + ".getIBAValueView()");

		return avv;
	}
	public static String getSoftType(WTObject obj) throws WTException {

//		if (VERBOSE)
//			Debug.P(" ** START " + CLASSNAME + ".getSoftType()");

		String typename = "";

		TypeIdentifier type = TypeIdentifierUtility.getTypeIdentifier(obj);
		typename = type.getTypename();

//		if (VERBOSE)
//			Debug.P(" ** END " + CLASSNAME + ".getSoftType()");

		return typename;

	}

	/**
	 * Description: ����IBA���� Created on 2005-06-08
	 * 
	 * @param propertyName
	 * @param propertyValue
	 * @param part
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException, RemoteExceptiontion return_type WTPart
	 */
	public static WTPart setIBAProperty(String propertyName,
			Object propertyValue, WTPart part, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (part == null)
			return part;
		IBAHolder ibaholder = (IBAHolder) part;
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();
		try {

			ibaholder = IBAValueHelper.service.refreshAttributeContainer(
					ibaholder, null, locale, null);
			defaultattributecontainer = (DefaultAttributeContainer) ibaholder
					.getAttributeContainer();
			Object obj = null;
			Object obj1 = null;
			String s = propertyName.toString();
			String s1 = propertyValue.toString();

			wt.iba.definition.litedefinition.AttributeDefDefaultView attributedefdefaultview = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(s);
			if (attributedefdefaultview == null) {
				Object aobj[] = { CLASSNAME };
				String s3 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "3", aobj)
						+ "'" + s + "'.";
				throw new WTException(s3);
			}
			wt.iba.value.litevalue.AbstractValueView aabstractvalueview[] = defaultattributecontainer
					.getAttributeValues(attributedefdefaultview);
			if (aabstractvalueview.length > 1) {
				Object aobj1[] = { CLASSNAME };
				String s4 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "0", aobj1)
						+ "(" + s + ").";
				throw new WTException(s4);
			}
			if (aabstractvalueview.length < 1) {
				Object obj2 = null;
				if (attributedefdefaultview instanceof IntegerDefView) {
					Long long2 = new Long(s1);
					long l1 = long2.longValue();
					obj2 = new IntegerValueDefaultView(
							(IntegerDefView) attributedefdefaultview, l1);
				}/* else if (attributedefdefaultview instanceof FloatDefView) {
					BigDecimal bigdecimal = new BigDecimal(s1);
					int j = NumericToolkit.countSigFigs(s1);
					obj2 = new FloatValueDefaultView(
							(FloatDefView) attributedefdefaultview, bigdecimal
									.doubleValue(), j);
				}*/
				else if (attributedefdefaultview instanceof FloatDefView) {
					BigDecimal bigdecimal = new BigDecimal(s1);
					
					int j = NumericToolkit.countSigFigs(s1);
					obj2 = new FloatValueDefaultView(
							(FloatDefView) attributedefdefaultview, Float.parseFloat(s1), j);
				}
				else if (attributedefdefaultview instanceof BooleanDefView) {
					Boolean boolean2 = new Boolean(s1);
					boolean flag1 = boolean2.booleanValue();
					obj2 = new BooleanValueDefaultView(
							(BooleanDefView) attributedefdefaultview, flag1);
				} else if (attributedefdefaultview instanceof StringDefView)
					obj2 = new StringValueDefaultView(
							(StringDefView) attributedefdefaultview, s1);
				else if (attributedefdefaultview instanceof UnitDefView) {
					StringTokenizer stringtokenizer2 = new StringTokenizer(s1,
							" ");
					String s7 = stringtokenizer2.nextToken();
					BigDecimal bigdecimal1 = new BigDecimal(s7);
					int k = NumericToolkit.countSigFigs(s7);
					String s10 = stringtokenizer2.nextToken();
					Unit unit1 = new Unit(bigdecimal1.doubleValue(), 14, s10);
					double d4 = unit1.getValue();
					obj2 = new UnitValueDefaultView(
							(UnitDefView) attributedefdefaultview, d4, k);
				} else if (attributedefdefaultview instanceof RatioDefView) {
					StringTokenizer stringtokenizer3 = new StringTokenizer(s1,
							":");
					Double double3 = new Double(stringtokenizer3.nextToken());
					Double double5 = new Double(stringtokenizer3.nextToken());
					double d2 = double3.doubleValue() / double5.doubleValue();
					obj2 = new RatioValueDefaultView(
							(RatioDefView) attributedefdefaultview, d2, double5
									.doubleValue());
				} else if (attributedefdefaultview instanceof TimestampDefView) {
					String s5 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper1 = new DateHelper(s1);
					Timestamp timestamp1 = new Timestamp(datehelper1.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp1, 3, locale, TimeZone
							.getTimeZone(s5));
					obj2 = new TimestampValueDefaultView(
							(TimestampDefView) attributedefdefaultview,
							timestamp1);
				} else if (attributedefdefaultview instanceof URLDefView) {
					obj2 = new URLValueDefaultView(
							(URLDefView) attributedefdefaultview, s1, "");
				} else {
					Object aobj3[] = { CLASSNAME };
					String s8 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj3)
							+ "'" + s + "'.";
					throw new WTException(s8);
				}
				defaultattributecontainer
						.addAttributeValue(((wt.iba.value.litevalue.AbstractValueView) (obj2)));
			} else {
				if (aabstractvalueview[0] instanceof IntegerValueDefaultView) {
					Long long1 = new Long(s1);
					long l = long1.longValue();
					((IntegerValueDefaultView) aabstractvalueview[0])
							.setValue(l);
				} else if (aabstractvalueview[0] instanceof FloatValueDefaultView) {
					Float float1 = new Float(s1);
					int i = NumericToolkit.countSigFigs(s1);
					double d = float1.doubleValue();
					((FloatValueDefaultView) aabstractvalueview[0]).setValue(d);
					((FloatValueDefaultView) aabstractvalueview[0])
							.setPrecision(i);
				} else if (aabstractvalueview[0] instanceof BooleanValueDefaultView) {
					Boolean boolean1 = new Boolean(s1);
					boolean flag = boolean1.booleanValue();
					((BooleanValueDefaultView) aabstractvalueview[0])
							.setValue(flag);
				} else if (aabstractvalueview[0] instanceof StringValueDefaultView)
					((StringValueDefaultView) aabstractvalueview[0])
							.setValue(s1);
				else if (aabstractvalueview[0] instanceof UnitValueDefaultView) {
					StringTokenizer stringtokenizer = new StringTokenizer(s1,
							" ");
					Double double1 = new Double(stringtokenizer.nextToken());
					String s9 = stringtokenizer.nextToken();
					Unit unit = new Unit(double1.doubleValue(), 14, s9);
					double d3 = unit.getValue();
					((UnitValueDefaultView) aabstractvalueview[0]).setValue(d3);
				} else if (aabstractvalueview[0] instanceof RatioValueDefaultView) {
					StringTokenizer stringtokenizer1 = new StringTokenizer(s1,
							":");
					Double double2 = new Double(stringtokenizer1.nextToken());
					Double double4 = new Double(stringtokenizer1.nextToken());
					double d1 = double2.doubleValue() / double4.doubleValue();
					((RatioValueDefaultView) aabstractvalueview[0])
							.setValue(d1);
					((RatioValueDefaultView) aabstractvalueview[0])
							.setDenominator(double4.doubleValue());
				} else if (aabstractvalueview[0] instanceof TimestampValueDefaultView) {
					String s2 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper = new DateHelper(s1);
					Timestamp timestamp = new Timestamp(datehelper.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp, 3, locale, TimeZone
							.getTimeZone(s2));
					((TimestampValueDefaultView) aabstractvalueview[0])
							.setValue(timestamp);
				} else if (aabstractvalueview[0] instanceof URLValueDefaultView) {
					((URLValueDefaultView) aabstractvalueview[0]).setValue(s1);
				} else {
					Object aobj2[] = { CLASSNAME };
					String s6 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj2)
							+ "'" + s + "'.";
					throw new WTException(s6);
				}
				defaultattributecontainer
						.updateAttributeValue(aabstractvalueview[0]);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new WTException(exception);
		}
	
		
		
	
			

		/*
		 * 
		 * IBAHolder ibaHolder = (IBAHolder)part; ibaHolder =
		 * IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null,
		 * locale, null);
		 * 
		 * if(ibaHolder.getAttributeContainer() == null) { try { ibaHolder =
		 * IBAValueHelper.service.refreshAttributeContainer(ibaHolder, null,
		 * locale, null); if (ibaHolder.getAttributeContainer() == null) {
		 * ibaHolder.setAttributeContainer(new DefaultAttributeContainer()); } }
		 * catch(wt.util.WTException e) { ibaHolder.setAttributeContainer(new
		 * DefaultAttributeContainer()); } catch(java.rmi.RemoteException e) {
		 * ibaHolder.setAttributeContainer(new DefaultAttributeContainer()); } }
		 * 
		 * // ���IBA���� AbstractAttributeDefinizerNodeView ibaDefNode,
		 * ibaDefNode1, ibaDefNode2; AbstractAttributeDefinizerNodeView
		 * ibaDefNodes[]; AttributeDefDefaultView ibaDefView = null;
		 * 
		 * AbstractValueView ibaValueViews[]; DefaultAttributeContainer
		 * container =
		 * (DefaultAttributeContainer)(ibaHolder.getAttributeContainer());
		 * 
		 * try { // ������֯�������㲿�������� WTProperties wtproperties =
		 * WTProperties.getLocalProperties(); String organizationName =
		 * wtproperties.getProperty("property.orgcontainer");
		 * if(organizationName!=null) organizationName =
		 * organizationName.trim(); else organizationName =
		 * part.getOrganizationName();
		 * 
		 * ibaDefNode = getAttributeOrganizer(organizationName); ibaDefNodes =
		 * IBADefinitionHelper.service.getAttributeChildren(ibaDefNode);
		 * 
		 * // �õ����propertyName��Ӧ�Ľڵ� ibaDefNode = getNode(ibaDefNodes,
		 * propertyName);
		 * container.deleteAttributeValues(getDefaultViewObject(ibaDefNode));
		 * 
		 * 
		 * if(propertyValue.getClass().getName().equalsIgnoreCase("java.lang.Double"
		 * ) ||
		 * propertyValue.getClass().getName().equalsIgnoreCase("java.lang.Float"
		 * )) { // ���������ֵ��Double�ͻ���Float�� BigDecimal bigdecimal = new
		 * BigDecimal(propertyValue.toString());
		 * Debug.P(" 181 start setIBAProperty bigdecimal.toString() "
		 * + bigdecimal.toString()); int j =
		 * NumericToolkit.countSigFigs(propertyValue.toString());
		 * Debug.P(" 182 start setIBAProperty j " + j);
		 * FloatValueDefaultView ibaValueView = null;
		 * Debug.P(" 183 start setIBAProperty propertyValue.toString() "
		 * + propertyValue.toString()); ibaValueView = new
		 * FloatValueDefaultView((FloatDefView)getDefaultViewObject(ibaDefNode),
		 * bigdecimal.doubleValue(), j);
		 * Debug.P(" 184 start setIBAProperty ibaValueView.getName() "
		 * + ibaValueView.toString());
		 * container.addAttributeValue(ibaValueView);
		 * Debug.P(" 185 start setIBAProperty ibaValueView.getName() "
		 * + ibaValueView.toString()); } else
		 * if(propertyValue.getClass().getName
		 * ().equalsIgnoreCase("java.lang.Integer") ||
		 * propertyValue.getClass().getName().equalsIgnoreCase("java.lang.Long")
		 * ) { // ���������ֵ��Integer�ͻ���Long�� Long long2 = new
		 * Long(propertyValue.toString()); long l1 = long2.longValue();
		 * IntegerValueDefaultView ibaValueView = null; ibaValueView = new
		 * IntegerValueDefaultView
		 * ((IntegerDefView)getDefaultViewObject(ibaDefNode), l1);
		 * container.addAttributeValue(ibaValueView); } else
		 * if(propertyValue.getClass
		 * ().getName().equalsIgnoreCase("java.lang.Boolean")) { //
		 * ���������ֵ��Boolean�� Boolean boolean2 = new
		 * Boolean(propertyValue.toString()); boolean flag1 =
		 * boolean2.booleanValue(); BooleanValueDefaultView ibaValueView = null;
		 * ibaValueView = new
		 * BooleanValueDefaultView((BooleanDefView)getDefaultViewObject
		 * (ibaDefNode), flag1); container.addAttributeValue(ibaValueView); }
		 * else { StringValueDefaultView ibaValueView = null; ibaValueView = new
		 * StringValueDefaultView
		 * ((StringDefView)getDefaultViewObject(ibaDefNode),
		 * propertyValue.toString()); container.addAttributeValue(ibaValueView);
		 * } } catch ( wt.iba.value.IBAValueException ibave) {
		 * ibave.printStackTrace(); } catch ( java.rmi.RemoteException re ) {
		 * re.printStackTrace(); } catch ( wt.util.WTException wte ) {
		 * wte.printStackTrace(); } catch ( IOException e ) {
		 * e.printStackTrace(); }
		 * 
		 * IBAValueDBService ibavaluedbservice = new IBAValueDBService(); //
		 * Object obj =
		 * ((DefaultAttributeContainer)container).getConstraintParameter(); //
		 * AttributeContainer attributecontainer1 =
		 * ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null,
		 * null); container =
		 * (DefaultAttributeContainer)ibavaluedbservice.updateAttributeContainer
		 * (ibaHolder, container != null ? container.getConstraintParameter() :
		 * null, null, null); ibaHolder.setAttributeContainer(container);
		 */
		// PersistenceHelper..manager.update((Persistable)ibaholder);
		try {
			// AttributeDelegateFactory attributedelegatefactory =
			// AttributeDelegateFactory.getInstance();
			// AttributeDelegate attributedelegate =
			// attributedelegatefactory.getDelegate(part, propertyName);
			// attributedelegate.setObjectAttributeValueString(part,
			// propertyName.toString(), propertyValue.toString(), locale, null);

			// IBAValueDBService ibavaluedbservice = new IBAValueDBService();
			// Object obj =
			// ((DefaultAttributeContainer)container).getConstraintParameter();
			// AttributeContainer attributecontainer1 =
			// ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null,
			// null);
			// container =
			// (DefaultAttributeContainer)ibavaluedbservice.updateAttributeContainer(ibaHolder,
			// container != null ? container.getConstraintParameter() : null,
			// null, null);
			// ibaHolder.setAttributeContainer(container);

			IBAValueDBService ibavaluedbservice = new IBAValueDBService();
			Object obj = defaultattributecontainer.getConstraintParameter();
			AttributeContainer attributecontainer1 = ibavaluedbservice
					.updateAttributeContainer(ibaholder, obj, null, null);
			ibaholder.setAttributeContainer(attributecontainer1);

			if (WorkInProgressHelper.isWorkingCopy((Workable) part))
				PersistenceHelper.manager.modify((Persistable) ibaholder);
//			else
//				PersistenceServerHelper.manager.update((Persistable) ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		try {
			part = (WTPart) PersistenceHelper.manager
					.refresh((Persistable) ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return part;
	}
	
	/**
	 * Description: ����IBA���� Created on 2005-06-08
	 * 
	 * @param propertyName
	 * @param propertyValue
	 * @param part
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException, RemoteExceptiontion return_type WTPart
	 */
	public static WTChangeIssue setIBAProperty(String propertyName,
			Object propertyValue, WTChangeIssue issue, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (issue == null)
			return issue;
		IBAHolder ibaholder = (IBAHolder) issue;
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();
		try {

			ibaholder = IBAValueHelper.service.refreshAttributeContainer(
					ibaholder, null, locale, null);
			defaultattributecontainer = (DefaultAttributeContainer) ibaholder
					.getAttributeContainer();
			Object obj = null;
			Object obj1 = null;
			String s = propertyName.toString();
			String s1 = propertyValue.toString();

			wt.iba.definition.litedefinition.AttributeDefDefaultView attributedefdefaultview = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(s);
			if (attributedefdefaultview == null) {
				Object aobj[] = { CLASSNAME };
				String s3 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "3", aobj)
						+ "'" + s + "'.";
				throw new WTException(s3);
			}
			wt.iba.value.litevalue.AbstractValueView aabstractvalueview[] = defaultattributecontainer
					.getAttributeValues(attributedefdefaultview);
			if (aabstractvalueview.length > 1) {
				Object aobj1[] = { CLASSNAME };
				String s4 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "0", aobj1)
						+ "(" + s + ").";
				throw new WTException(s4);
			}
			if (aabstractvalueview.length < 1) {
				Object obj2 = null;
				if (attributedefdefaultview instanceof IntegerDefView) {
					Long long2 = new Long(s1);
					long l1 = long2.longValue();
					obj2 = new IntegerValueDefaultView(
							(IntegerDefView) attributedefdefaultview, l1);
				}/* else if (attributedefdefaultview instanceof FloatDefView) {
					BigDecimal bigdecimal = new BigDecimal(s1);
					int j = NumericToolkit.countSigFigs(s1);
					obj2 = new FloatValueDefaultView(
							(FloatDefView) attributedefdefaultview, bigdecimal
									.doubleValue(), j);
				}*/
				else if (attributedefdefaultview instanceof FloatDefView) {
					BigDecimal bigdecimal = new BigDecimal(s1);
					
					int j = NumericToolkit.countSigFigs(s1);
					obj2 = new FloatValueDefaultView(
							(FloatDefView) attributedefdefaultview, Float.parseFloat(s1), j);
				}
				else if (attributedefdefaultview instanceof BooleanDefView) {
					Boolean boolean2 = new Boolean(s1);
					boolean flag1 = boolean2.booleanValue();
					obj2 = new BooleanValueDefaultView(
							(BooleanDefView) attributedefdefaultview, flag1);
				} else if (attributedefdefaultview instanceof StringDefView)
					obj2 = new StringValueDefaultView(
							(StringDefView) attributedefdefaultview, s1);
				else if (attributedefdefaultview instanceof UnitDefView) {
					StringTokenizer stringtokenizer2 = new StringTokenizer(s1,
							" ");
					String s7 = stringtokenizer2.nextToken();
					BigDecimal bigdecimal1 = new BigDecimal(s7);
					int k = NumericToolkit.countSigFigs(s7);
					String s10 = stringtokenizer2.nextToken();
					Unit unit1 = new Unit(bigdecimal1.doubleValue(), 14, s10);
					double d4 = unit1.getValue();
					obj2 = new UnitValueDefaultView(
							(UnitDefView) attributedefdefaultview, d4, k);
				} else if (attributedefdefaultview instanceof RatioDefView) {
					StringTokenizer stringtokenizer3 = new StringTokenizer(s1,
							":");
					Double double3 = new Double(stringtokenizer3.nextToken());
					Double double5 = new Double(stringtokenizer3.nextToken());
					double d2 = double3.doubleValue() / double5.doubleValue();
					obj2 = new RatioValueDefaultView(
							(RatioDefView) attributedefdefaultview, d2, double5
									.doubleValue());
				} else if (attributedefdefaultview instanceof TimestampDefView) {
					String s5 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper1 = new DateHelper(s1);
					Timestamp timestamp1 = new Timestamp(datehelper1.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp1, 3, locale, TimeZone
							.getTimeZone(s5));
					obj2 = new TimestampValueDefaultView(
							(TimestampDefView) attributedefdefaultview,
							timestamp1);
				} else if (attributedefdefaultview instanceof URLDefView) {
					obj2 = new URLValueDefaultView(
							(URLDefView) attributedefdefaultview, s1, "");
				} else {
					Object aobj3[] = { CLASSNAME };
					String s8 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj3)
							+ "'" + s + "'.";
					throw new WTException(s8);
				}
				defaultattributecontainer
						.addAttributeValue(((wt.iba.value.litevalue.AbstractValueView) (obj2)));
			} else {
				if (aabstractvalueview[0] instanceof IntegerValueDefaultView) {
					Long long1 = new Long(s1);
					long l = long1.longValue();
					((IntegerValueDefaultView) aabstractvalueview[0])
							.setValue(l);
				} else if (aabstractvalueview[0] instanceof FloatValueDefaultView) {
					Float float1 = new Float(s1);
					int i = NumericToolkit.countSigFigs(s1);
					double d = float1.doubleValue();
					((FloatValueDefaultView) aabstractvalueview[0]).setValue(d);
					((FloatValueDefaultView) aabstractvalueview[0])
							.setPrecision(i);
				} else if (aabstractvalueview[0] instanceof BooleanValueDefaultView) {
					Boolean boolean1 = new Boolean(s1);
					boolean flag = boolean1.booleanValue();
					((BooleanValueDefaultView) aabstractvalueview[0])
							.setValue(flag);
				} else if (aabstractvalueview[0] instanceof StringValueDefaultView)
					((StringValueDefaultView) aabstractvalueview[0])
							.setValue(s1);
				else if (aabstractvalueview[0] instanceof UnitValueDefaultView) {
					StringTokenizer stringtokenizer = new StringTokenizer(s1,
							" ");
					Double double1 = new Double(stringtokenizer.nextToken());
					String s9 = stringtokenizer.nextToken();
					Unit unit = new Unit(double1.doubleValue(), 14, s9);
					double d3 = unit.getValue();
					((UnitValueDefaultView) aabstractvalueview[0]).setValue(d3);
				} else if (aabstractvalueview[0] instanceof RatioValueDefaultView) {
					StringTokenizer stringtokenizer1 = new StringTokenizer(s1,
							":");
					Double double2 = new Double(stringtokenizer1.nextToken());
					Double double4 = new Double(stringtokenizer1.nextToken());
					double d1 = double2.doubleValue() / double4.doubleValue();
					((RatioValueDefaultView) aabstractvalueview[0])
							.setValue(d1);
					((RatioValueDefaultView) aabstractvalueview[0])
							.setDenominator(double4.doubleValue());
				} else if (aabstractvalueview[0] instanceof TimestampValueDefaultView) {
					String s2 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper = new DateHelper(s1);
					Timestamp timestamp = new Timestamp(datehelper.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp, 3, locale, TimeZone
							.getTimeZone(s2));
					((TimestampValueDefaultView) aabstractvalueview[0])
							.setValue(timestamp);
				} else if (aabstractvalueview[0] instanceof URLValueDefaultView) {
					((URLValueDefaultView) aabstractvalueview[0]).setValue(s1);
				} else {
					Object aobj2[] = { CLASSNAME };
					String s6 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj2)
							+ "'" + s + "'.";
					throw new WTException(s6);
				}
				defaultattributecontainer
						.updateAttributeValue(aabstractvalueview[0]);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new WTException(exception);
		}
		try {
			// AttributeDelegateFactory attributedelegatefactory =
			// AttributeDelegateFactory.getInstance();
			// AttributeDelegate attributedelegate =
			// attributedelegatefactory.getDelegate(part, propertyName);
			// attributedelegate.setObjectAttributeValueString(part,
			// propertyName.toString(), propertyValue.toString(), locale, null);

			// IBAValueDBService ibavaluedbservice = new IBAValueDBService();
			// Object obj =
			// ((DefaultAttributeContainer)container).getConstraintParameter();
			// AttributeContainer attributecontainer1 =
			// ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null,
			// null);
			// container =
			// (DefaultAttributeContainer)ibavaluedbservice.updateAttributeContainer(ibaHolder,
			// container != null ? container.getConstraintParameter() : null,
			// null, null);
			// ibaHolder.setAttributeContainer(container);

			IBAValueDBService ibavaluedbservice = new IBAValueDBService();
			Object obj = defaultattributecontainer.getConstraintParameter();
			AttributeContainer attributecontainer1 = ibavaluedbservice
					.updateAttributeContainer(ibaholder, obj, null, null);
			ibaholder.setAttributeContainer(attributecontainer1);

		//	if (WorkInProgressHelper.isWorkingCopy((Workable) issue))
		//		PersistenceHelper.manager.modify((Persistable) ibaholder);
//			else
			PersistenceServerHelper.manager.update((Persistable) ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		try {
			issue = (WTChangeIssue) PersistenceHelper.manager
					.refresh((Persistable) ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return issue;
	}
		


	public static WTDocument setIBAProperty(String propertyName,
			Object propertyValue, WTDocument doc, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (doc == null)
			return doc;
		IBAHolder ibaholder = (IBAHolder) doc;
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();
		try {

			ibaholder = IBAValueHelper.service.refreshAttributeContainer(
					ibaholder, null, locale, null);
			defaultattributecontainer = (DefaultAttributeContainer) ibaholder
					.getAttributeContainer();
			Object obj = null;
			Object obj1 = null;
			String s = propertyName.toString();
			String s1 = propertyValue.toString();

			wt.iba.definition.litedefinition.AttributeDefDefaultView attributedefdefaultview = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(s);
			if (attributedefdefaultview == null) {
				Object aobj[] = { CLASSNAME };
				String s3 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "3", aobj)
						+ "'" + s + "'.";
				throw new WTException(s3);
			}
			wt.iba.value.litevalue.AbstractValueView aabstractvalueview[] = defaultattributecontainer
					.getAttributeValues(attributedefdefaultview);
			if (aabstractvalueview.length > 1) {
				Object aobj1[] = { CLASSNAME };
				String s4 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "0", aobj1)
						+ "(" + s + ").";
				throw new WTException(s4);
			}
			if (aabstractvalueview.length < 1) {
				Object obj2 = null;
				if (attributedefdefaultview instanceof IntegerDefView) {
					Long long2 = new Long(s1);
					long l1 = long2.longValue();
					obj2 = new IntegerValueDefaultView(
							(IntegerDefView) attributedefdefaultview, l1);
				} else if (attributedefdefaultview instanceof FloatDefView) {
					BigDecimal bigdecimal = new BigDecimal(s1);
					int j = NumericToolkit.countSigFigs(s1);
					obj2 = new FloatValueDefaultView(
							(FloatDefView) attributedefdefaultview, bigdecimal
									.doubleValue(), j);
				} else if (attributedefdefaultview instanceof BooleanDefView) {
					Boolean boolean2 = new Boolean(s1);
					boolean flag1 = boolean2.booleanValue();
					obj2 = new BooleanValueDefaultView(
							(BooleanDefView) attributedefdefaultview, flag1);
				} else if (attributedefdefaultview instanceof StringDefView)
					obj2 = new StringValueDefaultView(
							(StringDefView) attributedefdefaultview, s1);
				else if (attributedefdefaultview instanceof UnitDefView) {
					StringTokenizer stringtokenizer2 = new StringTokenizer(s1,
							" ");
					String s7 = stringtokenizer2.nextToken();
					BigDecimal bigdecimal1 = new BigDecimal(s7);
					int k = NumericToolkit.countSigFigs(s7);
					String s10 = stringtokenizer2.nextToken();
					Unit unit1 = new Unit(bigdecimal1.doubleValue(), 14, s10);
					double d4 = unit1.getValue();
					obj2 = new UnitValueDefaultView(
							(UnitDefView) attributedefdefaultview, d4, k);
				} else if (attributedefdefaultview instanceof RatioDefView) {
					StringTokenizer stringtokenizer3 = new StringTokenizer(s1,
							":");
					Double double3 = new Double(stringtokenizer3.nextToken());
					Double double5 = new Double(stringtokenizer3.nextToken());
					double d2 = double3.doubleValue() / double5.doubleValue();
					obj2 = new RatioValueDefaultView(
							(RatioDefView) attributedefdefaultview, d2, double5
									.doubleValue());
				} else if (attributedefdefaultview instanceof TimestampDefView) {
					String s5 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper1 = new DateHelper(s1);
					Timestamp timestamp1 = new Timestamp(datehelper1.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp1, 3, locale, TimeZone
							.getTimeZone(s5));
					obj2 = new TimestampValueDefaultView(
							(TimestampDefView) attributedefdefaultview,
							timestamp1);
				} else if (attributedefdefaultview instanceof URLDefView) {
					obj2 = new URLValueDefaultView(
							(URLDefView) attributedefdefaultview, s1, "");
				} else {
					Object aobj3[] = { CLASSNAME };
					String s8 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj3)
							+ "'" + s + "'.";
					throw new WTException(s8);
				}
				defaultattributecontainer
						.addAttributeValue(((wt.iba.value.litevalue.AbstractValueView) (obj2)));
			} else {
				if (aabstractvalueview[0] instanceof IntegerValueDefaultView) {
					Long long1 = new Long(s1);
					long l = long1.longValue();
					((IntegerValueDefaultView) aabstractvalueview[0])
							.setValue(l);
				} else if (aabstractvalueview[0] instanceof FloatValueDefaultView) {
					Float float1 = new Float(s1);
					int i = NumericToolkit.countSigFigs(s1);
					double d = float1.doubleValue();
					((FloatValueDefaultView) aabstractvalueview[0]).setValue(d);
					((FloatValueDefaultView) aabstractvalueview[0])
							.setPrecision(i);
				} else if (aabstractvalueview[0] instanceof BooleanValueDefaultView) {
					Boolean boolean1 = new Boolean(s1);
					boolean flag = boolean1.booleanValue();
					((BooleanValueDefaultView) aabstractvalueview[0])
							.setValue(flag);
				} else if (aabstractvalueview[0] instanceof StringValueDefaultView)
					((StringValueDefaultView) aabstractvalueview[0])
							.setValue(s1);
				else if (aabstractvalueview[0] instanceof UnitValueDefaultView) {
					StringTokenizer stringtokenizer = new StringTokenizer(s1,
							" ");
					Double double1 = new Double(stringtokenizer.nextToken());
					String s9 = stringtokenizer.nextToken();
					Unit unit = new Unit(double1.doubleValue(), 14, s9);
					double d3 = unit.getValue();
					((UnitValueDefaultView) aabstractvalueview[0]).setValue(d3);
				} else if (aabstractvalueview[0] instanceof RatioValueDefaultView) {
					StringTokenizer stringtokenizer1 = new StringTokenizer(s1,
							":");
					Double double2 = new Double(stringtokenizer1.nextToken());
					Double double4 = new Double(stringtokenizer1.nextToken());
					double d1 = double2.doubleValue() / double4.doubleValue();
					((RatioValueDefaultView) aabstractvalueview[0])
							.setValue(d1);
					((RatioValueDefaultView) aabstractvalueview[0])
							.setDenominator(double4.doubleValue());
				} else if (aabstractvalueview[0] instanceof TimestampValueDefaultView) {
					String s2 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper = new DateHelper(s1);
					Timestamp timestamp = new Timestamp(datehelper.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp, 3, locale, TimeZone
							.getTimeZone(s2));
					((TimestampValueDefaultView) aabstractvalueview[0])
							.setValue(timestamp);
				} else if (aabstractvalueview[0] instanceof URLValueDefaultView) {
					((URLValueDefaultView) aabstractvalueview[0]).setValue(s1);
				} else {
					Object aobj2[] = { CLASSNAME };
					String s6 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj2)
							+ "'" + s + "'.";
					throw new WTException(s6);
				}
				defaultattributecontainer
						.updateAttributeValue(aabstractvalueview[0]);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new WTException(exception);
		}
		
		
	try {
		
			IBAValueDBService ibavaluedbservice = new IBAValueDBService();
			Object obj = defaultattributecontainer.getConstraintParameter();
			AttributeContainer attributecontainer1 = ibavaluedbservice
					.updateAttributeContainer(ibaholder, obj, null, null);
			ibaholder.setAttributeContainer(attributecontainer1);

			if (WorkInProgressHelper.isWorkingCopy((Workable) doc))
				PersistenceHelper.manager.modify((Persistable) ibaholder);
			else
				PersistenceServerHelper.manager.update((Persistable) ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		try {
			doc = (WTDocument) PersistenceHelper.manager
					.refresh((Persistable) ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return doc;
	}


	public static WTPartUsageLink setIBAProperty(String propertyName,
			Object propertyValue, WTPartUsageLink link, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (link == null)
			return link;
		IBAHolder ibaholder = (IBAHolder) link;
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
				.getAttributeContainer();
		try {

			ibaholder = IBAValueHelper.service.refreshAttributeContainer(
					ibaholder, null, locale, null);
			defaultattributecontainer = (DefaultAttributeContainer) ibaholder
					.getAttributeContainer();
			Object obj = null;
			Object obj1 = null;
			String s = propertyName.toString();
			String s1 = propertyValue.toString();

			wt.iba.definition.litedefinition.AttributeDefDefaultView attributedefdefaultview = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(s);
			if (attributedefdefaultview == null) {
				Object aobj[] = { CLASSNAME };
				String s3 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "3", aobj)
						+ "'" + s + "'.";
				throw new WTException(s3);
			}
			wt.iba.value.litevalue.AbstractValueView aabstractvalueview[] = defaultattributecontainer
					.getAttributeValues(attributedefdefaultview);
			if (aabstractvalueview.length > 1) {
				Object aobj1[] = { CLASSNAME };
				String s4 = WTMessage.getLocalizedMessage(
						"wt.adapter.iba.ibaResource", "0", aobj1)
						+ "(" + s + ").";
				throw new WTException(s4);
			}
			if (aabstractvalueview.length < 1) {
				Object obj2 = null;
				if (attributedefdefaultview instanceof IntegerDefView) {
					Long long2 = new Long(s1);
					long l1 = long2.longValue();
					obj2 = new IntegerValueDefaultView(
							(IntegerDefView) attributedefdefaultview, l1);
				} else if (attributedefdefaultview instanceof FloatDefView) {
					BigDecimal bigdecimal = new BigDecimal(s1);
					int j = NumericToolkit.countSigFigs(s1);
					obj2 = new FloatValueDefaultView(
							(FloatDefView) attributedefdefaultview, bigdecimal
									.doubleValue(), j);
				} else if (attributedefdefaultview instanceof BooleanDefView) {
					Boolean boolean2 = new Boolean(s1);
					boolean flag1 = boolean2.booleanValue();
					obj2 = new BooleanValueDefaultView(
							(BooleanDefView) attributedefdefaultview, flag1);
				} else if (attributedefdefaultview instanceof StringDefView)
					obj2 = new StringValueDefaultView(
							(StringDefView) attributedefdefaultview, s1);
				else if (attributedefdefaultview instanceof UnitDefView) {
					StringTokenizer stringtokenizer2 = new StringTokenizer(s1,
							" ");
					String s7 = stringtokenizer2.nextToken();
					BigDecimal bigdecimal1 = new BigDecimal(s7);
					int k = NumericToolkit.countSigFigs(s7);
					String s10 = stringtokenizer2.nextToken();
					Unit unit1 = new Unit(bigdecimal1.doubleValue(), 14, s10);
					double d4 = unit1.getValue();
					obj2 = new UnitValueDefaultView(
							(UnitDefView) attributedefdefaultview, d4, k);
				} else if (attributedefdefaultview instanceof RatioDefView) {
					StringTokenizer stringtokenizer3 = new StringTokenizer(s1,
							":");
					Double double3 = new Double(stringtokenizer3.nextToken());
					Double double5 = new Double(stringtokenizer3.nextToken());
					double d2 = double3.doubleValue() / double5.doubleValue();
					obj2 = new RatioValueDefaultView(
							(RatioDefView) attributedefdefaultview, d2, double5
									.doubleValue());
				} else if (attributedefdefaultview instanceof TimestampDefView) {
					String s5 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper1 = new DateHelper(s1);
					Timestamp timestamp1 = new Timestamp(datehelper1.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp1, 3, locale, TimeZone
							.getTimeZone(s5));
					obj2 = new TimestampValueDefaultView(
							(TimestampDefView) attributedefdefaultview,
							timestamp1);
				} else if (attributedefdefaultview instanceof URLDefView) {
					obj2 = new URLValueDefaultView(
							(URLDefView) attributedefdefaultview, s1, "");
				} else {
					Object aobj3[] = { CLASSNAME };
					String s8 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj3)
							+ "'" + s + "'.";
					throw new WTException(s8);
				}
				defaultattributecontainer
						.addAttributeValue(((wt.iba.value.litevalue.AbstractValueView) (obj2)));
			} else {
				if (aabstractvalueview[0] instanceof IntegerValueDefaultView) {
					Long long1 = new Long(s1);
					long l = long1.longValue();
					((IntegerValueDefaultView) aabstractvalueview[0])
							.setValue(l);
				} else if (aabstractvalueview[0] instanceof FloatValueDefaultView) {
					Float float1 = new Float(s1);
					int i = NumericToolkit.countSigFigs(s1);
					double d = float1.doubleValue();
					((FloatValueDefaultView) aabstractvalueview[0]).setValue(d);
					((FloatValueDefaultView) aabstractvalueview[0])
							.setPrecision(i);
				} else if (aabstractvalueview[0] instanceof BooleanValueDefaultView) {
					Boolean boolean1 = new Boolean(s1);
					boolean flag = boolean1.booleanValue();
					((BooleanValueDefaultView) aabstractvalueview[0])
							.setValue(flag);
				} else if (aabstractvalueview[0] instanceof StringValueDefaultView)
					((StringValueDefaultView) aabstractvalueview[0])
							.setValue(s1);
				else if (aabstractvalueview[0] instanceof UnitValueDefaultView) {
					StringTokenizer stringtokenizer = new StringTokenizer(s1,
							" ");
					Double double1 = new Double(stringtokenizer.nextToken());
					String s9 = stringtokenizer.nextToken();
					Unit unit = new Unit(double1.doubleValue(), 14, s9);
					double d3 = unit.getValue();
					((UnitValueDefaultView) aabstractvalueview[0]).setValue(d3);
				} else if (aabstractvalueview[0] instanceof RatioValueDefaultView) {
					StringTokenizer stringtokenizer1 = new StringTokenizer(s1,
							":");
					Double double2 = new Double(stringtokenizer1.nextToken());
					Double double4 = new Double(stringtokenizer1.nextToken());
					double d1 = double2.doubleValue() / double4.doubleValue();
					((RatioValueDefaultView) aabstractvalueview[0])
							.setValue(d1);
					((RatioValueDefaultView) aabstractvalueview[0])
							.setDenominator(double4.doubleValue());
				} else if (aabstractvalueview[0] instanceof TimestampValueDefaultView) {
					String s2 = WTProperties.getLocalProperties().getProperty(
							"wt.method.timezone", "GMT");
					DateHelper datehelper = new DateHelper(s1);
					Timestamp timestamp = new Timestamp(datehelper.getDate()
							.getTime());
					WTStandardDateFormat.format(timestamp, 3, locale, TimeZone
							.getTimeZone(s2));
					((TimestampValueDefaultView) aabstractvalueview[0])
							.setValue(timestamp);
				} else if (aabstractvalueview[0] instanceof URLValueDefaultView) {
					((URLValueDefaultView) aabstractvalueview[0]).setValue(s1);
				} else {
					Object aobj2[] = { CLASSNAME };
					String s6 = WTMessage.getLocalizedMessage(
							"wt.adapter.iba.ibaResource", "1", aobj2)
							+ "'" + s + "'.";
					throw new WTException(s6);
				}
				defaultattributecontainer
						.updateAttributeValue(aabstractvalueview[0]);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			throw new WTException(exception);
		}
		
	try {
	
			IBAValueDBService ibavaluedbservice = new IBAValueDBService();
			Object obj = defaultattributecontainer.getConstraintParameter();
			AttributeContainer attributecontainer1 = ibavaluedbservice
					.updateAttributeContainer(ibaholder, obj, null, null);
			ibaholder.setAttributeContainer(attributecontainer1);

		
			PersistenceServerHelper.manager.update((Persistable) ibaholder);
			
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		try {
			link = (WTPartUsageLink) PersistenceHelper.manager
					.refresh((Persistable) ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return link;
	}
	
	
	

	/**
	 * Description: �����ĵ���IBA���� Created on 2005-09-05
	 * 
	 * @param propertyName
	 * @param propertyValue
	 * @param doc
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type WTDocument
	 */
/*	public static WTDocument setIBAProperty(String propertyName,
			Object propertyValue, WTDocument doc, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (doc == null)
			return doc;
		return (WTDocument) setIBAProperty(propertyName, propertyValue,
				(WTObject) doc, locale);
	}
*/
	/**
	 * Description: ����EPMDocument��IBA���� Created on 2005-09-05
	 * 
	 * @param propertyName
	 * @param propertyValue
	 * @param
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type WTDocument
	 */
	public static EPMDocument setIBAProperty(String propertyName,
			Object propertyValue, EPMDocument epmDoc, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (epmDoc == null)
			return epmDoc;
		return (EPMDocument) setIBAProperty(propertyName, propertyValue,
				(WTObject) epmDoc, locale);
	}

	/**
	 * Description: ���ö����IBA���� Created on 2005-09-05
	 * 
	 * @param propertyName
	 * @param propertyValue
	 * @param
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type WTDocument
	 */
	public static WTObject setIBAProperty(String propertyName,
			Object propertyValue, WTObject wto, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (wto == null)
			return wto;

		if (!(wto instanceof IBAHolder)) {
			return wto;
		}

		IBAHolder ibaHolder = (IBAHolder) wto;
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// ���IBA����
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;

		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder
				.getAttributeContainer());

		try {
			// ������֯������WTObject������
			if (!(wto instanceof OrganizationOwned)) {
				// ֻ��OrganizationOwned�����ܻ����֯������
				return wto;
			}

			// ������֯����
			WTProperties wtproperties = WTProperties.getLocalProperties();
			String organizationName = wtproperties
					.getProperty("property.orgcontainer");
			if (organizationName != null)
				organizationName = organizationName.trim();
			else
				organizationName = ((OrganizationOwned) wto)
						.getOrganizationName();

			ibaDefNode = getAttributeOrganizer(organizationName);
			ibaDefNodes = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);

			// �õ����propertyName��Ӧ�Ľڵ�
			ibaDefNode = getNode(ibaDefNodes, propertyName);
			
			System.out.println("ibaDefNode  "+ibaDefNode);
			
			container.deleteAttributeValues(getDefaultViewObject(ibaDefNode));

			if (propertyValue.getClass().getName().equalsIgnoreCase(
					"java.lang.Double")
					|| propertyValue.getClass().getName().equalsIgnoreCase(
							"java.lang.Float")) {
				// ���������ֵ��Double�ͻ���Float��
				BigDecimal bigdecimal = new BigDecimal(propertyValue.toString());
				System.out
						.println(" 181 start setIBAProperty bigdecimal.toString() "
								+ bigdecimal.toString());
				int j = NumericToolkit.countSigFigs(propertyValue.toString());
//				Debug.P(" 182 start setIBAProperty j " + j);
				FloatValueDefaultView ibaValueView = null;
				System.out
						.println(" 183 start setIBAProperty propertyValue.toString() "
								+ propertyValue.toString());
				ibaValueView = new FloatValueDefaultView(
						(FloatDefView) getDefaultViewObject(ibaDefNode),
						bigdecimal.doubleValue(), j);
				System.out
						.println(" 184 start setIBAProperty ibaValueView.getName() "
								+ ibaValueView.toString());
				container.addAttributeValue(ibaValueView);
				System.out
						.println(" 185 start setIBAProperty ibaValueView.getName() "
								+ ibaValueView.toString());
			} else if (propertyValue.getClass().getName().equalsIgnoreCase(
					"java.lang.Integer")
					|| propertyValue.getClass().getName().equalsIgnoreCase(
							"java.lang.Long")) {
				// ���������ֵ��Integer�ͻ���Long��
				Long long2 = new Long(propertyValue.toString());
				long l1 = long2.longValue();
				IntegerValueDefaultView ibaValueView = null;
				ibaValueView = new IntegerValueDefaultView(
						(IntegerDefView) getDefaultViewObject(ibaDefNode), l1);
				container.addAttributeValue(ibaValueView);
			} else if (propertyValue.getClass().getName().equalsIgnoreCase(
					"java.lang.Boolean")) {
				// ���������ֵ��Boolean��
				Boolean boolean2 = new Boolean(propertyValue.toString());
				boolean flag1 = boolean2.booleanValue();
				BooleanValueDefaultView ibaValueView = null;
				ibaValueView = new BooleanValueDefaultView(
						(BooleanDefView) getDefaultViewObject(ibaDefNode),
						flag1);
				container.addAttributeValue(ibaValueView);
			} else {
				StringValueDefaultView ibaValueView = null;
				ibaValueView = new StringValueDefaultView(
						(StringDefView) getDefaultViewObject(ibaDefNode),
						propertyValue.toString());
				container.addAttributeValue(ibaValueView);
			}
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		// Object obj =
		// ((DefaultAttributeContainer)container).getConstraintParameter();
		// AttributeContainer attributecontainer1 =
		// ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null,
		// null);
		container = (DefaultAttributeContainer) ibavaluedbservice
				.updateAttributeContainer(ibaHolder,
						container != null ? container.getConstraintParameter()
								: null, null, null);
		ibaHolder.setAttributeContainer(container);

		try {
			if (WorkInProgressHelper.isWorkingCopy((Workable) wto))
				PersistenceHelper.manager.modify((Persistable) ibaHolder);
			else
				PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		} catch (Exception e) {

		}

		try {
			ibaHolder = (IBAHolder) PersistenceHelper.manager
					.refresh((Persistable) ibaHolder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return (WTObject) ibaHolder;
	}

	/**
	 * Description: ����IBA���� Created on 2005-06-08
	 * 
	 * @param propertyName
	 * @param propertyValue
	 * @param numberStr
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type WTPart
	 */
	public static WTPart setIBAProperty(String propertyName,
			Object propertyValue, String numberStr, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (StringUtil.nullOrBlank(numberStr))
			return null;

		WTPart part = null;

		try {
			part = getLatestIterationPart(numberStr);
			part = setIBAProperty(propertyName, propertyValue, part, locale);

		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		}

		return part;
	}

	
	
	
	/**
	 * Description: ��ݸ�ı�Ų������°汾��WTPart Created on 2005-06-08
	 * 
	 * @param partNumber
	 * @throws
	 * @return WTPart
	 */
	public static WTPart getLatestIterationPart(String partNumber) {
		WTPart part = null;
		/*
		 * try { QuerySpec qs = new QuerySpec(wt.part.WTPart.class);
		 * SearchCondition temp = new SearchCondition(wt.part.WTPart.class,
		 * wt.part.WTPart.NUMBER, SearchCondition.EQUAL,
		 * partNumber.toUpperCase()); qs.appendSearchCondition(temp);
		 * qs.appendAnd(); SearchCondition latest =
		 * VersionControlHelper.getSearchCondition(wt.part. WTPart.class, true);
		 * qs.appendSearchCondition(latest); QueryResult qr =
		 * PersistenceHelper.manager.find(qs); if (qr.hasMoreElements()) { part
		 * = (WTPart)qr.nextElement(); }
		 * 
		 * if(part != null) { qr =
		 * VersionControlHelper.service.allIterationsOf(part.getMaster()) ;
		 * 
		 * if (qr.hasMoreElements()) { part = (WTPart)qr.nextElement(); } } }
		 * catch(Exception e) { e.printStackTrace(); } finally { if(part ==
		 * null) { part = (WTPart)getLatestIterationProduct(partNumber); } }
		 */
		return part;
	}

	/**
	 * Description: ��ݸ�ı�Ų������°汾��WTProduct Created on productNumber
	 * 
	 * @param productNumber
	 * @throws
	 * @return WTProduct
	 */
	public static WTProductInstance2 getLatestIterationProduct(
			String productNumber) {
		WTProductInstance2 product = null;
		/*
		 * try { QuerySpec qs = new QuerySpec(wt.part.WTProductInstance2.class);
		 * SearchCondition temp = new
		 * SearchCondition(wt.part.WTProductInstance2.class,
		 * wt.part.WTProductInstance2.NUMBER, SearchCondition.EQUAL,
		 * productNumber.toUpperCase()); qs.appendSearchCondition(temp);
		 * qs.appendAnd(); SearchCondition latest =
		 * VersionControlHelper.getSearchCondition(wt.part.
		 * WTProductInstance2.class, true); qs.appendSearchCondition(latest);
		 * QueryResult qr = PersistenceHelper.manager.find(qs); if
		 * (qr.hasMoreElements()) { product =
		 * (WTProductInstance2)qr.nextElement(); }
		 * 
		 * if(product != null) { qr =
		 * VersionControlHelper.service.allIterationsOf(product.getMaster()) ;
		 * 
		 * if (qr.hasMoreElements()) { product =
		 * (WTProductInstance2)qr.nextElement(); } }
		 * 
		 * qs = null; qr = null; } catch(Exception e) { e.printStackTrace(); }
		 */
		return product;
	}

	/**
	 * Description: ɾ�� WTPart ��IBA���� Created on 2005-09-22
	 * 
	 * @param propertyName
	 * @param part
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type WTPart
	 */
	public static WTPart deleteIBAProperty(String propertyName, WTPart part,
			Locale locale) throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (part == null)
			return part;
		return (WTPart) deleteIBAProperty(propertyName, (WTObject) part, locale);
	}

	/**
	 * Description: ɾ�� WTDocument ��IBA���� Created on 2005-09-22
	 * 
	 * @param propertyName
	 * @param doc
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type WTDocument
	 */
	public static WTDocument deleteIBAProperty(String propertyName,
			WTDocument doc, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (doc == null)
			return doc;
		return (WTDocument) deleteIBAProperty(propertyName, (WTObject) doc,
				locale);
	}

	/**
	 * Description: ɾ��EPMDocument��IBA���� Created on 2005-09-22
	 * 
	 * @param propertyName
	 * @param epmDoc
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type EPMDocument
	 */
	public static EPMDocument deleteIBAProperty(String propertyName,
			EPMDocument epmDoc, Locale locale)
			throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (epmDoc == null)
			return epmDoc;
		return (EPMDocument) deleteIBAProperty(propertyName, (WTObject) epmDoc,
				locale);
	}

	/**
	 * Description: ɾ������IBA���� Created on 2005-09-22
	 * 
	 * @param propertyName
	 * @param wto
	 * @param locale
	 * @throws wt.introspection.WTIntrospectionException
	 *             , wt.util.WTException,
	 *             RemoteExceptiontion,IllegalAccessException
	 *             ,ClassNotFoundException,SQLException return_type WTObject
	 */
	public static WTObject deleteIBAProperty(String propertyName, WTObject wto,
			Locale locale) throws wt.introspection.WTIntrospectionException,
			wt.util.WTException, RemoteException {
		if (wto == null)
			return wto;

		if (!(wto instanceof IBAHolder)) {
			return wto;
		}

		IBAHolder ibaHolder = (IBAHolder) wto;
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);

		if (ibaHolder.getAttributeContainer() == null) {
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (wt.util.WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (java.rmi.RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}

		// ���IBA����
		AbstractAttributeDefinizerNodeView ibaDefNode, ibaDefNode1, ibaDefNode2;
		AbstractAttributeDefinizerNodeView ibaDefNodes[];
		AttributeDefDefaultView ibaDefView = null;

		AbstractValueView ibaValueViews[];
		DefaultAttributeContainer container = (DefaultAttributeContainer) (ibaHolder
				.getAttributeContainer());

		try {
			// ������֯������WTObject������
			if (!(wto instanceof OrganizationOwned)) {
				// ֻ��OrganizationOwned�����ܻ����֯������
				return wto;
			}

			// ������֯����
			WTProperties wtproperties = WTProperties.getLocalProperties();
			String organizationName = wtproperties
					.getProperty("property.orgcontainer");
			if (organizationName != null)
				organizationName = organizationName.trim();
			else
				organizationName = ((OrganizationOwned) wto)
						.getOrganizationName();

			ibaDefNode = getAttributeOrganizer(organizationName);
			ibaDefNodes = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);

			if (ibaDefNodes != null) {
				for (int i = 0; i < ibaDefNodes.length; i++) {
					if (ibaDefNodes[i] == null)
						continue;

					if (ibaDefNodes[i].getName().equalsIgnoreCase(propertyName))
						continue;

					// ֻҪ�������а�propertyName
					ibaDefNode = ibaDefNodes[i];
					container
							.deleteAttributeValues(getDefaultViewObject(ibaDefNode));
				}
			}
		} catch (wt.iba.value.IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (java.rmi.RemoteException re) {
			re.printStackTrace();
		} catch (wt.util.WTException wte) {
			wte.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		// Object obj =
		// ((DefaultAttributeContainer)container).getConstraintParameter();
		// AttributeContainer attributecontainer1 =
		// ibavaluedbservice.updateAttributeContainer(ibaHolder, obj, null,
		// null);
		container = (DefaultAttributeContainer) ibavaluedbservice
				.updateAttributeContainer(ibaHolder,
						container != null ? container.getConstraintParameter()
								: null, null, null);
		ibaHolder.setAttributeContainer(container);
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);

		return (WTObject) ibaHolder;
	}

	public static AttributeDefDefaultView getDefaultViewObject(Object obj) {
		AttributeDefDefaultView attributedefdefaultview = null;
		if (obj == null)
			return null;

		try {
			if (obj instanceof AttributeDefNodeView)
				attributedefdefaultview = IBADefinitionHelper.service
						.getAttributeDefDefaultView((AttributeDefNodeView) obj);
		} catch (java.rmi.RemoteException remoteexception) {
			remoteexception.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}

		return attributedefdefaultview;
	}

	public static AbstractAttributeDefinizerNodeView getNode(
			AbstractAttributeDefinizerNodeView[] nodes, String s) {
		int i;

		if (nodes == null || nodes.length == 0 || StringUtil.nullOrBlank(s))
			return null;

		for (i = 0; i < nodes.length; i++) {
			if (nodes[i] == null)
				continue;

			if (nodes[i].getName().equalsIgnoreCase(s))
				return nodes[i];
		}

		return null;
	}

	/**
	 * Description: ��ݸ��������ƣ��õ����Խڵ���ͼ Created on 2005-06-08
	 * 
	 * @param
	 * @param
	 * @throws WTException
	 *             , WTPropertyVetoException, RemoteException,
	 *             IOException,InstantiationException
	 *             ,IllegalAccessException,ClassNotFoundException,SQLException
	 *             return_type AttributeOrgNodeView
	 */
	public static AttributeOrgNodeView getAttributeOrganizer(String s) {
		if (StringUtil.nullOrBlank(s))
			return null;

		int i;
		AttributeOrgNodeView attributeorgnodeview[] = null;
		try {
			attributeorgnodeview = IBADefinitionHelper.service
					.getAttributeOrganizerRoots();

			if (attributeorgnodeview == null
					|| attributeorgnodeview.length == 0)
				return null;

			for (i = 0; i < attributeorgnodeview.length; i++) {
				if (attributeorgnodeview[i] == null)
					continue;

				if (attributeorgnodeview[i].getName().equalsIgnoreCase(s))
					return attributeorgnodeview[i];
			}

			// ���û���ҵ�����֯����ͬ�ģ����ص�һ��
			return attributeorgnodeview[0];
		} catch (java.rmi.RemoteException remoteexception) {
			remoteexception.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}

		return null;
	}

	public static IBAHolder setRepeatStringIBAProperty(String propertyName,
			String value, IBAHolder ibaHolder) throws WTIntrospectionException,
			WTException, RemoteException {
		if (VERBOSE)
			System.out
					.println("*** in setRepeatStringIBAProperty, propertyName = "
							+ propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (ibaHolder == null)
			return ibaHolder;
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);
		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out
						.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out
								.println("*** AttributeContainer is still null.");
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		DefaultAttributeContainer container = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		try {
//			if (VERBOSE)
//				Debug.P("**** Begin set IBA ...");
			AbstractAttributeDefinizerNodeView ibaDefNode = getAttributeOrganizer(attrorg);
			AbstractAttributeDefinizerNodeView ibaDefNodes[] = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);
			ibaDefView = getDefaultViewObject(ibaDefNode);
			ibaValueView = new StringValueDefaultView(
					(StringDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
//			if (VERBOSE)
//				Debug.P("*** fill " + propertyName + " completed.");
		} catch (IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		wt.iba.value.AttributeContainer attributecontainer1 = ibavaluedbservice
				.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);
//		if (VERBOSE)
//			Debug.P("*** exit addIBA ...");
		return ibaHolder;
	}

	private AbstractAttributeDefinizerNodeView getNodeChild(
			AbstractAttributeDefinizerNodeView node, String s) {
		AbstractAttributeDefinizerNodeView abstractattributedefinizernodeview = null;
		AbstractAttributeDefinizerNodeView aabstractattributedefinizernodeview[] = null;
		if (!(node instanceof AbstractAttributeDefinizerNodeView) || s == null
				|| s.length() == 0)
			return null;
		try {
			aabstractattributedefinizernodeview = IBADefinitionHelper.service
					.getAttributeChildren(node);
		} catch (RemoteException remoteexception) {
			remoteexception.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		if (aabstractattributedefinizernodeview == null)
			return null;
		for (int i = 0; i < aabstractattributedefinizernodeview.length; i++)
			if (aabstractattributedefinizernodeview[i] != null
					&& aabstractattributedefinizernodeview[i].getName()
							.equalsIgnoreCase(s))
				return aabstractattributedefinizernodeview[i];

		return null;
	}

	public static IBAHolder removeStringIBAProperty(String propertyName,
			IBAHolder ibaHolder) throws WTIntrospectionException, WTException,
			RemoteException {
		if (VERBOSE)
			System.out
					.println("*** in removeStringIBAProperty, propertyName = "
							+ propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);
		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out
						.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out
								.println("*** AttributeContainer is still null.");
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		DefaultAttributeContainer container = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		try {
//			if (VERBOSE)
//				Debug.P("**** Begin remove IBA ...");
			AbstractAttributeDefinizerNodeView ibaDefNode = getAttributeOrganizer(attrorg);
			AbstractAttributeDefinizerNodeView ibaDefNodes[] = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);
			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
		} catch (IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		wt.iba.value.AttributeContainer attributecontainer1 = ibavaluedbservice
				.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);
//		if (VERBOSE)
//			Debug.P("*** exit remove ... ");
		return ibaHolder;
	}

	public WTPart setGeneralStringIBAProperty(String propertyName,
			String value, WTPart part) throws WTIntrospectionException,
			WTException, RemoteException {
		if (VERBOSE)
			System.out
					.println("*** in setGeneralStringIBAProperty, propertyName = "
							+ propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (part == null)
			return part;
//		if (VERBOSE)
//			Debug.P("*** in addIBA ...");
		IBAHolder ibaHolder = part;
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);
		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out
						.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out
								.println("*** AttributeContainer is still null.");
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		DefaultAttributeContainer container = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		try {
//			if (VERBOSE)
//				Debug.P("**** Begin set IBA ...");
			AbstractAttributeDefinizerNodeView ibaDefNode = getAttributeOrganizer(attrorg);
			AbstractAttributeDefinizerNodeView ibaDefNodes[] = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);
			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			ibaValueView = new StringValueDefaultView(
					(StringDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
//			if (VERBOSE)
//				Debug.P("*** fill " + propertyName + " completed.");
		} catch (IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		wt.iba.value.AttributeContainer attributecontainer1 = ibavaluedbservice
				.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);
//		if (VERBOSE)
//			Debug.P("*** exit addIBA ...");
		return (WTPart) ibaHolder;
	}

	public static IBAHolder setGeneralIntegerIBAProperty(String propertyName,
			long value, IBAHolder ibaHolder) throws WTIntrospectionException,
			WTException, RemoteException {
		if (VERBOSE)
			System.out
					.println("*** in setGeneralIntegerIBAProperty, propertyName = "
							+ propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (ibaHolder == null)
			return ibaHolder;
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);
		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out
						.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out
								.println("*** AttributeContainer is still null.");
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}
		AttributeDefDefaultView ibaDefView = null;
		IntegerValueDefaultView ibaValueView = null;
		DefaultAttributeContainer container = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		try {
//			if (VERBOSE)
//				Debug.P("**** Begin set IBA ...");
			AbstractAttributeDefinizerNodeView ibaDefNode = getAttributeOrganizer(attrorg);
			AbstractAttributeDefinizerNodeView ibaDefNodes[] = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);
			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			ibaValueView = new IntegerValueDefaultView(
					(IntegerDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
//			if (VERBOSE)
//				Debug.P("*** fill " + propertyName + " completed.");
		} catch (IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		wt.iba.value.AttributeContainer attributecontainer1 = ibavaluedbservice
				.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);
//		if (VERBOSE)
//			Debug.P("*** exit addIBA ...");
		return ibaHolder;
	}

	public static IBAHolder setGeneralFloatIBAProperty(String propertyName,
			float value, IBAHolder ibaHolder) throws WTIntrospectionException,
			WTException, RemoteException {
		if (VERBOSE)
			System.out
					.println("*** in setGeneralFloatIBAProperty, propertyName = "
							+ propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);
		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out
						.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out
								.println("*** AttributeContainer is still null.");
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}
		AttributeDefDefaultView ibaDefView = null;
		FloatValueDefaultView ibaValueView = null;
		DefaultAttributeContainer container = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		try {
//			if (VERBOSE)
//				Debug.P("**** Begin set IBA ...");
			AbstractAttributeDefinizerNodeView ibaDefNode = getAttributeOrganizer(attrorg);
			AbstractAttributeDefinizerNodeView ibaDefNodes[] = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);
			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			String temp = String.valueOf(value);
//			if (VERBOSE)
//				Debug.P("value=" + value);
			ibaValueView = new FloatValueDefaultView((FloatDefView) ibaDefView);
			ibaValueView.setValue(value);
			ibaValueView.setPrecision(temp.length() - 1);
			container.addAttributeValue(ibaValueView);
//			if (VERBOSE)
//				Debug.P("*** fill " + propertyName + " completed.");
		} catch (IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		} catch (WTPropertyVetoException wte) {
			wte.printStackTrace();
		}
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		wt.iba.value.AttributeContainer attributecontainer1 = ibavaluedbservice
				.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);
//		if (VERBOSE)
//			Debug.P("*** exit addIBA ...");
		return ibaHolder;
	}

	public static IBAHolder setGeneralStringIBAProperty(String propertyName,
			String value, IBAHolder ibaHolder) throws WTIntrospectionException,
			WTException, RemoteException {
		if (VERBOSE)
			System.out
					.println("*** in setGeneralStringIBAProperty, propertyName = "
							+ propertyName);
		Locale locale = Locale.SIMPLIFIED_CHINESE;
		if (ibaHolder == null)
			return ibaHolder;
		PersistenceServerHelper.manager.update((Persistable) ibaHolder);
		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, locale, null);
		if (ibaHolder.getAttributeContainer() == null) {
			if (VERBOSE)
				System.out
						.println("*** AttributeContainer is null, set new Container.");
			try {
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, locale, null);
				if (ibaHolder.getAttributeContainer() == null) {
					if (VERBOSE)
						System.out
								.println("*** AttributeContainer is still null.");
					ibaHolder
							.setAttributeContainer(new DefaultAttributeContainer());
				}
			} catch (WTException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			} catch (RemoteException e) {
				ibaHolder
						.setAttributeContainer(new DefaultAttributeContainer());
			}
		}
		AttributeDefDefaultView ibaDefView = null;
		StringValueDefaultView ibaValueView = null;
		DefaultAttributeContainer container = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		try {
//			if (VERBOSE)
//				Debug.P("**** Begin set IBA ...");
			AbstractAttributeDefinizerNodeView ibaDefNode = getAttributeOrganizer(attrorg);
			AbstractAttributeDefinizerNodeView ibaDefNodes[] = IBADefinitionHelper.service
					.getAttributeChildren(ibaDefNode);
			ibaDefNode = getNode(ibaDefNodes, propertyName);
			ibaDefView = getDefaultViewObject(ibaDefNode);
			container.deleteAttributeValues(ibaDefView);
			ibaValueView = new StringValueDefaultView(
					(StringDefView) ibaDefView, value);
			container.addAttributeValue(ibaValueView);
//			if (VERBOSE)
//				Debug.P("*** fill " + propertyName + " completed.");
		} catch (IBAValueException ibave) {
			ibave.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		Object obj = container.getConstraintParameter();
		wt.iba.value.AttributeContainer attributecontainer1 = ibavaluedbservice
				.updateAttributeContainer(ibaHolder, obj, null, null);
		ibaHolder.setAttributeContainer(attributecontainer1);
//		if (VERBOSE)
//			Debug.P("*** exit addIBA ...");
		return ibaHolder;
	}

	static {
		CLASSNAME = IBAHelper.class.getName();
		try {
			WTProperties wtproperties = WTProperties.getLocalProperties();
			VERBOSE = wtproperties.getProperty("ext.util.verbose",
					false);

			attrorg = wtproperties.getProperty("property.orgcontainer");

		} catch (Exception exception) {
			exception.printStackTrace(System.err);
			throw new ExceptionInInitializerError(exception);
		}
	}
}
