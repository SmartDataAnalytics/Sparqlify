package org.aksw.sparqlify.core.cast;

import org.apache.jena.sparql.expr.NodeValue;

public interface NodeValueToObject
{
	Object toObject(NodeValue nodeValue);
}