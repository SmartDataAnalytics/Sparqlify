package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

import org.aksw.sparqlify.core.SqlDatatype;
import org.openjena.atlas.io.IndentedWriter;

public interface SqlExpr {
	List<SqlExpr> getArgs();

	SqlDatatype getDatatype();	

	/* Converts the SqlExpr back into an Expr
	 * Null if not possible
	 */
	//SqlExpr eval();
	//Expr asExpr();
	
	public void asString(IndentedWriter writer);

}
