package org.aksw.sparqlify.admin.web.common;

import javax.sql.DataSource;

import org.aksw.service_framework.core.ServiceProviderBase;
import org.aksw.service_framework.core.SparqlService;


public class ServiceProviderRdb2Rdf
	extends ServiceProviderBase<SparqlService>
{
	protected DataSource dataSource;
	protected Runnable closeAction;
	private SparqlService sparqlService;
	
	public ServiceProviderRdb2Rdf(String name, DataSource dataSource, Runnable closeAction, SparqlService sparqlService) {
		super(name);
		this.dataSource = dataSource;
		this.sparqlService = sparqlService;
	}
	
	@Override
	public SparqlService getService() {
		return sparqlService;
	}

	
	@Override
	public void close() {
		if(closeAction != null) {
			closeAction.run();
		}
	}

//	@Override
//	public boolean isClosed() {
//		dataSource.
//	}
}
