package org.aksw.service_framework.jpa.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.aksw.service_framework.core.ServiceExecution;
import org.aksw.service_framework.core.ServiceExecutionContextFactory;
import org.aksw.service_framework.core.ServiceLauncher;
import org.aksw.service_framework.core.ServiceRepository;
import org.aksw.sparqlify.admin.web.common.EntityHolder;



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

	public static <T> List<T> listAll(EntityManager em, Class<T> clazz) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		
		em.getTransaction().begin();
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
	 * TODO This could be done in parallel
	 * 
	 */
	public void startAllServices() {
		List<C> configs = getConfigs();
		for(C config : configs) {
			Object serviceId = emf.getPersistenceUnitUtil().getIdentifier(config);
			
			EntityHolder<E> executionContext = executionContextFactory.create();
			
			ServiceExecution<S> serviceExecution = serviceLauncher.launch(config, executionContext);
			
			idToServiceExecution.put(serviceId, serviceExecution);
		}
	}


}
