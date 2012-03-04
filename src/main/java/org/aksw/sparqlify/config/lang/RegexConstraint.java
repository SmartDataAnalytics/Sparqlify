package org.aksw.sparqlify.config.lang;

import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.karneim.util.collection.regex.PatternPro;

public class RegexConstraint
	implements Constraint
{	
	private Var var;
	private String attribute; // type, value, datatype, language
	private PatternPro pattern;
	
	public RegexConstraint(Var var, String attribute, String pattern) {
		super();
		this.var = var;
		this.attribute = attribute;
		this.pattern = new PatternPro(pattern);
	}
	
	
	public RegexConstraint(Var var, String attribute, PatternPro pattern) {
		super();
		this.var = var;
		this.attribute = attribute;
		this.pattern = pattern;
	}
	
	public RegexConstraint copySubstitute(Map<? extends Node, Node> map) {
		Var value = (Var)map.get(var);
		if(value == null || var.equals(value)) {
			return this;
		}

		return new RegexConstraint(value, attribute, pattern);
	}

	
	public Var getVar() {
		return var;
	}
	public String getAttribute() {
		return attribute;
	}
	public PatternPro getPattern() {
		return pattern;
	}
	
	
	/**
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return The union of the two patterns; null indicates no constraint.
	 */
	public static PatternPro union(PatternPro a, PatternPro b) {
		if(a == null || b == null) {
			return null;
		} else {		
			PatternPro c = new PatternPro(a);
			c.addAll(b);
			return c;
		}
	}
	
	/**
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return The intersection of the two patterns; null indicates no constraint.
	 */
	public static PatternPro intersect(PatternPro a, PatternPro b) {
		if(a == null) {
			return b;
		} else if(b == null) {
			return a;
		} else {		
			PatternPro c = new PatternPro(a);
			c.retainAll(b);
			return c;
		}
	}
	
	/**
	 * A pattern is only satisfiable if it is either null (= unconstrained) or
	 * the underlying automaton has a non-empty set of states.
	 * 
	 * @param a
	 * @return
	 */
	public static boolean isSatisfiable(PatternPro a) {
		return a == null || !a.getAutomaton().getStates().isEmpty();
	}
}
