package org.aksw.sparqlify.test;

import org.springframework.core.io.Resource;

public class TestBundleCsv {
	private Resource csv;
	private Resource config;
	private Resource mapping;
	private Resource expected;
	
	public TestBundleCsv(Resource csv, Resource config, Resource mapping,
			Resource expected) {
		super();
		this.csv = csv;
		this.config = config;
		this.mapping = mapping;
		this.expected = expected;
	}

	public Resource getCsv() {
		return csv;
	}

	public Resource getConfig() {
		return config;
	}

	public Resource getExpected() {
		return expected;
	}

	public Resource getMapping() {
		return mapping;
	}

	@Override
	public String toString() {
		return "TestBundleCsv [csv=" + csv + ", config=" + config
				+ ", mapping=" + mapping + ", expected=" + expected + "]";
	}
}
