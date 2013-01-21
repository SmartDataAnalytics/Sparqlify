package org.aksw.sparqlify.core.cast;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public interface NodeValueToObject
{
	Object toObject(NodeValue nodeValue);
}