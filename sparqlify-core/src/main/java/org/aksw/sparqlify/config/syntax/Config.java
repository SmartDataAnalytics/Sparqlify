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
	
}
