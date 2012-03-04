package org.aksw.sparqlify.algebra.sparql.expr;

import java.sql.SQLException;

import org.apache.commons.lang.NotImplementedException;
import org.postgis.PGgeometry;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class E_Intersects
	extends ExprFunction2
{
    private static final String symbol = "ST_Intersects" ;

	
	public E_Intersects(Expr a, Expr b) {
		super(a, b, symbol);
	}

	@Override
	public NodeValue eval(NodeValue x, NodeValue y) {
		throw new NotImplementedException();
	}


	@Override
	public Expr copy(Expr arg1, Expr arg2) {
		return new E_Intersects(arg1, arg2);
	}
}
