package org.aksw.sparqlify.sparqlview;

import org.aksw.commons.sparql.api.http.QueryExecutionFactoryHttp;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorRestructify;
import org.aksw.sparqlify.core.cast.NewWorldTest;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.util.SparqlifyUtils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;


public class SparqlViewMain {
	public static void main(String[] args) {
		//SparqlViewSystem system = new SparqlViewSystem();
		
		//system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ex:<http://ex.org> Construct { ?s a ex:BigProject . ex:BigProject a ex:Type . } { ?s ex:funding ?o . Filter(?o > 1000) . }", Syntax.syntaxSPARQL_11)));
		//system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ex:<http://ex.org> Construct { ?p a ex:Facet . ?l ?p ?r . } { ?l ?p ?r . Filter(?p = <http://hasBeneficiary>) . }", Syntax.syntaxSPARQL_11)));
		
		
		//system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Construct { ?t a ft:Facet . } { ?s a ?t }", Syntax.syntaxSPARQL_11)));
		
		//system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Construct { ?s a ?t . } { ?s a ?t . }", Syntax.syntaxSPARQL_11)));
		//system.addView(SparqlView.create("MyView", QueryFactory.create("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Construct { ?s a ft:LabeledThing . } { ?s <"  + RDFS.label + "> ?x }", Syntax.syntaxSPARQL_11)));
		
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(typeSystem);
		CandidateViewSelector<SparqlView> candidateViewSelector = new CandidateViewSelectorRestructify(); 

		
		candidateViewSelector.addView(SparqlView.create("MyView", QueryFactory.create("Construct { ?s ?p ?o . } { Graph ?g { ?s ?p ?o } Filter(?g != <http://fp7-pp.publicdata.eu/> ) }", Syntax.syntaxSPARQL_11)));
		//system.addView(SparqlView.create("MyView", QueryFactory.create("Construct { ?s ?p ?o . } { Graph ?g { ?s ?p ?o } Filter(?g != <http://ns.ontowiki.net/SysBase/> ) }", Syntax.syntaxSPARQL_11)));
		
		QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp("http://localhost:8810/sparql");
		//QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp("http://leipzig-data.de:8890/sparql");
		QueryExecutionFactorySparqlView sv = new QueryExecutionFactorySparqlView(qef, candidateViewSelector, Dialect.VIRTUOSO);
		
		//QueryExecution qe = sv.createQueryExecution("Prefix ft:<http://fintrans.publicdata.eu/ec/ontology/> Select Distinct ?t { ?s a ?t . }");
		QueryExecution qe = sv.createQueryExecution("select distinct ?g { graph ?g { ?s ?p ?o }}");

		ResultSet rs = qe.execSelect();
		ResultSetFormatter.out(System.out, rs);
		
	}
}
