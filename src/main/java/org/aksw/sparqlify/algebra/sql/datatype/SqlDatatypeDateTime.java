package org.aksw.sparqlify.algebra.sql.datatype;

public class SqlDatatypeDateTime
	extends SqlDatatypeBase
{
	private static SqlDatatypeDateTime instance;
	
	public static SqlDatatypeDateTime getInstance() {
		if (instance == null) {
			instance = new SqlDatatypeDateTime();
		}
	
		return instance;
	}
	
	protected SqlDatatypeDateTime() {
		super(91);
	}
}