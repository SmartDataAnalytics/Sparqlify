package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Collections;
import java.util.List;

import org.aksw.sparqlify.core.sql.schema.Schema;

/**
 * Leaf nodes in an SQL expression. May carry aliases.
 * 
 * @author raven
 *
 */
public abstract class SqlOpLeaf
	extends SqlOpBase0
{
	protected String aliasName;
	
	public SqlOpLeaf(Schema schema, boolean isEmpty, String aliasName) {
		super(schema, isEmpty);
		this.aliasName = aliasName;
	}
	
	public abstract String getId();
	
	public String getAliasName() {
		return aliasName;
	}
	
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
	public List<SqlOp> getSubOps() {
		return Collections.emptyList();
	}
}
