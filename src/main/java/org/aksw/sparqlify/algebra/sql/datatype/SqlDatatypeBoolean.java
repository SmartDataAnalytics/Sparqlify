package org.aksw.sparqlify.algebra.sql.datatype;

class SqlDatatypeBoolean
	extends SqlDatatypeBase
{
	private static SqlDatatypeBoolean instance;
	
	public static SqlDatatypeBoolean getInstance() {
		if(instance == null) {
			instance = new SqlDatatypeBoolean();
		}
		
		return instance;
	}
	
	protected SqlDatatypeBoolean() {
		super(-7);
	}
}
