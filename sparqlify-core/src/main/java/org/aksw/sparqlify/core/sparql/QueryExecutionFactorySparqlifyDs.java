package org.aksw.sparqlify.core.sparql;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
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
	extends QueryExecutionFactoryBackQuery
{
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutionFactorySparqlifyDs.class);
	
	
	private SparqlSqlStringRewriter rewriter;
	private DataSource dataSource;

	public QueryExecutionFactorySparqlifyDs(SparqlSqlStringRewriter rewriter, DataSource dataSource)
	{
		this.rewriter = rewriter;
		this.dataSource = dataSource;
	}
	
	@Override
	public QueryExecutionStreaming createQueryExecution(Query query) {
		//System.out.println(query);
		
		logger.info("Created qef for query: " + query);
		
		Connection conn = null;
		try {
			conn = dataSource.getConnection();			
			logger.debug("Opened connection: [" + conn + "]");

			// Turning off auto commit is a prerequisite for streaming result sets
			// (at least on PostgreSQL)
			conn.setAutoCommit(false);

			QueryExecutionStreaming result = new QueryExecutionSparqlify(rewriter, conn, true, query, this);
			
			//conn.commit();
			
			return result;
		} catch (SQLException e) {
			// If something goes wrong in the iterator creation, close the connection again
			if(conn != null) {
				try {
					conn.rollback();
					conn.close();
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
