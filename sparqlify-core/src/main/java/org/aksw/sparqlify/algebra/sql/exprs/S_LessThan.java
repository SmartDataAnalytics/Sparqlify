package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.DatatypeSystem;
import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.SqlDatatype;

/**
 * 
 * 
 * @author raven
 *
 */
public class S_LessThan
	extends SqlExpr2
{
	public S_LessThan(SqlExpr left, SqlExpr right, SqlDatatype datatype) {
		super(left, right, datatype);
	}
	
	
	public static SqlExpr create(SqlExpr left, SqlExpr right, DatatypeSystem system) {
		if(S_Equal.getCommonDataype(left, right, system) == null) {
			return SqlExprValue.FALSE;	
		}
		
		return new S_LessThan(left, right, DatatypeSystemDefault._BOOLEAN);
	}
}
