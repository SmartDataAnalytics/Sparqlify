package org.aksw.sparqlify.admin.web.common;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.service_framework.core.ServiceExecutionBase;

import com.jolbox.bonecp.BoneCPDataSource;


public class ServiceExecutionRdb2Rdf
	extends ServiceExecutionBase<QueryExecutionFactory>
{
	private BoneCPDataSource dataSource;
	private QueryExecutionFactory queryExecutionFactory;
	
	public ServiceExecutionRdb2Rdf(String name, BoneCPDataSource dataSource, QueryExecutionFactory queryExecutionFactory) {
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
}