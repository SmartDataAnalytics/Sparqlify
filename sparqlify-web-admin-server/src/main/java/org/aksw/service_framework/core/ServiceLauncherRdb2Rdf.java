package org.aksw.service_framework.core;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.service_framework.jpa.core.ServiceProvider;
import org.aksw.service_framework.jpa.core.ServiceProviderJpaRdbRdf;
import org.aksw.service_framework.utils.LogUtils;
import org.aksw.sparqlify.admin.model.JdbcDataSource;
import org.aksw.sparqlify.admin.model.LogMessage;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;
import org.aksw.sparqlify.admin.web.common.ContextStateFlags;
import org.aksw.sparqlify.admin.web.common.LoggerMem;
import org.aksw.sparqlify.admin.web.common.ServiceProviderRdb2Rdf;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;


public class ServiceLauncherRdb2Rdf
	implements ServiceLauncher<Rdb2RdfConfig, Rdb2RdfExecution, QueryExecutionFactory>
{
	private static final Logger logger = LoggerFactory.getLogger(ServiceLauncherRdb2Rdf.class);

	
	@Override
	//public ServiceExecution<QueryExecutionFactory> launch(EntityManagerFactory emf, Rdb2RdfConfig serviceConfig, Rdb2RdfExecution context, boolean isRestart) {
	public ServiceProvider<QueryExecutionFactory> launch(EntityManagerFactory emf, Rdb2RdfConfig serviceConfig, Rdb2RdfExecution context, boolean isRestart) {
		
//		String serviceName = serviceConfig.getContextPath();
//		ServiceExecution<?> serviceExecution = nameToExecution.get(serviceName);
//		if(serviceExecution != null) {
//			throw new RuntimeException("A service with the name " + serviceName + " is already executing");
//		}
		String serviceName = "foobar";

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		Object configId = emf.getPersistenceUnitUtil().getIdentifier(serviceConfig);
		Object executionContextId = emf.getPersistenceUnitUtil().getIdentifier(context);
		

		serviceConfig = em.find(Rdb2RdfConfig.class, configId);
		context = em.find(Rdb2RdfExecution.class, executionContextId);
		
//		em.merge(context);
//		em.merge(serviceConfig);

		//Rdb2RdfExecution serviceState = context.getEntity();
		
		//serviceState.setName(serviceName);
		context.setStatus(ContextStateFlags.STARTING);
		context.getLogMessages().clear();
		context.getLogMessages().add(new LogMessage("info", "Starting service " + serviceName));
		context.setConfig(serviceConfig);

		em.getTransaction().commit();
		em.getTransaction().begin();
		//context.commit();

		//context.openSession();
		
		
		ServiceProvider<QueryExecutionFactory> result = null;
		
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
				context.getLogMessages().addAll(lm);
				throw new RuntimeException("Errors encountered during parsing of the mapping");
			}
			
	
			QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, smlConfig, 1000l, 30);
	
			// A Test Query
			qef.createQueryExecution("Prefix ex: <http://example.org/> Ask { ?s ex:b ex:c }");
	
			result = new ServiceProviderRdb2Rdf(serviceName, dataSource, qef);
	
			result = new ServiceProviderJpaRdbRdf<QueryExecutionFactory>(result, emf, context);
			
			//nameToExecution.put(serviceName, sparqlServiceExecution);
			context.setStatus(ContextStateFlags.RUNNING);
			context.getLogMessages().add(new LogMessage("info", "Service successfully started."));

		} catch(Exception e) {
			context.setStatus(ContextStateFlags.STOPPED);
			context.getLogMessages().add(new LogMessage("error", ExceptionUtils.getFullStackTrace(e)));
			context.getLogMessages().add(new LogMessage("info", "Service failed to start."));
		}
		finally {
			em.getTransaction().commit();
			em.close();
			//context.commit();
		}
		
		return result;
	}
}
