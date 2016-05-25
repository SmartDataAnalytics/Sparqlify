package org.aksw.sparqlify.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.views.Constraint;
import org.aksw.jena_sparql_api.views.PrefixSet;
import org.aksw.sparqlify.config.lang.RegexConstraint;
import org.aksw.sparqlify.database.PrefixConstraint;
import org.aksw.sparqlify.database.StartsWithConstraint;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;


public class ConstraintContainer {

    //private List<Constraint> rawConstraints = new ArrayList<Constraint>();

    // TODO: I think the original prefix objects should be retained, rather than building plain maps


    private Map<Var, RdfTermPattern> varRegexConstraints = new HashMap<Var, RdfTermPattern>(); // var -> attribute -> constraint
    private Map<Var, PrefixSet> varPrefixConstraints = new HashMap<Var, PrefixSet>();


    public Map<Var, RdfTermPattern> getVarPatternConstraints() {
        return varRegexConstraints;
    }

    public Map<Var, PrefixSet> getVarPrefixConstraints() {
        return varPrefixConstraints;
    }

    public ConstraintContainer() {
    }

    public ConstraintContainer(List<Constraint> constraints) {
        addAll(constraints);
    }


    public RdfTermPattern getPattern(Var var) {
        return varRegexConstraints.get(var);
    }

    public void addAll(List<Constraint> constraints) {
        for(Constraint c : constraints) {
            MultiMethod.invoke(this, "add", c);
        }
    }



    public ConstraintContainer(Map<Var, RdfTermPattern> varRegexConstraints) {
        this.varRegexConstraints = varRegexConstraints;
    }

    public ConstraintContainer(Map<Var, RdfTermPattern> varRegexConstraints, Map<Var, PrefixSet> varPrefixConstraints) {
        this.varRegexConstraints = varRegexConstraints;
        this.varPrefixConstraints = varPrefixConstraints;
    }

    /*
    public PatternPro getRegexPatterns(Var var, String attribute) {

    }*/


    /**
     * Creates a copy, optionally renaming variables
     *
     * @param a
     * @param renamer
     * @return
     */
    public static <K, V> Map<K, V> copy(Map<K, V> a, Map<? super K, ? super K> renamer) {
        Map<K, V> result = new HashMap<K, V>();

        for(Entry<K, V> e : a.entrySet()) {

            K var = e.getKey();

            if(renamer != null) {
                K sub = (K)renamer.get(var);
                if(sub != null) {
                    var = sub;
                }
            }


            //RdfTermPattern map = new RdfTermPattern(e.getValue());
            result.put(var, e.getValue());

        }

        return result;
    }
    /*
    public static Map<Var, RdfTermPattern> copy(Map<Var, RdfTermPattern> a, Map<? extends Node, ? extends Node> renamer) {
        Map<Var, RdfTermPattern> result = new HashMap<Var, RdfTermPattern>();

        for(Entry<Var, RdfTermPattern> e : a.entrySet()) {

            Var var = e.getKey();

            if(renamer != null) {
                Var sub = (Var)renamer.get(var);
                if(sub != null) {
                    var = sub;
                }
            }


            RdfTermPattern map = new RdfTermPattern(e.getValue());
            result.put(var, map);

        }

        return result;
    }
    */

    /*
    public static Map<Var, VarRegex> copyMerge(Map<Var, VarRegex> a, Map<Var, Map<String, PatternPro>> b) {
        Map<Var, VarRegex> c = copy(a, null);
        merge(c, b);
        return c;
    }*/

    /**
     * In-place merge of two constraints into the first argument.
     *
     * If for a variable and attribute there is no corresponding pattern in the other map, it is considered unconstrained in the other map.
     *
     * @param a
     */
    /*
    public static void merge(Map<Var, VarRegex> a, Map<Var, VarRegex> b) {
        for(Entry<Var, Map<String, PatternPro>> e : b.entrySet()) {

            Map<String, PatternPro> ae = a.get(e.getKey());

            for(Entry<String, PatternPro> f : e.getValue().entrySet()) {

                if(ae == null) {
                    ae = new HashMap<String, PatternPro>();
                    a.put(e.getKey(), ae);
                }

                PatternPro patternA = ae.get(f.getKey());
                if(patternA == null) {
                    ae.put(f.getKey(), f.getValue());
                } else {
                    patternA.getAutomaton().retainAll(f.getValue().getAutomaton());
                }

            }
        }
    }*/


    public void add(Var var, StartsWithConstraint constraint) {
        PrefixSet prefixes = varPrefixConstraints.get(var);
        if(prefixes == null) {
            prefixes = new PrefixSet();
            varPrefixConstraints.put(var, prefixes);
        }

        prefixes.addAll(Collections.singleton(constraint.getPrefix()));
    }


    /**
     * Use add(var, StartsWithConstraint instead.
     *
     * Actually this whole class should be deprecated :/
     *
     * @param constraint
     */
    @Deprecated
    public void add(PrefixConstraint constraint) {
        PrefixSet prefixes = varPrefixConstraints.get(constraint.getVar());
        if(prefixes == null) {
            prefixes = new PrefixSet();
            varPrefixConstraints.put(constraint.getVar(), prefixes);
        }

        prefixes.addAll(constraint.getPrefixes());
    }

    public void add(RegexConstraint constraint) {
        RdfTermPattern map = varRegexConstraints.get(constraint.getVar());
        if(map == null) {
            map = new RdfTermPattern();
            varRegexConstraints.put(constraint.getVar(), map);
        }

        if(constraint.getAttribute().equals("value")) {
            map.setValue(RegexConstraint.intersect(map.getValue(), constraint.getPattern()));
        } else if(constraint.getAttribute().equals("datatype")) {
            map.setDatatype(RegexConstraint.intersect(map.getDatatype(), constraint.getPattern()));
        } else {
            throw new RuntimeException("Should not happen; Unknown attribute '" + constraint.getAttribute() + "'");
        }
    }


    public ConstraintContainer copySubstitute(Map<Node, Node> renamer) {
        return new ConstraintContainer(copy(varRegexConstraints, renamer), copy(varPrefixConstraints, renamer));
    }

    /**
     *
     * @param other
     */
    /*
    public ConstraintContainer copyMerge(ConstraintContainer other) {
        return new ConstraintContainer(copyMerge(this.varRegexConstraints, other.varRegexConstraints));
    }*/
}
