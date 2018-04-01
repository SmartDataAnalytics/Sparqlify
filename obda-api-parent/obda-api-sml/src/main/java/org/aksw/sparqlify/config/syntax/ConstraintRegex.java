package org.aksw.sparqlify.config.syntax;

import org.aksw.obda.domain.api.Constraint;
import org.apache.jena.sparql.core.Var;

import com.karneim.util.collection.regex.PatternPro;

public class ConstraintRegex
implements Constraint
{	
private Var var;
private String attribute; // type, value, datatype, language
private PatternPro pattern;

public ConstraintRegex(Var var, String attribute, String pattern) {
	super();
	this.var = var;
	this.attribute = attribute;
	this.pattern = new PatternPro(pattern);
}
}