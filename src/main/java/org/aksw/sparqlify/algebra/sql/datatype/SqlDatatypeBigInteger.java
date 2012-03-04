package org.aksw.sparqlify.algebra.sql.datatype;

class SqlDatatypeBigInteger
	extends SqlDatatypeBase
{
	private static SqlDatatypeBigInteger instance;

	public static SqlDatatypeBigInteger getInstance() {
		if (instance == null) {
			instance = new SqlDatatypeBigInteger();
		}

		return instance;
	}

	protected SqlDatatypeBigInteger() {
		super(-5);
	}
}