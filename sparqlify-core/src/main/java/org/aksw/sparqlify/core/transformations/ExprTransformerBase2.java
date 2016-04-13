package org.aksw.sparqlify.core.transformations;

import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import org.apache.jena.sparql.expr.Expr;


public abstract class ExprTransformerBase2
	implements ExprTransformer
{
	@Override
	public E_RdfTerm transform(Expr orig, List<E_RdfTerm> exprs) {
		if(exprs.size() != 2) {
			throw new RuntimeException("Exactly two arguments expected. Got: " + exprs.size() + " " + exprs);
		}
		
		E_RdfTerm a = exprs.get(0);
		E_RdfTerm b = exprs.get(1);
		
		E_RdfTerm result = transform(orig, a, b);
		return result;
	}
	
	public abstract E_RdfTerm transform(Expr orig, E_RdfTerm a, E_RdfTerm b);
}
