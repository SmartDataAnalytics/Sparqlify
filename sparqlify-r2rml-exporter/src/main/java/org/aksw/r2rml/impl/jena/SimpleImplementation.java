package org.aksw.r2rml.impl.jena;

import java.util.function.BiFunction;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;

public class SimpleImplementation
	extends Implementation
{
	protected BiFunction<? super Node, ? super EnhGraph, ? extends EnhNode> ctor;
	
	public SimpleImplementation(BiFunction<? super Node, ? super EnhGraph, ? extends EnhNode> ctor) {
		super();
		this.ctor = ctor;
	}

	@Override
	public EnhNode wrap(Node node, EnhGraph eg) {
		EnhNode result = ctor.apply(node, eg);
		return result;
	}

	@Override
	public boolean canWrap(Node node, EnhGraph eg) {
		return true;
	}

}
