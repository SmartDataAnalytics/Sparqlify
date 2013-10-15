package org.aksw.service_framework.core;





/*
 * The differences to spring batch are 
 * - Services never finish, jobs do
 * - Instead of using a JobsParamater object, we use a bean directly (possibly a JPA entity)
 *     By this, we get a table holding the parameters, which we can easily
 *     expose via SPARQL (ideally even using auto mapping)
 *     
 *     We could create multiple versions of the same class (as an entity and as a config object),
 *     but this is what we want to avoid.
 * - A service execution holds a reference to the service configuration and to a
 *   service context, where the service context is again a bean /entity
 *
 * Spring batch uses essentially Maps for this purpose (JobParameters and ExecutionContext)
 * 
 * On the other hand, the question is whether we could create something like a
 * BeanExecutionContext.
 * 
 */

public class SparqlServiceManagerImpl
	implements SparqlServiceManager
{

}


/**
 * A bean that essentially wraps a map
 * 
 * 
 * 
 * @author raven
 *
 */
//public class SparqlServiceManagerImpl
//	implements SparqlServiceManager
//{
//	private static final Logger logger = LoggerFactory.getLogger(SparqlServiceManagerImpl.class);
//	
//	private SessionFactory sessionFactory;
//	
//	private Class<?> configClazz;
//	
//	public SparqlServiceManagerImpl(SessionFactory sessionFactory) {
//		this.sessionFactory = sessionFactory;
//	}
//	
//	
//	// Running services
//	private Map<String, ServiceExecution<QueryExecutionFactory>> nameToExecution = new HashMap<String, ServiceExecution<QueryExecutionFactory>>();
//
//	
//	public void resetServiceStates() {
//		Session session = sessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		List<?> states = session.createCriteria(Rdb2RdfExecution.class).list();
//		for(Object state : states) {
//			session.delete(state);
//		}
//
//		tx.commit();
//	}
//	
//	public void startAllServices() {
//		Session session = sessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		List<?> configs = session.createCriteria(Rdb2RdfConfig.class).list();
//		for(Object config : configs) {
//			Rdb2RdfConfig c = (Rdb2RdfConfig)config;
//			registerService(c);
//		}
//	}
//
//	public void stopService(String name) {
//		ServiceExecution<?> serviceExecution = nameToExecution.get(name);
//		try {
//			serviceExecution.close();
//			
//			Session session = sessionFactory.openSession();
//			Transaction tx = session.beginTransaction();
//		
//			Rdb2RdfExecution serviceState = (Rdb2RdfExecution) session.get(Rdb2RdfExecution.class, name);
//			serviceState.setStatus(ContextStateFlags.STOPPED);
//			session.update(serviceState);
//			
//			tx.commit();
//		}
//		catch(Exception e) {
//			logger.error("Failed to close service", e);
//		}		
//	}
//
//	public void unregisterService(String name) {
//		ServiceExecution<?> serviceExecution = nameToExecution.get(name);
//		try {
//			serviceExecution.close();
//			
//			//config =
//			
//			Session session = sessionFactory.openSession();
//			Transaction tx = session.beginTransaction();
//		
//			Object serviceState = session.get(Rdb2RdfExecution.class, name);
//			session.delete(serviceState);
//			//session.delete(arg0);
//			
//			tx.commit();
//		}
//		catch(Exception e) {
//			logger.error("Failed to close service", e);
//		}
//		
//		nameToExecution.remove(name);
//	}
//	
//	@Override
//	public void registerService(Rdb2RdfConfig serviceConfig) {
//		try {
//			createExecution(serviceConfig);
//		} catch (Exception e) {
//			logger.error("Something went wrong: ", e);
//		}
//	}
//
//	
//	public void createExecution(Rdb2RdfConfig serviceConfig) throws SQLException, IOException {
//
//		String serviceName = serviceConfig.getContextPath();
//		ServiceExecution<?> serviceExecution = nameToExecution.get(serviceName);
//		if(serviceExecution != null) {
//			throw new RuntimeException("A service with the name " + serviceName + " is already executing");
//		}
//
//		Session session = sessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//
//		Rdb2RdfExecution serviceState = new Rdb2RdfExecution();
//		serviceState.setName(serviceName);
//		serviceState.setStatus(ContextStateFlags.STARTING);
//		serviceState.getLogMessages().add(new LogMessage("info", "Starting service " + serviceName));
//
//		try {
//			//
//			
//			JdbcDataSource dsConfig = serviceConfig.getJdbcDataSource();
//	
//			//LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
//			//LoggerFactory.getLogger(this.getClass()).
//			
//			
//			BoneCPConfig c = new BoneCPConfig();
//			c.setUsername(dsConfig.getUsername());
//			c.setPassword(dsConfig.getPassword());
//			c.setJdbcUrl(dsConfig.getJdbcUrl());
//			
//			BoneCPDataSource dataSource = new BoneCPDataSource(c);
//		
//	
//			LoggerMem loggerMem = new LoggerMem(logger);
//			LoggerCount loggerCount = new LoggerCount(loggerMem);
//			
//			String smlConfigStr = serviceConfig.getTextResource().getData();
//	
//			Config smlConfig = SparqlifyUtils.parseSmlConfig(smlConfigStr, loggerCount);
//	
//			List<LogMessage> lm = Lists.transform(loggerMem.getLogEvents(), LogUtils.convertLog);
//			
//			
//			
//			if(loggerCount.getErrorCount() != 0) {
//				serviceState.getLogMessages().addAll(lm);
//				throw new RuntimeException("Errors encountered during parsing of the mapping");
//			}
//			
//	
//			QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, smlConfig, 1000l, 30);
//	
//			// A Test Query
//			qef.createQueryExecution("Prefix ex: <http://example.org/> Ask { ?s ex:b ex:c }");
//	
//			ServiceExecutionRdb2Rdf sparqlServiceExecution = new ServiceExecutionRdb2Rdf(serviceName, dataSource, qef);
//			
//			nameToExecution.put(serviceName, sparqlServiceExecution);
//			serviceState.setStatus(ContextStateFlags.RUNNING);
//
//		} catch(Exception e) {
//			serviceState.setStatus(ContextStateFlags.STOPPED);
//			serviceState.getLogMessages().add(new LogMessage("error", ExceptionUtils.getFullStackTrace(e)));
//		}
//		finally {
//			session.saveOrUpdate(serviceState);
//			tx.commit();
//			
//			session.close();
//		}
//		
//	}
//	
//}


