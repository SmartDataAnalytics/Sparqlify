package org.aksw.sparqlify.admin.web.common;


public interface EntityHolder<T> {
	T getEntity();
	void save();
}
