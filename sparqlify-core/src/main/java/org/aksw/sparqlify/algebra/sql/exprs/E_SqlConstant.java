package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.SqlDatatype;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueVisitor;
import com.hp.hpl.jena.sparql.function.FunctionEnv;

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
	private SqlDatatype datatype;

	public E_SqlConstant(Object value, SqlDatatype datatype) {
		this.value = value;
		this.datatype = datatype;
	}
	
	public Object getValue() {
		return value;
	}

	@Override
	public SqlDatatype getDatatype() {
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
