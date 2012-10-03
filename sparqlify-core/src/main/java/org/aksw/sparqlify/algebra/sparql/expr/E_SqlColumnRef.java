package org.aksw.sparqlify.algebra.sparql.expr;

import com.hp.hpl.jena.sparql.expr.ExprVar;

/**
 * A reference to a column of a table (alias).
 * Acts as a variable so that Jena does not
 * notice anything invasive.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class E_SqlColumnRef
	extends ExprVar
{
	private String aliasName;
	private String columnName;	
	

	public static String createCombinedName(String columnName, String aliasName) {
		return aliasName == null ? columnName : aliasName + "." + columnName;
	}
	
	public E_SqlColumnRef(String columnName, String aliasName) {
		super(createCombinedName(columnName, aliasName));

		this.aliasName = aliasName;
		this.columnName = columnName;
	}
	
	public String getAliasName() {
		return aliasName;
	}
	
	public String getColumnName() {
		return columnName;
	}
}
