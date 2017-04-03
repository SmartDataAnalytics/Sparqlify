package org.aksw.sparqlify.core.sql.expr.evaluation;

import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.datatypes.SqlExprOps;

/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_LogicalOr
	extends SqlExprEvaluator2
{
	@Override
	public SqlExpr eval(SqlExpr a, SqlExpr b) {
		SqlExpr result = SqlExprOps.logicalOr(a, b);
		if(result == null) {
			result = new S_LogicalOr(a, b);
			
			//SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("OR");
			//result = new S_Serialize(TypeToken.Boolean, "OR", Arrays.asList(a, b), serializer);
		}
		
		return result;
	}
}
