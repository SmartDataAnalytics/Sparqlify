package org.aksw.sparqlify.config.lang;

import java.util.Map;

import com.hp.hpl.jena.graph.Node;

public interface Constraint {
	public Constraint copySubstitute(Map<? extends Node, Node> map);
}
