package org.aksw.r2rml.impl.jena;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class PredicateMapImpl
	extends TermMapImpl
{
	public PredicateMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
