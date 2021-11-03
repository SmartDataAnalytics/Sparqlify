package org.aksw.sparqlify.util;

import java.util.List;

import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;

public class ViewDefinitionFactory {
	
	private ConfigParser parser = new ConfigParser();
	private SyntaxBridge syntaxBridge;
	
	public ViewDefinitionFactory(ConfigParser parser, SyntaxBridge syntaxBridge) {
		this.parser = parser;
		this.syntaxBridge = syntaxBridge;
	}
	
	public ViewDefinition create(String viewDefStr) {
	

		Config config;
//		try {
			config = parser.parse(viewDefStr, null);
//		} catch (Runt e) {
//			throw new RuntimeException(e);
//		}

		
		List<org.aksw.obda.jena.domain.impl.ViewDefinition> syntacticViewDefs = config.getViewDefinitions();
	
		org.aksw.obda.jena.domain.impl.ViewDefinition syntacticViewDef = syntacticViewDefs.get(0);
		
		ViewDefinition result = syntaxBridge.create(syntacticViewDef);


		return result;
	}
	
	/**
	 * Convenience method for accessing the underlying datatype system
	 * 
	 * @return
	 */
	public TypeSystem getDatatypeSystem() {
		return syntaxBridge.getSchemaProvider().getDatatypeSystem();
	}
}
