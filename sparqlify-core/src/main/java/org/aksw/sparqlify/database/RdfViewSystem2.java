package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Arrays;
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

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.util.Pair;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.normal_form.Clause;
import org.aksw.jena_sparql_api.normal_form.NestedNormalForm;
import org.aksw.jena_sparql_api.restriction.RestrictionManagerImpl;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.jena_sparql_api.utils.expr.NodeValueUtils;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.jena_sparql_api.views.TwoWayBinding;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.config.lang.PrefixSet;
import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.RdfViewConjunction;
import org.aksw.sparqlify.core.RdfViewInstance;
import org.aksw.sparqlify.core.RdfViewSystem;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.ReplaceConstants;
import org.aksw.sparqlify.restriction.RdfTermType;
import org.apache.commons.collections15.Transformer;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.Op1;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConstraintContext
{
	private ConstraintContext parent;
	
	// 
}


class ViewQuad {
	private RdfView view;
	private Quad quad;
	
	// TODO Maybe another field for some constraints
	
	public ViewQuad(RdfView view, Quad quad) {
		this.view = view;
		this.quad = quad;
	}

	public RdfView getView() {
		return view;
	}

	public Quad getQuad() {
		return quad;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((quad == null) ? 0 : quad.hashCode());
		result = prime * result + ((view == null) ? 0 : view.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewQuad other = (ViewQuad) obj;
		if (quad == null) {
			if (other.quad != null)
				return false;
		} else if (!quad.equals(other.quad))
			return false;
		if (view == null) {
			if (other.view != null)
				return false;
		} else if (!view.equals(other.view))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return view.toString() + ":" + quad.toString();
	}
}



class Context {
	
}


class NestedStack<T>
{
	private NestedStack<T> parent;	
	private T value;

	public NestedStack(NestedStack<T> parent, T value) {
		super();
		this.parent = parent;
		this.value = value;
	}
	
	public NestedStack<T> getParent() {
		return parent;
	}

	public T getValue() {
		return value;
	}
	
	
	public List<T> asList() {
		List<T> result = new ArrayList<T>();
		
		NestedStack<T> current = this;
		while(current != null) {
			result.add(current.getValue());
			current = current.parent;
		}
		
		Collections.reverse(result);
		
		return result;
	}
}



public class RdfViewSystem2
	implements RdfViewSystem
{

	
	private Logger logger = LoggerFactory.getLogger(RdfViewSystem2.class);
	
	private int viewId = 1;
	private Table<Object> table;
	
	PrefixIndex<Object> idxTest;
	private Set<RdfView> views = new HashSet<RdfView>();
	
	public RdfViewSystem2() {
		TableBuilder<Object> builder = new TableBuilder<Object>();
		builder.addColumn("g_prefix", String.class);
		builder.addColumn("s_prefix", String.class);
		builder.addColumn("p_prefix", String.class);
		builder.addColumn("o_type", Integer.class);
		builder.addColumn("o_prefix", String.class);
		builder.addColumn("view", ViewQuad.class);
		
		table = builder.create();
		
		
		Transformer<Object, Set<String>> prefixExtractor = new Transformer<Object, Set<String>>() {
			@Override
			public Set<String> transform(Object input) {
				return Collections.singleton(input.toString());
			}
			
		};
			
		MetaIndexFactory factory = new PrefixIndexMetaFactory(prefixExtractor);
		//MetaIndexFactory factory = new PatriciaAccessorFactory(prefixExtractor);
		
		IndexMetaNode root = IndexMetaNode.create(table, factory, "s_prefix");
		IndexMetaNode s = IndexMetaNode.create(root, factory, "p_prefix");
		TreeIndex.attach(table, root);
		
		//IndexMetaNode o = IndexMetaNode.create(s, factory, "o");

		IndexMetaNode root2 = IndexMetaNode.create(table, factory, "p_prefix");
		IndexMetaNode s2 = IndexMetaNode.create(root2, factory, "s_prefix");
		TreeIndex.attach(table, root2);
		//IndexMetaNode o = IndexMetaNode.create(s, factory, "o");

		/*
		idxS = PrefixIndex.attach(prefixExtractor, table, "s_prefix");
		PrefixIndex.attach(, "s_prefix");
		
		idxTest = PrefixIndex.attach(prefixExtractor, table, "p_prefix");
		idxTest = PrefixIndex.attach(prefixExtractor, table, "o_prefix");
		*/

	}
	
		
	@Override
	public void addView(RdfView view) {

		//Validation.validateView(view);
		
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
		
		index(copy);
	}
	
	
	public static Constraint deriveConstraint(Expr expr) {
		if(expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive) {
			return deriveConstraint(expr);
		} 
		
		return null;
	}
	
	public static StartsWithConstraint deriveConstraint(E_StrConcat expr) {
		return deriveConstraint(expr);
	}
	
	public static StartsWithConstraint deriveConstraint(E_StrConcatPermissive expr) {
		return deriveConstraint(expr);
	}

	public static StartsWithConstraint deriveConstraintConcat(ExprFunction concat) {
	
		// TODO If all arguments are constant, we could infer a constant constraint
		String prefix = "";
		for(Expr arg : concat.getArgs()) {
			if(arg.isConstant()) {
				prefix += arg.getConstant().asUnquotedString();
			} else {
				break;
			}
		}
		
		
		return new StartsWithConstraint(prefix);			
	}
	
	

	public Map<Var, RdfTermType> deriveTypeConstraints(RdfView view) {
		Map<Var, RdfTermType> result = new HashMap<Var, RdfTermType>();		
		
		for(Entry<Node, Expr> entry : view.getBinding().entrySet()) {
			Var var = 
					(Var)entry.getKey();
			
			ExprFunction termCtor = (ExprFunction)entry.getValue();
			// TODO Use the type field of RdfTerm
			//String functionIri = termCtor.getFunctionSymbol().toString();
			String functionIri = termCtor.getFunctionSymbol().getSymbol();
			if(functionIri.equals(SparqlifyConstants.rdfTermLabel)) {
				
				Expr arg = termCtor.getArg(1);
				if(arg.isConstant()) {
					Object o = NodeValueUtils.getValue(arg.getConstant());
					
					Number number = (Number)o;
					switch(number.intValue()) {
					case 1:
						result.put(var, RdfTermType.URI);
						break;
					case 2:
					case 3:
						result.put(var, RdfTermType.LITERAL);
						break;
					}
				}
			} else if(functionIri.equals(SparqlifyConstants.uriLabel)) {
				result.put(var, RdfTermType.URI);
			} else if(functionIri.equals(SparqlifyConstants.plainLiteralLabel) || functionIri.equals(SparqlifyConstants.typedLiteralLabel)) {
				result.put(var, RdfTermType.LITERAL);
			}
		}

		return result;
		
	}

	
	/**
	 * Derive prefix constraints for variables based on
	 * variable definitions:
	 * 
	 * concat('constant', var, rest) -> prefix = 'constant'
	 * 
	 * TODO: Actually we should not add these constraints to the view, but just return them
	 * 
	 */
	public void deriveRestrictions(RdfView view) {
		RestrictionManagerImpl restrictions = view.getRestrictions();
		
		for(Entry<Var, PrefixSet> entry : view.getConstraints().getVarPrefixConstraints().entrySet()) {
			restrictions.stateUriPrefixes(entry.getKey(), entry.getValue());
		}
		
		for(Entry<Node, Expr> entry : view.getBinding().entrySet()) {
			Var var = (Var)entry.getKey();
			
			ExprFunction termCtor = (ExprFunction)entry.getValue();

			/*
			if(!(expr instanceof RdfTerm)) {
				throw new RuntimeException("RdfTerm expected");
			}*/

			// TODO We assume RdfTerm here for now, but should check
			Expr expr = termCtor.getArgs().get(1);
			
			
			
			if(expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive) {

				StartsWithConstraint constraint = deriveConstraintConcat((ExprFunction)expr);
				
				restrictions.stateUriPrefixes(var, new PrefixSet(constraint.getPrefix()));
				
				//RdfTerm<Constraint> constraint = new RdfTerm<Constraint>(null, new StartsWithConstraint(prefix), null, null);
				
				//view.getConstraints().add(var, constraint);
				
			}			
		}
	}
	

	public static RdfTermType getType(Node node, RestrictionManagerImpl restrictions) {
		if(node.isVariable()) {
			RestrictionImpl r = restrictions.getRestriction((Var)node);
			if(r != null) {
				return r.getType();
			}
		} else if(node.isURI()) {
			return RdfTermType.URI;
		} else if(node.isLiteral()) {
			return RdfTermType.LITERAL;
		}
		
		return RdfTermType.UNKNOWN;
	}
	
	private void index(RdfView view) {
		
		/*
		if(view.getName().equals("lgd_node_tags_string")) {
			System.out.println("Debug");
		}
		*/
		
		RestrictionManagerImpl restrictions = new RestrictionManagerImpl();
		view.setRestrictions(restrictions);

		
		deriveRestrictions(view);
		
		

		//derivePrefixConstraints(view);
		
		// Index the pattern constraints
		Map<Var, PrefixSet> prefixConstraints = view.getConstraints().getVarPrefixConstraints();
		for(Entry<Var, PrefixSet> entry : prefixConstraints.entrySet()) {
			restrictions.stateUriPrefixes(entry.getKey(), entry.getValue());
		}

		Map<Var, RdfTermType> typeConstraints = deriveTypeConstraints(view);
		for(Entry<Var, RdfTermType> entry : typeConstraints.entrySet()) {
			restrictions.stateType(entry.getKey(), entry.getValue());
		}
				
		
		
		for(Quad quad : view.getQuadPattern()) {

			List<Collection<?>> collections = new ArrayList<Collection<?>>();

			for(int i = 0; i < 4; ++i) {
				Node node = QuadUtils.getNode(quad, i);

				if(i == 3) {
					RdfTermType type = getType(node, restrictions);					
					switch(type) {
					case URI:
						collections.add(Collections.singleton(1));
						break;
						
					case LITERAL:
						collections.add(Collections.singleton(2));
						break;
						
					default: 
						// Either URI or literal
						collections.add(Arrays.asList(1, 2));
						break;
					}
				}
				
				if(node.isVariable()) {
					
					PrefixSet p = prefixConstraints.get(node);
					if(p != null) {
						collections.add(p.getSet());
					} else {
						collections.add(Collections.singleton(""));
					}
					
				} else if (node.isURI()) {
					collections.add(Collections.singleton(node.getURI()));
				/* } else if(node.isLiteral()) {
					collections.add(Collections.singleton(node.getLiteralLexicalForm()));
					*/
				} else {
					throw new RuntimeException("Should not happen");
				}
			}

			ViewQuad viewQuad = new ViewQuad(view, quad);
			CartesianProduct<Object> cartesian = new CartesianProduct<Object>(collections);
			for(List<Object> item : cartesian) {
				List<Object> row = new ArrayList<Object>(item);
				row.add(viewQuad);
				table.add(row);
			}
		}
		
		
		
		/*
		List<ExprList> clauses = DnfUtils.toClauses(view.getFilter());
		System.out.println("DNF = " + clauses);

		Set<Set<Expr>> dnf = FilterUtils.toSets(clauses);
		
		
		for(Quad quad : view.getQuadPattern()) {
			Set<Set<Expr>> filter = FilterUtils.determineFilterDnf(quad, dnf);
			
			Map<Var, ValueSet<NodeValue>> constraints = FilterUtils.extractValueConstraintsDnf(filter);
			
			System.out.println("For quad " + quad + " got expr " + filter);
			System.out.println("Value const = " + constraints);
			
			
			//graphs.add(new FilteredGraph(quad, filter));
		}*/
	}
	
	
	public Op getApplicableViews(Query query)
	{		
		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);		
		
		op = ReplaceConstants.replace(op);
		//op = FilterPlacementOptimizer.optimize(op);
		
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
		
		Op augmented = _getApplicableViews(op);

		
		Op optimizedFilters = FilterPlacementOptimizer2.optimize(augmented);
		
		//System.out.println(optimizedFilters);
		
		//Op result = augmented;
		Op result = optimizedFilters;
		
		//System.out.println("Algebra with optimized filters: " + result);
		
		return result;
		
		//return getApp
	}

	
	/**
	 * If a variable equals a (uri or string) constant, it means that the view must provide
	 * a prefix for that value.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static VariableConstraint deriveIsPrefixOfConstraint(Expr a, Expr b)
	{
		if(!(a.isVariable() && b.isConstant())) {
			return null;
		}
		
		Object value = NodeValueUtils.getValue(b.getConstant());
		
		
		return new VariableConstraint(a.getVarName(), new IsPrefixOfConstraint(value.toString()));		
	}
	
	
	/**
	 * Returns IsPrefixOf Constraints for equality expressions between variables and constants.
	 * 
	 * Used for looking up view candidates.
	 * Not used for satisfiability checks.
	 * 
	 * 
	 * @param expr
	 * @return
	 */
	public static VariableConstraint deriveViewLookupConstraint(Expr expr) {
		if(expr instanceof E_Equals) {
			E_Equals e = (E_Equals)expr;
			
			VariableConstraint c = deriveIsPrefixOfConstraint(e.getArg1(), e.getArg2());
			if(c == null) {
				c = deriveIsPrefixOfConstraint(e.getArg2(), e.getArg1());
			}
			
			return c;
		}
		else {
			return null;
		}
	}
	
	
	
	/**
	 * Order the quads of the quadPattern by selectivity.
	 * 
	 * Iterate the quads in order, and for each quad, do a lookup of the
	 * views that may yield answers to it.
	 * So for each quad we get a set of candidate bindings to the views.
	 * 
	 * Binding is a mapping from query-var to a set of view variables/constant
	 * The view varibale may have constraints on it, which carry over to the query variable.
	 * 
	 * So given
	 * Create View x ...
	 *     {?x a Class . }
	 *     With ?x.value prefix "foo"
	 * 
	 * a query Select {?s ?p ?o }
	 * and a binding(?s = ?x),
	 * then we can infer the constraint on ?x.value prefix "foo"
	 * 
	 * So if there is a second pattern 
	 *    { ?x a Class. ?x label ?z }
	 *    
	 *    Then we can use this constraint on the lookup at the second pattern.
	 *    
	 * Note that the selectivity of query quad pattern change with the bindings,
	 * so we should be able to quickly update the selectivity of the quads in the presence of bindings. 
	 * 
	 * Another note: Maybe we could treat constants as specially delimited prefixes,
	 * such as 'aaaa' = prefix
	 * 'aaaaa$' = constant
	 */
	public List<RdfViewConjunction> getApplicableViewsBase(OpQuadPattern op, RestrictionManagerImpl restrictions)
	{
		List<RdfViewConjunction> result = new ArrayList<RdfViewConjunction>();
		
		QuadPattern queryQuads = op.getPattern(); //PatternUtils.collectQuads(op);
		//RestrictionManager restrictions = new RestrictionManager(exprs);
		
		Pair<NavigableMap<Integer, Set<Quad>>, Map<Quad, Set<ViewQuad>>> candidates = findQuadWithFewestViewCandidates(queryQuads, restrictions);

		NavigableMap<Integer, Set<Quad>> nToQuads = candidates.getKey();
		Map<Quad, Set<ViewQuad>> quadToCandidates = candidates.getValue();
		
		
		List<Quad> order = new ArrayList<Quad>();
		for(Set<Quad> quads : nToQuads.values()) {
			order.addAll(quads);
		}
			
		
		//System.out.println("Order:\n" + Joiner.on("\n").join(order));
		
		Set<ViewQuad> viewQuads = quadToCandidates.get(order.get(0));
		getApplicableViewsRec2(0, order, viewQuads, quadToCandidates, restrictions, null, result);
		
		return result; 
	}

	
	private static final String[] columnNames = new String[]{"g_prefix", "s_prefix", "p_prefix", "o_prefix"};

	public Set<ViewQuad> findCandidates(Quad quad, RestrictionManagerImpl restrictions) {
		
		//Multimap<Quad, ViewQuad> quadToView = HashMultimap.create();
		
		Set<Map<String, Constraint>> constraints = new HashSet<Map<String, Constraint>>();
		
		Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);
		Set<Clause> dnf = restrictions.getEffectiveDnf(quadVars);
		
		// TODO Get clauses by var
		RestrictionImpl[] termRestriction = new RestrictionImpl[4];
		for(Clause clause : dnf) {
			Map<String, Constraint> columnConstraints = new HashMap<String, Constraint>();
			
			
			// Prefix constraints
			for(int i = 0; i < 4; ++i) {
				Node n = QuadUtils.getNode(quad, i);
				
				/*
				if(!(n instanceof Var)) {
					System.out.println("debug");
				}
				*/
				
				Var var = (Var)QuadUtils.getNode(quad, i);
				
				RestrictionImpl r = clause.getRestriction(var);
				termRestriction[i] = r;
				
				if(r == null) {
					continue;
				}
				
				if(r.getType().equals(RdfTermType.URI) && r.hasConstant()) {
					String columnName = columnNames[i];

					columnConstraints.put(columnName, new IsPrefixOfConstraint(r.getNode().getURI()));
				}
			}

			// Object type constraint
			RestrictionImpl r = termRestriction[3];
			if(r != null) {
				switch(r.getType()) {
				case URI:
					columnConstraints.put("o_type", new EqualsConstraint(1));
					break;
				case LITERAL:
					columnConstraints.put("o_type", new EqualsConstraint(2));
					break;
				}
			}
			
			// TODO Remove subsumed constraints
			constraints.add(columnConstraints);
		}
		

		Set<ViewQuad> viewQuads = new HashSet<ViewQuad>();
		if(constraints.isEmpty()) {
			// Add a dummy element to look up all views in the subsequent loop
			constraints.add(new HashMap<String, Constraint>());			
		}
		
		for(Map<String, Constraint> columnConstraints : constraints) {
		
			Collection<List<Object>> rows = table.select(columnConstraints);

			/*
			System.out.println("BEGIN");
			System.out.println("Constraints: " + columnConstraints);
			TableImpl.printTable(rows, System.out);
			System.out.println("END");
			*/
			
			for(List<Object> row : rows) {
				// The view is the last element of the list
				ViewQuad viewQuad = (ViewQuad)row.get(row.size() - 1);
				viewQuads.add(viewQuad);
			}
		}
		
		return viewQuads;		
	}

	
	public Pair<NavigableMap<Integer, Set<Quad>>, Map<Quad, Set<ViewQuad>>> findQuadWithFewestViewCandidates(QuadPattern queryQuads, RestrictionManagerImpl restrictions)
	{
		//Map<Integer, Map<Quad, Set<ViewQuad>>> quadToView = new TreeMap<Integer, Map<Quad, Set<ViewQuad>>>();
				
		NavigableMap<Integer, Set<Quad>> nToQuads = new TreeMap<Integer, Set<Quad>>();

		Map<Quad, Set<ViewQuad>> quadToCandidates = new HashMap<Quad, Set<ViewQuad>>();
		
		
		for(Quad quad : queryQuads) {
			if(quadToCandidates.containsKey(quad)) {
				continue;
			}
			
			Set<ViewQuad> viewQuads = findCandidates(quad, restrictions);

			
			int n = viewQuads.size();
			Set<Quad> nQuads = nToQuads.get(n);
			if(nQuads == null) {
				nQuads = new HashSet<Quad>();
				nToQuads.put(n, nQuads);
			}
			nQuads.add(quad);

			
			quadToCandidates.put(quad, viewQuads);
		}

		return Pair.create(nToQuads, quadToCandidates);
	}


	public static List<String> getCandidateNames(NestedStack<RdfViewInstance> instances) {
		List<String> viewNames = new ArrayList<String>();		
		if(instances != null) {
			for(RdfViewInstance instance : instances.asList()) {
				viewNames.add(instance.getParent().getName());
			}
		}
		
		return viewNames;
	}
	
	/**
	 * 
	 * @param index
	 * @param quadOrder
	 * @param viewQuads The viewQuads provided by the parent invocation - with any restrictions encountered during the rewrite taken into account
	 * @param candidates The viewQuads here are the ones from only analyzing the query filter
	 *                   In some cases (small candidate sets) it might be faster filtering this set of view candidates, rather than doing a new lookup.
	 * @param restrictions
	 * @param result
	 */
	public void getApplicableViewsRec2(int index, List<Quad> quadOrder, Set<ViewQuad> viewQuads, Map<Quad, Set<ViewQuad>> candidates, RestrictionManagerImpl restrictions, NestedStack<RdfViewInstance> instances, List<RdfViewConjunction> result)
	{
		List<String> debug = Arrays.asList("view_nodes", "node_tags_resource_kv"); // "view_lgd_relation_specific_resources");
		List<String> viewNames = new ArrayList<String>();		
		if(instances != null) {
			for(RdfViewInstance instance : instances.asList()) {
				viewNames.add(instance.getParent().getName());
			}
		}
		
		
		if(index >= quadOrder.size()) {
			// We expect at least one quad - Bail out of the recursion happens at the end
			throw new RuntimeException("Should not happen");
		}
		
		int nextIndex = index + 1;
		boolean isRecursionEnd = nextIndex == quadOrder.size();

		Quad queryQuad = quadOrder.get(index);

		/*
		System.out.println(index + " " + queryQuad);
		for(ViewQuad viewQuad : viewQuads) {
			System.out.println("\t" + viewQuad);
		}
		System.out.println("");
		*/

		//Set<ViewQuad> viewQuads = candidates.get(queryQuad);
		

		int subId = 0;
		for(ViewQuad viewQuad : viewQuads) {
			++subId;

			String viewName = viewQuad.getView().getName();
			
			/*
			if(viewName.equals("view_nodes")) {
				System.out.println("debug");
			}
					
					
			if(viewNames.containsAll(debug) && viewName.equals("view_lgd_relation_specific_resources")) {
				System.out.println("debug");
			}

			if(viewName.equals("view_lgd_relation_specific_resources")) {
				System.out.println("debug");
			}
			*/


			RestrictionManagerImpl subRestrictions = new RestrictionManagerImpl(restrictions);
			RestrictionManagerImpl viewRestrictions = viewQuad.getView().getRestrictions();


			for(int i = 0; i < 4; ++i) {
				Var queryVar = (Var)QuadUtils.getNode(queryQuad, i);
				Node viewNode = QuadUtils.getNode(viewQuad.getQuad(), i);

				
				if(viewNode.isVariable()) {
					Var viewVar = (Var)viewNode;
					
					RestrictionImpl viewRs = viewRestrictions.getRestriction(viewVar);
					if(viewRs != null) {
						subRestrictions.stateRestriction(queryVar, viewRs);
					}
					
					if(subRestrictions.isUnsatisfiable()) {
						break;
					}

					//subRestrictions.stateEqual(queryVar, viewVar);

					if(subRestrictions.isUnsatisfiable()) {
						break;
					}					
				} else {
					subRestrictions.stateNode(queryVar, viewNode);
				}

				if(subRestrictions.isUnsatisfiable()) {
					break;
				}
			}
			
			if(subRestrictions.isUnsatisfiable()) {
				continue;
			}
			
			
			// TODO The restriction manager supersedes the two way binding
			// But changing that is a bit of work
			TwoWayBinding binding = TwoWayBinding.getVarMappingTwoWay(queryQuad, viewQuad.getQuad());

			// Try to join this instance with the candidates of the other quads 
			int instanceId = index;
			RdfViewInstance instance = new RdfViewInstance(queryQuad, viewQuad.getQuad(), instanceId, subId, viewQuad.getView(), binding);


			// Try adding the restrictions of the view to the subRestriction
			// TODO: Use the restrictions of the view, rather than using "the inferred defining exprs"
			/*
			RestrictionManager viewRestrictions = instance.getParent().getRestrictions();
			
			
			isUnsatisfiable = false;
			Set<Var> queryQuadVars = QuadUtils.getVarsMentioned(queryQuad);
			for(Var queryQuadVar : queryQuadVars) {

				Restriction viewR = viewRestrictions.getRestriction(queryQuadVar);
				if(viewR != null) {
					subRestrictions.stateRestriction(queryQuadVar, viewR);
				}
				
				isUnsatisfiable = subRestrictions.isUnsatisfiable();
				if(isUnsatisfiable) {
					break;
				}

				
				/*
				Collection<Expr> es = instance.getInferredDefiningExprs(queryQuadVar);
				for(Expr expr : es) {						
					VariableConstraint vc = deriveViewLookupConstraint(expr);
					if(vc != null) {
						System.out.println(vc);
					}
					
					// Check if this constraint has an impact on the satisfiability
					isUnsatisfiable = subRestriction.getSatisfiability() == Boolean.FALSE;
					if(isUnsatisfiable) {
						break;
					}
				}
				* /
				if(isUnsatisfiable) {
					break;
				}
			}
			if(isUnsatisfiable) {
				continue;
			}
			*/
			
			NestedStack<RdfViewInstance> nextInstances = new NestedStack<RdfViewInstance>(instances, instance);

			if(isRecursionEnd) {
				//System.out.println("QuadPattern candidate: " + getCandidateNames(nextInstances));
				/*
				TwoWayBinding completeBinding = new TwoWayBinding();
				List<RdfViewInstance> list = instances.asList();
				
				for(RdfViewInstance item : list) {
					completeBinding.addAll(item.getBinding());
				}*/
				
				RdfViewConjunction viewConjunction = new RdfViewConjunction(nextInstances.asList(), subRestrictions);

				// remove self joins
				RdfViewSystemOld.merge(viewConjunction);

				
				
				result.add(viewConjunction);
				// We have reached the end!
				// Yield another view conjunction
				
				continue;
				
			} else {
			
				Quad nextQuad = quadOrder.get(nextIndex);
				
				// With the new restriction do a lookup for the next quad
				Set<ViewQuad> nextCandidates = findCandidates(nextQuad, subRestrictions);
				
				getApplicableViewsRec2(nextIndex, quadOrder, nextCandidates, candidates, subRestrictions, nextInstances, result);
			}
		}
		
		
	}
		
		
		
		
		// A thing I noticed: Actually I don't want to pick the most selective quad (the one with the most filters/least view candidates),
		// but those quads that most likely cause unsatisfiabilities.
		// Hm, but actually: If I pick those quads with the least view candidates first, then I will quickly
		// Get to those quads causing contradictions
		
	public Op getApplicableViews(OpQuadPattern op, RestrictionManagerImpl restrictions)
	{
		List<RdfViewConjunction> conjunctions = getApplicableViewsBase(op, restrictions);
		
		OpDisjunction result = OpDisjunction.create();
		
		for(RdfViewConjunction item : conjunctions) {
			Op tmp = new OpRdfViewPattern(item);
			result.add(tmp);
		}
		
		return result;
		
		//return new OpRdfUnionViewPattern(conjunctions);
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
	 * Given a sparql query in quad form, this method replaces
	 * (sub sets of) quad patterns with view instances (view patterns)
	 * 
	 * The method also passes the filter conditions that an op must
	 * satisfy.
	 * 
	 * @param op
	 * @return
	 */
	public Op _getApplicableViews(Op op)
	{
		return _getApplicableViews(op, new RestrictionManagerImpl());
	}

	public Op _getApplicableViews(Op op, RestrictionManagerImpl restrictions)
	{
		return MultiMethod.invoke(this, "getApplicableViews", op, restrictions);
	}

	public Op getApplicableViews(OpProject op, RestrictionManagerImpl restrictions) {
		return new OpProject(_getApplicableViews(op.getSubOp(), restrictions), op.getVars());
	}
	
	public Op getApplicableViews(OpOrder op, RestrictionManagerImpl restrictions) {
		return new OpOrder(_getApplicableViews(op.getSubOp(), restrictions), op.getConditions());
	}
	
	public Op getApplicableViews(OpGroup op, RestrictionManagerImpl restrictions) {
		return new OpGroup(_getApplicableViews(op.getSubOp(), restrictions), op.getGroupVars(), op.getAggregators());
	}
	
	
	/**
	 * We treat OpExtend as a filter for now
	 * TODO This breaks for instance Count(*) as Jena also does renaming with extend:
	 * 
	 * Jena generates
	 * Project(?c, Extend(?c, ?x, Group(Count(*) As ?x)))
	 * So in this case, extend creates a new alias for a variable
	 * 
	 * 
	 */
	public Op getApplicableViews(OpExtend op, RestrictionManagerImpl _restrictions) {
		RestrictionManagerImpl restrictions = new RestrictionManagerImpl(_restrictions);
		
		for(Var var : op.getVarExprList().getVars()) {
			Expr expr = op.getVarExprList().getExpr(var);
			
			Expr item = new E_Equals(new ExprVar(var), expr);
			restrictions.stateExpr(item);
		}

		//OpProject projection = new OpPro
		
		//return _getApplicableViews(OpFilterIndexed.filter(restrictions, op.getSubOp()), restrictions);
		Op filter =  OpFilterIndexed.filter(restrictions, _getApplicableViews(op.getSubOp(), restrictions));
		
		Op result = op.copy(filter);
		return result;
	}
	
	public Op getApplicableViews(OpFilter op, RestrictionManagerImpl restrictions) 
	{
		/*
		RestrictionManager subRestrictions = new RestrictionManager(restrictions);
		
		for(Expr expr : op.getExprs()) {
			subRestrictions.stateExpr(expr);
		}
		
		return OpFilter.filter(op.getExprs(), _getApplicableViews(op.getSubOp(), subRestrictions));
		*/
		
		RestrictionManagerImpl subRestrictions = new RestrictionManagerImpl(restrictions);
		
		for(Expr expr : op.getExprs()) {
			subRestrictions.stateExpr(expr);
		}
		
		return OpFilterIndexed.filter(subRestrictions, _getApplicableViews(op.getSubOp(), subRestrictions));		
	}

	public Op getApplicableViews(OpUnion op, RestrictionManagerImpl restrictions) 
	{
		//ExprList subExprsLeft = new ExprList(exprs);
		//ExprList subExprsRight = new ExprList(exprs);
		RestrictionManagerImpl subRestrictionsLeft = new RestrictionManagerImpl(restrictions);
		RestrictionManagerImpl subRestrictionsRight = new RestrictionManagerImpl(restrictions);

		//return new OpDisjunction.
		return OpDisjunction.create(_getApplicableViews(op.getLeft(), subRestrictionsLeft), _getApplicableViews(op.getRight(), subRestrictionsRight));

		//return new OpUnion(getApplicableViews(op.getLeft(), subExprsLeft), getApplicableViews(op.getRight(), subExprsRight));
	}
	
	
	public Op getApplicableViews(OpJoin op, RestrictionManagerImpl restrictions) {
		return OpJoin.create(_getApplicableViews(op.getLeft(), restrictions), _getApplicableViews(op.getRight(), restrictions));
	}

	
	/**
	 * Create a new retrictions manager, where "bound" retrictions are removed 
	 * 
	 * @param restrictions
	 * @return
	 */
	public static RestrictionManagerImpl filterRestrictionsBound(RestrictionManagerImpl restrictions) {
		RestrictionManagerImpl result = new RestrictionManagerImpl();
		
		for(Clause clause : restrictions.getCnf()) {
			if(!FilterPlacementOptimizer2.doesClauseContainBoundExpr(clause)) {
				// FIXME Creating a new NormalForm for each clause is somewhat overkill
				result.stateCnf(new NestedNormalForm(Collections.singleton(clause)));
			}
		}
	
		return result;
	}
	
	public Op getApplicableViews(OpLeftJoin op, RestrictionManagerImpl restrictions) 
	{		
		Op left = _getApplicableViews(op.getLeft(), restrictions);

		//List<RestrictionManager> moreRestrictions = getRestrictions(left);

		RestrictionManagerImpl subRestrictions = filterRestrictionsBound(restrictions);//new RestrictionManager(restrictions);

		RestrictionManagerImpl moreRestrictions = filterRestrictionsBound(getRestrictions2(left));
		
		// Filter out !Bound restrictions
		if(moreRestrictions != null) {
			/*
			for(Clause clause : moreRestrictions.getCnf()) {
				if(!FilterPlacementOptimizer2.doesClauseContainBoundExpr(clause)) {
					// FIXME Creating a new NormalForm for each clause is somewhat overkill
					subRestrictions.stateCnf(new NestedNormalForm(Collections.singleton(clause)));
				}
			}*/
			
			
			subRestrictions.stateRestriction(moreRestrictions);
		}
		
		if(op.getExprs() != null) {
			for(Expr expr : op.getExprs()) {
				subRestrictions.stateExpr(expr);
			}
		}
		
		
		//RestrictionManager union = RestrictionManager.createUnion(moreRestrictions);
		
		//System.out.println(union);
		
		
		
		Op right = _getApplicableViews(op.getRight(), subRestrictions);
		
		return OpLeftJoin.create(left, right, new ExprList());
	}
	
	public Op getApplicableViews(OpSlice op, RestrictionManagerImpl restrictions)
	{
		return new OpSlice(_getApplicableViews(op.getSubOp(), restrictions), op.getStart(), op.getLength());
	}	
	
	public Op getApplicableViews(OpDistinct op, RestrictionManagerImpl restrictions)
	{
		return new OpDistinct(_getApplicableViews(op.getSubOp(), restrictions));
	}
	
	
	public static RestrictionManagerImpl getRestrictions2(Op op) {
		if(op instanceof OpFilterIndexed) {
			return ((OpFilterIndexed) op).getRestrictions();
		} else if(op instanceof Op1) {
			return getRestrictions2(((Op1) op).getSubOp());
		} else if(op instanceof OpJoin) {
			throw new RuntimeException("TODO Merge the restrictions of both sides of the join");
		} else if(op instanceof OpLeftJoin) {
			return getRestrictions2(((OpLeftJoin) op).getLeft());
		} else if(op instanceof OpDisjunction) {
			return null; // TODO We could factor out restrictions common to all elements
		} else if(op instanceof OpRdfViewPattern) {
			return null;
		} else {
			throw new RuntimeException("Should not happen");
		}		
	}
	
	
	
	public static List<RestrictionManagerImpl> getRestrictions(Op op) {
		List<RestrictionManagerImpl> result = new ArrayList<RestrictionManagerImpl>();
		
		getRestrictions(op, result);
		
		return result;
	}
	

	/**
	 * 
	 * Returns a disjunction (list) of restrictions that apply for a given node
	 */
	public static void getRestrictions(Op op, Collection<RestrictionManagerImpl> result) {
		if(op instanceof Op1) {
			getRestrictions(((Op1) op).getSubOp(), result);
		} else if(op instanceof OpJoin) {
			throw new RuntimeException("TODO Merge the restrictions of both sides of the join");
		} else if(op instanceof OpLeftJoin) {
			getRestrictions(((OpLeftJoin) op).getLeft(), result);
		} else if(op instanceof OpDisjunction) {
			OpDisjunction o = (OpDisjunction)op;
			for(Op subOp : o.getElements()) {
				getRestrictions(subOp, result);
			}
		} else if(op instanceof OpRdfViewPattern) {
			OpRdfViewPattern o = (OpRdfViewPattern)op;
			result.add(o.getConjunction().getRestrictions());			
		} else {
			throw new RuntimeException("Should not happen");
		}
	}
	
	
	
	/*
	public static void main(String[] args) {
		Query query = QueryFactory.create("Select Distinct ?s {?s ?p ?o }");
		
		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);	
		
		System.out.println(op);

	}*/
	/*
	public static void main(String[] args)
		throws Exception
	{
		File configFile = new File("examples/LinkedGeoData - PrefixTest.sparqlify");
	
		ConfigParser parser = new ConfigParser();

		InputStream in = new FileInputStream(configFile);
		Config config;
		try {
			config = parser.parse(in);
		} finally {
			in.close();
		}

		RdfViewSystem2 system = new RdfViewSystem2();
		ConfiguratorRdfViewSystem.configure(config, system);		
		

		Query query = QueryFactory.create("Select * { ?s a <http://www.w3.org/2002/07/owl#Class> . ?s ?p ?o . Filter(?o = <http://o1> || ?o = <http://o2>) .}");

		
		system.getApplicableViews(query);
		
		
	}*/



	@Override
	public Collection<RdfView> getViews() {
		return views;
	}
	
	
	/**
	 * About my index:
	 * Actually i'd like an index that can provide multiple "access-paths":
	 * So for each column, a set of different indexes can be used:
	 * 
	 * For instance, I could index by column "o" using a prefix index or a tree set index (supports ordering)
	 * 
	 * Basically, my idea of indexing is having a map<K, Object>, whereas different map implementations
	 * support efficiently answering different constraints (?a greater constant, ?a startsWith, etc...)
	 * 
	 * MultiIndex idx = new MultiIndex();
	 * SubIndex sub = idx.addTreeMap("columnName")
	 * sub.add
	 * 
	 * 
	 * sub.add("columnName", 
	 * 
	 * 
	 */
	
}
