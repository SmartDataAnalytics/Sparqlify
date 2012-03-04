package org.aksw.sparqlify.core;

import org.aksw.sparqlify.config.lang.RegexConstraint;

import com.karneim.util.collection.regex.PatternPro;

public class RdfTermPattern 
	extends RdfTerm<PatternPro>
{
	/*
	public RdfTermPattern(RdfTermPattern other) {
		this.lexicalValue = other.lexicalValue;
		this.datatype = other.datatype;
	}*/
	public RdfTermPattern() {
	}
	
	public RdfTermPattern(PatternPro value) {
		super(value);
	}

	public RdfTermPattern(RdfTermPattern other) {
		super(other);
	}

	public RdfTermPattern(PatternPro value, PatternPro datatype) {
		super(null, value, null, datatype);
	}


	public PatternPro getDatatype() {
		return datatype;
	}


	public void setDatatype(PatternPro datatype) {
		this.datatype = datatype;
	}
	
	
	public static RdfTermPattern intersect(RdfTermPattern a, RdfTermPattern b) {
		return new RdfTermPattern(
				RegexConstraint.intersect(a.getValue(), b.getValue()),
				RegexConstraint.intersect(a.getDatatype(), b.getDatatype()));
	}
	
	
	public boolean isSatisfiable() {
		return 
				RegexConstraint.isSatisfiable(value) &&
				RegexConstraint.isSatisfiable(datatype);
	}
}

