package org.aksw.r2rml.jena.plugin;

import org.aksw.r2rml.jena.domain.api.GraphMap;
import org.aksw.r2rml.jena.domain.api.LogicalTable;
import org.aksw.r2rml.jena.domain.api.ObjectMap;
import org.aksw.r2rml.jena.domain.api.PredicateMap;
import org.aksw.r2rml.jena.domain.api.PredicateObjectMap;
import org.aksw.r2rml.jena.domain.api.SubjectMap;
import org.aksw.r2rml.jena.domain.api.TermMap;
import org.aksw.r2rml.jena.domain.api.TriplesMap;
import org.aksw.r2rml.jena.domain.impl.GraphMapImpl;
import org.aksw.r2rml.jena.domain.impl.LogicalTableImpl;
import org.aksw.r2rml.jena.domain.impl.ObjectMapImpl;
import org.aksw.r2rml.jena.domain.impl.PredicateMapImpl;
import org.aksw.r2rml.jena.domain.impl.PredicateObjectMapImpl;
import org.aksw.r2rml.jena.domain.impl.SubjectMapImpl;
import org.aksw.r2rml.jena.domain.impl.TermMapImpl;
import org.aksw.r2rml.jena.domain.impl.TriplesMapImpl;
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
