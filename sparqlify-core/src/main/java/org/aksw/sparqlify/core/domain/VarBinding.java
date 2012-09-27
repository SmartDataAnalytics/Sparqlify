package org.aksw.sparqlify.core.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;

import com.hp.hpl.jena.sparql.core.Var;


/**
 * A variable binding maps query vars to sets of view variables.
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class VarBinding {
	private IBiSetMultimap<Var, Var> queryVarToViewVars = new BiHashMultimap<Var, Var>();
	
	public VarBinding() {
		this.queryVarToViewVars = new BiHashMultimap<Var, Var>();
	}

	public VarBinding(IBiSetMultimap<Var, Var> queryVarToViewVars) {
		this.queryVarToViewVars = queryVarToViewVars;
	}

	
	public void put(Var queryVar, Var viewVar) {
		queryVarToViewVars.put(queryVar, viewVar);
	}
	
	
	public Set<Var> getQueryVars() {
		return queryVarToViewVars.asMap().keySet();
	}

	public Set<Var> get(Var queryVar) {
		return queryVarToViewVars.get(queryVar);
	}
	
	
	public IBiSetMultimap<Var, Var> getMap()
	{
		return queryVarToViewVars;
	}


	public VarBinding computeClosure() {
		Map<Var, Set<Var>> state = new HashMap<Var, Set<Var>>();

		// Invert the binding: For the set of viewVars, get the set of queryVars that map to them
		IBiSetMultimap<Var, Var> viewVarsToQueryVars = queryVarToViewVars.getInverse();

		
		for(Entry<Var, Collection<Var>> entry : queryVarToViewVars.asMap().entrySet()) {
			Var queryVar = entry.getKey(); //open.iterator().next();
			Collection<Var> viewVars = entry.getValue();

			// Re-use a corresponding viewVar-set
			// in the state, otherwise create a new one
			Set<Var> mergedViewVars = state.get(queryVar);
			if(mergedViewVars == null) {
				mergedViewVars = new HashSet<Var>(viewVars);
			}
			
			for(Var viewVar : viewVars) {
				Set<Var> queryVarsB = viewVarsToQueryVars.get(viewVar);
				
				for(Var queryVarB : queryVarsB) {
					if(queryVarB.equals(queryVar)) {
						continue;
					}
					
					Set<Var> viewVarsC = state.get(queryVarB);
					// A viewVars set might not exist for a key yet
					if(viewVarsC == null) {
						viewVarsC = queryVarToViewVars.get(queryVarB);
					}

					mergedViewVars.addAll(viewVarsC);
					
					state.put(queryVarB, mergedViewVars);					
				}
			}
		}			

		// OPTIMIZE: Use some batch function for this - or at least hide the complexity here
		IBiSetMultimap<Var, Var> map = new BiHashMultimap<Var, Var>();
		for(Entry<Var, Set<Var>> entry : state.entrySet()) {
			Var queryVar = entry.getKey();
			
			for(Var viewVar : entry.getValue()) {
				map.put(queryVar, viewVar);
			}
			
		}
		
		VarBinding result = new VarBinding(map); 
		
		return result;
	}
		
	
//	public static <K, V> Set<V> getAll(Collection<K> keys, IBiSetMultimap<K, V> map) {
//		Set<V> result
//	}
	
	

	/*

	public Set<Var> getEquivalences(Var key, boolean reflexiv)
	{
		Set<Var> result = MultimapUtils.transitiveGetBoth(queryVarToViewVars, key);
		if(reflexiv) {
			result.add(key);
		}
		
		return result;
	}
	
	public Set<Var> getAllEquivalences(Collection<Var> keys, boolean reflexiv)
	{
		Set<Var> result = new HashSet<Var>();

		Set<Var> open = new HashSet<Var>(keys);
		while(!open.isEmpty()) {
			Var key = open.iterator().next();
			open.remove(key);

			Set<Var> equivs = getEquivalences(key, reflexiv);
			open.removeAll(equivs);
			
			result.addAll(equivs);			
		}			

		return result;
	}
	
	*/
	
	@Override
	public String toString() {
		return "" + queryVarToViewVars;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((queryVarToViewVars == null) ? 0 : queryVarToViewVars
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
		VarBinding other = (VarBinding) obj;
		if (queryVarToViewVars == null) {
			if (other.queryVarToViewVars != null)
				return false;
		} else if (!queryVarToViewVars.equals(other.queryVarToViewVars))
			return false;
		return true;
	}
	
}
