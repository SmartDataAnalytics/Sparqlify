package org.aksw.sparqlify.web;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.ConfiguratorCandidateSelector;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.ViewDefinitionNormalizerImpl;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.sparql.QueryEx;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.core.sparql.QueryFactoryEx;
import org.aksw.sparqlify.util.ExprRewriteSystem;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.validation.LoggerCount;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.sun.jersey.spi.container.servlet.ServletContainer;


public class Main {
	
	public static void onErrorPrintHelpAndExit(Options cliOptions, LoggerCount loggerCount, int exitCode) {

		if(loggerCount.getErrorCount() != 0) {
			//logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());
			
			printHelpAndExit(cliOptions, exitCode);
			
			throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
		}
		
	}
	
	/**
	 * @param exitCode
	 */
	public static void printHelpAndExit(Options cliOptions, int exitCode) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(HttpSparqlEndpoint.class.getName(), cliOptions);
		System.exit(exitCode);
	}

	private static final Logger logger = LoggerFactory
			.getLogger(HttpSparqlEndpoint.class);
	private static final Options cliOptions = new Options();

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		
		LoggerCount loggerCount = new LoggerCount(logger);

		Class.forName("org.postgresql.Driver");
		
		CommandLineParser cliParser = new GnuParser();

		cliOptions.addOption("P", "port", true, "Server port");
		cliOptions.addOption("C", "context", true, "Context e.g. /sparqlify");
		cliOptions.addOption("B", "backlog", true,
				"Maximum number of connections");

		cliOptions.addOption("t", "type", true,
				"Database type (posgres, mysql,...)");
		cliOptions.addOption("d", "database", true, "Database name");
		cliOptions.addOption("u", "username", true, "");
		cliOptions.addOption("p", "password", true, "");
		cliOptions.addOption("h", "hostname", true, "");
		cliOptions.addOption("c", "class", true, "JDBC driver class");
		cliOptions.addOption("j", "jdbcurl", true, "JDBC URL");

		// Note: Q and D are exclusive. If either is given, no server is started
		cliOptions.addOption("Q", "query", true, "");
		cliOptions.addOption("D", "dump", false, "");

		// TODO Rename to m for mapping file soon
		cliOptions.addOption("m", "mapping", true, "Sparqlify mapping file");

		cliOptions.addOption("t", "timeout", true, "Maximum query execution timeout");
		cliOptions.addOption("n", "resultsetsize", true, "Maximum result set size");

		

		CommandLine commandLine = cliParser.parse(cliOptions, args);

		
		// Parsing of command line args
		String portStr = commandLine.getOptionValue("P", "7531");
		//String backLogStr = commandLine.getOptionValue("B", "100");
		//String contextStr = commandLine.getOptionValue("C", "/sparqlify");
		int port = Integer.parseInt(portStr);
		//int backLog = Integer.parseInt(backLogStr);

//		String hostName = commandLine.getOptionValue("h", "");
//		String dbName = commandLine.getOptionValue("d", "");
//		String userName = commandLine.getOptionValue("u", "");
//		String passWord = commandLine.getOptionValue("p", "");
//		
//		String jdbcUrl = commandLine.getOptionValue("j", "");
		
		
		
		
		//String driverClassName = commandLine.getOptionValue("c", "");
		
		boolean isDump = commandLine.hasOption("D");
		String queryString = commandLine.getOptionValue("Q", "");
		
		boolean isQuery = !queryString.isEmpty();
		if(!isQuery) {
			queryString = null;
		}
		
		if(isDump && isQuery) {
			loggerCount.error("Options D and Q are mutually exclusive");
		}
		
		if(isDump) {
			queryString = "Construct { ?s ?p ?o } { ?s ?p ?o }";
		}
		
		
		String maxQueryExecutionTimeStr = commandLine.getOptionValue("t", null);
		Integer maxQueryExecutionTime = maxQueryExecutionTimeStr == null
				? null
				: Integer.parseInt(maxQueryExecutionTimeStr);
		
		String maxResultSetSizeStr = commandLine.getOptionValue("n", null);
		Long maxResultSetSize = maxResultSetSizeStr == null
				? null
				: Long.parseLong(maxResultSetSizeStr);
		

		Config config = SparqlifyCliHelper.parseSmlConfig(commandLine, loggerCount);
		onErrorPrintHelpAndExit(cliOptions, loggerCount, -1);

		/*
		 * Connection Pool  
		 */
		DataSource dataSource = SparqlifyCliHelper.parseDataSource(commandLine, loggerCount);
		onErrorPrintHelpAndExit(cliOptions, loggerCount, -1);		
		
		
		RdfViewSystemOld.initSparqlifyFunctions();
		
		

		//TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
		//TypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
		
		ExprRewriteSystem ers = SparqlifyUtils.createExprRewriteSystem();
		TypeSystem typeSystem = ers.getTypeSystem();
		
		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");


		Connection conn = dataSource.getConnection();
		try {
			SchemaProvider schemaProvider = new SchemaProviderImpl(conn, typeSystem, typeAlias);
			SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
	
			//OpMappingRewriter opMappingRewriter = SparqlifyUtils.createDefaultOpMappingRewriter(typeSystem);
			//MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(typeSystem);
			MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(ers);
			OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
			
			
			CandidateViewSelector<ViewDefinition> candidateViewSelector = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());
	
			
			//RdfViewSystem system = new RdfViewSystem2();
			ConfiguratorCandidateSelector.configure(config, syntaxBridge, candidateViewSelector, loggerCount);
		}
		finally {
			conn.close();
		}

		logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());
		
		if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
		}

		
//		SparqlSqlOpRewriter ssoRewriter = SparqlifyUtils.createSqlOpRewriter(candidateViewSelector, opMappingRewriter, typeSystem);
//		
//		SqlExprSerializerSystem serializerSystem = SparqlifyUtils.createSerializerSystem();
//		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(serializerSystem);
//
//		
//		SparqlSqlStringRewriter rewriter = new SparqlSqlStringRewriterImpl(ssoRewriter, sqlOpSerializer);//SparqlifyUtils.createSparqlSqlStringRewriter(ssoRewriter);
//
//		
//		
//		//SparqlSqlStringRewriter rewriter = SparqlifyUtils.createTestRewriter(candidateViewSelector, opMappingRewriter, typeSystem);
//
//		//SparqlSqlRewriter rewriter = new SparqlSqlRewriterImpl(candidateViewSelector, opMappingRewriter, sqlOpSelectBlockCollector, sqlOpSerializer);
//
//		
//		QueryExecutionFactory qefDefault = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);
//		
//		if(maxQueryExecutionTime != null) {
//			qefDefault = QueryExecutionFactoryTimeout.decorate(qefDefault, maxQueryExecutionTime * 1000);
//		}
//		
//		if(maxResultSetSize != null) {
//			qefDefault = QueryExecutionFactoryLimit.decorate(qefDefault, false, maxResultSetSize);
//		}
//		
//		
//		QueryExecutionFactory qefExplain = new QueryExecutionFactorySparqlifyExplain(dataSource, ssoRewriter, sqlOpSerializer);
//		
//		
//		QueryExecutionFactoryEx qef = new QueryExecutionFactoryExImpl(qefDefault, qefExplain);
		
		QueryExecutionFactoryEx qef = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, config, maxResultSetSize, maxQueryExecutionTime);
		
		if(queryString != null) {
			QueryEx queryEx = QueryFactoryEx.create(queryString); 
			
			if(queryEx.isSelectType()) {
				QueryExecution qe = qef.createQueryExecution(queryEx);
				ResultSet rs = qe.execSelect();
				System.out.println(ResultSetFormatter.asText(rs));
			}
			else if(queryEx.isConstructType()) {
				QueryExecution qe = qef.createQueryExecution(queryString);
				Iterator<Triple> it = qe.execConstructTriples();
				SparqlFormatterUtils.writeText(System.out, it);
				//model.write(System.out, "N-TRIPLES");
			}
			else {
				throw new RuntimeException("Query type not supported: " + queryString);
			}

			
			return;
		}
		
		
		Server server = createSparqlEndpoint(qef, port);
		server.start();
		
		//sparqler = qef; //new QueryExecutionFactorySparqlify(system, conn);

		//QueryExecutionFactoryStreamingProvider provider = new QueryExecutionFactoryStreamingProvider(qef);
		//QueryExecutionFactoryStreamingProvider.qeFactory = qef;

		// server.stop();
	}
	
	public static Server createSparqlEndpoint(QueryExecutionFactoryEx qef, int port) throws Exception {
		HttpSparqlEndpoint.sparqler = qef;
		
		
		ServletHolder sh = new ServletHolder(ServletContainer.class);

		
		/*
		 * For 0.8 and later the "com.sun.ws.rest" namespace has been renamed to
		 * "com.sun.jersey". For 0.7 or early use the commented out code instead
		 */
		// sh.setInitParameter("com.sun.ws.rest.config.property.resourceConfigClass",
		// "com.sun.ws.rest.api.core.PackagesResourceConfig");
		// sh.setInitParameter("com.sun.ws.rest.config.property.packages",
		// "jetty");
		sh.setInitParameter(
				"com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");
		sh.setInitParameter("com.sun.jersey.config.property.packages",
				"org.aksw.sparqlify.web");

		Server server = new Server(port);
		ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

		context.getServletContext().setAttribute("queryExecutionFactory", qef);		
		context.addServlet(sh, "/*");		
		
		return server;
	}
}
