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
	private List<NamedViewTemplateDefinition> definitions = new ArrayList<NamedViewTemplateDefinition>();

	public List<NamedViewTemplateDefinition> getDefinitions() {
		return definitions;
	}

	public void setViewDefinitions(List<NamedViewTemplateDefinition> definitions) {
		this.definitions = definitions;
	}
}
