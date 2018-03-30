package org.aksw.r2rml.api;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface TriplesMap
	extends Resource
{
	SubjectMap getSubjectMap();
	TriplesMap setSubjectMap(SubjectMap subjectMap);
	
	
	Set<PredicateObjectMap> getPredicateObjectMaps();

	LogicalTable getLogicalTable();
	TriplesMap setLogicalTable(LogicalTable logicalTable);
}
