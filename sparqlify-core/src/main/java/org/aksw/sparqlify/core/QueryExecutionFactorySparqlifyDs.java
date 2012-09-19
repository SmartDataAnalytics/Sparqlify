package org.aksw.sparqlify.core;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutionFactorySparqlifyDs.class);
	
	
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
		
		logger.info("Created qef for query: " + query);
		
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			// Turning off auto commit is a prerequisite for streaming result sets
			// (at least on PostgreSQL)
			conn.setAutoCommit(false);

			QueryExecutionStreaming result = new QueryExecutionSparqlify(system, conn, true, query, this);
			
			conn.commit();
			
			return result;
		} catch (SQLException e) {
			if(conn != null) {
				try {
					conn.rollback();
				} catch(SQLException f) {
					throw new RuntimeException(f);
				}
			}
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
