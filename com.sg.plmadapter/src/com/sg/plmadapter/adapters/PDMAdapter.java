package com.sg.plmadapter.adapters;

import org.eclipse.core.runtime.IAdapterFactory;

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
		if (adaptableObject instanceof Folder) {
			Folder folder = (Folder) adaptableObject;
			Folder container = folder.getContainer();
			Object type = container.getValue(Folder.F_CONTAINER_TYPE);
			if (CONTAINER_WINDCHILL.equals(type)) {
				String url = (String) container.getValue(F_WINDCHILL_ADDRESS);
				IPDMServiceProvider service = PLMAdapter.getService(url);
				if (service == null) {
					String username = (String) container
							.getValue(F_WINDCHILL_USERNAME);
					String password = (String) container
							.getValue(F_WINDCHILL_PASSWORD);
					service = new WindchillService(url, username, password,
							folder);
					PLMAdapter.registeService(url, service);
				}
				return service;
			}
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IPDMServiceProvider.class };
	}

}
