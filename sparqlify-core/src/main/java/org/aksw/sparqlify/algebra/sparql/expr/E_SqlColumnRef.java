package org.aksw.sparqlify.algebra.sparql.expr;

import org.aksw.sparqlify.algebra.sql.exprs.ExprSql;
import org.aksw.sparqlify.core.datatypes.XClass;
import org.apache.jena.sparql.expr.ExprVar;


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
	implements ExprSql
{
	private String aliasName;
	private String columnName;
	private XClass datatype; 
	

	public static String createCombinedName(String columnName, String aliasName) {
		return aliasName == null ? columnName : aliasName + "." + columnName;
	}
	
	public E_SqlColumnRef(String columnName, String aliasName, XClass datatype) {
		super(createCombinedName(columnName, aliasName));

		this.aliasName = aliasName;
		this.columnName = columnName;
		this.datatype = datatype;
	}
	
	public String getAliasName() {
		return aliasName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public XClass getDatatype() {
		return datatype;
	}
}
