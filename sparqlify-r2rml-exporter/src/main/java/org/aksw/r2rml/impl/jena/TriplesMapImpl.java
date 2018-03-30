/**
 * 
 */
package org.aksw.r2rml.impl.jena;

import java.util.Set;

import org.aksw.r2rml.api.LogicalTable;
import org.aksw.r2rml.api.PredicateObjectMap;
import org.aksw.r2rml.api.SubjectMap;
import org.aksw.r2rml.api.TriplesMap;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class TriplesMapImpl
	extends AbstractR2rmlResourceImpl
	implements TriplesMap
{
	public TriplesMapImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	@Override
	public SubjectMap getSubjectMap() {
		SubjectMap result = getObjectAs(this, RR.subjectMap, SubjectMap.class).orElse(null);
		
		return result;
	}
	
	@Override
	public TriplesMap setSubjectMap(SubjectMap subjectMap) {
		setProperty(this, RR.subjectMap, subjectMap);
		return this;
	}

	@Override
	public Set<PredicateObjectMap> getPredicateObjectMaps() {
		Set<PredicateObjectMap> result = new SetFromResourceAndProperty<>(this, RR.predicateObjectMap, PredicateObjectMap.class);
		return result;
	}
	

	@Override
	public TriplesMapImpl setLogicalTable(LogicalTable logicalTable) {
		removeAll(RR.logicalTable);
		addProperty(RR.logicalTable, logicalTable);
		return this;
	}
	
	public LogicalTable getLogicalTable() {
		LogicalTable result = getObjectAs(this, RR.logicalTable, LogicalTable.class).orElse(null);
		
		return result;
	}
}