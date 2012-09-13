package org.aksw.sparqlify.algebra.sql.nodes;

/**
 * Simply assigns an alias to a subnode
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlAlias
	extends SqlNodeBase1
{
	public SqlAlias(String aliasName, SqlNode subNode) {
		super(aliasName, subNode);
	}

	@Override
	public SqlNode copy1(SqlNode subNode) {
		return new SqlAlias(this.getAliasName(), subNode);
	}
}
