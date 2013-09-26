package org.aksw.sparqlify.platform.web;

import java.net.URL;
import java.security.ProtectionDomain;

import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.web.HttpSparqlEndpoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.container.servlet.ServletContainer;


/**
 * 
 * 
 * http://stackoverflow.com/questions/10738816/deploying-a-servlet-
 * programmatically-with-jetty
 * http://stackoverflow.com/questions/3718221/add-resources
 * -to-jetty-programmatically
 * 
 * @author raven
 * 
 * 
 */
public class MainSparqlifyPlatform {
	
	private static final Logger logger = LoggerFactory.getLogger(MainSparqlifyPlatform.class);
	
	// Source:
	// http://eclipsesource.com/blogs/2009/10/02/executable-wars-with-jetty/
	public static void main(String[] args) throws Exception {
		int port = 7531;

		System.getProperties().setProperty("configDirectory", "/home/raven/Projects/Eclipse/sparqlify-parent/sparqlify-platform/config/example/sparqlify-platform/");

		
		Server server = new Server();
		SocketConnector connector = new SocketConnector();

		// Set some timeout options to make debugging easier.
		connector.setMaxIdleTime(1000 * 60 * 60);
		connector.setSoLingerTime(-1);
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		WebAppContext context = new WebAppContext();
		context.setServer(server);
		context.setContextPath("/");

		ProtectionDomain protectionDomain = MainSparqlifyPlatform.class.getProtectionDomain();
		URL location = protectionDomain.getCodeSource().getLocation();
		String externalForm = location.toExternalForm();
		
		// Try to detect whether we are being run from an
		// archive (uber jar) or just from compiled classes
		if(externalForm.endsWith("/classes/")) {
			externalForm = "src/main/webapp";
		}
		
		
		logger.debug("Loading webAppContext from " + externalForm);
		context.setWar(externalForm);

		server.setHandler(context);
		try {
			server.start();
			System.in.read();
			server.stop();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}

	// public static void main(String[] args) throws Exception {
	//
	// // Server server = new Server(8090);
	// //
	// // ServletContextHandler context = new
	// ServletContextHandler(ServletContextHandler.SESSIONS);
	// // context.setContextPath("/batch");
	// //
	// // // Setup Spring context
	// // context.addEventListener(new ContextLoaderListener());
	// // context.setInitParameter("contextConfigLocation",
	// "classpath*:**/testContext.xml");
	// //
	// // server.setHandler(context);
	// //
	// // // Add servlets
	// // context.addServlet(new ServletHolder(new BatchReceiver()),
	// "/receiver/*");
	// // context.addServlet(new ServletHolder(new BatchSender()), "/sender/*");
	// //
	// // server.start();
	// // server.join();
	// //
	// int port = 7531;
	//
	// System.getProperties().setProperty("configDirectory",
	// "/home/raven/Projects/Eclipse/sparqlify-parent/sparqlify-platform/config/example/sparqlify-platform/");
	//
	// WebAppContext webAppContext = new WebAppContext();
	// webAppContext.setContextPath("/");
	// webAppContext.setWar("src/main/webapp");
	//
	//
	//
	//
	// Server server = new Server(port);
	// server.setHandler(webAppContext);
	//
	// //ServletContextHandler context = new ServletContextHandler(server, "/",
	// ServletContextHandler.SESSIONS);
	//
	// // context.addServlet(webAppContext, "/");
	// // server.addHandler(webAppContext);
	// // server.start();
	// //
	// //
	// //
	// // //context.getServletContext().setAttribute("queryExecutionFactory",
	// qef);
	// // context.addServlet(sh, "/*");
	//
	//
	// server.start();
	// server.join();
	//
	// //return server;
	//
	// }

	public static Server createSparqlEndpoint(QueryExecutionFactoryEx qef,
			int port) throws Exception {
		HttpSparqlEndpoint.sparqler = qef;

		ServletHolder sh = new ServletHolder(ServletContainer.class);

		// http://stackoverflow.com/questions/805280/loading-up-a-web-xml-for-integration-tests-with-jetty
		// WebAppContext webAppContext = new WebAppContext();
		// webAppContext.setContextPath("/");
		// webAppContext.setWar();

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
		ServletContextHandler context = new ServletContextHandler(server, "/",
				ServletContextHandler.SESSIONS);

		context.getServletContext().setAttribute("queryExecutionFactory", qef);
		context.addServlet(sh, "/*");

		return server;
	}
}
