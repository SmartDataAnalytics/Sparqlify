/**
 * 
 */
package org.aksw.obda.jena.r2rml.domain.impl;

import java.util.Set;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.obda.jena.r2rml.domain.api.LogicalTable;
import org.aksw.obda.jena.r2rml.domain.api.PredicateObjectMap;
import org.aksw.obda.jena.r2rml.domain.api.SubjectMap;
import org.aksw.obda.jena.r2rml.domain.api.TriplesMap;
import org.aksw.obda.jena.r2rml.vocab.RR;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class TriplesMapImpl
	extends AbstractMappingComponent
	implements TriplesMap
{
	public TriplesMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	@Override
	public SubjectMap getSubjectMap() {
		SubjectMap result = ResourceUtils.getPropertyValue(this, RR.subjectMap, SubjectMap.class);
		
		return result;
	}
	
	@Override
	public TriplesMap setSubjectMap(SubjectMap subjectMap) {
		ResourceUtils.setProperty(this, RR.subjectMap, subjectMap);
		return this;
	}

	@Override
	public Set<PredicateObjectMap> getPredicateObjectMaps() {
		Set<PredicateObjectMap> result = new SetFromPropertyValues<>(this, RR.predicateObjectMap, PredicateObjectMap.class);
		return result;
	}
	

	@Override
	public TriplesMapImpl setLogicalTable(LogicalTable logicalTable) {
		removeAll(RR.logicalTable);
		addProperty(RR.logicalTable, logicalTable);
		return this;
	}
	
	public LogicalTable getLogicalTable() {
		LogicalTable result = ResourceUtils.getPropertyValue(this, RR.logicalTable, LogicalTable.class);
		
		return result;
	}
}