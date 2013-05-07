package org.aksw.sparqlify.core.transformations;

import org.aksw.sparqlify.algebra.sparql.expr.E_Cast;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.expr.util.ExprUtils;

import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class ExprTransformerCast
	extends ExprTransformerBase1
{

	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a) {
		
		String typeName = ExprUtils.getFunctionId(orig.getFunction());
		NodeValue typeExpr = NodeValue.makeString(typeName);
		
		Expr av = a.getLexicalValue();
	
		//Expr c = new E_Cast(av, bv);
		Expr c = new E_Function(typeName, new ExprList(av));
		
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(c, typeExpr);
		
		return result;
	}

}


@Deprecated
class ExprTransformerCastOld
	extends ExprTransformerBase2
{

	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a, E_RdfTerm b) {
		
		Expr av = a.getLexicalValue();
		Expr bv = b.getLexicalValue();

		Expr c = new E_Cast(av, bv);
		
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(c, bv);
		
		return result;
	}

}
