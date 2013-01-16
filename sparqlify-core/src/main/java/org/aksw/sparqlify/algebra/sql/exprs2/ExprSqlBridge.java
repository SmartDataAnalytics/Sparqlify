package org.aksw.sparqlify.algebra.sql.exprs2;


import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

public class ExprSqlBridge
	extends ExprFunction0
{
	private SqlExpr sqlExpr;

	public ExprSqlBridge(SqlExpr sqlExpr) {
		super(ExprSqlBridge.class.toString());
		this.sqlExpr = sqlExpr;
	}

	public SqlExpr getSqlExpr() {
		return sqlExpr;
	}

	@Override
	public NodeValue eval(FunctionEnv env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expr copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "ExprSqlBridge(" + sqlExpr.toString() + ")";
	}

}
