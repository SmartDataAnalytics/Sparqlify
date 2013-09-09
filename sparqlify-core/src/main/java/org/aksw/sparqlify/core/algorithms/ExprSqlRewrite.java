package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprConstant;
import org.aksw.sparqlify.algebra.sql.nodes.Projection;
import org.aksw.sparqlify.core.TypeToken;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Expr is a Sparql level expressions, whose variables are
 * bound by the projection to SQL expressions
 * 
 * TODO In there a reason for expr not having type E_RdfTerm???
 * 
 * @author raven
 *
 */
public class ExprSqlRewrite {
	private Expr expr;
	private Projection projection;
	
	public ExprSqlRewrite(Expr expr, Projection projection) {
		super();
		this.expr = expr;
		this.projection = projection;
	}

	public E_RdfTerm getRdfTermExpr() {
		return (E_RdfTerm)expr;
	}
	
	
	public E_RdfTerm asConstRdfTerm() {
		E_RdfTerm tmp = getRdfTermExpr();
		
		List<Expr> args = tmp.getArgs();

		List<Expr> newArgs = new ArrayList<Expr>(4);
		
		for(int i = 0; i < args.size(); ++i) {
			Expr arg = args.get(i);
			
			// By default, subst is the arg
			Expr subst = arg;
			
			if(arg.isVariable()) {
				String varName = arg.getVarName();
				
				SqlExpr sqlExpr = projection.getNameToExpr().get(varName);
				if(sqlExpr.isConstant()) {
					SqlExprConstant con = sqlExpr.asConstant();

					if(i == 0) {
						//if(con.getDatatype().equals(TypeToken.Int)) {
						int type = (Integer)con.getValue().getValue();
						subst = NodeValue.makeInteger(type);
						
					} else {
					
						if(con.getDatatype().equals(TypeToken.String)) {
						
							String str = (String)con.getValue().getValue();
						
							subst = NodeValue.makeString(str);
						}
					}
				}
				
			}
			
			newArgs.add(subst);
		}
		
		E_RdfTerm result = new E_RdfTerm(newArgs);
		return result;
	}

	
	public Expr getExpr() {
		return expr;
	}
	public Projection getProjection() {
		return projection;
	}
	
	public Set<S_ColumnRef> getInvolvedColumns() {
		Set<S_ColumnRef> result = new HashSet<S_ColumnRef>();
		
		Collection<SqlExpr> exprs = projection.getNameToExpr().values();
		for(SqlExpr expr : exprs) {
			SqlExprUtils.collectColumnReferences(expr, result);
		}
		
		return result;
	}

	public List<SqlExpr> getSqlExprs() {
		List<SqlExpr> result = new ArrayList<SqlExpr>(4);
		for(int i = 0; i < 4; ++i) {
			SqlExpr sqlExpr = getSqlExpr(i);
			result.add(sqlExpr);
		}
		
		return result;
	}
	
	public SqlExpr getSqlExpr(int index) {
		E_RdfTerm rdfTerm = getRdfTermExpr();
		Expr expr = rdfTerm.getArg(index + 1);
		
		String varName = expr.getVarName();
		Map<String, SqlExpr> map = projection.getNameToExpr();
		SqlExpr result = map.get(varName);
		
		return result;
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