package org.aksw.sparqlify.config.syntax;

import java.util.ArrayList;
import java.util.List;

/**
 * A sparqlify configuration.
 * Currently simple a list of view definitions.
 * 
 * @author raven
 *
 */
public class TemplateConfig {
	private List<ViewTemplateDefinition> definitions = new ArrayList<ViewTemplateDefinition>();

	public List<ViewTemplateDefinition> getDefinitions() {
		return definitions;
	}

	public void setViewDefinitions(List<ViewTemplateDefinition> definitions) {
		this.definitions = definitions;
	}
}
