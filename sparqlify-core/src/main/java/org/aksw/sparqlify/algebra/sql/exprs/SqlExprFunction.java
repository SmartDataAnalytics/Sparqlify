package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

import org.aksw.sparqlify.core.SqlDatatype;

// Not used yet
public class SqlExprFunction
	extends SqlExprBase
{
	private String name;
	
	private List<SqlExpr> args;
	
	public List<SqlExpr> getArgs() {
		return args;
	}
	
	public SqlExprFunction(String name, List<SqlExpr> args, SqlDatatype datatype) {
		super(datatype);
		this.args = args;
	}
	
	public String getName() {
		return name;
	}
}
