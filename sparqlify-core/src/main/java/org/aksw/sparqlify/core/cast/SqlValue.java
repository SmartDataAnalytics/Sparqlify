package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;

public class SqlValue {
	public static final SqlValue TRUE = new SqlValue(TypeToken.Boolean, true);
	public static final SqlValue FALSE = new SqlValue(TypeToken.Boolean, false);
	
	public static final SqlValue TYPE_ERROR = new SqlValue(TypeToken.TypeError, false);
	
	/**
	 * Null values allowed
	 * 
	 */
	private TypeToken typeToken;
	private Object value;
	
	public SqlValue(TypeToken typeToken, Object value) {
		this.typeToken = typeToken;
		this.value = value;
	}

	public TypeToken getTypeToken() {
		return typeToken;
	}

	public Object getValue() {
		return value;
	}

	
	
	@Override
	public String toString() {
		return value + "(" + typeToken + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((typeToken == null) ? 0 : typeToken.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SqlValue other = (SqlValue) obj;
		if (typeToken == null) {
			if (other.typeToken != null)
				return false;
		} else if (!typeToken.equals(other.typeToken))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}