package org.aksw.sparqlify.core;

import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sdb.core.Gensym;


public class ColRelGenerator {
	private Generator columnNameGenerator;
	private Generator relationNameGenerator;
	
	
	public ColRelGenerator()
	{
		this(Gensym.create("c"), Gensym.create("r"));
	}
	
	public ColRelGenerator(Generator columnNameGenerator, Generator relationNameGenerator) {
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
