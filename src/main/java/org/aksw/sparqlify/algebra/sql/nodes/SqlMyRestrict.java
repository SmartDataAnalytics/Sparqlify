package org.aksw.sparqlify.algebra.sql.nodes;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;



/**
 * A fundamental question is how to generate sql statements from
 * relational algebra expressions.
 * 
 * 
 * 
 * 
 * We need to consider:
 * table
 * query (is itself already a statement, in contrast to table)
 * join (can join tables and queries)
 * union (can only join statements)
 * restriction (contributes to the where clause)
 * projection (basically initiates a new sql statement which wraps the inner statement)
 * slicing (union and offset)
 * sort
 * 
 * Essentially this class combines (and removes) the following elements from the algebra
 * slice,
 * project,
 * restrict
 * 
 * 
 * Basically this class is analogous to SqlSelectBlock, but without the SDBRequest Dependency
 * 
 * @author raven
 *
 */

public class SqlMyRestrict
	extends SqlNodeBase1
{
	private SqlExprList conditions = new SqlExprList();

	public SqlMyRestrict(String aliasName, SqlNode subNode) {
		super(aliasName, subNode);
	}
	
	public SqlExprList getConditions()
	{
		return conditions;
	}

	@Override
	SqlNode copy1(SqlNode subNode) {
		return new SqlMyRestrict(getAliasName(), subNode);
	}	
}