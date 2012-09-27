package org.aksw.sparqlify.core.domain;

import org.aksw.sparqlify.algebra.sql.nodes.SqlNode;


/**
 * This class expresses an RDB and RDF mapping.
 * It is comprised of
 * - A set of variable definitions
 * - An SQL node
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class Mapping
//	TODO: A mapping is an algebraic entity extends Op
{
	private VarDefinition varDefinition;
	private SqlNode sqlNode;


	public Mapping(SqlNode sqlNode) {
		this.varDefinition = new VarDefinition();
		this.sqlNode = sqlNode;
	}


	/**
	 * This constructor does not create copy of the arguments 
	 * 
	 * @param other
	 */
	public Mapping(VarDefinition varDefinition, SqlNode sqlNode) {
		this.varDefinition = varDefinition;
		this.sqlNode = sqlNode;
	}


	public VarDefinition getVarDefinition() {
		return varDefinition;
	}


	public SqlNode getSqlNode() {
		return sqlNode;
	}
	
	
	/*
	public Mapping createRenamed(Map<String, String> map) {
		varDefinition.createWithRenamedColumnReferences();
		
	}*/

		
	@Override
	public String toString() {
		return "Mapping [varDefinition=" + varDefinition
				+ ", sqlNode=" + sqlNode + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sqlNode == null) ? 0 : sqlNode.hashCode());
		result = prime
				* result
				+ ((varDefinition == null) ? 0 : varDefinition
						.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mapping other = (Mapping) obj;
		if (sqlNode == null) {
			if (other.sqlNode != null)
				return false;
		} else if (!sqlNode.equals(other.sqlNode))
			return false;
		if (varDefinition == null) {
			if (other.varDefinition != null)
				return false;
		} else if (!varDefinition.equals(other.varDefinition))
			return false;
		return true;
	}
}
