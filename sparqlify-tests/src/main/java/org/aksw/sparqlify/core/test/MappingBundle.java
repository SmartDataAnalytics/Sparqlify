package org.aksw.sparqlify.core.test;

import java.util.List;

import org.springframework.core.io.Resource;

public class MappingBundle {
	
	private Resource mapping;
	
	/**
	 * Expected holds the quads that are expected to be created from the mapping.
	 * May be null.
	 * Note that this field is equivalent to a query 'Select ?g ?s ?p ?o { Graph ?g { ?s ?p ?o } }'
	 */
	private String name;
	private Resource expected;	
	private List<QueryBundle> queryBundles;

	public MappingBundle(String name, Resource mapping, Resource expected, List<QueryBundle> queryBundles) {
		this.name = name;
		this.mapping = mapping;
		this.expected = expected;
		this.queryBundles = queryBundles;
	}
	
	public String getName() {
		return name;
	}
	
	public Resource getMapping() {
		return mapping;
	}

	public Resource getExpected() {
		return expected;
	}

	public List<QueryBundle> getQueryBundles()
	{
		return queryBundles;
	}

	@Override
	public String toString() {
		return "MappingBundle [mapping=" + mapping + ", expected=" + expected
				+ ", queryBundles=" + queryBundles + "]";
	}	
}

