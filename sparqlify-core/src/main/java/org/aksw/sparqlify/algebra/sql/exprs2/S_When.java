package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;


/**
 * 
 * 
 * @author raven
 *
 */
public class S_When
	extends SqlExpr2
{
	public S_When(TypeToken type, SqlExpr left, SqlExpr right) {
		super(type, "when", left, right);
	}

	@Override
	public SqlExpr2 copy(SqlExpr left, SqlExpr right) {
		return new S_When(datatype, left, right);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
