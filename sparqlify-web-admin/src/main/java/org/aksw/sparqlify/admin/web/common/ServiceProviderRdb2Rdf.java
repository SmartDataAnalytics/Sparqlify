package org.aksw.sparqlify.admin.web.common;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.service_framework.core.ServiceProviderBase;

import com.jolbox.bonecp.BoneCPDataSource;


public class ServiceProviderRdb2Rdf
	extends ServiceProviderBase<QueryExecutionFactory>
{
	private BoneCPDataSource dataSource;
	private QueryExecutionFactory queryExecutionFactory;
	
	public ServiceProviderRdb2Rdf(String name, BoneCPDataSource dataSource, QueryExecutionFactory queryExecutionFactory) {
		super(name);
		this.dataSource = dataSource;
		this.queryExecutionFactory = queryExecutionFactory;
	}
	
	@Override
	public QueryExecutionFactory getService() {
		return queryExecutionFactory;
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
