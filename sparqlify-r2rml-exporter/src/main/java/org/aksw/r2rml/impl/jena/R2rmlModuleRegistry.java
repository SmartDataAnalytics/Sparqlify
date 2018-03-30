package org.aksw.r2rml.impl.jena;

import org.aksw.r2rml.api.GraphMap;
import org.aksw.r2rml.api.LogicalTable;
import org.aksw.r2rml.api.ObjectMap;
import org.aksw.r2rml.api.PredicateMap;
import org.aksw.r2rml.api.PredicateObjectMap;
import org.aksw.r2rml.api.SubjectMap;
import org.aksw.r2rml.api.TermMap;
import org.aksw.r2rml.api.TriplesMap;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;

public class R2rmlModuleRegistry {
    
	static {
		R2rmlModuleRegistry.init(BuiltinPersonalities.model);
    }
	
	public static void test() {
		
	}
    
	public static void init(Personality<RDFNode> p) {
    	p.add(TriplesMap.class, new SimpleImplementation(TriplesMapImpl::new));
    	p.add(LogicalTable.class, new SimpleImplementation(LogicalTableImpl::new));
    	p.add(PredicateObjectMap.class, new SimpleImplementation(PredicateObjectMapImpl::new));
    	p.add(GraphMap.class, new SimpleImplementation(GraphMapImpl::new));
    	p.add(SubjectMap.class, new SimpleImplementation(SubjectMapImpl::new));
    	p.add(PredicateMap.class, new SimpleImplementation(PredicateMapImpl::new));
    	p.add(ObjectMap.class, new SimpleImplementation(ObjectMapImpl::new));
    	p.add(TermMap.class, new SimpleImplementation(TermMapImpl::new));
    }
}
