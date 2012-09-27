package org.aksw.sparqlify.core.domain;


import com.hp.hpl.jena.sparql.core.QuadPattern;


/**
 * A view definition is comprised of
 * - A name
 * - A template (a set of quad patterns)
 * - A mapping
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ViewDefinition {
	private String name;
	
	// Note: all quads in the template must be composed of variables only
	// Constants and expressions are associated to a variable in the mapping
	// object.
	private QuadPattern template;
	private Mapping mapping;
	
	// The source can point to an arbitrary object from
	// which this view definition was derived.
	// Mainly intended for pointing back to to the syntactic
	// construct this view definition was created from in order to be 
	// able to provide better feedback to the user if problems are
	// encountered.
	// 
	// (source can e.g. be an object representing a Sparqlify-ML or R2R-ML
	// definition).
	private Object source;

	private ViewDefinition(String name, QuadPattern template, Mapping mapping, Object source)
	{
		this.name = name;
		this.template = template;
		this.mapping = mapping;
		this.source = source;
	}

	
	public String getName() {
		return name;
	}


	public QuadPattern getTemplate() {
		return template;
	}


	public Mapping getMapping() {
		return mapping;
	}


	public Object getSource() {
		return source;
	}
}
