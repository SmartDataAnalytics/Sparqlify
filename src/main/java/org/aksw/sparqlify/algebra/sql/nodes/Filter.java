package org.aksw.sparqlify.algebra.sql.nodes;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;

public class Filter {
	private SqlExprList conditions = new SqlExprList();

	public SqlExprList getConditions() {
		return conditions;
	}
}
