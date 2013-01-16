package org.aksw.sparqlify.restrictions;

import org.aksw.sparqlify.restriction.RdfTermType;
import org.aksw.sparqlify.restriction.RestrictionImpl;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.aksw.sparqlify.restriction.experiment.RestrictionManager2;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;



public class RestrictionTest {

	
	@Test
	public void testRestriction2() {
		RestrictionImpl r = new RestrictionImpl();		
		Assert.assertTrue(r.getSatisfiability());
		
		RestrictionImpl a = new RestrictionImpl();
		a.stateRestriction(r);
		Assert.assertTrue(r.getSatisfiability());

		a.stateType(RdfTermType.UNKNOWN);
		Assert.assertTrue(r.getSatisfiability());
		
		//a.stateUriPrefixes(null);
		//Assert.assertTrue(r.getSatisfiability());
		
		a.stateNode(null);
		Assert.assertTrue(r.getSatisfiability());
	}
	
	@Test
	public void testRestriction() {
		
		RestrictionImpl r = new RestrictionImpl();
		Assert.assertTrue(r.isConsistent());
		
		r.stateNode(Node.createURI("http://example.org"));
		Assert.assertEquals(RdfTermType.URI, r.getType());
		
		r.stateNode(Node.createURI("http://example.org"));
		Assert.assertTrue(r.isConsistent());

		r.stateNode(Node.createURI("http://foo.bar"));
		Assert.assertFalse(r.isConsistent());
		
	}
	
	@Test
	public void testRestrictionManager() {
		RestrictionManager2 m = new RestrictionManager2();
		
		Assert.assertTrue(m.getSatisfiability());
		
		Expr expr = new E_Equals(new ExprVar("x"), NodeValue.makeNode(Node.createURI("http://foo.bar")));
		m.stateExpr(expr);		
		Assert.assertTrue(m.getSatisfiability());

		m.stateNode(Var.alloc("x"), Node.createURI("http://example.org"));
		Assert.assertFalse(m.getSatisfiability());
		
	}
	
	public void testRestrictionManagerOld() {
		RestrictionManagerImpl m = new RestrictionManagerImpl();
		
		Assert.assertTrue(m.getSatisfiability());
		
		Expr expr = new E_Equals(new ExprVar("x"), NodeValue.makeNode(Node.createURI("http://foo.bar")));
		m.stateExpr(expr);		
		Assert.assertTrue(m.getSatisfiability());

		m.stateNode(Var.alloc("x"), Node.createURI("http://example.org"));
		Assert.assertFalse(m.getSatisfiability());
		
	}
	
}
