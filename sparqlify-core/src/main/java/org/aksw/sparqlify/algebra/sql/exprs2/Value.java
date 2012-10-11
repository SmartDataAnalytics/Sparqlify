package org.aksw.sparqlify.algebra.sql.exprs2;

// TODO I don't think it is a good idea having such class (similar to NodeValue in Jena).
/*
 * In the SqlExpr class hierarchy, we are associate a logical datatype with each expression.
 * Based on these datatypes, we can pick functions suitable for evaluation.
 * 
 * 
 * 
 * 
 */
public interface Value {
	<T> T getValue();

	/**
	 * 
	 * 
	 * @return
	 */
	
	/*
	 * The following methods apply to the physical datatype (not the logical one)
	 * 
	 * Example:
	 * A polygon can be passed around as a string in WKT, however, logically the datatype is geometry.
	 * 
	 */
	
	boolean isNumeric();
	Number asNumber();
	
	boolean isString();
	String asString();
	
	
}

