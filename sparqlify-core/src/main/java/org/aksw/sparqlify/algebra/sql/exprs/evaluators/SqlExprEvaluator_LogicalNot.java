package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.datatypes.SqlExprEvaluator1;
import org.aksw.sparqlify.core.datatypes.SqlExprOps;

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
