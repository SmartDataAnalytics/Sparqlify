package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlOpEmpty
	extends SqlOpBase0
{
	public SqlOpEmpty(Schema schema) {
		super(schema, true);
	}

	public static SqlOpEmpty create() {
		SqlOpEmpty result = create(new SchemaImpl());
		return result;
	}
	
	public static SqlOpEmpty create(Schema schema) {
		return new SqlOpEmpty(schema);
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}
}
