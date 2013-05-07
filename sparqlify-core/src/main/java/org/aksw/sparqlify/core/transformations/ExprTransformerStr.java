package org.aksw.sparqlify.core.transformations;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.vocabulary.XSD;

public class ExprTransformerStr
	extends ExprTransformerBase1
{

	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a) {
		
		Expr av = a.getLexicalValue();

		Expr c = new E_Str(av);
		
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(c, NodeValue.makeString(XSD.xstring.getURI()));
		
		return result;
	}

}
