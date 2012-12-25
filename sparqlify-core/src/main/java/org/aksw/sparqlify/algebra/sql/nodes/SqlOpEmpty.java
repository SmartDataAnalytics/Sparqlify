package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlOpEmpty
	extends SqlOpLeaf
{
	public SqlOpEmpty(Schema schema) {
		this(schema, null);
	}

	public SqlOpEmpty(Schema schema, String aliasName) {
		super(schema, true, aliasName);
	}

	public static SqlOpEmpty create() {
		SqlOpEmpty result = create(new SchemaImpl());
		return result;
	}
	
	public static SqlOpEmpty create(Schema schema) {
		return new SqlOpEmpty(schema);
	}

	public static SqlOpEmpty create(Schema schema, String aliasName) {
		return new SqlOpEmpty(schema, aliasName);
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}	
}
