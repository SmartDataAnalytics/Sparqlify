package org.aksw.service_framework.jpa.core;

/**
 * A ServiceProvider is an object from which the service can be obtained.
 * Additionally, a name and a close method are associated with the service.
 * 
 * Multiple calls to getService should return the same object.
 * 
 * A service provider only has to be capable of stopping a service, but not of
 * (re)starting it.
 * 
 * @author raven
 *
 * @param <S>
 */
public interface ServiceProvider<S> {
	String getName();
	S getService();

	//boolean isClosed();
	void close();
}
