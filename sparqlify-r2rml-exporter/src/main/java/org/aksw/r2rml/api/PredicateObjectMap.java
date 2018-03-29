package org.aksw.r2rml.api;

import java.util.Set;

import org.apache.jena.rdf.model.Resource;

public interface PredicateObjectMap
	extends Resource
{
	Set<Resource> getPredicates();
	Set<PredicateMap> getPredicateMaps();
	Set<ObjectMap> getObjectMaps();

//	Resource getPredicate();
//	PredicateObjectMap setPredicate(Resource predicate);
//
//	TermMap getPredicateMap();
//	PredicateObjectMap setPredicateMap(TermMap termMap);
//	
//	TermMap getObjectMap();
//	PredicateObjectMap setObjectMap(TermMap termMap);
}
