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

import org.aksw.commons.util.Pair;
import org.aksw.jena_sparql_api.normal_form.Clause;
import org.aksw.jena_sparql_api.normal_form.NestedNormalForm;
import org.aksw.jena_sparql_api.restriction.RestrictionImpl;
import org.aksw.jena_sparql_api.restriction.RestrictionManager;
import org.aksw.jena_sparql_api.views.RdfTermType;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.database.IndirectEquiMap;
import org.apache.commons.lang.NotImplementedException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;

import sparql.CnfUtils;

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
public class RestrictionManagerBeforeChangingToMap implements RestrictionManager {

    private RestrictionManagerBeforeChangingToMap parent;

    private IndirectEquiMap<Var, RestrictionImpl> restrictions = new IndirectEquiMap<Var, RestrictionImpl>();


    // TODO I want to get rid of this ExprIndex instance
    // I rather want to have a set of dnfs managed here
    // The question is how to combine that with the parent lookup (can we avoid copying everything?)

    // Hm, is it sufficient to cache unsatisfiable clauses?
    // So I never copy the DNF, but I keep track of the clauses which can be ignored
    private Set<Clause> unsatisfiableClauses = new HashSet<Clause>();


    /*
    public Iterator<Clause> getEffectiveClauses() {

    }*/

    private NestedNormalForm cnf;

    //private ExprIndex expr;




    // Mapping of constraints derived from the expressions in expr
    private Map<Expr, RestrictionImpl> exprToRestriction = new HashMap<Expr, RestrictionImpl>();


    // Mapping of variables to constants - derived from the restrictions
    private Map<Var, Node> binding = new HashMap<Var, Node>();
    private BindingMap bindingMap = new BindingHashMap();


    // Without any constraints, we assume a tautology
    private Boolean satisfiability = Boolean.TRUE;

    public RestrictionManagerBeforeChangingToMap() {
        this.cnf = new NestedNormalForm(null, false);
        Set<Expr> emptyExprSet = Collections.emptySet();
        this.cnf.add(new Clause(emptyExprSet));
        this.satisfiability = Boolean.TRUE;
    }

    public RestrictionManagerBeforeChangingToMap(RestrictionManagerBeforeChangingToMap parent) {
        this.parent = parent;
        this.cnf = new NestedNormalForm(parent.getCnf(), true);
    }

    public RestrictionManagerBeforeChangingToMap(NestedNormalForm cnf) {
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
        RestrictionImpl r = getOrCreateLocalRestriction(var);
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


    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#isUnsatisfiable(org.apache.jena.sparql.expr.Expr)
     */
    @Override
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

            RestrictionImpl a = getRestriction(e.getArg1());
            RestrictionImpl b = getRestriction(e.getArg2());

            return determineSatisfiabilityEquals(a, b);
        } else {
            return null;
        }

    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#getRestriction(org.apache.jena.sparql.expr.Expr)
     */
    @Override
    public RestrictionImpl getRestriction(Expr expr) {
        if(expr.isVariable()) {
            return restrictions.get(expr.asVar());
        } else {
            return exprToRestriction.get(expr);
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

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#stateEqual(org.apache.jena.sparql.core.Var, org.apache.jena.sparql.core.Var)
     */
//	@Override
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
                RestrictionImpl ar = getRestriction(a);

                Collection<Var> be = getEquivalences(b);
                RestrictionImpl br = getRestriction(b);

                // TODO We copy the equivalences in order to avoid ConcurrentModificationException
                restrictions.stateEqual(new HashSet<Var>(ae), ar);
                restrictions.stateEqual(new HashSet<Var>(be), br);

                didCopy = true;
            }
        }

        Pair<RestrictionImpl, RestrictionImpl> conflict = restrictions.stateEqual(a, b);
        //Restriction r;
        if(conflict != null) {

            RestrictionImpl r = conflict.getKey();
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

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#getRestriction(org.apache.jena.sparql.core.Var)
     */
    @Override
    public RestrictionImpl getRestriction(Var a) {
        RestrictionImpl result = restrictions.get(a);
        if(result == null && parent != null) {
            return parent.getRestriction(a);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#getOrCreateRestriction(org.apache.jena.sparql.core.Var)
     */
    @Override
    public RestrictionImpl getOrCreateLocalRestriction(Var a) {
        RestrictionImpl result = restrictions.get(a);

        if(result == null && parent != null) {
            RestrictionImpl toCopy = parent.getRestriction(a);
            if(toCopy != null) {
                result = toCopy.clone();
            }
        }

        if(result == null) {
            result = new RestrictionImpl();
            restrictions.put(a, result);
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#stateType(org.apache.jena.sparql.core.Var, org.aksw.sparqlify.database.Type)
     */
    @Override
    public void stateType(Var a, RdfTermType type) {
        RestrictionImpl r = getOrCreateLocalRestriction(a);
        if(r.stateType(type)) {
            if(r.isUnsatisfiable()) {
                this.satisfiability = false;
            } else {
                check(a);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#stateNode(org.apache.jena.sparql.core.Var, org.apache.jena.graph.Node)
     */
    @Override
    public void stateNode(Var a, Node b) {
        RestrictionImpl r = getOrCreateLocalRestriction(a);
        if(r.stateNode(b)) {
            if(r.isConsistent() == false) {
                satisfiability = Boolean.FALSE;
                return;
            }

            check(a);

            if(!(satisfiability == Boolean.FALSE)) {
                for(Var v : restrictions.getEquivalences(a)) {
                    binding.put(v, b);
                    bindingMap.add(v, b);
                }
            }

        }
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#stateUri(org.apache.jena.sparql.core.Var, java.lang.String)
     */
    @Override
    public void stateUri(Var a, String uri) {
        stateNode(a, NodeFactory.createURI(uri));
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#stateLiteral(org.apache.jena.sparql.core.Var, org.apache.jena.sparql.expr.NodeValue)
     */
    @Override
    public void stateLiteral(Var a, NodeValue b) {
        stateNode(a, b.asNode());
    }

    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#stateLexicalValuePrefixes(org.apache.jena.sparql.core.Var, org.aksw.sparqlify.config.lang.PrefixSet)
     */
    /*
    @Override
    public void stateLexicalValuePrefixes(Var a, PrefixSet prefixes) {
        RestrictionImpl r = getOrCreateLocalRestriction(a);
        if(r.stateUriPrefixes(prefixes)) {
            check(a);
        }
    }
    */

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
        NestedNormalForm newCnf = CnfUtils.toCnf(expr);
        stateCnf(newCnf);
    }


    public void stateCnf(NestedNormalForm newCnf) {
        deriveRestrictions(newCnf);

        if(satisfiability == Boolean.FALSE) {
            return;
        }

        cnf.addAll(newCnf);
        checkClauses(newCnf);
    }


    /* (non-Javadoc)
     * @see org.aksw.sparqlify.database.IRestrictionManager#stateNonEqual(org.apache.jena.sparql.core.Var, org.apache.jena.sparql.core.Var)
     */
    @Override
    public void stateNonEqual(Var a, Var b) {
        throw new NotImplementedException();
    }


    // Adds all constraints of the rm to this one
    public boolean stateRestriction(RestrictionManagerBeforeChangingToMap rm) {
        if(this.isUnsatisfiable()) {
            return false;
        }

        if(true) {
            throw new RuntimeException("Not implemented yet");
        }


        if(rm.isUnsatisfiable()) {
            this.satisfiability = Boolean.FALSE;
            return true;
        }

        this.stateCnf(rm.getCnf());

        if(this.isUnsatisfiable()) {
            return true;
        }

        return true;
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
        RestrictionImpl r = getOrCreateLocalRestriction(a);
        if(r.stateUriPrefixes(prefixes)) {
            if(!r.isConsistent()) {
                satisfiability = Boolean.FALSE;
                return;
            }

            check(a);
        }
    }

    public boolean isUnsatisfiable() {
        return satisfiability == Boolean.FALSE;
    }


}