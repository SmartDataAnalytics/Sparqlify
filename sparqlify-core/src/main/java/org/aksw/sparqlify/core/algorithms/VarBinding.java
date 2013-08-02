package org.aksw.sparqlify.core.algorithms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.utils.QuadUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;


class BindingVal
	extends VarConst<Var, Node>
{
	public BindingVal() {
		super();
	}
	
	public BindingVal(Set<Var> keys, Node value) {
		super(keys, value);
	}	
}



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
	private Map<Var, Integer> keyToToken = new HashMap<Var, Integer>();
	private Map<Var, Integer> valueToToken = new HashMap<Var, Integer>();
	private Map<Integer, BindingVal> tokenToSet = new HashMap<Integer, BindingVal>();
	//private Map<Integer, Node> tokenToValue = new HashMap<Integer, Node>();
	
	private int nextToken;
	
	public VarBinding() {
	}

	public Map<Var, Node> getQueryVarToConstant() {
		Map<Var, Node> result = new HashMap<Var, Node>();
		
		for(Entry<Var, Integer> entry : keyToToken.entrySet()) {
			Var value = entry.getKey();
			Integer token = entry.getValue();
			
			BindingVal val = tokenToSet.get(token);
			Node constant = val.getValue();
			result.put(value, constant);
		}
		
		return result;				
	}
	
	public IBiSetMultimap<Var, Var> getQueryVarToViewVars() {
		IBiSetMultimap<Var, Var> result = new BiHashMultimap<Var, Var>();
		
		for(Entry<Var, Integer> entry : keyToToken.entrySet()) {
			Var value = entry.getKey();
			Integer token = entry.getValue();
			
			BindingVal val = tokenToSet.get(token);
			Set<Var> keys = val.getKeys();
			result.putAll(value, keys);
		}
		
		return result;		
	}

	/*
	 * Return a mapping from view to query vars
	 * 
	 */
	public IBiSetMultimap<Var, Var> getViewVarToQueryVars() {
		IBiSetMultimap<Var, Var> tmp = getQueryVarToViewVars();
		IBiSetMultimap<Var, Var> result = tmp.getInverse();
		
		return result;
		
		/*
		Multimap<Var, Var> result = HashMultimap.create();
		
		for(Entry<Var, Integer> entry : valueToToken.entrySet()) {
			Var value = entry.getKey();
			Integer token = entry.getValue();
			
			BindingVal val = tokenToSet.get(token);
			Set<Var> keys = val.getKeys();
			result.putAll(value, keys);
		}
		
		return result;
		*/
	}
	
	public Set<Var> getQueryVars() {
		return keyToToken.keySet();
	}

	public Set<Var> getViewVars(Var queryVar) {
		BindingVal tmp = get(queryVar);
		if(tmp == null) {
			return null;
		} 
		
		return tmp.getKeys();
	}
	
	public Node getConstant(Var queryVar) {
		BindingVal tmp = get(queryVar);
		if(tmp == null) {
			return null;
		} 
		
		return tmp.getValue();
	}
	
	
	public BindingVal get(Var queryVar) {
		Integer token = keyToToken.get(queryVar);

		if(token == null) {
			return null;
		}

		BindingVal result = tokenToSet.get(token);
		
		return result;
	}
	
	/*
	public VarBinding(IBiSetMultimap<Var, Var> queryVarToViewVars) {
		this.queryVarToViewVars = queryVarToViewVars;
	}
	*/

	/*
	public boolean putAll(VarBinding other) {
		for(other.ge)
	}
	*/
		
	public boolean put(Var queryVar, Var viewVar) {
		Integer keyToken = keyToToken.get(queryVar);
		Integer valueToken = valueToToken.get(viewVar);


		if(valueToken == null) {
			if(keyToken == null) {
				Integer token = nextToken++;
				
				valueToToken.put(viewVar, token);
				keyToToken.put(queryVar, token);
				
				BindingVal val = new BindingVal();
				val.getKeys().add(viewVar);
				
				tokenToSet.put(token, val);				
			} else {

				// The value already exists - point the key to the token
				valueToToken.put(viewVar, keyToken);
				
				BindingVal val = tokenToSet.get(keyToken);
				
				val.getKeys().add(viewVar);
			}
		} else {
			if(keyToken == null) {

				// The value exists, but has not yet been associated with the key
				keyToToken.put(queryVar, valueToken);
				
				
			} else {
				
				if(keyToken.equals(valueToken)) {
					return true;
				}
				
				// Merge both the key and value token things
				BindingVal a = tokenToSet.get(keyToken);
				BindingVal b = tokenToSet.get(valueToken);
				
				Node valA = a.getValue();
				Node valB = b.getValue();
				
				Node res = valA == null ? valB : valB;
				
				if(valB != null && valB != res) {
					return false;
				}
				
				a.setValue(res);
				
				for(Var vv : b.getKeys()) {
					valueToToken.put(vv, keyToken);
				}				
				
				tokenToSet.remove(valueToken);
				a.getKeys().addAll(b.getKeys());
			} 			
		}
		
		return true;
	}

	
	public boolean put(Var queryVar, Node node) {
		if(node == null) {
			return true;
		} else if(node.isVariable()) {
			return put(queryVar, (Var)node);
		}

		Integer token = keyToToken.get(queryVar);
		
		if(token == null) {
			token = nextToken++;
			
			keyToToken.put(queryVar, token);
			
			BindingVal val = new BindingVal();
			tokenToSet.put(token, val);
			
			val.setValue(node);
			
			return true;
		} else {
			BindingVal val = tokenToSet.get(token);
			Node existing = val.getValue();
			if(existing == null) {
				val.setValue(node);
				return true;
			} else if(existing.equals(node)) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	
	public void putAll(VarBinding that) {
		for(Entry<Var, Var> entry : that.getQueryVarToViewVars().entries()) {
			this.put(entry.getKey(), entry.getValue());
		}

		for(Entry<Var, Node> entry : that.getQueryVarToConstant().entrySet()) {
			this.put(entry.getKey(), entry.getValue());
		}
	}

	
	public static VarBinding create(Quad a, Quad b)
	{
		VarBinding result = new VarBinding();

		List<Node> nAs = QuadUtils.quadToList(a);
		List<Node> nBs = QuadUtils.quadToList(b);
		
		for(int i = 0; i < 4; ++i) {
			Var nA = (Var)nAs.get(i);
			Node nB = nBs.get(i);
			
			if(!result.put(nA, nB)) {
				return null;
			}
		}
		
		return result;
	}


	@Override
	public String toString() {
		String result = "{";
		boolean isFirst = true;
		for(Entry<Var, Integer> entry : keyToToken.entrySet()) {
			Var key = entry.getKey();
			Integer token = entry.getValue();
			BindingVal val = tokenToSet.get(token);
			
			if(isFirst) {
				isFirst = false;
			} else {
				result += ", ";
			}
			
			result += key + ": (" + token + ")" + val;
		}
		result += "}";
		return result;
		
		//return "keyToToken: " + keyToToken + ", tokenToSet:" + tokenToSet;
	}

	
	
	/*
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
	
	* /
	
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
	*/
}
