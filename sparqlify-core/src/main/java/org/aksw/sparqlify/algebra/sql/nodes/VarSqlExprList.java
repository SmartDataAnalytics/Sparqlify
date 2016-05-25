package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.apache.jena.sparql.core.Var;

public class VarSqlExprList {
	private Map<Var, SqlExpr> varToExpr = new HashMap<Var, SqlExpr>();
	private List<Var> vars = new ArrayList<Var>();
	
	public Map<Var, SqlExpr> getVarSqlExprList() {
		return varToExpr;
	}
	
	public List<Var> getVar() {
		return vars;
	}
}
