package org.aksw.sparqlify.core.algorithms;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.sparql.core.Var;


/**
 * View instances relate variables (usually those of of a
 * SPARQL query subject to rewriting) to the variables and
 * constants of a view.
 * 
 * A view instance is comprised of
 * - A view definition
 * - A binding, which relates query variables to variables of the view and constants
 * 
 * TODO View instances should implement a common interface with mappings
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ViewInstance {

	
	private ViewDefinition viewDefinition;
	private VarBinding binding;
	
	
	public ViewInstance(ViewDefinition viewDefinition, VarBinding binding) {
		this.viewDefinition = viewDefinition;
		this.binding = binding;
	}
	
	
	public ViewDefinition getViewDefinition() {
		return viewDefinition;
	}
	
	
	public VarBinding getBinding() {
		return binding;
	}


	/*
	public Set<Var> getViewVariablesForQueryVariable(Var var) {
		
	}*/
	
	
	
	/**
	 * Convenience getter.
	 * 
	 * Returns the set of definitions associated with a view variable.
	 * 
	 * @param viewVar
	 * @return
	 */
	public Set<RestrictedExpr> getDefinitionsForViewVariable(Var viewVar) {		
		Collection<RestrictedExpr> defs = viewDefinition.getMapping().getVarDefinition().getDefinitions(viewVar);
		
		Set<RestrictedExpr> result = new HashSet<RestrictedExpr>(defs);
		return result;
	}
	
	
	/**
	 * Convenience getter.
	 * 
	 * Returns the variable definitions underlying this instance.
	 * @return 
	 * 
	 * @return
	 */
	public VarDefinition getVarDefinition() {		
		return viewDefinition.getMapping().getVarDefinition();
	}

	
	
	/**
	 * Returns for a query variable the set of definitions that is indirectly associated with it via the binding
	 * 
	 * @param queryVar
	 */
	/*
	public Set<VariableDefinition> getDefinitionsForQueryVariable(Var queryVar) {
		Set<Var> viewVars = binding.getViewVariablesForQueryVariable(queryVar);
		
		Set<VariableDefinition> result = new HashSet<VariableDefinition>();
		
		for(Var viewVar : viewVars) {
			Collection<VariableDefinition> varDefs = viewDefinition.getMapping().getVariableDefinitions().get(viewVar);
			result.addAll(varDefs);
		}
		
		return result;
	}
	*/
	
	/*
	public getSqlNode() 
	{
		return viewDefinition.getMapping().getSqlNode();
	}*/
	
	//public 
	
	

	@Override
	public String toString() {
		return "ViewInstance for " + viewDefinition.getName() + ", binding: " + binding;
		
		//return "ViewInstance [viewDefinition=" + viewDefinition + ", binding="
		//		+ binding + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((binding == null) ? 0 : binding.hashCode());
		result = prime * result
				+ ((viewDefinition == null) ? 0 : viewDefinition.hashCode());
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
		ViewInstance other = (ViewInstance) obj;
		if (binding == null) {
			if (other.binding != null)
				return false;
		} else if (!binding.equals(other.binding))
			return false;
		if (viewDefinition == null) {
			if (other.viewDefinition != null)
				return false;
		} else if (!viewDefinition.equals(other.viewDefinition))
			return false;
		return true;
	}	
}
