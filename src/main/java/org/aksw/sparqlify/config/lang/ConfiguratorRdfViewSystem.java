package org.aksw.sparqlify.config.lang;

import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.RdfViewSystem;

public class ConfiguratorRdfViewSystem {
	public static void configure(Config config, RdfViewSystem system) {

		//List<RdfView> virtualGraphs = new ArrayList<RdfView>();
		for(ViewDefinition item :config.getViewDefinitions()) {
			RdfView virtualGraph = RdfView.create(item);
			
			system.addView(virtualGraph);
		}
	}
}
