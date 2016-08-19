package org.aksw.sparqlify.core.rewrite.expr.transform;

import java.util.List;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.apache.jena.sparql.expr.Expr;


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
