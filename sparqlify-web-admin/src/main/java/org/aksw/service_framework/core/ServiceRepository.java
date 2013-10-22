package org.aksw.service_framework.core;

import org.aksw.service_framework.jpa.core.ServiceControl;




public interface ServiceRepository<S> {

//	void killAll();
	void startAll();
//	
//	void start(String name);
//	void stop(String name);
	
	ServiceControl<S> startByConfigId(Object configId);
	void stopByConfigId(Object configId);
}


