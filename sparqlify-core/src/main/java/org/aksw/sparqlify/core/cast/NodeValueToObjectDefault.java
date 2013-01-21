package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.expr.util.NodeValueUtils;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public class NodeValueToObjectDefault
	implements NodeValueToObject
{
//	private Class<?> javaClass;
//
//	@Override
//	public Class<?> getJavaClass() {
//		return javaClass;
//	}

	@Override
	public Object toObject(NodeValue nodeValue) {
		Object result = NodeValueUtils.getValue(nodeValue);
		return result;
	}
}