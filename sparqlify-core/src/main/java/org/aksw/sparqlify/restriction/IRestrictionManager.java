package org.aksw.sparqlify.restriction;

import org.aksw.sparqlify.config.lang.PrefixSet;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public interface IRestrictionManager {
	
	Boolean determineSatisfiability(Expr expr);

	RestrictionImpl getRestriction(Expr expr);

	//void stateEqual(Var a, Var b);

	RestrictionImpl getRestriction(Var a);

	RestrictionImpl getOrCreateLocalRestriction(Var a);

	void stateType(Var a, Type type);

	void stateNode(Var a, Node b);

	void stateUri(Var a, String uri);

	void stateLiteral(Var a, NodeValue b);

	void stateLexicalValuePrefixes(Var a, PrefixSet prefixes);

	void stateNonEqual(Var a, Var b);

}