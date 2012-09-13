package org.aksw.sparqlify.algebra.sql.datatype;

import java.util.Set;

import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

/**
 * A class with knowlegde about the type hierarchy.
 * 
 * For instance, given two datatypes, it is possible to determine
 * the most general datatype (int, float -> numeric), or the 
 * mightier of the two (float, double) -> double. 
 * 
 * 
 * Essentially, the dataypes form a tree, 
 * 
 * 
 * @author raven
 *
 */
public interface DatatypeSystem {
	
	
    SqlDatatype getByName(String name);
    
    // Same as getByName, but throws exception if none found
    SqlDatatype requireByName(String name);

    
	Object cast(Object value, SqlDatatype target);
	
	// Return a factory for creating cast-expressions between the given datatypes
	// Null if no such cast exists.
	// If no expression is required, return an factory that only returns the original expression
	// Do not assume that the factory will evaluated the original expression
	// so cast(string, int).create(NodeValue.makeString('666')) may return
	// Cast((string, int), NodeValue('666')) rather than NodeValue.makeInteger(666)
	Factory1<SqlExpr> cast(SqlDatatype from, SqlDatatype to);

	
	//getPossibleCasts(SqlDatatype from)
	
	/**
	 * Returns the top-most node of the tree of compatible datatypes
	 * 
	 * Hm, how to justify not returning object?
	 * 
	 * so integer and string -> numeric
	 * string -> text
	 * 
	 * The rationale is, that all numeric types have similar semantics in regard
	 * to comparison and arithmetic operations. However mixing string and numeric
	 * is invalid.
	 * 
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	SqlDatatype mostGenericDatatype(SqlDatatype from, SqlDatatype to);
	
	Set<SqlDatatype> supremumDatatypes(SqlDatatype from, SqlDatatype to);

	/**
	 * Returns -1, 0, 1 if a is more specific, equal, more general the b.
	 * null if no subsumption relation exists
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	Integer compare(SqlDatatype a, SqlDatatype b);

}
