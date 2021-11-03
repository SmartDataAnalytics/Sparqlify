package org.aksw.sparqlify.admin.web.api;

import java.util.Map;

import org.aksw.service_framework.core.SparqlService;
import org.aksw.service_framework.jpa.core.ServiceEventListener;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;


public class ServiceEventListenerRegister
    implements ServiceEventListener<Rdb2RdfConfig, Rdb2RdfExecution, SparqlService>
{
    private Map<String, SparqlService> nameToQef;

//	public ServiceEventListenerRegister(Map<String, QueryExecutionFactory> nameToQef) {
//		this.nameToQef = nameToQef;
//	}

    public ServiceEventListenerRegister(Map<String, SparqlService> nameToQef) {
        this.nameToQef = nameToQef;
    }

    @Override
    public void onAfterServiceStart(Rdb2RdfConfig config,
            Rdb2RdfExecution executionContext, SparqlService sparqlService) {
        String name = config.getContextPath();


        nameToQef.put(name, sparqlService);
    }

    @Override
    public void onBeforeServiceStop(Rdb2RdfConfig config,
            Rdb2RdfExecution executionContext, SparqlService sparqlService) {

        String name = config.getContextPath();

        nameToQef.remove(name);
    }

}
