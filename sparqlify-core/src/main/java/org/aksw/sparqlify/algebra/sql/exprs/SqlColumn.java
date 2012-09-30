package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.core.SqlDatatype;

public class SqlColumn
	extends SqlExpr0
{
	private SqlTable table;
	private String name;

	public SqlColumn(SqlTable table, String name, SqlDatatype datatype) {
		super(datatype);
		this.table = table;
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
