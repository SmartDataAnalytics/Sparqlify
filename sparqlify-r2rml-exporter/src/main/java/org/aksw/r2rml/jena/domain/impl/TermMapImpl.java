package org.aksw.r2rml.jena.domain.impl;

import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.vocab.RR;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


public class TermMapImpl
	extends AbstractMappingComponent
	implements TermMap
{
	public TermMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	public Resource getTermType() {
		Resource result = getObjectAs(this, RR.termType, Resource.class).orElse(null);
		return result;
	}
	
	public TermMap setTermType(Resource termType) {
		setProperty(this, RR.termType, termType);
		return this;
	}

	public String getColumn() {
		String result = getLiteralValue(this, RR.column, Literal::getString).orElse(null);
		return result;
	}
	
	public TermMap setColumn(String columnName) {
		setLiteralValue(this, RR.column, columnName);
		return this;
	}

	
	public String getLanguage() {
		String result = getLiteralValue(this, RR.language, Literal::getString).orElse(null);
		return result;
	}
	
	public TermMap setLanguage(String template) {
		setLiteralValue(this, RR.language, template);
		return this;
	}

	public Resource getDatatype() {
		Resource result = getObjectAs(this, RR.datatype, Resource.class).orElse(null);
		return result;
	}
	
	public TermMap setDatatype(Resource datatype) {
		setProperty(this, RR.datatype, datatype);
		return this;
	}

	public RDFNode getConstant() {
		RDFNode result = getObjectAs(this, RR.constant, RDFNode.class).orElse(null);
		return result;
	}
	
	public TermMap setConstant(RDFNode constant) {
		setProperty(this, RR.constant, constant);
		return this;
	}
	
	
	public String getTemplate() {
		String result = getLiteralValue(this, RR.template, Literal::getString).orElse(null);
		return result;
	}
	
	public TermMap setTemplate(String template) {
		setLiteralValue(this, RR.template, template);
		return this;
	}

}
