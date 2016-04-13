package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.atlas.io.IndentedWriter;

import org.apache.jena.sparql.expr.Expr;

/**
 * A wrapper for a Jena expression. I guess wrapping Jena expressions in
 * our object structure is the best way to enable optimizations on both levels.
 * Of course, this implies that we need to be able to convert from our structure
 * back to Jena expressions.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class S_Jena
	extends SqlExprBase
{
	public S_Jena(TypeToken datatype) {
		super(datatype);
		// TODO Auto-generated constructor stub
	}

	private Expr expr;
	
	public Expr getExpr() {
		return expr;
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("Jena " + expr.toString());
	}


	@Override
	public List<SqlExpr> getArgs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SqlExprType getType() {

		SqlExprType result;

		if(expr.isConstant()) {
			result = SqlExprType.Constant;
		} else if(expr.isVariable()) {
			result = SqlExprType.Variable;
		} else if(expr.isFunction()) {
			result = SqlExprType.Function;
		} else {
			throw new RuntimeException("Should not happen: could not determine expression type of " + expr);
		}
		
		return result;
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
