package org.aksw.sparqlify.admin.web.common;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * 
 * TODO Maybe rather then doing a direct save, the request should be delegated to a parent container
 *
 * @author raven
 *
 * @param <T>
 */
public class EntityHolderJpa<T>
	implements EntityHolder<T>
{
	private EntityManagerFactory emf;
	
	private T entity;
	
	public EntityHolderJpa(T entity, EntityManagerFactory emf) {
		this.entity = entity;
		this.emf = emf;
	}
	
	public T getEntity() {
		return entity;
	}
	
	public void save() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
	
		em.persist(entity);

		em.getTransaction().commit();
		em.close();
	}
}