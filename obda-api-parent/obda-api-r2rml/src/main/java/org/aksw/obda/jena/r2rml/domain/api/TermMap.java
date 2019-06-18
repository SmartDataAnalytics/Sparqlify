package org.aksw.obda.jena.r2rml.domain.api;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface TermMap
	extends MappingComponent
{
	Resource getTermType();
	TermMap setTermType(Resource termType);

	String getColumn();
	TermMap setColumn(String columnName);
	
	String getLanguage();
	TermMap setLanguage(String language);
	
	Resource getDatatype();
	TermMap setDatatype(Resource datatype);
	
	RDFNode getConstant();
	TermMap setConstant(RDFNode constant);

	String getTemplate();
	TermMap setTemplate(String template);
}
