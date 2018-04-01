package org.aksw.sparqlify.config.lang;

import java.util.Collection;

import org.aksw.obda.jena.domain.impl.ViewDefinition;
import org.aksw.sparqlify.config.syntax.Config;

public class ViewDefinitionParser {
	protected ConfigParser parser;

	public ViewDefinitionParser() {
		this(new ConfigParser());
	}

	public ViewDefinitionParser(ConfigParser parser) {
		super();
		this.parser = parser;
	}

	public ViewDefinition parse(String str) {
		Config config = parser.parse(str);
		ViewDefinition result = expectOne(config);
		return result;
	}

	public static ViewDefinition expectOne(Config config) {
		Collection<ViewDefinition> viewDefs = config.getViewDefinitions();
		if(viewDefs.size() != 1) {
			throw new RuntimeException("Exactly 1 view definition expected, but got " + viewDefs.size());
		}

		ViewDefinition result = viewDefs.iterator().next();
		return result;
	}
	
}
