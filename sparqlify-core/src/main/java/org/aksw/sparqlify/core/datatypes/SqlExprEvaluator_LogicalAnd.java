package org.aksw.sparqlify.core.datatypes;

import java.util.Arrays;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Serialize;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;

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
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("AND");
			result = new S_Serialize(TypeToken.Boolean, "AND", Arrays.asList(a, b), serializer);
		}

		
		return result;
	}
}
