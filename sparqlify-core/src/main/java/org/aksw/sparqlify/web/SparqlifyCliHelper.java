package org.aksw.sparqlify.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.CommandLine;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

public class SparqlifyCliHelper {
	
	public static DataSource parseDataSource(CommandLine commandLine, Logger logger) {
		
		String hostName = commandLine.getOptionValue("h", "");
		String dbName = commandLine.getOptionValue("d", "");
		String userName = commandLine.getOptionValue("u", "");
		String passWord = commandLine.getOptionValue("p", "");

		
		String jdbcUrl = commandLine.getOptionValue("j", "");

		if(!jdbcUrl.isEmpty() && (!hostName.isEmpty() || !dbName.isEmpty())) {
			logger.error("Option 'j' is mutually exclusive with 'h' and 'd'");
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
		}
		
		BoneCPConfig cpConfig = new BoneCPConfig();
		
		if(jdbcUrl.isEmpty()) {
			cpConfig.setDatasourceBean(dataSourceBean);			
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
		
		cpConfig.setMinConnectionsPerPartition(1);
		cpConfig.setMaxConnectionsPerPartition(3);
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
			SchemaProvider schemaProvider = new SchemaProviderImpl(conn, typeSystem, typeAlias);
			SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

			result = SyntaxBridge.bridge(syntaxBridge, viewDefinitions, logger);
		}
		finally {
			conn.close();
		}

		return result;
	}

	public static Config parseSmlConfig(CommandLine commandLine, Logger logger) throws IOException, RecognitionException {
		String configFileStr = commandLine.getOptionValue("m");

		if (configFileStr == null) {
			logger.error("No mapping file given");
			return null;
		}

		File configFile = new File(configFileStr);
		if (!configFile.exists()) {
			logger.error("File does not exist: " + configFileStr);
			return null;
		}

		ConfigParser parser = new ConfigParser();

		InputStream in = new FileInputStream(configFile);
		Config config = null;
		try {
			config = parser.parse(in, logger);
		} finally {
			in.close();
		}

		return config;
	}
}