package org.aksw.sparqlify.config.loader;

import java.sql.Connection;

import javax.sql.DataSource;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.commons.sparql.api.limit.QueryExecutionFactoryLimit;
import org.aksw.commons.sparql.api.timeout.QueryExecutionFactoryTimeout;
import org.aksw.sparqlify.config.lang.ConfiguratorRdfViewSystem;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.core.RdfViewSystem;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.database.RdfViewSystem2;
import org.aksw.sparqlify.validation.LoggerCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SparqlifyQefFactory {
	private Config config = null;
	private DataSource dataSource = null;
	
	private Integer maxQueryExecutionTime = null;
	private Long maxResultSetRowCount = null;
	
	private static Logger logger = LoggerFactory.getLogger(SparqlifyQefFactory.class);
	
	public SparqlifyQefFactory() {
	}
	
	public Config getConfig() {
		return config;
	}
	
	public void setConfig(Config config) {
		this.config = config;
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setMaxQueryExecutionTime(Integer seconds) {
		this.maxQueryExecutionTime = seconds;
	}
	
	public Integer getMaxQueryExecutionTime() {
		return maxQueryExecutionTime;
	}
	
	public void setMaxResultSetRowCount(Long count) {
		this.maxResultSetRowCount = count;
	}
	
	public Long getMaxResultSetRowCount() {
		return maxResultSetRowCount;
	}
	
	
	public QueryExecutionFactory create() throws Exception {

		LoggerCount loggerCount = new LoggerCount(logger);

		
		RdfViewSystem system = new RdfViewSystem2();
		ConfiguratorRdfViewSystem.configure(config, system, loggerCount);


		logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());
		
		if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
		}
		Connection conn = dataSource.getConnection();
		 
		RdfViewSystemOld.loadDatatypes(conn, system.getViews());
		conn.close();

		QueryExecutionFactory<QueryExecutionStreaming> qef = new QueryExecutionFactorySparqlifyDs(system, dataSource);
		
		if(maxQueryExecutionTime != null) {
			qef = QueryExecutionFactoryTimeout.decorate(qef, maxQueryExecutionTime * 1000);
		}
		
		if(maxResultSetRowCount != null) {
			qef = QueryExecutionFactoryLimit.decorate(qef, false, maxResultSetRowCount);
		}
		
		return qef;
	}
}
