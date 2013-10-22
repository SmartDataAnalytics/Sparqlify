package org.aksw.service_framework.jpa.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;

public class ServiceProviderJpaRdbRdf<S>
	extends ServiceProviderDecorator<S>
{
	private EntityManagerFactory emf;
	private Rdb2RdfExecution executionContext;
	
	public ServiceProviderJpaRdbRdf(ServiceProvider<S> decoratee, EntityManagerFactory emf, Rdb2RdfExecution executionContext) {
		super(decoratee);
		this.emf = emf;
		this.executionContext = executionContext;
	}
	
	@Override
	public void close() {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		
		super.close();
		
		executionContext.setStatus("STOPPED");
		em.merge(executionContext);
		
		em.getTransaction().commit();
		em.close();
	}
	
}
