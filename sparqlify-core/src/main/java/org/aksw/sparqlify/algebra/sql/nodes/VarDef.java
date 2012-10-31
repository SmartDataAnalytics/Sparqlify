package org.aksw.sparqlify.algebra.sql.nodes;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.restriction.RestrictionImpl;
import org.aksw.sparqlify.restriction.RestrictionSetImpl;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;


public class VarDef {
	private Expr expr;
	private RestrictionSetImpl restrictions = new RestrictionSetImpl();


	public VarDef(Expr expr) {
		this.expr = expr;
	}
	
	


	public VarDef(Expr expr, RestrictionImpl restriction) {
		this.expr = expr;
		
		if(restriction != null) {
			this.restrictions.stateRestriction(restriction);
		}
	}


	public VarDef(Expr expr, RestrictionSetImpl restrictions) {
		this.expr = expr;
		this.restrictions = restrictions;
	}


	public Expr getExpr() {
		return expr;
	}


	public boolean isTermCtorExpr() {
		return expr instanceof E_RdfTerm; 
	}


	public boolean isConstant() {
		return expr.isConstant(); 
	}

	
	public E_RdfTerm getTermCtorExpr() {
		return (E_RdfTerm)expr;
	}
	

	public NodeValue getConstant() {
		return expr.getConstant();		
	}
	
	
	public RestrictionSetImpl getRestrictions() {
		return restrictions;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result
				+ ((restrictions == null) ? 0 : restrictions.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VarDef other = (VarDef) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (restrictions == null) {
			if (other.restrictions != null)
				return false;
		} else if (!restrictions.equals(other.restrictions))
			return false;
		return true;
	}
}
