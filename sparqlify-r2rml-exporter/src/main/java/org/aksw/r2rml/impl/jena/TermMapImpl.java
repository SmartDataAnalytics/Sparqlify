package org.aksw.r2rml.impl.jena;

import org.aksw.r2rml.api.TermMap;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


public class TermMapImpl
	extends AbstractR2rmlResourceImpl
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
		RDFNode result = getObjectAs(this, RR.datatype, RDFNode.class).orElse(null);
		return result;
	}
	
	public TermMap setConstant(RDFNode constant) {
		setProperty(this, RR.datatype, constant);
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
