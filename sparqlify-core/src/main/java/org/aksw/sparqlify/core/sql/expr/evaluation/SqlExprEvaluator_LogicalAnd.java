package org.aksw.sparqlify.core.sql.expr.evaluation;

import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.datatypes.SqlExprOps;

/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_LogicalAnd
	extends SqlExprEvaluator2
{
	@Override
	public SqlExpr eval(SqlExpr a, SqlExpr b) {
		SqlExpr result = SqlExprOps.logicalAnd(a, b);
		if(result == null) {
			result = new S_LogicalAnd(a, b);
			
			//SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("AND");
			//result = new S_Serialize(TypeToken.Boolean, "AND", Arrays.asList(a, b), serializer);
		}

		
		return result;
	}
}
