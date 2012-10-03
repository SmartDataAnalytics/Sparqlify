package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;

import mapping.ExprCopy;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * Expand all arguments of any concat expression into this expression.
 * Also merges consecutive constants.
 * 
 */
public class ExprTransformerConcatNested
	implements ExprTransformer
{	
	@Override
	public Expr transform(ExprFunction fn) {
		
		List<Expr> newArgs = new ArrayList<Expr>();
		for(Expr arg : fn.getArgs()) {
			if(SqlTranslationUtils.isConcatExpr(arg)) {
				
				ExprFunction fnArg = arg.getFunction();
				for(Expr a : fnArg.getArgs()) {
					newArgs.add(a);
				}
			} else {
				newArgs.add(arg);
			}
		}
		
		ExprList merged = SqlTranslationUtils.mergeConsecutiveConstants(newArgs);
		
		
		Expr result = ExprCopy.getInstance().copy(fn, merged);

		return result;
	}	
}