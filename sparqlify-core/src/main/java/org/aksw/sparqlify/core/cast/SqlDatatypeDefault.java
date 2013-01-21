package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public class SqlDatatypeDefault
	implements SqlDatatype
{
	private NodeValueToObject nodeValueToObject;
	private TypeToken typeToken;

	public SqlDatatypeDefault(TypeToken typeToken, NodeValueToObject nodeValueToObject) {
		this.typeToken = typeToken;
		this.nodeValueToObject = nodeValueToObject;
	}
	
	public NodeValueToObject getNodeValueToObject() {
		return nodeValueToObject;
	}

	public TypeToken getTypeToken() {
		return typeToken;
	}

	@Override
	public SqlValue toSqlValue(NodeValue nodeValue) {
		Object o = nodeValueToObject.toObject(nodeValue);
		SqlValue result = new SqlValue(typeToken, o);
		return result;
	}
	
}