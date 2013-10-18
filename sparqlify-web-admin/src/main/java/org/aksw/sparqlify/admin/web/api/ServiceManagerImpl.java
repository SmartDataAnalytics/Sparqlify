package org.aksw.sparqlify.admin.web.api;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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
	
	@Override
	public void startService(String id) {
		Node uri = Node.createURI(id);
		Quad quad = new Quad(Quad.defaultGraphNodeGenerated, uri, RDF.type.asNode(), ServiceVocab.ServiceExecution.asNode());
		
		List<EntityRef> entityRef = inverseMapper.map(quad);

		EntityManagerFactory emf = serviceRepository.getEntityManagerFactory();
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		List<?> entities = EntityRefUtils.fetchEntities(em, entityRef); 
				
		for(Object entity : entities) {
			System.out.println(entity);
		}
		
		em.getTransaction().commit();
		em.close();
		
		
	}

	@Override
	public void stopService(String id) {
		// TODO Auto-generated method stub
		
	}
	
}