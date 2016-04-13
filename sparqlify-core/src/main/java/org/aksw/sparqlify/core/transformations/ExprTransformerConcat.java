package org.aksw.sparqlify.core.transformations;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.trash.ExprCopy;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.vocabulary.XSD;

/**
 * Expand all arguments of any concat expression into this expression.
 * Also merges consecutive constants.
 * 
 */
public class ExprTransformerConcat
	implements ExprTransformer
{	
	@Override
	public E_RdfTerm transform(Expr fn, List<E_RdfTerm> exprs) {
		
		List<Expr> newArgs = new ArrayList<Expr>();
		for(E_RdfTerm expr : exprs) {
			Expr arg = expr.getLexicalValue();
			
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
		
		
		Expr newVal = ExprCopy.getInstance().copy(fn, merged);
		
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(newVal, XSD.xstring);
		

		return result;
	}	
}
