package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.Arrays;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Serialize;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
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
	@Override()
	public SqlExpr eval(SqlExpr a) {
		SqlExpr result = SqlExprOps.logicalNot(a);

		String opSymbol = "NOT";
		if(result == null) {
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1(opSymbol);
			result = new S_Serialize(TypeToken.Boolean, "NOT", Arrays.asList(a), serializer);
		}


		return result;
	}
}
