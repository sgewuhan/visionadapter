package ext.tmt.utils;

/* bcwti
 *
 * Copyright (c) 2008 Parametric Technology Corporation (PTC). All Rights
 * Reserved.
 *
 * This software is the confidential and proprietary information of PTC.
 * You shall not disclose such confidential information and shall use it
 * only in accordance with the terms of the license agreement.
 *
 * ecwti
 */

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.ReferenceFactory;
import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.RatioDefView;
import wt.iba.definition.litedefinition.ReferenceDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.litedefinition.TimestampDefView;
import wt.iba.definition.litedefinition.URLDefView;
import wt.iba.definition.litedefinition.UnitDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.AttributeContainer;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.LoadValue;
import wt.services.applicationcontext.implementation.DefaultServiceProvider;
import wt.session.SessionHelper;
import wt.util.WTContext;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTStandardDateFormat;
import wt.util.range.Range;

import com.ptc.core.command.common.bean.entity.NewEntityCommand;
import com.ptc.core.command.common.bean.entity.PrepareEntityCommand;
import com.ptc.core.foundation.type.server.impl.SoftAttributesHelper;
import com.ptc.core.meta.common.AnalogSet;
import com.ptc.core.meta.common.AttributeIdentifier;
import com.ptc.core.meta.common.AttributeTypeIdentifier;
import com.ptc.core.meta.common.ConstraintIdentifier;
import com.ptc.core.meta.common.DataSet;
import com.ptc.core.meta.common.DataTypesUtility;
import com.ptc.core.meta.common.DefinitionIdentifier;
import com.ptc.core.meta.common.DiscreteSet;
import com.ptc.core.meta.common.IdentifierFactory;
import com.ptc.core.meta.common.OperationIdentifier;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.TypeInstanceIdentifier;
import com.ptc.core.meta.common.WildcardSet;
import com.ptc.core.meta.container.common.AttributeContainerSpec;
import com.ptc.core.meta.container.common.AttributeTypeSummary;
import com.ptc.core.meta.container.common.ConstraintContainer;
import com.ptc.core.meta.container.common.ConstraintData;
import com.ptc.core.meta.container.common.ConstraintException;
import com.ptc.core.meta.container.common.impl.DefaultConstraintValidator;
import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.core.meta.type.common.TypeInstance;
import com.ptc.core.meta.type.runtime.server.PopulatedAttributeContainerFactory;
import com.ptc.core.meta.type.server.TypeInstanceUtility;

public class IBAUtils {

	private static boolean VERBOSE = true;

	public static final String IBA_REQUIRED = "IBA_REQUIRED";

	public static final String IBA_IDENTIFIER = "IBA_IDENTIFIER";

	public static final String IBA_NAME = "IBA_NAME";

	public static final String IBA_VALUE = "IBA_VALUE";

	public static final String IBA_LABEL = "IBA_LABEL";

	public static final String IBA_DATATYPE = "IBA_DATATYPE";

	public static final String IBA_EDITABLE = "IBA_EDITABLE";

	public static final String IBA_STRING_LENGTH_MIN = "IBA_STRING_LENGTH_MIN";

	public static final String IBA_STRING_LENGTH_MAX = "IBA_STRING_LENGTH_MAX";

	public static final String IBA_OPTIONS_VECTOR = "IBA_OPTIONS_VECTOR";

	Hashtable<String, Object[]> ibaContainer;
	Hashtable<String, String> ibaNames;

	/**
	 * Default Constructor
	 **/
	@SuppressWarnings("unused")
	private IBAUtils() {
		ibaContainer = new Hashtable<String, Object[]>();
	}

	public IBAUtils(IBAHolder ibaHolder) {
		super();
		initializeIBAPart(ibaHolder);
	}

	/**
	 * Converts the IBAValueDisplayString into a String
	 * 
	 * @return String The IBAValueDisplay string
	 * @exception Will
	 *                print a stack trace
	 */
	public String toString() {

		StringBuffer tempString = new StringBuffer();
		Enumeration<String> res = ibaContainer.keys();
		try {
			while (res.hasMoreElements()) {
				String theKey = (String) res.nextElement();
				AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer
						.get(theKey))[1];
				tempString.append(theKey
						+ " - "
						+ IBAValueUtility.getLocalizedIBAValueDisplayString(
								theValue, SessionHelper.manager.getLocale()));
				tempString.append('\n');
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (tempString.toString());

	}

	/**
	 * Method to retreive an IBAValue from the Windchill database given a name
	 * supplied in the staging database
	 * 
	 * @param name
	 *            Name of the soft attribute as supplied in the staging database
	 * @return String the IBAValue found in the Windchill database
	 * @exception If
	 *                the IBAValue is not found in the Windchill database the
	 *                method will return a null String
	 */
	public String getIBAValue(String name) {
		try {
			return getIBAValue(name, SessionHelper.manager.getLocale());
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method to retreive an IBAValue from the Windchill database given a name
	 * supplied in the staging database
	 * 
	 * @param name
	 *            Name of the soft attribute as supplied in the staging database
	 * @param locale
	 *            A valid Locale object
	 * @return String the IBAValue found in the Windchill database
	 * @exception If
	 *                the IBAValue is not found in the Windchill database the
	 *                method will return a null String
	 */
	public String getIBAValue(String name, Locale locale) {
		try {
			AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer
					.get(name))[1];
			return (IBAValueUtility.getLocalizedIBAValueDisplayString(theValue,
					locale));
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}

	}

	/**
	 * Responsible for initializing a object that has soft attributes
	 * 
	 * @param ibaHolder
	 *            Container that holds the soft attributes
	 * @exception Exception
	 *                is handled if unable to intialize a part with soft
	 *                attributes
	 */
	private void initializeIBAPart(IBAHolder ibaHolder) {
		ibaContainer = new Hashtable<String, Object[]>();
		ibaNames = new Hashtable<String, String>();
		try {
			ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
					ibaHolder, null, SessionHelper.manager.getLocale(), null);
			DefaultAttributeContainer theContainer = (DefaultAttributeContainer) ibaHolder
					.getAttributeContainer();
			if (theContainer != null) {
				AttributeDefDefaultView[] theAtts = theContainer
						.getAttributeDefinitions();
				for (int i = 0; i < theAtts.length; i++) {
					AbstractValueView[] theValues = theContainer
							.getAttributeValues(theAtts[i]);
					if (theValues != null) {
						Object[] temp = new Object[2];
						temp[0] = theAtts[i];
						temp[1] = theValues[0];
						ibaContainer.put(theAtts[i].getName(), temp);

					}
					ibaNames.put(theAtts[i].getDisplayName(),
							theAtts[i].getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Responsible for translating the display name of a soft attribute to a
	 * string
	 * 
	 * @param displayName
	 *            The display name of a soft attribute
	 * @return String The representation of the soft attribute display name as a
	 *         String
	 */
	public String translateIBAName(String displayName) {
		return ibaNames.get(displayName).toString();
	}

	/**
	 * Method responsible for updating a part that has soft attributes
	 * 
	 * @param ibaHolder
	 *            A container for the soft attributes associated with the part
	 * @return IBAHolder The resulting soft attribute container after the update
	 * @throws Exception
	 *             Thrown if unable to update the attribute values contained
	 *             within the IBAHolder
	 */
	public IBAHolder updateIBAPart(IBAHolder ibaHolder) throws Exception {

		ibaHolder = IBAValueHelper.service.refreshAttributeContainer(ibaHolder,
				null, SessionHelper.manager.getLocale(), null);
		DefaultAttributeContainer theContainer = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();

		Enumeration<Object[]> res = ibaContainer.elements();
		// Loop through each of the IBA path values in the ibaContainer
		while (res.hasMoreElements()) {
			try {
				Object[] temp = (Object[]) res.nextElement();
				AbstractValueView theValue = (AbstractValueView) temp[1];
				AttributeDefDefaultView theDef = (AttributeDefDefaultView) temp[0];
				if (theValue.getState() == AbstractValueView.CHANGED_STATE) {
					theContainer.deleteAttributeValues(theDef);
					theValue.setState(AbstractValueView.NEW_STATE);
					theContainer.addAttributeValue(theValue);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		ibaHolder.setAttributeContainer(theContainer);
		LoadValue.applySoftAttributes(ibaHolder);
		return ibaHolder;

	}

	public IBAHolder updateAttributeContainer(IBAHolder ibaHolder) {
		try {
			return updateIBAPart(ibaHolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ibaHolder;
	}

	/**
	 * Update without checkout/checkin
	 * 
	 * @param ibaholder
	 * @return
	 */
	public static boolean updateIBAHolder(IBAHolder ibaholder)
			throws WTException {
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		boolean flag = true;
		try {
			PersistenceServerHelper.manager.update((Persistable) ibaholder);
			AttributeContainer attributecontainer = ibaholder
					.getAttributeContainer();
			Object obj = ((DefaultAttributeContainer) attributecontainer)
					.getConstraintParameter();
			AttributeContainer attributecontainer1 = ibavaluedbservice
					.updateAttributeContainer(ibaholder, obj, null, null);
			ibaholder.setAttributeContainer(attributecontainer1);
		} catch (WTException wtexception) {
			System.out.println("updateIBAHOlder: Couldn't update. "
					+ wtexception);
			throw new WTException(
					"IBAUtility.updateIBAHolder() - Couldn't update IBAHolder : "
							+ wtexception);
		}
		return flag;
	}

	/**
	 * Pass in a name if you want to set the value of an existing attribute. to
	 * create a new iba def you must pass in a full iba path value.
	 * 
	 * @param name
	 *            Name of the attribute you would like to change the value of
	 * @param value
	 *            Value that you would like to change the soft attribute to
	 * @throws WTPropertyVetoException
	 *             Thrown if an invalid value is used for the named soft
	 *             attribute
	 */
	public void setIBAValue(String name, String value)
			throws WTPropertyVetoException {
		AbstractValueView ibaValue = null;
		AttributeDefDefaultView theDef = null;
		Object[] obj = (Object[]) ibaContainer.get(name);
		if (obj != null) {
			ibaValue = (AbstractValueView) obj[1];
			theDef = (AttributeDefDefaultView) obj[0];
		}
		if (ibaValue == null) {
			System.out.println("IBA Value is null.");
		}
		if (ibaValue == null)
			theDef = getAttributeDefinition(name);
		if (theDef == null) {
			System.out.println("definition is null ...");
			return;
		}
		ibaValue = internalCreateValue(theDef, value);
		if (ibaValue == null) {
			System.out.println("after creation, iba value is null ..");
			return;
		}

		ibaValue.setState(AbstractValueView.CHANGED_STATE);
		Object[] temp = new Object[2];
		temp[0] = theDef;
		temp[1] = ibaValue;

		ibaContainer.put(theDef.getName(), temp);

	}

	// This method is a "black-box": pass in a string, like
	// "Electrical/Resistance/
	// ResistanceRating" and get back a IBA definition object.
	/**
	 * @param ibaPath
	 *            as a string
	 * @return AttributeDefDefaultView
	 **/
	// //////////////////////////////////////////////////////////////////////////////
	private AttributeDefDefaultView getAttributeDefinition(String ibaPath) {

		AttributeDefDefaultView ibaDef = null;
		try {
			ibaDef = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(ibaPath);
			if (ibaDef == null) {
				AbstractAttributeDefinizerView ibaNodeView = DefinitionLoader
						.getAttributeDefinition(ibaPath);
				if (ibaNodeView != null)
					ibaDef = IBADefinitionHelper.service
							.getAttributeDefDefaultView((AttributeDefNodeView) ibaNodeView);
			}
		} catch (Exception wte) {
			wte.printStackTrace();
		}

		return ibaDef;
	}

	// another "black-box": pass in a string, and get back an IBA value object.
	// /////////////////////////////////////////////////////////////////////////////////
	/**
	 * @param theValue
	 *            as a string
	 * @return theDef as a AbstractAttributeDefinizerView
	 * @return AbstractValueView
	 **/
	private AbstractValueView internalCreateValue(
			AbstractAttributeDefinizerView theDef, String theValue) {

		AbstractValueView theView = null;
		if (theDef instanceof FloatDefView)
			theView = LoadValue.newFloatValue(theDef, theValue, null);
		else if (theDef instanceof StringDefView)
			theView = LoadValue.newStringValue(theDef, theValue);
		else if (theDef instanceof IntegerDefView)
			theView = LoadValue.newIntegerValue(theDef, theValue);
		else if (theDef instanceof RatioDefView)
			theView = LoadValue.newRatioValue(theDef, theValue, null);
		else if (theDef instanceof TimestampDefView)
			theView = LoadValue.newTimestampValue(theDef, theValue);
		else if (theDef instanceof BooleanDefView)
			theView = LoadValue.newBooleanValue(theDef, theValue);
		else if (theDef instanceof URLDefView)
			theView = LoadValue.newURLValue(theDef, theValue, null);
		else if (theDef instanceof ReferenceDefView)
			theView = LoadValue.newReferenceValue(theDef, "ClassificationNode",
					theValue);
		else if (theDef instanceof UnitDefView)
			theView = LoadValue.newUnitValue(theDef, theValue, null);
		return theView;
	}

	/**
	 * 设定IBA属性, 如果返回值为true, 则需要更新对象: persistable =
	 * IBAValueHelper.service.updateIBAHolder(ibaHolder, null, null, null)
	 * 
	 * @param ibaHolder
	 *            设定IBA属性目标对象
	 * @param ibaValues
	 *            要设定的属性名和值集合
	 * @return 对象是否被更改需要保存
	 * @throws WTException
	 */
	public static boolean setIBAValues2(IBAHolder ibaHolder,
			Properties ibaValues) throws WTException {
		System.out.println("in iba set ......");
		// 去对象的原有IBA属性
		HashMap ibaMap = new HashMap();
		Locale locale = WTContext.getContext().getLocale();
		TimeZone tzone = WTContext.getContext().getTimeZone();
		TypeInstance ti = getIBAValuesInternal(ibaHolder, null, ibaMap, false);
		IdentifierFactory idFactory = (IdentifierFactory) DefaultServiceProvider
				.getService(com.ptc.core.meta.common.IdentifierFactory.class,
						"default");

		// 整理要赋值的IBA属性
		ArrayList listIBAId = new ArrayList();
		ArrayList listIBATypeId = new ArrayList();
		ArrayList listIBAValue = new ArrayList();
		for (Enumeration en = ibaValues.keys(); en.hasMoreElements();) {
			String iName = (String) en.nextElement();
			System.out.println("iName" + iName);
			String iVal = (String) ibaValues.get(iName);
			System.out.println("iVal" + iVal);
			if (iVal == null) // null ==> 使用默认值
				continue;

			HashMap ibaInfo = (HashMap) ibaMap.get(iName);
			if (ibaInfo == null) { // 未定义的属性名称
				Persistable p = (Persistable) ibaHolder;
				String oid = PersistenceHelper.isPersistent(p) ? new ReferenceFactory()
						.getReferenceString(p) : ibaHolder.getClass().getName()
						+ ":NEW";
				System.out.println("未定义的IBA属性名: [" + iName + "], " + oid);
				continue;
			}
			Boolean required = (Boolean) ibaInfo.get(IBA_REQUIRED);
			if (required != null && required.booleanValue() && iVal.equals(""))
				throw new WTException("属性<" + iName + ">的值不能为空!");

			AttributeIdentifier ai = (AttributeIdentifier) idFactory
					.get((String) ibaInfo.get(IBA_IDENTIFIER));
			DefinitionIdentifier ati = ai.getDefinitionIdentifier();

			String dataType = (String) ibaInfo.get(IBA_DATATYPE);
			Object iv = convertStringToIBAValue(iVal, dataType, locale, tzone);

			listIBAId.add(ai);
			listIBAValue.add(iv);
			listIBATypeId.add(ati);
		}
		System.out.println("setIBAValues2===========");
		// 逐个赋值
		HashMap vmap = new HashMap();
		TypeInstanceIdentifier tii = (TypeInstanceIdentifier) ti
				.getIdentifier();
		for (int i = 0; i < listIBAId.size(); i++) {
			AttributeTypeIdentifier ati = (AttributeTypeIdentifier) listIBATypeId
					.get(i);
			AttributeIdentifier[] ais = ti.getAttributeIdentifiers(ati);
			if (ais.length > 0) {
				vmap.put(ais[0], ti.get(ais[0]));
				ti.put(ais[0], listIBAValue.get(i));
			} else {
				AttributeIdentifier ai = ati.newAttributeIdentifier(tii);
				vmap.put(ai, null);
				ti.put(ai, listIBAValue.get(i));
			}
		}
		System.out.println("setIBAValues2=====1392======");

		ti.acceptDefaultContent();
		ti.purgeDefaultContent();

		// 检查约束
		if (tii.isInitialized())
			TypeInstanceUtility.populateConstraints(ti, OperationIdentifier
					.newOperationIdentifier("STDOP|com.ptc.windchill.update"));
		else
			TypeInstanceUtility.populateConstraints(ti, OperationIdentifier
					.newOperationIdentifier("STDOP|com.ptc.windchill.create"));
		DefaultConstraintValidator dac = DefaultConstraintValidator
				.getInstance();

		ConstraintContainer cc = ti.getConstraintContainer();
		System.out.println("setIBAValues2=====1409======");

		if (cc != null) {
			AttributeIdentifier ais[] = ti.getAttributeIdentifiers();
			for (int i = 0; i < ais.length; i++) {
				Object ibaVal = ti.get(ais[i]);
				System.out.println("isInitialized=====141415======");

				try {
					dac.isValid(ti, cc, ais[i], ibaVal);
					System.out.println("isInitialized=====1421======");

				} catch (ConstraintException ce) {
					if ((!ce.getConstraintIdentifier()
							.getEnforcementRuleClassname()
							.equals("com.ptc.core.meta.container.common.impl.DiscreteSetConstraint")
							|| vmap == null || vmap.get(ais[i]) == null || (!(vmap
							.get(ais[i]) instanceof Comparable) || ((Comparable) ti
							.get(ais[i])).compareTo((Comparable) vmap
							.get(ais[i])) != 0)
							&& !vmap.get(ais[i]).equals(ti.get(ais[i])))
							&& !ce.getConstraintIdentifier()
									.getEnforcementRuleClassname()
									.equals("com.ptc.core.meta.container.common.impl.ImmutableConstraint")) {
						WTException wtexception = interpretConstraintViolationException(
								ce, locale);
						System.out.println("wtexception=====1409======"
								+ wtexception);

						if (wtexception != null)
							throw wtexception;
					}
				}
			}
		}

		// 保存到目标对象
		TypeInstanceUtility.updateIBAValues((IBAHolder) ibaHolder, ti);
		return ti.isDirty();
	}

	/**
	 * 获取IBA信息的内部实现
	 * 
	 * @param obj
	 *            IBAHolder对象或typeIdentifer字串
	 * @param ibaList
	 *            *
	 * @param ibaMap
	 *            *
	 * @throws WTException
	 */
	@SuppressWarnings("unused")
	public
	static TypeInstance getIBAValuesInternal(Object obj, List<HashMap<String, Serializable>> ibaList,
			Map<String, HashMap<String, Serializable>> ibaMap, boolean returnOpts) throws WTException {
		TypeInstanceIdentifier tii = null;
		Locale locale = WTContext.getContext().getLocale();
		boolean forTypedObj = false;

		// 取TypeInstanceIdentifier
		if (obj instanceof IBAHolder) { // obj是一个IBAHolder(Typed)对象
			tii = TypeIdentifierUtility.getTypeInstanceIdentifier(obj);
			forTypedObj = true;
		} else { // obj是一个TypeIdentifier字符串, e.g. WTTYPE|wt.doc.WTDocument|...
			IdentifierFactory idFactory = (IdentifierFactory) DefaultServiceProvider
					.getService(
							com.ptc.core.meta.common.IdentifierFactory.class,
							"default");
			TypeIdentifier ti = (TypeIdentifier) idFactory.get((String) obj);
			tii = ti.newTypeInstanceIdentifier();
		}

		// 获取TypeInstance
		TypeInstance typeInstance = null;
		try {
			if (false) {
				PopulatedAttributeContainerFactory pacFactory = (PopulatedAttributeContainerFactory) DefaultServiceProvider
						.getService(PopulatedAttributeContainerFactory.class,
								"virtual");
				com.ptc.core.meta.container.common.AttributeContainer ac = pacFactory
						.getAttributeContainer(null,
								(TypeIdentifier) tii.getDefinitionIdentifier());

				if (ac == null) {
					if (obj instanceof String)
						throw new WTException("未定义的SoftType类型: " + obj);
					else
						throw new WTException("未定义的SoftType类型: " + tii);
				}

				AttributeContainerSpec acSpec = new AttributeContainerSpec();
				IdentifierFactory idFact = (IdentifierFactory) DefaultServiceProvider
						.getService(
								com.ptc.core.meta.common.IdentifierFactory.class,
								"logical");
				AttributeTypeIdentifier ati1 = (AttributeTypeIdentifier) idFact
						.get("ALL_SOFT_SCHEMA_ATTRIBUTES",
								tii.getDefinitionIdentifier());
				acSpec.putEntry(ati1, true, true);
				AttributeTypeIdentifier ati2 = (AttributeTypeIdentifier) idFact
						.get("ALL_SOFT_ATTRIBUTES",
								tii.getDefinitionIdentifier());
				acSpec.putEntry(ati2, true, true);
				AttributeTypeIdentifier ati3 = (AttributeTypeIdentifier) idFact
						.get("ALL_SOFT_CLASSIFICATION_ATTRIBUTES",
								tii.getDefinitionIdentifier());
				acSpec.putEntry(ati3, true, true);
				if (tii.isInitialized())
					acSpec.setNextOperation(OperationIdentifier
							.newOperationIdentifier("STDOP|com.ptc.windchill.update"));
				else
					acSpec.setNextOperation(OperationIdentifier
							.newOperationIdentifier("STDOP|com.ptc.windchill.create"));
				PrepareEntityCommand peCmd = new PrepareEntityCommand();
				peCmd.setLocale(locale);
				peCmd.setFilter(acSpec);
				peCmd.setSource(tii);
				peCmd = (PrepareEntityCommand) peCmd.execute();
				typeInstance = peCmd.getResult();
				Set set = (Set) typeInstance.getSingle(ati3);
				if (set != null) {
					for (Iterator iterator = set.iterator(); iterator.hasNext(); typeInstance
							.purge((AttributeTypeIdentifier) iterator.next()))
						;
				}
				typeInstance.purge(ati1);
				typeInstance.purge(ati2);
				typeInstance.purge(ati3);
				AttributeTypeIdentifier ati[] = typeInstance
						.getAttributeTypeIdentifiers();
				for (int j = 0; j < ati.length; j++)
					if (ati[j].getContext() instanceof AttributeTypeIdentifier)
						typeInstance.purge(ati[j]);
			} else {
				typeInstance = SoftAttributesHelper.getSoftSchemaTypeInstance(
						tii, null, locale);
			}
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			throw new WTException(
					wtpropertyvetoexception,
					"SoftAttributesHelper.getSoftSchemaTypeInstance(): "
							+ "Exception encountered when trying to create a type instance");
		} catch (UnsupportedOperationException unsupportedoperationexception) {
			throw new WTException(
					unsupportedoperationexception,
					"SoftAttributesHelper.getSoftSchemaTypeInstance(): "
							+ "Exception encountered when trying to create a type instance");
		}

		// 对IBAHolder对象,填充未设定的属性
		if (forTypedObj) {
			TypeInstanceUtility.populateMissingTypeContent(typeInstance, null);
		}

		// 逐个获取IBA属性
		AttributeIdentifier[] ais = typeInstance.getAttributeIdentifiers();
		for (int i = 0; ais != null && i < ais.length; i++) {
			DefinitionIdentifier di = ais[i].getDefinitionIdentifier();
			AttributeTypeIdentifier ati = (AttributeTypeIdentifier) di;
			AttributeTypeSummary ats = typeInstance
					.getAttributeTypeSummary(ati);

			String ibaIdentifier = ais[i].toExternalForm();
			String name = ati.getAttributeName();
			ati.getWithTailContext();

			String value = String.valueOf(typeInstance.get(ais[i]));
			String dataType = ats.getDataType();
			String label = ats.getLabel();
			Boolean required = ats.isRequired() ? new Boolean(true) : null;
			Boolean editable = ats.isEditable() ? new Boolean(true) : null;

			int min = ats.getMinStringLength();
			int max = ats.getMaxStringLength();
			Integer minStringLength = min == 0 ? null : new Integer(min);
			Integer maxStringLength = max == 0 ? null : new Integer(max);

			HashMap<String, Serializable> ibaInfo = new HashMap<String, Serializable>();
			ibaInfo.put(IBA_IDENTIFIER, ibaIdentifier);
			ibaInfo.put(IBA_NAME, name);
			ibaInfo.put(IBA_VALUE, value);
			ibaInfo.put(IBA_LABEL, label);
			ibaInfo.put(IBA_DATATYPE, dataType);
			ibaInfo.put(IBA_REQUIRED, required);
			ibaInfo.put(IBA_EDITABLE, editable);
			ibaInfo.put(IBA_STRING_LENGTH_MIN, minStringLength);
			ibaInfo.put(IBA_STRING_LENGTH_MAX, maxStringLength);

			if (returnOpts) {
				Vector options = null;
				DataSet dsVal = ats.getLegalValueSet();
				if (dsVal != null && dsVal instanceof DiscreteSet) {
					Object[] eles = ((DiscreteSet) dsVal).getElements();
					options = new Vector();
					for (int j = 0; eles != null && j < eles.length; j++) {
						options.add(String.valueOf(eles[j]));
					}
				}
				ibaInfo.put(IBA_OPTIONS_VECTOR, options);
			}

			if (ibaList != null) {
				ibaList.add(ibaInfo);
			}
			if (ibaMap != null) {
				ibaMap.put(name, ibaInfo);
			}
		}

		return typeInstance;
	}
	
	@SuppressWarnings({ "unused", "deprecation" })
	public static String getIBAValuesInternal(Object obj) throws WTException {
		StringBuffer ibaValues = new StringBuffer();
		TypeInstanceIdentifier tii = null;
		Locale locale = WTContext.getContext().getLocale();
		boolean forTypedObj = false;

		// 取TypeInstanceIdentifier
		if (obj instanceof IBAHolder) { // obj是一个IBAHolder(Typed)对象
			tii = TypeIdentifierUtility.getTypeInstanceIdentifier(obj);
			forTypedObj = true;
		} else { // obj是一个TypeIdentifier字符串, e.g. WTTYPE|wt.doc.WTDocument|...
			IdentifierFactory idFactory = (IdentifierFactory) DefaultServiceProvider
					.getService(
							com.ptc.core.meta.common.IdentifierFactory.class,
							"default");
			TypeIdentifier ti = (TypeIdentifier) idFactory.get((String) obj);
			tii = ti.newTypeInstanceIdentifier();
		}

		// 获取TypeInstance
		TypeInstance typeInstance = null;
		try {
			if (false) {
				PopulatedAttributeContainerFactory pacFactory = (PopulatedAttributeContainerFactory) DefaultServiceProvider
						.getService(PopulatedAttributeContainerFactory.class,
								"virtual");
				com.ptc.core.meta.container.common.AttributeContainer ac = pacFactory
						.getAttributeContainer(null,
								(TypeIdentifier) tii.getDefinitionIdentifier());

				if (ac == null) {
					if (obj instanceof String)
						throw new WTException("未定义的SoftType类型: " + obj);
					else
						throw new WTException("未定义的SoftType类型: " + tii);
				}

				AttributeContainerSpec acSpec = new AttributeContainerSpec();
				IdentifierFactory idFact = (IdentifierFactory) DefaultServiceProvider
						.getService(
								com.ptc.core.meta.common.IdentifierFactory.class,
								"logical");
				AttributeTypeIdentifier ati3 = (AttributeTypeIdentifier) idFact
						.get("ALL_SOFT_CLASSIFICATION_ATTRIBUTES",
								tii.getDefinitionIdentifier());
				acSpec.putEntry(ati3, true, true);
				if (tii.isInitialized())
					acSpec.setNextOperation(OperationIdentifier
							.newOperationIdentifier("STDOP|com.ptc.windchill.update"));
				else
					acSpec.setNextOperation(OperationIdentifier
							.newOperationIdentifier("STDOP|com.ptc.windchill.create"));
				PrepareEntityCommand peCmd = new PrepareEntityCommand();
				peCmd.setLocale(locale);
				peCmd.setFilter(acSpec);
				peCmd.setSource(tii);
				peCmd = (PrepareEntityCommand) peCmd.execute();
				typeInstance = peCmd.getResult();
				Set set = (Set) typeInstance.getSingle(ati3);
				if (set != null) {
					for (Iterator iterator = set.iterator(); iterator.hasNext(); typeInstance
							.purge((AttributeTypeIdentifier) iterator.next()))
						;
				}
				typeInstance.purge(ati3);
				AttributeTypeIdentifier ati[] = typeInstance
						.getAttributeTypeIdentifiers();
				for (int j = 0; j < ati.length; j++)
					if (ati[j].getContext() instanceof AttributeTypeIdentifier)
						typeInstance.purge(ati[j]);
			} else {
				typeInstance = SoftAttributesHelper.getSoftSchemaTypeInstance(
						tii, null, locale);
			}
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			throw new WTException(
					wtpropertyvetoexception,
					"SoftAttributesHelper.getSoftSchemaTypeInstance(): "
							+ "Exception encountered when trying to create a type instance");
		} catch (UnsupportedOperationException unsupportedoperationexception) {
			throw new WTException(
					unsupportedoperationexception,
					"SoftAttributesHelper.getSoftSchemaTypeInstance(): "
							+ "Exception encountered when trying to create a type instance");
		}

		// 对IBAHolder对象,填充未设定的属性
		if (forTypedObj) {
			TypeInstanceUtility.populateMissingTypeContent(typeInstance, null);
		}

		// 逐个获取IBA属性
		AttributeIdentifier[] ais = typeInstance.getAttributeIdentifiers();
		for (int i = 0; ais != null && i < ais.length; i++) {
			DefinitionIdentifier di = ais[i].getDefinitionIdentifier();
			AttributeTypeIdentifier ati = (AttributeTypeIdentifier) di;
			AttributeTypeSummary ats = typeInstance
					.getAttributeTypeSummary(ati);

			String ibaIdentifier = ais[i].toExternalForm();
			String name = ati.getAttributeName();
			ati.getWithTailContext();

			String value = String.valueOf(typeInstance.get(ais[i]));
			ibaValues.append(value + ",");
		}
		return ibaValues.toString().substring(0, ibaValues.toString().lastIndexOf(","));
	}

	/**
	 * 将字符串值按指定类型转换为IBA属性值对应的对象
	 * 
	 * @param strVal
	 *            字符串值
	 * @param dataType
	 *            数据类型(java类型)
	 * @param locale
	 *            *
	 * @param timezone
	 *            *
	 * @return Object值对象
	 * @throws WTException
	 */
	static Object convertStringToIBAValue(String strVal, String dataType,
			Locale locale, TimeZone timezone) throws WTException {
		Object obj = null;
		if (dataType.equals("java.lang.Long"))
			try {
				obj = Long.valueOf(strVal);
			} catch (Exception exception) {
				Object aobj1[] = { strVal };
				throw new WTException(
						"com.ptc.core.HTMLtemplateutil.server.processors.processorsResource",
						"58", aobj1);
			}
		else if (dataType.equals("com.ptc.core.meta.common.FloatingPoint"))
			try {
				obj = DataTypesUtility.toFloatingPoint(strVal, locale);
			} catch (Exception exception1) {
				Object aobj3[] = { strVal };
				throw new WTException(
						"com.ptc.core.HTMLtemplateutil.server.processors.processorsResource",
						"59", aobj3);
			}
		else if (dataType.equals("java.lang.Boolean"))
			obj = Boolean.valueOf(strVal);
		else if (dataType.equals("java.sql.Timestamp"))
			try {
				Date date = null;
				try {
					date = WTStandardDateFormat.parse(strVal, 3, locale,
							timezone);
				} catch (ParseException parseexception) {
					try {
						date = WTStandardDateFormat.parse(strVal, 25, locale,
								timezone);
					} catch (ParseException parseexception1) {
						date = WTStandardDateFormat.parse(strVal, 26, locale,
								timezone);
					}
				}
				obj = new Timestamp(date.getTime());
			} catch (ParseException parseexception) {
				Object aobj5[] = { strVal };
				throw new WTException(
						"com.ptc.core.HTMLtemplateutil.server.processors.processorsResource",
						"60", aobj5);
			}
		else
			obj = new String(strVal);
		return obj;
	}

	/**
	 * 解释IBA约束错误, 来自: EntityTaskDelegate
	 * 
	 * @param constraintexception
	 *            *
	 * @param locale
	 *            *
	 * @return *
	 * @throws WTException
	 */
	public static WTException interpretConstraintViolationException(
			ConstraintException constraintexception, Locale locale)
			throws WTException {
		AttributeIdentifier attributeidentifier = constraintexception
				.getAttributeIdentifier();
		AttributeTypeIdentifier attributetypeidentifier = (AttributeTypeIdentifier) attributeidentifier
				.getDefinitionIdentifier();
		AttributeContainerSpec attributecontainerspec = new AttributeContainerSpec();
		attributecontainerspec.putEntry(attributetypeidentifier, true, true);
		NewEntityCommand newentitycommand = new NewEntityCommand();
		try {
			((NewEntityCommand) newentitycommand)
					.setIdentifier(attributetypeidentifier.getContext());
			newentitycommand.setFilter(attributecontainerspec);
			newentitycommand.setLocale(locale);
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			throw new WTException(wtpropertyvetoexception);
		}
		newentitycommand.execute();
		TypeInstance typeinstance = newentitycommand.getResult();
		AttributeTypeSummary attributetypesummary = typeinstance
				.getAttributeTypeSummary((AttributeTypeIdentifier) attributeidentifier
						.getDefinitionIdentifier());
		String s = attributetypesummary.getLabel();
		// Object obj = constraintexception.getAttributeContent();
		ConstraintIdentifier constraintidentifier = constraintexception
				.getConstraintIdentifier();
		String s1 = constraintidentifier.getEnforcementRuleClassname();
		ConstraintData constraintdata = constraintexception.getConstraintData();
		// String s2 = " ";
		String s3 = "com.ptc.core.HTMLtemplateutil.server.processors.processorsResource";
		String s4 = null;
		java.io.Serializable serializable = constraintdata
				.getEnforcementRuleData();
		ArrayList arraylist = new ArrayList();
		arraylist.add(s);
		if (s1.equals("com.ptc.core.meta.container.common.impl.RangeConstraint")) {
			if (serializable instanceof AnalogSet) {
				Range range = ((AnalogSet) serializable).getBoundingRange();
				if (range.hasLowerBound() && range.hasUpperBound()) {
					arraylist.add(range.getLowerBoundValue());
					arraylist.add(range.getUpperBoundValue());
					s4 = "72";
				} else if (range.hasLowerBound()) {
					arraylist.add(range.getLowerBoundValue());
					s4 = "73";
				} else if (range.hasUpperBound()) {
					arraylist.add(range.getUpperBoundValue());
					s4 = "74";
				}
			} else {
				s4 = "75";
			}
		} else if (s1
				.equals("com.ptc.core.meta.container.common.impl.ImmutableConstraint"))
			s4 = "78";
		else if (s1
				.equals("com.ptc.core.meta.container.common.impl.DiscreteSetConstraint")) {
			if (serializable instanceof DiscreteSet) {
				Object aobj[] = ((DiscreteSet) serializable).getElements();
				String s5 = "";
				for (int j = 0; j < aobj.length; j++)
					s5 = s5 + aobj[j].toString() + ",";

				String s7 = s5.substring(0, s5.length() - 1);
				arraylist.add(s7);
				s4 = "83";
			} else {
				s4 = "84";
			}
		} else if (s1
				.equals("com.ptc.core.meta.container.common.impl.StringLengthConstraint")) {
			if (serializable instanceof AnalogSet) {
				Range range1 = ((AnalogSet) serializable).getBoundingRange();
				if (range1.hasLowerBound() && range1.hasUpperBound()) {
					arraylist.add(range1.getLowerBoundValue());
					arraylist.add(range1.getUpperBoundValue());
					s4 = "79";
				} else if (range1.hasLowerBound()) {
					arraylist.add(range1.getLowerBoundValue());
					s4 = "80";
				} else if (range1.hasUpperBound()) {
					arraylist.add(range1.getUpperBoundValue());
					s4 = "81";
				}
			} else {
				s4 = "82";
			}
		} else if (s1
				.equals("com.ptc.core.meta.container.common.impl.StringFormatConstraint")) {
			if (serializable instanceof DiscreteSet) {
				Object aobj1[] = ((DiscreteSet) serializable).getElements();
				String s6 = "";
				for (int k = 0; k < aobj1.length; k++)
					s6 = s6 + "\"" + aobj1[k].toString() + "\" or ";

				String s8 = s6.substring(0, s6.length() - 4);
				arraylist.add(s8);
				s4 = "85";
			} else {
				s4 = "84";
			}
		} else if (s1
				.equals("com.ptc.core.meta.container.common.impl.UpperCaseConstraint"))
			s4 = "86";
		else if (s1
				.equals("com.ptc.core.meta.container.common.impl.ValueRequiredConstraint"))
			s4 = "77";
		else if (s1
				.equals("com.ptc.core.meta.container.common.impl.WildcardConstraint")) {
			if (serializable instanceof WildcardSet) {
				arraylist.add(((WildcardSet) serializable).getValue());
				int i = ((WildcardSet) serializable).getMode();
				if (i == 1) {
					s4 = "87";
					arraylist.add(((WildcardSet) serializable).getValue());
				} else if (i == 2) {
					if (((WildcardSet) serializable).isNegated())
						s4 = "89";
					else
						s4 = "88";
				} else if (i == 3) {
					if (((WildcardSet) serializable).isNegated())
						s4 = "91";
					else
						s4 = "90";
				} else if (i == 4)
					if (((WildcardSet) serializable).isNegated())
						s4 = "93";
					else
						s4 = "92";
			} else {
				s4 = "84";
			}
		} else {
			s4 = "84";
		}
		if (s4 != null)
			return new WTException(s3, s4, arraylist.toArray());
		else
			return null;
	}
	/**
	 * return multiple IBA values
	 * 
	 * @param name
	 * @return
	 */
	public Vector getIBAValues(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					vector.addElement(IBAValueUtility
							.getLocalizedIBAValueDisplayString(theValue,
									SessionHelper.manager.getLocale()));
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}
}