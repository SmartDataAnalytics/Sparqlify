package org.aksw.sparqlify.core.datatypes;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;


public interface XClass {
	//DatatypeSystem getDatatypeSystem();
	//TypeToken getTypeToken();
	Class<?> getCorrespondingClass();
	String getName();

	List<XClass> getDirectSuperClasses();
	
	TypeToken getToken();
	
	boolean isAssignableFrom(XClass that);
}
