package org.aksw.sparqlify.core;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional;

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

