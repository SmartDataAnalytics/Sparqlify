package org.aksw.sparqlify.core;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.sql.DataSource;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.MultiMaps;
import org.aksw.commons.collections.iterators.StackCartesianProductIterator;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfUnionViewPattern;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;
import org.aksw.sparqlify.algebra.sql.nodes.SqlQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.compile.sparql.SqlGenerator;
import org.aksw.sparqlify.core.jena.functions.BNode;
import org.aksw.sparqlify.core.jena.functions.PlainLiteral;
import org.aksw.sparqlify.core.jena.functions.RdfTerm;
import org.aksw.sparqlify.core.jena.functions.RightPad;
import org.aksw.sparqlify.core.jena.functions.TypedLiteral;
import org.aksw.sparqlify.core.jena.functions.Uri;
import org.aksw.sparqlify.core.jena.functions.UrlDecode;
import org.aksw.sparqlify.core.jena.functions.UrlEncode;
import org.aksw.sparqlify.trash.RdfViewDatabase;
import org.aksw.sparqlify.views.transform.FilterPlacementOptimizer;
import org.aksw.sparqlify.views.transform.ViewRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparql.DnfUtils;
import sparql.EquiMap;
import sparql.FilterUtils;
import sparql.TwoWayBinding;
import sparql.ValueSet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpNull;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;





class State<T>
{
    /*
    Iterator<Pair<T, State<T>> getTransitions()
    {
        return null;
    }*/
}



/**
 * A quad where each position can be a set of nodes.
 *
 * @author raven
 *
 */
class SetQuad
{
    public List<Set<Node>> nodes = new ArrayList<Set<Node>>();

    public SetQuad()
    {
        nodes.add(new HashSet<Node>());
        nodes.add(new HashSet<Node>());
        nodes.add(new HashSet<Node>());
        nodes.add(new HashSet<Node>());
    }

    public SetQuad(Quad quad, Map<? extends Node, ? extends Collection<? extends Node>> map)
    {
        for(Node node : QuadUtils.quadToList(quad)) {
            nodes.add(new HashSet<Node>(MapUtils.getOrElse(map, node, Collections.singleton(node))));
        }
    }

    boolean contains(Quad quad) {
        List<Node> n = QuadUtils.quadToList(quad);

        for(int i = 0; i < 4; ++i) {
            Set<Node> set = nodes.get(i);
            Node item = n.get(i);

            if(!set.contains(item)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return "SetQuad [nodes=" + nodes + "]";
    }
}




class QuadIndex<T>
{
    /**
     * Sort quads by the number of vars.
     *
     */
    private NavigableMap<Quad, T> quadByNumVars = new TreeMap<Quad, T>();


    /**
     * Index for lookup by a certain constant or variable based on a 'column' (g, s, p, o) position.
     *
     * TODO What about a pattern like "?s ?p ?o . Filter(?p In(a, b, c, ...) ?
     * In that case the constants a, b, ... would refer to the same pattern.
     * Is that a problem? I guess not?
     * Let's see:
     *
     * Given a quad from a query we want to quickly find
     * (or rule out) view candidates.
     *
     * So for each column the cases are:
     *
     * query - view
     *
     * const - const
     * var   - const
     * const - var
     * var   - var
     *
     *
     * So if there is a const in one position, and there exists no const
     * on the other quad on the same pos, we "only" have to check views
     * with a var in that location.
     *
     *
     * @param queryQuad
     * @param expr
     * @param value
     */
    private List<Map<Node, T>> byConst = new ArrayList<Map<Node, T>>();
    private List<Map<Node, T>> byVar = new ArrayList<Map<Node, T>>();


    /**
     * When views that are declared as disjoint it is assumed that their
     * corresponding virtual graphs do not have any triples in common.
     *
     * I just added this attribute because it might be useful for
     * optimization.
     *
     */
    public Multimap<RdfView, RdfView> disjointViews = HashMultimap.create();


    public void add(Quad quad, Expr expr, T value) {
        //List<Quad> QuadUtils.quadToList(quad);

        for(int i = 0; i < 4; ++i) {
        }
    }


    public Set<Map.Entry<Object,T>> lookup(Node g, Node s, Node p, Node o) {
        return null;
    }

    public Set<Map.Entry<Object,T>> lookup(Quad quad) {
        List<Node> nodes = QuadUtils.quadToList(quad);

        // Create a set of candidate quads based on the constants

        for(int i = 0; i < 4; ++i) {
            Node node = nodes.get(i);

            if(node.isConcrete()) {
                //byConst.get(
            }
        }

        return null;
    }
}



public class RdfViewSystemOld
    implements RdfViewSystem
{
    private static final Logger logger = LoggerFactory.getLogger(RdfViewSystemOld.class);

    public static void initSparqlifyFunctions() {
        FunctionRegistry.get().put(SparqlifyConstants.rdfTermLabel, RdfTerm.class);

        FunctionRegistry.get().put(SparqlifyConstants.blankNodeLabel, BNode.class);
        FunctionRegistry.get().put(SparqlifyConstants.uriLabel, Uri.class);
        FunctionRegistry.get().put(SparqlifyConstants.plainLiteralLabel, PlainLiteral.class);
        FunctionRegistry.get().put(SparqlifyConstants.typedLiteralLabel, TypedLiteral.class);

        FunctionRegistry.get().put("http://aksw.org/sparqlify/urlDecode", UrlDecode.class);

        // Jena does not yet seem to have this strangely named encode_for_uri function
        FunctionRegistry.get().put("http://aksw.org/sparqlify/urlEncode", UrlEncode.class);

        FunctionRegistry.get().put(SparqlifyConstants.rightPadLabel, RightPad.class);
    }

    /**
     * Register core functions.
     *
     * The most basic one is rdfTerm. The whole system would break without it.
     *
     */
    static {
        initSparqlifyFunctions();
    }

    private DataSource dataSource;

    private Set<RdfView> views = new HashSet<RdfView>();

    QuadIndex<RdfView> index = new QuadIndex<RdfView>();

    int viewId = 0;



    public static BiMap<Node, Node> createVariableMappingInstance(RdfView view, int id) {
        BiMap<Node, Node> result = HashBiMap.create();
        Set<Var> vars = view.getVarsMentioned();
        for(Var var : vars) {
            result.put(var, Var.alloc("inst" + id + "_" + var.getName()));
        }

        return result;
    }


    public static RdfView createViewInstance(RdfView view, int id) {
        throw new RuntimeException("outdated");
        //return view.copySubstitute(createVariableMappingInstance(view, id));
    }

    public void addView(RdfView view) {

        ++viewId;

        Set<Var> vars = view.getVarsMentioned();
        Map<Node, Node> rename = new HashMap<Node, Node>();
        for(Var var : vars) {
            rename.put(var, Var.alloc("view" + viewId + "_" + var.getName()));
        }

        RdfView copy = view.copySubstitute(rename);

        // Rename the variables in the view to make them globally unique


        //logger.trace("Renamed variables of view: " + copy);

        this.views.add(copy);

        index(view);
    }


    public static SqlQuery getTableOrQueryAsQuery(SqlNodeOld node) {
        if(node instanceof SqlQuery) {
            return (SqlQuery)node;
        } else if(node instanceof SqlTable) {
            return new SqlQuery("a", "SELECT * FROM " + ((SqlTable)node).getTableName());
        } else {
            throw new IllegalArgumentException("Query or Table expected, got " + node.getClass());
        }
    }

    /**
     * For each view, retrieve a map from column name to Datatype
     * @param conn
     * @throws Exception
     */
    public void loadDatatypes(Connection conn) throws Exception {
        loadDatatypes(conn, views);
    }

    public static void loadDatatypes(Connection conn, Collection<RdfView> views)
        throws Exception
    {
        for(RdfView view : views) {

            if(view.getSqlNode() == null ) {
                continue;
            }

            if(!view.getColumnToDatatype().isEmpty()) {
                continue;
            }

            SqlGenerator generator = new SqlGenerator();


            //String queryString = generator.generateMM(view.getSqlNode()); //SqlAlgebraToString.asString(view.getSqlNode());
            String queryString = getTableOrQueryAsQuery(view.getSqlNode()).getQueryString();

            logger.warn("Using ugly hack for adding a limit");
            if(!queryString.contains("LIMIT")) {
                queryString += " LIMIT 1";
            }

            logger.debug("Retrieving datatypes for columns of: " + queryString);

            Map<String, SqlDatatype> columnToType = RdfViewDatabase.getTypes(conn, queryString);
            for(Entry<String, SqlDatatype> entry : columnToType.entrySet()) {
                logger.info(entry.getKey() + " -> " + entry.getValue());
            }

            view.getColumnToDatatype().putAll(columnToType);
        }
    }

    /**
     * Indexes the given view:
     *
     * For each quad the following information is determined:
     * . applicable filters (if any).
     * . added to a map for
     *
     *
     * @param view
     */
    private void index(RdfView view) {

        List<ExprList> clauses = DnfUtils.toClauses(view.getFilter());
        System.out.println("DNF = " + clauses);

        Set<Set<Expr>> dnf = FilterUtils.toSets(clauses);


        for(Quad quad : view.getQuadPattern()) {
            Set<Set<Expr>> filter = FilterUtils.determineFilterDnf(quad, dnf);

            Map<Var, ValueSet<NodeValue>> constraints = FilterUtils.extractValueConstraintsDnf(filter);

            System.out.println("For quad " + quad + " got expr " + filter);
            System.out.println("Value const = " + constraints);


            //graphs.add(new FilteredGraph(quad, filter));
        }
    }


    public Op getApplicableViews(Query query)
    {
        Op op = Algebra.compile(query);
        op = Algebra.toQuadForm(op);


        op = ReplaceConstants.replace(op);

        // Add a projection if the query contains a result star
        // in order to filter out auto-generated variables
        if(query.isSelectType() && query.isQueryResultStar()) {
            List<Var> vars = query.getProjectVars();
            op = new OpProject(op, vars);
        }



        //Set<OpRdfViewPattern> result = getApplicableViews(op);
        //Set<OpRdfViewPattern> result = getApplicableViews(op);

        //TransformFilterPlacement transformer = new TransformFilterPlacement();

        //op.(transformer);

        //op = Algebra.optimize(op);

        Op augmented = getApplicableViews(op);
        Op optimizedFilters = FilterPlacementOptimizer.optimize(augmented);

        //System.out.println(optimizedFilters);

        //Op result = augmented;
        Op result = optimizedFilters;

        System.out.println(result);

        return result;

        //return getApp
    }


    /**
     * Does an merge of self-joins if possible.
     *
     *
     * @param a
     * @param b
     * @return null if no merge occured, or a new instance holding the merge
     */
    static RdfViewInstance merge(RdfViewInstance a, RdfViewInstance b)
    {
        if(a.getParent() != b.getParent()) {
            return null;
        }


        // Does the merged view pattern contain the query pattern?
        // If yes, merge!
        //
        // Map the query quad via the bindings back
        // for both views
        // then check if both quads exist in parent
        //
        // so replace all view instance vars in the binding with the parent vars
        //
        // Hm, isn't that a subsumption problem?
        // something along the way: if the merged query pattern is equal to or more specific (by making different variables equal)
        // than the view then the view can be joined?
        // If a mapping is more specific, it must include the parent's variable in an equality set:
        //
        //
        // Example 1:
        // View: { ?s geo:denomination ?o . ?s geo:religion ?p. }
        // Query: { ?x geo:denomination ?y . ?x geo:religion ?y .}
        //
        // ?y=[?inst1_view1_o, ?inst2_view1_p], ?x=[?inst1_view1_s, ?inst2_view1_s]}
        //
        // The two instances can be merged because:
        //
        //
        //
        // Mapping the instances back to the parent variables gives
        //     ?y=[?view1_o, ?view1_p], ?x=[?view1_s, ?view1_s]}
        //
        // Therefore, the two quey patterns map back to
        // { [?view1_s, ?view1_s] geo:denomination [?view1_o, ?view1_p] . [?view1_s, ?view1_s] geo:religion [?view1_o, ?view1_p])
        // and this contains the view pattern:
        // {  ?view1_s            geo:denomination  ?view1_o            .  ?view1_s            geo:religion            ?view1_p
        //
        //
        // Exapmle 2:
        // View: { ?s geo:denomination ?o . ?s geo:religion ?p. }
        // Query: { ?x geo:denomination ?y . ?h geo:religion ?i .}
        //
        // ?y=[?inst1_view1_o], ?i=[?inst2_view1_p], ?h=[?inst2_view1_s], ?x=[?inst1_view1_s]
        // the equivalence of ?s does not hold (as it is mapped to non-equal variables
        // ?y=[?view1_o], ?i=[?view1_p], ?h=[?view1_s], ?x=[?view1_s]
        //
        // As the last step we need to replace all variables of the second instance
        // with those of the first.
        //
        //
        //
        //System.out.println(a.getBinding());



        //System.out.println(Var.alloc("test").equals(Var.alloc("test")));

        //BiSetM
        //Map<Var, Collection<Var>> tmp = a.getBinding().getEquiMap().getEquivalences().asMap();

        // Map back
        // INSIGHT Mapping back to parent doesn't help because then everything gets equal
        //SetMultimap<Var, Var> back = HashMultimap.create();

        /*
        for(Entry<Var, Var> entry : Iterables.concat(a.getBinding().getEquiMap().getEquivalences().entries(), b.getBinding().getEquiMap().getEquivalences().entries())) {
            back.put(entry.getValue(), entry.getKey());
        }
        */

        //back.asMap().putAll(a.getBinding().getEquiMap().getEquivalences().getInverse().asMap());
        //back.asMap().putAll(b.getBinding().getEquiMap().getEquivalences().getInverse().asMap());


        SetMultimap<Var, Var> backA = HashMultimap.create(a.getParentToQueryBinding());
        SetMultimap<Var, Var> backB = HashMultimap.create(b.getParentToQueryBinding());

        //System.out.println("BackA: " + backA);
        //System.out.println("BackB: " + backB);

        // Now check if each parent variable in backA maps to the same
        // query variables as in backB
        // If that is the case, we have a self join
        for(Var varA : backA.keySet()) {

            Set<Var> varsA = backA.get(varA);
            Set<Var> varsB = backB.get(varA);

            if(varsB.isEmpty()) {
                continue;
            }


            if(Sets.intersection(varsA, varsB).isEmpty()) {
                //System.out.println("RESULT: No self join");

                return null;
            }
        }


        //Set valsA = a.getBinding().getEquiMap().getKeyToValue();
        // TODO URGENT: Above check is not sufficient
        // I think we need to check whether the key-to-value entries map to the same thing
        for(Entry<Var, Node> entry : a.getBinding().getEquiMap().getKeyToValue().entrySet()) {
            Var parentVar = (Var)a.getRenamer().inverse().get(entry.getKey());

            Var bVar = (Var)b.getRenamer().get(parentVar);
            Node bValue = b.getBinding().getEquiMap().getKeyToValue().get(bVar);

            if(bValue != null && !entry.getValue().equals(bValue)) {
                return null;
            }
        }


        //System.out.println("RESULT: Self join");


        // Create a copy of a (this is needed because we might be trying
        // out whether the cartesian product of view conjunctions is satisfiable, therefore
        // we must not change the state of the product)
        RdfViewInstance result = a.copy();

        // Now we have a self join. This means we have to express the binding
        // of the second view instance in terms of the first one


        // Add to 'a' all query patterns of 'b'
        result.getQueryQuads().addAll(b.getQueryQuads());
        result.getViewQuads().addAll(b.getViewQuads());

        // Replace in the binding of b all variables with those of a
        TwoWayBinding mergedBinding = new TwoWayBinding();
        mergedBinding.addAll(b.getBinding());


        // Map the names of b backt to the parent, and from the parent back to a
        Map<Node, Node> mergeMap = MapUtils.createChainMap(b.getRenamer().inverse(), result.getRenamer());
        mergedBinding = mergedBinding.copySubstitute(mergeMap);

        result.getBinding().addAll(mergedBinding);

        /*
        System.out.println("------------------------------------------------------");
        System.out.println("a: " + a.getBinding());
        System.out.println("b: " + b.getBinding());

        System.out.println("MERGE RESULT: " + result.getBinding());
        */
        //System.out.println("MERGE RESULT: " + result.getQueryQuads());

        return result;



        // Idea: Check if any view variable maps to distinct query-variables that are not equal
        // In this case the view join can't be used
        // GAAAAH Of course doesn't work - since of course the things are equal
        /*

        SetMultimap<Var, Var> backInverse = HashMultimap.create();
        for(Entry<Var, Var> entry : back.entries()) {
            backInverse.put(entry.getValue(), entry.getKey());
        }

        System.out.println("Back inverse: " + backInverse);
        for(Entry<Var, Collection<Var>> entry : backInverse.asMap().entrySet()) {

            boolean noMatch = false;
            if(!mergedBinding.getEquiMap().areAllEqual(entry.getValue())) {
                noMatch = true;
                break;
            }

            if(noMatch == true) {
                System.out.println("No Self join!");
                return null;
            }
        }*/


        /*
        for(Entry<Var, Var> entry : a.getBinding().getEquiMap().getEquivalences().entries()) {
            Var back = (Var)a.getRenamer().inverse().get(entry.getValue());
            if(back == null) {
                continue;
            }

            map.put(entry.getKey(), back);
        }

        System.out.println("BackA " + map);
         */

        //System.out.println("Merged " + mergedBinding);
        //System.out.println("Back " + back);



        /**
         * For each quad of the left query pattern query check if there exist
         * equal or more generic* quads in the view
         *
         * The queryQuad is a SetQuad because of the constraints,
         * the more general queryQuad must be contained in it.
         *
         * ----
         * We start off with the query patterns in a:
         *
         *
         * TODO Maybe we are only creating a set of candidates joins here:
         * For each pattern in 'a' we check if there is a corresponding pattern in b,
         * however multiple of such patterns may apply. But maybe if multiple patterns apply
         * then it doesn't matter, since they are all equal?????
         * So the question is whether we need a subsequent cartesian product...
         *
         */
        /*
        for(Quad queryQuadA : a.getQueryQuads()) {
            SetQuad multiQuadA = new SetQuad(queryQuadA, backA.asMap());
            System.out.println("mqa:" + multiQuadA);

            boolean foundMatch = false;
            for(Quad queryQuadB : b.getQueryQuads()) {
                SetQuad multiQuadB = new SetQuad(queryQuadB, backB.asMap());
                System.out.println("mqb:" + multiQuadB);


                /*
                if(multiQuad.contains(viewQuad)) {
                    foundMatch = true;
                    break;
                }* /
            }

            if(foundMatch == false) {
                System.out.println("No Self join!");

                return null;
            }

        }*/

        //System.out.println("Self join!");
    }

    /**
     * Eleminate self-joins from the conjunction
     * In place operation
     *
     * @param conjunction
     * @return
     */
    public static void merge(RdfViewConjunction conjunction) {

        //System.out.println("merge");
        //ViewConjunction result = new ViewConjunction(viewBindings, completeBinding)

        //List<RdfView> instances = new ArrayList<RdfView
        //RdfViewConjunction result = new RdfViewConjunction(conjunction);
        for(int i = 0; i < conjunction.getViewBindings().size(); ++i) {
            RdfViewInstance a = conjunction.getViewBindings().get(i);

            for(int j = i + 1; j < conjunction.getViewBindings().size(); ++j) {
                RdfViewInstance b = conjunction.getViewBindings().get(j);

                RdfViewInstance view = merge(a, b);
                if(view != null) { // FIXME Assumes that b is always merged into a (maybe in the future it may change)
                    a = view;
                    conjunction.getViewBindings().set(i, view);
                    conjunction.getViewBindings().remove(j);
                    --j;
                }




            }
        }

        // Recompute the two way binding
        /*
        logger.debug("Pre Merge: " + conjunction.getCompleteBinding());


        conjunction.getCompleteBinding().clear();
        for(RdfViewInstance instance : conjunction.getViewBindings()) {
            conjunction.getCompleteBinding().addAll(instance.getBinding());
        }

        logger.debug("Post Merge: " + conjunction.getCompleteBinding());
        */
        /*
        for(ViewInstance view : conjunction.getViewBindings()) {



            for(Quad quadPattern : view.getInstance().getQuadPattern()) {
                System.out.println(quadPattern);
            }
        }


        return conjunction;
        */
    }


    /**
     * Given a sparql query in quad form, this method replaces
     * (sub sets of) quad patterns with view instances (view patterns)
     *
     * The method also passes the filter conditions that an op must
     * satisfy.
     *
     * @param op
     * @return
     */
    public Op getApplicableViews(Op op)
    {
        return getApplicableViews(op, new ExprList());
    }

    public Op getApplicableViews(Op op, ExprList exprs)
    {
        return MultiMethod.invoke(this, "_getApplicableViews", op, exprs);
    }

    public Op _getApplicableViews(OpProject op, ExprList exprs) {
        return new OpProject(getApplicableViews(op.getSubOp(), exprs), op.getVars());
    }

    public Op _getApplicableViews(OpOrder op, ExprList exprs) {
        return new OpOrder(getApplicableViews(op.getSubOp(), exprs), op.getConditions());
    }

    public Op _getApplicableViews(OpGroup op, ExprList exprs) {
        return new OpGroup(getApplicableViews(op.getSubOp(), exprs), op.getGroupVars(), op.getAggregators());
    }

    // We treat OpExtend as a filter for now
    public Op _getApplicableViews(OpExtend op, ExprList _exprs) {
        ExprList exprs = ExprList.copy(_exprs);

        for(Var var : op.getVarExprList().getVars()) {
            Expr expr = op.getVarExprList().getExpr(var);

            Expr item = new E_Equals(new ExprVar(var), expr);
            exprs.add(item);
        }

        return getApplicableViews(OpFilter.filter(exprs, op.getSubOp()));
    }

    public Op _getApplicableViews(OpFilter op, ExprList exprs)
    {
        ExprList subExprs = ExprList.copy(exprs);
        subExprs.addAll(op.getExprs());

        return OpFilter.filter(op.getExprs(), getApplicableViews(op.getSubOp(), subExprs));
    }

    public Op _getApplicableViews(OpUnion op, ExprList exprs)
    {
        ExprList subExprsLeft = ExprList.copy(exprs);
        ExprList subExprsRight = ExprList.copy(exprs);

        //return new OpDisjunction.
        return OpDisjunction.create(getApplicableViews(op.getLeft(), subExprsLeft), getApplicableViews(op.getRight(), subExprsRight));

        //return new OpUnion(getApplicableViews(op.getLeft(), subExprsLeft), getApplicableViews(op.getRight(), subExprsRight));
    }


    public Op _getApplicableViews(OpJoin op, ExprList exprs) {
        return OpJoin.create(getApplicableViews(op.getLeft(), exprs), getApplicableViews(op.getRight(), exprs));
    }

    public Op _getApplicableViews(OpLeftJoin op, ExprList exprs)
    {
        logger.warn("May seed to implement left join properly");

        return OpLeftJoin.create(getApplicableViews(op.getLeft(), exprs), getApplicableViews(op.getRight()), new ExprList());
    }

    public Op _getApplicableViews(OpSlice op, ExprList exprs)
    {
        return new OpSlice(getApplicableViews(op.getSubOp(), exprs), op.getStart(), op.getLength());
    }

    public Op _getApplicableViews(OpDistinct op, ExprList exprs)
    {
        return new OpDistinct(getApplicableViews(op.getSubOp(), exprs));
    }

    public Op _getApplicableViews(OpQuadPattern op, ExprList exprs)
    {
        // Index the query
        Multimap<Quad, RdfViewInstance> queryPatternToCandidate = HashMultimap.create();




        /**
         * Step 1: Determine candidate views.
         * These are views with at least one triple pattern that is compatible
         * to the query.
         *
         *
         */
        int instanceId = 0;

        QuadPattern queryQuads = op.getPattern(); //PatternUtils.collectQuads(op);
        for(Quad quad : queryQuads) {
            // TODO WHY DID I COUNT INSTANCE IDS HERE???? - 27/09/2011
            ++instanceId;

            // TODO Does that help making the variables unique?


            // NOTE Intuitively we might say that if we do not find a
            // view candidate for a pattern we can abort.
            // However this is wrong in the case of left joins
            // They can be answered even if no view matches patterns on the right side
            for(RdfView view : views) {


                // TODO HACK we should avoid those generous substitutions
                // Each view instance should have a local map of renamed vars
                // Substitutions should only occurr if neccessairy
                // Maybe let the View instance do the substitution on demand;
                // Providing a "Rename" Wrapper around everything is not a more efficient solution
                // than to copy when needed.
                //RdfView viewInstance = createViewInstance(view, instanceId);

                int subId = 0;
                for(Quad cand : view.getQuadPattern().getList()) {
                    ++subId;


                    TwoWayBinding binding = TwoWayBinding.getVarMappingTwoWay(quad, cand);

                    // Add identity mappings for variables that map to constants but are not mapped to
                    // Example: View = {?x ?y ?z } Query= { ?s <someUri> ?o .}
                    // We need to map ?y to ?y, so we can add a pseudo filter statement on the query
                    // with filter(?y = <someUri>)

                    // Check the binding against the variable contraints
                    if(binding != null) {
                        IBiSetMultimap<Var, Var> inverse = binding.getEquiMap().getEquivalences().getInverse();
                        Set<Var> keys = new HashSet<Var>(binding.getEquiMap().getKeyToValue().keySet());

                        keys.removeAll(inverse.asMap().keySet());

                        for(Var key : keys) {
                            // TODO URGENT Clarify how to do that properly
                            // Making a var equal to itself seems wrong, as it evaluates to true

                            //binding.makeEqual(key, key);
                        }



                        queryPatternToCandidate.put(quad, new RdfViewInstance(quad, cand, instanceId, subId, view, binding));
                    }
                }
            }
        }



        // For all candidate views we rename the variables to globally unique names
        //Map<Pair<View, Var>> queryPatternToCandidate.

        logger.debug("Number of view candidates: " + queryPatternToCandidate.size());

        /*
        for(Entry<Quad, Collection<RdfViewInstance>> entry : queryPatternToCandidate.asMap().entrySet()) {
            // TODO Write out a nice table
        }*/

        System.out.println("Candidates: " + queryPatternToCandidate);

        // Order the candidates ascending by their number per quad
        Map<Integer, Set<Collection<RdfViewInstance>>> orderMap = new TreeMap<Integer, Set<Collection<RdfViewInstance>>>();

        for(Entry<Quad, Collection<RdfViewInstance>> entry : queryPatternToCandidate.asMap().entrySet()) {
            MultiMaps.put(orderMap, entry.getValue().size(), entry.getValue());
        }
        List<Collection<RdfViewInstance>> order = new ArrayList<Collection<RdfViewInstance>>(MultiMaps.values(orderMap));
        //for(Collection<RdfViewInstance>)


        //List<TwoWayBinding> results = new ArrayList<TwoWayBinding>();
        List<RdfViewConjunction> union = new ArrayList<RdfViewConjunction>();

        // Set up the cartisian products of the views
        // TODO There is a bug with empty cartesian products
        //CartesianProduct<RdfViewInstance> cartesian = CartesianProduct.create(queryPatternToCandidate.asMap().values());
        StackCartesianProductIterator<RdfViewInstance> it = new StackCartesianProductIterator<RdfViewInstance>(order);


        ViewRewriter rewriter = new ViewRewriter();


        while (it.hasNext()) {

            List<RdfViewInstance> current = it.peek();

            // Make a copy
            List<RdfViewInstance> list = new ArrayList<RdfViewInstance>(current);


            String logMessage = "";
            for(RdfViewInstance item : list) {
                logMessage += item.getInstanceId() + "\t";
            }


            //RdfViewInstance last = list.get(list.size() - 1);

            // Make sure that the combination is logically satisfiable
            TwoWayBinding completeBinding = new TwoWayBinding();

            boolean isSatisfiable = true;
            for(RdfViewInstance item : list) {
                if(!completeBinding.isCompatible(item.getBinding())) {
                    isSatisfiable = false;
                    break;
                }
                completeBinding.addAll(item.getBinding());
            }
            if(!isSatisfiable) {
                logger.debug("SKIP: " + logMessage);

                it.next();

                continue;
            }


            // Check the pattern satisfiability

            boolean isPatternSatisfiable = true;
            EquiMap<Var, Node> equiMap = completeBinding.getEquiMap();
            for(Var a : equiMap.keySet()) {
                RdfTermPattern pattern = new RdfTermPattern();

                Node value = equiMap.getKeyToValue().get(a);
                if(value != null) {
                    NodeValue nv = NodeValue.makeNode(value);

                    pattern = RdfTermPattern.intersect(pattern, RdfTermPatternDerivation.deriveRegex(nv));
                }

                //for(Var b : equiMap.getEquivalences().get(a)) {

                for(RdfViewInstance inst : list) {
                    Set<Var> bs = inst.getQueryToParentBinding().get(a);

                    for(Var b : bs) {
                        RdfTermPattern tmp = inst.getParent().getConstraints().getVarPatternConstraints().get(b);

                        pattern = RdfTermPattern.intersect(pattern, tmp);

                        if(!pattern.isSatisfiable()) {
                            isPatternSatisfiable = false;
                            break;
                        }
                    }

                    if(!isPatternSatisfiable) {
                        break;
                    }
                }
            }
            if(!isPatternSatisfiable) {
                logger.debug("SKIP: " + logMessage);

                it.next();

                continue;
            }


            // Test the binding we have so far whether it makes the filters unsatisfiable

            // Create a filter node if we have exprs
            Op testEmptyOp;

            //RdfViewConjunction tmp = new RdfViewConjunction(list, completeBinding);
            RdfViewConjunction tmp = new RdfViewConjunction(new ArrayList<RdfViewInstance>(list), completeBinding);
            merge(tmp);

            // Only test those expressions that contain all variables that we have bound so far
            ExprList applicableExprs = new ExprList();
            Set<Var> boundVars = completeBinding.keySet();
            for(Expr expr : exprs) {
                if(boundVars.containsAll(expr.getVarsMentioned())) {
                    applicableExprs.add(expr);
                }
            }

            testEmptyOp = new OpRdfViewPattern(tmp);
            if(!applicableExprs.isEmpty()) {
                testEmptyOp = OpFilter.filter(applicableExprs, testEmptyOp);
            }


            //Gensym generator = Gensym.create("a");
            ColRelGenerator generator = new ColRelGenerator();
            SqlNodeOld sqlNode = rewriter.rewriteMM(generator, testEmptyOp);


            // Check if rewriting makes it unsatisfiable
            if(sqlNode instanceof SqlNodeEmpty) {
                logger.debug("SKIP: " + logMessage);

                it.next();

                continue;
            }


            // TODO Do a check by converting to SQL
            // TODO Ideally we here know the filter conditions
            // that must be met anyway

            logger.debug("CHK : " + logMessage);


            if(!it.canDescend()) {
                logger.debug("ADD : " + logMessage);

                // We can't descend any deeper
                union.add(tmp);
                it.next();
            }
            else {
                it.descend();
            }
        }

        // Collect the results

        OpDisjunction result = OpDisjunction.create();

        for(RdfViewConjunction conjunction : union) {

            Op item = new OpRdfViewPattern(conjunction);



            // Query variables mapped to constants must be replaced by filter conditions
            ExprList constraints = new ExprList();
            for(RdfViewInstance viewInstance : conjunction.getViewBindings()) {

                for(Entry<Var, Node> entry : viewInstance.getBinding().getEquiMap().getKeyToValue().entrySet()) {

                    /*
                    Node baseVar = viewInstance.getRenamer().inverse().get(entry.getKey());

                    Map<Node, Expr> viewBinding = viewInstance.getParent().getBinding();

                    Expr expr = viewBinding.get(baseVar);

                    if(expr != null) {
                        Expr constraint = new E_Equals(expr, NodeValue.makeNode(entry.getValue()));
                        constraints.add(constraint);
                    }
                    */


                    //Expr definingExpr = viewInstance.getDefiningExpr(entry.getKey());
                    //if(definingExpr != null) {
                        //Expr expand = expandConstant(entry.getValue());

                    List<Expr> definingExprs = viewInstance.getInferredDefiningExprs(entry.getKey());

                    for(Expr definingExpr : definingExprs) {
                        // ISSUE: For each constant in the query, create a single variable.
                        // Rather than ending up with
                        // ?inst1_view1_p: <http://aksw.org/sparqlify/rdfTerm>("1"^^xsd:decimal, ?h_1, "", "")
                        // ?inst1_view2_p: <http://aksw.org/sparqlify/rdfTerm>("1"^^xsd:decimal, ?h_4, "", "")
                        // have
                        // ?inst1_p: [expr1, expr2 ]


                        //sqlBinding.put(entry.getKey(), expand);

                        // I think: If there is a defining expr for something used as a constant,
                        // create a filter with the defining expr and use the constant in the projection
                        // Similar to: Select "myConst" From ... Where column = "myConst".
                        // TODO But doesn't work ... So maybe I thought:
                        // At this point we define whether some sparql variable must equal some constant,
                        // and the corresponding SQL definition us used later

                        //Expr constraint = new E_Equals(new ExprVar(entry.getKey()), definingExpr);
                        Expr constraint = new E_Equals(new ExprVar(entry.getKey()), NodeValue.makeNode(entry.getValue()));

                        constraints.add(constraint);
                    }
                    /*
                    else {
                        //sqlBinding.put(entry.getKey(), definingExpr);
                    }*/


                }
            }

            if(!constraints.isEmpty()) {
                item = OpFilter.filter(constraints, item);
            }


            result.add(item);

            System.out.println("CompleteBinding: " + conjunction.getCompleteBinding());
            for(RdfViewInstance viewBinding : conjunction.getViewBindings()) {
                System.out.println("    : " + viewBinding.getQueryQuad());
            }
        }




        if(result.size() == 0) {
            return OpNull.create();
        } else if(result.size() == 1) {
            return result.get(0);
        } else {
            return result;
        }

        /*
        OpRdfUnionViewPattern result = new OpRdfUnionViewPattern();

        for(RdfViewConjunction conjunction : union) {
            result.getConjunctions().add(conjunction);

            System.out.println("CompleteBinding: " + conjunction.getCompleteBinding());
            for(RdfViewInstance viewBinding : conjunction.viewBindings) {
                System.out.println("    : " + viewBinding.getQueryQuad());
            }
        }
        */




        // Eleminate self joins

        // We now have something like
        // V1 a join V2 b join V3 c... Where a.s = b.s And a.x = b.x .....



        // given V1 a Join V1 b



        /*
        for(Map.Entry<Quad, Pair<RdfView, TwoWayBinding>> entry : queryPatternToCandidate.entries()) {
            for(RdfView a : candidates) {
                for(Map.Entry<Quad, Pair<RdfView, TwoWayBinding>> entry : queryPatternToCandidate.entries()) {

                }
            }
        }
        */




    }


    public static boolean isSatisfiable(List<RdfViewInstance> list)
    {
        TwoWayBinding completeBinding = new TwoWayBinding();
        boolean isOk = true;
        for(RdfViewInstance item : list) {
            if(!completeBinding.isCompatible(item.getBinding())) {
                isOk = false;
                break;
            }

            completeBinding.addAll(item.getBinding());
        }

        return isOk;
    }

    /**
     * Given a set of views, find those that might provide an answer to the given quad
     *
     * @param views
     * @param quad
     * @param filters
     * @return
     */
    public static Set<OpRdfUnionViewPattern> getApplicableViews(Set<RdfView> views, Quad quad, Expr filters, Binding substitution)
    {



        return null;
    }


    @Override
    public Collection<RdfView> getViews() {
        return views;
    }


}
