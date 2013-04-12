package org.aksw.sparqlify.core.test;

import java.util.List;

import org.springframework.core.io.Resource;

public class TestBundle {
	private String name;
	private Resource sql;
	private Resource mapping;
	private Resource expected;
	private Resource manifest;
	private List<QueryBundle> queryBundles;
	

	public TestBundle(String name, Resource sql, Resource mapping, Resource expected, Resource manifest, List<QueryBundle> queryBundles) {
		super();
		this.name = name;
		this.sql = sql;
		this.mapping = mapping;
		this.expected = expected;
		this.queryBundles = queryBundles;
	}
	
	public String getName() {
		return name;
	}

	public Resource getSql() {
		return sql;
	}
	
	public Resource getMapping() {
		return mapping;
	}

	public Resource getExpected() {
		return expected;
	}

	public Resource getManifest() {
		return manifest;
	}

	public List<QueryBundle> getQueryBundles()
	{
		return queryBundles;
	}

	@Override
	public String toString() {
		return "TestBundle [sql=" + sql + ", mapping=" + mapping
				+ ", expected=" + expected + ", manifest=" + manifest + "]";
	}
}