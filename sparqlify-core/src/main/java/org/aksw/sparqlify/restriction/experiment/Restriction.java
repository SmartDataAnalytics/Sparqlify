package org.aksw.sparqlify.restriction.experiment;

/**
 * A new attempt on the restriction stuff.
 * 
 * A restriction declaratively describes a set of values.
 * It is used in conjuction with an expression to state
 * the expression's set of values when evaluated against a database table.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
@Deprecated
public interface Restriction {
	Restriction intersect(Restriction that);
	
	/**
	 * Union this restriction with that
	 * 
	 * @param other
	 * @return
	 */
	Restriction union(Restriction that);

	/**
	 * Negate this restriction
	 * 
	 * @return
	 */
	Restriction negate();

	
	boolean isUnsatisfiable();
}


