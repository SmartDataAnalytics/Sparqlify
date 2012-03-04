package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystem;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

public class S_GreaterThanOrEqual
	extends SqlExpr2
{
	public S_GreaterThanOrEqual(SqlExpr left, SqlExpr right, SqlDatatype datatype) {
		super(left, right, datatype);
	}
	
	public static SqlExpr create(SqlExpr left, SqlExpr right, DatatypeSystem system) {
		if(S_Equal.getCommonDataype(left, right, system) == null) {
			return SqlExprValue.FALSE;	
		}
		
		return new S_GreaterThanOrEqual(left, right, DatatypeSystemDefault._BOOLEAN);
	}

}
