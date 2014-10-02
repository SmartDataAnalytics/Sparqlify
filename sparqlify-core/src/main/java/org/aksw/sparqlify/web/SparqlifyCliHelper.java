package org.aksw.sparqlify.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderSql92;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

public class SparqlifyCliHelper {

    public static void addDatabaseOptions(Options cliOptions) {
        cliOptions.addOption("t", "type", true,
                "Database type (posgres, mysql,...)");
        cliOptions.addOption("e", "database type", true, "Database type (default: postgres)");
        cliOptions.addOption("p", "port", true, "");
        cliOptions.addOption("U", "username", true, "");
        cliOptions.addOption("W", "password", true, "");
        cliOptions.addOption("h", "hostname", true, "");
        cliOptions.addOption("c", "class", true, "JDBC driver class");
        cliOptions.addOption("j", "jdbcurl", true, "JDBC URL");
    }

    public static DataSource parseDataSource(CommandLine commandLine, Logger logger) {

        String dbType = commandLine.getOptionValue("e", "");
        String hostName = commandLine.getOptionValue("h", "");
        String dbName = commandLine.getOptionValue("d", "");
        String userName = commandLine.getOptionValue("U", "");
        String passWord = commandLine.getOptionValue("W", "");
        String portStr = commandLine.getOptionValue("p", ""); //.trim();


        String jdbcUrl = commandLine.getOptionValue("j", "");

        if(!jdbcUrl.isEmpty() && (!dbType.isEmpty() || !hostName.isEmpty() || !dbName.isEmpty() || !portStr.isEmpty())) {
            logger.error("Option 'j' is mutually exclusive with 'e', 'h', 'd' and 'p'");
            return null;
        }

        if(jdbcUrl.isEmpty() && hostName.isEmpty()) {
            dbType = "postgres";
            hostName = "localhost";
        }

        /*
         * Connection Pool
         */

/*
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
*/

        BoneCPConfig cpConfig = new BoneCPConfig();

        if(jdbcUrl.isEmpty()) {
            String p = portStr.isEmpty() ? "" : ""  + portStr;
            jdbcUrl = "jdbc:" + dbType + "://" + hostName + p + "/" + dbName;
        }

        logger.debug("Created JDBC Url: " + jdbcUrl);

        cpConfig.setJdbcUrl(jdbcUrl);
        cpConfig.setUsername(userName);
        cpConfig.setPassword(passWord);


        /*
        cpConfig.setJdbcUrl(dbconf.getDbConnString()); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
        cpConfig.setUsername(dbconf.getUsername());
        cpConfig.setPassword(dbconf.getPassword());
        */

        cpConfig.setMinConnectionsPerPartition(2);
        cpConfig.setMaxConnectionsPerPartition(8);
        cpConfig.setConnectionTimeoutInMs(5000);
//		cpConfig.setMinConnectionsPerPartition(1);
//		cpConfig.setMaxConnectionsPerPartition(1);

        cpConfig.setPartitionCount(1);
        //BoneCP connectionPool = new BoneCP(cpConfig); // setup the connection pool

        BoneCPDataSource dataSource = new BoneCPDataSource(cpConfig);

        return dataSource;
    }


    public static List<ViewDefinition> extractViewDefinitions(List<org.aksw.sparqlify.config.syntax.ViewDefinition> viewDefinitions, DataSource dataSource, TypeSystem typeSystem, Map<String, String> typeAlias, Logger logger) throws SQLException {
//		Connection conn;
//		ViewDefinitionFactory x = SparqlifyUtils.createViewDefinitionFactory(conn, typeSystem, typeAlias);

        List<ViewDefinition> result;

        Connection conn = dataSource.getConnection();
        try {
            SchemaProvider schemaProvider = new SchemaProviderSql92(conn, typeSystem);
            SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

            result = SyntaxBridge.bridge(syntaxBridge, viewDefinitions, logger);
        }
        finally {
            conn.close();
        }

        return result;
    }

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

    public static List<File> parseFiles(CommandLine commandLine, String optName, boolean mustExist, Logger logger) {
        String[] fileNames = commandLine.getOptionValues(optName);

        if (fileNames == null || fileNames.length == 0) {
            logger.error("File or folder name required for option '" + optName + "'"); //"No mapping file given");
            return null;
        }

        List<File> result = new ArrayList<File>();
        for(String fileName : fileNames) {
            File file = new File(fileName);
            if (mustExist && !file.exists()) {
                logger.error("File does not exist: " + fileName);
                return null;
            }

            result.add(file);
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
        List<File> configFiles = parseFiles(commandLine, "m", true, logger);
        if(configFiles == null) {
            return null;
        }

        Config result = null;
        for(File configFile : configFiles) {
            InputStream in = new FileInputStream(configFile);
            Config tmp = SparqlifyUtils.parseSmlConfig(in, logger);
            if(result == null) {
                result = tmp;
            } else {
                result.merge(tmp);
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
}