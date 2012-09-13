package org.aksw.sparqlify.database;

public interface Constraint {
	public boolean isSatisfiedBy(Object value);
}
