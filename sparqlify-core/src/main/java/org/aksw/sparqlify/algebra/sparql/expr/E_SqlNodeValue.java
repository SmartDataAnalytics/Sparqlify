package org.aksw.sparqlify.algebra.sparql.expr;

import org.aksw.sparqlify.algebra.sql.exprs.ExprSql;
import org.aksw.sparqlify.core.SqlDatatype;
import org.aksw.sparqlify.expr.util.NodeValueUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;

public class E_SqlNodeValue
	extends NodeValue
	implements ExprSql 
{
	private NodeValue nodeValue;
	private SqlDatatype datatype;
	
	public E_SqlNodeValue(NodeValue nodeValue, SqlDatatype datatype) {
		this.nodeValue = nodeValue;
		this.datatype = datatype;
	}
	
	public SqlDatatype getDatatype() {
		return datatype;
	}
	
	// FIXME This should probably return a Java object encapsulating the SQL value
	public Object getSqlValue() {
		Object result = NodeValueUtils.getValue(this.nodeValue);
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
