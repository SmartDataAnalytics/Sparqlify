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
public class ConstructConfig {
	private List<ConstructViewDefinition> viewDefinitions = new ArrayList<ConstructViewDefinition>();

	public List<ConstructViewDefinition> getViewDefinitions() {
		return viewDefinitions;
	}

	public void setViewDefinitions(List<ConstructViewDefinition> viewDefinitions) {
		this.viewDefinitions = viewDefinitions;
	}
}
