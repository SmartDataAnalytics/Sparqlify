package org.aksw.sparqlify.demos;

import java.sql.Connection;
import java.sql.Statement;
import java.util.TreeSet;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;
import org.aksw.sparqlify.compile.sparql.SqlGenerator;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.ResultSetFactory;
import org.aksw.sparqlify.core.jena.functions.RdfTerm;
import org.aksw.sparqlify.views.transform.ViewRewriter;
import org.apache.log4j.PropertyConfigurator;
import org.postgresql.ds.PGSimpleDataSource;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;


public class Demo {

	public static void main(String[] args) throws Exception {

		PropertyConfigurator.configure("log4j.properties");
		
		
		FunctionRegistry.get().put("http://aksw.org/beef/rdfTerm", RdfTerm.class);

		PGSimpleDataSource dataSource = new PGSimpleDataSource();
		
		dataSource.setDatabaseName("test");
		dataSource.setUser("postgres");
		dataSource.setPassword("postgres");
		dataSource.setServerName("localhost");
		dataSource.setPortNumber(5432);

		Connection conn = dataSource.getConnection();
		
		RdfViewSystemOld system = new RdfViewSystemOld();

		

		
		
		//system.addView(RdfView.create("{ ?s geo:religion ?r. ?x geo:subject ?s . ?x geo:predicate geo:religion . ?x geo:object ?r . ?x geo:contributor ?c .  } with ?s = beef:uri(concat('http://person/', ?person_id)); ?r = beef:uri(concat('http://rel/', ?xname)); ?x = beef:uri(concat('http://religion/reifier/', ?person_id)); ?c = beef:plainLiteral(?contributor, 'de'); select person_id, xname, contributor from religion"));
		//system.addView(RdfView.create("{ ?p geo:firstname ?f. ?p geo:lastname ?l . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));
		//system.addView(RdfView.create("{ ?x geo:id ?i . } with ?x = beef:uri(concat('http://person/', ?id)) ; ?i = beef:typedLiteral(?id, 'http://integer.org'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));
		
		system.addView(RdfView.create("{ geo:a geo:b geo:c . } with select 1"));
		system.addView(RdfView.create("{ geo:a geo:b geo:j . } with select 1"));
		
		
		system.loadDatatypes(conn);
		Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?a ?b ?c .}");
		//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?a ?b ?c . Filter(?c > 3 && ?c < 5) .}");
		//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s geo:religion ?r. ?x geo:subject ?s .} limit 3");

		
		
		
		
		
		//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?a ?b ?c . Filter(?c < 3) .}");

		Op view = system.getApplicableViews(query);
		
	
		//
		ViewRewriter sqlRewriter = new ViewRewriter();
		SqlNodeOld sqlNode = sqlRewriter.rewriteMM(view);
		
		System.out.println("Final sparql var mapping = " + sqlNode.getSparqlVarToExprs());
		
		SqlGenerator sqlGenerator = new SqlGenerator();
		String sqlQuery = sqlGenerator.generateMM(sqlNode);
		System.out.println(sqlQuery);

		Statement stmt = conn.createStatement();
		ResultSet rs = ResultSetFactory.create(stmt, sqlQuery, sqlNode.getSparqlVarToExprs(), null);
		SqlExpr x;
		
		
		System.out.println("Result");
		System.out.println("--------------------------------------");
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			
			//System.out.println(qs.get("s") + "\t" + qs.get("p") + "\t" + qs.get("o"));

			for(String a : new TreeSet<String>(rs.getResultVars())) {
				RDFNode node  = qs.get(a);
				System.out.print(a + ":" + node + "\t");
			}
			System.out.println();
			
		}
		
		
		conn.close();
	}
}


/*
TODO Add to test suite: Missing languate tags:
For some reason ?o = RdfTerm(1 ...) rather than some var
system.addView(RdfView.create("{ ?v1s geo:denomination ?v1o . } with ?v1s = beef:uri(concat('http://entity/', ?id)) ; ?v1o = beef:uri(concat('http://denid/', ?name)); select id, name from denomination"));

system.addView(RdfView.create("{ ?v3s geo:religion ?v3r. } with ?v3s = beef:uri(concat('http://entity/', ?id)) ; ?v3r = beef:uri(concat('http://rel/', ?name)); select id, name from religion"));

system.addView(RdfView.create("{ ?p geo:firstname ?f. ?p geo:lastname ?l . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?language); select id, firstname, lastname, language from person"));

Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s ?p ?o . Filter(regex(?p, 'name')) .}");
*/

// ISSUE: Joins don't work - there is a problem is alias assignment

//system.addView(RdfView.create("{ ?v1s geo:denomination ?v1o . } with ?v1s = beef:uri(concat('http://person/', ?id)) ; ?v1o = beef:uri(concat('http://denid/', ?xname)); select id, xname from denomination"));

//system.addView(RdfView.create("{ ?p a geo:Person . ?p geo:firstname ?f. ?p geo:lastname ?l . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));
//system.addView(RdfView.create("{ ?s geo:religion ?r. ?x geo:subject ?s . ?x geo:predicate geo:religion . ?x geo:object ?r . ?x geo:contributor ?c .  } with ?s = beef:uri(concat('http://person/', ?id)); ?r = beef:uri(concat('http://rel/', ?xname)); ?x = beef:uri(concat('http://religion/reifier/', ?id)); ?c = beef:plainLiteral(?contributor, 'de'); select id, xname, contributor from religion"));
//system.addView(RdfView.create("{ ?s geo:religion ?r. ?x geo:subject ?s . ?x geo:predicate geo:religion . ?x geo:object ?r . ?x geo:contributor ?c .  } with ?s = beef:uri(concat('http://person/', ?id)); ?r = beef:uri(concat('http://rel/', ?xname)); ?x = beef:uri(concat('http://religion/reifier/', ?id)); ?c = beef:plainLiteral(?contributor, 'de'); select id, xname, contributor from religion"));

//system.addView(RdfView.create("{ ?p a geo:Person . ?p geo:firstname ?f. ?p geo:lastname ?l . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));
//system.addView(RdfView.create("{ ?p a geo:Person . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));





//system.addView(RdfView.create("{ ?p a geo:Person . ?p geo:firstname ?f. ?p geo:lastname ?l . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));
//system.addView(RdfView.create("{ ?p geo:firstname ?f . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));



//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s ?p ?o . Filter(regex(lang(?o), 'r')) .}");
//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s ?p ?o . Filter(langMatches(lang(?o), 'fr')) .}");
//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s geo:firstname ?o . ?s geo:lastname ?z .}");



//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?x geo:subject ?s . ?x geo:predicate ?p . ?x geo:object ?o . ?x geo:contributor ?c .}");
//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?x geo:subject ?s . ?x geo:predicate ?p . ?x geo:contributor ?c .}");
//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s geo:contributor ?o .}");

//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s a geo:Person . Optional { ?s geo:religion ?o .} Optional { ?s geo:denomination ?p } . }");
//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { { ?x a ?r . } union { ?a geo:religion ?b . } } limit 1 offset 1");

// ISSUE: Type mix


//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?a ?b ?c . ?d ?b ?c . Filter(?c < 1) .}");
//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?a ?b ?c . ?d ?e ?f .}");

// Example with datatypes
//system.addView(RdfView.create("{ ?p geo:firstname ?f. ?p geo:lastname ?l . } with ?p = beef:uri(concat('http://person/', ?id)) ; ?f = beef:plainLiteral(?firstname, 'fr'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));
//system.addView(RdfView.create("{ ?x geo:id ?i . } with ?x = beef:uri(concat('http://person/', ?id)) ; ?i = beef:typedLiteral(?id, 'http://integer.org'); ?l = beef:plainLiteral(?lastname, ?lng); select id, firstname, lastname, lng from person"));


