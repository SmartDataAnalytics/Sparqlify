package org.aksw.sparqlify.admin.web.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.aksw.sparqlify.jpa.EntityRef;


public class EntityRefUtils {

	@SuppressWarnings("rawtypes")
	public static CriteriaQuery<?> toCriteria(EntityManager em, EntityRef entityRef) {

		CriteriaBuilder cb = em.getCriteriaBuilder();

		Class<?> entityClass = entityRef.getEntityClass();
		
		CriteriaQuery cq = cb.createQuery(entityClass);
		@SuppressWarnings("unchecked")
		Root r = cq.from(entityClass);
		
		Map<String, Object> map = entityRef.getPropertyToValue();
		
		List<Predicate> predicates = new ArrayList<Predicate>(map.size());
		
		for(Entry<String, Object> entry : map.entrySet()) {
			String propertyName = entry.getKey();
			Object value = entry.getValue();
			
			Predicate predicate = cb.equal(r.get(propertyName), value);
			
			predicates.add(predicate);
		}
		
		cq.where(predicates.toArray(new Predicate[0]));
		
		@SuppressWarnings("unchecked")
		CriteriaQuery result = cq.select(r);
		
		return result;
		//List<ConfigToExecution> result = em.createQuery(cq).getResultList();
	}

	public static List<?> fetchEntities(EntityManager em, List<EntityRef> entityRefs) {
		List<Object> result = new ArrayList<Object>();
		for(EntityRef entityRef : entityRefs) {
			List<?> items = fetchEntities(em, entityRef);
			result.addAll(items);
		}
		return result;
	}

	
	public static List<?> fetchEntities(EntityManager em, EntityRef entityRef) {
		CriteriaQuery<?> criteriaQuery = EntityRefUtils.toCriteria(em, entityRef);
		List<?> result = em.createQuery(criteriaQuery).getResultList();		
		return result;
	}
}