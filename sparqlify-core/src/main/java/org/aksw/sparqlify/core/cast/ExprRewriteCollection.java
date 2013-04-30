package org.aksw.sparqlify.core.cast;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;

public class ExprRewriteCollection {

	private Set<Expr> rewriteExprs = new HashSet<Expr>();

	private ExprList paramVars;
	private Var varArgVar; // A variable denoting a variable argument list.
	
	public ExprRewriteCollection(ExprList paramVars, Var varArgVar) {
		this.paramVars = paramVars;
		this.varArgVar = varArgVar;
	}

	public void addExpr(Expr rewriteExpr) {
		
		// TODO validate
		
		this.rewriteExprs.add(rewriteExpr);
	}
}