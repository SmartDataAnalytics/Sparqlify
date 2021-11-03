package org.aksw.sparqlify.core.sql.expr.evaluation;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

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
	    return new S_Concat(args);
//		
//		SqlFunctionSerializer serializer = new SqlFunctionSerializer_Join("||");
//		SqlExpr result = new S_Serialize(TypeToken.String, "concat", args, serializer);
//		
//		return result;
	}
}
