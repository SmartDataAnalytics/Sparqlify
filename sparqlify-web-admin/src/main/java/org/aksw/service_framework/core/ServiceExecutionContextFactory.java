package org.aksw.service_framework.core;

import org.aksw.sparqlify.admin.web.common.EntityHolder;

public interface ServiceExecutionContextFactory<E> {
	EntityHolder<E> create();
}