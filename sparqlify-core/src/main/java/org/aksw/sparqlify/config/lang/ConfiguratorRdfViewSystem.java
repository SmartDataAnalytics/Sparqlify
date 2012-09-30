package org.aksw.sparqlify.config.lang;

import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.RdfViewSystem;
import org.aksw.sparqlify.validation.Validation;
import org.slf4j.Logger;

public class ConfiguratorRdfViewSystem {
	
	
	public static void configure(Config config, RdfViewSystem system, Logger logger) {

		//List<RdfView> virtualGraphs = new ArrayList<RdfView>();
		for(ViewDefinition item : config.getViewDefinitions()) {
			RdfView virtualGraph = RdfView.create(item);
			
			if(logger != null) {
				Validation.validateView(virtualGraph, logger);
			}
			
			system.addView(virtualGraph);
		}
	}
}
