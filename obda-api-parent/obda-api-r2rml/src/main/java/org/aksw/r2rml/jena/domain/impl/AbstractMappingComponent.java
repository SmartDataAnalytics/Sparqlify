package org.aksw.r2rml.jena.domain.impl;

import org.aksw.r2rml.jena.domain.api.MappingComponent;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public abstract class AbstractMappingComponent
	extends ResourceImpl
	implements MappingComponent
{
	public AbstractMappingComponent(Node node, EnhGraph graph) {
		super(node, graph);
	}

}
