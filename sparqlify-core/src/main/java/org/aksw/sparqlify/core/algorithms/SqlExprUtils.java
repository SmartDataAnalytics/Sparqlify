package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.commons.factory.Factory2;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_IsNotNull;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.aksw.sparqlify.util.CnfTransformer;
import org.aksw.sparqlify.util.ExprAccessor;
import org.aksw.sparqlify.util.SqlExprAccessor;

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

	
	
	
	
	//public static final CnfTransformer<SqlExpr> cnfTransformer = new CnfTransformer<SqlExpr>(new SqlExprAccessor());
	public static final ExprAccessor<SqlExpr> accessor = new SqlExprAccessor();
	
	public static SqlExpr toCnfExpr(SqlExpr expr) {
		SqlExpr result = CnfTransformer.eval(expr, accessor);
		return result;
	}

	public static List<Collection<SqlExpr>> toCnf(SqlExpr expr) {
		SqlExpr tmp = toCnfExpr(expr);
		List<Collection<SqlExpr>> result = CnfTransformer.toCnf(tmp, accessor);
		return result;
	}

	public static List<Collection<SqlExpr>> toCnf(Iterable<SqlExpr> exprs) {

		List<Collection<SqlExpr>> result = CnfTransformer.toCnf(exprs, accessor);
		return result;
	}

	public static void optimizeNotNullInPlace(List<Collection<SqlExpr>> cnf) {
		
		for(Collection<SqlExpr> clause : cnf) {
			Iterator<SqlExpr> it = clause.iterator();
			
			Set<S_ColumnRef> columnRefs = new HashSet<S_ColumnRef>();
			
			for(SqlExpr expr : clause) {
				if(expr instanceof S_IsNotNull) {
					SqlExprUtils.collectColumnReferences(expr, columnRefs);
					
				}				
			}
			
			while(it.hasNext()) {	
				SqlExpr expr = it.next();
				
				if(expr instanceof S_IsNotNull) {
					S_IsNotNull tmp = (S_IsNotNull)expr;
					SqlExpr arg = tmp.getExpr();
					
					boolean contained = columnRefs.contains(arg);
					if(contained) {
						it.remove();
					}
				}
			}
		}
	}

}
