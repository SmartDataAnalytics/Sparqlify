package org.aksw.sparqlify.core.datatypes;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_LogicalNot
	extends SqlExprEvaluator1
{
	@Override
	public SqlExpr eval(SqlExpr a) {
		SqlExpr result = SqlExprOps.logicalNot(a);
		return result;
	}
}
