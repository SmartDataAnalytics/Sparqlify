package org.aksw.sparqlify.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

public class NestedNormalForm
	extends NestedSet<Clause>
{
	private NestedNormalForm parent;
	private Map<Var, Set<Clause>> varToClauses = new HashMap<Var, Set<Clause>>();
	
	public NestedNormalForm(Set<Clause> clauses) {
		super(clauses, false);
		this.parent = null;
		
		index(this);
	}
	
	/*
	public void getLocalClauses(Collection<Clause> result) {
		for(Set<Clause> value : varToClauses.values()) {
			result.addAll(value);
		}		
	}
	
	public void getClauses(Collection<Clause> result) {
		
	}
	
	public Set<Clause> getClauses() {
		Set<Clause> result = new HashSet<Clause>();
		getLocalClauses(result);
		
		if(parent != null) {
			getClauses(result);
		}
		
		return result;		
	}*/
	
	
	
	private void index(Set<Clause> clauses) {
		for(Clause clause : clauses) {
			index(clause);
		}
	}

	private void index(Clause clause) {
		for(Var var : clause.getVarsMentioned()) {
			Set<Clause> clauses = varToClauses.get(var);
			if(clauses == null) {
				clauses = new HashSet<Clause>();
				varToClauses.put(var, clauses);
			}
			clauses.add(clause);
		}		
	}
	
	public NestedNormalForm(NestedNormalForm parent, boolean asView) {
		super(parent, asView);
		this.parent = parent;

		if(!asView) {
			if(parent != null) {
				// Index the clauses
				index(this);
			}
		}
	}
	
	public NestedNormalForm getParent() 
	{
		return parent;
	}

	public Set<Clause> getClausesByVar(Var var) {
		Set<Clause> result = varToClauses.get(var);
		
		if(result == null && parent != null) {
			return parent.getClausesByVar(var);
		}
		
		return result;
	}
	
	/*
	public Set<Clause> getClausesByVars(Collection<Var> vars) {
		if(vars.size() == 1) {
			return getClausesByVar(vars.iterator().next());
		}

		Set<Clause> result = new HashSet<Clause>();
		
		float scanFactor = 0.75f;
		if(vars.size() > scanFactor * this.size()) {
			for(Clause expr : this) {
				if(CollectionUtils.containsAny(expr.getVarsMentioned(), vars)) {
					result.add(expr);
				}
			}
		} else {
			// TODO Maybe its always faster to go with this option
			for(Var var : vars) {
				result.addAll(getClausesByVar(var));
			}
		}
		
		return result;		
		
		
	}*/

	//protected Set<Clause> 
	
	@Override
	protected void onRemove(Clause clause) {
		for(Var var : clause.getVarsMentioned()) {
			Set<Clause> clauses = varToClauses.get(var);
			if(clauses == null) {
				if(parent != null) {
					clauses = parent.getClausesByVar(var);
					
					if(clauses != null && clauses.contains(clause)) {
						clauses = new NestedSet<Clause>(clauses, true);
						varToClauses.put(var, clauses);
						clauses.remove(clause);
					}
				}				
			} else {
				clauses.remove(clause);
			}			
		}
	}
	
	@Override
	protected void onAdd(Clause clause) {
		for(Var var : clause.getVarsMentioned()) {
			Set<Clause> clauses = varToClauses.get(var);
			if(clauses == null && parent != null) {
				clauses = parent.getClausesByVar(var);
				
				if(clauses != null && !clauses.contains(clause)) {
					clauses = new NestedSet<Clause>(clauses, true);
					varToClauses.put(var, clauses);
				}				
			}
			
			// If there is still no clause
			if(clauses == null) {
				clauses = new HashSet<Clause>();
				varToClauses.put(var, clauses);
			}

			clauses.add(clause);
		}		
	}

	
	/*
	public Set<Clause> getClauses() 
	{
		HashSet<Clause> result = new HashSet<Clause>();
		
		NestedNormalForm current = this;
		while(current != null) {
			this.getClauses()
		}
		
		return result;
	}
	*/

	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result
				+ ((varToClauses == null) ? 0 : varToClauses.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NestedNormalForm other = (NestedNormalForm) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (varToClauses == null) {
			if (other.varToClauses != null)
				return false;
		} else if (!varToClauses.equals(other.varToClauses))
			return false;
		return true;
	}
	*/
	
	
	
}