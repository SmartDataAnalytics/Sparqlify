package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;

public class S_GeographyFromText
	extends SqlExpr1
{
	public S_GeographyFromText(SqlExpr expr) {
		super(expr, DatatypeSystemDefault._GEOGRAPHY);
	}
	
	public static SqlExpr create(SqlExpr expr) {
		if(!(expr.getDatatype().equals(DatatypeSystemDefault._STRING))) {
			return null;
		}
		
		return new S_GeographyFromText(expr);
	}
}
