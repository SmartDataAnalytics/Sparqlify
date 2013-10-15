package org.aksw.service_framework.core;

import java.util.List;

import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.service_framework.utils.LogUtils;
import org.aksw.sparqlify.admin.model.JdbcDataSource;
import org.aksw.sparqlify.admin.model.LogMessage;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;
import org.aksw.sparqlify.admin.web.common.ContextStateFlags;
import org.aksw.sparqlify.admin.web.common.EntityHolder;
import org.aksw.sparqlify.admin.web.common.LoggerMem;
import org.aksw.sparqlify.admin.web.common.ServiceExecutionRdb2Rdf;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

public class ServiceLauncherRdb2Rdf
	implements ServiceLauncher<Rdb2RdfConfig, QueryExecutionFactory, Rdb2RdfExecution>
{
	private static final Logger logger = LoggerFactory.getLogger(SparqlServiceManagerImpl.class);

	
	@Override
	public ServiceExecution<QueryExecutionFactory> launch(Rdb2RdfConfig serviceConfig, EntityHolder<Rdb2RdfExecution> context) {

//		String serviceName = serviceConfig.getContextPath();
//		ServiceExecution<?> serviceExecution = nameToExecution.get(serviceName);
//		if(serviceExecution != null) {
//			throw new RuntimeException("A service with the name " + serviceName + " is already executing");
//		}
		String serviceName = "foobar";
		Rdb2RdfExecution serviceState = context.getEntity();
		
		//serviceState.setName(serviceName);
		serviceState.setStatus(ContextStateFlags.STARTING);
		serviceState.getLogMessages().add(new LogMessage("info", "Starting service " + serviceName));

		context.save();
		
		
		ServiceExecution<QueryExecutionFactory> result = null;
		
		try {
			//
			
			JdbcDataSource dsConfig = serviceConfig.getJdbcDataSource();
	
			//LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
			//LoggerFactory.getLogger(this.getClass()).
			
			
			BoneCPConfig c = new BoneCPConfig();
			c.setUsername(dsConfig.getUsername());
			c.setPassword(dsConfig.getPassword());
			c.setJdbcUrl(dsConfig.getJdbcUrl());
			
			BoneCPDataSource dataSource = new BoneCPDataSource(c);
		
	
			LoggerMem loggerMem = new LoggerMem(logger);
			LoggerCount loggerCount = new LoggerCount(loggerMem);
			
			String smlConfigStr = serviceConfig.getTextResource().getData();
	
			Config smlConfig = SparqlifyUtils.parseSmlConfig(smlConfigStr, loggerCount);
	
			List<LogMessage> lm = Lists.transform(loggerMem.getLogEvents(), LogUtils.convertLog);
			
			
			
			if(loggerCount.getErrorCount() != 0) {
				serviceState.getLogMessages().addAll(lm);
				throw new RuntimeException("Errors encountered during parsing of the mapping");
			}
			
	
			QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, smlConfig, 1000l, 30);
	
			// A Test Query
			qef.createQueryExecution("Prefix ex: <http://example.org/> Ask { ?s ex:b ex:c }");
	
			result = new ServiceExecutionRdb2Rdf(serviceName, dataSource, qef);
			
			//nameToExecution.put(serviceName, sparqlServiceExecution);
			serviceState.setStatus(ContextStateFlags.RUNNING);

		} catch(Exception e) {
			serviceState.setStatus(ContextStateFlags.STOPPED);
			serviceState.getLogMessages().add(new LogMessage("error", ExceptionUtils.getFullStackTrace(e)));
		}
		finally {
			context.save();
		}
		
		return result;
	}
}