package org.aksw.sparqlify.config.lang;

import org.aksw.jena_sparql_api.views.SparqlView;
import org.aksw.sparqlify.config.syntax.ConstructConfig;
import org.aksw.sparqlify.config.syntax.ConstructViewDefinition;
import org.aksw.sparqlify.sparqlview.SparqlViewSystem;

public class ConfiguratorConstructViewSystem {
	public static void configure(ConstructConfig config, SparqlViewSystem system) {

		//List<RdfView> virtualGraphs = new ArrayList<RdfView>();
		for(ConstructViewDefinition item :config.getViewDefinitions()) {
			SparqlView virtualGraph = SparqlView.create(item.getName(), item.getQuery());
			
			system.addView(virtualGraph);
		}
	}
}
