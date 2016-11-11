package org.aksw.sparqlify.config.v0_2.bridge;

import java.util.Map;
import java.util.Set;

public class BasicTableInfo {
	private Map<String, String> rawTypeMap;
	private Set<String> nullableColumns;

	public BasicTableInfo(Map<String, String> rawTypeMap, Set<String> nullableColumns) {
		this.rawTypeMap = rawTypeMap;
		this.nullableColumns = nullableColumns;
	}

	public Map<String, String> getRawTypeMap() {
		return rawTypeMap;
	}

	public Set<String> getNullableColumns() {
		return nullableColumns;
	}

	@Override
	public String toString() {
		return "BasicTableInfo [rawTypeMap=" + rawTypeMap + ", nullableColumns=" + nullableColumns + "]";
	}
}