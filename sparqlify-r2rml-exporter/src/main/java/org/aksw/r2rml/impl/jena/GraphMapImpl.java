package org.aksw.r2rml.impl.jena;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class GraphMapImpl
	extends TermMapImpl
{
	public GraphMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
