package org.aksw.sparqlify.demos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;
import org.aksw.sparqlify.compile.sparql.SqlGenerator;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.ResultSetFactory;
import org.aksw.sparqlify.core.jena.functions.RdfTerm;
import org.aksw.sparqlify.views.transform.ViewRewriter;
import org.apache.log4j.PropertyConfigurator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;


public class DemoWortschatz {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");

		
		FunctionRegistry.get().put("http://aksw.org/beef/rdfTerm", RdfTerm.class);

		String prefixes = "";
		prefixes += "Prefix wso:<http://aksw.org/wortschatz/ontology/> ";
		prefixes += "Prefix owl:<" + OWL.getURI() + "> ";
		prefixes += "Prefix rdfs:<" + RDFS.getURI() + "> ";
		
		String str = prefixes + "Select * { ?x wso:frequency ?f . ?x wso:sigma ?s . ?x owl:annotatedSource ?a . ?a rdfs:label ?l . filter(?f < 4 && ?s > 40 && ?s < 41) . } limit 100";
	
		str = "Select * { ?s ?p ?o . } limit 10";
		
		ResultSet rs = executeSelect(str);
		System.out.println(ResultSetFormatter.asText(rs));

	}
	
	public static ResultSet executeSelect(String str)
		throws Exception
	{
		Class.forName("com.mysql.jdbc.Driver").newInstance();

		 
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/wortschatz", "root", "mysql");

		
		RdfViewSystemOld system = new RdfViewSystemOld();

		// co_n
		system.addView(RdfView.create("{ ?a wso:coOccursDirectlyWith ?b . ?x owl:annotatedSource ?a . ?x owl:annotatedProperty wso:coOccursDirectlyWith . ?x owl:annotatedTarget ?b . ?x wso:frequency ?f . ?x wso:sigma ?s } with ?a = beef:uri(concat('http://aksw.org/wortschatz/word/', ?w1_id)); ?b = beef:uri(concat('http://aksw.org/wortschatz/word/', ?w2_id)); ?x = beef:uri(concat('http://aksw.org/wortschatz/co-occurence/direct/', ?w1_id, '/', ?w2_id)); ?f = beef:plainLiteral(?freq); ?s = beef:plainLiteral(?sig); co_n"));
		
		// co_s
		system.addView(RdfView.create("{ ?a wso:coOccursInSentenceWith ?b . ?x owl:annotatedSource ?a . ?x owl:annotatedProperty wso:coOccursInSentenceWith . ?x owl:annotatedTarget ?b . ?x wso:frequency ?f . ?x wso:sigma ?s } with ?a = beef:uri(concat('http://aksw.org/wortschatz/word/', ?w1_id)); ?b = beef:uri(concat('http://aksw.org/wortschatz/word/', ?w2_id)); ?x = beef:uri(concat('http://aksw.org/wortschatz/co-occurence/sentence/', ?w1_id, '/', ?w2_id)); ?f = beef:plainLiteral(?freq); ?s = beef:plainLiteral(?sig); co_s"));

		// inv_so
		system.addView(RdfView.create("{ ?a wso:source ?b . } with ?a = beef:uri(concat('http://aksw.org/wortschatz/sentence/', ?s_id)); ?b = beef:uri(concat('http://aksw.org/wortschatz/source/', ?so_id)); inv_so"));
		
		// inv_w
		system.addView(RdfView.create("{ ?a wso:partOf ?b . } with ?a = beef:uri(concat('http://aksw.org/wortschatz/word/', ?w_id)); ?b = beef:uri(concat('http://aksw.org/wortschatz/sentence/', ?s_id)); inv_w"));

		// sentences
		system.addView(RdfView.create("{ ?a rdfs:label ?b . } with ?a = beef:uri(concat('http://aksw.org/wortschatz/sentence/', ?s_id)); ?b = beef:plainLiteral(?sentence); sentences"));

		// words
		system.addView(RdfView.create("{ ?a rdfs:label ?b . ?a wso:frequency ?f . } with ?a = beef:uri(concat('http://aksw.org/wortschatz/word/', ?w_id)); ?b = beef:plainLiteral(?word); ?f = beef:plainLiteral(?freq); words"));
		
		// sources
		system.addView(RdfView.create("{ ?a wso:sourceUri ?b . ?a wso:date ?f . } with ?a = beef:uri(concat('http://aksw.org/wortschatz/source/', ?so_id)); ?b = beef:uri(concat(?source)); ?f = beef:plainLiteral(?date); sources"));
		
		
		//system.addView(RdfView.create("{ ?s geo:source ?o .  } with ?s = beef:uri(concat('http://aksw.org/wortschatz/source/', ?so_id)); ?o = beef:uri(?source); select so_id, source from sources"));

		//Query query = QueryFactory.create("Prefix geo:<http://ex.org/> Select * { ?s ?p ?o . Filter(?s = <http://aksw.org/wortschatz/source/4415>) }");

		
		
		system.loadDatatypes(conn);
		
		Query query = QueryFactory.create(str);
		

		//Query query = QueryFactory.create("Prefix wso:<http://aksw.org/wortschatz/ontology/> Select * { . } limit 100");
		
		
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
		
		return rs;
	}
	
		/*
		System.out.println(ResultSetFormatter.asText(rs));
		
		if(true) {
			return;
		}
		
		
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
	*/
}
