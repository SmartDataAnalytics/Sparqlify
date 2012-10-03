package org.aksw.sparqlify.restriction;

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
public interface Restriction {
	Restriction and(Restriction other);
	Restriction or(Restriction other);
	Restriction not(Restriction other);
	
	boolean isUnsatisfiable();
}
