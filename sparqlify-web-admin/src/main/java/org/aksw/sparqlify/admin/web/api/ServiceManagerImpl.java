package org.aksw.sparqlify.admin.web.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;

import org.aksw.service_framework.jpa.core.ServiceRepositoryJpaImpl;
import org.aksw.sparqlify.jpa.EntityInverseMapper;
import org.aksw.sparqlify.jpa.EntityRef;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;

public class ServiceManagerImpl<C, E, S>
	implements ServiceManager
{
	private EntityInverseMapper inverseMapper;
	private ServiceRepositoryJpaImpl<C, E, S> serviceRepository;

	
	public ServiceManagerImpl(ServiceRepositoryJpaImpl<C, E, S> serviceRepository, EntityInverseMapper inverseMapper) {
		this.inverseMapper = inverseMapper;
		this.serviceRepository = serviceRepository;
	}
	
	public static <C, E, S> ServiceManagerImpl<C, E, S> create(ServiceRepositoryJpaImpl<C, E, S> serviceRepository, EntityInverseMapper inverseMapper) {
		ServiceManagerImpl<C, E, S> result = new ServiceManagerImpl<C, E, S>(serviceRepository, inverseMapper);
		
		return result;
	}
	
	public static Set<Object> extractIds(EntityManagerFactory emf, Collection<?> entities) {
		PersistenceUnitUtil puu = emf.getPersistenceUnitUtil();
		
		Set<Object> result = new HashSet<Object>();
		for(Object entity : entities) {
			Object id = puu.getIdentifier(entity);

			result.add(id);
		}
		return result;
	}
	
	public Set<Object> getEntityIds(String serviceUriStr) {
		Node uri = Node.createURI(serviceUriStr);
		Quad quad = new Quad(Quad.defaultGraphNodeGenerated, uri, RDF.type.asNode(), ServiceVocab.ServiceExecution.asNode());

		List<EntityRef> entityRef = inverseMapper.map(quad);

		EntityManagerFactory emf = serviceRepository.getEntityManagerFactory();
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		List<?> entities = EntityRefUtils.fetchEntities(em, entityRef); 
		Set<Object> result = extractIds(emf, entities);
		
				
		em.getTransaction().commit();
		em.close();
		
		return result;
	}

	@Override
	public void startService(String serviceUriStr) {
		Set<Object> ids = getEntityIds(serviceUriStr);
		serviceRepository.startExecutions(ids);
	}

	@Override
	public void stopService(String serviceUriStr) {
		Set<Object> ids = getEntityIds(serviceUriStr);
		serviceRepository.stopExecutions(ids);
	}
	
}