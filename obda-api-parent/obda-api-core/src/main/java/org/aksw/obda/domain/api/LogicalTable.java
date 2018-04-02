package org.aksw.obda.domain.api;

import java.util.Optional;

public interface LogicalTable
	extends Polymorphic
{	

	// Essential functions
	
	default Optional<String> tryGetTableName() {
		return Optional.empty();
	}
	
	default Optional<String> tryGetQueryString() {
		return Optional.empty();
	}
	
	// Convenience functions based on the essential ones
	
	default boolean isTableName() {
		return tryGetTableName().isPresent();
	}

	default boolean isQueryString() {
		return tryGetQueryString().isPresent();
	}

	default String getTableName() {
		return tryGetTableName()
				.orElseThrow(() -> new RuntimeException("Not a table"));
	}

	default String getQueryString() {
		return tryGetQueryString()
				.orElseThrow(() -> new RuntimeException("Not a query string"));
	}
}
