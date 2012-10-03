package org.aksw.sparqlify.core.algorithms;

import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

/**
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */

public class ExprTransformerRdfTermComparator
	implements ExprTransformer
{
	private ExprEvaluator exprEvaluator;
	
	public ExprTransformerRdfTermComparator(ExprEvaluator exprEvaluator) {
		this.exprEvaluator = exprEvaluator;
	}
	
	public Expr handleConcat(ExprFunction fn) {
		
		Expr result = SqlTranslationUtils.optimizeEqualsConcat(fn);
		
		return result;
	}

	@Override
	public Expr transform(ExprFunction fn) {
	
		Expr result = null;
		
		List<Expr> exprs = fn.getArgs();
		
		Expr left = exprs.get(0);
		Expr right = exprs.get(1);
		
				
		E_RdfTerm leftTerm = SqlTranslationUtils.expandRdfTerm(left);
		E_RdfTerm rightTerm = SqlTranslationUtils.expandRdfTerm(right);
		
		// If none of the arguments is a E_rdfTerm, continue with further checks
		if(leftTerm == null && rightTerm == null) {
			
			Expr tmp = handleConcat(fn);
			
			return tmp;
		}
		
		// However, if one of the arguments is one, transform
		if(leftTerm == null) {
			leftTerm = SqlTranslationUtils.expandConstant(left);
		}
		
		if(rightTerm == null) {
			
			rightTerm = SqlTranslationUtils.expandConstant(right);
			
		}
	
		if(leftTerm != null && rightTerm != null) {
			
			Expr eqT = new E_Equals(leftTerm.getType(), rightTerm.getType());
			Expr eqV = new E_Equals(leftTerm.getLexicalValue(), rightTerm.getLexicalValue());
			Expr eqD = new E_Equals(leftTerm.getDatatype(), rightTerm.getDatatype());
			Expr eqL = new E_Equals(leftTerm.getLanguageTag(), rightTerm.getLanguageTag());
			
			Expr tmp =
					new E_LogicalAnd(
							new E_LogicalAnd(eqT, eqV),
							new E_LogicalAnd(eqD, eqL)
					);
			
			result = exprEvaluator.eval(tmp, null);
			
		} else {
			
			throw new RuntimeException("Should not happen: " + fn);
		}
		
		
		
		return result;
	}
	
}