package org.aksw.sparqlify.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.commons.sparql.api.limit.QueryExecutionFactoryLimit;
import org.aksw.commons.sparql.api.timeout.QueryExecutionFactoryTimeout;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.ConfiguratorCandidateSelector;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.util.MapReader;
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
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Main {
	/**
	 * @param exitCode
	 */
	public static void printHelpAndExit(int exitCode) {
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

		// Note: Q and D are exclusive. If either is given, no server is started
		cliOptions.addOption("Q", "query", true, "");
		cliOptions.addOption("D", "dump", false, "");
		
		cliOptions.addOption("c", "config", true, "Sparqlify config file");

		cliOptions.addOption("t", "timeout", true, "Maximum query execution timeout");
		cliOptions.addOption("n", "resultsetsize", true, "Maximum result set size");
		
		CommandLine commandLine = cliParser.parse(cliOptions, args);

		
		// Parsing of command line args
		String portStr = commandLine.getOptionValue("P", "7531");
		//String backLogStr = commandLine.getOptionValue("B", "100");
		//String contextStr = commandLine.getOptionValue("C", "/sparqlify");
		int port = Integer.parseInt(portStr);
		//int backLog = Integer.parseInt(backLogStr);

		String hostName = commandLine.getOptionValue("h", "localhost");
		String dbName = commandLine.getOptionValue("d", "");
		String userName = commandLine.getOptionValue("u", "");
		String passWord = commandLine.getOptionValue("p", "");

		boolean isDump = commandLine.hasOption("D");
		String query = commandLine.getOptionValue("Q", "");
		
		boolean isQuery = !query.isEmpty();
		if(!isQuery) {
			query = null;
		}
		
		if(isDump && isQuery) {
			logger.error("Options D and Q are exclusive");
		}
		
		if(isDump) {
			query = "Construct { ?s ?p ?o } { ?s ?p ?o }";
		}
		
		
		String maxQueryExecutionTimeStr = commandLine.getOptionValue("t", null);
		Integer maxQueryExecutionTime = maxQueryExecutionTimeStr == null
				? null
				: Integer.parseInt(maxQueryExecutionTimeStr);
		
		String maxResultSetSizeStr = commandLine.getOptionValue("n", null);
		Long maxResultSetSize = maxResultSetSizeStr == null
				? null
				: Long.parseLong(maxResultSetSizeStr);
		
		
		String configFileStr = commandLine.getOptionValue("c");

		if (configFileStr == null) {
			logger.error("No config file given");

			printHelpAndExit(-1);
		}

		File configFile = new File(configFileStr);
		if (!configFile.exists()) {
			logger.error("File does not exist: " + configFileStr);

			printHelpAndExit(-1);
		}

		LoggerCount loggerCount = new LoggerCount(logger);
		
		ConfigParser parser = new ConfigParser();

		InputStream in = new FileInputStream(configFile);
		Config config;
		try {
			config = parser.parse(in, loggerCount);
		} finally {
			in.close();
		}


		/*
		 * Connection Pool  
		 */
		
		PGSimpleDataSource dataSourceBean = new PGSimpleDataSource();

		dataSourceBean.setDatabaseName(dbName);
		dataSourceBean.setServerName(hostName);
		dataSourceBean.setUser(userName);
		dataSourceBean.setPassword(passWord);

		BoneCPConfig cpConfig = new BoneCPConfig();
		cpConfig.setDatasourceBean(dataSourceBean);
		/*
		cpConfig.setJdbcUrl(dbconf.getDbConnString()); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
		cpConfig.setUsername(dbconf.getUsername()); 
		cpConfig.setPassword(dbconf.getPassword());
		*/
		
		cpConfig.setMinConnectionsPerPartition(5);
		cpConfig.setMaxConnectionsPerPartition(20);
//		cpConfig.setMinConnectionsPerPartition(1);
//		cpConfig.setMaxConnectionsPerPartition(1);
		
		cpConfig.setPartitionCount(1);
		//BoneCP connectionPool = new BoneCP(cpConfig); // setup the connection pool	

		BoneCPDataSource dataSource = new BoneCPDataSource(cpConfig);

		/*
		ComboPooledDataSource pooledDataSource = new ComboPooledDataSource();
		pooledDataSource.
		*/
		
		
		RdfViewSystemOld.initSparqlifyFunctions();
		
		
		Connection conn = dataSource.getConnection();

		DatatypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
		
		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");


		SchemaProvider schemaProvider = new SchemaProviderImpl(conn, datatypeSystem, typeAlias);
		SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

		CandidateViewSelector candidateViewSelector = new CandidateViewSelectorImpl();

		
		//RdfViewSystem system = new RdfViewSystem2();
		ConfiguratorCandidateSelector.configure(config, syntaxBridge, candidateViewSelector, loggerCount);


		logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());
		
		if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
		}

		

		SparqlSqlRewriter rewriter = SparqlifyUtils.createTestRewriter(candidateViewSelector, datatypeSystem);

		//SparqlSqlRewriter rewriter = new SparqlSqlRewriterImpl(candidateViewSelector, opMappingRewriter, sqlOpSelectBlockCollector, sqlOpSerializer);

		
		QueryExecutionFactory<QueryExecutionStreaming> qef = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);
		
		if(maxQueryExecutionTime != null) {
			qef = QueryExecutionFactoryTimeout.decorate(qef, maxQueryExecutionTime * 1000);
		}
		
		if(maxResultSetSize != null) {
			qef = QueryExecutionFactoryLimit.decorate(qef, false, maxResultSetSize);
		}
		
		
		if(query != null) {
//			{
//			QueryExecution qe = qef.createQueryExecution("Select * { ?s ?p ?o }");
//			ResultSet rs = qe.execSelect();
//			System.out.println(ResultSetFormatter.asText(rs));
//			}

//			{
			QueryExecution qe = qef.createQueryExecution(query);
			Model model = qe.execConstruct();
			model.write(System.out, "N-TRIPLES");
//			}
			
			return;
		}
		
		
		
		//sparqler = qef; //new QueryExecutionFactorySparqlify(system, conn);

		//QueryExecutionFactoryStreamingProvider provider = new QueryExecutionFactoryStreamingProvider(qef);
		//QueryExecutionFactoryStreamingProvider.qeFactory = qef;
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
		context.addServlet(sh, "/*");		

		server.start();

		// server.stop();
	}
}
