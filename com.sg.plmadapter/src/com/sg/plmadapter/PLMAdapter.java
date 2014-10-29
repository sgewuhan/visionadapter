package com.sg.plmadapter;

import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sg.business.model.IPDMServiceProvider;

public class PLMAdapter implements BundleActivator {

	private static BundleContext context;
	private static ConcurrentHashMap<String, IPDMServiceProvider> serviceCache;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		PLMAdapter.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		PLMAdapter.context = null;
	}

	public static IPDMServiceProvider getService(String key) {
		if(serviceCache == null) {
			return null;
		}
		return serviceCache.get(key);
	}

	public static void registeService(String key, IPDMServiceProvider service) {
		if(serviceCache == null){
			serviceCache = new ConcurrentHashMap<String, IPDMServiceProvider>();
		}
		serviceCache.put(key, service);
	}

}
