package org.aksw.service_framework.core;

import org.aksw.sparqlify.admin.web.common.EntityHolder;

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
	ServiceExecution<S> launch(C config, EntityHolder<E> context, boolean isRestart);
}
