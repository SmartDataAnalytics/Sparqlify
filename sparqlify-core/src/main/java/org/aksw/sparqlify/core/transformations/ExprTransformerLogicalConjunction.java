package org.aksw.sparqlify.core.transformations;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.trash.ExprCopy;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.vocabulary.XSD;

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
