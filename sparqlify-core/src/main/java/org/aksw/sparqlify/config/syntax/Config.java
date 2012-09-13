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
public class Config {
	private List<ViewDefinition> viewDefinitions = new ArrayList<ViewDefinition>();

	public List<ViewDefinition> getViewDefinitions() {
		return viewDefinitions;
	}

	public void setViewDefinitions(List<ViewDefinition> viewDefinitions) {
		this.viewDefinitions = viewDefinitions;
	}
}
