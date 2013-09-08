package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.expr.util.SqlExprUtils;
import org.apache.jena.atlas.io.IndentedWriter;

public class SqlAggFunction
	//extends SqlExprFunction
	implements SqlAggregator
{
	private SqlExprFunction fn;
	
	public SqlAggFunction(SqlExprFunction fn) {
		this.fn = fn;
		
		if(fn.getArgs().size() > 1) {
			throw new RuntimeException("Aggregators may at most have 1 argument, got: " + fn.getArgs());
		}
	}

	@Override
	public SqlExpr getExpr() {
		List<SqlExpr> args = fn.getArgs();
		
		SqlExpr result = args.isEmpty() ? null : args.get(0);
		return result;
	}
	
	public TypeToken getDatatype() {
    	return fn.getDatatype();
	}
	
	@Override
	public String toString() {
		return fn.toString();
	}

	@Override
	public void asString(IndentedWriter writer) {
		String str = toString();
		writer.print(str);
	}

	@Override
	public SqlAggFunction copy(SqlExpr arg) {
		SqlExprFunction cp = fn.copy(SqlExprUtils.exprToList(arg));
		
		SqlAggFunction result = new SqlAggFunction(cp);
		
		return result;
	}
}
