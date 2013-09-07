package org.aksw.sparqlify.dump.db;

public class DumpDbConfig {
	private String jdbcUrl;
	private String userName;
	
	// Note: Password must be specified explictly when resuming dump - we are not storing it here.
}
