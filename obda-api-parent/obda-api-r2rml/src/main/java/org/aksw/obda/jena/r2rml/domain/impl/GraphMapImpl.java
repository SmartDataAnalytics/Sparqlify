package org.aksw.obda.jena.r2rml.domain.impl;

import org.aksw.obda.jena.r2rml.domain.api.GraphMap;
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
