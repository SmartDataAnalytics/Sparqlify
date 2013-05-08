package org.aksw.sparqlify.core.algorithms;

import java.util.HashSet;
import java.util.Set;

public class VarConst<K, V> {
	private Set<K> keys;
	private V value;

	
	public VarConst() {
		this.keys = new HashSet<K>();
	}
	

	public VarConst(Set<K> keys, V value) {
		this.keys = keys;
		this.value = value;
	}

	public Set<K> getKeys() {
		return keys;
	}

	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}


	@Override
	public String toString() {
		return "[keys=" + keys + ", value=" + value + "]";
	}
}