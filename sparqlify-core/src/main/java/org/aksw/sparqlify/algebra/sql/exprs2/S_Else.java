package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;

/**
 * This is a hack (or at least trick)
 * to treat CASE ... WHEN statements as functions
 * 
 * Maybe not the best choice to do it like this.
 * 
 * @author raven
 *
 */
public class S_Else
	extends SqlExpr1
{
	public S_Else(SqlExpr arg) {
		super(TypeToken.Boolean, "else", arg);
	}

	@Override
	public S_Else copy(SqlExpr arg) {
		S_Else result = new S_Else(arg);
		return result;
	}
	
	public static S_Else create(SqlExpr a) {
		return new S_Else(a);
	}

	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
