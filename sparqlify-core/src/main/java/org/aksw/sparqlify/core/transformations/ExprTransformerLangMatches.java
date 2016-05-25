package org.aksw.sparqlify.core.transformations;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.XSD;

public class ExprTransformerLangMatches
	extends ExprTransformerBase2
{

	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a, E_RdfTerm b) {
		
		Expr x = a.getLexicalValue();
		Expr y = b.getLexicalValue();
		
		E_Equals inner = new E_Equals(x, y);

		E_RdfTerm result = E_RdfTerm.createTypedLiteral(inner, XSD.xboolean);

		return result;
	}
}
