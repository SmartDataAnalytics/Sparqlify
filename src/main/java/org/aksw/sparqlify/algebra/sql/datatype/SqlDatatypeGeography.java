package org.aksw.sparqlify.algebra.sql.datatype;

public class SqlDatatypeGeography
	extends SqlDatatypeBase
{
	private static SqlDatatypeGeography instance;

	public static SqlDatatypeGeography getInstance() {
		if (instance == null) {
			instance = new SqlDatatypeGeography();
		}

		return instance;
	}

	protected SqlDatatypeGeography() {
		super(1111);
	}
}