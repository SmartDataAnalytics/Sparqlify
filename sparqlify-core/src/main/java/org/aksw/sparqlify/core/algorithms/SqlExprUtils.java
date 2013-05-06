package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.factory.Factory2;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.expr.util.ExprUtils;

import com.google.common.collect.Iterables;

public class SqlExprUtils
{
	public static Set<S_ColumnRef> getColumnReferences(SqlExpr expr) {
		Set<S_ColumnRef> result = new HashSet<S_ColumnRef>();
		
		collectColumnReferences(expr, result);
		
		return result;
	}

	public static void collectColumnReferences(SqlExpr expr, Collection<S_ColumnRef> result) {
		if(expr.isFunction()) {
			
			List<SqlExpr> args = expr.getArgs();
			for(SqlExpr arg : args) {
				collectColumnReferences(arg, result);
			}
			
		}
		else if(expr.isConstant()) {
			// Nothing to do
		}
		else if(expr.isVariable()) {
			S_ColumnRef columnRef = (S_ColumnRef)expr;
			result.add(columnRef);
		}
		else {
			throw new RuntimeException("Should not happen");
		}
	}

	
	
	public static List<SqlExpr> toDnf(Collection<? extends Iterable<SqlExpr>> clauses) {
		List<SqlExpr> result = new ArrayList<SqlExpr>();
		
		if(clauses.size() == 1) {
			Iterable<SqlExpr> itr = clauses.iterator().next();
			
			Iterables.addAll(result, itr);
		}
		else if(!clauses.isEmpty()) {
			List<SqlExpr> ors = new ArrayList<SqlExpr>();
			for(Iterable<SqlExpr> clause : clauses) {
				SqlExpr and = andifyBalanced(clause);
				ors.add(and);
			}
			
			SqlExpr or = orifyBalanced(ors);
			result.add(or);
		}
		
		return result;
	}
	
	
	public static SqlExpr orifyBalanced(Iterable<SqlExpr> exprs) {
		return ExprUtils.opifyBalanced(exprs, new Factory2<SqlExpr>() {
			@Override
			public SqlExpr create(SqlExpr a, SqlExpr b)
			{
				return new S_LogicalOr(a, b);
			}
		});		
	}	
	
	public static SqlExpr andifyBalanced(Iterable<SqlExpr> exprs) {
		return ExprUtils.opifyBalanced(exprs, new Factory2<SqlExpr>() {
			@Override
			public SqlExpr create(SqlExpr a, SqlExpr b)
			{
				return new S_LogicalAnd(a, b);
			}
		});		
	}	
	
	public static boolean containsFalse(Iterable<SqlExpr> exprs, boolean includeTypeErrors) {
		for(SqlExpr expr : exprs) {
			if(S_Constant.FALSE.equals(expr) || (includeTypeErrors && S_Constant.TYPE_ERROR.equals(expr))) {
				return true;
			}
		}
		
		return false;
	}

}
