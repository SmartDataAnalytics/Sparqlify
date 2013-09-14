package org.aksw.sparqlify.type_system;


/**
 * 
 * 
 * @author raven
 *
 * @param <T>
 */
public interface TypeModel<T> {
	//List<T> getDirectSuperTypes(T type);
	
	boolean isSuperTypeOf(T superType, T subType);
}
