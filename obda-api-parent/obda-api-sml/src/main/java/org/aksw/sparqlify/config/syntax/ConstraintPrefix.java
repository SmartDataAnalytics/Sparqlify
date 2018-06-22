package org.aksw.sparqlify.config.syntax;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.views.PrefixSet;
import org.aksw.obda.domain.api.Constraint;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class ConstraintPrefix
	implements Constraint
{
    protected Var var;
    protected String attribute;
    protected PrefixSet prefixes;

    public ConstraintPrefix(Var var, String attribute, String prefix) {
        this.var = var;
        this.attribute = attribute;
        this.prefixes = new PrefixSet(prefix);
    }

    public ConstraintPrefix(Var var, String attribute, List<String> prefixes) {
        super();
        this.var = var;
        this.attribute = attribute;
        this.prefixes = new PrefixSet(new TreeSet<String>(prefixes));
    }

    public ConstraintPrefix(Var var, String attribute, PrefixSet prefixes) {
        super();
        this.var = var;
        this.attribute = attribute;
        this.prefixes = prefixes;
    }

    public ConstraintPrefix copySubstitute(Map<? extends Node, Node> map) {
        Var value = (Var)map.get(var);
        if(value == null || var.equals(value)) {
            return this;
        }

        return new ConstraintPrefix(value, attribute, prefixes);
    }

    public Var getVar() {
        return var;
    }
    public String getAttribute() {
        return attribute;
    }
    public PrefixSet getPrefixes() {
        return prefixes;
    }



    @Override
    public String toString() {
        return "PrefixConstraint [var=" + var + ", attribute=" + attribute
                + ", prefixes=" + prefixes + "]";
    }


    /**
     *
     *
     * @param a
     * @param b
     * @return The union of the two patterns; null indicates no constraint.
     */
    /*
    public static PatternPro union(PatternPro a, PatternPro b) {
        if(a == null || b == null) {
            return null;
        } else {
            PatternPro c = new PatternPro(a);
            c.addAll(b);
            return c;
        }
    }

    /**
     *
     *
     * @param a
     * @param b
     * @return The intersection of the two patterns; null indicates no constraint.
     * /
    public static PatternPro intersect(PatternPro a, PatternPro b) {
        if(a == null) {
            return b;
        } else if(b == null) {
            return a;
        } else {
            PatternPro c = new PatternPro(a);
            c.retainAll(b);
            return c;
        }
    }

    /**
     * A pattern is only satisfiable if it is either null (= unconstrained) or
     * the underlying automaton has a non-empty set of states.
     *
     * @param a
     * @return
     * /
    public static boolean isSatisfiable(PatternPro a) {
        return a == null || !a.getAutomaton().getStates().isEmpty();
    }
    */

}
