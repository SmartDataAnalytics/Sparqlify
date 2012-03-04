package org.aksw.sparqlify.algebra.sql.datatype;

public class SqlDatatypeReal
	extends SqlDatatypeBase
{
	private static SqlDatatypeReal instance;
	
	public static SqlDatatypeReal getInstance() {
		if(instance == null) {
			instance = new SqlDatatypeReal();
		}
		
		return instance;
	}
	
	protected SqlDatatypeReal() {
		super(4);
	}
}
