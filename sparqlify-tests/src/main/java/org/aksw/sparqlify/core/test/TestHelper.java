package org.aksw.sparqlify.core.test;

import java.util.Set;

import org.aksw.jenax.arq.util.quad.CompareUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.resultset.ResultSetCompare;

import com.google.common.collect.Sets;

public class TestHelper {

    public static boolean isEqual(Set<Quad> actual, Set<Quad> expected) {

        Set<Quad> alignedActual = CompareUtils.alignActualQuads(expected, actual);


        Set<Quad> excessive = Sets.difference(alignedActual, expected);
        Set<Quad> missing = Sets.difference(expected, alignedActual);

        if(!excessive.isEmpty() || !missing.isEmpty()) {

            System.err.println("Expected : " + expected);
            System.err.println("Actual   : " + alignedActual);

            System.err.println("Excessive: " + excessive);
            System.err.println("Missing  : " + missing);
        }

        return expected.equals(alignedActual);
        //Assert.assertEquals(expected, alignedActual);
    }


    public static boolean isEqual(ResultSet actual, ResultSet expected) {

        boolean isEqual = true;
        ResultSetRewindable act = null;
        ResultSetRewindable exp = null;
        if (expected != null) {
            // TODO File in JIRA issue: This impl is bugged (look at the code,
            // rs1 should be rs1a)
            act = ResultSetFactory.makeRewindable(actual);
            exp = ResultSetFactory.makeRewindable(expected) ;


            isEqual = ResultSetCompare.equalsByTerm(act, exp);

            act.reset();
            exp.reset();
        }

        if(!isEqual) {
            String eStr = ResultSetFormatter.asText(exp);
            System.out.println("Expected");
            System.out.println("------");
            System.out.println(eStr);

            String aStr = ResultSetFormatter.asText(act);
            System.out.println("Actual");
            System.out.println("------");
            System.out.println(aStr);
        }

        return isEqual;
    }

}
