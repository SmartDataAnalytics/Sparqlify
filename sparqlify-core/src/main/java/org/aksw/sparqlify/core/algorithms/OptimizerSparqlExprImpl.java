package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.compile.sparql.SqlExprOptimizer;
import org.aksw.sparqlify.core.interfaces.OptimizerSparqlExpr;

import com.hp.hpl.jena.sparql.expr.Expr;

// TODO I think we do not need any special optimizer anymore
// It can be built directly into the SQL translation
public class OptimizerSparqlExprImpl
	implements OptimizerSparqlExpr
{
	@Override
	public Expr optimize(Expr expr) {
		Expr result = SqlExprOptimizer.optimizeMM(expr);
		
		return result;
	}
}
