package org.aksw.r2rml.jena.domain.impl;

import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class ObjectMapImpl
	extends TermMapImpl
	implements ObjectMap
{
	public ObjectMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
