package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.algebra.sql.nodes.Projection;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public class ExprSqlRewrite {
	private Expr expr;
	private Projection projection;
	
	public ExprSqlRewrite(Expr expr, Projection projection) {
		super();
		this.expr = expr;
		this.projection = projection;
	}

	public Expr getExpr() {
		return expr;
	}
	public Projection getProjection() {
		return projection;
	}
	
	public List<String> getReferencedColumnNames() {
		Set<Var> vars = expr.getVarsMentioned();
	
		List<String> result = new ArrayList<String>(vars.size());
		for(Var var : vars) {
			result.add(var.getVarName());
		}
		
		return result;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result
				+ ((projection == null) ? 0 : projection.hashCode());
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
		ExprSqlRewrite other = (ExprSqlRewrite) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (projection == null) {
			if (other.projection != null)
				return false;
		} else if (!projection.equals(other.projection))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExprSqlRewrite [expr=" + expr + ", projection="
				+ projection + "]";
	}
}