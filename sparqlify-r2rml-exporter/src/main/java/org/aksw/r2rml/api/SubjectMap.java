package org.aksw.r2rml.api;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface SubjectMap
	extends TermMap
{	
	/**
	 * Return a set view (never null) of resources specified via rr:class
	 * 
	 * @return
	 */
	Set<Resource> getTypes();
}
