package exp.sparql;

import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

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
