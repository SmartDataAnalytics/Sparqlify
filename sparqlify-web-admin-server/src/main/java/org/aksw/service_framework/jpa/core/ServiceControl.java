package org.aksw.service_framework.jpa.core;

public interface ServiceControl<T>
{
	void start();
	void stop();
	
	T getService();
}
