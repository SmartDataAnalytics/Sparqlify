package org.aksw.sparqlify.core.cast;

import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.core.TypeToken;

import com.hp.hpl.jena.sparql.expr.NodeValue;

public class SqlTypeMapperImpl
	implements SqlTypeMapper
{
	private Map<String, SqlDatatype> map = new HashMap<String, SqlDatatype>();
	
	public SqlTypeMapperImpl() {
		super();
	}
	
	public Map<String, SqlDatatype> getMap() {
		return map;
	}

//	@Override
	public SqlDatatype getSqlDatatype(String datatypeUri) {
		SqlDatatype result = map.get(datatypeUri);
		return result;
	}

	public static <K, V> void putIfNotExists(Map<K, V> map, K key, V value) {
		boolean containsKey = map.containsKey(key);
		if(containsKey) {
			throw new RuntimeException("Key " + key + " already mapped.");
		}
		
		map.put(key, value);
	}
	
	@Override
	public void register(String datatypeUri, SqlDatatype sqlType) {
		
		putIfNotExists(map, datatypeUri, sqlType);
	}
}