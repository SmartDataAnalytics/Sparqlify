package org.aksw.service_framework.core;

import java.util.Set;



public interface ServiceRepository<S> {

//	void killAll();
	void startAll();
//	
//	void start(String name);
//	void stop(String name);
	
	void startExecutions(Set<?> executionIds);
	void stopExecutions(Set<?> executionIds);
}


