package org.aksw.sparqlify.restriction;

import java.util.EnumSet;

import org.aksw.sparqlify.config.lang.PrefixSet;

import com.hp.hpl.jena.graph.Node;
import com.karneim.util.collection.regex.PatternPro;


public interface Restriction
	extends Cloneable
{
	/*
	 * Meta data methods.
	 */
	/*
	boolean isSet();
	RestrictionSet asSet();
	*/
	
	/*
	 * Core methods
	 */
	
	
	boolean stateRestriction(Restriction other);
	
	/**
	 * State whether the resource is a URI or a Literal
	 * 
	 * @param type
	 * @return
	 */
	boolean stateType(RdfTermType newType);

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

	
	/**
	 * Test wether the restriction is unsatisfiable
	 * 
	 * @return
	 */
	boolean isUnsatisfiable();
	

	Restriction clone();
	
	/**
	 * State a URI regex pattern.
	 * To be done.
	 */
	void statePattern(PatternPro pattern);
	
	/*
	 * Retrieval
	 * 
	 * Important: All retrieval methods must return complete sets, otherwise
	 * it will result in missing information
	 */

	boolean hasConstant();
	
	/**
	 * The set of possible RDF term types this restriction may have.
	 * 
	 * @return
	 */
	EnumSet<RdfTermType> getRdfTermTypes();

	/**
	 * Retrieve the node if it has been set.
	 * Implies an RDF term type consistent with the node value.s
	 * 
	 * @return The set node. Null if not set.
	 */
	Node getNode();
		
	
	/**
	 * Retrieve the set of possible URI prefixes.
	 * Implies Type.URI
	 * 
	 * @return
	 */
	PrefixSet getUriPrefixes();
}
