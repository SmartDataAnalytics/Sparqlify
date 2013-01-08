package org.aksw.sparqlify.config.v0_2.bridge;

import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.validation.Validation;
import org.slf4j.Logger;

public class ConfiguratorCandidateSelector {
		

	public static void configure(Config config, SyntaxBridge bridge, CandidateViewSelector candidateSelector, Logger logger) {

		for(ViewDefinition item : config.getViewDefinitions()) {
			org.aksw.sparqlify.core.domain.input.ViewDefinition virtualGraph = bridge.create(item);
			
			if(logger != null) {
				Validation.validateView(virtualGraph, logger);
			}
			
			candidateSelector.addView(virtualGraph);
		}
	}

}
