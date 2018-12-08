package org.aksw.r2rml.jena.domain.impl;

import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
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
		Resource result = ResourceUtils.getPropertyValue(this, RR.termType, Resource.class);
		return result;
	}
	
	public TermMap setTermType(Resource termType) {
		ResourceUtils.setProperty(this, RR.termType, termType);
		return this;
	}

	public String getColumn() {
		String result = ResourceUtils.getLiteralPropertyValue(this, RR.column, String.class);
		return result;
	}
	
	public TermMap setColumn(String columnName) {
		ResourceUtils.setLiteralProperty(this, RR.column, columnName);
		return this;
	}

	
	public String getLanguage() {
		String result = ResourceUtils.getLiteralPropertyValue(this, RR.language, String.class);
		return result;
	}
	
	public TermMap setLanguage(String template) {
		ResourceUtils.setLiteralProperty(this, RR.language, template);
		return this;
	}

	public Resource getDatatype() {
		Resource result = ResourceUtils.getPropertyValue(this, RR.datatype, Resource.class);
		return result;
	}
	
	public TermMap setDatatype(Resource datatype) {
		ResourceUtils.setProperty(this, RR.datatype, datatype);
		return this;
	}

	public RDFNode getConstant() {
		RDFNode result = ResourceUtils.getPropertyValue(this, RR.constant, RDFNode.class);
		return result;
	}
	
	public TermMap setConstant(RDFNode constant) {
		ResourceUtils.setProperty(this, RR.constant, constant);
		return this;
	}
	
	
	public String getTemplate() {
		String result = ResourceUtils.getLiteralPropertyValue(this, RR.template, String.class);
		return result;
	}
	
	public TermMap setTemplate(String template) {
		ResourceUtils.setLiteralProperty(this, RR.template, template);
		return this;
	}

}
