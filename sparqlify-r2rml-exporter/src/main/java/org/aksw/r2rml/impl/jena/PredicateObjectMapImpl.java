/**
 * 
 */
package org.aksw.r2rml.impl.jena;

import java.util.Set;

import org.aksw.r2rml.api.ObjectMap;
import org.aksw.r2rml.api.PredicateMap;
import org.aksw.r2rml.api.PredicateObjectMap;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * @author sherif
 * 
 */
public class PredicateObjectMapImpl
	extends AbstractR2rmlResourceImpl
	implements PredicateObjectMap
{
	public PredicateObjectMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public Set<Resource> getPredicates() {
		Set<Resource> result = new SetFromResourceAndProperty<>(this, RR.predicate, Resource.class);
		return result;
	}

	@Override
	public Set<PredicateMap> getPredicateMaps() {
		Set<PredicateMap> result = new SetFromResourceAndProperty<>(this, RR.predicateMap, PredicateMap.class);
		return result;
	}

	@Override
	public Set<ObjectMap> getObjectMaps() {
		Set<ObjectMap> result = new SetFromResourceAndProperty<>(this, RR.objectMap, ObjectMap.class);
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
