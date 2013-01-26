package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.sparqlify.core.TypeToken;

/**
 * A simple schema which keeps track of column names and column datatypes.
 * 
 * In the future we might also keep track of indexes and stuff, 
 * just like the H2 schema object.
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SchemaImpl
	implements Schema
{
	private List<String> names;
	private Map<String, TypeToken> nameToType;
	private Set<String> nullableNames;

	/**
	 * An empty schema without any columns.
	 * FIXME If zero-columns schema cause troubles, we will change the behaviour
	 * of this ctor to create a single dummy column
	 * 
	 */
	public SchemaImpl() {
		this(new ArrayList<String>(), new HashMap<String, TypeToken>(), new HashSet<String>());
	}
	
	public SchemaImpl(List<String> names, Map<String, TypeToken> nameToType) {
		this(names, nameToType, new HashSet<String>());
	}

	public SchemaImpl(List<String> names, Map<String, TypeToken> nameToType, Set<String> nullableNames) {
		this.names = names;
		this.nameToType = nameToType;
		this.nullableNames = nullableNames;
	}
	
	
	
	public static SchemaImpl create(List<String> names, Map<String, TypeToken> nameToType) {
		return new SchemaImpl(names, nameToType);
	}
	
	@Override
	public int getColumnCount() {
		return names.size();
	}

	@Override
	public String getColumnName(int index) {
		return names.get(index);
	}

	@Override
	public TypeToken getColumnType(int index) {
		String name = names.get(index);
		TypeToken result = nameToType.get(name);
		
		return result;
	}

	@Override
	public List<String> getColumnNames() {
		return names;
	}

	@Override
	public TypeToken getColumnType(String name) {
		TypeToken result = nameToType.get(name);
		
		return result;
	}

	@Override
	public Map<String, TypeToken> getTypeMap() {
		return nameToType;
	}

	@Override
	public String toString() {
		String result = "[";
		boolean isFirst = true;
		for(String name : names) {
			TypeToken type = nameToType.get(name);
			
			if(isFirst) {
				isFirst = false;
			} else {
				result += ", ";
			}
			
			result += name + ": " + type;
		}
		
		result += "]";
		
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((nameToType == null) ? 0 : nameToType.hashCode());
		result = prime * result + ((names == null) ? 0 : names.hashCode());
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
		SchemaImpl other = (SchemaImpl) obj;
		if (nameToType == null) {
			if (other.nameToType != null)
				return false;
		} else if (!nameToType.equals(other.nameToType))
			return false;
		if (names == null) {
			if (other.names != null)
				return false;
		} else if (!names.equals(other.names))
			return false;
		return true;
	}

	@Override
	public boolean isNullable(String columnName) {
		boolean result = nullableNames.contains(columnName);
		return result;
	}

//	@Override
//	public boolean isNullable(boolean assumption) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	
}
