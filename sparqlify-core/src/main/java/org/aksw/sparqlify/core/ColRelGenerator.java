package org.aksw.sparqlify.core;

import org.aksw.commons.collections.generator.Generator;

public class ColRelGenerator {
	private Generator<String> columnNameGenerator;
	private Generator<String> relationNameGenerator;
	
	
	public ColRelGenerator()
	{
		this(Generator.create("c"), Generator.create("r"));
	}
	
	public ColRelGenerator(Generator<String> columnNameGenerator, Generator<String> relationNameGenerator) {
		this.columnNameGenerator = columnNameGenerator;
		this.relationNameGenerator = relationNameGenerator;
	}
	
	public Generator forRelation() {
		return relationNameGenerator;
	}
	
	public Generator forColumn() {
		return columnNameGenerator;
	}
	
	public String nextColumn() {
		return columnNameGenerator.next();
	}
	
	public String nextRelation() {
		return relationNameGenerator.next();
	}
}
