package org.aksw.r2rml.jena.domain.impl;

import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class GraphMapImpl
	extends TermMapImpl
	implements GraphMap
{
	public GraphMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
