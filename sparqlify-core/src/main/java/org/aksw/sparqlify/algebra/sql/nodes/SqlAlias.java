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
	public SqlAlias(String aliasName, SqlNodeOld subNode) {
		super(aliasName, subNode);
	}

	@Override
	public SqlNodeOld copy1(SqlNodeOld subNode) {
		return new SqlAlias(this.getAliasName(), subNode);
	}
}
