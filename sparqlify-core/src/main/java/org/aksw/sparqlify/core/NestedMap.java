package org.aksw.sparqlify.core;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class NestedMap<K, V>
	extends AbstractMap<K, V>
{
	private NestedMap<K, V> parent;
	private Map<K, V> data;

	public NestedMap() {
		this(null);
	}
		
	public NestedMap(NestedMap<K, V> parent) {
		this(parent, new HashMap<K, V>());
	}

	public NestedMap(NestedMap<K, V> parent, Map<K, V> data) {
		this.parent = parent;
		this.data = data;
	}

	@Override
	public V put(K key, V value) {
		return data.put(key, value);
	}
	
	@Override
	public boolean containsKey(Object key) {
		boolean result = data.containsKey(key);
		if(!result) {
			result = parent.containsKey(key);
		}
		
		return result;
	}

	
	public void collectMap(Map<K, V> result) {
		if(parent != null) {
			parent.collectMap(result);
		}
		
		result.putAll(data);
	}
	
	public Map<K, V> collectMap() {
		Map<K, V> result = new HashMap<K, V>();
		collectMap(result);
		return result;
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		Map<K, V> tmp = collectMap();

		return tmp.entrySet();
	}
	
	
	public static <K, V> NestedMap<K, V> create() {
		return new NestedMap<K, V>();
	}

	public static <K, V> NestedMap<K, V> create(NestedMap<K, V> parent) {
		return new NestedMap<K, V>(parent);
	}

}
