/**
 * 
 */
package org.aksw.obda.jena.r2rml.domain.impl;

import java.util.Set;

import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.obda.jena.r2rml.domain.api.GraphMap;
import org.aksw.obda.jena.r2rml.domain.api.ObjectMap;
import org.aksw.obda.jena.r2rml.domain.api.PredicateMap;
import org.aksw.obda.jena.r2rml.domain.api.PredicateObjectMap;
import org.aksw.obda.jena.r2rml.vocab.RR;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * @author sherif
 * 
 */
public class PredicateObjectMapImpl
	extends AbstractMappingComponent
	implements PredicateObjectMap
{
	public PredicateObjectMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public Set<Resource> getPredicates() {
		Set<Resource> result = new SetFromPropertyValues<>(this, RR.predicate, Resource.class);
		return result;
	}

	@Override
	public Set<PredicateMap> getPredicateMaps() {
		Set<PredicateMap> result = new SetFromPropertyValues<>(this, RR.predicateMap, PredicateMap.class);
		return result;
	}

	@Override
	public Set<ObjectMap> getObjectMaps() {
		Set<ObjectMap> result = new SetFromPropertyValues<>(this, RR.objectMap, ObjectMap.class);
		return result;
	}
	
	@Override
	public Set<GraphMap> getGraphMaps() {
		Set<GraphMap> result = new SetFromPropertyValues<>(this, RR.graphMap, GraphMap.class);
		return result;
	}


/*
	@Override
	public Resource getPredicate() {
		Resource result = getPropertyResourceValue(RR.predicate);
		return result;
	}

	@Override
	public PredicateObjectMap setPredicate(Resource predicate) {
		setProperty(this, RR.predicate, predicate);
		return this;
	}

	@Override
	public TermMap getPredicateMap() {
		TermMap result = getObjectAs(this, RR.predicateMap, TermMap.class).orElse(null);
		return result;
	}

	@Override
	public PredicateObjectMap setPredicateMap(TermMap termMap) {
		setProperty(this, RR.predicateMap, termMap);
		return this;
	}

	@Override
	public TermMap getObjectMap() {
		TermMap result = getObjectAs(this, RR.objectMap, TermMap.class).orElse(null);
		return result;
	}

	@Override
	public PredicateObjectMap setObjectMap(TermMap termMap) {
		setProperty(this, RR.objectMap, termMap);
		return this;
	}
*/

}
