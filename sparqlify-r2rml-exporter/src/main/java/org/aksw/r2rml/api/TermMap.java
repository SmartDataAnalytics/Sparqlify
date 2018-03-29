package org.aksw.r2rml.api;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface TermMap
	extends Resource
{
	Resource getTermType();
	TermMap setTermType(Resource termType);

	String getColumn();
	TermMap setColumn(String columnName);
	
	String getLanguage();
	TermMap setLanguage(String template);
	
	Resource getDatatype();
	TermMap setDatatype(Resource datatype);
	
	RDFNode getConstant();
	TermMap setConstant(RDFNode constant);

	String getTemplate();
	TermMap setTemplate(String template);
}
