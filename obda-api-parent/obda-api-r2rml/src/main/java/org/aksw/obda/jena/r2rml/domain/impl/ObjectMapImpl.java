package org.aksw.obda.jena.r2rml.domain.impl;

import org.aksw.obda.jena.r2rml.domain.api.ObjectMap;
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
