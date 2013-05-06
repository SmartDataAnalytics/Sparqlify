package org.aksw.sparqlify.core.transformations;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.vocabulary.XSD;

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
