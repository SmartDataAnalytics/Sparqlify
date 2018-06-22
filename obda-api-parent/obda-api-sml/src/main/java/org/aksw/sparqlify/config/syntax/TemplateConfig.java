package org.aksw.sparqlify.config.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

/**
 * A sparqlify configuration. Currently simple a list of view definitions.
 * 
 * @author raven
 * 
 */
public class TemplateConfig {
	private PrefixMapping prefixMapping = new PrefixMappingImpl();
	private List<NamedViewTemplateDefinition> definitions = new ArrayList<NamedViewTemplateDefinition>();

	public PrefixMapping getPrefixMapping() {
		return prefixMapping;
	}

	public List<NamedViewTemplateDefinition> getDefinitions() {
		return definitions;
	}

	public void setViewDefinitions(List<NamedViewTemplateDefinition> definitions) {
		this.definitions = definitions;
	}
}
