package org.aksw.sparqlify.core.datatypes;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;

/**
 * TODO: This class is very similar to Jena's RDFDataype. Maybe we could simply code by re use.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface XClass {
	//DatatypeSystem getDatatypeSystem();
	//TypeToken getTypeToken();
	Class<?> getCorrespondingClass();

	List<XClass> getDirectSuperClasses();
	
	// This could be "getUri()"
	@Deprecated
	TypeToken getToken();
	
	String getName();
	
	
	//String parse(String lexicalValue, String datatype);
	
	boolean isAssignableFrom(XClass that);
}
