package org.aksw.obda.jena.r2rml.domain.impl;

import org.aksw.obda.jena.r2rml.domain.api.PredicateMap;
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
