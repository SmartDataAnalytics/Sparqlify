/**
 * 
 */
package org.aksw.obda.jena.r2rml.domain.impl;

import java.util.Set;

import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.obda.jena.r2rml.domain.api.GraphMap;
import org.aksw.obda.jena.r2rml.domain.api.SubjectMap;
import org.aksw.obda.jena.r2rml.vocab.RR;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class SubjectMapImpl
	extends TermMapImpl
	implements SubjectMap
{
	public SubjectMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public Set<Resource> getTypes() {
		Set<Resource> result = new SetFromPropertyValues<>(this, RR.rrClass, Resource.class);
		return result;
	}

	@Override
	public Set<GraphMap> getGraphMaps() {
		Set<GraphMap> result = new SetFromPropertyValues<>(this, RR.graphMap, GraphMap.class);
		return result;
	}
	
//	@Override
//	public GraphMap getGraphMap() {
//		GraphMap result = getObjectAs(this, RR.graphMap, GraphMap.class).orElse(null);
//		return result;
//	}

//	@Override
//	public SubjectMap setGraphMap(GraphMap graphMap) {
//		setProperty(this, RR.graphMap, graphMap);
//		return this;
//	}
}