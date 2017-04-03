package org.aksw.sparqlify.core.datatypes;

import org.aksw.sparqlify.core.sql.expr.serialization.SqlFunctionSerializer;
import org.aksw.sparqlify.type_system.MethodSignature;


/**
 * A method (actually a function - there is no object support yet) in the
 * Sparqlify system. A method can be associated with
 * - a SQL rewrite transformation
 * - an invocable that performs the method.
 * 
 * Examples:
 * 
 * Intersection of geometries, for which no invocable is provided:
 *   boolean bif:intersects(geometry, geometry, precision_in_km):
 *    
 *   The big question is: should the SQL serializer be part of this class?
 *      
 *   
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface XMethod {
	
	String getName();
	
	MethodSignature<XClass> getSignature();
	
	//String getSerializer();
	
	/**
	 * 
	 * 
	 * @return The object implementing the method. Null if there is no implementation.
	 */
	Invocable getInvocable();

	
	/**
	 * 
	 * 
	 * 
	 * 
	 */
	SqlFunctionSerializer getSerializer();
}

