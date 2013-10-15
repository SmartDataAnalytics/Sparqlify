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
public interface ServiceLauncher<C, S, E> {
	ServiceExecution<S> launch(C config, EntityHolder<E> context);
}
