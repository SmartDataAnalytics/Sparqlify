package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;


public interface SqlExpr {
	List<SqlExpr> getArgs();

	//TypeToken getDatatype();
	TypeToken getDatatype();


	boolean isVariable();
	boolean isConstant();
	boolean isFunction();
	

	SqlExprType getType();

	SqlExprFunction asFunction();
	SqlExprConstant asConstant();
	SqlExprVar asVariable();

	
	/* Converts the SqlExpr back into an Expr
	 * Null if not possible
	 */
	//SqlExpr eval();
	//Expr asExpr();
	
	public void asString(IndentedWriter writer);

}
