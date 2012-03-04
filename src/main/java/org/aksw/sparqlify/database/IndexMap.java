package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

class IndexMap<K, V> {
	private BiMap<K, Integer> keyToIndex = HashBiMap.create(); 
	private List<V> values = new ArrayList<V>();
	
	private Map<K, V> keyToValue = new HashMap<K, V>();

	
	public IndexMap() {
	}
	
	
	public void setIndex(int index, K key) {
		//K old = keyToIndex.inverse().get(index);
		throw new NotImplementedException();
	}
	
	public void put(K key, V value) {
		Integer index = keyToIndex.get(key);
		if(index == null) {
			keyToIndex.put(key, values.size());			
			values.add(value);
		} else {
			values.set(index, value);
		}

		keyToValue.put(key, value);
	}
	
	public Integer getIndex(K key) {
		return keyToIndex.get(key);
	}
	
	public V get(int index) {
		return values.get(index);
	}
	
	public K getKey(int index) {
		return keyToIndex.inverse().get(index);
	}
	
	public V get(K key) {
		return keyToValue.get(key);
	}
	
	public void clear()
	{
		keyToIndex.clear();
		keyToValue.clear();
		values.clear();
	}

	/*
	public static <H, N> boolean containsAll(Collection<H> haystack, Collection<N> needle) {
		for(N item : needle) {
			if(!haystack.contains(item)) {
				return false;
			}
		}
		return true;
	}*/
	
	/**
	 * 
	 * TODO Do not modify this view, it won't update the structures
	 * @return
	 */
	public Set<K> keySet() {
		return keyToValue.keySet();
	}
	
	public boolean containsAllKeys(Collection<K> keys) {
		return keyToIndex.keySet().containsAll(keys);
	}
}