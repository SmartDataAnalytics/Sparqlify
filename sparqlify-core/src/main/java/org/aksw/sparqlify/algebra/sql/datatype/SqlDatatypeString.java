package org.aksw.sparqlify.algebra.sql.datatype;

class SqlDatatypeString
	extends SqlDatatypeBase
{
	private static SqlDatatypeString instance;
	
	public static SqlDatatypeString getInstance() {
		if(instance == null) {
			instance = new SqlDatatypeString();
		}
		
		return instance;
	}

	protected SqlDatatypeString() {
		super(-1);
	}
}
