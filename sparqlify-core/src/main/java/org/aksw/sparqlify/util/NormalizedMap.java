package org.aksw.sparqlify.util;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;

class TransformerTrimAndToLowerCase
	implements Transformer<String, String>
{
	@Override
	public String transform(String input) {
		String result;
		if(input == null) {
			result = null;
		} else {
			result = input.trim().toLowerCase();
		}
		
		return result;
	}	
}


public class NormalizedMap<K, V>
	extends AbstractMap<K, V>
{
	// This map contains the mapping for the *normalized* key
	private Map<K, V> map;
	
	// Original inserted key to normalized key
	private BidiMap<K, K> origToNorm; 
	
	private Transformer<K, K> keyNormalizer;
	

	public NormalizedMap(Transformer<K, K> keyNormalizer) {
		this(new HashMap<K, V>(), keyNormalizer);
	}
	
	public NormalizedMap(Map<K, V> map, Transformer<K, K> keyNormalizer) {
		this.map = map;
		this.keyNormalizer = keyNormalizer;		

		this.origToNorm = new DualHashBidiMap<K, K>();
	}

	@Override
	public boolean containsKey(Object key) {
		boolean result;
		try {
			@SuppressWarnings("unchecked")
			K k = (K)key;
			
			K normKey = keyNormalizer.transform(k);
				
			result = map.containsKey(normKey);
		} catch(ClassCastException e) {
			result = false;
		}			
		
		return result;
	}

	
	@Override
	public V put(K key, V value) {
		K normKey = keyNormalizer.transform(key);
	
		//if(origToNorm.inverseBidiMap().get(normKey)
		
		
		origToNorm.put(key, normKey);
		map.put(normKey, value);
		
		return value;
	}
	
	@Override
	public V get(Object key) {
		V result;
		try {
			@SuppressWarnings("unchecked")
			K k = (K)key;
			
			K normKey = keyNormalizer.transform(k);
			
			result = map.get(normKey);
			
		} catch(ClassCastException e) {
			result = null;
		}			
		
		return result;
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		// TODO: Return a chain map using the original keys
		return map.entrySet();
	}

	/**
	 * Note: The different to apache.commons.collections.CaseInsensitiveMap
	 * is, that the original case of the keys is retained. 
	 * 
	 * @return
	 */
	public static <V> Map<String, V> createCaseInsensitiveMap() {
		Transformer<String, String> transformer = new TransformerTrimAndToLowerCase();
		
		Map<String, V> result = new NormalizedMap<String, V>(transformer);

		
		return result;
	}
	
	
	public static void main(String[] args) {
		//Map<String,String> test = new CaseInsensitiveMap<String>();
		Map<String, Integer> test = NormalizedMap.createCaseInsensitiveMap();
		
		test.put("a", 1);
		test.put("B", 2);
		
		System.out.println(test.get("A"));
		System.out.println(test.get("B"));

		test.put("A", 3);
		System.out.println(test.get("a"));
		
	}
}
