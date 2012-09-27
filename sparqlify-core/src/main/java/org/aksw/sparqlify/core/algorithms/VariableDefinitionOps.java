package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.domain.RestrictedExpr;
import org.aksw.sparqlify.restriction.RestrictionSet;

import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;

public class VariableDefinitionOps {

	public static RestrictedExpr<Expr> plus(RestrictedExpr<Expr> a, RestrictedExpr<Expr> b) {
		RestrictedExpr<Expr> result =
				RestrictedExpr.create((Expr)new E_Add(a.getExpr(), b.getExpr()));
		
		return result;
	}
	
	public static RestrictedExpr<Expr> equals(RestrictedExpr<Expr> a, RestrictedExpr<Expr>  b) {
		RestrictionSet ar = a.getRestrictions().clone();
		
		ar.stateRestriction(b.getRestrictions());
		
		E_Equals equals = new E_Equals(a.getExpr(), b.getExpr());
		
		RestrictedExpr<Expr> result = RestrictedExpr.create((Expr)equals, ar);
		
		return result;
	}

	/*
	public static G_Primitive<VarDef> equals(RestrictedExpr<Expr> a, RestrictedExpr<Expr> b) {
		VarDef tmp = equals(a.getExpr(), b.getExpr());
		
		G_Primitive<VarDef> result = new G_Primitive<VarDef>(tmp);
		
		return result;
	}
	*/
}
