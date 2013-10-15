package org.aksw.sparqlify.admin.web.common;

import java.io.Serializable;

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
	private EntityManager em = null;
	
	private Class<T> clazz;
	private Serializable id;
	
	private T entity;
	
	public EntityHolderJpa(EntityManagerFactory emf, Class<T> clazz, Serializable id) {
		this.emf = emf;
		this.clazz = clazz;
		this.id = id;
		this.entity = null;
	}
	
	public T getEntity() {
		return entity;
	}
	
	public void openSession() {
		if(em != null) {
			throw new RuntimeException("Session is already open");
		}
		
		em = emf.createEntityManager();
		em.getTransaction().begin();
		
		entity = em.find(clazz, id);
		if(entity == null) {
			throw new RuntimeException("Instance with id " + id + " of entity " + clazz.getName() + " not found");
		}
	}
	
	public void commit() {
		if(em == null) {
			throw new RuntimeException("No session was openend");
		}
//		EntityManager em = emf.createEntityManager();
//		em.getTransaction().begin();
	
		em.merge(entity);

		// TODO This flush is probably superfluous
		em.flush();

		em.getTransaction().commit();
		em.close();

		em = null;
		entity = null;
	}
}