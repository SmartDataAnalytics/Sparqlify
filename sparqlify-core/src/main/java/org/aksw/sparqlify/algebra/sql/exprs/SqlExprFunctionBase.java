package org.aksw.sparqlify.algebra.sql.exprs;


/* Not used anymore, retrieval of arguments has been added to the base interface
public abstract class SqlExprFunctionBase
	extends SqlExprBase
{
	public SqlExprFunctionBase(SqlDatatype datatype) {
		super(datatype);
	}

	public abstract List<SqlExpr> getArgs();

	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), getArgs());
	}
}
*/