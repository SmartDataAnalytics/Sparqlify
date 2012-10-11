package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.restriction.RestrictionSet;

import com.hp.hpl.jena.sparql.expr.E_Add;
import com.hp.hpl.jena.sparql.expr.E_Equals;

public class VariableDefinitionOps {

	public static RestrictedExpr plus(RestrictedExpr a, RestrictedExpr b) {
		RestrictedExpr result =
				RestrictedExpr.create(new E_Add(a.getExpr(), b.getExpr()));
		
		return result;
	}
	
	public static RestrictedExpr equals(RestrictedExpr a, RestrictedExpr  b) {
		RestrictionSet ar = a.getRestrictions().clone();
		
		ar.stateRestriction(b.getRestrictions());
		
		E_Equals equals = new E_Equals(a.getExpr(), b.getExpr());
		
		RestrictedExpr result = RestrictedExpr.create(equals, ar);
		
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
