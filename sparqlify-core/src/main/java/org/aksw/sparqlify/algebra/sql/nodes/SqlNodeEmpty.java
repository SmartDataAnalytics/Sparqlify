package org.aksw.sparqlify.algebra.sql.nodes;

/**
 * A node corresponding to an empty relation.
 * 
 * 
 * @author raven
 *
 */
public class SqlNodeEmpty
	extends SqlNodeBase0
{
	/*
	private static SqlNodeEmpty defaultInstance;
	
	public static SqlNodeEmpty getDefaultInstance() {
		if(defaultInstance == null) {
			defaultInstance = new SqlNodeEmpty("default");
		}
		
		return defaultInstance;
	}
	*/

	public SqlNodeEmpty()
	{
		this("no alias");
	}
	
	public SqlNodeEmpty(String aliasName) {
		super(aliasName);
	}

	@Override
	public SqlNode copy0() {
		return new SqlNodeEmpty("not set");
	}
}
