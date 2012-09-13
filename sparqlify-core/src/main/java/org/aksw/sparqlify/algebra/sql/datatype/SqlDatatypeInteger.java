package org.aksw.sparqlify.algebra.sql.datatype;

class SqlDatatypeInteger
	extends SqlDatatypeBase
{
	private static SqlDatatypeInteger instance;
	
	public static SqlDatatypeInteger getInstance() {
		if(instance == null) {
			instance = new SqlDatatypeInteger();
		}
		
		return instance;
	}
	
	protected SqlDatatypeInteger() {
		super(4);
	}
}
