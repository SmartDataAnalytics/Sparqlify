package org.aksw.sparqlify.core.test;

import org.springframework.core.io.Resource;

public class QueryBundle {
	private String name;
	private Resource query;
	private Resource result;
	
	public QueryBundle(String name, Resource query, Resource result) {
		super();
		this.name = name;
		this.query = query;
		this.result = result;
	}
	
	public String getName() {
		return name;
	}
	
	public Resource getQuery() {
		return query;
	}
	
	public Resource getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "QueryBundle [query=" + query + ", result=" + result + "]";
	}
}