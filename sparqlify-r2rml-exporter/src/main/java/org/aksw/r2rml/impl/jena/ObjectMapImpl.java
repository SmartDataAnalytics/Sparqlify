package org.aksw.r2rml.impl.jena;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class ObjectMapImpl
	extends TermMapImpl
{
	public ObjectMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
