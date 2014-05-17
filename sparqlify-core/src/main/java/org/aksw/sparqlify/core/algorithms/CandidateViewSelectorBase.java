package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.factory.Factory;
import org.aksw.commons.util.Pair;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.utils.QuadUtils;
import org.aksw.sparqlify.algebra.sparql.domain.OpRdfViewPattern;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.config.lang.PrefixSet;
import org.aksw.sparqlify.core.OpQuadPattern2;
import org.aksw.sparqlify.core.ReplaceConstants;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.IViewDef;
import org.aksw.sparqlify.database.Clause;
import org.aksw.sparqlify.database.Constraint;
import org.aksw.sparqlify.database.EqualsConstraint;
import org.aksw.sparqlify.database.FilterPlacementOptimizer2;
import org.aksw.sparqlify.database.FilterSplit;
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
import org.aksw.sparqlify.restriction.RdfTermType;
import org.aksw.sparqlify.restriction.RestrictionImpl;
import org.aksw.sparqlify.restriction.RestrictionManagerImpl;
import org.apache.commons.collections15.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.Op1;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign;
import com.hp.hpl.jena.sparql.algebra.op.OpConditional;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpExtend;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpGroup;
import com.hp.hpl.jena.sparql.algebra.op.OpJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpLeftJoin;
import com.hp.hpl.jena.sparql.algebra.op.OpOrder;
import com.hp.hpl.jena.sparql.algebra.op.OpProject;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.algebra.op.OpSlice;
import com.hp.hpl.jena.sparql.algebra.op.OpTopN;
import com.hp.hpl.jena.sparql.algebra.op.OpUnion;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * 
 * @author raven
 *
 * @param <T> The ViewDefinition type. Must inherit from IViewDef
 * @param <I> Type for result items - i.e. when the recursion reaches its end, it gives the list of found view candidates to a function that can transform it into an object of type I
 * @param <C> Context type used during recursion when looking for view candidates for quad patterns. Intended to track custom information for unsatisfiability determination.
 */
public abstract class CandidateViewSelectorBase<T extends IViewDef, C>
	implements CandidateViewSelector<T>
{
	private static Logger logger = LoggerFactory.getLogger(CandidateViewSelectorBase.class);
	
	private int viewId = 1;
	private Table<Object> table;
	
	private PrefixIndex<Object> idxTest;
	//private Set<T> views = new HashSet<T>();
	private List<T> views = new ArrayList<T>();

	//private ViewDefinitionNormalizer<T> viewDefinitionNormalizer;
	
	
	
	/**
	 * 
	 * 
	 * @param opMappingRewriter May be null. If given, we can do rewrites during the SQL generation to prune empty result mappings early. 
	 */
	public CandidateViewSelectorBase() {
				
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

	/*
	 * Abstract methods
	 */
	//public abstract I createUnionItem(List<ViewInstance<T>> list, RestrictionManagerImpl restrictions);
	public abstract Op createOp(OpQuadPattern2 opQuadPattern, List<RecursionResult<T, C>> viewInstances);

	
	/**
	 * Optionally override this method to track custom information during the recursion of candidate selection of quad patterns
	 * 
	 * 
	 * @param baseContext
	 * @param viewInstance
	 * @return
	 * @throws UnsatisfiabilityException
	 */
	public C createContext(C baseContext, ViewInstance<T> viewInstance)
			throws UnsatisfiabilityException
	{
		return baseContext;
	}

	
	
	public void addView(T viewDef) {
		//Validation.validateView(view);
		
		++viewId;

		Set<Var> vars = QuadUtils.getVarsMentioned(viewDef.getTemplate());
		Map<Var, Var> oldToNew = new HashMap<Var, Var>();
		for(Var var : vars) {
			oldToNew.put(var, Var.alloc("view" + viewId + "_" + var.getName()));
		}
		

		T copy = (T)viewDef.copyRenameVars(oldToNew);

		
		/*
		for(Entry<Var, Collection<RestrictedExpr>> entry : copy.getMapping().getVarDefinition().getMap().asMap().entrySet()) {
			System.out.println(entry.getKey().getClass());
		}
		*/
		
		/*
		VarDefinition varDef = view.getMapping().getVarDefinition().copyRenameVars(oldToNew);
		
		Mapping m = new Mapping(varDef, view.getMapping().getSqlOp());
		T copy = new T(view.getName(), view.getTemplate(), view.getViewReferences(), m, view);
		*/
		
		// Rename the variables in the view to make them globally unique
		//logger.trace("Renamed variables of view: " + copy);

		
		index(copy);
	}
	
		
	//@Override
	/*
	public void addView(T view) {

		//Validation.validateView(view);
		
		++viewId;

		Set<Var> vars = QuadUtils.getVarsMentioned(view.getTemplate());
		Map<Node, Node> rename = new HashMap<Node, Node>();
		for(Var var : vars) {
			rename.put(var, Var.alloc("view" + viewId + "_" + var.getName()));
		}
		
		T copy = view.copySubstitute(rename);
		
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
	
	
	
	//private ViewDefinitionNormalizer viewDefinitionNormalizer = new ViewDefinitionNormalizer();
	
	/**
	 * Override this function to transform the view before actually indexing it
	 * 
	 * @param view
	 * @return
	 */
	public T normalizeView(T view) {
		return view;
	}
	
	
	/**
	 * 
	 * @param view
	 */
	private void index(T view) {

		//RestrictionManagerImpl restrictions = new RestrictionManagerImpl();
		//view.setRestrictions(restrictions);

		T normalized = normalizeView(view);
		
		
		RestrictionManagerImpl varRestrictions = normalized.getVarRestrictions();
		
		this.views.add(normalized);

		
		//System.out.println("Normalized view:\n" + normalized + "\n");
		//System.out.println();
		
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

				// This check is only performed prior to the object position
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

					PrefixSet p = null;
					VarDefinition varDefinition = normalized.getVarDefinition();
					
					if(varDefinition != null) {
						Collection<RestrictedExpr> restExprs = varDefinition.getDefinitions(var);
						
						for(RestrictedExpr restExpr : restExprs) {
							PrefixSet tmp = restExpr.getRestrictions().getUriPrefixes();
							if(p == null) {
								p = tmp;
							} else {
								p.addAll(tmp);
							}
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
					// FIXME We ignore deriving constraints for literals here
					//throw new RuntimeException("Should not happen");
				}
			}

			ViewQuad<T> viewQuad = new ViewQuad<T>(normalized, quad);
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

		logger.warn("JENA'S ALGEBRA OPTIMIZATION DISABLED");
//		op = Algebra.optimize(op);
//		logger.debug("[Algebra] Jena Optimized: " + op);

		op = ReplaceConstants.replace(op);
		//logger.debug("[Algebra] ConstantsEleminated: " + op);

		// Note:
		// OpAssign: The assignments end up in a mapping's variable definition
		// I guess it is valid to convert them to OpExtend
		
		
		
		Op augmented = _getApplicableViews(op);
		//logger.debug("[Algebra] View Candidates: " + augmented);

		
		Op optimizedFilters = FilterPlacementOptimizer2.optimize(augmented);
		
		//logger.debug("[Algebra] Filter Placement Optimized: " + optimizedFilters);
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
	public List<RecursionResult<T, C>> getApplicableViewsBase(OpQuadPattern2 op, RestrictionManagerImpl restrictions)
	{
		//List<ViewInstanceJoin<T>> result = new ArrayList<ViewInstanceJoin<T>>();
		List<RecursionResult<T, C>> result = new ArrayList<RecursionResult<T, C>>();
		
		QuadPattern queryQuads = op.getPattern(); //PatternUtils.collectQuads(op);
		//RestrictionManager restrictions = new RestrictionManager(exprs);
		
		Pair<NavigableMap<Integer, Set<Quad>>, Map<Quad, Set<ViewQuad<T>>>> candidates = findQuadWithFewestViewCandidates(queryQuads, restrictions);

		NavigableMap<Integer, Set<Quad>> nToQuads = candidates.getKey();
		Map<Quad, Set<ViewQuad<T>>> quadToCandidates = candidates.getValue();
		
		
		List<Quad> order = new ArrayList<Quad>();
		for(Set<Quad> quads : nToQuads.values()) {
			order.addAll(quads);
		}
		
		
		//System.out.println("Candidate order: " + StringUtils.itemPerLine(order));
		
		//System.out.println("Order:\n" + Joiner.on("\n").join(order));
		Set<ViewQuad<T>> viewQuads = quadToCandidates.get(order.get(0));
		getApplicableViewsRec2(0, order, viewQuads, quadToCandidates, restrictions, null, result, null);
		
		
		
		return result; 
	}

	
	private static final String[] columnNames = new String[]{"g_prefix", "s_prefix", "p_prefix", "o_prefix"};

	
	public Map<String, Constraint> inferColumnConstraints(Quad quad, RestrictionImpl[] termRestriction, Clause clause) {
		
		Map<String, Constraint> result = new HashMap<String, Constraint>();
		
		
		boolean isUnsatisfiable = false;
		// Prefix constraints
		for(int i = 0; i < 4; ++i) {
			Node n = QuadUtils.getNode(quad, i);
			
			/*
			if(!(n instanceof Var)) {
				System.out.println("debug");
			}
			*/
			
			Var var = (Var)n;
			
			RestrictionImpl r;
			
			RestrictionImpl clauseRest = clause.getRestriction(var);
			RestrictionImpl bindRest = termRestriction[i];
			
			if(bindRest == null) {
				r = clauseRest;
			} else if(clauseRest != null) {
				r = bindRest.clone();
				r.stateRestriction(clauseRest);
			} else {
				r = null;
			}
			
			
			if(r == null) {
				continue;
			} else if(r.isUnsatisfiable()) {
				isUnsatisfiable = true;
				break;
			}
			
			
			
			termRestriction[i] = r;
			
			if(r.getRdfTermTypes().contains(RdfTermType.URI) && r.hasConstant()) {
				String columnName = columnNames[i];

				result.put(columnName, new IsPrefixOfConstraint(r.getNode().getURI()));
			}
		}
		
		if(isUnsatisfiable) {
			return null;
		}

		// Object type constraint
		RestrictionImpl r = termRestriction[3];
		if(r != null) {
			switch(r.getType()) {
			case URI:
				result.put("o_type", new EqualsConstraint(1));
				break;
			case LITERAL:
				result.put("o_type", new EqualsConstraint(2));
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * TODO: This method is far from optimal performance right now:
	 * We need to consider prefix-set-restrictions of the variables during the lookup in order to rule
	 * out returning to many view-quad candidates.
	 * 
	 * @param quad
	 * @param restrictions
	 * @return
	 */
	public Set<ViewQuad<T>> findCandidates(Quad quad, RestrictionManagerImpl restrictions) {
		
//		System.out.println("Looking for candidates:");
//		System.out.println("Quad: " + quad);
//		System.out.println("Restrictions: " + restrictions);
		
		
		//Multimap<Quad, ViewQuad> quadToView = HashMultimap.create();
		
		Set<Map<String, Constraint>> constraints = new HashSet<Map<String, Constraint>>();
		
		Set<Var> quadVars = QuadUtils.getVarsMentioned(quad);
		Set<Clause> dnf = restrictions.getEffectiveDnf(quadVars);
		
		if(dnf.isEmpty()) {
			dnf = new HashSet<Clause>();
			dnf.add(new Clause());
		}

		RestrictionImpl[] baseTermRestriction = new RestrictionImpl[4];

		// Get the restrictions for the variables
		for(int i = 0; i < 4; ++i) {
			Node n = QuadUtils.getNode(quad, i);
			
			Var var = (Var)n;
			
			RestrictionImpl r = restrictions.getRestriction(var);
			baseTermRestriction[i] = r;
		}
		
		logger.trace("\nTerm restrictions for " + quad + ":\n" + StringUtils.itemPerLine(baseTermRestriction));
		
		Set<ViewQuad<T>> result = new HashSet<ViewQuad<T>>();
		for(Clause clause : dnf) {
			
			// NOTE: This array gets modified by inferColumnConstraints
			RestrictionImpl[] termRestriction = Arrays.copyOf(baseTermRestriction, baseTermRestriction.length);
			Map<String, Constraint> columnConstraints = inferColumnConstraints(quad, termRestriction, clause);
	
			
			// null indicates unsatisfiablity
			if(columnConstraints == null) {
				continue;
			}
			
			Set<ViewQuad<T>> viewQuads = new HashSet<ViewQuad<T>>();
			if(constraints.isEmpty()) {
				// Add a dummy element to look up all views in the subsequent loop
				constraints.add(new HashMap<String, Constraint>());			
			}
		
		
			Collection<List<Object>> rows = table.select(columnConstraints);

			//System.out.println("    [Row]" + rows.size() + " rows for " + columnConstraints);
			/*
			System.out.println("BEGIN");
			System.out.println("Constraints: " + columnConstraints);
			TableImpl.printTable(rows, System.out);
			System.out.println("END");
			*/
			
			for(List<Object> row : rows) {
				// The view is the last element of the list
				ViewQuad<T> viewQuad = (ViewQuad<T>)row.get(row.size() - 1);
				viewQuads.add(viewQuad);
			}
			
			// Finally: Cross check the viewQuads against the namespace prefixes
			

			// TODO: We can still cross check the viewQuads against the clauses!
			
			int filterCount = 0;
			Iterator<ViewQuad<T>> itViewQuad = viewQuads.iterator();
			while(itViewQuad.hasNext()) {
				ViewQuad<T> viewQuad = itViewQuad.next();
	
				Quad q = viewQuad.getQuad();
				boolean isUnsatisfiable = false;
				for(int i = 0; i < 4; ++i) {
					RestrictionImpl queryRest = termRestriction[i];
					
					Node n = QuadUtils.getNode(q, i);
					
	
					RestrictionImpl viewRest;
					if(n.isVariable()) {
						Var var = (Var)n;
						viewRest = viewQuad.getView().getVarRestrictions().getRestriction(var);	
					} else if(n.isURI()) {
						viewRest = new RestrictionImpl();
						viewRest.stateNode(n);
					} else {
						viewRest = null;
						//throw new RuntimeException("FIXME ME handle this case");
					}
					 
					if(viewRest == null || queryRest == null) {
						continue;
					}
					
					RestrictionImpl tmp = viewRest.clone();
					tmp.stateRestriction(queryRest);
					
					if(tmp.isUnsatisfiable()) {
						isUnsatisfiable = true;
						break;
					}
				}
	
				if(isUnsatisfiable) {
					//System.out.println("Filtered: " + viewQuad);
					++filterCount;
					itViewQuad.remove();
				}
			}
			
			int total = viewQuads.size() + filterCount;
			logger.debug(viewQuads.size() + " of " + total + " candidates remaining (" + filterCount + " filtered)");
		
			
			result.addAll(viewQuads);
			//System.out.println("Total results: " + viewQuads.size());
			//logger.debug(StringUtils.itemPerLine(viewQuads));
			//logger.info("--------------------------------");
		}
		logger.debug("Total number of candidates after " + dnf.size() + " clauses: " + result.size());
		
		return result;
	}

	
	public Pair<NavigableMap<Integer, Set<Quad>>, Map<Quad, Set<ViewQuad<T>>>> findQuadWithFewestViewCandidates(QuadPattern queryQuads, RestrictionManagerImpl restrictions)
	{
		//Map<Integer, Map<Quad, Set<ViewQuad>>> quadToView = new TreeMap<Integer, Map<Quad, Set<ViewQuad>>>();
				
		NavigableMap<Integer, Set<Quad>> nToQuads = new TreeMap<Integer, Set<Quad>>();

		Map<Quad, Set<ViewQuad<T>>> quadToCandidates = new HashMap<Quad, Set<ViewQuad<T>>>();
		
		
		for(Quad quad : queryQuads) {
			if(quadToCandidates.containsKey(quad)) {
				continue;
			}
			
			Set<ViewQuad<T>> viewQuads = findCandidates(quad, restrictions);

			
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


	public static <T extends IViewDef> List<String> getCandidateNames(NestedStack<ViewInstance<T>> instances) {
		List<String> viewNames = new ArrayList<String>();		
		if(instances != null) {
			for(ViewInstance<T> instance : instances.asList()) {
				viewNames.add(instance.getViewDefinition().getName());
			}
		}
		
		return viewNames;
	}
	
	
	/**
	 * Checks whether the view quad is consistent with the current constraints
	 * 
	 * @param restrictions
	 * @param queryQuad
	 * @param viewQuad
	 * @return
	 */
	public static <T extends IViewDef> ViewInstance<T> createViewInstance(RestrictionManagerImpl subRestrictions, Quad queryQuad, ViewQuad<T> viewQuad) {

		
		// Restrictions that are intrinsic to the view definition
		RestrictionManagerImpl viewRestrictions = viewQuad.getView().getVarRestrictions();

		// Only the restrictions that apply to this quad
		//RestrictionManagerImpl mapRestrictions = new RestrictionManagerImpl();
		
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
			return null;
		}
		
		//System.out.println("SubRestrictions: " + subRestrictions.getRestrictions().size());
		
		
		// TODO The restriction manager supersedes the two way binding
		// But changing that is a bit of work
		VarBinding binding = VarBinding.create(queryQuad, viewQuad.getQuad());//VarBinding.getVarMappingTwoWay(queryQuad, viewQuad.getQuad());
		if(binding == null) {
			// FIXME Not sure if we need to skip on null (-> unsatisfiable) or
			// whether we need to do some handling
			//System.out.println("Null binding");
			throw new RuntimeException("Null binding");
			//continue;
		}
				
		// Try to join this instance with the candidates of the other quads 
		//int instanceId = index;
		//ViewInstance instance = new ViewInstance(queryQuad, viewQuad.getQuad(), instanceId, subId, viewQuad.getView(), binding);
		ViewInstance<T> instance = new ViewInstance<T>(viewQuad.getView(), binding);

	
		return instance;
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
	public void getApplicableViewsRec2(int index, List<Quad> quadOrder, Set<ViewQuad<T>> viewQuads, Map<Quad, Set<ViewQuad<T>>> candidates, RestrictionManagerImpl restrictions, NestedStack<ViewInstance<T>> instances, List<RecursionResult<T, C>> result, C baseContext) //Mapping baseMapping)
	{
		//List<String> debug = Arrays.asList("view_nodes", "node_tags_resource_kv"); // "view_lgd_relation_specific_resources");
		List<String> viewNames = new ArrayList<String>();
		if(instances != null) {
			for(ViewInstance<T> instance : instances.asList()) {
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

		
		//System.out.println("Query Quad" + queryQuad + ": " + candidates.size());

		/*
		System.out.println(index + " " + queryQuad);
		for(ViewQuad viewQuad : viewQuads) {
			System.out.println("\t" + viewQuad);
		}
		System.out.println("");
		*/

		//Set<ViewQuad> viewQuads = candidates.get(queryQuad);
		

		//int subId = 0;
		for(ViewQuad<T> viewQuad : viewQuads) {
			//++subId;

			//String viewName = viewQuad.getView().getName();
			
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


//			// All restrictions that apply to this quad (even those whose variables are not bound by the quad pattern)
//			RestrictionManagerImpl subRestrictions = new RestrictionManagerImpl(restrictions);
//			
//			// Restrictions that are intrinsic to the view definition
//			RestrictionManagerImpl viewRestrictions = viewQuad.getView().getVarRestrictions();
//
//			// Only the restrictions that apply to this quad
//			//RestrictionManagerImpl mapRestrictions = new RestrictionManagerImpl();
//			
//			if(viewRestrictions == null) {
//				throw new NullPointerException();
//			}
//
//			for(int i = 0; i < 4; ++i) {
//				Var queryVar = (Var)QuadUtils.getNode(queryQuad, i);
//				Node viewNode = QuadUtils.getNode(viewQuad.getQuad(), i);
//
//				
//				if(viewNode.isVariable()) {
//					Var viewVar = (Var)viewNode;
//					
//					RestrictionImpl viewRs = viewRestrictions.getRestriction(viewVar);
//					if(viewRs != null) {
//						subRestrictions.stateRestriction(queryVar, viewRs);
//					}
//					
//					if(subRestrictions.isUnsatisfiable()) {
//						break;
//					}
//
//					//subRestrictions.stateEqual(queryVar, viewVar);
//
//					if(subRestrictions.isUnsatisfiable()) {
//						break;
//					}					
//				} else {
//					subRestrictions.stateNode(queryVar, viewNode);
//				}
//
//				if(subRestrictions.isUnsatisfiable()) {
//					break;
//				}
//			}
//			
//			if(subRestrictions.isUnsatisfiable()) {
//				continue;
//			}
//			
//			//System.out.println("SubRestrictions: " + subRestrictions.getRestrictions().size());
//			
//			
//			// TODO The restriction manager supersedes the two way binding
//			// But changing that is a bit of work
//			VarBinding binding = VarBinding.create(queryQuad, viewQuad.getQuad());//VarBinding.getVarMappingTwoWay(queryQuad, viewQuad.getQuad());
//			if(binding == null) {
//				// FIXME Not sure if we need to skip on null (-> unsatisfiable) or
//				// whether we need to do some handling
//				//System.out.println("Null binding");
//				throw new RuntimeException("Null binding");
//				//continue;
//			}
//					
//			// Try to join this instance with the candidates of the other quads 
//			int instanceId = index;
//			//ViewInstance instance = new ViewInstance(queryQuad, viewQuad.getQuad(), instanceId, subId, viewQuad.getView(), binding);
//			ViewInstance instance = new ViewInstance(viewQuad.getView(), binding);

			// All restrictions that apply to this quad (even those whose variables are not bound by the quad pattern)
			RestrictionManagerImpl subRestrictions = new RestrictionManagerImpl(restrictions);

			ViewInstance<T> viewInstance = createViewInstance(subRestrictions, queryQuad, viewQuad);
			if(viewInstance == null) {
				continue;
			}

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
			
			NestedStack<ViewInstance<T>> nextInstances = new NestedStack<ViewInstance<T>>(instances, viewInstance);

			C nextContext;
			try {
				nextContext = createContext(baseContext, viewInstance);
			} catch(UnsatisfiabilityException e) {
				continue;
			}

			
			
//			boolean enablePruningMappingRewrite = true;
//			if(enablePruningMappingRewrite && mappingOps != null) {
//
//				Mapping mapping = mappingOps.createMapping(instance);
//				
//				if(baseMapping == null) {
//					nextMapping = mapping;
//				} else {
//					nextMapping = mappingOps.join(baseMapping, mapping);
//				}
//				
//				if(nextMapping.isEmpty()) {
//					continue;
//				}
//			}
			
			
			
			if(isRecursionEnd) {
				//System.out.println("QuadPattern candidate: " + getCandidateNames(nextInstances));
				/*
				VarBinding completeBinding = new VarBinding();
				List<ViewInstance> list = instances.asList();
				
				for(ViewInstance item : list) {
					completeBinding.addAll(item.getBinding());
				}*/
				
				//I item = createUnionItem(nextInstances.asList(), subRestrictions);
				
				ViewInstanceJoin<T> viewConjunction = new ViewInstanceJoin<T>(nextInstances.asList(), subRestrictions);
				RecursionResult<T, C> recResult = RecursionResult.create(viewConjunction, nextContext);
				
				//result.add(viewConjunction);
				result.add(recResult);
				

				// remove self joins
				//SelfJoinEliminator.merge(viewConjunction);

				
				
				//result.add(viewConjunction);
				// We have reached the end!
				// Yield another view conjunction
				
//				System.out.println("Current candidate result size: " + result.size());
				//System.out.println(viewConjunction);
				//System.out.println("ViewNames: " + viewConjunction.getViewNames());
				
				continue;
				
			} else {
				
				//logger.debug("subRestrictions:\n" + StringUtils.itemPerLine(subRestrictions.getRestrictions().entrySet()));
				
			
				Quad nextQuad = quadOrder.get(nextIndex);
				
				// With the new restriction do a lookup for the next quad
				Set<ViewQuad<T>> nextCandidates = findCandidates(nextQuad, subRestrictions);
				//System.out.println("Candidates found: " + nextCandidates.size() + " restrictions:\n" + subRestrictions.getRestrictions().size());
				
				getApplicableViewsRec2(nextIndex, quadOrder, nextCandidates, candidates, subRestrictions, nextInstances, result, nextContext);
			}
		}
		
		
	}
		
		
		
		
		// A thing I noticed: Actually I don't want to pick the most selective quad (the one with the most filters/least view candidates),
		// but those quads that most likely cause unsatisfiabilities.
		// Hm, but actually: If I pick those quads with the least view candidates first, then I will quickly
		// Get to those quads causing contradictions
		
	public Op getApplicableViews(OpQuadPattern2 op, RestrictionManagerImpl restrictions)
	{
		//List<ViewInstanceJoin<T>> conjunctions =
		List<RecursionResult<T, C>> conjuncions = getApplicableViewsBase(op, restrictions);
		
		Op result = createOp(op, conjuncions);
		/*
		OpDisjunction result = OpDisjunction.create();
		
		for(ViewInstanceJoin item : conjunctions) {
			Op tmp = new OpViewInstanceJoin(item);
			result.add(tmp);
		}
		*/
		
		return result;
		
		//return new OpRdfUnionViewPattern(conjunctions);
	}

	
	/**
	 * 
	 * TODO FIX THIS
	 * @param list
	 * @return
	 */
	public static <T extends IViewDef> boolean isSatisfiable(List<ViewInstance<T>> list)
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
		Ops type = Ops.valueOf(op.getClass().getSimpleName());
		Op result;
		
		switch(type) {

		case OpOrder:
			result = getApplicableViews((OpOrder)op, restrictions);
			break;
			
		case OpDistinct:
			result = getApplicableViews((OpDistinct)op, restrictions);
			break;

		case OpFilter:
			result = getApplicableViews((OpFilter)op, restrictions);
			break;

		case OpGroup:
			result = getApplicableViews((OpGroup)op, restrictions);
			break;
			
		case OpJoin:
			result = getApplicableViews((OpJoin)op, restrictions);
			break;

		case OpLeftJoin:
			result = getApplicableViews((OpLeftJoin)op, restrictions);
			break;

		case OpExtend:
			result = getApplicableViews((OpExtend)op, restrictions);
			break;			
			
		case OpQuadPattern2:
			result = getApplicableViews((OpQuadPattern2)op, restrictions);
			break;			
		
		case OpSlice:
			result = getApplicableViews((OpSlice)op, restrictions);
			break;
			
		case OpProject:
			result = getApplicableViews((OpProject)op, restrictions);
			break;

		case OpUnion:
			result = getApplicableViews((OpUnion)op, restrictions);
			break;
			
			
		default:
			throw new RuntimeException("Unknown op type: " + op.getClass());
		}
		
		return result;

		
		
		//return MultiMethod.invoke(this, "getApplicableViews", op, restrictions);
	}

	public Op getApplicableViews(OpSequence op, RestrictionManagerImpl restrictions) {
		List<Op> members = op.getElements();
		
		List<Op> newMembers = new ArrayList<Op>(members.size());
		for(Op member : members) {
			Op newMember = _getApplicableViews(member, restrictions);
			newMembers.add(newMember);
		}
				
		Op result = OpSequence.create().copy(newMembers);
		return result;
	}


	public Op getApplicableViews(OpTopN op, RestrictionManagerImpl restrictions) {
		Op subOp = _getApplicableViews(op.getSubOp(), restrictions);
		
		Op result = new OpTopN(subOp, op.getLimit(), op.getConditions());
		return result;
	}
	
	public Op getApplicableViews(OpDisjunction op, RestrictionManagerImpl restrictions) {
		List<Op> members = op.getElements();
		
		List<Op> newMembers = new ArrayList<Op>(members.size());
		for(Op member : members) {
			Op newMember = _getApplicableViews(member, restrictions);
			newMembers.add(newMember);
		}
				
		Op result = OpDisjunction.create().copy(newMembers);
		return result;
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
	 * Extend defines now variables and is therefore similar to a VarDefinition.
	 * 
	 * 
	 * Below is outdated.
	 * 
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
		Op result = processOpExtend(op.getSubOp(), op.getVarExprList(), _restrictions);
		return result;
	}
	
	public Op getApplicableViews(OpAssign op, RestrictionManagerImpl _restrictions) {
		Op result = processOpExtend(op.getSubOp(), op.getVarExprList(), _restrictions);
		return result;
	}
	
	
	public Op processOpExtend(Op subOp, VarExprList varExprs, RestrictionManagerImpl _restrictions) {

		Op newSubOp = _getApplicableViews(subOp, _restrictions);
		Op result = OpExtend.extend(newSubOp, varExprs);
		//Op result = op.copy(subOp);
			
		return result;

		// Outdated code:
//		RestrictionManagerImpl restrictions = new RestrictionManagerImpl(_restrictions);
//		
//		for(Var var : op.getVarExprList().getVars()) {
//			Expr expr = op.getVarExprList().getExpr(var);
//			
//			Expr item = new E_Equals(new ExprVar(var), expr);
//			restrictions.stateExpr(item);
//		}
//
//		
//		//return _getApplicableViews(OpFilterIndexed.filter(restrictions, op.getSubOp()), restrictions);
//		Op filter =  OpFilterIndexed.filter(restrictions, _getApplicableViews(op.getSubOp(), restrictions));
//		
//		Op result = op.copy(filter);
//		return result;
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
		
		RestrictionManagerImpl combinedRestrictions = new RestrictionManagerImpl(restrictions);
		
		// FIXME For filters we create an empty subRestrictions, but we pass on the combined restrictions for
		// optimizing the lookups
		// What I want to say is: We only keep track of the restrictions in order optimizing the lookups of candidate views
		// We do not optimize filter placement here 
		RestrictionManagerImpl subRestrictions = new RestrictionManagerImpl();
		
		for(Expr expr : op.getExprs()) {
			subRestrictions.stateExpr(expr);
			combinedRestrictions.stateExpr(expr);
		}
		
		Op newSubOp = _getApplicableViews(op.getSubOp(), combinedRestrictions);
		Op result = OpFilterIndexed.filter(subRestrictions, newSubOp);		
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
	    // TODO Restrictions from the left hand side should carry over to the right hand side
	    // Even better, if constraints on both sides were equally respected
	    
	    Op result = processJoinSplitLhs(op.getLeft(), op.getRight(), null, restrictions, false);
	    return result;
	    
		//return OpJoin.create(_getApplicableViews(op.getLeft(), restrictions), _getApplicableViews(op.getRight(), restrictions));
	}

	
	/**
	 * Create a new retrictions manager, where "bound" retrictions are removed 
	 * 
	 * @param restrictions
	 * @return
	 */
	public static RestrictionManagerImpl filterRestrictionsBound(RestrictionManagerImpl restrictions) {
		RestrictionManagerImpl result = new RestrictionManagerImpl();
		
		if(restrictions == null || restrictions.getCnf() == null) {
			logger.warn("Restrictions were null here - not sure if this should happen");
		}
		
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
		Op result = processLeftJoin(op.getLeft(), op.getRight(), op.getExprs(), restrictions);
		return result;
	}

	public Op getApplicableViews(OpConditional op, RestrictionManagerImpl restrictions) {
		Op result = processLeftJoin(op.getLeft(), op.getRight(), null, restrictions);
		
		//OpLeftJoin tmp = 
		
		//Op result = new OpConditional(tmp.getLeft(), tmp.getRight());
		
		return result;

	}

	
	// Seems to be working now
	public Op processLeftJoin(Op left, Op right, Iterable<Expr> exprs, RestrictionManagerImpl restrictions)
	{
		Op result = processJoinSplitLhs(left, right, exprs, restrictions, true);
		//Op result = processLeftJoinDirect(left, right, exprs, restrictions);
		return result;
	}
	
	public Op processJoinSplitLhs(Op left, Op right, Iterable<Expr> exprs, RestrictionManagerImpl restrictions, boolean isLeftJoin)
	{
		FilterSplit filterSplit = FilterPlacementOptimizer2.splitFilter(left, restrictions);
		RestrictionManagerImpl leftRestrictions = filterSplit.getPushable();
		
		
		Op newLeft = _getApplicableViews(left, filterSplit.getPushable());

		//System.out.println("so far so good\n" + newLeft);
		
		newLeft = FilterPlacementOptimizer2.optimize(newLeft, leftRestrictions);

		
		//System.out.println("so far so good\n" + newLeft);
		
		List<Op> members;
		
		if(newLeft instanceof OpDisjunction) {
			
			OpDisjunction union = (OpDisjunction)newLeft;

			members = union.getElements();
			
		} else {
			members = Collections.singletonList(newLeft);
		}
		
		
		OpDisjunction newUnion = OpDisjunction.create();
		
		for(Op member : members) {
				
				//System.out.println("Member: " + member);
			
//				RestrictionManagerImpl subRestrictions = filterRestrictionsBound(leftRestrictions);
//				RestrictionManagerImpl tmp = getRestrictions2(member);
//				RestrictionManagerImpl moreRestrictions = filterRestrictionsBound(tmp);

				RestrictionManagerImpl subRestrictions = new RestrictionManagerImpl(leftRestrictions);
				//RestrictionManagerImpl subRestrictions = new RestrictionManagerImpl(restrictions);
				RestrictionManagerImpl tmp = getRestrictions2(member);
				if(tmp != null) {
				    subRestrictions.stateRestriction(tmp);
				}
				
				/*
				if(moreRestrictions != null) {
					/*
					for(Clause clause : moreRestrictions.getCnf()) {
						if(!FilterPlacementOptimizer2.doesClauseContainBoundExpr(clause)) {
							// FIXME Creating a new NormalForm for each clause is somewhat overkill
							subRestrictions.stateCnf(new NestedNormalForm(Collections.singleton(clause)));
						}
					}* /
					
					
					subRestrictions.stateRestriction(moreRestrictions);
				}
				*/
				
				if(exprs != null) {
					for(Expr expr : exprs) {
						subRestrictions.stateExpr(expr);
					}
				}

				FilterSplit rsplit = FilterPlacementOptimizer2.splitFilter(right, subRestrictions);
				
				RestrictionManagerImpl rclauses = rsplit.getPushable();
				Op newRight = _getApplicableViews(right, rclauses);

				

				ExprList joinExprs = new ExprList();

				Op item;
				if(isLeftJoin) {
                    if(!rsplit.getNonPushable().getCnf().isEmpty()) {
                        joinExprs.addAll(rsplit.getNonPushable().getExprs());
                    }


				
                    item = (OpLeftJoin)OpLeftJoin.create(member, newRight, joinExprs);
				}
				else {
				    item = OpJoin.create(member, newRight);
                    item = new OpFilterIndexed(item, rsplit.getNonPushable());
				}
				
				if(!filterSplit.getNonPushable().getCnf().isEmpty()) {
					item = new OpFilterIndexed(item, filterSplit.getNonPushable());
				}


				newUnion.add(item);
				
				
				

				//System.out.println("Member item:\n" + item);
				//System.out.println("-------");
		}
		
		
		List<Op> elements = newUnion.getElements();
		
		Op result;
		if(newUnion.size() == 1) {
			result = elements.iterator().next();
		} else {
			result = newUnion;
		}
				
		//logger.debug("Left join candidates:\n" + result);
		//System.out.println("Left join candidates:\n" + result);
		
		return result;
	}
	
	
	public OpLeftJoin processLeftJoinDirect(Op left, Op right, Iterable<Expr> exprs, RestrictionManagerImpl restrictions)
	{
		Op newLeft = _getApplicableViews(left, restrictions);
		
		//List<RestrictionManager> moreRestrictions = getRestrictions(left);

		RestrictionManagerImpl subRestrictions = filterRestrictionsBound(restrictions);//new RestrictionManager(restrictions);

		RestrictionManagerImpl moreRestrictions = filterRestrictionsBound(getRestrictions2(newLeft));
		
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
		
		if(exprs != null) {
			for(Expr expr : exprs) {
				subRestrictions.stateExpr(expr);
			}
		}
		
		
		//RestrictionManager union = RestrictionManager.createUnion(moreRestrictions);
		
		//System.out.println(union);
		
		
		
		Op newRight = _getApplicableViews(right, subRestrictions);
		
		OpLeftJoin result = (OpLeftJoin)OpLeftJoin.create(newLeft, newRight, new ExprList());
		return result;
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
			
			OpFilterIndexed opFilter = (OpFilterIndexed)op;
			RestrictionManagerImpl filterRestrictions = opFilter.getRestrictions(); 
			RestrictionManagerImpl subRestrictions = getRestrictions2(opFilter.getSubOp());
			
			RestrictionManagerImpl result;
			if(subRestrictions == null) {
				result = new RestrictionManagerImpl();
				//result = new RestrictionManagerImpl(subRestrictions);
			} else {
				result = new RestrictionManagerImpl(subRestrictions);
			}

			result.stateRestriction(filterRestrictions);

			return result;
			//return ((OpFilterIndexed) op).getRestrictions();
		} else if(op instanceof Op1) {
			return getRestrictions2(((Op1) op).getSubOp());
		} else if(op instanceof OpJoin) {
			throw new RuntimeException("TODO Merge the restrictions of both sides of the join");
		} else if(op instanceof OpLeftJoin) {
			return getRestrictions2(((OpLeftJoin) op).getLeft());
		} else if(op instanceof OpConditional) {
			return getRestrictions2(((OpConditional)op).getLeft());			
		} else if(op instanceof OpDisjunction) {
			return null; // TODO We could factor out restrictions common to all elements
		} else if(op instanceof OpViewInstanceJoin) {
			OpViewInstanceJoin opPattern = (OpViewInstanceJoin)op;
			return opPattern.getJoin().getRestrictions();
		} else if(op instanceof OpMapping) {
			OpMapping opMapping = (OpMapping)op;
			return opMapping.getRestrictions();
		} else {
			throw new RuntimeException("Should not happen: Unhandled Op: " + op.getClass() + " --- "+ op);
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
	public Collection<T> getViews() {
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


