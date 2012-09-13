package org.aksw.sparqlify.algebra.sparql.transform;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;

public interface SqlFunctionDefinition
{
	public String getName();
	
	/**
	 * Create an instance of this function with the specified arguments
	 * @param args
	 * @return
	 */
	SqlExpr create(SqlExprList args);
}