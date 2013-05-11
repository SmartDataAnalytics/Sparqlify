package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.datatypes.SqlExprOps;

/**
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_ParseInt
	extends SqlExprEvaluator1
{
	@Override
	public SqlExpr eval(SqlExpr a) {
		SqlValue value = a.asConstant().getValue();
		String str = "" + value.getValue();

		long val;
		try {
			val = Long.parseLong(str);
		} catch (Exception e){
			return S_Constant.TYPE_ERROR;
		}
		
		S_Constant result = S_Constant.create(new SqlValue(TypeToken.Int, val));
		
		
		return result;
	}
}
