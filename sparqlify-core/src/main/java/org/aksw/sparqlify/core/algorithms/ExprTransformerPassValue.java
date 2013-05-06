package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;
import org.aksw.sparqlify.trash.ExprCopy;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

/**
 * Expr transformer for rdfTerm expressions:
 * the value field of such expressions is passed through
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ExprTransformerPassValue
	implements ExprTransformer
{	
	public ExprTransformerPassValue() {
	}

	@Override
	public Expr transform(ExprFunction fn) {
		List<Expr> args = fn.getArgs();
		List<Expr> newArgs = new ArrayList<Expr>(args.size());
		
		for(Expr arg : args) {
			Expr newArg = SqlTranslationUtils.getLexicalValueOrExpr(arg);
			newArgs.add(newArg);
		}
		
		Expr result = ExprCopy.getInstance().copy(fn, newArgs);
		
		return result;
	}
}