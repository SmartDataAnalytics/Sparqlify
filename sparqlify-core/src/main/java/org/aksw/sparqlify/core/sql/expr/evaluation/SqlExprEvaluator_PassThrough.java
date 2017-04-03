package org.aksw.sparqlify.core.sql.expr.evaluation;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;

public class SqlExprEvaluator_PassThrough
	implements SqlExprEvaluator
{
	private TypeToken typeName;
	private String functionName;
	
	public SqlExprEvaluator_PassThrough(TypeToken typeName, String functionName) {
		this.typeName = typeName;
		this.functionName = functionName;
	}
	
	@Override
	public SqlExpr eval(List<SqlExpr> args) {

		SqlExpr result = new S_Function(typeName, functionName, args);
		
		return result;
	}
}
