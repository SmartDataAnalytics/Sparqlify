package org.aksw.sparqlify.core.sparql;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryBackQuery;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriter;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public class QueryExecutionFactorySparqlifyExplain
	extends QueryExecutionFactoryBackQuery
{
	private DataSource dataSource;
	private SparqlSqlOpRewriter ssoRewriter;
	private SqlOpSerializer sqlOpSerializer;
	
	public QueryExecutionFactorySparqlifyExplain(DataSource dataSource,
			SparqlSqlOpRewriter ssoRewriter, SqlOpSerializer sqlOpSerializer) {
		super();
		this.dataSource = dataSource;
		this.ssoRewriter = ssoRewriter;
		this.sqlOpSerializer = sqlOpSerializer;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public SparqlSqlOpRewriter getRewriter() {
		return ssoRewriter;
	}
	
	public SqlOpSerializer getSerializer() {
		return sqlOpSerializer;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getState() {
		return null;
	}

	@Override
	public QueryExecution createQueryExecution(Query query) {

		QueryExecution result = new QueryExecutionSparqlifyExplain(query, ssoRewriter, sqlOpSerializer, dataSource);
		
		return result;
	}

}
