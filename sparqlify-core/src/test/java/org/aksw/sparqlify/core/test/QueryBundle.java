package org.aksw.sparqlify.core.test;

import org.springframework.core.io.Resource;

public class QueryBundle {
	private Resource query;
	private Resource result;
	
	public QueryBundle(Resource query, Resource result) {
		super();
		this.query = query;
		this.result = result;
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