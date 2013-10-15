package org.aksw.service_framework.jpa.core;

import javax.persistence.EntityManagerFactory;

import org.aksw.service_framework.core.ServiceExecutionContextFactory;
import org.aksw.sparqlify.admin.web.common.EntityHolder;
import org.aksw.sparqlify.admin.web.common.EntityHolderJpa;


//public class ServiceExecutionContextFactoryJpa<E>
//	implements ServiceExecutionContextFactory<E>
//{
//	private EntityManagerFactory emf;
//	
//	private Class<E> clazz;
//	
//	public ServiceExecutionContextFactoryJpa(EntityManagerFactory emf, Class<E> clazz) {
//		this.clazz = clazz;
//		this.emf = emf;
//	}
//
//	@Override
//	public EntityHolder<E> create() {
//		E entity;
//		try {
//			entity = clazz.newInstance();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//
//		EntityHolder<E> result = new EntityHolderJpa<E>(entity, emf);
//		
//		return result;
//	}
//}