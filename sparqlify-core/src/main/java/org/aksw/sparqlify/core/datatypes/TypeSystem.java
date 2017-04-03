package org.aksw.sparqlify.core.datatypes;

import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator;
import org.apache.jena.sparql.expr.NodeValue;

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
public interface TypeSystem {
	
	SparqlFunction createSparqlFunction(String name, SqlExprEvaluator evaluator);
	
	void registerSqlFunction(String sparqlFunctionName, XMethod sqlFunction);
	
	//Collection<XMethod> getSqlFunctions(String name);
	
    XClass getByName(String name);
    XClass getByName(TypeToken token);
    
    @Deprecated
    XClass getByClass(Class<?> clazz);
    
	TypeToken getTokenForClass(Class<?> clazz);
	
    // Same as getByName, but throws exception if none found
	XClass requireByName(String name);

    void registerCoercion(XMethod method);
	
    SparqlFunction getSparqlFunction(String name);
    SqlMethodCandidate lookupMethod(String sparqlFunctionName, List<TypeToken> argTypes);
	
	List<TypeToken> getDirectSuperClasses(TypeToken type);
	
	NodeValue cast(NodeValue value, TypeToken target);
	
	/**
	 *Return a factory for creating cast-expressions between the given datatypes
	 * Null if no such cast exists. 
	 * 
	 * Constant folding may be performed, but do not rely on it.
	 * So cast(string, int).create(NodeValue.makeString('666')) may return
	 * Cast((string, int), NodeValue('666')) rather than NodeValue.makeInteger(666)
	 */
	UnaryOperator<SqlExpr> cast(TypeToken from, TypeToken to);

	
	boolean isSuperClassOf(TypeToken a, TypeToken b);

	//getPossibleCasts(TypeToken from)
	
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
	TypeToken mostGenericDatatype(TypeToken from, TypeToken to);
	
	Set<TypeToken> supremumDatatypes(TypeToken from, TypeToken to);

	/**
	 * Returns -1, 0, 1 if a is more specific, equal, more general the b.
	 * null if no subsumption relation exists
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	Integer compare(TypeToken a, TypeToken b);

}
