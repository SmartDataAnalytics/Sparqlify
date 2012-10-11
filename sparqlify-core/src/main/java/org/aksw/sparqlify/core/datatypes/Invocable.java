package org.aksw.sparqlify.core.datatypes;

/**
 * Interface for something that can be invoked to yield some result.
 * Note: Metadata, such as which argument types are supported, have to be kept
 * elsewhere.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface Invocable {
	Object invoke(Object...args);
}
