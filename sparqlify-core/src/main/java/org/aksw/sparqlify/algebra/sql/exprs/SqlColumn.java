package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;

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
