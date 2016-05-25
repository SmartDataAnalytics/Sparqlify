package org.aksw.sparqlify.core.domain.input;

import java.util.List;

import org.aksw.jena_sparql_api.views.VarDefinition;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.apache.jena.sparql.core.Var;

public class SparqlSqlOpRewrite {
	// Whether the query is known to yield an empty result
	private boolean isEmptyResult;
	
	private SqlOp sqlOp;
	private VarDefinition varDefinition;
	private List<Var> projectionOrder;

	public SparqlSqlOpRewrite(SqlOp sqlOp, boolean isEmptyResult, VarDefinition varDefinition, List<Var> projectionOrder) {
		this.sqlOp = sqlOp;
		this.isEmptyResult = isEmptyResult;
		this.varDefinition = varDefinition;
		this.projectionOrder = projectionOrder;
	}

	public boolean isEmptyResult() {
		return isEmptyResult;
	}
	
	public SqlOp getSqlOp() {
		return sqlOp;
	}

	public VarDefinition getVarDefinition() {
		return varDefinition;
	}

	public List<Var> getProjectionOrder() {
		return projectionOrder;
	}

	@Override
	public String toString() {
		return "Rewrite [sqlOp" + sqlOp + ", varDefinition="
				+ varDefinition + ", projectionOrder=" + projectionOrder + "]";
	}

}
