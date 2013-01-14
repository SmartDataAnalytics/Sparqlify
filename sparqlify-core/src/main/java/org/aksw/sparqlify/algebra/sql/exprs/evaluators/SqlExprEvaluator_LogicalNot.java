package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
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

		//String opSymbol = "NOT";
		if(result == null) {
			result = new S_LogicalNot(a); 
			//SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1(opSymbol);
			//result = new S_Serialize(TypeToken.Boolean, "NOT", Arrays.asList(a), serializer);
			
		}


		return result;
	}
}
