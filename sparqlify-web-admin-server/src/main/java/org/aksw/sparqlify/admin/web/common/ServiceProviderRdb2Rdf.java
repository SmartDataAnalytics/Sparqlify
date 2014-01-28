package org.aksw.sparqlify.admin.web.common;

import org.aksw.service_framework.core.ServiceProviderBase;
import org.aksw.service_framework.core.SparqlService;

import com.jolbox.bonecp.BoneCPDataSource;


public class ServiceProviderRdb2Rdf
	extends ServiceProviderBase<SparqlService>
{
	private BoneCPDataSource dataSource;
	private SparqlService sparqlService;
	
	public ServiceProviderRdb2Rdf(String name, BoneCPDataSource dataSource, SparqlService sparqlService) {
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
		dataSource.close();
	}

//	@Override
//	public boolean isClosed() {
//		dataSource.
//	}
}
