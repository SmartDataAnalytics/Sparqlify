package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystem;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

public class S_Substract
	extends S_Arithmetic
{

	public S_Substract(SqlExpr left, SqlExpr right, SqlDatatype datatype) {
		super("-", left, right, datatype);
		// TODO Auto-generated constructor stub
	}
	
	public static SqlExpr create(SqlExpr left, SqlExpr right, DatatypeSystem system) {
		// TODO: Datatype must also be numeric
		// TODO: The common datatype is not the right one: short + short -> integer
		SqlDatatype common = S_Equal.getCommonDataype(left, right, system);
		if(common == null) {
			return SqlExprValue.FALSE;	
		}
				
		return new S_Substract(left, right, common);
	}

}
