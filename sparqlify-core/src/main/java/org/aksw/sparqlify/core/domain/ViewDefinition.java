package org.aksw.sparqlify.core.domain;


import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;


/**
 * A view definition is comprised of
 * - A name
 * - A template (a set of quad patterns)
 * - A mapping
 * - A set of references to variables declared in other view definitions.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ViewDefinition {
	private String name;
	
	// Note: all quads in the template must (should?) be composed of variables only
	// Constants and expressions are associated to a variable in the mapping
	// object.
	private QuadPattern template;
	private Mapping mapping;
	
	// References to variables declaced in other views. Useful for efficient
	// mapping table handling, as self join elimination can be applied.
	// Corresponds to R2RML's rr:join.
	private Map<Var, VarReference> varReferences = new HashMap<Var, VarReference>();

	
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

	private ViewDefinition(String name, QuadPattern template, Map<Var, VarReference> varReferences, Mapping mapping, Object source)
	{
		this.name = name;
		this.template = template;
		this.mapping = mapping;
		this.varReferences = varReferences;
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

	
	public Map<Var, VarReference> getVarReferences() {
		return varReferences;
	}

	public Object getSource() {
		return source;
	}
}
