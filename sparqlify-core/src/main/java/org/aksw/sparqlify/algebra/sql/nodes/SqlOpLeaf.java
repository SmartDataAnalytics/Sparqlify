package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Collections;
import java.util.List;

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
	
	public String getAliasName() {
		return aliasName;
	}

	@Override
	public List<SqlOp> getSubOps() {
		return Collections.emptyList();
	}
}
