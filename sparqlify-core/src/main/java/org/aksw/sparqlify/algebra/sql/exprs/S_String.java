package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

import org.aksw.sparqlify.core.SqlDatatype;

/**
 * A piece of SQL code. Ideally this class should be avoided in favor
 * of expressing SQL expressions in a structured way. However, its so
 * much quicker to hack something up that way.
 * 
 * Dependencies on expressions or columns may be given 
 *
 * @author raven
 *
 */
public class S_String
	extends SqlExprN
{
	private String sqlString;
	
	public S_String(String sqlString, SqlDatatype datatype, List<SqlExpr> sqlExprs) {
		super(sqlExprs, datatype);
		
		this.sqlString = sqlString;
	}
	
	public String getSqlString() {
		return sqlString;
	}
	
	@Override
	public String toString() {
		return getSqlString();
	}
}
