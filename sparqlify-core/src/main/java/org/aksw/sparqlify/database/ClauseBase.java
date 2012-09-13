package org.aksw.sparqlify.database;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * Dnf, Clauses, and Exprs should be treated as immutable!
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ClauseBase
{
	private Set<Var> varsMentioned;
	private Set<Expr> exprs;
	
	private Multimap<Var, Expr> varToExpr;
	
	/**
	 * Get expressions having one of the given variables
	 * 
	 * @param vars
	 * @return
	 */
	public Set<Expr> getExprs(Collection<Var> vars) {
		Set<Expr> result = new HashSet<Expr>();

		float scanFactor = 0.75f;
		if(vars.size() > scanFactor * exprs.size()) {
			for(Expr expr : exprs) {
				if(CollectionUtils.containsAny(expr.getVarsMentioned(), vars)) {
					result.add(expr);
				}
			}
		} else {
			// TODO Maybe its always faster to go with this option
			for(Var var : vars) {
				result.addAll(varToExpr.get(var));
			}
		}
		
		return result;
	}
	
	public ClauseBase(Set<Expr> exprs) {
		this.exprs = exprs;
		
		varToExpr = HashMultimap.create();

		
		Set<Var> vars = new HashSet<Var>();
		for(Expr expr : exprs) {
			Set<Var> evs = expr.getVarsMentioned();
			vars.addAll(evs);
			
			for(Var ev : evs) {
				varToExpr.put(ev, expr);
			}
		}
		
		this.varsMentioned = vars;
		
		
	}
	
	public Set<Expr> getExprs() {
		return exprs;
	}
	
	/* No add method: clauses should be immutable, as they might be indexed
	 * somewhere
	@Override
	public boolean add(Expr expr) {
		if(super.add(expr)) { 
			varsMentioned.addAll(expr.getVarsMentioned());
			return true;
		}
		
		return false;
	}*/

	public Set<Var> getVarsMentioned()
	{
		return varsMentioned;
	}
	
	
	public int size() {
		return exprs.size();
	}

	//private Set<Expr> exprs = new HashSet<Expr>();
	
	@Override
	public String toString() {
		return exprs.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exprs == null) ? 0 : exprs.hashCode());
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
		ClauseBase other = (ClauseBase) obj;
		if (exprs == null) {
			if (other.exprs != null)
				return false;
		} else if (!exprs.equals(other.exprs))
			return false;
		return true;
	}
}