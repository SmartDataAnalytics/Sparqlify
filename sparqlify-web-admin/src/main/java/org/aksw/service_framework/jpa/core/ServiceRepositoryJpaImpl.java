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

import org.aksw.service_framework.core.ServiceExecution;
import org.aksw.service_framework.core.ServiceExecutionContextFactory;
import org.aksw.service_framework.core.ServiceLauncher;
import org.aksw.service_framework.core.ServiceRepository;
import org.aksw.service_framework.jpa.model.ConfigToExecution;
import org.aksw.sparqlify.admin.web.common.EntityHolder;
import org.aksw.sparqlify.admin.web.common.EntityHolderJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A little service framework based on hibernate
 * TODO abstract from the SessionFactory
 * 
 * 
 * 
 * Assumptions:
 *   - ExecutionContexts can be cleaned up using session.delete()
 *     In general it might be necessary to invoke a custom handler
 * 
 * 
 * ISSUES:
 * - I guess this class needs to be able to raise events about added and removed services
 * 
 * - Is it possibly to allow the creation of different kinds of objects from a service config?  
 *   I would say no. If for some reason different service objects have to be created
 *   from one type of configuration object, then possibly the service has to be
 *   of type object and the user has to cast it herself. 
 * 
 * @author raven
 *
 */
public class ServiceRepositoryJpaImpl<C, E, S>
	implements ServiceRepository<S>
{
	private static final Logger logger = LoggerFactory.getLogger(ServiceRepositoryJpaImpl.class);
	
	private EntityManagerFactory emf;

	//private Class<S> serviceClass;
	private Class<E> executionContextClass;
	private Class<C> configClass;

	private ServiceLauncher<C, S, E> serviceLauncher;	
	private ServiceExecutionContextFactory<E> executionContextFactory;
	
	// TODO We can map configs to their respective executions
	// But how can we retrieve a service by its name?
	// Or do we only need access via id?
	private Map<Object, ServiceExecution<S>> idToServiceExecution = new HashMap<Object, ServiceExecution<S>>();
	
	// We need a mapping from config to execution
	
	public ServiceRepositoryJpaImpl(
			EntityManagerFactory emf,
			Class<C> configClass,
			Class<E> executionContextClass,
			//Class<S> serviceClass,
			ServiceLauncher<C, S, E> serviceLauncher)
	{
		this.emf = emf;

		this.configClass = configClass;
		this.executionContextClass = executionContextClass;
		this.serviceLauncher = serviceLauncher;
		
		executionContextFactory = new ServiceExecutionContextFactoryJpa<E>(emf, executionContextClass);
	}

	public static <C, E, S> ServiceRepositoryJpaImpl<C, E, S> create(
		EntityManagerFactory emf,
		Class<C> configClass,
		Class<E> executionContextClass,
		ServiceLauncher<C, S, E> serviceLauncher
	)
	{
		ServiceRepositoryJpaImpl<C, E, S> result = new ServiceRepositoryJpaImpl<C, E, S>(emf, configClass, executionContextClass, serviceLauncher);
		return result;
	}
	
	
	public void killAll() {
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
		T result = null;
		try {
			 result = clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		em.persist(result);
		em.flush();
		
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

		Object executionContextId = emf.getPersistenceUnitUtil().getIdentifier(executionContext);
		

		
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
			
			EntityManager em = emf.createEntityManager();
			em.getTransaction().begin();

			
			Object configId = emf.getPersistenceUnitUtil().getIdentifier(config);
			
			// If the service is already running, skip it
			ServiceExecution<S> serviceExecution = idToServiceExecution.get(configId);
			if(serviceExecution != null) {
				continue;
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
			
			em.getTransaction().commit();
			em.close();
			
			
			EntityHolder<E> holder = new EntityHolderJpa<E>(executionContext, emf);			
			serviceExecution = serviceLauncher.launch(config, holder, isRestart);
			idToServiceExecution.put(configId, serviceExecution);
		}
	}


}
