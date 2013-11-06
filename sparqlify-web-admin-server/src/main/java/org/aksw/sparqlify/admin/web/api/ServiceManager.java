package org.aksw.sparqlify.admin.web.api;

public interface ServiceManager {
	void startService(String id);
	void stopService(String id);
	
	/**
	 * 
	 * Register a service based on the id of a configuration object
	 * @param configId
	 * @return A string identifying the registered service.
	 */
	String registerService(Object configId);

	void deleteService(String id);
	
	/**
	 * Retrieve the configId for a given serivceId
	 * @param id
	 * @return
	 */
	Object getConfigId(String id);
}
