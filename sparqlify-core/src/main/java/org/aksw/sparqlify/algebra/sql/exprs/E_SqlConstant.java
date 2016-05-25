package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.datatypes.XClass;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeValueVisitor;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Duplicate of E_SqlNodeValue
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
@Deprecated
public class E_SqlConstant
	extends NodeValue
	implements ExprSql
{
	private Object value;
	private XClass datatype;

	public E_SqlConstant(Object value, XClass datatype) {
		this.value = value;
		this.datatype = datatype;
	}
	
	public Object getValue() {
		return value;
	}

	@Override
	public XClass getDatatype() {
		return datatype;
	}

	public NodeValue asNodeValue() {
		//NodeValueUtils.
		NodeValue nv = null;//NodeValue.make
		return nv;
	}
	
	@Override
	public NodeValue eval(Binding binding, FunctionEnv env) {
		return asNodeValue();
	}

	@Override
	protected Node makeNode() {
		return asNodeValue().asNode();
	}

	@Override
	public void visit(NodeValueVisitor visitor) {
		throw new RuntimeException("Not implemented");
	}
}
