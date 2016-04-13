package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.datatypes.XClass;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;

/**
 * A reference to an SQL function
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class E_SqlFunction
	extends ExprFunction
	implements ExprSql
{

	protected E_SqlFunction(String fName) {
		super(fName);
	}

	@Override
	public void visit(ExprVisitor visitor) {
	}

	@Override
	public Expr getArg(int i) {
		return null;
	}

	@Override
	public int numArgs() {
		return 0;
	}

	@Override
	public NodeValue eval(Binding binding, FunctionEnv env) {
		return null;
	}

	@Override
	public Expr copySubstitute(Binding binding) { //, boolean foldConstants) {
		return null;
	}

	@Override
	public Expr applyNodeTransform(NodeTransform transform) {
		return null;
	}

	@Override
	public XClass getDatatype() {
		// TODO Auto-generated method stub
		return null;
	}
}
