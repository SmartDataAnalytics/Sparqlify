package org.aksw.r2rml.jena.plugin;

import org.aksw.jena_sparql_api.sparql.ext.init.InitJenaSparqlApiSparqlExtensions;
import org.apache.jena.system.JenaSubsystemLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitJenaPluginR2rml implements JenaSubsystemLifecycle {
	private static final Logger logger = LoggerFactory.getLogger(InitJenaSparqlApiSparqlExtensions.class);

	public void start() {
		JenaPluginR2rml.init();
	}

	@Override
	public void stop() {
	}
}
