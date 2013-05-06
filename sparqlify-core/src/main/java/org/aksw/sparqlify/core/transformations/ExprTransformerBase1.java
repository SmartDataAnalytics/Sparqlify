package org.aksw.sparqlify.core.transformations;

import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import com.hp.hpl.jena.sparql.expr.Expr;


public abstract class ExprTransformerBase1
	implements ExprTransformer
{
	@Override
	public E_RdfTerm transform(Expr orig, List<E_RdfTerm> exprs) {
		if(exprs.size() != 1) {
			throw new RuntimeException("Exactly one argument expected. Got: " + exprs.size() + " " + exprs);
		}
		
		E_RdfTerm a = exprs.get(0);
		
		E_RdfTerm result = transform(orig, a);
		return result;
	}
	
	public abstract E_RdfTerm transform(Expr orig, E_RdfTerm a);
}
