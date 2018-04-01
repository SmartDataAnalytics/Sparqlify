package org.aksw.r2rml.jena.domain.impl;

import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class PredicateMapImpl
	extends TermMapImpl
	implements PredicateMap
{
	public PredicateMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
