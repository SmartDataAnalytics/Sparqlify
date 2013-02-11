package exp.sparql;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import sparql.Equivalence;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;


public class EquivalenceTest
{
	@Test
	public void oeutest1x()
	{
		eval(
			"Select * { ?s ?p <http://uri> .  ?s ?p <http://uri2> . }",
			"Select * { ?s1 ?p <http://uri> .  ?s1 ?p <http://uri2> .  }",
			true
		);
	}

	
	@Test
	public void test1()
	{
		eval(
			"Select * { ?x <http://knows> ?y . ?y <http://knows> ?z . ?z <http://type> <http://Person> . ?x ?yy ?zz . }",
			"Select * { ?s <http://knows> ?o . ?o <http://knows> ?a . ?a <http://type> <http://Person> . ?s ?yy ?zz . }",
			true
		);
	}

	@Test
	public void test1shuffeled()
	{
		eval(
			"Select * { ?x <http://knows> ?y . ?y <http://knows> ?z . ?z <http://type> <http://Person> . ?x ?yy ?zz . }",
			"Select * { ?a <http://type> <http://Person> . ?o <http://knows> ?a . ?s ?yy ?zz . ?s <http://knows> ?o . }",
			true
		);
	}
	
	
	@Test
	public void simpleOptional1()
	{
		eval("Select * { ?a <http://knows> ?b . Optional { ?b <http://knows> ?c . } . }",
			 "Select * { ?x <http://knows> ?y . ?y <http://knows> ?z . }",
			 false
		);
	}
	
	@Test
	public void simpleOptional2()
	{
		eval("Select * { ?a <http://knows> ?b . Optional { ?b <http://knows> ?c . } . }",
			 "Select * { ?x <http://knows> ?y . Optional { ?y <http://knows> ?z . } . }",
			 true
		);
	}

	@Test
	public void simpleOptional3a()
	{
		eval("Select * { ?a <http://knows> ?b . Optional { ?b <http://knows> ?c . } . ?c <http://knows> ?d . }",
			 "Select * { ?x <http://knows> ?y . Optional { ?y <http://knows> ?z . } . ?z <http://knows> ?w . }",
			 true);
	}

	//@Test
	public void simpleOptional3b()
	{
		eval("Select * { ?a <http://knows> ?b . Optional { ?b <http://knows> ?c . } . ?c <http://knows> ?d . }",
			 "Select * { ?x <http://knows> ?y . ?y <http://knows> ?z . ?z <http://knows> ?w . }",
			 false
		);
	}

	@Test
	public void simpleOptional4()
	{
		eval("Select * { ?a <http://knows> ?b . Optional { ?b <http://knows> ?c . Optional { ?c <http://knows> ?d . Filter(?a = ?b) .} . } . }",
			 "Select * { ?x <http://knows> ?y . Optional { ?y <http://knows> ?z . Optional { ?z <http://knows> ?w . } . } . }",
			 true
		);
	}


	//@Test
	//@Ignore
	public void differentLengthA()
	{
		eval("Select * { ?a <http://knows> ?b . }",
			 "Select * { ?x <http://knows> ?y . ?s ?p ?o . }",
			 false
		);
	}

	@Test
	public void differentLengthB()
	{
		eval("Select * { ?a <http://knows> ?b . ?s ?p ?o . }",
			 "Select * { ?x <http://knows> ?y . }",
			 false
		);
	}

	
	public static void eval(String qa, String qb, boolean expectMapping)
	{	
		Query a = QueryFactory.create(qa);
		Query b = QueryFactory.create(qb);

		Set<Map<Node, Node>> isos = Equivalence.findIsomorphy(a, b);

		System.out.println("Mapping result: " + isos);
		
		// FIXME This is so far just an approximation:
		Assert.assertTrue(expectMapping == !isos.isEmpty()); 		
	}	
	
	/*
	public static void evalOld(String qa, String qb)
	{	
		Query a = QueryFactory.create(qa);
		Query b = QueryFactory.create(qb);

		Map<Node, Node> iso = Equivalence.findIsomorphy(a, b);
		
		System.out.println("Mapping result: " + iso);
	}
	*/	
}
