package org.aksw.sparqlify.algebra.sparql.expr;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;
import org.apache.jena.sparql.expr.NodeValue;

public class E_Cast
	extends ExprFunction2
{

	public E_Cast(Expr expr1, Expr expr2) {
		super(expr1, expr2, "cast");
	}

	@Override
	public NodeValue eval(NodeValue x, NodeValue y) {
		throw new RuntimeException("Not implemented, and should not be called anyway");
	}

	@Override
	public Expr copy(Expr arg1, Expr arg2) {
		return new E_Cast(arg1, arg2);
	}

}
