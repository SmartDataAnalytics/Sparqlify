package org.aksw.sparqlify.core.algorithms;

import java.util.List;

import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;

public class ExprTransformerLang
	implements ExprTransformer
{
	@Override
	public Expr transform(ExprFunction expr) {
		
		List<Expr> args = expr.getArgs();
		if(args.size() != 1) {
			throw new RuntimeException("Invalid number of arguments; 1 expected, got: " + expr);
		}
		
		// FIXME Check whether lang has the right number of argument
		Expr arg = args.get(0);
		
		
		Expr result = SqlTranslationUtils.extractLanguageTag(arg);
		if(result == null) {
			throw new RuntimeException("Could not handle extraction of a language tag for: " + expr);
		}
		
		return result;
	}
}
