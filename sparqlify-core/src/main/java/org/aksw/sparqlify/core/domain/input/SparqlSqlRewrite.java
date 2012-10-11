package org.aksw.sparqlify.core.domain.input;

import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;

public class SparqlSqlRewrite {
	private String sqlQueryString;
	private VarDefinition varDefinition;
	private List<Var> projectionOrder;

	public SparqlSqlRewrite(String sqlQueryString, VarDefinition varDefinition, List<Var> projectionOrder) {
		this.sqlQueryString = sqlQueryString;
		this.varDefinition = varDefinition;
		this.projectionOrder = projectionOrder;
	}

	public String getSqlQueryString() {
		return sqlQueryString;
	}

	public VarDefinition getVarDefinition() {
		return varDefinition;
	}

	public List<Var> getProjectionOrder() {
		return projectionOrder;
	}

	@Override
	public String toString() {
		return "Rewrite [sqlQueryString=" + sqlQueryString + ", varDefinition="
				+ varDefinition + ", projectionOrder=" + projectionOrder + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((projectionOrder == null) ? 0 : projectionOrder.hashCode());
		result = prime * result
				+ ((sqlQueryString == null) ? 0 : sqlQueryString.hashCode());
		result = prime * result
				+ ((varDefinition == null) ? 0 : varDefinition.hashCode());
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
		SparqlSqlRewrite other = (SparqlSqlRewrite) obj;
		if (projectionOrder == null) {
			if (other.projectionOrder != null)
				return false;
		} else if (!projectionOrder.equals(other.projectionOrder))
			return false;
		if (sqlQueryString == null) {
			if (other.sqlQueryString != null)
				return false;
		} else if (!sqlQueryString.equals(other.sqlQueryString))
			return false;
		if (varDefinition == null) {
			if (other.varDefinition != null)
				return false;
		} else if (!varDefinition.equals(other.varDefinition))
			return false;
		return true;
	}
}
