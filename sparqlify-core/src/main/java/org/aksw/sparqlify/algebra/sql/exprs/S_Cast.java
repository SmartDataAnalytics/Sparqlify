package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.DatatypeSystemOld;
import org.aksw.sparqlify.core.SqlDatatype;

public class S_Cast
	extends SqlExpr1
{

	public S_Cast(SqlExpr expr, SqlDatatype datatype) {
		super(expr, datatype);
	}

	public static S_Cast create(SqlExpr value, String datatypeId, DatatypeSystemOld system) {
		//system.getByName(name)
		SqlDatatype datatype = system.getByName(datatypeId);
		if(datatype == null) {
			throw new RuntimeException("Unknown datatype: " + datatypeId);
		}
		
		return new S_Cast(value, datatype);
	}
}
