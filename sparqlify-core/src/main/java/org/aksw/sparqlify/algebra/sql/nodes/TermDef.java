package org.aksw.sparqlify.algebra.sql.nodes;

import org.aksw.sparqlify.restriction.Restriction;
import org.aksw.sparqlify.restriction.RestrictionSet;

import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * A recipe for creating an RdfTerm from a relation.
 * 
 * I am not sure whether a TermDef can be an arbitrary Sparql-expression
 * or whether it must always be an E_RdfTerm2 object.
 *
 * In the former case we can use arbitrary sparql expressions, which seemns more general.
 * 
 * 
 * 2011 Nov 8
 * 
 * @author Claus Stader
 * 
 *
 */
public class TermDef {
	//private E_RdfTerm2 expr;
	private Expr expr;

	private RestrictionSet restrictions = new RestrictionSet();
	
	/**
	 * A restriction associated with this expression.
	 * 
	 * 
	 * @return
	 */
	public RestrictionSet getRestrictions() {
		return restrictions;
	}
	
	/** 
	 * The RdfTerm may only be constructed from rows of the underlying relation that
	 * have a matching discriminator value. 
	 * 
	 * If multiple tables with a discriminator columns are unioned, then such resulting relation
	 * might have multiple discriminator columns which may be null.
	 * Alternatively, if discriminator values are unique, merging of these columns may be done.
	 * 
	 * 
	 * For this class, a discriminatorValue of null means, that the variable can always be generated
	 */
	private Integer discriminatorValue;

	
	// Not sure if it is better if we just used an generic condition instead
	private SqlNode condition;


	public TermDef(Expr expr) {
		this.expr = expr;
	}

	public TermDef(Expr expr, Restriction restriction) {
		this.expr = expr;
		
		if(restriction != null) {
			this.restrictions.stateRestriction(restriction);
		}
	}

	public TermDef(Expr expr, RestrictionSet restrictions) {
		this.expr = expr;
		this.restrictions = restrictions;
	}
	
	/*
	public TermDef(Expr expr, Integer discriminatorValue) {
		this.expr = expr;
		this.discriminatorValue = discriminatorValue;
	}
	*/
	
	public Expr getExpr() {
		return expr;
	}
	
	
	public SqlNode getCondition() {
		return condition;
	}
	
	/**
	 * 
	 * 
	 * @return The discriminator value under which the term may be constructed. NULL if not required.
	 */
	public Integer getDiscriminatorValue() {
		return discriminatorValue;
	}
	
	@Override
	public String toString() {
		return expr + ": " + restrictions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
		TermDef other = (TermDef) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}
	
	
	
}
