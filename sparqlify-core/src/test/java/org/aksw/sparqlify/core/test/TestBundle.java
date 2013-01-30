package org.aksw.sparqlify.core.test;

import org.springframework.core.io.Resource;

class TestBundle {
	private Resource sql;
	private Resource mapping;
	private Resource expected;
	private Resource manifest;
	
	public TestBundle(Resource sql, Resource mapping, Resource expected, Resource manifest) {
		super();
		this.sql = sql;
		this.mapping = mapping;
		this.expected = expected;
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

	@Override
	public String toString() {
		return "TestBundle [sql=" + sql + ", mapping=" + mapping
				+ ", expected=" + expected + ", manifest=" + manifest + "]";
	}
}