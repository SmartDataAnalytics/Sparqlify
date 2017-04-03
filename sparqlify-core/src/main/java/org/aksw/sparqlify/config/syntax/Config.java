package org.aksw.sparqlify.config.syntax;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;

/**
 * A sparqlify configuration.
 * Currently simple a list of view definitions.
 * 
 * @author raven
 *
 */
public class Config {
	private PrefixMapping prefixMapping = new PrefixMappingImpl();
	private List<ViewDefinition> viewDefinitions = new ArrayList<ViewDefinition>();
	private List<FunctionDeclarationTemplate> functionDeclarations = new ArrayList<FunctionDeclarationTemplate>(); 
	
	public List<ViewDefinition> getViewDefinitions() {
		return viewDefinitions;
	}

	public PrefixMapping getPrefixMapping() 
	{
		return prefixMapping;
	}
	
	public void setViewDefinitions(List<ViewDefinition> viewDefinitions) {
		this.viewDefinitions = viewDefinitions;
	}

	public List<FunctionDeclarationTemplate> getFunctionDeclarations() {
		return functionDeclarations;
	}
	
	
	public void merge(Config other) {
	    // TODO Check for overwrites of the prefix mappings
	    prefixMapping.setNsPrefixes(other.prefixMapping);

	    viewDefinitions.addAll(other.viewDefinitions);
	    
	    functionDeclarations.addAll(other.functionDeclarations);
	}
}
