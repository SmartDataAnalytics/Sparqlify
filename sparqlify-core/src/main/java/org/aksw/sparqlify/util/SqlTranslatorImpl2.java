package org.aksw.sparqlify.util;

import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs2.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutor;
import org.aksw.sparqlify.core.cast.TypedExprTransformer;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class SqlTranslatorImpl2
	implements SqlTranslator
{
	private static final Logger logger = LoggerFactory.getLogger(SqlTranslatorImpl2.class);
	
	private ExprEvaluator exprTransformer;
	private ExprBindingSubstitutor exprBindingSubstitutor;
	private TypedExprTransformer typedExprTransformer;

	public SqlTranslatorImpl2(ExprBindingSubstitutor exprBindingSubstitutor, ExprEvaluator exprTransformer, TypedExprTransformer typedExprTransformer) {
		this.exprTransformer = exprTransformer;
		this.exprBindingSubstitutor = exprBindingSubstitutor;
		this.typedExprTransformer = typedExprTransformer;
	}
	
	/**
	 * TODO: There are two use cases:
	 * a) Rewrite an expression completely to SQL -> result is an SqlExpr object
	 * b) Partially rewrite an expression to SQL -> result is an SqlExprRewrite object
	 * 
	 * I think the interface should return the SqlExprRewrite and
	 * a static helper function then extracts the SqlExpr from it
	 * 
	 */
	@Override
	public ExprSqlRewrite translate(Expr sparqlExpr, Map<Var, Expr> binding,
			Map<String, TypeToken> typeMap) {
		Expr e1;
		
		if(binding != null) {
			e1 = exprBindingSubstitutor.substitute(sparqlExpr, binding);
		} else {
			e1 = sparqlExpr;
		}
		
		Expr e2 = exprTransformer.transform(e1);
		//System.out.println("[ExprRewrite Phase 2]: " + e2);

		ExprSqlRewrite e3 = typedExprTransformer.rewrite(e2, typeMap);
		logger.debug("[ExprRewrite Phase 3]: " + e3);

		return e3;
	}

	
	public static SqlExpr asSqlExpr(ExprSqlRewrite rewrite) {
		Expr et = rewrite.getExpr();
		if(et instanceof ExprSqlBridge) {
		
			ExprSqlBridge bridge = (ExprSqlBridge)et;
			
			SqlExpr result = bridge.getSqlExpr();

			return result;
		} else {
			throw new RuntimeException("Could not completely rewrite: " + rewrite + " --- stopped at: " + et);
		}
	}
	
}