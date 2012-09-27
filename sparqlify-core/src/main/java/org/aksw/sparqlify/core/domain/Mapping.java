package org.aksw.sparqlify.core.domain;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;

import com.hp.hpl.jena.sparql.core.Var;


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
	
	private SqlOp sqlOp;


	public Mapping(SqlOp sqlOp) {
		this.varDefinition = new VarDefinition();
		this.sqlOp = sqlOp;
	}


	/**
	 * This constructor does not create copy of the arguments 
	 * 
	 * @param other
	 */
	public Mapping(VarDefinition varDefinition, SqlOp sqlOp) {
		this.varDefinition = varDefinition;
		this.sqlOp = sqlOp;
	}


	public VarDefinition getVarDefinition() {
		return varDefinition;
	}


	public SqlOp getSqlOp() {
		return sqlOp;
	}
	
	
	/*
	public Mapping createRenamed(Map<String, String> map) {
		varDefinition.createWithRenamedColumnReferences();
		
	}*/

		
	@Override
	public String toString() {
		return "Mapping [varDefinition=" + varDefinition
				+ ", sqlOp=" + sqlOp + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sqlOp == null) ? 0 : sqlOp.hashCode());
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
		if (sqlOp == null) {
			if (other.sqlOp != null)
				return false;
		} else if (!sqlOp.equals(other.sqlOp))
			return false;
		if (varDefinition == null) {
			if (other.varDefinition != null)
				return false;
		} else if (!varDefinition.equals(other.varDefinition))
			return false;
		return true;
	}
}
