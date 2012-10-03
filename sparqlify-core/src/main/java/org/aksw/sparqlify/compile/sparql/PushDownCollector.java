package org.aksw.sparqlify.compile.sparql;

import java.util.Map;

import mapping.ExprCopy;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.old.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;

public class PushDownCollector {
	public static Expr collect(Expr expr, Generator generator, Map<Var, SqlExpr> sql)
	{
		Expr result = (Expr)MultiMethod.invokeStatic(PushDownCollector.class, "_collect", expr, generator, sql);
		return result;
	}

	public static ExprList collectArgs(Iterable<Expr> exprs, Generator generator, Map<Var, SqlExpr> sql) {
		ExprList result = new ExprList();
		for(Expr expr : exprs) {
			Expr tmp = collect(expr, generator, sql);
			result.add(tmp);
		}
		
		return result;
	}

	
	public static Expr _collect(Expr expr, Generator generator, Map<Var, SqlExpr> sql) {
		return expr;
	}

	public static Expr _collect(ExprFunction expr, Generator generator, Map<Var, SqlExpr> sql) {
		ExprList args = collectArgs(expr.getArgs(), generator, sql);
		Expr result = ExprCopy.getInstance().copy(expr, args);
		return result;
	}

	
	public static Expr _collect(ExprSqlBridge expr, Generator generator, Map<Var, SqlExpr> sql) {
		Var id = Var.alloc(generator.next());
	
		sql.put(id, expr.getSqlExpr());
		return new ExprVar(id);
	}
}
