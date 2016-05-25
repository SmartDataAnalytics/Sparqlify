package org.aksw.sparqlify.core.cast;

import org.aksw.jena_sparql_api.utils.expr.NodeValueUtils;
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
		Object result = NodeValueUtils.getValue(nodeValue);
		return result;
	}
}