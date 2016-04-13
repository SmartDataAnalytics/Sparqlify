package org.aksw.sparqlify.core.cast;

import org.aksw.commons.util.factory.Factory1;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Note to myself: My initial idea was to have a method
 * Factory1<Expr> getCastFactory(sourceTypeName, targetTypeName)
 * which upon invocation must return an appropriate factory. 
 * However, instead of using this factory, we could use E_Cast
 * 
 * 
 * cast(intgeger) -> double
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface CastSystem {
	
	/**
	 * Cast a constant value
	 * 
	 * @param sourceTypeName
	 * @param targetTypeName
	 * @return
	 * @throws CastException
	 */
	public NodeValue cast(NodeValue value, String targetTypeName)
			throws CastException;

	
	public Factory1<Expr> lookupCast(String sourceTypeName, String targetTypeName);
	
}


