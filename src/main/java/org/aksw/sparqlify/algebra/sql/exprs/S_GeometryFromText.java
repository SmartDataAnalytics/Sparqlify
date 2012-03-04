package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;

public class S_GeometryFromText
	extends SqlExpr1
{
	public S_GeometryFromText(SqlExpr expr) {
		super(expr, DatatypeSystemDefault._GEOMETRY);
	}
	
	public static SqlExpr create(SqlExpr expr) {
		if(!(expr.getDatatype().equals(DatatypeSystemDefault._STRING))) {
			return null;
		}
		
		return new S_GeometryFromText(expr);
	}
}