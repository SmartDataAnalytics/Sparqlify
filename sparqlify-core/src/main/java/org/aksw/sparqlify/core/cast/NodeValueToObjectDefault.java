package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.expr.util.NodeValueUtilsSparqlify;
import org.apache.jena.sparql.expr.NodeValue;

/*
class NodeValueToConstant
	implements NodeValueToObject
{
	private Object

	@Override
	public Object toObject(NodeValue nodeValue) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
*/

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
		Object result = NodeValueUtilsSparqlify.getValue(nodeValue);
		return result;
	}
}