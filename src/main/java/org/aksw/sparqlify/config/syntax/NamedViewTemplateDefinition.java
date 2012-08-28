package org.aksw.sparqlify.config.syntax;

public class NamedViewTemplateDefinition
	extends ViewTemplateDefinition
{
	private String name;
	
	public NamedViewTemplateDefinition(String name, ViewTemplateDefinition viewTemplateDefinition) {
		super(viewTemplateDefinition.getConstructTemplate(), viewTemplateDefinition.getVarBindings());
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
