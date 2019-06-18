package org.aksw.obda.jena.r2rml.plugin;

import org.apache.jena.sys.JenaSubsystemLifecycle;

public class InitJenaPluginR2rml implements JenaSubsystemLifecycle {
	//private static final Logger logger = LoggerFactory.getLogger(InitJenaPluginR2rml.class);

	public void start() {
		JenaPluginR2rml.init();
	}

	@Override
	public void stop() {
	}
}
