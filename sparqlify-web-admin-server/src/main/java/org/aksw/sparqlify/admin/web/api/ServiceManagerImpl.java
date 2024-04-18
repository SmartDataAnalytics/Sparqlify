package org.aksw.sparqlify.admin.web.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;

import org.aksw.service_framework.jpa.core.ServiceRepositoryJpaImpl;
import org.aksw.service_framework.jpa.core.ServiceState;
import org.aksw.sparqlify.jpa.EntityInverseMapper;
import org.aksw.sparqlify.jpa.EntityRef;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.vocabulary.RDF;

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
        Node uri = NodeFactory.createURI(serviceUriStr);
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

    public Set<Object> getConfigIds(String serviceUriStr) {
        Set<Object> result = new HashSet<Object>();

        Set<Object> executionContextIds = getEntityIds(serviceUriStr);
        for(Object executionContextId : executionContextIds) {
            ServiceState state = serviceRepository.getStateByExecutionContextId(executionContextId);
            Object configId = state != null ? state.getConfigId() : null;

            if(state != null) {
                result.add(configId);
            }
        }

        return result;
    }

    @Override
    public void startService(String serviceUriStr) {
        Set<Object> configIds = getConfigIds(serviceUriStr);
        for(Object configId : configIds) {
            serviceRepository.startByConfigId(configId);
        }
    }

    @Override
    public void stopService(String serviceUriStr) {
        Set<Object> configIds = getConfigIds(serviceUriStr);
        for(Object configId : configIds) {
            serviceRepository.stopByConfigId(configId);
        }
    }

    @Override
    public void deleteService(String serviceUriStr) {
        Set<Object> configIds = getConfigIds(serviceUriStr);
        for(Object configId : configIds) {
            serviceRepository.deleteByConfigId(configId);
        }
    }

    @Override
    public String registerService(Object configId) {
        serviceRepository.startByConfigId(configId);

        // TODO Currently we cannot map a configId to its URI
        return null;
    }

    @Override
    public Object getConfigId(String serviceUriStr) {
        Set<Object> configIds = getConfigIds(serviceUriStr);

        if(configIds.size() > 1) {
            throw new RuntimeException("Multiple ids not expected");
        }

        Object result = configIds.isEmpty() ? null : configIds.iterator().next();

        return result;
    }


//	@Override
//	public void deleteService(String serviceUriStr) {
//		Set<Object> executionContextIds = getEntityIds(serviceUriStr);
//		for(Object executionContextId : executionContextIds) {
//			ServiceState state = serviceRepository.getStateByExecutionContextId(executionContextId);
//			Object configId = state != null ? state.getConfigId() : null;
//
//			serviceRepository.stopByConfigId(configId);
//
//		}
//	}

}