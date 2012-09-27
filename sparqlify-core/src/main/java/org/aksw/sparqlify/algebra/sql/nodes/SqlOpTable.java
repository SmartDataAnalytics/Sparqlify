package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlOpTable
	extends SqlOpBase0
{
	private String tableName;
	
	public SqlOpTable(Schema schema, String tableName) {
		super(schema);
		this.tableName = tableName;
	}
	
	
	public String getTableName() {
		return tableName;
	}
}
