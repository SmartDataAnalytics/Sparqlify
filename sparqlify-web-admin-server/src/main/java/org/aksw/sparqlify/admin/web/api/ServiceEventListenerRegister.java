package org.aksw.sparqlify.admin.web.api;

import java.util.Map;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.service_framework.jpa.core.ServiceEventListener;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;


public class ServiceEventListenerRegister
	implements ServiceEventListener<Rdb2RdfConfig, Rdb2RdfExecution, QueryExecutionFactory>
{
	private Map<String, QueryExecutionFactory> nameToQef;

//	public ServiceEventListenerRegister(Map<String, QueryExecutionFactory> nameToQef) {
//		this.nameToQef = nameToQef;
//	}
	
	public ServiceEventListenerRegister(Map<String, QueryExecutionFactory> nameToQef) {
		this.nameToQef = nameToQef;
	}
	
	@Override
	public void onAfterServiceStart(Rdb2RdfConfig config,
			Rdb2RdfExecution executionContext, QueryExecutionFactory qef) {
		String name = config.getContextPath();
		
		nameToQef.put(name, qef);
	}

	@Override
	public void onBeforeServiceStop(Rdb2RdfConfig config,
			Rdb2RdfExecution executionContext, QueryExecutionFactory qef) {

		String name = config.getContextPath();
		
		nameToQef.remove(name);
	}

}
