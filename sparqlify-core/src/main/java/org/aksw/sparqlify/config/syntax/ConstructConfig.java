package org.aksw.sparqlify.config.syntax;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

/**
 * A sparqlify configuration.
 * Currently simple a list of view definitions.
 * 
 * @author raven
 *
 */
public class ConstructConfig {

	private PrefixMapping prefixMapping = new PrefixMappingImpl();
	private List<ConstructViewDefinition> viewDefinitions = new ArrayList<ConstructViewDefinition>();

	public PrefixMapping getPrefixMapping() 
	{
		return prefixMapping;
	}

	public List<ConstructViewDefinition> getViewDefinitions() {
		return viewDefinitions;
	}

	public void setViewDefinitions(List<ConstructViewDefinition> viewDefinitions) {
		this.viewDefinitions = viewDefinitions;
	}
}
