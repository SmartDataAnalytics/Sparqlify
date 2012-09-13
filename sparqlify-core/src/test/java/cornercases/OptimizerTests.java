package cornercases;

import org.aksw.sparqlify.compile.sparql.SqlExprOptimizer;
import org.junit.Test;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class OptimizerTests {
	
	
	/**
	 * This test case was created for the following sparqlify config:
	 *
	 * Problems were, that equals-concat returned non-optimal results, and
	 * the satisfiability check would not detect E_LogicalAnd(E_Equals(?x = ?y), false)
	 * 
	 *
Create View view_way_to_nodes As
	Construct {
		?w lgdo:hasNodes ?wn .
	}
	With 
		?w = spy:uri(concat('http://linkedgeodata.org/resource/way/', ?way_id))
		?wn = spy:uri(concat('http://linkedgeodata.org/resource/waynodelist/', ?way_id, 0))  
	From
		way_nodes;

Create View view_way_nodes_list_nil_test As
	Construct {
		?x rdf:first ?y .
	}
	With
		?x = spy:uri(concat('http://linkedgeodata.org/resource/waynodelist/', ?way_id, ?first_sequence_id))
		?y = spy:uri(concat('http://linkedgeodata.org/resource/node/', ?first_node_id))
	From
		[[SELECT * FROM lgd_way_nodes WHERE rest_sequence_id IS NULL]];

	 * 
	 */
	@Test
	public void equalsConcat() {

		Expr expr = ExprUtils.parse("<http://aksw.org/sparqlify/rdfTerm>('1'^^xsd:decimal, concat('http://linkedgeodata.org/resource/waynodelist/', ?way_id, '0'), '', '') = <http://aksw.org/sparqlify/rdfTerm>('1'^^xsd:decimal, concat('http://linkedgeodata.org/resource/way/', ?a_2_way_id), '', '')");
		
		Expr optimized = SqlExprOptimizer.optimizeMM(expr);					

		System.out.println(optimized);
			
			
	}
	
	//<http://aksw.org/sparqlify/rdfTerm>("1"^^xsd:decimal, concat("http://linkedgeodata.org/resource/waynodelist/", ?way_id, "0"), "", "") = <http://aksw.org/sparqlify/rdfTerm>("1"^^xsd:decimal, concat("http://linkedgeodata.org/resource/way/", ?a_2_way_id), "", "") )
	
}
