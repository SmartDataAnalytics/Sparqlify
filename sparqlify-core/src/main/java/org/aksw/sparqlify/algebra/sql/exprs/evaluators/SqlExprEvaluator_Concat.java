package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

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
public class SqlExprEvaluator_Concat
	implements SqlExprEvaluator
{
	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		
		SqlFunctionSerializer serializer = new SqlFunctionSerializer_Join("||");
		SqlExpr result = new S_Serialize(TypeToken.String, "concat", args, serializer);
		
		return result;
	}
}
