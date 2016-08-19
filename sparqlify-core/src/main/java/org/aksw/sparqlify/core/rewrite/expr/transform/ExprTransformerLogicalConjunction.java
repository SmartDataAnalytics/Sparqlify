package org.aksw.sparqlify.core.rewrite.expr.transform;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.ExprCopy;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.XSD;

public class ExprTransformerLogicalConjunction
	extends ExprTransformerBase2
{

	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a, E_RdfTerm b) {

		Expr av = a.getLexicalValue();
		Expr bv = b.getLexicalValue();
		
		Expr newVal = ExprCopy.getInstance().copy(orig, av, bv); 

		E_RdfTerm result = E_RdfTerm.createTypedLiteral(newVal, XSD.xboolean);

		return result;
	}

}
