package org.aksw.service_framework.core;



public interface ServiceExecution<T> {

	T getService();

	ServiceStatus getStatus();
	void start();
	
	// Stop should free any resources in order to prevent resource leak
	void stop();
	
	//void kill();
	
	/**
	 * Free resources associated with the context
	 */
	//void close();
}