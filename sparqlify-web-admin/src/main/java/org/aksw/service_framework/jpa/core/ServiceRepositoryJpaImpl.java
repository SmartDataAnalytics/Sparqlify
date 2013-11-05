package org.aksw.service_framework.jpa.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.aksw.service_framework.core.ServiceLauncher;
import org.aksw.service_framework.jpa.model.ConfigToExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



//class ServiceEvent<C, E, S> {
//	
//	private Class<S> configClass;
//	private Class<E> executionContextClass;
//	private ServiceControl<S> serviceCtrl;
//	private ServiceState state; 
//	
//	public ServiceEvent(Class<S> configClass, Class<E> executionContextClass, ServiceControl<S> serviceCtrl, ServiceState state) {
//		this.configClass = configClass;
//		this.executionContextClass = executionContextClass;
//		this.serviceCtrl = serviceCtrl;
//		this.state = state;
//	}
//
//	public Class<S> getConfigClass() {
//		return configClass;
//	}
//
//	public Class<E> getExecutionContextClass() {
//		return executionContextClass;
//	}
//
//	public ServiceControl<S> getServiceCtrl() {
//		return serviceCtrl;
//	}
//
//	public ServiceState getState() {
//		return state;
//	}	
//}




/**
 * A service framework based on JPA
 * 
 * There is assumed to be a set of service configuration objects of type C.
 * Upon initialization of the repository, each object c of C will be used to spawn a service as follows:
 *
 * - First, a service execution context e of type E will be obtained: If such e already exists,
 *   such as from a prior execution of c, it will be reused. Otherwise a new instance of E will be created.
 * - A launcher will be invoked with c and e.
 * - The result of a launches is a ServiceProvider which has a getService() and a close() method
 *   - If the launcher fails to start the service for whatever reason, the provider is null
 *   
 * - The repository exposes the service via a ServiceControl object, that allows retrieving the service
 *  (via getService()) as well as stopping and starting the service.
 * 
 * Notes:
 * - The launcher is invoked for every config object, and it is up to the launcher to decide whether
 * to return a serviceProvider object or null. This means, enabling/disabling services must be part of the config object.
 * 
 * TODO: We need to distinguish between a disabled service (i.e. don't bring the service up on restart)
 * and a service that could not be brought up due to an error (i.e. retry to start on restart) 
 * In other words: Services need a 'start on boot' flag, which could be part of the cte table.
 * 
 * TODO finish below...
 * We actually keep track of start/stopped status in the cte table.
 * However, if upon launch a prior executionContext is found, the service
 * 
 * @author raven
 *
 */
public class ServiceRepositoryJpaImpl<C, E, S>
	implements ServiceRepositoryJpa<S>
{
	private static final Logger logger = LoggerFactory.getLogger(ServiceRepositoryJpaImpl.class);
	
	private EntityManagerFactory emf;

	//private Class<S> serviceClass;
	private Class<E> executionContextClass;
	private Class<C> configClass;

	private ServiceLauncher<C, E, S> serviceLauncher;	

	
	private Map<Object, ServiceControlJpaImpl<C, E, S>> configIdToControl = new HashMap<Object, ServiceControlJpaImpl<C, E, S>>();
	
	private Map<Object, ServiceState> configIdToState = new HashMap<Object, ServiceState>();
	private Map<Object, ServiceState> executionIdToState = new HashMap<Object, ServiceState>();
	private Map<Object, ServiceState> executionContextIdToState = new HashMap<Object, ServiceState>();
	
	// Maybe: executionContextIdToConfigId
	// ConfigId to
	
	private Map<Object, ServiceProvider<S>> configIdToProvider = new HashMap<Object, ServiceProvider<S>>();
	
	// We need a mapping from config to execution
	
	private List<ServiceEventListener<C, E, S>> eventListerners = new ArrayList<ServiceEventListener<C,E,S>>();
	

	public List<ServiceEventListener<C, E, S>> getServiceEventListeners() {
		return eventListerners;
	}
	
	
	
	public ServiceRepositoryJpaImpl(
			EntityManagerFactory emf,
			Class<C> configClass,
			Class<E> executionContextClass,
			//Class<S> serviceClass,
			ServiceLauncher<C, E, S> serviceLauncher)
	{
		this.emf = emf;

		this.configClass = configClass;
		this.executionContextClass = executionContextClass;
		this.serviceLauncher = serviceLauncher;
		
//		executionContextFactory = new ServiceExecutionContextFactoryJpa<E>(emf, executionContextClass);
	}

	public static <C, E, S> ServiceRepositoryJpaImpl<C, E, S> create(
		EntityManagerFactory emf,
		Class<C> configClass,
		Class<E> executionContextClass,
		ServiceLauncher<C, E, S> serviceLauncher
	)
	{
		ServiceRepositoryJpaImpl<C, E, S> result = new ServiceRepositoryJpaImpl<C, E, S>(emf, configClass, executionContextClass, serviceLauncher);
		return result;
	}
	
	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		return this.emf;
	}
	

	public void startAll() {
		startAllServices();
	}
	
//	@SuppressWarnings("unchecked")
//	public static <T> List<T> listAll(Session session, Class<T> clazz) {
//		List<?> tmp = session.createCriteria(clazz).list();
//		List<T> result = new ArrayList<T>();
//		for(Object o : tmp) {
//			result.add((T)o);
//		}
//		
//		return result;
//	}
	
	public static <T> T getEntity(EntityManagerFactory emf, Class<T> clazz, Object id) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		T result = em.find(clazz, id);
		
		em.getTransaction().commit();
		em.close();

		return result;
	}
	
	public static <T> List<T> listAll(EntityManagerFactory emf, Class<T> clazz) {
		List<T> result = null;

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		try {
			result = ServiceRepositoryJpaImpl.listAll(em, clazz);
		} finally {
			em.close();
		}

		return result;
	}

	public static <T> List<T> deleteAll(EntityManagerFactory emf, Class<T> clazz) {
		List<T> result = null;

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		try {
			deleteAll(em, clazz);
		} finally {
			em.close();
		}

		return result;
	}

	public static <T> List<T> listAll(EntityManager em, Class<T> clazz) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> r = cq.from(clazz);
		cq.select(r);
		
		List<T> result = em.createQuery(cq).getResultList();
		
		return result;
	}
	
	public static void deleteAll(EntityManager em, Class<?> clazz) {
		List<?> items = listAll(em, clazz);
		for(Object item : items) {
			em.remove(item);
		}
	}

	
//	public static void deleteAll(Session session, Class<?> clazz) {
//		List<?> states = session.createCriteria(clazz).list();
//		for(Object state : states) {
//			session.delete(state);
//		}
//	}
	
	public List<C> getConfigs() {
		List<C> result = ServiceRepositoryJpaImpl.listAll(emf, configClass);
		return result;
	}

	public List<E> getExecutionContexts() {
		List<E> result = ServiceRepositoryJpaImpl.listAll(emf, executionContextClass);
		return result;
	}


	// Note: Only invoke after having killed the services first
	// Should probably be a private method
	public void deleteExecutionContexts() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		deleteAll(em, executionContextClass);
		
		em.getTransaction().commit();
		em.close();
	}
	
	/**
	 * Stops a service and removes its execution record
	 * 
	 * @param configId
	 */
	public void deleteByConfigId(Object configId) {
		ServiceState state = configIdToState.get(configId);
		
		stopByConfigId(configId);
		

		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		List<ConfigToExecution> ctes = getMappings(em, configId);
		
		E executionContext = em.find(executionContextClass, state.getExecutionContextId());
		em.remove(executionContext);
		
		
		for(ConfigToExecution cte : ctes) {
			em.remove(cte);
		}
		
		em.getTransaction().commit();
		em.close();
	}
	
	
	public List<ConfigToExecution> getMappings(EntityManager em, Object configId) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		
		CriteriaQuery<ConfigToExecution> cq = cb.createQuery(ConfigToExecution.class);
		Root<ConfigToExecution> r = cq.from(ConfigToExecution.class);
		
		cq.where(
			cb.equal(r.get("configClassName"), configClass.getName()),
			cb.equal(r.get("configIdStr"), "" + configId),
			cb.equal(r.get("executionContextClassName"), executionContextClass.getName())
		);
		
		cq.select(r);
		
		List<ConfigToExecution> result = em.createQuery(cq).getResultList();

		return result;
	}
	
	/**
	 * Note: There should always be only at most one execution context,
	 * but for dealing with abnormalities we allow a list to be returned.
	 * 
	 * The idea is basically, that if there are multiple contexts, then
	 * the database is messed up and we can just delete the contexts.
	 * 
	 * @param configId
	 * @return
	 */
	public static List<Integer> getIds(List<ConfigToExecution> mappings) {
		List<Integer> result = new ArrayList<Integer>(mappings.size());
		for(ConfigToExecution mapping : mappings) {
			result.add(mapping.getId());
		}
		return result;
	}
	
	public Map<Integer, E> getExecutions(EntityManager em, List<Integer> ids)
	{
		Map<Integer, E> result = new HashMap<Integer, E>();
		
	
		for(Integer id : ids) {
			E item = em.find(executionContextClass, id);
			result.put(id, item);
		}		
		
		return result;
	}
	

	public static <T> T newEntity(EntityManager em, Class<T> clazz) {
		
		T result;
		if(clazz.equals(Void.class)) {
			result = null;
		}
		else {
			try {
				 result = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			
			em.persist(result);
			em.flush();
		}		
		return result;
	}
	
//	public static Object deserialize(byte[] data) {
//		try {
//			ByteArrayInputStream bain = new ByteArrayInputStream(data);
//			ObjectInputStream oin = new ObjectInputStream(bain);
//			Object result = oin.readObject();
//			oin.close();
//			return result;
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
//	public static byte[] serialize(Object o) {
//		try {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(baos);
//			oos.writeObject(o);
//	
//			oos.flush();
//			oos.close();
//			
//			byte[] result = baos.toByteArray();
//			return result;
//		} catch(Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//	
	public ConfigToExecution newCte(EntityManager em, Object configId, E executionContext) {

		Object executionContextId;
		if(executionContext == null) {
			executionContextId = -1;
		} else {
			executionContextId = emf.getPersistenceUnitUtil().getIdentifier(executionContext);
		}

		
		ConfigToExecution result = new ConfigToExecution();
		result.setConfigClassName(configClass.getName());
		result.setConfigId((Serializable)configId);
		//result.setConfigId(serialize(configId));
		result.setConfigIdStr(configId.toString());
		
		result.setExecutionContextClassName(executionContextClass.getName());
		result.setExecutionContextId((Serializable)executionContextId);
		//result.setExecutionContextId(serialize(executionContextId));
		result.setExecutionContextIdStr(executionContextId.toString());
		
		
		em.persist(result);
		em.flush();
		
		return result;
	}

	/**
	 * TODO This could be done in parallel
	 * 
	 */
	public void startAllServices() {
		List<C> configs = getConfigs();
		for(C config : configs) {
			startByConfig(config);
		}
	}
	
//	public ServiceExecution<S> getExecutionByConfigId(Object configId) {
//		Object executionId = configIdToExecutionId.get(configId);
//		ServiceExecution<S> result = idToServiceExecution.get(executionId);
//		
//		return result;
//	}

	public ServiceControl<S> startByConfigId(Object id) {

		C config = getEntity(emf, configClass, id);
		
		ServiceControl<S> result = startByConfig(config);
		return result;
	}
	
	public ServiceControl<S> startByConfig(C config) {
		
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();

		
		Object configId = emf.getPersistenceUnitUtil().getIdentifier(config);
		

		ServiceControlJpaImpl<C, E, S> result = configIdToControl.get(configId);
		if(configId == null) {
			result = new ServiceControlJpaImpl<C, E, S>(this, configId);
			configIdToControl.put(configId, result);
		}

				
		// If the service is already running, skip it
		ServiceProvider<S> provider = configIdToProvider.get(configId);
		if(provider != null) {
			return result;
		}

		
		// Check if there is already is a service execution object
		List<ConfigToExecution> ctes = getMappings(em, configId);
		
		//List<E> executionContexts = getExecutionContexts(config); 
		
		if(ctes.size() > 1) {
			logger.warn("Deleting multiple execution contexts for config " + config);
			//deleteAll(emf, executionContextClass);
			
			for(ConfigToExecution cte : ctes) {
				
				// Delete associated execution contexts
				Object cteId = cte.getExecutionContextId(); //deserialize(cte.getExecutionContextId());
				E tmp = em.find(executionContextClass, cteId);
				em.remove(tmp);
				
				em.remove(cte);
			}
			
			// Should be empty now
			ctes = getMappings(em, configId);
		}
		
		ConfigToExecution cte;
		E executionContext;
		boolean isRestart = false;
		
		if(ctes.isEmpty()) {
			executionContext = newEntity(em, executionContextClass);
			cte = newCte(em, configId, executionContext);
		}
		else if(ctes.size() == 1) {
			cte = ctes.get(0);
			Object cteId = cte.getExecutionContextId(); //deserialize(cte.getExecutionContextId());
			executionContext = em.find(executionContextClass, cteId);

			// If we failed to retrieve an execution context, just create a new one
			if(executionContext == null) {
				em.remove(cte);
				
				executionContext = newEntity(em, executionContextClass);
				cte = newCte(em, configId, executionContext);
			}
			else {
				isRestart = true;
			}
		}
		else {
			throw new RuntimeException("Multiple execution contexts after delete for " + config);
		}
		Object executionId = emf.getPersistenceUnitUtil().getIdentifier(cte);
		
		
		Serializable executionContextId = (Serializable)emf.getPersistenceUnitUtil().getIdentifier(executionContext);

		cte.setStatus("STARTING");
		
		em.flush();
		em.getTransaction().commit();
		em.close();
		


		//EntityHolder<E> holder = new EntityHolderJpa<E>(emf, executionContextClass, executionContextId);

		ServiceProvider<S> serviceProvider;
		try {
			serviceProvider = serviceLauncher.launch(emf, config, executionContext, isRestart);
			if(serviceProvider != null) {
				configIdToProvider.put(configId, serviceProvider);
			}
			
			setStatus(cte, "RUNNING");
		} catch(Exception e) {
			logger.error("Failed to launch service", e);
			setStatus(cte, "STOPPED");
			serviceProvider = null;
		}

		S service = serviceProvider != null ? serviceProvider.getService() : null;
		
		
		ServiceState state = new ServiceState(configId, executionId, executionContextId);
		configIdToState.put(configId, state);
		executionIdToState.put(executionId, state);
		executionContextIdToState.put(executionContextId, state);
		
		em = emf.createEntityManager();
		em.getTransaction().begin();
		try {
			for(ServiceEventListener<C, E, S> listener : eventListerners) {
				listener.onAfterServiceStart(config, executionContext, service);
			}
		}
		finally {
			em.getTransaction().commit();
			em.close();
		}
		
		return result;
	}


	public void stopByConfigId(Object configId) {
		ServiceState state = configIdToState.get(configId);
		if(state == null) {
			throw new RuntimeException("No service with config id " + configId);
		}
		
		ServiceProvider<S> provider = configIdToProvider.get(configId);
		S service = provider != null ? provider.getService() : null;
		
		
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		C config = em.find(configClass, configId);
		
		Object executionContextId = state.getExecutionContextId();		
		E executionContext = em.find(executionContextClass, executionContextId);
		try {
			for(ServiceEventListener<C, E, S> listener : eventListerners) {
				listener.onBeforeServiceStop(config, executionContext, service);
			}
		}
		finally {
			em.getTransaction().commit();
			em.close();
		}

		if(provider != null) {
			provider.close();
			
			configIdToProvider.remove(configId);
		}

				
		//configIdToState.remove(state.getConfigId());
		//executionIdToState.remove(state.getExecutionId());
		//executionContextIdToState.remove(state.getExecutionContextId());
	}

	
	public void setStatus(ConfigToExecution cte, String status) {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		cte.setStatus(status);
		em.merge(cte);
		
		em.getTransaction().commit();
		em.close();
	}

	public ServiceProvider<S> getServiceProviderByConfigId(Object configId) {
		return configIdToProvider.get(configId);
	}
	
	public ServiceState getStateByConfigId(Object configId) {
		return configIdToState.get(configId);
	}
	
	public ServiceState getStateByExecutionId(Object executionId) {
		return executionIdToState.get(executionId);
	}

	public ServiceState getStateByExecutionContextId(Object executionContextId) {
		return executionContextIdToState.get(executionContextId);
	}

	
	
//	@Override
//	public void startByExecutionContextIds(Set<?> executionContextIds) {
//		for(Object executionContextId : executionContextIds) {
//			startExecution(executionContextId);
//		}
//	}
//	
//	public void startExecution(Object executionContextId) {
//		ServiceState<C, S, E> serviceExecution = executionContextIdToState.get(executionContextId);
//		System.out.println(serviceExecution);
//		//serviceExecution.start();
//	}
//	
//
//	@Override
//	public void stopExecutions(Set<?> executionContextIds) {
//		for(Object executionContextId : executionContextIds) {
//			stopExecution(executionContextId);
//		}
//	}
//	
//	//@Override
//	public void stopExecution(Object executionContextId) {
//		ServiceState<C, S, E> serviceExecution = executionContextIdToState.get(executionContextId);
//	}
//
//	public ServiceProvider<S> getServiceProviderByConfigId(Object configId) {
//		ServiceProvider<S> result = configIdToProvider.get(configId);
//		return result;
//	}

}
