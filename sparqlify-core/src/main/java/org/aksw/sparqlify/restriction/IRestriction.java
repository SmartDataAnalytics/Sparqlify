package org.aksw.sparqlify.restriction;

import org.aksw.sparqlify.config.lang.PrefixSet;

import com.hp.hpl.jena.graph.Node;
import com.karneim.util.collection.regex.PatternPro;

public interface IRestriction
	extends Cloneable
{

	boolean stateRestriction(Restriction other);

	/**
	 * State whether the resource is a URI or a Literal
	 * 
	 * @param type
	 * @return
	 */
	boolean stateType(Type newType);

	/**
	 * Stating a node implies stating the type
	 * 
	 * @param newNode
	 * @return
	 */
	boolean stateNode(Node newNode);

	/**
	 * States a set of valid prefixes.
	 * 
	 * Note: Stating an empty set implies that no URI can be used as a value.
	 * If you do not want to constrain the prefixes, don't call this method.
	 * 
	 * If the set of prefixes becomes empty after stating more prefixes,
	 * the constraint becomes inconsistent.
	 * 
	 * @param prefixes
	 */
	boolean stateUriPrefixes(PrefixSet prefixes);

	// To be done.
	void statePattern(PatternPro pattern);

	boolean isUnsatisfiable();
	

	IRestriction clone();
}