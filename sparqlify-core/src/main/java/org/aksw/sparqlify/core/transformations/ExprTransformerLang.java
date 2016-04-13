package org.aksw.sparqlify.core.transformations;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.XSD;

public class ExprTransformerLang
	extends ExprTransformerBase1
{
	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm expr) {

		Expr lang = expr.getLanguageTag();
		
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(lang, XSD.xstring);
				
		return result;
	}
}
