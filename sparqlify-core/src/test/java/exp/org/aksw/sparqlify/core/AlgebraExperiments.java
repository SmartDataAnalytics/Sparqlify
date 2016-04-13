package exp.org.aksw.sparqlify.core;

import org.junit.Test;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;

public class AlgebraExperiments {

	@Test
	public void nestedOptionals() {
		Query query = new Query();
		QueryFactory.parse(query, "Prefix ex:<http://ex.org/> Select * { ?s a ?t . Optional { ?s ex:addr ?a . Optional { ?s ex:city ?c . Filter(?c = ?t)}}}", "http://ex.org/", Syntax.syntaxSPARQL_11);

		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);
		
		op = Algebra.optimize(op);
		System.out.println(op);
		//OpConditional
	}

	@Test
	public void parallelOptionals() {
		Query query = new Query();
		QueryFactory.parse(query, "Prefix ex:<http://ex.org/> Select * { ?s a ?t . Optional { ?s ex:addr ?a } . Optional { ?s ex:city ?c . Filter(?c = ?t)}}", "http://ex.org/", Syntax.syntaxSPARQL_11);

		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);
		
		System.out.println(op);
		
		//Algebra.optimize(op)
	}

}

