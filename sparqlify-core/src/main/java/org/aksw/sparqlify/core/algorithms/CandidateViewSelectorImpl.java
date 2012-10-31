package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.jena.util.QuadUtils;
import org.aksw.commons.util.Pair;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.config.lang.PrefixSet;
import org.aksw.sparqlify.core.ReplaceConstants;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.database.Clause;
import org.aksw.sparqlify.database.Constraint;
import org.aksw.sparqlify.database.EqualsConstraint;
import org.aksw.sparqlify.database.FilterPlacementOptimizer2;
import org.aksw.sparqlify.database.IndexMetaNode;
import org.aksw.sparqlify.database.IsPrefixOfConstraint;
import org.aksw.sparqlify.database.MetaIndexFactory;
import org.aksw.sparqlify.database.NestedNormalForm;
import org.aksw.sparqlify.database.OpFilterIndexed;
import org.aksw.sparqlify.database.PrefixIndex;
import org.aksw.sparqlify.database.PrefixIndexMetaFactory;
import org.aksw.sparqlify.database.StartsWithConstraint;
import org.aksw.sparqlify.database.Table;
import org.aksw.sparqlify.database.TableBuilder;
import org.aksw.sparqlify.database.TreeIndex;
import org.aksw.sparqlify.database.VariableConstraint;
import org.aksw.sparqlify.expr.util.NodeValueUtils;
import org.aksw.sparqlify.restriction.RestrictionImpl;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.aksw.sparqlify.restriction.RdfTermType;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;

class ConstraintContext
{
	private ConstraintContext parent;
	
	// 
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



public class CandidateViewSelectorImpl
	implements CandidateViewSelector
{

	
	private Logger logger = LoggerFactory.getLogger(CandidateViewSelectorImpl.class);
	
	private int viewId = 1;
	private Table<Object> table;
	
	PrefixIndex<Object> idxTest;
	private Set<ViewDefinition> views = new HashSet<ViewDefinition>();
	
	public CandidateViewSelectorImpl() {
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
	
	
	
	public void addView(ViewDefinition viewDef) {
		//Validation.validateView(view);
		
		++viewId;

		Set<Var> vars = QuadUtils.getVarsMentioned(viewDef.getTemplate());
		Map<Var, Var> oldToNew = new HashMap<Var, Var>();
		for(Var var : vars) {
			oldToNew.put(var, Var.alloc("view" + viewId + "_" + var.getName()));
		}
		

		ViewDefinition copy = viewDef.copyRenameVars(oldToNew);

		
		/*
		for(Entry<Var, Collection<RestrictedExpr>> entry : copy.getMapping().getVarDefinition().getMap().asMap().entrySet()) {
			System.out.println(entry.getKey().getClass());
		}
		*/
		
		/*
		VarDefinition varDef = view.getMapping().getVarDefinition().copyRenameVars(oldToNew);
		
		Mapping m = new Mapping(varDef, view.getMapping().getSqlOp());
		ViewDefinition copy = new ViewDefinition(view.getName(), view.getTemplate(), view.getViewReferences(), m, view);
		*/
		
		// Rename the variables in the view to make them globally unique
		//logger.trace("Renamed variables of view: " + copy);

		
		index(copy);
	}
	
		
	//@Override
	/*
	public void addView(ViewDefinition view) {

		//Validation.validateView(view);
		
		++viewId;

		Set<Var> vars = QuadUtils.getVarsMentioned(view.getTemplate());
		Map<Node, Node> rename = new HashMap<Node, Node>();
		for(Var var : vars) {
			rename.put(var, Var.alloc("view" + viewId + "_" + var.getName()));
		}
		
		ViewDefinition copy = view.copySubstitute(rename);
		
		// Rename the variables in the view to make them globally unique
		//logger.trace("Renamed variables of view: " + copy);

		this.views.add(copy);
		
		index(copy);
	}
	*/
	
	
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
	
	
	
	private ViewDefinitionNormalizer viewDefinitionNormalizer = new ViewDefinitionNormalizer();
	
	/**
	 * 
	 * @param view
	 */
	private void index(ViewDefinition view) {

		//RestrictionManagerImpl restrictions = new RestrictionManagerImpl();
		//view.setRestrictions(restrictions);

		ViewDefinition normalized = viewDefinitionNormalizer.normalize(view);		
		RestrictionManagerImpl varRestrictions = normalized.getVarRestrictions();
		
		this.views.add(normalized);


		//derivePrefixConstraints(view);
		
		// Index the pattern constraints
		/*
		Map<Var, PrefixSet> prefixConstraints = view.getConstraints().getVarPrefixConstraints();
		for(Entry<Var, PrefixSet> entry : prefixConstraints.entrySet()) {
			restrictions.stateUriPrefixes(entry.getKey(), entry.getValue());
		}

		Map<Var, Type> typeConstraints = deriveTypeConstraints(view);
		for(Entry<Var, Type> entry : typeConstraints.entrySet()) {
			restrictions.stateType(entry.getKey(), entry.getValue());
		}
		*/

		//RestrictedExpr expr;
		//expr.getRestrictions().getType()
		
		
		for(Quad quad : normalized.getTemplate()) {

			List<Collection<?>> collections = new ArrayList<Collection<?>>();

			for(int i = 0; i < 4; ++i) {
				Node node = QuadUtils.getNode(quad, i);

				if(i == 3) {
					RdfTermType type = getType(node, varRestrictions);					
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
					
					Var var = (Var)node;
					Collection<RestrictedExpr> restExprs = normalized.getMapping().getVarDefinition().getDefinitions(var);
					
					PrefixSet p = null;
					for(RestrictedExpr restExpr : restExprs) {
						PrefixSet tmp = restExpr.getRestrictions().getUriPrefixes();
						if(p == null) {
							p = tmp;
						} else {
							p.addAll(tmp);
						}
					}
					
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

			ViewQuad viewQuad = new ViewQuad(normalized, quad);
			CartesianProduct<Object> cartesian = new CartesianProduct<Object>(collections);
			for(List<Object> item : cartesian) {
				List<Object> row = new ArrayList<Object>(item);
				row.add(viewQuad);
				table.add(row);
			}		
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
		}
	}
	*/
	
	
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
	public List<ViewInstanceJoin> getApplicableViewsBase(OpQuadPattern op, RestrictionManagerImpl restrictions)
	{
		List<ViewInstanceJoin> result = new ArrayList<ViewInstanceJoin>();
		
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


	public static List<String> getCandidateNames(NestedStack<ViewInstance> instances) {
		List<String> viewNames = new ArrayList<String>();		
		if(instances != null) {
			for(ViewInstance instance : instances.asList()) {
				viewNames.add(instance.getViewDefinition().getName());
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
	public void getApplicableViewsRec2(int index, List<Quad> quadOrder, Set<ViewQuad> viewQuads, Map<Quad, Set<ViewQuad>> candidates, RestrictionManagerImpl restrictions, NestedStack<ViewInstance> instances, List<ViewInstanceJoin> result)
	{
		List<String> debug = Arrays.asList("view_nodes", "node_tags_resource_kv"); // "view_lgd_relation_specific_resources");
		List<String> viewNames = new ArrayList<String>();		
		if(instances != null) {
			for(ViewInstance instance : instances.asList()) {
				viewNames.add(instance.getViewDefinition().getName());
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
			RestrictionManagerImpl viewRestrictions = viewQuad.getView().getVarRestrictions();

			if(viewRestrictions == null) {
				throw new NullPointerException();
			}

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
			VarBinding binding = VarBinding.create(queryQuad, viewQuad.getQuad());//VarBinding.getVarMappingTwoWay(queryQuad, viewQuad.getQuad());

					
			// Try to join this instance with the candidates of the other quads 
			int instanceId = index;
			//ViewInstance instance = new ViewInstance(queryQuad, viewQuad.getQuad(), instanceId, subId, viewQuad.getView(), binding);
			ViewInstance instance = new ViewInstance(viewQuad.getView(), binding);


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
			
			NestedStack<ViewInstance> nextInstances = new NestedStack<ViewInstance>(instances, instance);

			if(isRecursionEnd) {
				//System.out.println("QuadPattern candidate: " + getCandidateNames(nextInstances));
				/*
				VarBinding completeBinding = new VarBinding();
				List<ViewInstance> list = instances.asList();
				
				for(ViewInstance item : list) {
					completeBinding.addAll(item.getBinding());
				}*/
				
				ViewInstanceJoin viewConjunction = new ViewInstanceJoin(nextInstances.asList(), subRestrictions);

				// remove self joins
				SelfJoinEliminator.merge(viewConjunction);

				
				
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
		List<ViewInstanceJoin> conjunctions = getApplicableViewsBase(op, restrictions);
		
		OpDisjunction result = OpDisjunction.create();
		
		for(ViewInstanceJoin item : conjunctions) {
			Op tmp = new OpViewInstanceJoin(item);
			result.add(tmp);
		}
		
		return result;
		
		//return new OpRdfUnionViewPattern(conjunctions);
	}

	
	/**
	 * 
	 * TODO FIX THIS
	 * @param list
	 * @return
	 */
	public static boolean isSatisfiable(List<ViewInstance> list)
	{
		return true;
	}
	/*
	public static boolean isSatisfiable(List<ViewInstance> list)
	{
		VarBinding completeBinding = new VarBinding();
		boolean isOk = true;
		for(ViewInstance item : list) {
			if(!completeBinding.isCompatible(item.getBinding())) {
				isOk = false;
				break;
			}
			
			completeBinding.addAll(item.getBinding());	
		}

		return isOk;
	}
	*/
	
	
	
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
		
		Op result = OpFilterIndexed.filter(subRestrictions, _getApplicableViews(op.getSubOp(), subRestrictions));		
		return result;
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



	//@Override
	public Collection<ViewDefinition> getViews() {
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
