package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.util.Pair;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class IndirectEquiMap<K, V> {
	private Map<K, Integer> keyToToken = new HashMap<K, Integer>();
	private Multimap<Integer, K> tokenToKeys = HashMultimap.create(); 
	private Map<Integer, V> tokenToValue = new HashMap<Integer, V>();
	
	private int nextToken = 0;
	
	public Set<K> keySet() {
		return keyToToken.keySet();
	}
	
	public Multimap<Integer, K> getEquivalences()
	{
		return tokenToKeys;
	}
	
	public Collection<K> getEquivalences(K key) {
		return tokenToKeys.get(keyToToken.get(key));
	}
	
	
	private void putKeyToken(K key, int token) {
		keyToToken.put(key, token);
		tokenToKeys.put(token, key);		
	}
	
	
	public void stateEqual(K a, K b, V value) {
		stateEqual(a, b, value, true);		
	}
	
	
	public Pair<V, V> stateEqual(K a, K b) {
		return stateEqual(a, b, null, false);
	}
	

	/*
	public Collection<> stateEqual(Collection<K> keys) {
		
	}*/
	
	public void stateEqual(Collection<K> keys, V value) {
		int newToken = ++nextToken;
		
		for(K key : keys) {
			Integer oldToken = keyToToken.get(key);
			if(oldToken != null) {
				tokenToValue.remove(oldToken);
				tokenToKeys.putAll(newToken, tokenToKeys.get(oldToken));
				tokenToKeys.removeAll(oldToken);
			}
			
			putKeyToken(key, newToken);
		}

		tokenToValue.put(newToken, value);
	}
	
	
	/**
	 * States an equality between keys.
	 * 
	 * if overwrite is true, conflicts can not occur as they are overwritten with value. Return value is always null.
	 * if overwrite is false, in case of conflict the pair of conflicting values is returned
	 * 
	 * Conflicts can be resolved using stateEqual(a, b, value)
	 * 
	 * 
	 * @param a
	 * @param b
	 */
	private Pair<V, V> stateEqual(K a, K b, V value, boolean overwrite) {
		Integer ta = keyToToken.get(a);
		Integer tb = keyToToken.get(b);
		
		if(ta == null) {
			if(tb == null) {
				int token = ++nextToken;
				
				putKeyToken(a, token);
				putKeyToken(b, token);
				
			} else {
				putKeyToken(a, tb);
			}
		} else {
			if(tb == null) {
				putKeyToken(b, ta);
			} else {
				
				V va = tokenToValue.get(ta);
				V vb = tokenToValue.get(tb);

				if(va != null && vb != null && !va.equals(vb)) {
					// Conflict: Equality stated, but two distinct values
					if(overwrite) {
						va = value;
					} else  {
						return Pair.create(va, vb);
					}
				}
				
				if(va == null) {
					va = vb;
				}
				
				// Copy to avoid ConcurrentModificationException
				Collection<K> ka = new ArrayList<K>(tokenToKeys.get(ta));
				Collection<K> kb = new ArrayList<K>(tokenToKeys.get(tb));
				
				Collection<K> tmp;
				int tt;
				if(kb.size() > ka.size()) {
					tmp = ka;
					ka = kb;
					kb = tmp;
					
					tt = ta;
					ta = tb;
					tb = tt;
				}
				
				for(K k : kb) {
					tokenToKeys.remove(tb, k);
					putKeyToken(k, ta);
				}

				if(va != null) {
					tokenToValue.put(ta, va);
				}
			}
		}

		return null;
	}
	
	
	/**
	 * Puts a new value, overwrites existing ones.
	 * 
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		Integer token = keyToToken.get(key);
		if(token == null) {
			token = ++nextToken;
			keyToToken.put(key, token);
		}
		
		tokenToValue.put(token, value);
		tokenToKeys.put(token, key);
	}
	
	public V get(K key) {
		Integer token = keyToToken.get(key);
		if(token == null) {
			return null;
		}
		
		return tokenToValue.get(token);
	}


	public boolean isEqual(K a, K b) {
		Integer ta = keyToToken.get(a);
		return ta != null && ta.equals(keyToToken.get(b));
	}
	
	@Override
	public String toString() {
		String result = "[";
		boolean isFirst = true;
		for(Entry<Integer, Collection<K>> entry : tokenToKeys.asMap().entrySet()) {
			if(!isFirst) {
				result += ", ";
			}
			
			result += entry.getValue() + ": " + tokenToValue.get(entry.getKey());
		}
		result += "]";
		return result;
	}
}