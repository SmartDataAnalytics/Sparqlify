package org.aksw.sparqlify.rest;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.commons.sparql.api.limit.QueryExecutionFactoryLimit;
import org.aksw.commons.sparql.api.timeout.QueryExecutionFactoryTimeout;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.lang.ConfiguratorRdfViewSystem;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.core.RdfViewSystem;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.database.RdfViewSystem2;
import org.aksw.sparqlify.validation.LoggerCount;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		/*
		PropertyConfigurator.configure("log4j.properties");
		LogManager.getLogManager().readConfiguration(
				new FileInputStream("jdklog.properties"));
		*/

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

		RdfViewSystem system = new RdfViewSystem2();
		ConfiguratorRdfViewSystem.configure(config, system, loggerCount);


		logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());
		
		if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
		}

		
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
		
		Connection conn = dataSource.getConnection();
		 
		RdfViewSystemOld.loadDatatypes(conn, system.getViews());
		conn.close();

		QueryExecutionFactory<QueryExecutionStreaming> qef = new QueryExecutionFactorySparqlifyDs(system, dataSource);
		
		if(maxQueryExecutionTime != null) {
			qef = QueryExecutionFactoryTimeout.decorate(qef, maxQueryExecutionTime * 1000);
		}
		
		if(maxResultSetSize != null) {
			qef = QueryExecutionFactoryLimit.decorate(qef, false, maxResultSetSize);
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
				"org.aksw.sparqlify.rest");

		Server server = new Server(port);
		Context context = new Context(server, "/", Context.SESSIONS);
		context.addServlet(sh, "/*");
		

		server.start();

		// String qs =
		// URLEncoder.encode("Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct {?s ?p ?o } {?s ?p ?o . Filter(?p = rdfs:label && langMatches(lang(?o), 'de') && ?o = 'Buslinie') .}",
		// "UTF8");
		// String qs =
		// URLEncoder.encode("Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct {?s ?p ?o } {?s ?p ?o . Filter(str(?o) = 'Hotel' || str(?o) = 'Tourism') .} Order by Desc(?o)",
		// "UTF8");

		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct {?s ?p ?o . ?s ?x ?y . } { ?s ?p ?o  . ?s ?x ?y . Filter(?y < 7.0) . Filter(?p = rdf:type && ?o = <http://linkedgeodata.org/ontology/Bench>) .} Order By ?s limit 100",
		// "UTF8");
		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct { ?wn ?x ?y . } { ?w lgdo:hasNodesSeq ?wn . ?wn ?x ?y . Filter(?w = <http://linkedgeodata.org/resource/way/10896141>) . } Limit 100",
		// "UTF8");

		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct { ?w ?p ?o . ?o ?x ?y . ?y ?a ?b .} { ?w ?p ?o . ?o ?x ?y . ?y ?a ?b . Filter(?w = <http://linkedgeodata.org/resource/way/10896141> && ?p = <http://linkedgeodata.org/ontology/hasNodeSeq> && ?a = rdf:rest) . } Limit 100",
		// "UTF8");
		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct { ?a ?c ?x .} { ?a lgdo:hasNodeList ?c . ?c rdf:rest ?x . } Limit 100",
		// "UTF8");
		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct { ?a ?b ?c .} { ?a ?b ?c . Filter(langMatches(lang(?c), 'de')) . Filter(regex(?c, 'upe')) . } Limit 100",
		// "UTF8");

		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct { ?a ?b ?c .} { ?a a lgdo:Relation . ?a a lgdo:TramRoute . ?a lgdo:hasMember ?x . ?x a lgdo:TramStop . } Limit 100",
		// "UTF8");

		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Select * { ?a ?t ?r . ?a ?t ?tr . ?a ?hm ?x . ?x ?t ?ts . Filter(?t = rdf:type && ?r = lgdo:Relation && ?tr = lgdo:TramRoute && ?hm = lgdo:hasMember && ?ts = lgdo:TramStop) . } Limit 100",
		// "UTF8");

		Point2D.Double a = new Point2D.Double(12.34593062612, 51.33298118419);
		Point2D.Double b = new Point2D.Double(12.404552986346, 51.348557018545);

		// Rectangle2D.Double c = new Rectangle2D.Double(a.x, a.y, b.x - a.x,
		// b.y - a.y);

		String polygon = "POLYGON((" + a.x + " " + a.y + "," + b.x + " " + a.y
				+ "," + b.x + " " + b.y + "," + a.x + " " + b.y + "," + a.x
				+ " " + a.y + "))";

		String qs = URLEncoder
				.encode("Prefix geo:<http://www.georss.org/georss/> Prefix ogc:<http://www.opengis.net/rdf#> Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Select * { ?a rdf:type lgdo:TramRoute . ?a lgdo:hasMember ?b . ?b a lgdo:TramStop . ?b rdfs:label ?l . ?b geo:geometry ?geo . Filter(ogc:intersects(?geo, ogc:geomFromText('"
						+ polygon + "'))) . } Limit 100", "UTF8");

		// What happens if we search for a specific triple?
		// qs =
		// URLEncoder.encode("Prefix geo:<http://www.georss.org/georss/> Prefix ogc:<http://www.opengis.net/rdf#> Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Select * { <http://linkedgeodata.org/resource/node/123> a lgdo:TramStop .}",
		// "UTF8");

		// qs =
		// URLEncoder.encode("Prefix geo:<http://www.georss.org/georss/> Prefix ogc:<http://www.opengis.net/rdf#> Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Select Distinct ?b ?l { ?a rdf:type lgdo:TramRoute . ?a lgdo:hasMember ?b . ?b a lgdo:TramStop . ?b rdfs:label ?l . ?b geo:geometry ?geo . } Limit 100",
		// "UTF8");
		qs = URLEncoder.encode("Select * {?s ?p ?o . } Limit 10");

		// qs =
		// URLEncoder.encode("DESCRIBE <http://linkedgeodata.org/resource/node/_20982927>",
		// "UTF8");

		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Select * { ?b rdfs:label ?l . Filter(langMatches(lang(?l), 'de')) .} Limit 100",
		// "UTF8");
		// String qs =
		// URLEncoder.encode("Prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> Prefix lgdo:<http://linkedgeodata.org/ontology/> Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Select * { ?b rdfs:label ?l .} Limit 3",
		// "UTF8");

		// Filter(?z = <http://linkedgeodata.org/resource/node/20974744>)

		// String qs =
		// URLEncoder.encode("Prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> Prefix owl:<http://www.w3.org/2002/07/owl#> Construct {?s ?p ?o } {?s ?p ?o . Filter(?o = 'Hotel') .}",
		// "UTF8");
		// URL test = new
		// URL("http://localhost:9999/sparql?query=Prefix+rdfs%3A%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E+Construct+%7B+%3Fs+rdfs%3Alabel+%3Fo+.+%7D+%7B+%3Fs+rdfs%3Alabel+%3Fo+.+%7D");

		/*
		 * URL test = new URL("http://localhost:9999/sparql?query=" + qs);
		 * 
		 * URLConnection connection = test.openConnection();
		 * //connection.setRequestProperty("Accept", "application/rdf+xml");
		 * //connection.setRequestProperty("Accept",
		 * MediaType.APPLICATION_JSON); connection.setRequestProperty("Accept",
		 * MediaType.TEXT_PLAIN);
		 * 
		 * System.out.println(connection.getRequestProperties());
		 * //connection.setRequestProperty("Accept", "text/plain");
		 * 
		 * StreamUtils.copyThenClose(test.openStream(), System.out);
		 */

		/*
		 * Client c = Client.create(); WebResource r =
		 * c.resource("http://localhost:9999/");
		 * System.out.println(r.get(String.class));
		 */
		// server.stop();
	}

}
