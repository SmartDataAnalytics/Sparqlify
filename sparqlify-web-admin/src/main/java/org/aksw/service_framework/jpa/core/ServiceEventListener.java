package org.aksw.service_framework.jpa.core;

public interface ServiceEventListener<C, E, S> {
//	void handleServiceEvent(ServiceEvent<C, E, S> event);
	void onAfterServiceStart(C config, E executionContext, S service);
	void onBeforeServiceStop(C config, E executionContext, S service);
}