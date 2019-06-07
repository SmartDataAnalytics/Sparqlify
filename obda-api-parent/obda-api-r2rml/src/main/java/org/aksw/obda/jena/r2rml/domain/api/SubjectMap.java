package org.aksw.obda.jena.r2rml.domain.api;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface SubjectMap
	extends TermMap
{	
//	GraphMap getGraphMap();
//	SubjectMap setGraphMap(GraphMap graphMap);

	Set<GraphMap> getGraphMaps();
	
	/**
	 * Return a set view (never null) of resources specified via rr:class
	 * 
	 * @return
	 */
	Set<Resource> getTypes();
}
