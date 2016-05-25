package org.aksw.sparqlify.algebra.sparql.expr;

import org.aksw.sparqlify.core.SqlDatatype;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprVisitor;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;


/**
 * Wraps a function and add an SQL datatype field.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class E_SqlFunction
	extends ExprFunction
{
	private ExprFunction exprFunction;
	private SqlDatatype resultType;

	public E_SqlFunction(ExprFunction exprFunction, SqlDatatype resultType) {
		super(exprFunction.getFunctionName(null));

		this.resultType = resultType;
	}
	
	public SqlDatatype getDatatype() {
		return resultType;
	}
	
	@Override
	public void visit(ExprVisitor visitor) {
		exprFunction.visit(visitor);
	}

	@Override
	public Expr getArg(int i) {
		return exprFunction.getArg(i);
	}

	@Override
	public int numArgs() {
		return exprFunction.numArgs();
	}

	@Override
	public NodeValue eval(Binding binding, FunctionEnv env) {
		return exprFunction.eval(binding, env);
	}

//	@Override
//	public Expr copySubstitute(Binding binding, boolean foldConstants) {
//		return exprFunction.copySubstitute(binding, foldConstants);
//	}

    @Override
    public Expr copySubstitute(Binding binding) {
        return exprFunction.copySubstitute(binding);
    }
	
	@Override
	public Expr applyNodeTransform(NodeTransform transform) {
		return exprFunction.applyNodeTransform(transform);
	}

}
