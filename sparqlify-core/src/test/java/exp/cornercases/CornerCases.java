package exp.cornercases;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

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
