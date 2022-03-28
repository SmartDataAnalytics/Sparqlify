package org.aksw.sparqlify.restriction.experiment;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.cluster.IndirectEquiMap;
import org.aksw.jena_sparql_api.exprs_ext.E_StrConcatPermissive;
import org.aksw.jena_sparql_api.normal_form.Clause;
import org.aksw.jena_sparql_api.normal_form.NestedNormalForm;
import org.aksw.jena_sparql_api.restriction.RestrictionImpl;
import org.aksw.jena_sparql_api.restriction.RestrictionSetImpl;
import org.aksw.jena_sparql_api.views.RdfTermType;
import org.aksw.jenax.arq.util.expr.CnfUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;


/**
 * A monotone container for assigning constraints to expressions.
 * A constraint that has been added cannot be removed anymore.
 *
 * WARNING: While working on an instance having the parent set,
 * do not modify any of the parents in the chain. Otherwise
 * results can be unexpected.
 *
 *
 *
 * Note adding constraints to variables is logically equivalent
 * to extending the encapsulated filter expression with
 * [...] AND (constraint). e.g. [?a = foo] AND (?a prefix bar)
 *
 * Therfore, if a constraint is inconsistent (FALSE), then the
 * whole expression is inconsistent.
 *
 * Furthermore, it is possible to state expressions, such as
 * ?a = concat('foo', bar).
 * In this case, constraints for the variable will be derived
 * from the expression.
 *
 * TODO Now that i realize: actually we first derive a description
 * (startsWith), and depending on the context we derive a constraint.
 *
 * However, a description is always a constraint (for what it describes and vice versa)
 * so there is no point in separating the concepts in the class hierarchy,
 * but on the instance level (so when using these constraints).
 *
 * TODO Actually we can delay checking of filter expressions
 *
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class RestrictionManager2 {

    private RestrictionManager2 parent;

    private IndirectEquiMap<Var, RestrictionSetImpl> restrictions = new IndirectEquiMap<Var, RestrictionSetImpl>();
    private NestedNormalForm cnf;


    /*
    public void getLocalVariables(Collection<Var> result) {
        result.addAll(restrictions.keySet());
    }*/

    public Set<Var> getVariables() {
        Set<Var> result = new HashSet<Var>();

        RestrictionManager2 current = this;
        while(current != null) {
            result.addAll(current.restrictions.keySet());
            current = current.parent;
        }

        return result;
    }


    /*
    public IndirectEquiMap<Var, RestrictionSet> getRestrictions() {
        if(parent == null) {
            return restrictions;
        }


        IndirectEquiMap<Var, RestrictionSet> result = new IndirectEquiMap<Var, RestrictionSet>();

        //result.

        RestrictionManager2 current = this;
        while(this != null) {
            result.addAll(current.restrictions.keySet());
            current = current.parent;
        }

        return result;

        Set<Var> vars = getVariables();
        for(Var var : vars) {
            RestrictionSet r = getRestriction(var);

            //restrictions.pu
        }
    }
    */

    //private ExprIndex expr;




    // Mapping of constraints derived from the expressions in expr
    //private Map<Expr, Restriction> exprToRestriction = new HashMap<Expr, Restriction>();


    // Mapping of variables to constants - derived from the restrictions
    private Map<Var, Node> binding = new HashMap<Var, Node>();
    private Binding bindingMap = BindingFactory.binding();


    // Without any constraints, we assume a tautology
    private Boolean satisfiability = Boolean.TRUE;

    public RestrictionManager2() {
        this.cnf = new NestedNormalForm(null, false);
        Set<Expr> emptyExprSet = Collections.emptySet();
        this.cnf.add(new Clause(emptyExprSet));
        this.satisfiability = Boolean.TRUE;
    }

    public RestrictionManager2(RestrictionManager2 parent) {
        this.parent = parent;
        this.cnf = new NestedNormalForm(parent.getCnf(), true);
    }

    public RestrictionManager2(NestedNormalForm cnf) {
        this.cnf = cnf;

        deriveRestrictions(cnf);
    }

    public NestedNormalForm getCnf() {
        return cnf;
    }

    public Boolean getSatisfiability()
    {
        return satisfiability;
    }

    public static RestrictionImpl deriveRestriction(Expr expr) {
        if(expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive) {
            return deriveRestriction(expr);
        } else if(expr.isConstant()) {
            RestrictionImpl result = new RestrictionImpl();
            result.stateNode(expr.getConstant().asNode());
            return result;
        }

        return null;
    }

    public static RestrictionImpl deriveRestriction(E_StrConcat expr) {
        return deriveRestrictionConcat(expr);
    }

    public static RestrictionImpl deriveRestriction(E_StrConcatPermissive expr) {
        return deriveRestrictionConcat(expr);
    }

    public static RestrictionImpl deriveRestrictionConcat(ExprFunction concat) {

        // TODO If all arguments are constant, we could infer a constant constraint
        String prefix = "";
        for(Expr arg : concat.getArgs()) {
            if(arg.isConstant()) {
                prefix += arg.getConstant().asUnquotedString();
            } else {
                break;
            }
        }

        RestrictionImpl result = new RestrictionImpl();

        result.stateUriPrefixes(new org.aksw.jena_sparql_api.views.PrefixSet(prefix));

        return result;
    };


    // Actually we could have a global cache here - exprs have an identity, so we
    // will always derive the same constraint - so thats a nice property I should exploit!
    public void deriveRestrictions(Set<Clause> cnf) {

        for(Clause clause : cnf) {
            if(clause.getExprs().size() == 1) {
                for(Entry<Var, RestrictionImpl> entry : clause.getRestrictions().entrySet()) {
                    stateRestriction(entry.getKey(), entry.getValue());
                }
                //deriveRestriction(clause.getExprs().iterator().next());
            }
        }
    }



    //private EquiMap<Var, PrefixSet> varToUriPrefixes = new EquiMap<Var, PrefixSet>();


    /*
    private IBiSetMultimap<Var, Var> equivalences = new BiHashMultimap<Var, Var>();
    private Map<Var, Node> varToNode = new HashMap<Var, Node>();

    //private Multimap<Var, Constraint> varToConstraint = HashMultimap.create();
    private Map<Var, PrefixSet> varToUriPrefixes = new HashMap<Var, PrefixSet>();
    */


    public boolean stateRestriction(Var var, RestrictionImpl restriction) {
        return stateRestriction(var, new RestrictionSetImpl(restriction));
    }


    public boolean stateRestriction(Var var, RestrictionSetImpl restriction) {
        RestrictionSetImpl r = getOrCreateLocalRestriction(var);
        if(r.stateRestriction(restriction)) {
            if(r.isUnsatisfiable()) {
                satisfiability = Boolean.FALSE;
            } else {
                check(var);
            }

            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#check(org.apache.jena.sparql.core.Var)
     */
    public void check(Var var) {
        Collection<Var> vars = restrictions.getEquivalences(var);
        check(vars);
    }


    public Set<Clause> getClausesForVars(Collection<Var> vars) {
        Set<Clause> result = new HashSet<Clause>();
        for(Var var : vars) {
            Set<Clause> tmp = cnf.getClausesByVar(var);
            if(tmp != null) {
                result.addAll(tmp);
            }
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#check(java.util.Collection)
     */
    public void check(Collection<Var> vars) {
        Set<Clause> clauses = getClausesForVars(vars);
        checkClauses(clauses);
    }

    public void checkClauses(Collection<Clause> clauses) {

        for(Clause clause : clauses) {
            check(clause);

            if(satisfiability == Boolean.FALSE) {
                return;
            }
        }
    }


    public void check(Clause clause)
    {
        // Hm, the cnf is nested for each restriction manager, but the clauses are not nested
        // Ok, so if I change a clause, I create a new one
        // The old one gets removed from the cnf, the new one gets added
        // The question is, do I want nesting withing clauses? Naaah, guess not

        //Clause modify = null;
        Set<Expr> modify = new HashSet<Expr>();

        Boolean isClauseSat = true;
        for(Expr expr : clause.getExprs()) {
            Boolean satisfiability = determineSatisfiability(expr);
            if(satisfiability == null) {
                modify.add(expr);
            }
            else if(satisfiability == true) {
                continue;
            } else { // satisfiability == false
                isClauseSat = false;
                break;
            }
        }

        // If one of the clauses is not satisfiable, the whole cnf is'nt
        if(!isClauseSat) {
            this.satisfiability = Boolean.FALSE;
            return;
        }


        // TODO We could make nested clauses
        if(modify != null) {
            cnf.remove(clause);
            cnf.add(new Clause(modify));
        }
    }


    public Boolean determineSatisfiability(Expr expr) {

        /*
        BindingMap bindingMap = new BindingMap();
        for(Entry<Var, Node> entry : binding.entrySet()) {
            bindingMap.add(entry.getKey(), entry.getValue());
        }*/

        if(binding.keySet().containsAll(expr.getVarsMentioned())) {
            try {
                NodeValue value = ExprUtils.eval(expr, bindingMap);
                return value.getBoolean();
            } catch(Exception e) {
                // Evaluation of the expression failed despite all variables were bound
                // Satisfiability unknown
                System.err.println(e);
                return null;
            }
        }
        else if(expr instanceof E_LogicalNot) {
            Boolean tmp = determineSatisfiability(((E_LogicalNot)expr).getArg());
            return tmp == null ? null : !tmp;
        }
        else if(expr instanceof E_Equals) {
            E_Equals e = (E_Equals)expr;

            RestrictionSetImpl a = getRestriction(e.getArg1());
            RestrictionSetImpl b = getRestriction(e.getArg2());

            return determineSatisfiabilityEquals(a, b);
        } else {
            return null;
        }

    }


    public RestrictionSetImpl getRestriction(Expr expr) {
        if(expr.isVariable()) {
            return restrictions.get(expr.asVar());
        } else {
            return new RestrictionSetImpl();
            //return null;
            //return exprToRestriction.get(expr);
        }
    }


    /**
     *
     * Supported Constraints: Constant, StartsWith1
     *
     * @param r
     * @param c
     */
    public static Boolean determineSatisfiabilityEquals(RestrictionImpl a, RestrictionImpl b) {
        if(a == null || b == null) {
            return null;
        }

        RestrictionImpl tmp = new RestrictionImpl(a);
        tmp.stateRestriction(b);

        if(!tmp.isConsistent()) {
            return false;
        } else {
            return null;
        }
    }

    public static Boolean determineSatisfiabilityEquals(RestrictionSetImpl a, RestrictionSetImpl b) {
        if(a == null || b == null) {
            return null;
        }

        RestrictionSetImpl tmp = new RestrictionSetImpl(a);
        tmp.stateRestriction(b);

        if(tmp.isUnsatisfiable()) {
            return false;
        } else {
            return null;
        }
    }


    public boolean isEqual(Var a, Var b) {
        boolean e = restrictions.isEqual(a, b);
        if(e) {
            return true;
        }
        else {
            return (parent != null) ? parent.isEqual(a, b) : false;
        }
    }

    public Collection<Var> getEquivalences(Var a) {
        Collection<Var> result = restrictions.getEquivalences(a);
        if(result.isEmpty() && parent != null) {
            return parent.getEquivalences(a);
        }

        return result;
    }

    public void stateEqual(Var a, Var b) {

        boolean didCopy = false;
        if(restrictions.isEqual(a, b)) {
            return;
        } else {
            if(parent != null && parent.isEqual(a, b)) {
                return;
            } else {
                // Copy the equivalences from the parent
                Collection<Var> ae = getEquivalences(a);
                RestrictionSetImpl ar = getRestriction(a);

                Collection<Var> be = getEquivalences(b);
                RestrictionSetImpl br = getRestriction(b);

                // TODO We copy the equivalences in order to avoid ConcurrentModificationException
                restrictions.stateEqual(new HashSet<Var>(ae), ar);
                restrictions.stateEqual(new HashSet<Var>(be), br);

                didCopy = true;
            }
        }

        Entry<RestrictionSetImpl, RestrictionSetImpl> conflict = restrictions.tryStateEqual(a, b);
        //Restriction r;
        if(conflict != null) {

            RestrictionSetImpl r = conflict.getKey();
            if(didCopy) {
                r = r.clone();
            }

            r.stateRestriction(conflict.getValue());
            restrictions.stateEqual(a, b, r);
        }
        /*
        else {
            r = restrictions.get(a);
        }*/

        // Recheck clauses with variable a (which is now equal to b)
        check(a);
    }

    public RestrictionSetImpl getRestriction(Var a) {
        RestrictionSetImpl result = restrictions.get(a);
        if(result == null && parent != null) {
            return parent.getRestriction(a);
        }

        return result;
    }

    public RestrictionSetImpl getOrCreateLocalRestriction(Var a) {
        RestrictionSetImpl result = restrictions.get(a);

        if(result == null && parent != null) {
            RestrictionSetImpl toCopy = parent.getRestriction(a);
            if(toCopy != null) {
                result = toCopy.clone();
            }
        }

        if(result == null) {
            result = new RestrictionSetImpl();
            restrictions.put(a, result);
        }

        return result;
    }

    public void stateType(Var a, RdfTermType type) {
        RestrictionSetImpl r = getOrCreateLocalRestriction(a);
        if(r.stateType(type)) {
            if(r.isUnsatisfiable()) {
                this.satisfiability = false;
            } else {
                check(a);
            }
        }
    }

    public void stateNode(Var a, Node b) {
        RestrictionSetImpl r = getOrCreateLocalRestriction(a);
        if(r.stateNode(b)) {
            if(r.isUnsatisfiable()) {
                satisfiability = Boolean.FALSE;
                return;
            }

            check(a);

            if(!(satisfiability == Boolean.FALSE)) {
                for(Var v : restrictions.getEquivalences(a)) {
                    binding.put(v, b);
                    bindingMap = BindingFactory.builder(bindingMap)
                            .add(v, b)
                            .build();
                    // bindingMap.add(v, b);
                }
            }

        }
    }

    public void stateUri(Var a, String uri) {
        stateNode(a, NodeFactory.createURI(uri));
    }

    public void stateLiteral(Var a, NodeValue b) {
        stateNode(a, b.asNode());
    }

    public void stateLexicalValuePrefixes(Var a, org.aksw.jena_sparql_api.views.PrefixSet prefixes) {
        RestrictionSetImpl r = getOrCreateLocalRestriction(a);
        if(r.stateUriPrefixes(prefixes)) {
            check(a);
        }
    }

    /**
     * States a new expression, which is treated as conjuncted with previous expressions.
     *
     * This means that the restrictions are monotone in regard to adding new expressions.
     *
     * Given (?a = b) && (?a = x || ?a = y)
     *
     * Note: We are only interested in 'global' restrictions, we are not dealing with alternate
     * varible assignments here (e.g. ?a = x OR ?a = y)
     *
     *
     * @param expr
     */
    public void stateExpr(Expr expr) {
        NestedNormalForm newCnf = toCnf(expr);
        stateCnf(newCnf);
    }


    public static NestedNormalForm toCnf(Expr expr) {
        Set<Set<Expr>> ss = CnfUtils.toSetCnf(expr);

        Set<Clause> clauses = new HashSet<Clause>();
        for(Set<Expr> s : ss) {
            clauses.add(new Clause(s));
        }

        return new NestedNormalForm(clauses);
    }

    public void stateCnf(NestedNormalForm newCnf) {
        deriveRestrictions(newCnf);

        if(satisfiability == Boolean.FALSE) {
            return;
        }

        cnf.addAll(newCnf);
        checkClauses(newCnf);
    }


    public void stateNonEqual(Var a, Var b) {
        throw new NotImplementedException();
    }


    // TODO I need this method due to the lack of suppert for CNF lookups on tables right now
    // Also, it does not use nesting
    public Set<Clause> getEffectiveDnf(Collection<Var> vars) {
        List<Clause> clauses = new ArrayList<Clause>(getClausesForVars(vars));

        // Order the clauses by number of expressions
        Collections.sort(clauses, new Comparator<Clause>() {
            @Override
            public int compare(Clause a, Clause b) {
                return a.size() - b.size();
            }
        });

        Set<Clause> result = new HashSet<Clause>();
        getEffectiveDnf(0, clauses, null, result);

        return result;
    }


    /**
     * I use this method for getting constraints for finding view candidates
     *
     *
     * @param dnfs
     * @param index
     * @param dnfIndex
     * @param blacklist
     * @param depth
     * @param parentClause
     * @param result
     */
    public void getEffectiveDnf(int index, List<Clause> cnfs, Clause parentClause, Set<Clause> result) {
        if(index >= cnfs.size()) {
            if(parentClause != null) {
                result.add(parentClause);
            }

            return;
        }

        Clause clause = cnfs.get(index);
        for(Expr expr : clause.getExprs()) {
            Set<Expr> exprs = new HashSet<Expr>();
            if(parentClause != null) {
                exprs.addAll(parentClause.getExprs());
            }
            exprs.add(expr);
            Clause merged = new Clause(exprs);

            getEffectiveDnf(index + 1, cnfs, merged, result);
        }
    }

    @Override
    public String toString() {
        if(satisfiability == Boolean.FALSE) {
            return "inconsistent";
        } else {
            return restrictions + " " + cnf.toString();
        }
    }

    public void stateUriPrefixes(Var a, org.aksw.jena_sparql_api.views.PrefixSet prefixes) {
        RestrictionSetImpl r = getOrCreateLocalRestriction(a);
        if(r.stateUriPrefixes(prefixes)) {
            if(r.isUnsatisfiable()) {
                satisfiability = Boolean.FALSE;
                return;
            }

            check(a);
        }
    }

    public boolean isUnsatisfiable() {
        return satisfiability == Boolean.FALSE;
    }



    /**
     * How to create unions of CNFs?
     *
     * (a AND (b OR c))   OR    (A AND (b OR c))
     *
     * The good thing: It should be easy to figure out whether a clause already exists in the CNF
     * Actually: Can we even separate the restrictions from the CNF? I guess so.
     * Actually, thats why I have the copy on write stuff anyway.
     *
     *
     * Ok: First step: Create a union of the restrictions per variable
     *
     *
     *
     *
     * @param rms
     * @return
     */
    public static RestrictionManager2 createUnion(Collection<RestrictionManager2> rms) {

        // TODO Actually we just want the query variables - and not all (which includes view vars)
        Set<Var> vars = new HashSet<Var>();
        for(RestrictionManager2 rm : rms) {
            if(rm.isUnsatisfiable()) {
                continue;
            }

            vars.addAll(rm.getVariables());
        }

        RestrictionManager2 result = new RestrictionManager2();
        // TODO: How to deal with the equivalences and the CNFs?


        for(Var var : vars) {

            RestrictionSetImpl newRs = new RestrictionSetImpl();
            for(RestrictionManager2 rm : rms) {
                if(rm.isUnsatisfiable()) {
                    continue;
                }

                RestrictionSetImpl rs = rm.getRestriction(var);
                if(rs == null || rs.isUnsatisfiable()) {
                    continue;
                }

                for(RestrictionImpl r : rs.getRestrictions()) {
                    newRs.addAlternative(r);
                }
            }

            result.stateRestriction(var, newRs);
        }

        return result;
    }


}