package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.DatatypeSystem;
import org.aksw.sparqlify.core.SqlDatatype;

public class S_Add
	extends S_Arithmetic
{

	public S_Add(SqlExpr left, SqlExpr right, SqlDatatype datatype) {
		super("+", left, right, datatype);
		// TODO Auto-generated constructor stub
	}

	public static SqlExpr create(SqlExpr left, SqlExpr right, DatatypeSystem system) {
		// TODO: Datatype must also be numeric
		SqlDatatype common = S_Equal.getCommonDataype(left, right, system);
		if(common == null) {
			return SqlExprValue.FALSE;	
		}
		
		return new S_Add(left, right, common);
	}
}
