package org.aksw.sparqlify.algebra.sparql.nodes;

import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * An op-node that may hold information about the applicable filter 
 * @author raven
 *
 */
public abstract class OpBase
	implements Op
{
	private ExprList filters;
	
	public abstract String getName();
}
