package org.aksw.r2rml.impl.jena;

import org.aksw.r2rml.api.GraphMap;
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
