package org.aksw.sparqlify.core.rewrite.expr.transform;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
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
