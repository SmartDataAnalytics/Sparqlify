package exp.cornercases;

import org.junit.Test;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class CornerCases {
	@Test
	public void testLangTag() {
		Model model = ModelFactory.createDefaultModel();
		model.add(model.createResource("http://s.org"), model.createProperty("http://p.org"), model.createLiteral("test"));
		model.add(model.createResource("http://s.org"), model.createProperty("http://p.org"), model.createLiteral("test", "de"));
		
		QueryExecution qe = QueryExecutionFactory.create("Construct { ?s ?p ?o . } { ?s ?p ?o . Filter(?o = 'test' && langMatches(lang(?o), 'de')). }", model);
		//QueryExecution qe = QueryExecutionFactory.create("Construct { ?s ?p ?o . } { ?s ?p ?o . Filter(?o = 'test'). }", model);
		Model result = qe.execConstruct();
		result.write(System.out, "N-TRIPLE");
	}
}
