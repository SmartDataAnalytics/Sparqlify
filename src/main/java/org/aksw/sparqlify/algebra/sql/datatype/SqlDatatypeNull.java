package org.aksw.sparqlify.algebra.sql.datatype;

// Do not use, nulls must be typed
public class SqlDatatypeNull
//	extends SqlDatatypeBase
{
	private static SqlDatatypeNull instance;
	
	public static SqlDatatypeNull getInstance() {
		if(instance == null) {
			instance = new SqlDatatypeNull();
		}
		
		return instance;
	}
	
	protected SqlDatatypeNull() {
		//super(-1);
	}
}
