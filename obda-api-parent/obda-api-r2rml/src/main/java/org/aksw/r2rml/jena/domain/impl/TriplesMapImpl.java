/**
 * 
 */
package org.aksw.r2rml.jena.domain.impl;

import java.util.Set;

import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.vocab.RR;
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
		SubjectMap result = ResourceUtils.getPropertyValue(this, RR.subjectMap, SubjectMap.class).orElse(null);
		
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
		LogicalTable result = ResourceUtils.getPropertyValue(this, RR.logicalTable, LogicalTable.class).orElse(null);
		
		return result;
	}
}