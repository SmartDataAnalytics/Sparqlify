package org.aksw.sparqlify.util;

import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr1;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr2;


public class SqlExprAccessor
	implements ExprAccessor<SqlExpr>
{

	@Override
	public boolean isLogicalNot(SqlExpr expr) {
		return expr instanceof S_LogicalNot;
	}

	@Override
	public boolean isLogicalAnd(SqlExpr expr) {
		return expr instanceof S_LogicalAnd;
	}

	@Override
	public boolean isLogicalOr(SqlExpr expr) {
		return expr instanceof S_LogicalOr;
	}

	@Override
	public SqlExpr getArg(SqlExpr expr) {
		return ((SqlExpr1)expr).getExpr();
	}

	@Override
	public SqlExpr getArg1(SqlExpr expr) {
		return ((SqlExpr2)expr).getLeft();
	}

	@Override
	public SqlExpr getArg2(SqlExpr expr) {
		return ((SqlExpr2)expr).getRight();
	}

	@Override
	public SqlExpr createLogicalAnd(SqlExpr a, SqlExpr b) {
		return S_LogicalAnd.create(a, b);
	}

	@Override
	public SqlExpr createLogicalOr(SqlExpr a, SqlExpr b) {
		return S_LogicalOr.create(a, b);
	}

	@Override
	public SqlExpr createLogicalNot(SqlExpr expr) {
		return S_LogicalNot.create(expr);
	}

}
