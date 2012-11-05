package org.aksw.sparqlify.core.datatypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mapping.SparqlifyConstants;

import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;

import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCountVarDistinct;
import com.hp.hpl.jena.sparql.function.FunctionEnv;


/**
 * Implementation of a SPARQL function via an SQL one
 *
 * e.g.
 * agg:count() -> typedLiteral(sqlCount(), xsd:long)
 * 
 * 
 * 
 * @author Claus Stadler
 *
 */
public class SparqlSqlFunction {
	private List<Var> paramVars;
	private Expr expr;


	public SparqlSqlFunction(List<Var> paramVars, Expr expr) {
		this.paramVars = paramVars;
		this.expr = expr;

		assert isValid(paramVars, expr) : "paramVars must be a subset of expr vars";
	}

	public List<Var> getParamVars() {
		return paramVars;
	}

	public Expr getExpr() {
		return expr;
	}

	public static boolean isValid(List<Var> paramVars, Expr expr) {
		Set<Var> exprVars = expr.getVarsMentioned();
		
		boolean result = exprVars.containsAll(paramVars);
		return result;
	}
	
	public Expr instanciate(List<Expr> args) {
		if(args.size() != paramVars.size()) {
			throw new IllegalArgumentException("Invalid number of args. Expected: " + paramVars + ", got: " + args);
		}
		
		Map<Var, Expr> binding = new HashMap<Var, Expr>();
		for(int i = 0; i < paramVars.size(); ++i) {
			Var var = paramVars.get(i);
			Expr arg = args.get(i);
			
			binding.put(var, arg);
		}
		
		Expr result = instanciate(binding);
		return result;
	}

	public Expr instanciate(Map<Var, Expr> binding) {
		// FIXME Check if all paramVars are bound
		
		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(binding);
		
		Expr result = substitutor.transformMM(expr);
		
		return result;
	}
}

/*
 * Below classes might be useful in the translation process from
 * rewriting Exprs to Exprs and SqlExprs
 * 
 */

interface TermCtor
	extends Expr
{

}

abstract class ExprTermCtorBase
	extends ExprFunction0
	implements TermCtor
{
	protected ExprTermCtorBase(String fName) {
		super(fName);
	}

	@Override
	public NodeValue eval(FunctionEnv env) {
		throw new RuntimeException("Not supported");
//		return null;
	}
}


abstract class ExprTermCtorBase1
	extends ExprTermCtorBase
{	
	protected SqlExpr arg;

	protected ExprTermCtorBase1(String fName, SqlExpr arg) {
		super(fName);
		this.arg = arg;
	}

	public SqlExpr getArg() {
		return arg;
	}
}


abstract class ExprTermCtorBase2
	extends ExprTermCtorBase
{	
	protected SqlExpr arg1;
	protected SqlExpr arg2;

	protected ExprTermCtorBase2(String fName, SqlExpr arg1, SqlExpr arg2) {
		super(fName);
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

}


class E_BlankNodeCtor
	extends ExprTermCtorBase1
{
	public E_BlankNodeCtor(SqlExpr value) {
		super(SparqlifyConstants.blankNodeLabel, value);
	}
	
	@Override
	public Expr copy() {
		Expr result = new E_BlankNodeCtor(arg);
		return result;
	}
}

class E_UriCtor
	extends ExprTermCtorBase1
{
	public E_UriCtor(SqlExpr value) {
		super(SparqlifyConstants.uriLabel, value);
	}
	
	@Override
	public Expr copy() {
		Expr result = new E_UriCtor(arg);
		return result;
	}
}

class E_PlainLiteralCtor
	extends ExprTermCtorBase2
{
	public E_PlainLiteralCtor(SqlExpr value, SqlExpr lang) {
		super(SparqlifyConstants.plainLiteralLabel, value, lang);
	}
	
	@Override
	public Expr copy() {
		Expr result = new E_PlainLiteralCtor(arg1, arg2);
		return result;
	}
}

class E_TypedLiteralCtor
	extends ExprTermCtorBase2
{
	public E_TypedLiteralCtor(SqlExpr value, SqlExpr lang) {
		super(SparqlifyConstants.plainLiteralLabel, value, lang);
	}
	
	@Override
	public Expr copy() {
		Expr result = new E_TypedLiteralCtor(arg1, arg2);
		return result;
	}
}

