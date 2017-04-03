package exp.sparql;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class SparqlSemantics
{
	//@Test
	public void testInteger()
	{
		Model model = ModelFactory.createDefaultModel();
		model.add(RDF.type, RDFS.label, ResourceFactory.createPlainLiteral("4"));
		model.add(RDF.first, RDFS.label, ResourceFactory.createTypedLiteral(4));

		{
			ResultSet rs = QueryExecutionFactory.create("Select * {?s ?p ?o . Filter(?o = \"4\") . }", model).execSelect();
			while(rs.hasNext()) {
				System.out.println(rs.next());
			}
		}

		{
			ResultSet rs = QueryExecutionFactory.create("Select * {?s ?p ?o . ?x ?y ?z . Filter(?o = ?z) . }", model).execSelect();
			while(rs.hasNext()) {
				System.out.println(rs.next());
			}
		}
		
	}
	 
	 
}
