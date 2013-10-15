package org.aksw.service_framework.core;

public interface ServiceExecution<T> {

	T getService();
	
	String getName();
	/**
	 * Free resources associated with the context
	 */
	void close();
}