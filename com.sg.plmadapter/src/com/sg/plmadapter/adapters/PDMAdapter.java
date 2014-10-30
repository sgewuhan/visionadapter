package com.sg.plmadapter.adapters;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;

import com.mobnut.db.model.PrimaryObject;
import com.sg.business.model.Document;
import com.sg.business.model.Folder;
import com.sg.business.model.IPDMServiceProvider;
import com.sg.plmadapter.PLMAdapter;

public class PDMAdapter implements IAdapterFactory {

	private static final String F_WINDCHILL_ADDRESS = "wcaddress";

	private static final String CONTAINER_WINDCHILL = "wc";

	private static final String F_WINDCHILL_USERNAME = "wcuser";

	private static final String F_WINDCHILL_PASSWORD = "wcpassword";

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		Map<String, String> map = getContainerData(adaptableObject);
		if (adaptableObject instanceof Folder) {
			Folder folder = (Folder) adaptableObject;
			String type = map.get(Folder.F_CONTAINER_TYPE);
			if (CONTAINER_WINDCHILL.equals(type)) {
				return getProviderService(folder, map);
			}
		} else if (adaptableObject instanceof Document) {
			Document document = (Document) adaptableObject;
			String type = map.get(Folder.F_CONTAINER_TYPE);
			if (CONTAINER_WINDCHILL.equals(type)) {
				return getProviderService(document, map);
			}
		}
		return null;
	}

	private Map<String, String> getContainerData(Object object) {
		Map<String, String> map = new HashMap<String, String>();
		Folder container = null;
		if (object instanceof Folder) {
			Folder folder = (Folder) object;
			container = folder.getContainer();
		} else if (object instanceof Document) {
			Document document = (Document) object;
			container = document.getFolder().getContainer();
		}
		if (container != null) {
			String url = (String) container.getValue(F_WINDCHILL_ADDRESS);
			String username = (String) container.getValue(F_WINDCHILL_USERNAME);
			String password = (String) container.getValue(F_WINDCHILL_PASSWORD);
			String type = (String) container.getValue(Folder.F_CONTAINER_TYPE);
			map.put(F_WINDCHILL_ADDRESS, url);
			map.put(F_WINDCHILL_USERNAME, username);
			map.put(F_WINDCHILL_PASSWORD, password);
			map.put(Folder.F_CONTAINER_TYPE, type);
		}
		return map;
	}

	private IPDMServiceProvider getProviderService(PrimaryObject po,
			Map<String, String> map) {
		String url = map.get(F_WINDCHILL_ADDRESS);
		IPDMServiceProvider service = PLMAdapter.getService(url);
		if (service == null) {
			service = new WindchillService(url, map.get(F_WINDCHILL_USERNAME),
					map.get(F_WINDCHILL_PASSWORD), po);
			PLMAdapter.registeService(url, service);
		}
		return service;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IPDMServiceProvider.class };
	}

}
