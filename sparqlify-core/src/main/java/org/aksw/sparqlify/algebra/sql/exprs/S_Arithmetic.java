package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.SqlDatatype;

public abstract class S_Arithmetic
	extends SqlExpr2
{
	private String symbol;
	
	public S_Arithmetic(String symbol, SqlExpr left, SqlExpr right, SqlDatatype datatype) {
		super(left, right, datatype);
		this.symbol = symbol;
	}
	
	/**
	 * If the database backend is not strange, then addition, substraction and so on
	 * work with the symbols +, -, *, / (and some other operators).
	 * So in this case there is no need for explicit custom serialization in SqlAlgebraToString. 
	 * 
	 * @return
	 */
	public String getSymbol() {
		return symbol;
	}

	/*
	public static SqlExpr create(SqlExpr left, SqlExpr right, DatatypeSystem system) {
	}
	*/
}
