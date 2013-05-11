package org.aksw.sparqlify.core.cast;

import org.aksw.commons.util.factory.Factory2;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Add;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs2.S_GreaterThan;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LessThan;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LessThanOrEqual;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Multiply;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Substract;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

public class SqlExprFactoryUtils {

	public static final Factory2<SqlExpr> factoryLessThan = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_LessThan(a, b);
		}
	};

	public static final Factory2<SqlExpr> factoryLessThanOrEqual = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_LessThanOrEqual(a, b);
		}
	};

	public static final Factory2<SqlExpr> factoryEqual = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_Equals(a, b);
		}
	};

	public static final Factory2<SqlExpr> factoryGreaterThan = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_GreaterThan(a, b);
		}
	};

	public static final Factory2<SqlExpr> factoryGreaterThanOrEqual = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_GreaterThan(a, b);
		}
	};
	
	public static final Factory2<SqlExpr> factoryNumericPlus = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_Add(a, b);
		}
	};

	public static final Factory2<SqlExpr> factoryNumericMinus = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_Substract(a, b);
		}
	};

	public static final Factory2<SqlExpr> factoryNumericMultiply = new Factory2<SqlExpr>() {
		@Override
		public SqlExpr create(SqlExpr a, SqlExpr b) {
			return new S_Multiply(a, b);
		}
	};

//	public static final Factory2<SqlExpr> factoryNumericDivide = new Factory2<SqlExpr>() {
//		@Override
//		public SqlExpr create(SqlExpr a, SqlExpr b) {
//			return new S_Di(a, b);
//		}
//	};

	
}