package org.aksw.sparqlify.web;

import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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


        Server server = new Server(port);
        //SocketConnector connector = new SocketConnector();

        // Set some timeout options to make debugging easier.
//        connector.setMaxIdleTime(1000 * 60 * 60);
//        connector.setSoLingerTime(-1);
//        connector.setPort(port);
//        server.setConnectors(new Connector[] { connector });

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
}
