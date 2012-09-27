package org.aksw.sparqlify.core.domain;

import org.aksw.sparqlify.restriction.RestrictionSet;

/**
 * [SPARQL level]
 * 
 * A restrictedExpr is comprised of
 * - An SPARQL expression
 * - restrictions associated with its corresponding value after evaluation         (under some interpretation)
 * 
 * Example:
 * Some expression can be declared to yield integers in the range [0..4]
 * or uris with certain prefixes.
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class RestrictedExpr<T> {
	private T expr;
	private RestrictionSet restrictions;

	public RestrictedExpr(T expr) {
		this(expr, new RestrictionSet());
	}

	public RestrictedExpr(T expr, RestrictionSet restrictions) {
		super();
		this.expr = expr;
		this.restrictions = restrictions;
	}

	public static <T> RestrictedExpr<T> create(T expr) {
		return new RestrictedExpr<T>(expr);
	}

	public static <T> RestrictedExpr<T> create(T expr, RestrictionSet restrictions) {
		return new RestrictedExpr<T>(expr, restrictions);
	}

	
	public T getExpr() {
		return expr;
	}
	
	
	public RestrictionSet getRestrictions() {
		return restrictions;
	}

	@Override
	public String toString() {
		return "RestrictedExpr [expr=" + expr + ", restrictions="
				+ restrictions + "]";
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
		RestrictedExpr<?> other = (RestrictedExpr<?>) obj;
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
