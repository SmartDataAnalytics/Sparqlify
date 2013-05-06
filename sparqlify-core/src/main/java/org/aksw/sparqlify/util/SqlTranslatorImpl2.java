package org.aksw.sparqlify.util;

import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sql.exprs2.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutor;
import org.aksw.sparqlify.core.cast.TypedExprTransformer;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.core.transformations.RdfTermEliminator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class SqlTranslatorImpl2
	implements SqlTranslator
{
	private static final Logger logger = LoggerFactory.getLogger(SqlTranslatorImpl2.class);
	
	private ExprBindingSubstitutor exprBindingSubstitutor;
	private RdfTermEliminator rdfTermEliminator;
	private ExprEvaluator exprEvaluator;
	private TypedExprTransformer typedExprTransformer;

	public SqlTranslatorImpl2(ExprBindingSubstitutor exprBindingSubstitutor, RdfTermEliminator rdfTermEliminator, ExprEvaluator exprTransformer, TypedExprTransformer typedExprTransformer) {
		this.exprBindingSubstitutor = exprBindingSubstitutor;
		this.rdfTermEliminator = rdfTermEliminator;
		this.exprEvaluator = exprTransformer;
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
		
		E_RdfTerm e2 = rdfTermEliminator._transform(e1);
		
		Expr e3 = exprEvaluator.transform(e2);
		//System.out.println("[ExprRewrite Phase 2]: " + e2);

		ExprSqlRewrite e4 = typedExprTransformer.rewrite(e3, typeMap);
		//logger.debug("[ExprRewrite Phase 4]: " + e4);

		return e4;
	}

	/**
	 * Assumes that the rewrite's root expression is of type E_RdfTerm.
	 * Returns the SQL expression (if exists) corresponding to its value component.
	 * 
	 * @param rewrite
	 * @return
	 */
	public static SqlExpr asSqlExpr(ExprSqlRewrite rewrite) {
		
		Expr tmp = rewrite.getExpr();
	
		if(!(tmp instanceof E_RdfTerm)) {
			throw new RuntimeException("Wrong expression type - hould not happen");
		}
		
		E_RdfTerm rdfTerm = (E_RdfTerm)tmp;
		Expr et = rdfTerm.getLexicalValue();
		
		if(et.isVariable()) {

			String varName = et.getVarName();
			SqlExpr result = rewrite.getProjection().getNameToExpr().get(varName);

			return result;
		} else {
			throw new RuntimeException("Could not completely rewrite: " + rewrite + " --- stopped at: " + et);
		}
	}

	
	public static SqlExpr asSqlExprOld(ExprSqlRewrite rewrite) {
		
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