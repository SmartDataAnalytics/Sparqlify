package org.aksw.sparqlify.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.core.utils.RDFDataMgrEx;
import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.obda.jena.r2rml.impl.R2rmlImporter;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.validation.LoggerCount;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SparqlifyCliHelper {

	public static final ResourceLoader resourceLoader = new AnnotationConfigApplicationContext();
    //public static final ApplicationContext appContext = new AnnotationConfigApplicationContext();
	//public static final ResourceLoader resourceLoader = new FileSystemResourceLoader(); //new DefaultResourceLoader(SparqlifyCliHelper.class.getClassLoader()); //new PathMatchingResourcePatternResolver();
	
    public static void addDatabaseOptions(Options cliOptions) {
        cliOptions.addOption("t", "type", true,
                "Database type (posgres, mysql,...)");
        cliOptions.addOption("d", "database", true, "Database name");
        cliOptions.addOption("p", "port", true, "Database port");
        cliOptions.addOption("U", "username", true, "Database username");
        cliOptions.addOption("W", "password", true, "Database password");
        cliOptions.addOption("h", "hostname", true, "Database hostname");
        cliOptions.addOption("c", "class", true, "JDBC driver class");
        cliOptions.addOption("j", "jdbcurl", true, "JDBC URL");
    }

    public static DataSource parseDataSource(CommandLine commandLine, Logger logger) {

        String hostName = commandLine.getOptionValue("h", "");
        String dbName = commandLine.getOptionValue("d", "");
        String userName = commandLine.getOptionValue("U", "");
        String passWord = commandLine.getOptionValue("W", "");
        String portStr = commandLine.getOptionValue("p", "").trim();


        String jdbcUrl = commandLine.getOptionValue("j", "");

        if(!jdbcUrl.isEmpty() && (!hostName.isEmpty() || !dbName.isEmpty() || !portStr.isEmpty())) {
            logger.error("Option 'j' is mutually exclusive with 'h' and 'd' and 'p'");
            return null;
        }

        if(jdbcUrl.isEmpty() && hostName.isEmpty()) {
            hostName = "localhost";
        }

        /*
         * Connection Pool
         */

        PGSimpleDataSource dataSourceBean = null;

        if(jdbcUrl.isEmpty()) {
            dataSourceBean = new PGSimpleDataSource();

            dataSourceBean.setDatabaseName(dbName);
            dataSourceBean.setServerName(hostName);
            dataSourceBean.setUser(userName);
            dataSourceBean.setPassword(passWord);

            if(!portStr.isEmpty()) {
                int port = Integer.parseInt(portStr);
                dataSourceBean.setPortNumber(port);
            }
        }

        
        HikariConfig cpConfig = new HikariConfig();

        if(jdbcUrl.isEmpty()) {
            cpConfig.setDataSource(dataSourceBean);
        } else {
            cpConfig.setJdbcUrl(jdbcUrl);
            cpConfig.setUsername(userName);
            cpConfig.setPassword(passWord);
        }

        /*
        cpConfig.setJdbcUrl(dbconf.getDbConnString()); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
        cpConfig.setUsername(dbconf.getUsername());
        cpConfig.setPassword(dbconf.getPassword());
        */

//        cpConfig.parsetMinConnectionsPerPartition(2);
//        cpConfig.setMaxConnectionsPerPartition(8);
//        cpConfig.setConnectionTimeoutInMs(5000);
//		cpConfig.setMinConnectionsPerPartition(1);
//		cpConfig.setMaxConnectionsPerPartition(1);

//        cpConfig.setPartitionCount(1);
        //BoneCP connectionPool = new BoneCP(cpConfig); // setup the connection pool

        HikariDataSource dataSource = new HikariDataSource(cpConfig);

        return dataSource;
    }

//
//    public static List<ViewDefinition> extractViewDefinitions(List<org.aksw.sparqlify.config.syntax.ViewDefinition> viewDefinitions, DataSource dataSource, TypeSystem typeSystem, Map<String, String> typeAlias, Logger logger) throws SQLException {
////		Connection conn;
////		ViewDefinitionFactory x = SparqlifyUtils.createViewDefinitionFactory(conn, typeSystem, typeAlias);
//
//        List<ViewDefinition> result;
//
//        Connection conn = dataSource.getConnection();
//        try {
//            SchemaProvider schemaProvider = new SchemaProviderImpl(conn, typeSystem, typeAlias, sqlEscaper);
//            SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
//
//            result = SyntaxBridge.bridge(syntaxBridge, viewDefinitions, logger);
//        }
//        finally {
//            conn.close();
//        }
//
//        return result;
//    }

    public static Integer parseInt(CommandLine commandLine, String optName, boolean mustExist, Logger logger) {
        String valueStr = commandLine.getOptionValue(optName);

        Integer result = null;
        if(valueStr == null) {
            if(mustExist) {
                logger.error("Argument required for option '" + optName + "'"); //"No mapping file given");
            }
        } else {

            try {
                 result = Integer.parseInt(valueStr);
            } catch(Exception e) {
                logger.error("Integer value expected for argument '" + optName + "', got '" + valueStr + "' instead.");
            }
        }

        return result;
    }

    public static List<Resource> parseFiles(CommandLine commandLine, String optName, boolean mustExist, Logger logger) throws IOException {
        String[] locations = commandLine.getOptionValues(optName);

        if (locations == null || locations.length == 0) {
            logger.error("File or folder name required for option '" + optName + "'"); //"No mapping file given");
            return null;
        }

        //System.out.println("Workdir: " + System.getProperty("user.dir"));
        List<Resource> result = new ArrayList<Resource>();
        for(String location : locations) {
            Resource resource = resourceLoader.getResource(location);
//            File file = resource.getFile();
//            if(file != null) {
//            	file = file.getAbsoluteFile();
//            	System.out.println(file);
//            }
            
            if(!resource.exists()) {
                Resource fallback = resourceLoader.getResource("file:" + location);
                if(fallback.exists()) {
                    resource = fallback;
                }
            }

            //File file = new File(fileName);
            if (mustExist && !resource.exists()) {
                logger.error("Resource does not exist: " + location);
                return null;
            }

            result.add(resource);
        }

        return result;
    }

    public static File parseFile(CommandLine commandLine, String optName, boolean mustExist, Logger logger) {
        String fileName = commandLine.getOptionValue(optName);

        if (fileName == null) {
            logger.error("File or folder name required for option '" + optName + "'"); //"No mapping file given");
            return null;
        }

        File file = new File(fileName);
        if (mustExist && !file.exists()) {
            logger.error("File does not exist: " + fileName);
            return null;
        }

        return file;
    }

    /**
     * Returns a single configuration created from multiple files
     *
     * @param commandLine
     * @param logger
     * @return
     * @throws IOException
     * @throws RecognitionException
     */
    public static Config parseSmlConfigs(CommandLine commandLine, Logger logger) throws IOException, RecognitionException {
        List<Resource> configFiles = parseFiles(commandLine, "m", true, logger);
        if(configFiles == null) {
            return null;
        }
        

    	R2rmlImporter r2rmlImporter = new R2rmlImporter();
        Config result = new Config();
        for(Resource configFile : configFiles) {
        	
        	Config contrib = null;
        	String uri = Objects.toString(configFile.getURI(), null);
        	if(uri != null) {
                Lang lang = RDFDataMgr.determineLang(uri, null, null);
                if(lang != null) {
                	logger.info("Loading as R2RML: " + configFile);
                	Model model = RDFDataMgr.loadModel(uri);
                	RDFDataMgrEx.execSparql(model, "r2rml-inferences.sparql");
                	
                	r2rmlImporter.validate(model);
                    Collection<ViewDefinition> views = r2rmlImporter.read(model);
                    contrib = new Config();
                    contrib.setViewDefinitions(new ArrayList<>(views));
                }
        	}

        	if(contrib == null) {
            	logger.info("Loading as SMS: " + configFile);
                try(InputStream in = configFile.getInputStream()) {
                    contrib = SparqlifyUtils.parseSmlConfig(in, logger);
                }
        	}

        	if(contrib != null) {
        		result.merge(contrib);
        	}
        }

        return result;
    }
    public static Config parseSmlConfig(CommandLine commandLine, Logger logger) throws IOException, RecognitionException {
        File configFile = parseFile(commandLine, "m", true, logger);
        if(configFile == null) {
            return null;
        }

//		String configFileStr = commandLine.getOptionValue("m");
//
//		if (configFileStr == null) {
//			logger.error("No mapping file given");
//			return null;
//		}
//
//		File configFile = new File(configFileStr);
//		if (!configFile.exists()) {
//			logger.error("File does not exist: " + configFileStr);
//			return null;
//		}

        InputStream in = new FileInputStream(configFile);
        Config result = SparqlifyUtils.parseSmlConfig(in, logger);

        return result;
    }

    public static void onErrorPrintHelpAndExit(Options cliOptions, LoggerCount loggerCount, int exitCode) {

        if(loggerCount.getErrorCount() != 0) {
            //logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());

            SparqlifyCliHelper.printHelpAndExit(cliOptions, exitCode);

            throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
        }

    }

    /**
     * @param exitCode
     */
    public static void printHelpAndExit(Options cliOptions, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(SparqlifyCliHelper.class.getName(), cliOptions);
        System.exit(exitCode);
    }
}