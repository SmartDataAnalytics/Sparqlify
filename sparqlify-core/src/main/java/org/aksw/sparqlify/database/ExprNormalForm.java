package org.aksw.sparqlify.database;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

/*
class ExprIndex {
	private Multimap<Var, Expr> singleVar = HashMultimap.create();
	private
	
	public void add(Expr expr) {
		Set<Var> vars = expr.getVarsMentioned();
		if(vars.size() == 1) {
			singleVar.put(vars.iterator().next(), expr);
		}
	}
	
}*/

public abstract class ExprNormalForm {

	protected Multimap<Var, Clause> varToClauses = HashMultimap.create();
	protected Multimap<Set<Var>, Clause> varsToClauses = HashMultimap.create();
	protected Multimap<Expr, Clause> exprToClauses = HashMultimap.create();
	
	//protected Multimap<Set<Var>
	
	// expressions that are part of all clauses
	protected Set<Expr> commonExprs = new HashSet<Expr>();
	
	protected Set<Var> varsMentioned = new HashSet<Var>();
	
	public Set<Clause> filterByVars(Set<Var> requiredVars) {
		
		Set<Clause> result = new HashSet<Clause>();
		
		
		for(Entry<Set<Var>, Collection<Clause>> entry : varsToClauses.asMap().entrySet()) {

			Set<Var> clauseVars = entry.getKey();
			
			if(!clauseVars.containsAll(requiredVars)) {
				continue;
			}
			
			result.addAll(entry.getValue());
		}
		
		/*
		Collection<Clause> clauses = getClauses();		
		for(Clause clause : clauses) {
			Set<Var> clauseVars = clause.getVarsMentioned();
			
			if(!clauseVars.containsAll(requiredVars)) {
				continue;
			}
			
			result.add(clause);
		}*/

		return result;
	}
	
	public ExprNormalForm(Collection<Clause> clauses) {
		addAll(clauses);
	}

	private void addAll(Collection<Clause> clauses) {
		for(Clause clause : clauses) {
			add(clause);
		}
	}
	
	private boolean add(Clause clause) {
		Set<Var> vars = clause.getVarsMentioned();
		if(varsToClauses.put(vars, clause)) {
			for(Var var : vars) {
				varToClauses.put(var, clause);
			}
		
			varsMentioned.addAll(vars);
			
			
			// Map the new exprs to the clauses they appear in
			// and update the common exprs map			
			if(exprToClauses.isEmpty()) {
				commonExprs.addAll(clause.getExprs());
			} else {
				commonExprs.retainAll(clause.getExprs());				
			}

			
			for(Expr expr : clause.getExprs()) {
				exprToClauses.put(expr, clause);
			}			
			
			return true;
		}
				
		return false;
	}
	
	
	/**
	 * Return all expressions having exactly the specified vars
	 * 
	 * @param vars
	 * @return
	 */
	public Collection<Clause> getExactly(Set<Var> vars) {
		return varsToClauses.get(vars);
	}

	
	/*
	public static <K, V> Set<V> getAll(Map<K, V> map, Collection<?> keys) {
		Set<V> result = new HashSet<V>();

		float scanFactor = 0.75f;
		Collection<V> values = map.values();
		if(keys.size() > scanFactor * values.size()) {
			for(V clause : values) {
				if(CollectionUtils.containsAny(clause.getVarsMentioned(), keys)) {
					result.add(clause);
				}
			}
		} else {
			// TODO Maybe its always faster to go with this option
			for(Object key : keys) {
				result.addAll(map.get(key));
			}
		}
		
		return result;		
	}*/
	
	public Set<Clause> get(Collection<Var> vars) {
		Set<Clause> result = new HashSet<Clause>();

		float scanFactor = 0.75f;
		Collection<Clause> values = varsToClauses.values();
		if(vars.size() > scanFactor * values.size()) {
			for(Clause clause : values) {
				if(CollectionUtils.containsAny(clause.getVarsMentioned(), vars)) {
					result.add(clause);
				}
			}
		} else {
			// TODO Maybe its always faster to go with this option
			for(Var var : vars) {
				result.addAll(varToClauses.get(var));
			}
		}
		
		return result;
	}

	public Collection<Clause> get(Var var) {
		return varToClauses.get(var);
	}
	
	public Collection<Clause> getClauses() {
		return varsToClauses.values();
	}
	
	@Override
	public String toString() {
		return varsToClauses.values().toString();
	}
	
	public Set<Expr> getCommonExprs() {
		return commonExprs;
	}
	
	/*
	public Set<Expr> getCommonExprs(Var var) {
		
	}*/

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((varsToClauses == null) ? 0 : varsToClauses.hashCode());
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
		ExprNormalForm other = (ExprNormalForm) obj;
		if (varsToClauses == null) {
			if (other.varsToClauses != null)
				return false;
		} else if (!varsToClauses.equals(other.varsToClauses))
			return false;
		return true;
	}
	
	public int size() {
		return varsToClauses.size();
	}
	
	
	public Set<Var> getVarsMentioned() {
		return varsMentioned;
	}
	
	
	/*
	public Dnf eval(Map<Var, Node> binding) {
		
	}*/
}
