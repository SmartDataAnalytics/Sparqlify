package org.aksw.sparqlify.admin.web.common;


public interface EntityHolder<T> {
	T getEntity();

	/**
	 * Must be called prior to making changes to the entity.
	 * Especially, accessing of the attributes in cases where the entity is a proxy
	 * will fail without a session
	 */
	void openSession();
	//void save();
	
	// TODO commit also closes the session 
	void commit();
}
