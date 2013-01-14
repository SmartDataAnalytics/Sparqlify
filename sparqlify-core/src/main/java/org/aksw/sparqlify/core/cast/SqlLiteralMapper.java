package org.aksw.sparqlify.core.cast;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public interface SqlLiteralMapper {
	String serialize(NodeValue value);
}