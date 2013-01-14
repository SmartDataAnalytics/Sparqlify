package org.aksw.sparqlify.core.cast;

import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class ExprBindingSubstitutorImpl
	implements ExprBindingSubstitutor
{
	@Override
	public Expr substitute(Expr expr, Map<Var, Expr> binding) {

		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(binding);
		Expr result = substitutor.transformMM(expr);
		
		return result;
	}
}
