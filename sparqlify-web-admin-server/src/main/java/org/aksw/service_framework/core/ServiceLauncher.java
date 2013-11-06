package org.aksw.service_framework.core;

import javax.persistence.EntityManagerFactory;

import org.aksw.service_framework.jpa.core.ServiceProvider;

/**
 * 
 * @author raven
 *
 * @param <C> The config class
 * @param <S> The service class
 * @param <S> The execution context class
 */
public interface ServiceLauncher<C, E, S> {
	
	/**
	 * 
	 * 
	 * @param config
	 * @param context
	 * @param isRestart Indicates whether the service is being resumed from a prior execution context
	 * @return
	 */
	ServiceProvider<S> launch(EntityManagerFactory emf, C config, E context, boolean isRestart);
}
