package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

public class ExprTransformerRdfTermCtor
	implements ExprTransformer
{
	
	boolean hasRdfTermCtorArgument(ExprFunction fn) {
		return hasRdfTermCtorArgument(fn.getArgs());
	}
	
	boolean hasRdfTermCtorArgument(Iterable<Expr> exprs) {
		for(Expr expr : exprs) {
			if(expr instanceof E_RdfTerm) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public Expr transform(ExprFunction fn) {
		
		// Check if one argument is an RdfTermCtor
		if(!hasRdfTermCtorArgument(fn)) {
			return fn;
		}

		// Otherwise... TODO Somehow delegate to a function that can deal with it
		
		
				
		// TODO Auto-generated method stub
		return null;
	}
}