package org.aksw.sparqlify.core.builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Objects;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaperDoubleQuote;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.validation.LoggerCount;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FluentSparqlifyFactory {	
	private static final Logger logger = LoggerFactory.getLogger(FluentSparqlifyFactory.class);

	protected DataSource dataSource;
	protected SqlEscaper sqlEscaper;
	protected DatatypeToString datatypeToString;
	protected Config config;
	
	//protected TypeSerializer typeSerializer;
	//protected MappingOps mappingOps;
	//protected ViewDefinitionNormalizer<?> viewDefinitionNormalizer;
	

	public FluentSparqlifyFactory() {
		// Init defaults
		this.sqlEscaper = new SqlEscaperDoubleQuote();
		this.datatypeToString = new DatatypeToStringPostgres();
		this.config = new Config();
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public FluentSparqlifyFactory setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		return this;
	}

	public SqlEscaper getSqlEscaper() {
		return sqlEscaper;
	}

	public FluentSparqlifyFactory setSqlEscaper(SqlEscaper sqlEscaper) {
		this.sqlEscaper = sqlEscaper;
		return this;
	}

	public DatatypeToString getDatatypeToString() {
		return datatypeToString;
	}

	public FluentSparqlifyFactory setDatatypeToString(DatatypeToString datatypeToString) {
		this.datatypeToString = datatypeToString;
		return this;
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
	
	public FluentSparqlifyFactory addResource(String url) throws IOException, RecognitionException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(url);

		addResource(in, url);
		
		return this;	
	}
	public FluentSparqlifyFactory addResource(InputStream in, String name) throws IOException, RecognitionException {
		Objects.requireNonNull(in);

		ConfigParser parser = new ConfigParser();
		LoggerCount loggerCount = new LoggerCount(logger);
		Config c = parser.parse(in, loggerCount);
		

		if(loggerCount.getWarningCount() > 0 && !(loggerCount.getErrorCount() > 0)) {
			logger.warn("There were " + loggerCount.getWarningCount() +" warnings processing " + name);
		} else if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException(loggerCount.getWarningCount() + " warning and " + loggerCount.getErrorCount() + " errors encountered");
		}
		config.merge(c);
		
		return this;	
	}

	
	public FluentSparqlifyFactory addMappingFile(File file) throws FileNotFoundException, IOException, RecognitionException {
		InputStream in = new FileInputStream(file);
		addResource(in, file.getAbsolutePath());
		return this;
	}
	
	public QueryExecutionFactory create() throws SQLException, IOException {
		
//	    val config = new Config()
//	    	    val loggerCount = new LoggerCount(logger.underlying)
//
//
//	    	    val backendConfig = new SqlBackendConfig(new DatatypeToStringFlink(), new SqlEscaperBacktick())
//	    	    val sqlEscaper = backendConfig.getSqlEscaper()
//	    	    val typeSerializer = backendConfig.getTypeSerializer()
//
//
//	    	    val ers = SparqlifyUtils.createDefaultExprRewriteSystem()
//	    	    val mappingOps = SparqlifyUtils.createDefaultMappingOps(ers)
//
//
//	    	    val candidateViewSelector = new CandidateViewSelectorSparqlify(mappingOps, new ViewDefinitionNormalizerImpl());
//

		SparqlSqlStringRewriter rewriter = SparqlifyUtils.createDefaultSparqlSqlStringRewriter(dataSource, config, datatypeToString, sqlEscaper);

		QueryExecutionFactory result = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);
		return result;
	}
}