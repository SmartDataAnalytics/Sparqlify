package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.ExprSql;
import org.aksw.sparqlify.core.algorithms.RegisteredFunction;
import org.aksw.sparqlify.core.datatypes.XClass;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;

public class E_SqlFunctionRegistered
	extends ExprFunction
	implements ExprSql
{
	private RegisteredFunction regFn;
	private List<Expr> args;

	public E_SqlFunctionRegistered(RegisteredFunction regFn, List<Expr> args) {
		super(regFn.getDeclaration().getSignature().getFunctionName());
		
		this.regFn = regFn;
		this.args = args;
	}

	@Override
	public void visit(ExprVisitor visitor) {
	}

	@Override
	public Expr getArg(int i) {
		return args.get(i);
	}

	@Override
	public int numArgs() {
		return args.size();
	}

	@Override
	public NodeValue eval(Binding binding, FunctionEnv env) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Expr copySubstitute(Binding binding) { //, boolean foldConstants)
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Expr applyNodeTransform(NodeTransform transform) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public XClass getDatatype() {
		return regFn.getTypeSignature().getReturnType();
	}
	
	/**
	 * Whether the function can be evaluated
	 * @return
	 */
	public boolean canEvaluate() {
		return false;
	}
}
