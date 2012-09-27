package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.interfaces.OptimizerSparqlExpr;
import org.aksw.sparqlify.core.interfaces.TranslatorSql;

import com.hp.hpl.jena.sparql.expr.Expr;


public class TranslatorSqlImpl
	implements TranslatorSql
{

	private OptimizerSparqlExpr exprOptimizer = new OptimizerSparqlExprImpl();
	
	
	@Override
	public Expr translateSql(Expr sparqlExpr) {
		// Optimize the expression
		
		
		/*
		if (tmp.equals(NodeValue.FALSE)) {
			// TODO Somehow indicate an empty relation
		}
	
		// For the translation to sql we need the sparql expression
		// and the mapping of sparql variables to sql columns
		//SqlExpr sqlExpr = SqlExprOptimizer.translateMM(tmp);
	
		SqlExpr sqlExpr = forcePushDown(tmp, substitutor);
	
		//if(true) { throw new RuntimeException("Add support for discriminator column"); }
		/*
		Expr substituted = substitutor.transformMM(tmp);
	
		
		Expr x = PushDown.pushDownMM(substituted);
		if(!(x instanceof ExprSqlBridge)) {
			throw new RuntimeException("Failed to push down '" + tmp + "'");
		}
		SqlExpr sqlExpr = ((ExprSqlBridge)x).getSqlExpr();
		*/
	
		return null;
	}
}
