package org.aksw.sparqlify.restrictions;

import org.aksw.jena_sparql_api.restriction.RestrictionImpl;
import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.views.RdfTermType;
import org.aksw.sparqlify.restriction.experiment.RestrictionManager2;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Assert;
import org.junit.Test;



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

        r.stateNode(NodeFactory.createURI("http://example.org"));
        Assert.assertEquals(RdfTermType.IRI, r.getType());

        r.stateNode(NodeFactory.createURI("http://example.org"));
        Assert.assertTrue(r.isConsistent());

        r.stateNode(NodeFactory.createURI("http://foo.bar"));
        Assert.assertFalse(r.isConsistent());

    }

    @Test
    public void testRestrictionManager() {
        RestrictionManager2 m = new RestrictionManager2();

        Assert.assertTrue(m.getSatisfiability());

        Expr expr = new E_Equals(new ExprVar("x"), NodeValue.makeNode(NodeFactory.createURI("http://foo.bar")));
        m.stateExpr(expr);
        Assert.assertTrue(m.getSatisfiability());

        m.stateNode(Var.alloc("x"), NodeFactory.createURI("http://example.org"));
        Assert.assertFalse(m.getSatisfiability());

    }

    public void testRestrictionManagerOld() {
        RestrictionManagerImpl m = new RestrictionManagerImpl();

        Assert.assertTrue(m.getSatisfiability());

        Expr expr = new E_Equals(new ExprVar("x"), NodeValue.makeNode(NodeFactory.createURI("http://foo.bar")));
        m.stateExpr(expr);
        Assert.assertTrue(m.getSatisfiability());

        m.stateNode(Var.alloc("x"), NodeFactory.createURI("http://example.org"));
        Assert.assertFalse(m.getSatisfiability());

    }

}
