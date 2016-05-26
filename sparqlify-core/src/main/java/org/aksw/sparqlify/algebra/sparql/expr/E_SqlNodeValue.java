package org.aksw.sparqlify.algebra.sparql.expr;

import org.aksw.sparqlify.algebra.sql.exprs.ExprSql;
import org.aksw.sparqlify.core.datatypes.XClass;
import org.aksw.sparqlify.expr.util.NodeValueUtilsSparqlify;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class E_SqlNodeValue
	extends NodeValue
	implements ExprSql 
{
	private NodeValue nodeValue;
	private XClass datatype;
	
	public E_SqlNodeValue(NodeValue nodeValue, XClass datatype) {
		this.nodeValue = nodeValue;
		this.datatype = datatype;
	}
	
	public XClass getDatatype() {
		return datatype;
	}
	
	// FIXME This should probably return a Java object encapsulating the SQL value
	public Object getSqlValue() {
		Object result = NodeValueUtilsSparqlify.getValue(this.nodeValue);
		return result;
	}
	
	@Override
	protected Node makeNode() {
		return nodeValue.asNode();
	}

	@Override
	public void visit(NodeValueVisitor visitor) {
		nodeValue.visit(visitor);
	}
}
