package org.aksw.sparqlify.config.lang;

import java.util.Map;

import org.apache.jena.graph.Node;

public interface Constraint {
	public Constraint copySubstitute(Map<? extends Node, Node> map);
}
