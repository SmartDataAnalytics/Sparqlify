package org.aksw.sparqlify.algebra.sparql.expr.old;

import java.sql.SQLException;

import org.postgis.PGgeometry;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class E_GeographyFromText
	extends ExprFunction1
{
	private static final String symbol = "ST_GeographyFromText" ;
	
	
	public E_GeographyFromText(Expr arg) {
		super(arg, symbol);
	}
	
	
	@Override
	public NodeValue eval(NodeValue v) {
		try {
			return new NodeValueGeom(new PGgeometry(v.getString()));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Expr copy(Expr expr) {
		return new E_GeographyFromText(expr);
	}
}