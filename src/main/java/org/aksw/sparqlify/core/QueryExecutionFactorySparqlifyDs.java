package org.aksw.sparqlify.core;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;

import com.hp.hpl.jena.query.Query;

/**
 * Query execution that obtains fresh connections for each query from a datasource
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class QueryExecutionFactorySparqlifyDs
	extends QueryExecutionFactoryBackQuery<QueryExecutionStreaming>
{
	private RdfViewSystem system;
	private DataSource dataSource;

	public QueryExecutionFactorySparqlifyDs(RdfViewSystem system, DataSource dataSource)
	{
		this.system = system;
		this.dataSource = dataSource;
	}
	
	@Override
	public QueryExecutionStreaming createQueryExecution(Query query) {
		//System.out.println(query);
		
		try {
			return new QueryExecutionSparqlify(system, dataSource.getConnection(), true, query, this);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getId() {
		// TODO Implement
		throw new RuntimeException("Implement properly");
	}

	@Override
	public String getState() {
		// TODO Implement
		throw new RuntimeException("Implement properly");
	}
	
}
