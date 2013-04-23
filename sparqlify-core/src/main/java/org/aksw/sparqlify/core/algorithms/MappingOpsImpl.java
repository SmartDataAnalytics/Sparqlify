package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprAggregator;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Agg;
import org.aksw.sparqlify.algebra.sql.exprs2.S_AggCount;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.nodes.Projection;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpDistinct;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpExtend;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpFilter;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpGroupBy;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpProject;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpRename;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSlice;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;
import org.aksw.sparqlify.core.ArgExpr;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlTypeMapper;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.VarDefinition;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.aksw.sparqlify.expr.util.NodeValueUtils;
import org.aksw.sparqlify.restriction.RestrictionSetImpl;
import org.aksw.sparqlify.trash.ExprCommonFactor;
import org.aksw.sparqlify.trash.ExprCopy;
import org.aksw.sparqlify.util.SqlTranslatorImpl2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCount;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;
import com.hp.hpl.jena.vocabulary.XSD;


class SqlExprContext {
	private Map<Var, Expr> assignment;
	private SqlExpr sqlExpr;
	
	public SqlExprContext(Map<Var, Expr> assignment, SqlExpr sqlExpr) {
		super();
		this.assignment = assignment;
		this.sqlExpr = sqlExpr;
	}
	public Map<Var, Expr> getAssignment() {
		return assignment;
	}
	
	public SqlExpr getSqlExpr() {
		return sqlExpr;
	}
	
	@Override
	public String toString() {
		return "SqlExprContext [assignment=" + assignment + ", sqlExpr="
				+ sqlExpr + "]";
	}
}


/**
 * Maybe at some point we want to externalize some state of the rewriting,
 * such as column names generators.
 * 
 * I leave this class here for now as a reminder to myself.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
class RewriteContext {
	
}



// Helper class
class VarDefKey {		
	Set<SqlExpr> constraintExpr = new HashSet<SqlExpr>();
	Set<RestrictedExpr> definitionExprs = new HashSet<RestrictedExpr>();
	
	VarDefKey() { }

	@Override
	public String toString() {
		return "VarDefKey [constraintExpr=" + constraintExpr
				+ ", definitionExprs=" + definitionExprs + "]";
	}
	
	
	/*
	VarDefKey(Set<Expr> constraintExpr, Set<RestrictedExpr> definitionExprs) {
		this.constraintExpr = constraintExpr;
		this.definitionExprs = definitionExprs;
	}*/
}

/*
 * Some thoughts:
 * 
 * The original SqlNode was (sparqlVarToExpr, aliasToColumn, [args])
 * 
 * Now we have the new mapping opject which is (sparqlVarToExpr, sqlNode) whereas sparqLVarToExpr is now referred to as varDefinition
 * 
 * So we are missing the aliasToColumn mapping.
 * 
 * The question is, whether this should be realized with the injection of SQL projections operators.
 * I think my conclusion is yes: The rename operation injects an appropriate SQL projection (which should optimize away
 * later - such as multiple subsequent projections)
 *
 *
 */




public class MappingOpsImpl
	implements MappingOps
{

	private static final Logger logger = LoggerFactory.getLogger(MappingOpsImpl.class);

	/**
	 * The exprEvaluator contains an transformer that rewrites expressions in a way that they can be converted to SQL.
	 * Specifically, it tries to get rid of RDF term constructors.
	 * 
	 * TODO Better separate rewrite and partial evaluation.
	 */
	//private ExprEvaluator exprTransformer;
	

	/**
	 * After the transformation, the translater actually converts the expression to SQL. 
	 */
	private SqlTranslator sqlTranslator;

	
	//private DatatypeSystem datatypeSystem;
	private ExprDatatypeNorm exprNormalizer;
	

	public MappingOpsImpl(SqlTranslator sqlTranslator, ExprDatatypeNorm exprNormalizer) {//DatatypeAssigner datatypeAssigner) {
		//this.exprTransformer = exprTransformer;
		this.sqlTranslator = sqlTranslator;
		this.exprNormalizer = exprNormalizer;
		//this.exprNormalizer = new ExprDatatypeNorm(datatypeSystem)
	}

	/*
	public MappingOpsImpl(ExprDatatypeNorm exprNormalizer) {
		this.exprNormalizer = exprNormalizer;
	}
	*/
	
	public Mapping rename(Mapping a, Map<String, String> rename)
	{
		//SqlNode
		// Return the original mapping if there is nothing to rename
		//a.getSqlNode().get
		if(rename.isEmpty()) {
			return a;
		}
		
		VarDefinition renamedVarDef = VarDefinition.copyRename(a.getVarDefinition(), rename);
	
		SqlOpRename sqlOpRename = SqlOpRename.create(a.getSqlOp(), rename);
		
		Mapping result = new Mapping(renamedVarDef, sqlOpRename);
		
		return result;
	}

	

	public static SqlExpr translateSql(RestrictedExpr restExpr, Map<String, TypeToken> typeMap, SqlTranslator sqlTranslator) {
		if(restExpr.getRestrictions().isUnsatisfiable()) {
			//return SqlExprValue.FALSE;
			return S_Constant.FALSE;
		}
		
		Expr expr = restExpr.getExpr();
		SqlExpr result = translateSql(expr, typeMap, sqlTranslator);
		return result;
	}
	
	public static SqlExpr translateSql(Expr expr, Map<String, TypeToken> typeMap, SqlTranslator sqlTranslator) {
//		
//		Expr transformed;
//		if(exprTransformer != null && expr.isFunction()) {
//			transformed = exprTransformer.eval(expr.getFunction(), null);
//		} else {
//			transformed = expr;
//		}
//		
//		System.out.println("Transformed: " + transformed);
		ExprSqlRewrite exprRewrite = sqlTranslator.translate(expr, null, typeMap);
		SqlExpr result = SqlTranslatorImpl2.asSqlExpr(exprRewrite);
		
		return result;
	}

	

	/*
	public static Expr createSqlCondidion(Expr sparqlCondition, VarDefinition defExpr) {
	}*/	
	

	
//	public static SqlExpr createSqlExpr(Expr expr, Map<String, TypeToken> typeMap, SqlTranslator sqlTranslator) {
//		ExprSqlRewrite exprRewrite = sqlTranslator.translate(expr, null, typeMap);
//		SqlExpr sqlExpr = SqlTranslatorImpl2.asSqlExpr(exprRewrite);
//		
//		return sqlExpr;
//	}
	
	/**
	 * Creates an SQL condidion given a (SPARQL query filter) condition and a set of variable definitions.
	 * 
	 * The resulting expression takes alternatives into account:
	 * 
	 * Given for example:
	 * ?s -> {sa1, ..., san}
	 * ?o -> {so1}
	 * 
	 * ?o must hold independently of ?s
	 * 
	 * 
	 */
	
	
	/**
	 * Returns a list of Binding-Sql Expr pairs.
	 * 
	 * List<{Map<Var, RestExpr>, SqlExpr} 
	 * 
	 * 
	 * @param condition
	 * @param varDef
	 * @param typeMap
	 * @param sqlTranslator
	 * @return
	 */	
	public static List<SqlExprContext> createSqlExprs(Expr condition, VarDefinition varDef, Map<String, TypeToken> typeMap, SqlTranslator sqlTranslator) {
		List<SqlExprContext> result = new ArrayList<SqlExprContext>();

		Set<Var> conditionVars = condition.getVarsMentioned();
		
		// Common variables of the condition and the varDef
		Set<Var> cVars = conditionVars;
		cVars.retainAll(varDef.getMap().keySet());
		List<Var> commonVars = new ArrayList<Var>(cVars);

		// If the condition and the varDef have no variables in common,
		// we still need to consider the constants, such as FALSE and TRUE.
		if(commonVars.isEmpty()) {
			//SqlExpr sqlExpr = sqlTranslator.translate(condition, null, typeMap);
			ExprSqlRewrite exprRewrite = sqlTranslator.translate(condition, null, typeMap);
			SqlExpr sqlExpr = SqlTranslatorImpl2.asSqlExpr(exprRewrite);

			
			Map<Var, Expr> assignment = new HashMap<Var, Expr>();
			SqlExprContext tmp = new SqlExprContext(assignment, sqlExpr);

			result.add(tmp);
			
			return result;
		}
		
		// Sort the variable definitions by number of alternatives
		final Map<Var, Collection<RestrictedExpr>> map = varDef.getMap().asMap();
		Collections.sort(commonVars, new Comparator<Var>() {
			@Override
			public int compare(Var a, Var b) {
				return map.get(a).size() - map.get(b).size();
			}
		});


		// Evaluate the expression for all possible combinations of variable assignments
		
		List<Collection<RestrictedExpr>> assignments = new ArrayList<Collection<RestrictedExpr>>(commonVars.size());
		for(Var var : commonVars) {
			assignments.add(map.get(var));
		}

		CartesianProduct<RestrictedExpr> cart = CartesianProduct.create(assignments);
		
		//List<SqlExpr> ors = new ArrayList<SqlExpr>();
		for(List<RestrictedExpr> item : cart) {
			Map<Var, Expr> assignment = new HashMap<Var, Expr>(commonVars.size());
			for(int i = 0; i < commonVars.size(); ++i) {
				Var var = commonVars.get(i);
				
//				if(var.equals(Var.alloc("var_2"))) {
//					System.out.println("here");
//				}
				
				Expr expr = item.get(i).getExpr();
				
				assignment.put(var, expr);
			}
						
			//Expr expr = exprTransformer.eval(condition, assignment);
			
			//SqlExpr sqlExpr = sqlTranslator.translate(expr, null, typeMap);
			ExprSqlRewrite exprRewrite = sqlTranslator.translate(condition, assignment, typeMap);
			SqlExpr sqlExpr = SqlTranslatorImpl2.asSqlExpr(exprRewrite);

			
			SqlExprContext tmp = new SqlExprContext(assignment, sqlExpr);
			result.add(tmp);
		}
		
		return result;
		
	}

	public static SqlExpr createSqlCondition(Expr condition, VarDefinition varDef, Map<String, TypeToken> typeMap, SqlTranslator sqlTranslator) {
		List<SqlExprContext> items = createSqlExprs(condition, varDef, typeMap, sqlTranslator);
		
		List<SqlExpr> ors = new ArrayList<SqlExpr>();
		for(SqlExprContext item : items) {
			SqlExpr sqlExpr = item.getSqlExpr();
			
			if(sqlExpr.equals(S_Constant.TRUE)) {
				return S_Constant.TRUE;
			}
			
			if(sqlExpr.equals(S_Constant.FALSE)) {
				continue;
			}

			ors.add(sqlExpr);			
		}
		
		SqlExpr result = SqlExprUtils.orifyBalanced(ors);


		if(result == null) {
			//throw new NullPointerException();
			result = S_Constant.FALSE;
		}

		
		assert result != null : "Null Pointer Exception";
		
		return result;
	}
	
	@Deprecated // The new method is more nicely structured
	public static SqlExpr createSqlConditionOld(Expr condition, VarDefinition varDef, Map<String, TypeToken> typeMap, SqlTranslator sqlTranslator) {
		
		
		Set<Var> conditionVars = condition.getVarsMentioned();
		
		// Common variables of the condition and the varDef
		Set<Var> cVars = conditionVars;
		cVars.retainAll(varDef.getMap().keySet());
		List<Var> commonVars = new ArrayList<Var>(cVars);

		// If the condition and the varDef have no variables in common,
		// we still need to consider the constants, such as FALSE and TRUE.
		if(commonVars.isEmpty()) {
			//SqlExpr sqlExpr = sqlTranslator.translate(condition, null, typeMap);
			ExprSqlRewrite exprRewrite = sqlTranslator.translate(condition, null, typeMap);
			SqlExpr sqlExpr = SqlTranslatorImpl2.asSqlExpr(exprRewrite);

			
			//return S_Constant.TRUE;
			return sqlExpr;
		}
		
		// Sort the variable definitions by number of alternatives
		final Map<Var, Collection<RestrictedExpr>> map = varDef.getMap().asMap();
		Collections.sort(commonVars, new Comparator<Var>() {
			@Override
			public int compare(Var a, Var b) {
				return map.get(a).size() - map.get(b).size();
			}
		});


		// Evaluate the expression for all possible combinations of variable assignments
		
		List<Collection<RestrictedExpr>> assignments = new ArrayList<Collection<RestrictedExpr>>(commonVars.size());
		for(Var var : commonVars) {
			assignments.add(map.get(var));
		}

		CartesianProduct<RestrictedExpr> cart = CartesianProduct.create(assignments);
		
		List<SqlExpr> ors = new ArrayList<SqlExpr>();

		Map<Var, Expr> assignment = new HashMap<Var, Expr>(commonVars.size());
		for(List<RestrictedExpr> item : cart) {
			for(int i = 0; i < commonVars.size(); ++i) {
				Var var = commonVars.get(i);
				
//				if(var.equals(Var.alloc("var_2"))) {
//					System.out.println("here");
//				}
				
				Expr expr = item.get(i).getExpr();
				
				assignment.put(var, expr);
			}
						
			//Expr expr = exprTransformer.eval(condition, assignment);
			
			//SqlExpr sqlExpr = sqlTranslator.translate(expr, null, typeMap);
			ExprSqlRewrite exprRewrite = sqlTranslator.translate(condition, assignment, typeMap);
			SqlExpr sqlExpr = SqlTranslatorImpl2.asSqlExpr(exprRewrite);

			
			if(sqlExpr.equals(S_Constant.TRUE)) {
				return S_Constant.TRUE;
			}
			
			if(sqlExpr.equals(S_Constant.FALSE)) {
				continue;
			}

			ors.add(sqlExpr);
		}

		/*
		if(ors.isEmpty()) {
			
		}*/
		
		
		SqlExpr result = SqlExprUtils.orifyBalanced(ors);


		if(result == null) {
			//throw new NullPointerException();
			result = S_Constant.FALSE;
		}

		
		assert result != null : "Null Pointer Exception";
		
		return result;
	}
	
	

	/**
	 * Joins two var definitions on the given queryVar.
	 * 
	 * @param queryVar
	 * @param a
	 * @param b
	 * @param sqlTranslator
	 * @return
	 */
	public static VarDefKey joinDefinitionsOnEquals(
			Collection<RestrictedExpr> a,
			Collection<RestrictedExpr> b,
			Map<String, TypeToken> typeMap,
			//ExprEvaluator exprTransformer,
			SqlTranslator sqlTranslator
			)
	{
		VarDefKey result = new VarDefKey();
		
		result.definitionExprs.addAll(a);
				
		//boolean isPickDefSatisfiable = defs.isEmpty();
		for(RestrictedExpr rexprA : result.definitionExprs) {
			Set<RestrictedExpr> newRexprsA = new HashSet<RestrictedExpr>();

			// The restrictions that apply to the picked variable.
			// initialized only if there are no defs (init means true, i.e. no restriction)
			RestrictionSetImpl varRestrictions = b.isEmpty() ? new RestrictionSetImpl() : null;

			
			for(RestrictedExpr rexprB : b) {
				
				
				RestrictedExpr vdEquals = VariableDefinitionOps.equals(rexprA, rexprB);
				
				SqlExpr sqlExpr = translateSql(vdEquals, typeMap, sqlTranslator);
				
				if(sqlExpr.equals(S_Constant.FALSE)) {
					continue;
				}

				if(varRestrictions == null) {
					varRestrictions = new RestrictionSetImpl(); 
				}
				
				varRestrictions.addAlternatives(vdEquals.getRestrictions());

				
				result.constraintExpr.add(sqlExpr);
			}

			// Only if there is a non-unsatisfiable restriction...
			if(varRestrictions != null) {
				RestrictedExpr newRestrictedExpr = new RestrictedExpr(rexprA.getExpr(), varRestrictions);
				newRexprsA.add(newRestrictedExpr);
			}
			
			result.definitionExprs = newRexprsA;
		}
		
		if(result.definitionExprs.isEmpty()) {
			// All defining expressions resulted in unsatisfiable joins - return null to indicate this
			return null;
			//result.constraintExpr = null;
		} else {		
			return result;
		}
	}

	
	/**
	 * Returns a set of equals expressions,
	 * created from equating all pickDefs to the given varDefs
	 * 
	 * The set is intended to be interpreted as a disjunction between the elements.
	 * Empty set therefore indicates "no constraint"
	 * null indicates "unsatisfiable constraint"
	 * 
	 * 
	 * @param queryVar
	 * @param viewInstance
	 * @return
	 */
	public static VarDefKey joinDefinitionsOnEquals(
			Var queryVar,
			ViewInstance<ViewDefinition> viewInstance,
			//ExprEvaluator exprTransformer,
			SqlTranslator sqlTranslator
			)
	{
		Map<String, TypeToken> typeMap = viewInstance.getViewDefinition().getMapping().getSqlOp().getSchema().getTypeMap();

		//Set<Expr> result = new HashSet<Expr>();
		VarDefKey result = new VarDefKey();
			
		Set<Var> viewVars = viewInstance.getBinding().getViewVars(queryVar);
		
		// If the variable is unconstrained, then logically there is nothing todo
		if(viewVars.isEmpty()) {
			return result;
		}
		
		
		VarDefinition varDefinition = viewInstance.getVarDefinition();
		
		// Pick one of the view variables
		Var pickViewVar = viewVars.iterator().next();
		
				
		//Collection<RestrictedExpr> pickDefs = viewInstance.getDefinitionsForViewVariable(pickViewVar);
		
		
		// The pick-Restrictions may get further constrained with each additional variable
		//Collection<RestrictedExpr> nextPickDefs = new HashSet<RestrictedExpr>();
		result.definitionExprs.addAll(viewInstance.getDefinitionsForViewVariable(pickViewVar));
		
		
		for(Var viewVar : viewVars) {
			if(viewVar.equals(pickViewVar)) {
				continue;
			}
			
			Collection<RestrictedExpr> defs = varDefinition.getDefinitions(viewVar);

			
			// Intermediate result
			VarDefKey tmp = joinDefinitionsOnEquals(result.definitionExprs, defs, typeMap, sqlTranslator);
			
			//if(ExprEval.Type.FALSE == ExprEval.getDisjunctionType(tmp))

			if(tmp == null) {
				return null;
			}
			
			result.definitionExprs = tmp.definitionExprs;
			result.constraintExpr.addAll(tmp.constraintExpr);
		}
		
		if(result.definitionExprs.isEmpty()) {
			// All defining expressions resulted in unsatisfiable joins - return null to indicate this
			return null;
			//result.constraintExpr = null;
		} else {		
			return result;
		}

/*		
		if(result.isEmpty() && gotFalse) {
			// If we got no non-false expression, return null
			return null; 
		} else {
			return result;
		}
*/
	}
	
	/*
	public static SqlExpr pushDown(ExprList exprs) {
		SqlExprList result = new SqlExprList();
		
		for()
	}

	public static SqlExpr pushDown(Expr expr) {
		Expr pushed = PushDown.pushDownMM(expr);
		if(!(pushed instanceof ExprSqlBridge)) {
			throw new RuntimeException("Failed to push down '" + expr + "'");
		}
		SqlExpr result = ((ExprSqlBridge)pushed).getSqlExpr();

		return result;
	}
	*/

	/*
	public static SqlExpr translateSqlCnf(Set<Set<RestrictedExpr<Expr>>> cnf) {
		
	}
	*/
	public static Mapping createEmptyMapping() {
		SqlOp empty = SqlOpEmpty.create();
		Mapping result = new Mapping(empty);
		return result;
	}
	
	public static Mapping createEmptyMapping(ViewInstance<ViewDefinition> viewInstance) {
		SqlOp empty = SqlOpEmpty.create(viewInstance.getViewDefinition().getMapping().getSqlOp().getSchema());
		Mapping result = new Mapping(empty);
		return result;
	}
	
	/**
	 * Creates a mapping from a view instance:
	 * - For every binding variable, that maps to multiple things (contants + variables),
	 *   we need to equate all these things being mapped to
	 * 
	 * Schematically, we need to transform the binding to variable definitions from the original structure
	 * (query view definition)
	 *  q    v   d
	 *   
	 *         / dx1
	 *      ?x
	 *    /    \ dxn
	 * ?s  
	 *    \    / dy1
	 *      ?y
	 *         \ dyn
	 * 
	 * to conceptually a CNF:
	 * 
	 * (dx1 = dy1 OR ... OR dx1 = dyn) AND ... AND (dxn = dy1 OR ... OR dxn = dyn)
	 * 
	 * However, as equals is transitive and symmetric, it is sufficient to just pick
	 * the set of definitions for one view variable.
	 * 
	 * 
	 * 
	 * 
	 * @param generator
	 * @param viewInstance
	 * @return
	 */
	public Mapping createMapping(ViewInstance<ViewDefinition> viewInstance) {

		Set<Var> queryVars = viewInstance.getBinding().getQueryVars();
		
		Multimap<Var, RestrictedExpr> newVarDefMap = HashMultimap.create();
		
		List<SqlExpr> ands = null; 
		for(Var queryVar : queryVars) {

			Node constant = viewInstance.getBinding().getConstant(queryVar);
			if(constant != null) {
				NodeValue nv = NodeValue.makeNode(constant);
				newVarDefMap.put(queryVar, new RestrictedExpr(nv));
				
				continue;
			}

			
			VarDefKey ors = joinDefinitionsOnEquals(queryVar, viewInstance, sqlTranslator);
		
			if(ors == null) {

				return createEmptyMapping(viewInstance);
			}
			
			
			
			newVarDefMap.putAll(queryVar, ors.definitionExprs);
			
			SqlExpr or = SqlExprUtils.orifyBalanced(ors.constraintExpr);
			if(or == null || or.equals(S_Constant.TRUE)) {
				continue;
			} 

			// If ands is not null, it already indicates that it is not unconstrained
			if(ands == null) {
				 ands = new ArrayList<SqlExpr>();
			}

			if(or.equals(S_Constant.FALSE)) {
				continue;
			} else {
				ands.add(or);
			}
		}

		VarDefinition tmpVarDefinition = new VarDefinition(newVarDefMap);

		
		VarDefinition varDefinition = tmpVarDefinition.copyExpandConstants();
		
		Mapping result = null;
		SqlOp op = viewInstance.getViewDefinition().getMapping().getSqlOp();
		if(ands == null) { // Unconstrained
			 result = new Mapping(varDefinition, op);
		} else if(ands.isEmpty()) { // Unsatisfiable
			result = createEmptyMapping(viewInstance);
		} else { // Constrained.
			SqlOp filterOp = SqlOpFilter.create(op, ands);
			
			result = new Mapping(varDefinition, filterOp);
		}
		
		return result;
	}
	

	
	/**
	 * Returns a new mapping for b:
	 * 
	 * All column names that a and b have in commons are renamed in b.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public Mapping doJoinRename(Mapping a, Mapping b, Generator gen) {
		List<String> namesA = a.getSqlOp().getSchema().getColumnNames();
		List<String> namesB = b.getSqlOp().getSchema().getColumnNames();
		
		List<String> commonNames = new ArrayList<String>(namesB);
		commonNames.retainAll(namesA);
		
		if(commonNames.isEmpty()) {
			return b;
		}
		
		Map<String, String> rename = new HashMap<String, String>();
		for(String oldName : commonNames) {
			
			String newName = null;
			for(;;) {
				newName = gen.next();
				if(namesA.contains(newName)) {

					logger.trace("FIXME Have to generate another column name - should be prevented");
					continue;
				}
				
				break;
			}
			
			rename.put(oldName, newName);
		}
		
		VarDefinition renamedVarDef = VarDefinition.copyRename(b.getVarDefinition(), rename);
		SqlOpRename renamedOp = SqlOpRename.create(b.getSqlOp(), rename);
		
		Mapping result = new Mapping(renamedVarDef, renamedOp);
		
		return result;
	}
	
	public Mapping join(Mapping a, Mapping b) {
		Mapping result = joinCommon(a, b, false);
		return result;
	}
	
	public Mapping leftJoin(Mapping a, Mapping b) {
		Mapping result = joinCommon(a, b, true);
		return result;		
	}

	
	// TODO Handle left join
	public Mapping joinCommon(Mapping a, Mapping initB, boolean isLeftJoin) {

		Generator genSym = Gensym.create("h_");
		
		Mapping b = doJoinRename(a, initB, genSym);
		
		
		
		JoinType joinType = isLeftJoin ? JoinType.LEFT : JoinType.INNER;
		if(joinType == JoinType.LEFT) {
			logger.debug("Left Join encountered");
		}
		
		SqlOpJoin opJoin = SqlOpJoin.create(joinType, a.getSqlOp(), b.getSqlOp());
		
		Map<String, TypeToken> typeMap = opJoin.getSchema().getTypeMap();
		
		SqlOp opResult = opJoin;

		
		if(opJoin.getLeft().isEmpty() || (joinType.equals(JoinType.INNER) && opJoin.getRight().isEmpty())) {
			opResult = SqlOpEmpty.create(opJoin.getSchema());
		}
		
		VarDefinition vdA = a.getVarDefinition();
		VarDefinition vdB = b.getVarDefinition();
		
		Set<Var> varsA = vdA.getMap().keySet();
		Set<Var> varsB = vdB.getMap().keySet();
		
		// Create the join condidition for all common variables.
		// This is the reason calling this operation the "natural join"
		Set<Var> commonVars = new HashSet<Var>(varsA);
		commonVars.retainAll(varsB);
		
		Set<Var> varsAOnly = Sets.difference(varsA, commonVars);
		Set<Var> varsBOnly = Sets.difference(varsB, commonVars);
		
		Multimap<Var, RestrictedExpr> newVarDef = HashMultimap.create();
		
		// Add definitions of A only
		for(Var varA : varsAOnly) {
			Collection<RestrictedExpr> exprs = vdA.getMap().get(varA);
			newVarDef.putAll(varA, exprs);
		}

		// Add definitions of B only
		for(Var varB : varsBOnly) {
			Collection<RestrictedExpr> exprs = vdB.getMap().get(varB);
			newVarDef.putAll(varB, exprs);
		}

		// Add common definitions
		Set<SqlExpr> joinCondition = new HashSet<SqlExpr>();
		
		for(Var commonVar : commonVars) {
			Collection<RestrictedExpr> defsA = a.getVarDefinition().getDefinitions(commonVar);
			Collection<RestrictedExpr> defsB = b.getVarDefinition().getDefinitions(commonVar);
			
			VarDefKey tmp = joinDefinitionsOnEquals(defsA, defsB, typeMap, sqlTranslator);

			if(tmp == null) {
				opResult = SqlOpEmpty.create(opJoin.getSchema());
				break;
			}
			
			
			newVarDef.putAll(commonVar, tmp.definitionExprs);
			joinCondition.addAll(tmp.constraintExpr);
		}

		
		//ExprList jc = new ExprList(new ArrayList<Expr>(joinCondition));
		List<SqlExpr> jc = new ArrayList<SqlExpr>(joinCondition);
		SqlOpFilter opFilter = SqlOpFilter.create(opResult, jc);
		
		
		VarDefinition newVarDefinition = new VarDefinition(newVarDef);
		Mapping result = new Mapping(newVarDefinition, opFilter);

		return result;
	}

	
	
	@Override
	public Mapping slice(Mapping a, Long limit, Long offset) {
		
		SqlOpSlice opSlice = SqlOpSlice.create(a.getSqlOp(), offset, limit);
		
		Mapping result = new Mapping(a.getVarDefinition(), opSlice);

		return result;
	}


	public Mapping distinct(Mapping a) {
		SqlOp newOp = SqlOpDistinct.create(a.getSqlOp());
		Mapping result = new Mapping(a.getVarDefinition(), newOp);
		
		return result;
	}


	/**
	 * Removes variables from the definitions.
	 * Also projects away unreferenced columns in the sqlOp.
	 * 
	 */
	@Override
	public Mapping project(Mapping a, List<Var> vars) {		
				
		//List<String> referencedColumns = new HashSet<String>();
		List<String> referencedColumns = new ArrayList<String>();
		
		// Track all columns that contribute to the construction of SQL variables
		for(Var var : vars) {
			for(RestrictedExpr def : a.getVarDefinition().getDefinitions(var)) {
				for(Var item : def.getExpr().getVarsMentioned()) {
					
					String itemName = item.getName();
					if(!referencedColumns.contains(itemName)) {
						referencedColumns.add(itemName);
					}
				}
			}
		}
		
		
		SqlOpProject sqlOp = SqlOpProject.create(a.getSqlOp(), referencedColumns);
		VarDefinition newVarDef = a.getVarDefinition().copyProject(vars);		
	
		Mapping result = new Mapping(newVarDef, sqlOp);
		
		return result;
	}

	
	public ExprList compactConjuction(ExprList exprs) {
		ExprList result = new ExprList();
		
		for(Expr expr : exprs) {
			if(expr.isConstant()) {
				NodeValue nv = expr.getConstant();
				
				if(NodeValue.FALSE.equals(nv)) {
					// FIXME Optimize away the creation of a new list
					result = new ExprList();
					result.add(NodeValue.FALSE);
					return result;
				} else if(NodeValue.TRUE.equals(nv)) {
					continue;
				}
				
				result.add(expr);
			}
		}
		
		
		return result;
	}

	@Override
	public Mapping filter(Mapping a, ExprList exprs) {
		Map<String, TypeToken> typeMap = a.getSqlOp().getSchema().getTypeMap();
		
		List<SqlExpr> sqlExprs = new ArrayList<SqlExpr>();
		for(Expr expr : exprs) {
			
			// Replace any variables in the expression with the variable definitions			
			SqlExpr sqlExpr = createSqlCondition(expr, a.getVarDefinition(), typeMap, sqlTranslator);
			if(sqlExpr.equals(S_Constant.TRUE)) {
				continue;
			}

			sqlExprs.add(sqlExpr);
		}

		SqlOp op = SqlOpFilter.createIfNeeded(a.getSqlOp(), sqlExprs);
		
		Mapping result = new Mapping(a.getVarDefinition(), op);
		
		return result;
	}

	
	
//	/*
//	 * Two helper functions used for grouping: one assign blocking keys,
//	 * the other 
//	 */
//	// FIXME Above is not used yet
//	Transformer<RestrictedExpr, Integer> blockingKeyTransformer = new Transformer<RestrictedExpr, Integer>() {
//		@Override
//		public Integer transform(RestrictedExpr input) {
//			// TODO Auto-generated method stub
//			return 0;
//		}			
//	};

	
	public Mapping unionIfNeeded(List<Mapping> members) {
		Mapping result;
		if(members.size() == 1) {
			result = members.iterator().next();
		} else {
			result = union(members);
		}
		
		return result;
	}

	public static List<Mapping> removeEmptyMembers(List<Mapping> mappings) {
		List<Mapping> result = new ArrayList<Mapping>(mappings.size());
		for(Mapping mapping : mappings) {
			if(!mapping.isEmpty()) {
				result.add(mapping);
			}
		}
		return result;
	}

	public static List<Mapping> pushConstants(List<Mapping> mappings) {
		List<Mapping> result = new ArrayList<Mapping>(mappings.size());
		for(Mapping mapping : mappings) {
			Mapping newMapping = pushConstants(mapping);
			
			result.add(newMapping);
		}
		return result;
	}


	/**
	 * Pushes all constants of a given mapping's varDefinition into the SQL.
	 * Used for creating union's of mappings: By pushing variables into the SQL we do not
	 * have to deal with disjoint unions (i.e. unions with discriminator columns) in order to
	 * know which union member yields the constant. 
	 * 
	 * 
	 * @param mapping The mapping to process. 
	 * @return A new mapping with all constants pushed.
	 */
	public static Mapping pushConstants(Mapping mapping) {
		VarDefinition varDef = mapping.getVarDefinition();
		//Multimap<Var, RestrictedExpr> map = varDef.getMap();
		

		Set<String> columnNameBlacklist = new HashSet<String>(mapping.getSqlOp().getSchema().getColumnNames());
		Generator aliasGen = GeneratorBlacklist.create("C", columnNameBlacklist);

		Projection projection = new Projection();

		
		VarDefinition newVarDef = new VarDefinition();
		
		for(Entry<Var, Collection<RestrictedExpr>> entry : varDef.getMap().asMap().entrySet()) {
			
			Var var = entry.getKey();
			Collection<RestrictedExpr> restExprs = entry.getValue();
			
			
			for(RestrictedExpr restExpr : restExprs) {
				
				Expr newExpr;
				
				Expr expr = restExpr.getExpr();
				
				
				// The intention here is to obtain the quadruple representation of RDF terms.
				// Independent of whether it is a direct constant or a function that can be
				// evaluated to one.
				
				// The following distinguishes between e.g.
				// <http://ex.org/Amenity> and
				// E_RdfTerm(1, "http://ex.org/Amenity", "", "")
				E_RdfTerm rdfTerm;
				if(expr.isConstant()) {
					rdfTerm = SqlTranslationUtils.expandConstant(expr);
				} else {
					rdfTerm = SqlTranslationUtils.expandRdfTerm(expr); // Does not expand constants, hence the if statement 
				}
				
				
				// TODO THIS SEEMS BROKEN
				boolean isConstantArgsOnly = ExprUtils.isConstantArgsOnly(rdfTerm);
				if(isConstantArgsOnly) {
					// Obtain the node value of the constant 
					
					NodeValue constant = expr.eval(null, null); //expr.getConstant();

					String alias = aliasGen.next();

					

					// TODO Get the proper SQL literal for the constant
					String str = "" + NodeValueUtils.getValue(constant);
					S_Constant sqlExpr = new S_Constant(new SqlValue(TypeToken.String, str));
					projection.put(alias, sqlExpr);
					
					ExprVar exprVar = new ExprVar(alias);


					ExprList exprs = new ExprList();
					List<Expr> args = rdfTerm.getArgs();
					for(int j = 0; j < args.size(); ++j) {
												
						Expr e;
						if(j == 1) {
							e = exprVar;
						} else {
							e = args.get(j);
						}						
					
						exprs.add(e);
					}
					newExpr = ExprCopy.getInstance().copy(expr, exprs);
					
				}
				else {
					newExpr = expr;
				}
				
				RestrictedExpr newRestExpr = new RestrictedExpr(newExpr, restExpr.getRestrictions());
				
				newVarDef.getMap().put(var, newRestExpr);
			}
		}

		Mapping result;
		if(projection.isEmpty()) {
			result = mapping;
		} else {
			SqlOp sqlOp = mapping.getSqlOp();
			
			SqlOp newSqlOp = SqlOpExtend.create(sqlOp, projection);
			
			result = new Mapping(newVarDef, newSqlOp);
		}
	
		return result;
	}
	
	/**
	 * 
	 * [    null,  h: int, i: string,   null ]
	 * 
	 * 
	 * [ a: int      null,      null, x: geo ]
	 * 
	 * ?s = uri(concat("http://...", ?c1, ?c2));
	 * 
	 * 
	 * ?s -> [c1: string, c2: int] with expr foo and restriction bar.
	 * 
	 */
	public Mapping union(List<Mapping> members) {

		//Remove all members that do not yield results 		
		members = removeEmptyMembers(members);

		if(members.isEmpty()) {
			Mapping result = createEmptyMapping();
			return result;
		}

		// Make any constants part of the sql table
		members = pushConstants(members);
		
		
		// Create a column blacklist so that we do not end up extending member projections with column
		// names that they already have
		Set<String> columnNameBlacklist = new HashSet<String>();
		for(Mapping member : members) {
			List<String> memberColumnNames = member.getSqlOp().getSchema().getColumnNames();
			columnNameBlacklist.addAll(memberColumnNames);
		}

		//List<Mapping> members = rawMembers;
		
		
		/* TODO The alias generator must be configurable
		 * Example:
		 *     PostgreSQL works fine with lower case aliases,
		 *     H2 fails with them
		 */
		//Generator aliasGen = Gensym.create("C");
		Generator aliasGen = GeneratorBlacklist.create("C", columnNameBlacklist);

		
		if(members.size() == 1) {
			logger.warn("Single member union - should be avoided");
			Mapping result = members.get(0);
			return result;			
		}
		
		
		//public Mapping union(List<Mapping> members) {
		// Prepare the data structures from which the
		// result node will be created
		Multimap<Var, RestrictedExpr> unionVarDefs = HashMultimap.create();
		
		// For each union member, prepare a datastructe for its new projection
		List<Map<String, SqlExpr>> unionMemberProjections = new ArrayList<Map<String, SqlExpr>>();
		for (int i = 0; i < members.size(); ++i) {
			//Multimap<Var, Expr> tmp = HashMultimap.create();
			Map<String, SqlExpr> tmp = new HashMap<String, SqlExpr>();
			unionMemberProjections.add(tmp);
		}

		// Now we can start with the actual work				
		// Map each variable to the set of corresponding members
		Multimap<Var, Integer> varToMembers = HashMultimap.create();

		for (int i = 0; i < members.size(); ++i) {
			Mapping mapping = members.get(i);
			for (Var var : mapping.getVarDefinition().getMap().keySet()) {
				varToMembers.put(var, i);
			}
		}

		
		
		
		
		//Map<Var, RestrictedExpr> varToConstant = new HashMap<Var, RestrictedExpr>();
		Set<Var> constantVars = new HashSet<Var>();
		for(Entry<Var, Collection<Integer>> entry : varToMembers.asMap().entrySet()) {
			Var var = entry.getKey();
						
			for (int index : entry.getValue()) {
				Mapping member = members.get(index);
				
				Collection<RestrictedExpr> exprsForVar = member.getVarDefinition().getDefinitions(var);
				

				for(RestrictedExpr def : exprsForVar) {
					if(def.getExpr().isConstant()) {
						//varConstant.add(var);
						//varToConstant.put(var, def);
						constantVars.add(var);
					}			
				}
			}
		}
		
		NodeValue nullNode = NodeValue.makeString("should not appear anywhere");
//
//		
//		// For each var that maps to a constant, add a NULL mapping for
//		// every union member which does not define the variable as a constant
//		for(Entry<Var, TermDef> entry : varToConstant.entrySet()) {
//			Var var = entry.getKey();
//			TermDef baseTermDef = entry.getValue();
//			
//			for (int i = 0; i < sqlNodes.size(); ++i) {
//				SqlNode sqlNode = sqlNodes.get(i);
//				
//				Multimap<Var, TermDef> varDefs = sqlNode.getSparqlVarToExprs();
//				
//				boolean hasConstant = false;
//				for(TermDef termDef : varDefs.get(var)) {
//					if(termDef.getExpr().isConstant()) {
//						hasConstant = true;
//						continue;
//					}
//				}
//				
//				if(!hasConstant) {
//					ExprList exprs = new ExprList();
//					List<Expr> args = baseTermDef.getExpr().getFunction().getArgs();
//					//System.out.println("Args: " + args.size());
//					for(int j = 0; j < args.size(); ++j) {
//						Expr expr = j == 1 ? NodeValue.makeString(""): args.get(j);
//						
//						exprs.add(expr);
//					}
//					
//					Expr newExpr = ExprCopy.getInstance().copy(baseTermDef.getExpr(), exprs); 
//					
//					varToSqlNode.put((Var)var, i);
//					varDefs.put(var, new TermDef(newExpr));
//				}				
//			}
//		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		// If a variable maps to a constant, than the mapping does not apply to any union member
		// that does not define the constant.
		// This means we have to introduce a column for discrimination, which contains NULL for
		// all union members where to constaint is not applicable 		
		ExprCommonFactor factorizer = new ExprCommonFactor(aliasGen);

		
		Map<String, TypeToken> unionTypeMap = new HashMap<String, TypeToken>();
		
		
		// For each variable, cluster the corresponding expressions
		for(Entry<Var, Collection<Integer>> entry : varToMembers.asMap().entrySet()) {
			Var var = entry.getKey();
			Collection<Integer> memberIndexes = entry.getValue();
			
			// TODO Just clustering by hash may result in clashes!!!
			// For each hash we have to keep a list and explicitly compare for structural equivalence
			Multimap<String, ArgExpr> cluster = HashMultimap.create();
			
			

			//IBiSetMultimap<Integer, Integer> exprToOrigin = new BiHashMultimap<Integer, Integer>();
			//Multimap<Integer, Integer> exprToOrigin = HashMultimap.create();
			
			for (int index : memberIndexes) {
				Mapping member = members.get(index);

//								
//				if(exprsForVar.isEmpty()) {
//					// If a variable is not defined in a member, make sure that it is not a constant
//					if(constantVars.contains(var)) {
//						exprsForVar.add(new RestrictedExpr(nullNode));
//					}
//				}
				
				Collection<RestrictedExpr> exprsForVar = member.getVarDefinition().getDefinitions(var);
				
				for(RestrictedExpr def : exprsForVar) {
				
					Map<String, TypeToken> typeMap = member.getSqlOp().getSchema().getTypeMap(); //SqlNodeUtil.getColumnToDatatype(sqlNode);
					//Integer hash = ExprStructuralHash.hash(def.getExpr(), columnToDatatype);
					
					Expr expr = def.getExpr();
					Expr datatypeNorm = exprNormalizer.normalize(expr, typeMap);
					String hash = datatypeNorm.toString();
					logger.debug("Cluster for [" + expr + "] is [" + hash + "]");

					
//					if(expr.isConstant()) {
//						
//					}
					
					cluster.put(hash, new ArgExpr(expr, index));
				}
			}
			
		
			/*
			 *  Process the entries of the clusters we just created
			 */

			for(Entry<String, Collection<ArgExpr>> clusterEntry : cluster.asMap().entrySet()) {
				Collection<ArgExpr> argExprs = clusterEntry.getValue();
					
//				System.out.println("Clustered: " + clusterEntry.getKey());
//				for(ArgExpr argExpr : clusterEntry.getValue()) {
//					System.out.println("    " + argExpr);
//				}
				
				// First, we build a list of exprs of the cluster and
				// a map for mapping the clustered exprs back to their ops
				List<Expr> exprs = new ArrayList<Expr>();
				Map<Integer, Integer> exprToOp = new HashMap<Integer, Integer>();

				int i = 0;
				for(ArgExpr argExpr : argExprs) {
					
					Expr expr = argExpr.getExpr();
					
					// // FIXME Constant expansion should probably already be done in the var definition!
					//Expr expanded = ConstantExpander.transform(expr);
					
					exprs.add(expr);
					exprToOp.put(i, argExpr.getIndex());
					
					++i;
				}
				

				// Now we can finally factor the cluster
				// Legacy code - it takes variables rather than column references
				List<Map<Var, Expr>> tmpPartialProjections = new ArrayList<Map<Var, Expr>>();
				Expr common = factorizer.transform(exprs, tmpPartialProjections);

				
				// FIXME Get rid of this conversion
				List<Map<String, SqlExpr>> partialProjections = new ArrayList<Map<String, SqlExpr>>();
				for(int j = 0; j < tmpPartialProjections.size(); ++j) {
					int memberIndex = exprToOp.get(j);
					Mapping member = members.get(memberIndex);
					Map<String, TypeToken> typeMap = member.getSqlOp().getSchema().getTypeMap();
					
					Map<Var, Expr> tmpMap = tmpPartialProjections.get(j);

					Map<String, SqlExpr> map = new HashMap<String, SqlExpr>();
					for(Entry<Var, Expr> e : tmpMap.entrySet()) {
						
						String columnName = e.getKey().getVarName();
						Expr expr = e.getValue();
						//SqlExpr sqlExpr = sqlTranslator.translate(expr, null, typeMap);
						ExprSqlRewrite exprRewrite = sqlTranslator.translate(expr, null, typeMap);
						SqlExpr sqlExpr = SqlTranslatorImpl2.asSqlExpr(exprRewrite);

						
						//map.put(e.getKey().getVarName(), expr);
						map.put(columnName, sqlExpr);

//						if(columnName.equals("c_4")) {
//							System.out.println("Debug c_4");
//						}
						
						unionTypeMap.put(columnName, sqlExpr.getDatatype());
//						System.out.println("Union type map: " + unionTypeMap);
					}
					
					partialProjections.add(map);
				}
				
				
				// For our current variable, we can set up the projection of the result...
				// TODO: We do not want to lose any restrictions!
				unionVarDefs.put(var, new RestrictedExpr(common));

				// ... and now we adjust the projections of the children accordingly
				for (int j = 0; j < partialProjections.size(); ++j) {
					int originalIndex = exprToOp.get(j);

					//SqlNode tmp = sqlNodes.get(originalIndex);
					Map<String, SqlExpr> projection = unionMemberProjections.get(originalIndex);
					
					Map<String, SqlExpr> partialProjection = partialProjections.get(j);
					
					for(Entry<String, SqlExpr> ppEntry : partialProjection.entrySet()) {
						projection.put(ppEntry.getKey(), ppEntry.getValue());
					}					
				}
			}			
		}

		/*
		 * Compute the global type map based on all unionMemberProjections
         */
		/*
		for (int i = 0; i < unionMemberProjections.size(); ++i) {
			Map<String, SqlExpr> projection = unionMemberProjections.get(i);

			for(Entry<String, SqlExpr> entry : projection.entrySet()) {
				
				String columnName = entry.getKey();
				SqlExpr expr = entry.getValue();
				
				assert !unionTypeMap.containsKey(columnName) : "Column already mapped - attempted reassing of " + columnName;
				
				unionTypeMap.put(entry.getKey(), expr.getDatatype());
			}			
		}
		*/

		// The order of the column for the union
		List<String> unionColumnOrder = new ArrayList<String>(unionTypeMap.keySet());
//		System.out.println("unionTypeMap: " + unionTypeMap);
		
		/*
		 * For each member, fill it appropriately up with nulls in order to make it
		 * match the schema of the union
		 */
		List<SqlOp> extended = new ArrayList<SqlOp>();		
		
		for(int i = 0; i < members.size(); ++i) {
			Mapping member = members.get(i);
			Map<String, SqlExpr> unionMemberProjection = unionMemberProjections.get(i);
			
			Set<String> names = new HashSet<String>(unionMemberProjection.keySet());//member.getSqlOp().getSchema().getColumnNames());
			Set<String> unboundColumns = Sets.difference(unionTypeMap.keySet(), names);
		
			for(String columnName : unboundColumns) {
				
				TypeToken datatype = unionTypeMap.get(columnName);
				
				//NodeValue nullValue = new E_SqlNodeValue(NodeValue.nvNothing, datatype);
				S_Constant nullValue = new S_Constant(datatype);
				
				unionMemberProjection.put(columnName, nullValue);
			}
			
			// We first extend the original projection with the new expressions and renames
			Projection finalMemberProjection = new Projection(unionColumnOrder, unionMemberProjection);

			SqlOpExtend extend = SqlOpExtend.create(member.getSqlOp(), finalMemberProjection);
			
						
			
			// And afterwards limit the columns of each member to only those of the union
			SqlOpProject opProject = SqlOpProject.create(extend, unionColumnOrder);
			
			extended.add(opProject);
		}
		//System.out.println("ColumnOrder: " + unionColumnOrder);

		/*
		 * Create a mapping based on the new vardef an the new SQL union.
		 */
		SqlOpUnionN newUnion = SqlOpUnionN.create(extended);

		
//		for(SqlOp m : newUnion.getSubOps()) {
//			System.out.println("SubOp schema: " + m.getSchema().getColumnNames());
//		}
		
		VarDefinition varDefinition = new VarDefinition(unionVarDefs);
		Mapping result = new Mapping(varDefinition, newUnion);
		

		logger.info("Union of mappings:\n" + result);
		return result;
	}

	
	
	public static List<Map<Var, Expr>> createBindingProduct(VarDefinition varDef, Collection<Var> vars) {
				
		Multimap<Var, RestrictedExpr> map = varDef.getMap();
		List<Collection<RestrictedExpr>> assignments = new ArrayList<Collection<RestrictedExpr>>(vars.size());
		for(Var var : vars) {
			assignments.add(map.get(var));
		}

		CartesianProduct<RestrictedExpr> cart = CartesianProduct.create(assignments);
		
		List<Map<Var, Expr>> result = new ArrayList<Map<Var, Expr>>();
		for(List<RestrictedExpr> item : cart) {

			Iterator<Var> itVar = vars.iterator();
			Iterator<RestrictedExpr> itRestExpr = item.iterator();
			
			//BindingMap binding = new BindingHashMap();
			Map<Var, Expr> binding = new HashMap<Var, Expr>();
			for(int i = 0; i < vars.size(); ++i) {
				Var var = itVar.next();
				RestrictedExpr restExpr = itRestExpr.next();
				
				Expr expr = restExpr.getExpr();
				
				binding.put(var, expr);
				//binding.add(var, expr);
				
				//assignment.put(var, expr);
				result.add(binding);
			}			
		}

		return result;
	}
	

	/**
	 * Extends a mapping with additional variable definitions.
	 * 
	 * 
	 * Ignore below for now.
	 * 
	 * FIXME We need to introduce var orders in the var definitions, if extending may refer to other variables.
	 * 
	 * Example:
	 *   extend(?c = ?x)
	 *     group (?s) (?x=(count)) 
	 * 
	 * 
	 */
	@Override
	public Mapping extend(Mapping a, VarDefinition varDef) {		

		// TODO Replace variables in the definition with the SQL definitions
		
		Multimap<Var, RestrictedExpr> newMap = HashMultimap.create();
		
		Multimap<Var, RestrictedExpr> map = varDef.getMap();
		for(Entry<Var, Collection<RestrictedExpr>> entry : map.asMap().entrySet()) {
			Var var = entry.getKey();
			Collection<RestrictedExpr> restExprs = entry.getValue();
			
			
			for(RestrictedExpr restExpr : restExprs) {
				Expr expr = restExpr.getExpr();
				Set<Var> vars = expr.getVarsMentioned();

				if(vars.isEmpty()) {
					newMap.put(var, restExpr);
				}
				else {
					List<Map<Var, Expr>> bindings = createBindingProduct(a.getVarDefinition(), vars);
	
					for(Map<Var, Expr> binding : bindings) {
						NodeExprSubstitutor substitutor = new NodeExprSubstitutor(binding);
						
						Expr newExpr = substitutor.transformMM(expr);
						
						newMap.put(var, new RestrictedExpr(newExpr, restExpr.getRestrictions()));
					}
				}
			}
		}
		
		VarDefinition tmpVarDef = new VarDefinition(newMap);
		
		
		VarDefinition newVarDef = a.getVarDefinition().extend(tmpVarDef);
		Mapping result = new Mapping(newVarDef, a.getSqlOp());
		
		return result;
	}

	/**
	 * GroupBy 
	 * 
	 * 
	 * 
	 */
	@Override
	public Mapping groupBy(Mapping a, VarExprList groupVars,
			List<ExprAggregator> aggregators) {
		
		
		// TODO Add variables mentioned in aggregators aswell?
		List<Var> vars = new ArrayList<Var>(groupVars.getVars());
		
		List<Mapping> ms = MappingRefactor.refactorToUnion(a, vars);

		ListMultimap<String, Mapping> groups = MappingRefactor.groupBy(exprNormalizer, ms, vars);
		
		
		List<Mapping> gg = new ArrayList<Mapping>();
		for(Collection<Mapping> group : groups.asMap().values()) {
			List<Mapping> list = new ArrayList<Mapping>(group);
			
			Mapping u = union(list);
			// Get the SQL column references
			
			
			// Collect all column names we have to group by
			List<String> columnNames = new ArrayList<String>();			
			for(Var var : vars) {
				
				Collection<RestrictedExpr> defs = u.getVarDefinition().getDefinitions(var);
				if(defs.size() > 1) {
					throw new RuntimeException("Should not happen");
				}
				else if(defs.isEmpty()) {
					continue;
				} else {
					RestrictedExpr restExpr = defs.iterator().next();
					Expr expr = restExpr.getExpr();
					Set<Var> mentionedVars = expr.getVarsMentioned();
					
					for(Var mv : mentionedVars) {
						String varName = mv.getVarName();
						if(!columnNames.contains(varName)) {
							columnNames.add(varName);
						}
					}
				}
			}
			
			
			Map<String, TypeToken> typeMap = u.getSqlOp().getSchema().getTypeMap();
			List<SqlExpr> columnRefs = new ArrayList<SqlExpr>();
			for(String columnName : columnNames) {
				TypeToken type = typeMap.get(columnName);
				
				SqlExpr expr = new S_ColumnRef(type, columnName);
				columnRefs.add(expr);
			}
			
			List<SqlExprAggregator> sqlAggregators = new ArrayList<SqlExprAggregator>();
			
			SqlOpGroupBy sqlOpGroupBy = SqlOpGroupBy.create(u.getSqlOp(), columnRefs, sqlAggregators);
			
			
			Mapping tmp = new Mapping(u.getVarDefinition(), sqlOpGroupBy);
			
			gg.add(tmp);
		}
		
		Mapping result = unionIfNeeded(gg);
		
		
		
		// Gensym for SPARQL variables 
		Gensym varsym = Gensym.create("v");
		
		// Gensym for SQL columns
		Gensym aggSym = Gensym.create("G");
		
		// HACK
		for(ExprAggregator ea : aggregators) {
			
			Aggregator agg = ea.getAggregator();
			ExprSqlRewrite rewrite = rewrite(aggSym, agg);
			
			
			Projection ex = new Projection();
			ex.add(rewrite.getProjection());
			//ex.put("dummy", new S_Constant(TypeToken.Int, null));
			
			SqlOp newOp = SqlOpExtend.create(result.getSqlOp(), ex);
			
			/*
			ExprList args = new ExprList();
			ExprVar dummyCol = new ExprVar(Var.alloc("dummy")); 
			args.add(dummyCol);
			args.add(NodeValue.makeString(XSD.xlong.getURI()));
			Expr t = new E_Function(SparqlifyConstants.typedLiteralLabel, args);
			
			logger.warn("Using hack, no aggregator will be present - implement this properly");
			Var var = ea.getVar();
			*/

			Var var = ea.getVar(); //Var.alloc(varsym.next());

			Multimap<Var, RestrictedExpr> map = HashMultimap.create(result.getVarDefinition().getMap());
			map.put(var, new RestrictedExpr(rewrite.getExpr()));

			VarDefinition newVd = new VarDefinition(map);
			
			result = new Mapping(newVd, newOp); 
		}
		
		return result;
	}

	
	
	
	/**
	 * Returns a pair comprised of:
	 * - A SPARQL expression that references the SQL column of the aggregater
	 * - A projection 
	 * // Ignore: the typeMap is implied by the projection - A type map
	 * 
	 * @param agg
	 */
	public ExprSqlRewrite rewrite(Gensym gensym, Aggregator agg) {
		ExprSqlRewrite result;
		if(agg instanceof AggCount) {
			result = rewrite(gensym, (AggCount)agg);
		}
		else {
			throw new RuntimeException("Unsupported aggregator: " + agg);
		}
		
		return result;
	}
	
	public ExprSqlRewrite rewrite(Gensym gensym, AggCount agg) {
		
		String columnAlias = gensym.next();
		S_AggCount count = new S_AggCount();
		S_Agg sagg = new S_Agg(count);
		
		Projection p = new Projection();
		p.put(columnAlias, sagg);
		
		ExprVar columnRef = new ExprVar(columnAlias);
		
		ExprList args = new ExprList();
		args.add(columnRef);
		args.add(NodeValue.makeString(XSD.integer.getURI()));

		Expr e = new E_Function(SparqlifyConstants.typedLiteralLabel, args);

		ExprSqlRewrite result = new ExprSqlRewrite(e, p);
		return result;
	}

	
	
	/**
	 * FIXME The var definitions should not be lists rather than sets in order
	 * to ensure deterministic behaviour!
	 * 
	 * 
	 * 
	 * 1. Expand mappings according to their var definitions,
	 *    so that each mapping relates a variable only to a single expression.
	 *    
	 *    ?a = {uri(?name), plainLiteral(?id)
	 *      -> Two mappings with ?a = uri(?name) and ?a = plainLiteral(?id)
	 * 
	 * 2. Because we know that rdf terms are sorted by
	 *    (blank node, uri, plain literals, typed literals)
	 *    we can ensure this by
	 *
	 *
	 * Examples
	 *     Order By ?p ?o
	 *     Order By !Bound(?p)
	 */
	@Override
	public Mapping order(Mapping a, List<SortCondition> sortConditions) {
		
		logger.warn("Order by not implemented yet.");
		return a;
		/*
		VarDefinition varDef = a.getVarDefinition();
		SqlOp sqlOp = a.getSqlOp();
		Map<String, TypeToken> typeMap = sqlOp.getSchema().getTypeMap();
		
		// Get all variables referenced by the sort conditions
		Set<Var> tmpVars = new HashSet<Var>();
		for(SortCondition sortCondition : sortConditions) {
			Expr expr = sortCondition.getExpression();

			
			//translateSql(expr, typeMap, sqlTranslator)
			
			List<SqlExprContext> exprContexts = createSqlExprs(expr, varDef, typeMap, sqlTranslator);
			//rdfTerm.getArg(i)
			for(SqlExprContext exprContext : exprContexts) {
				System.out.println("ExprContext: " + exprContext);
			}
			//SqlExpr = translateSql(expr, typeMap, sqlTranslator)
			
			//E_RdfTerm rdfTerm = SqlTranslationUtils.expandRdfTerm(expr);
			//List<Expr> args = rdfTerm.getArgs();
//			for(Expr arg : args) {
//			}
			

			
			// If the expressions have different datatypes, cast them to string
			
			
			Set<Var> tmp = expr.getVarsMentioned();
			tmpVars.addAll(tmp);
		}		
		

		return a;
		
//		List<Var> vars = new ArrayList<Var>(tmpVars);
//		
//		List<Mapping> ms = MappingRefactor.refactorToUnion(a, vars);
//
//		ListMultimap<String, Mapping> groups = MappingRefactor.groupBy(exprNormalizer, ms, vars);
//
//		
//		for(SortCondition sortCondition : sortConditions) {
//			Expr expr = sortCondition.getExpression();
//			
//			Set<Var> sortVars = expr.getVarsMentioned();
//
//			
//	
//			
//			
//			groupBy(var, groupByNode, tmp, genG); //generator.forColumn());
//			*/
//		}
//    	

    	/*
    	if(true) {
    		return groupByNode;
    	}*/
    	
    	// TODO Somewhere in the below code it happens, that the projection gets messed up (resulting in e.g. <1> being returned as a property)
    	
    	// Now we have created the "group by projection"
    	// However, now we need to wrap it with another projection, in which we can add any expressions appearing in the order by
//		SqlProjection orderByNode = new SqlProjection(generator.nextRelation(), groupByNode);
//    	SqlNodeOld tmp2 = createNewAlias(orderByNode.getAliasName(), groupByNode, generator);
//    	SqlSelectBlockCollector.copyProjection(orderByNode, tmp2);
//    	
//    	
//    		// order by ...  str(?o)
//    		// with ?o = uri(concat())
//    		
//    	
//    	// Create a new projection
//
//    	/*
//    	String orderAlias = generator.nextRelation();
//    	SqlProjection orderNode = new SqlProjection(orderAlias, projection);
//    	createNewAlias(projection.getAliasName(), orderNode, projection);
//    	*/
//    	//SqlSelectBlockCollector.copyProjection(orderNode, projection);
//
//    	
//    	
//    	//SqlNode orderNode = wrapWithProjection(projection, generator); //new SqlProjection(projection.getAliasName(), projection);
//    	
//
//    	SqlNodeOld result = new SqlNodeOrder(orderByNode.getAliasName(), orderByNode, sqlConditions);
//	
//
//		Generator gen = Gensym.create("o");
//
//		// Build the sort conditions for our current node
//    	for(SortCondition condition : conditions) {
//    		
//    		SqlExprList pushed = shallowPushX(condition.getExpression(), orderByNode.getSparqlVarToExprs(), orderByNode.getAliasToColumn());
//
//    		if(pushed == null) {
//    			continue;
//    		}
//    		
//    		//SqlExprList pushed = forcePushDown(new ExprList(condition.getExpression()), a);
//    		
//    		for(SqlExpr sqlExpr : pushed) {
//        		// Don't sort by constant expression
//    			Set<SqlExprColumn> columnsMentioned = SqlExprBase.getColumnsMentioned(sqlExpr); 
//    			if(columnsMentioned.isEmpty()) {
//        			continue;
//        		}
//        		
//    			boolean allowExprsInOrderByClause = false;
//    			// Sql cannot order by columns that are not selected;
//    			// Therefore, any order expression becomes part of the projection
//    			// TODO Implement this properly!!!
//    			if(allowExprsInOrderByClause) {
//    				sqlConditions.add(new SqlSortCondition(sqlExpr, condition.getDirection()));
//    			} else {
//    				String dummyColumn = gen.next(); //generator.nextColumn();
//
//    				orderByNode.getAliasToColumn().put(dummyColumn, sqlExpr);
//    				
//    				sqlConditions.add(new SqlSortCondition(new SqlExprColumn(null, dummyColumn, sqlExpr.getDatatype()), condition.getDirection()));
//    				
//    				//result.getAliasToColumn().put(dummyColumn, new SqlExprColumn(projection.getAliasName(), dummyColumn, sqlExpr.getDatatype()));
//    			}
//    		}
//    	}
//    		
//    		/*
//    		for(Var var : condition.getExpression().getVarsMentioned()) {
//    			for(Expr expr : a.getSparqlVarToExprs().asMap().get(var)) {
//    				for(Var columnName : expr.getVarsMentioned()) {
//    					
//    					
//    					
//    					
//    					SqlExpr sqlExpr = a.getAliasToColumn().get(columnName.getName());
//    					
//    					
//    					
//    					String exprStr = sqlExprSerializer.serialize(sqlExpr);
//    					
//    					if(dirStr != null) {
//    						//exprStr = dirStr + "(" + exprStr + ")";
//    						exprStr = exprStr + " " + dirStr;
//    					}
//    					
//    					sortColumnExprStrs.add(exprStr);
//    				}
//    			}
//    		}
//    	}
//    	*/
//		
//		/*
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
//		 */
//
//		
//		//SqlProjection r2 = new SqlProjection(aliasName, orderNode);
//		//createNewAlias(aliasName, r2, orderNode);
//
//		result.getAliasToColumn().putAll(orderByNode.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(orderByNode.getSparqlVarToExprs());
//		return result;
//		
//		
//		
//		return a;
	}
	
	
	/**
	 * Merge all possible combinations of sparql vars into a single column 
	 * 
	 * @param node
	 */
//	public static Mapping groupBy(Var var, Mapping mapping) {// SqlNodeOld target, SqlNodeOld node, Generator generator) {
//		Collection<RestrictedExpr> restExprs = mapping.getVarDefinition().getMap().get(var);
//		
//		// We need to group even if there is just a single expression for the var
//		// WRONG: If there is just a single expression, we can do a soft grouping
//		// (i.e. ignore conversions to string (such as uris build from id int columns)
//
//		if(restExprs.size() <= 1) {
//			return mapping;
//		}
//		
//		
//		
//		// Create a copy of the exprs and sort by number of variables
//		List<RestrictedExpr> defs = new ArrayList<RestrictedExpr>(restExprs);
//		Collections.sort(defs, new Comparator<RestrictedExpr>(){
//			@Override
//			public int compare(RestrictedExpr arg0, RestrictedExpr arg1) {
//				return arg1.getExpr().getVarsMentioned().size() - arg1.getExpr().getVarsMentioned().size();
//			}});
//
//		
//		NodeExprSubstitutor substitutor = SqlNodeBinding.createSubstitutor(node.getAliasToColumn());
//		// Now for each component of the E_RdfTerm, create the projection
//		
//		
//		/*
//		if(defs.isEmpty()) {
//			System.out.println("debug");
//		}*/
//		
//		
//		
//		/*
//		// Rearrange exprs by args 
//		List<Map<Set<Var>, Expr>> argToExprs = new ArrayList<Map<Set<Var>, Expr>>();
//		for(int i = 0; i < 4; ++i) {
//			Set<Expr> newExprs = new HashSet<Expr>();
//			argToExprs.add(newExprs);
//		
//			for(Expr expr : exprs) {
//				E_RdfTerm term = (E_RdfTerm)expr;
//		
//				newExprs.add(term.getArgs().get(i));
//			}
//		}*/
//			
//		List<Expr> newArgs = new ArrayList<Expr>();
//		
//		Projection projection = new Projection();
//		
//		SqlOp sqlNode = mapping.getSqlOp();
//		
//		String datatype;
//		for(int i = 0; i < 4; ++i) {
//
//			// Dependency on the columns
//			List<SqlExpr> columnDeps = new ArrayList<SqlExpr>();
//
//			
//			datatype = (i == 0) 
//					? "::int"   //DatatypeSystemDefault._INTEGER
//					: "::text"; //DatatypeSystemDefault._STRING;
//			
//			
//			//Factory1<String> caster = castFactory.formatString(sqlExpr.getDatatype());
//			
//			
//			List<String> exprStrs = new ArrayList<String>();
//			for(RestrictedExpr restExpr : restExprs) {
//				
//				Expr e = restExpr.getExpr();
//				
//				E_RdfTerm args = (E_RdfTerm)e;
//				Expr arg = args.getArgs().get(i);			
//
//				//SqlExpr sqlExpr = SqlNodeBinding.forcePushDown(arg, substitutor);//(arg, node);
//				//SqlExpr sqlExpr = SqlNodeBinding.forcePushDown(arg, substitutor);
//				SqlExpr sqlExpr = createSqlCondition(condition, varDef, typeMap, sqlTranslator);
//				
//				Factory1<String> formatter = castFactory.formatString(sqlExpr.getDatatype());
//				
//				String exprStr = sqlExprSerializer.serialize(sqlExpr);
//				
//				exprStr = formatter.create(exprStr);
//				
//				
//				exprStrs.add(exprStr);
//			}
//
//			
//			String replacement = "";
//			/* TODO We need the dependencies to any column (see a few lines below)
//			if(exprs.size() == 1) {
//				replacement = exprStrs.get(0);
//				
//			} else*/ {
//			
//				String caseStr = "CASE\n";
//				String elseStr = "NULL" + datatype;
//				
//				for(int j = 0; j < defs.size(); ++j) {
//					RestrictedExpr def = defs.get(j);
//					Expr expr = def.getExpr();
//					
//					String exprStr = exprStrs.get(j);
//
//					if(expr.getVarsMentioned().isEmpty()) {
//						elseStr = exprStr;
//					} else {
//					
//						List<String> columnNames = new ArrayList<String>();
//						for(Var v : expr.getVarsMentioned()) {
//							
//
//							// Keep the dependency to the original columns
//							// If an expression does not depend on other columns, it will be treated as a constant.
//							if(true) {
//								String depName = v.getName();
//								//x SqlExpr depSqlExpr = sqlNode.getAliasToColumn().get(depName);
//								//x SqlDatatype depDatatype = depSqlExpr.getDatatype();
//								//sqlNode.
//								TypeToken depDatatype = sqlNode.getSchema().getTypeMap().get(depName);
//								
//								columnDeps.add(new S_ColumnRef(depDatatype, depName));
//							}
//								
//							
//							
//							//columnNames.add(v.getName() + " IS NOT NULL");
//							//x columnNames.add(target.getAliasName() + "." + v.getName() + " IS NOT NULL");
//							columnNames.add(v.getName() + " IS NOT NULL");
//							//projection.put(v.getName(), new expr)
//						}
//				
//						caseStr += "    WHEN (" + Joiner.on(" AND ").join(columnNames) + ") THEN " + "(" + exprStr + ")" + datatype + "\n";
//					}
//				}
//				
//				
//				caseStr += "    ELSE " + elseStr + "\n";
//				caseStr += "END ";
//
//				replacement = caseStr;
//			}
//
//
//			String columnAlias = generator.next();
//			newArgs.add(new ExprVar(columnAlias));
//
//			
//			S_String c = new S_String(replacement, TypeToken.String, columnDeps);
//			//target.getAliasToColumn().put(columnAlias, c);
//			//node.getAliasToColumn().put(columnAlias, c);
//			
//			
//			/*
//			System.out.println("Group By " + var);
//			System.out.println(SqlAlgebraToString.makeString(node));//asString(node, new IndentedWriter(System.out));
//			System.out.println("-----------------------------");
//			*/
//		}
//
//
//		E_RdfTerm replacement = new E_RdfTerm(newArgs);
//
//		SqlOp tmp = SqlOpExtend.create(sqlNode, projection);
//
//		
//		Mapping result = new Mapping(mapping.getVarDefinition(), tmp);
//		
//		return result;
//	}
	
	
	
	
}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	
//	// Pushes down an Expr object that should be interpreted as an SQL expression
//	public static SqlExpr forcePushDown(Expr expr, NodeExprSubstitutor substitutor) {
//		Expr substituted = substitutor.transformMM(expr);
//
//		
//		Expr x = PushDown.pushDownMM(substituted);
//		if(!(x instanceof ExprSqlBridge)) {
//			throw new RuntimeException("Failed to push down '" + expr + "'");
//		}
//		SqlExpr result = ((ExprSqlBridge)x).getSqlExpr();
//
//		return result;
//	}
//	
//	public static SqlExprList forcePushDown(ExprList exprs, SqlNodeOld node) {
//		SqlExprList result = new SqlExprList();
//		for(Expr expr : exprs) {
//			SqlExpr sqlExpr = forcePushDown(expr, node.getAliasToColumn());
//			result.add(sqlExpr);
//		}
//		
//		return result;
//	}
//
//
//
//	public static SqlExpr forcePushDown(Expr expr, Map<String, SqlExpr> aliasToColumn) {
//		NodeExprSubstitutor substitutor = createSubstitutor(aliasToColumn);
//		
//		return forcePushDown(expr, substitutor);
//	}
//
//	public static SqlExpr forceShallowPushDown(Expr expr, Map<String, SqlExpr> aliasToColumn) {
//
//		Map<String, SqlExpr> shallow = createShallowAliasToColumn(aliasToColumn);
//
//		NodeExprSubstitutor substitutor = createSubstitutor(shallow);
//		
//		return forcePushDown(expr, substitutor);
//	}
//	
//
//	
//
//	/**
//	 * Replace all occurrences of variables within expr with those of the
//	 * binding
//	 * 
//	 * @param a
//	 * @param expr
//	 * @return
//	 */
//	public static SqlExpr rewriteExpr(MappingOpsImpl a, Expr expr) {
//		// Expr tmp = expr.applyNodeTransform(new Renamer())
//
//		// Expr tmp = new E_Equals(a.getSparqlVarToExpr().get(var),
//		// b.getSparqlVarToExpr().get(var));
//		// tmp = SqlExprTranslator.optimizeMM(tmp);
//
//		// SqlExpr joinExpr = SqlExprTranslator.translateMM(tmp);
//
//		return null;
//	}
//
//	
//	public static String generateNextFreeId(String base, Set<String> used) {
//		if(!used.contains(base)) {
//			return base;
//		}
//		
//		for(int i = 1; ; ++i) {
//			String id = base + i;
//			if(!used.contains(id)) {
//				return id;
//			}
//		}
//	}
//	
//	
//	/**
//	 * Calculates the projection for the given join by renaming 
//	 * columns with same names on both sides of the join.
//	 * 
//	 * @param generator
//	 * @param left
//	 * @param right
//	 * @return
//	 */
//	public static SqlNodeOld doJoinRename(ColRelGenerator generator, SqlNodeOld left, String leftAlias, SqlNodeOld right, String rightAlias)
//	{
//		SqlNodeOld result = new SqlNodeEmpty();
//
//		if(leftAlias != null && leftAlias.equals(rightAlias)) {
//			throw new RuntimeException("Two aliases equal - should not happen");
//		}
//		
//		Set<String> colsA = left.getAliasToColumn().keySet();
//		Set<String> colsB = right.getAliasToColumn().keySet();
//		
//		Set<String> intersection = new HashSet<String>(Sets.intersection(colsA, colsB));
//		Set<String> union = Sets.union(colsA, colsB);
//
//		Map<String, String> colRefRenames = new HashMap<String, String>();
//
//		for(String common : intersection) {
//			SqlExpr sqlExpr = right.getAliasToColumn().get(common);
//			
//			String idBase = rightAlias + "_" + common;			
//			String id = generateNextFreeId(idBase, union);
//			
//			colRefRenames.put(common, id);
//		}
//		
//		Map<String, SqlExpr> newBMap = new HashMap<String, SqlExpr>();
//		for(String colB : colsB) {
//			String rename = colRefRenames.get(colB);
//			if(rename == null) {
//				rename = colB;
//			}
//			
//			SqlExpr sqlExpr = right.getAliasToColumn().get(colB);
//			
//			newBMap.put(rename, sqlExpr);
//		}
//		//b.getAliasToColumn().clear();
//		//b.getAliasToColumn().putAll(newBMap);
//		result.getAliasToColumn().putAll(newBMap);
//		
//		
//		Map<Node, Expr> exprMap = new HashMap<Node, Expr>();
//		for(Entry<String, String> entry : colRefRenames.entrySet()) {
//			exprMap.put(Var.alloc(entry.getKey()), new ExprVar(entry.getValue()));
//		}
//		
//		// Substitute the column references in b
//		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(exprMap);
//
//		Multimap<Var, VarDef> newSparqlMap = HashMultimap.create();
//		for(Entry<Var, VarDef> entry : right.getSparqlVarToExprs().entries()) {
//			VarDef before = entry.getValue();
//			VarDef after = new VarDef(substitutor.transformMM(before.getExpr()), before.getRestrictions());
//			
//			newSparqlMap.put(entry.getKey(), after);
//		}
//		//b.getSparqlVarToExprs().clear();
//		//b.getSparqlVarToExprs().putAll(newSparqlMap);
//		result.getSparqlVarToExprs().putAll(newSparqlMap);
//		
//		return result;
//	}
//
//
//	/**
//	 * Creates a substitutor object, that can replace the sparql variables
//	 * with their their expression
//	 * 
//	 * TODO Does not work that way with the datatype support
//	 * 
//	 * @param node
//	 * @return
//	 */
///*
//	public static NodeExprSubstitutor createSparqlVarExpander(SqlNode node) {
//		Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
//		for(Entry<Node, Expr> entry : node.getSparqlVarToExprs().entrySet()) {
//			expanderMap.put(Var.alloc(entry.getKey()), entry.getValue());
//		}
//		
//		NodeExprSubstitutor result = new NodeExprSubstitutor(expanderMap);
//
//		return result;
//	}
//*/
//	
//	/**
//	 * Create an object that can replace column references
//	 * with their expression.
//	 * 
//	 * Example:
//	 * concat('prefix', id) AS col1
//	 * 
//	 * The substitutor can replace sparql var references to col1 with an
//	 * SqlBridge to the concat expression. 
//	 * 
//	 * 
//	 * @param node
//	 * @return
//	 */
//	public static NodeExprSubstitutor createSubstitutor(Map<String, SqlExpr> aliasToColumn) {
//		Map<Var, Expr> sqlSubstitutionMap = new HashMap<Var, Expr>();
//		for(Entry<String, SqlExpr> entry : aliasToColumn.entrySet()) {
//			sqlSubstitutionMap.put(Var.alloc(entry.getKey()), new ExprSqlBridge(entry.getValue()));
//		}
//		
//		NodeExprSubstitutor result = new NodeExprSubstitutor(sqlSubstitutionMap);
//
//		return result;	
//	}	
//	
//	
//	
//	
//	public static NodeExprSubstitutor createSubstitutor(SqlNodeOld node) {
//		return createSubstitutor(node.getAliasToColumn());
//	}
//
//
//	public static Pair<SqlNodeOld, SqlNodeOld> createJoinAlias(SqlNodeOld node, ColRelGenerator generator) {
//		if(node instanceof SqlJoin || node.getAliasName() != null) {
//			// If the node is a join, then all the components already
//			// have aliases
//			return Pair.create(node, node);
//		} else {
//			// Deal with cases where the join argument does not yet
//			// have an alias, e.g. Join(a, Filter(...))
//			// In this case, the second argument will eventually become a sub-select.
//			// So we need to assign a new alias (subselect AS x), and update the projection
//			// accordingly: aliasToColumn must refer to columns of x
//			
//			
//			// The projection must reference the columns via the old alias
//			// However, the join result must refer to them with the new alias
//			
//			String newAlias = generator.nextRelation();
//			/*
//			SqlNode result = new SqlProjection(newAlias, node);
//			result.getAliasToColumn().putAll(node.getAliasToColumn());
//			result.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
//			*/
//			
//			SqlNodeOld proj = createNewAlias(newAlias, node, generator);
//			
//			return Pair.create(node, proj);
//			
//			
//			/*
//			SqlNode newProj = createNewAlias(newAlias, node, generator);
//			SqlNode result= new SqlProjection(newAlias, node);
//			result.getAliasToColumn().putAll(newProj.getAliasToColumn());
//			result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//			*/
//		}
//	}
//
//	/*
//	public static SqlNode createJoinProj(SqlNode node, ColRelGenerator generator) {
//		
//	}*/
//	
//	public static SqlNodeOld join(ColRelGenerator generator, SqlNodeOld _a, SqlNodeOld _b,
//			JoinType joinType) {
//
//		/*
//		if(joinType == JoinType.LEFT) {
//			System.out.println("debug");
//		}*/
//		
//		
//		// Generate fresh aliases for the members of the join
//		// (If any of the join members does not have an alias yet, it will become a
//		// sub expression)
//		// (SqlNode) JOIN (SqlNode) --> (SqlNode) r1 JOIN (SqlNode) r2
//		/*
//		Pair<SqlNode, SqlNode> pairA = createJoinAlias(_a, generator);
//		Pair<SqlNode, SqlNode> pairB = createJoinAlias(_b, generator);
//
//		
//		SqlNode a = pairA.getValue();
//		SqlNode b = pairB.getValue();
//		
//		_a = pairA.getKey();
//		_b = pairB.getKey();
//		
//		String leftAlias = a.getAliasName();
//		String rightAlias = b.getAliasName();
//		*/
//		
//		SqlNodeOld a = _a;
//		SqlNodeOld b = _b;
//		
//
//		//if(a.getAliasName() == null && !(a instanceof SqlJoin) || a instanceof SqlUnionN) {
//		if(!(a instanceof SqlJoin  || a instanceof SqlTable || a instanceof SqlQuery) || a instanceof SqlUnionN) {
//			a = createNewAlias(generator.nextRelation(), a, generator);
//
//			
//			
//			/*
//			a.getSparqlVarToExprs().putAll(_a.getSparqlVarToExprs());
//			a.getAliasToColumn().putAll(_a.getAliasToColumn());
//			*/
//		}
//		
//		//if(b.getAliasName() == null && !(b instanceof SqlJoin)  || b instanceof SqlUnionN) {
//		if(!(b instanceof SqlJoin || b instanceof SqlTable || b instanceof SqlQuery)  || b instanceof SqlUnionN) {
//			b = createNewAlias(generator.nextRelation(), b, generator);
//
//			/*
//			b = new SqlAlias(generator.nextRelation(), _b);
//			b.getSparqlVarToExprs().putAll(_b.getSparqlVarToExprs());
//			b.getAliasToColumn().putAll(_b.getAliasToColumn());
//			*/
//		}
//		
//
//		
//		
//		//
//		
//		SqlNodeOld c = doJoinRename(generator, a, a.getAliasName(), b, b.getAliasName());
//
//		/*
//		if(a instanceof SqlUnionN || b instanceof SqlUnionN) {
//			System.out.println("debug");
//		}
//		
//		if(!c.getAliasToColumn().equals(b.getAliasToColumn())) {
//			System.out.println("debug");			
//		}*/
//
//		/*
//		if(a instanceof SqlTable && b instanceof SqlTable) {
//			SqlTable x = (SqlTable)a;
//			SqlTable y = (SqlTable)b;
//			
//
//			if(x.getTableName().equals(y.getTableName())) {
//				System.out.println("Same table");
//				System.out.println(a.getSparqlVarToExprs());
//				System.out.println(b.getSparqlVarToExprs());
//			}
//		}
//		*/
//		
//		
//		SqlJoin result = SqlJoin.create(joinType, a, b);
//
//		
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		result.getAliasToColumn().putAll(c.getAliasToColumn());
//		
//		// TODO Take filters into account for further join conditions
//
//		
//		// Check if there is an overlap in the columns being used for the join
//		// if that is the case, rename the columns
//		// e.g. A(id, name) Join B(id, name) will result in
//		// a.id as a_id, b.id as b_id
//
//		
//		
//		// Do we need an alias?
//
//		// Update the projection:
//		// First use all variables from a, then use those that are exclusive to b
//		Set<Var> cVars = Sets.difference(c.getSparqlVarsMentioned(),
//				a.getSparqlVarsMentioned());
//
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
//		//result.getSqlVarToExpr().putAll(a.getSqlVarToExpr());
//
//		// TODO
//		// Given Join(A(id, name), B(id, name)), and the bindingis
//		// ?o = Literal(name)            ?t = Literal(name)
//		// In that case we need to rename 'name':
//		// Select a.name as renamed1, b.name as renamed2
//		for (Var var : cVars) {
//			Collection<VarDef> exprC = c.getSparqlVarToExprs().get(var);
//			result.getSparqlVarToExprs().putAll(var, exprC);
//		}
//		
//		
//		// Determine join conditions based on variables common to both join
//		// arguments
//		Set<Var> commons = Sets.intersection(a.getSparqlVarsMentioned(),
//				c.getSparqlVarsMentioned());
//
//
//		
//		/**
//		 * Build a map var -> sql expr
//		 */
//		/*
//		Map<Var, Expr> sqlSubstitutionMap = new HashMap<Var, Expr>();
//		for(Entry<String, SqlExpr> entry : result.getColumnToSqlExpr().entrySet()) {
//			sqlSubstitutionMap.put(Var.alloc(entry.getKey()), new ExprSqlBridge(entry.getValue()));
//		}
//		
//		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(sqlSubstitutionMap);
//		*/
//		
//		
//		/*
//		Map<String, SqlExpr> joinSub = new HashMap<String, SqlExpr>();
//		joinSub.putAll(result.getAliasToColumn());
//		
//		for(Entry<String, SqlExpr> entry : b.getAliasToColumn().entrySet()) {
//			
//			joinSub.put(entry.getKey(), new SqlExprColumn(b.getAliasName(), entry.getKey(), entry.getValue().getDatatype()));
//		}
//		NodeExprSubstitutor substitutor = createSubstitutor(joinSub);
//		*/
//		
//		//throw new RuntimeException("Fix me");
//		
//		NodeExprSubstitutor substitutor = createSubstitutor(result);
//		
//		
//		// Outerloop: For each variable in common 
//		// Innerloop: For each of the variables associated expression
//		//List<SqlExpr> ands = new ArrayList<SqlExpr>();
//		for (Var var : commons) {
//			List<SqlExpr> ors = new ArrayList<SqlExpr>();
//			
//			
//			// For each combination of expressions derive the join condition
//			// The outer loop or-ifies, the inner one and-ifies
//			// a1 - b1
//			// a2 - b1
//			Collection<VarDef> newTermDefs = new ArrayList<VarDef>();
//			Collection<VarDef> ebs = c.getSparqlVarToExprs().get(var);
//			boolean foundSatisfiableJoinCondition = false; //ebs.isEmpty();
//			RestrictionSet ras = new RestrictionSet(false);
//
//			for(VarDef ea : a.getSparqlVarToExprs().get(var)) {
//				
//				
//				// If there are no expressions to join on, then we retain the variable
//				RestrictionSet ra = ea.getRestrictions();
//				
//				//Restriction ra = (Restriction)ea.getRestriction().clone();
//				
//				for(VarDef eb : ebs) {
//
//					// Check if the constraints associated with the termDefs are unsatisfiable
//					RestrictionSet rc = ra.clone();
//					RestrictionSet rb = eb.getRestrictions();
//
//					rc.stateRestriction(rb);
//
//					if(rc.isUnsatisfiable()) {
//						continue;
//					}
//					
//					ras.addAlternatives(rc);
//					
//					// TODO Update restriction
//					//result.getSparqlVarToExprs().put(var, new TermDef(ea.getExpr(), ra));
//
//					
//					// substitute renamed variables in exprb
//					
//					// For common variables we need to chose one expression for the
//					// projection.
//					// Here we always use the first			
//					Expr tmp = new E_Equals(ea.getExpr(), eb.getExpr());
//					
//					
//					// Essentially add ... AND(EQUALS(ea.getDiscriminatorColumnName(), ea.getDCValue()) 
//					//if(true) { throw new RuntimeException("Add support for discriminator column"); }
//					
//					
//					
//					/*
//					Expr functionExpand = FunctionExpander.transform(tmp);
//					Expr constantExpand = ConstantExpander.transform(functionExpand);
//					Expr expand = expander.transformMM(constantExpand);
//					Expr optimized = SqlExprOptimizer.optimizeMM(constantExpand);
//					*/
//
//
//					// Substitute all variables with sql bridges
//					// TODO Do the variable transformation
//					// Then push down
//					
//					//Expr tmp = sub.transformMM(expr);
//					Expr optimized = SqlExprOptimizer.optimizeMM(tmp);					
//					Expr substituted = substitutor.transformMM(optimized);
//					Expr pushedExpr = PushDown.pushDownMM(substituted);
//
//					SqlExpr sqlExpr = null;
//					if(pushedExpr instanceof ExprSqlBridge) {
//						sqlExpr = ((ExprSqlBridge) pushedExpr).getSqlExpr();
//					} else {
//						throw new RuntimeException("Could not push an expression");
//					}
//					
//					if(isSatisfiable(sqlExpr)) {
//						ors.add(sqlExpr);
//						foundSatisfiableJoinCondition = true;
//					}
//				}
//				
//				newTermDefs.add(new VarDef(ea.getExpr(), ras));
//			}
//
//			result.getSparqlVarToExprs().putAll(var, newTermDefs);
//
//			// For the given variable there is no satisfiable join
//			// so the join is empty
//			if(!foundSatisfiableJoinCondition) {
//				SqlNodeOld x = new SqlNodeEmpty();
//				x.getSparqlVarToExprs().putAll(result.getSparqlVarToExprs());
//				x.getAliasToColumn().putAll(result.getAliasToColumn());
//				return x;
//			}
//			
//			
//			// Or-ify the expressions
//			if(!ors.isEmpty()) {
//				SqlExpr joinExpr = SqlExprUtils.orifyBalanced(ors);
//				
//				result.addCondition(joinExpr);
//			}
//		}			
//
//		// Check for unsatisfiable node
//		// TODO We need to consider the join condition and the filters
//		// as a unit (andify them); Example:
//		// join ... on(a = b) where (a != b)
//		// Treating the filter and the join condition separately causes us
//		// to miss the unsatisfiability in this case
//		if(!isSatisfiable(result.getConditions())) {
//			SqlNodeOld x = new SqlNodeEmpty();
//			x.getSparqlVarToExprs().putAll(result.getSparqlVarToExprs());
//			x.getAliasToColumn().putAll(result.getAliasToColumn());
//			return x;
//		}
//		
//		// Of the helper variables, only copy those over, which are still
//		// referenced
//
//		return result;
//	}
//
//	public static SqlNodeOld distinct(SqlNodeOld a) {
//		SqlDistinct result = new SqlDistinct(a.getAliasName(), a);
//
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
//
//		return result;
//	}
//	
//	public static SqlNodeOld slice(SqlNodeOld a, ColRelGenerator generator, long start, long length) {
//
//		SqlSlice result = new SqlSlice(a, start, length);
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
//		
//		/*
//		String aliasName = a.getAliasName() != null
//				? a.getAliasName()
//				: generator.next();
//
//		SqlSlice result = new SqlSlice(aliasName, a, start, length);
//
//		if(a.getAliasName() == null) {
//			SqlNode newProj = createNewAlias(aliasName, a, generator);
//			result.getAliasToColumn().putAll(newProj.getAliasToColumn());
//			result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//
//		} else {
//			result.getAliasToColumn().putAll(a.getAliasToColumn());
//			result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
//		}
//		*/
//		
//		
//		return result;
//	}
//
//	
//	
//	
//	/**
//	 * 
//	 * Select b.i i, b.j j From (
//	 *     Select a.i i, a.j j From ( ... ) a
//	 * ) b
//	 * 
//	 * 
//	 * Updates all references in the target node.
//	 * The sparqlVarsToExpr map just needs to be copied
//	 * For the 
//	 * 
//	 * @param alias
//	 * @param target
//	 * @param source
//	 */
//	public static void createNewAlias(String alias, SqlNodeOld target, SqlNodeOld source) {
//		
//		target.getSparqlVarToExprs().putAll(source.getSparqlVarToExprs());
//
//		
//		for(Entry<String, SqlExpr> entry : source.getAliasToColumn().entrySet()) {
//			target.getAliasToColumn().put(entry.getKey(), new SqlExprColumn(source.getAliasName(), entry.getKey(), entry.getValue().getDatatype()));
//		}
//	}
//	
//
//	
//	public static void replaceAlias(String alias, SqlNodeOld node, ColRelGenerator columnNameColRelGenerator) {
//		SqlNodeOld tmp = createNewAlias(alias, node, columnNameColRelGenerator);
//		
//		node.getAliasToColumn().clear();
//		node.getSparqlVarToExprs().clear();
//		
//		node.getAliasToColumn().putAll(tmp.getAliasToColumn());
//		node.getSparqlVarToExprs().putAll(tmp.getSparqlVarToExprs());
//	}
//
//
//	public static SqlAlias createNewAlias(String alias, SqlNodeOld node, ColRelGenerator generator) {
//		SqlAlias result = new SqlAlias(alias, node);
//		
//		Map<Var, Expr> varRename = new HashMap<Var, Expr>();
//    	for(Entry<String, SqlExpr> col : node.getAliasToColumn().entrySet()) {
//    		String newColumnName = col.getKey();//generator.nextColumn();
//    		
//    		varRename.put(Var.alloc(col.getKey()), new ExprVar(newColumnName));
//    		result.getAliasToColumn().put(newColumnName, new SqlExprColumn(alias, col.getKey(), col.getValue().getDatatype()));
//    	}
//    	
//    	NodeExprSubstitutor substitutor = new NodeExprSubstitutor(varRename);
//
//    	for(Entry<Var, VarDef> entry : node.getSparqlVarToExprs().entries()) {
//    		Expr newExpr = substitutor.transformMM(entry.getValue().getExpr());
//    		
//    		result.getSparqlVarToExprs().put(entry.getKey(), new VarDef(newExpr, entry.getValue().getRestrictions()));
//    	}
//		
//    	
//    	return result;
//	}
//
//
//	public static SqlProjection wrapWithProjection(String newAlias, SqlNodeOld tmp, ColRelGenerator generator) {
//		
//		SqlProjection node = new SqlProjection(newAlias, tmp);
//		node.getAliasToColumn().putAll(tmp.getAliasToColumn());
//		node.getSparqlVarToExprs().putAll(tmp.getSparqlVarToExprs());
//		
//		SqlProjection result = new SqlProjection(newAlias, node);
//
//		SqlAlias newProj = createNewAlias(newAlias, node, generator);
//		
//		result.getAliasToColumn().putAll(newProj.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//		
//		/*
//		Map<Var, Expr> varRename = new HashMap<Var, Expr>();
//    	for(Entry<String, SqlExpr> col : node.getAliasToColumn().entrySet()) {
//    		String newColumnName = generator.next();
//    		
//    		varRename.put(Var.alloc(col.getKey()), new ExprVar(newColumnName));
//    		result.getAliasToColumn().put(newColumnName, new SqlExprColumn(newAlias, col.getKey(), col.getValue().getDatatype()));
//    	}
//    	
//    	NodeExprSubstitutor substitutor = new NodeExprSubstitutor(varRename);
//
//    	for(Entry<Var, TermDef> entry : node.getSparqlVarToExprs().entries()) {
//    		Expr newExpr = substitutor.transformMM(entry.getValue().getExpr());
//    		
//    		result.getSparqlVarToExprs().put(entry.getKey(), new TermDef(newExpr, entry.getValue().getRestrictions()));
//    	}
//    	*/
//
//    	
//    	return result;
//	}
//
//	
//	public static SqlNodeOld extend(SqlNodeOld node, VarExprList varExprList) {
//
//
//		for(Entry<Var, Expr> entry : varExprList.getExprs().entrySet()) {
//			
//			Var var = entry.getKey();
//			Expr expr = entry.getValue();
//			
//			if(expr.isVariable()) {
//				Var otherVar = expr.asVar();
//				node.getSparqlVarToExprs().putAll(var, node.getSparqlVarToExprs().get(otherVar));
//			} else {
//				throw new RuntimeException("Implement me");
//			}
//			
//			//SqlExprList sqlExprs = shallowPushX(entry.getValue(), node.getSparqlVarToExprs(), node.getAliasToColumn());
//			//node.getAliasToColumn()
//			
//			
//		}
//		return node;
//	}
//
//	
//	public static SqlExprList shallowPushX(Expr expr, Multimap<Var, VarDef> sparqlVarToExprs, Map<String, SqlExpr> aliasToColumn) {
//		SqlExprList result = new SqlExprList();
//		if(expr.isVariable()) {
//			Var v = expr.asVar();
//			
//			Collection<VarDef> defs = sparqlVarToExprs.get(v);
//			
//			
//			if(defs.isEmpty()) {
//				logger.warn("Variable does not exist for sorting");
//				//continue;
//				return null;
//			} else if(defs.size() > 1) {
//				throw new RuntimeException("Should not happen"); // because we grouped by the var
//			}
//			
//			VarDef def = defs.iterator().next();
//			
//			Expr e = def.getExpr();
//			if(e instanceof E_RdfTerm) {
//				E_RdfTerm term = (E_RdfTerm)e;
//
//				result = new SqlExprList();
//				for(int i = 0; i < 4; ++i) {
//					Expr arg = term.getArgs().get(i);
//					SqlExpr sqlExpr = forceShallowPushDown(arg, aliasToColumn);
//					//SqlExpr sqlExpr = fullPush(arg, a);
//					
//					//SqlExpr sqlExpr = shallowPush(arg, a);
//					
//					result.add(sqlExpr);
//				}
//			} else {
//				throw new RuntimeException("Should not happen");
//			}
//		}
//		else {
//				result = shallowPush(new ExprList(expr), sparqlVarToExprs, aliasToColumn);
//			//pushed = fullPush(new ExprList(condition.getExpression()), a);
//			}
//		
//		return result;
//	}
//	
//	/**
//	 * Ordering requires some wrapping:
//	 * 
//	 * Given Order(subNode), we transform to SqlAlias(SqlProjection(Order(Alias(subNode)))
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * First, we add a projection to whatever node we have
//	 * Then, in this projection we group by the variables we are sorting
//	 * Then we wrap this projection
//	 * Finally, we create an SqlOrderBy node with the order by expressions
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * @param a
//	 * @param conditions
//	 * @param generator
//	 * @return
//	 */
//	public static SqlNodeOld order(SqlNodeOld a, List<SortCondition> conditions, ColRelGenerator generator) {
//		List<SqlSortCondition> sqlConditions = new ArrayList<SqlSortCondition>();
//
//
//		/*
//		if(a.getAliasName() == null) {
//			//a = wrapWithProjection(generator.next(), a, generator);
//			String newAlias = generator.nextRelation();
//			SqlNode newProj = createNewAlias(newAlias, a, generator);
//			a = new SqlProjection(newAlias, a);
//			a.getAliasToColumn().putAll(newProj.getAliasToColumn());
//			a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//
//		}*/
//		
//
//		// If the node does not have an alias yet, create one
//		//if(a.getAliasName() == null) {
//		//}
//		
//			
//		SqlNodeOld subSelect = createNewAlias(generator.nextRelation(), a, generator);
//
//		// Wrap whatever we have with a new projection (-> sub select)
//		// This new projection is the basis for the group by
//		/*
//		SqlProjection groupByNode = new SqlProjection(generator.nextRelation(), projection);
//    	SqlSelectBlockCollector.copyProjection(groupByNode, tmp);
//    	*/
//		SqlNodeOld groupByNode = subSelect;
//
//		//SqlNode projection = subSelect;
//
//    	SqlNodeOld tmp = createNewAlias(groupByNode.getAliasName(), subSelect, generator);
//
//		
//		// Transform the conditions
//		Generator genG = Gensym.create("s");
//    	for(SortCondition condition : conditions) {
//
//    		
//    		// SOMEWHAT HACKY: We need to avoid name clashes when grouping
//    		// A really hard to find bug was, the when e.g. grouping c_{5-10}, it might have
//    		// redefined e.g. c_1 
//    		// Group by the variables
//    		for(Var var : condition.getExpression().getVarsMentioned()) {
//    			SqlAlgebraToString.groupBy(var, groupByNode, tmp, genG); //generator.forColumn());
//    		}
//    	}
//    	
//
//    	/*
//    	if(true) {
//    		return groupByNode;
//    	}*/
//    	
//    	// TODO Somewhere in the below code it happens, that the projection gets messed up (resulting in e.g. <1> being returned as a property)
//    	
//    	// Now we have created the "group by projection"
//    	// However, now we need to wrap it with another projection, in which we can add any expressions appearing in the order by
//		SqlProjection orderByNode = new SqlProjection(generator.nextRelation(), groupByNode);
//    	SqlNodeOld tmp2 = createNewAlias(orderByNode.getAliasName(), groupByNode, generator);
//    	SqlSelectBlockCollector.copyProjection(orderByNode, tmp2);
//    	
//    	
//    		// order by ...  str(?o)
//    		// with ?o = uri(concat())
//    		
//    	
//    	// Create a new projection
//
//    	/*
//    	String orderAlias = generator.nextRelation();
//    	SqlProjection orderNode = new SqlProjection(orderAlias, projection);
//    	createNewAlias(projection.getAliasName(), orderNode, projection);
//    	*/
//    	//SqlSelectBlockCollector.copyProjection(orderNode, projection);
//
//    	
//    	
//    	//SqlNode orderNode = wrapWithProjection(projection, generator); //new SqlProjection(projection.getAliasName(), projection);
//    	
//
//    	SqlNodeOld result = new SqlNodeOrder(orderByNode.getAliasName(), orderByNode, sqlConditions);
//	
//
//		Generator gen = Gensym.create("o");
//
//		// Build the sort conditions for our current node
//    	for(SortCondition condition : conditions) {
//    		
//    		SqlExprList pushed = shallowPushX(condition.getExpression(), orderByNode.getSparqlVarToExprs(), orderByNode.getAliasToColumn());
//
//    		if(pushed == null) {
//    			continue;
//    		}
//    		
//    		//SqlExprList pushed = forcePushDown(new ExprList(condition.getExpression()), a);
//    		
//    		for(SqlExpr sqlExpr : pushed) {
//        		// Don't sort by constant expression
//    			Set<SqlExprColumn> columnsMentioned = SqlExprBase.getColumnsMentioned(sqlExpr); 
//    			if(columnsMentioned.isEmpty()) {
//        			continue;
//        		}
//        		
//    			boolean allowExprsInOrderByClause = false;
//    			// Sql cannot order by columns that are not selected;
//    			// Therefore, any order expression becomes part of the projection
//    			// TODO Implement this properly!!!
//    			if(allowExprsInOrderByClause) {
//    				sqlConditions.add(new SqlSortCondition(sqlExpr, condition.getDirection()));
//    			} else {
//    				String dummyColumn = gen.next(); //generator.nextColumn();
//
//    				orderByNode.getAliasToColumn().put(dummyColumn, sqlExpr);
//    				
//    				sqlConditions.add(new SqlSortCondition(new SqlExprColumn(null, dummyColumn, sqlExpr.getDatatype()), condition.getDirection()));
//    				
//    				//result.getAliasToColumn().put(dummyColumn, new SqlExprColumn(projection.getAliasName(), dummyColumn, sqlExpr.getDatatype()));
//    			}
//    		}
//    	}
//    		
//    		/*
//    		for(Var var : condition.getExpression().getVarsMentioned()) {
//    			for(Expr expr : a.getSparqlVarToExprs().asMap().get(var)) {
//    				for(Var columnName : expr.getVarsMentioned()) {
//    					
//    					
//    					
//    					
//    					SqlExpr sqlExpr = a.getAliasToColumn().get(columnName.getName());
//    					
//    					
//    					
//    					String exprStr = sqlExprSerializer.serialize(sqlExpr);
//    					
//    					if(dirStr != null) {
//    						//exprStr = dirStr + "(" + exprStr + ")";
//    						exprStr = exprStr + " " + dirStr;
//    					}
//    					
//    					sortColumnExprStrs.add(exprStr);
//    				}
//    			}
//    		}
//    	}
//    	*/
//		
//		/*
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
//		 */
//
//		
//		//SqlProjection r2 = new SqlProjection(aliasName, orderNode);
//		//createNewAlias(aliasName, r2, orderNode);
//
//		result.getAliasToColumn().putAll(orderByNode.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(orderByNode.getSparqlVarToExprs());
//		return result;
//
//		
//
//		/*
//		SqlProjection wrap = new SqlProjection(generator.nextRelation(), result);
//    	SqlNode tmp3 = createNewAlias(wrap.getAliasName(), result, generator);
//    	SqlSelectBlockCollector.copyProjection(wrap, tmp3);
//
//    	return wrap;
//    	*/
//	}
//	
//		
//	/*
//	public static SqlExpr fullPush(Expr expr, SqlNode node) {
//		return fullPush(new ExprList(exprs), node.getAliasToColumn(), node.getSparqlVarToExprs());
//	}
//	
//	public static SqlExprList fullPush(ExprList exprs, Map<String, SqlExpr> aliasToColumn, Multimap<Node, Expr> sparqlVarToExprs) {
//	}*/
//	
//	
//	/*
//	public static SqlExpr shallowPush(Expr expr, SqlNode node) {
//		return shallowPush(new ExprList(expr), node.getSparqlVarToExprs(), node.getAliasToColumn()).get(0);
//	}
//	*/
//	
//	
//	public static Map<String, SqlExpr> createShallowAliasToColumn(Map<String, SqlExpr> aliasToColumn) {
//		
//		Map<String, SqlExpr> result = new HashMap<String, SqlExpr>();
//		for(Entry<String, SqlExpr> entry : aliasToColumn.entrySet()) {
//			result.put(entry.getKey(), new SqlExprColumn(null, entry.getKey(), entry.getValue().getDatatype()));
//		}
//
//		return result;
//	}
//	
//	/**
//	 * Like a full push, except it does not replace sql-columns with their definition
//	 * 
//	 * @param exprs
//	 * @param node
//	 * @return
//	 */	
//	public static SqlExprList shallowPush(ExprList exprs, Multimap<Var, VarDef> sparqlVarToExprs, Map<String, SqlExpr> aliasToColumn) {//{SqlNode node) {
//		
//		Map<String, SqlExpr> shallowAliasToColumn = createShallowAliasToColumn(aliasToColumn);
//		
//		return fullPush(exprs, shallowAliasToColumn, sparqlVarToExprs);
//	}
//	
//	
//	public static SqlExpr fullPush(Expr expr, SqlNodeOld node) {
//		// Could be a bit more efficient...
//		SqlExprList tmp = fullPush(new ExprList(expr), node);
//		if(tmp.isEmpty()) {
//			throw new RuntimeException("Should not happen");
//		}
//		
//		return tmp.get(0);
//	}
//	
//	public static SqlExprList fullPush(ExprList exprs, SqlNodeOld node) {
//		return fullPush(exprs, node.getAliasToColumn(), node.getSparqlVarToExprs());
//	}
//	
//	
//	
//	public static SqlExprList fullPush(ExprList exprs, Map<String, SqlExpr> aliasToColumn, Multimap<Var, VarDef> sparqlVarToExprs) {
//		
//		NodeExprSubstitutor substitutor = aliasToColumn == null
//				? null
//			    : createSubstitutor(aliasToColumn);
//
//		SqlExprList result = new SqlExprList();
//		
//		for (Expr expr : exprs) {
//			
//			List<Var> vars = new ArrayList<Var>(expr.getVarsMentioned());
//			List<List<VarDef>> cartesianBase = new ArrayList<List<VarDef>>();
//			
//			// Substitute each sparql variable with its set of definitions on the sql level
//			for(Var var : vars) {
//				Collection<VarDef> varExprs = sparqlVarToExprs.get(var);
//				cartesianBase.add(new ArrayList<VarDef>(varExprs));
//			}
//			
//			CartesianProduct<VarDef> cartesian = new CartesianProduct<VarDef>(cartesianBase);
//			
//
//			List<SqlExpr> ors = new ArrayList<SqlExpr>();
//			for(List<VarDef> items : cartesian) {
//				
//				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
//				for(int i = 0; i < vars.size(); ++i) {
//					Var var = vars.get(i);
//					VarDef item = items.get(i);
//					
//					expanderMap.put(var, item.getExpr());
//				}
//
//				NodeExprSubstitutor expander = new NodeExprSubstitutor(expanderMap);
//				
//				
//				
//				/*
//				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
//				for(Entry<Node, Expr> entry : node.getSparqlVarToExprs().entrySet()) {
//					expanderMap.put(Var.alloc(entry.getKey()), entry.getValue());
//				}*/
//
//				/*
//				if(expr instanceof E_LessThan) {
//					System.out.println("lessthan here");
//				}
//				*/
//				
//				// Expand variables in the filter expression
//				// Example: Given ?r = term(...) and Filter(regex(?r...))
//				//          We will get Filter(regex(term(...)))
//				
//				
//				Expr functionExpand = FunctionExpander.transform(expr);
//				Expr constantExpand = ConstantExpander.transform(functionExpand);
//				Expr expand = expander.transformMM(constantExpand);
//				Expr simplified = SqlExprOptimizer.optimizeMM(expand);
//				
//				// Expr tmp = sub.transformMM(expr);
//				// TODO Do the variable transformation
//
//				Expr subbed = (substitutor == null)
//						? simplified
//						: substitutor.transformMM(simplified);
//						
//				Expr pushed = PushDown.pushDownMM(subbed);
//
//				SqlExpr sqlExpr = null;
//				if(pushed instanceof ExprSqlBridge) {
//					sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
//				} else {
//					throw new RuntimeException("Could not push expressions");
//				}
//
//				ors.add(sqlExpr);			
//			}
//			
//			SqlExpr orified = SqlExprUtils.orifyBalanced(ors);
//			// TODO Should we assume false or true?
//			if(orified != null) {
//				result.add(orified);
//			}
//		}
//
//		// FIXME We could bail out earlier
//		if(!isSatisfiable(result)) {
//			return new SqlExprList(SqlExprValue.FALSE);
//		}
//
//		
//		// Remove all 'true' from the result
//		// In Postgres 9.1, a "WHERE TRUE" in a union prevents the union to become an append-relation
//		// As a result, in LinkedGeoData "Select { ?s dc:modified ?o . } Order By ?s" won't work (efficiently)
//		// FIXME Although it makes sense to already get rid of unnecessary exprs here,
//		// it might be good to have a full SQL-level optimization at some later stage
//		Iterator<SqlExpr> it = result.iterator();
//		while(it.hasNext()) {
//			SqlExpr item = it.next();
//			if(item.equals(SqlExprValue.TRUE)) {
//				it.remove();
//			}
//		}
//		
//		
//		return result;
//	}
//
//	
//	public static boolean isSatisfiable(SqlExpr sqlExpr)
//	{
//		Expr expr = SqlExprToExpr.convert(sqlExpr);
//		if(expr.equals(SqlExprToExpr.UNKNOWN)) {
//			return true;
//		}
//		
//		
//		Set<Set<Expr>> dnf = DnfUtils.toSetDnf(expr);
//		
//		return DnfUtils.isSatisfiable(dnf);
//	}
//	
//	public static boolean isSatisfiable(SqlExprList sqlExprs) {
//		if(sqlExprs.isEmpty()) {
//			return true;
//		}
//		
//		for(SqlExpr sqlExpr : sqlExprs) {
//			if(!isSatisfiable(sqlExpr)) {
//				return false;
//			}
//		}
//		
//		return true;
//	}
//
//	public static SqlNodeOld group(SqlNodeOld a, VarExprList groupVars, List<ExprAggregator> exprAggregator, Generator colGenerator) {
//		
//		NodeExprSubstitutor substitutor = createSubstitutor(a.getAliasToColumn());
//
//		// TODO: Somehow extend the aggregator with RDF term
//
//		List<SqlExprAggregator> sqlAggregators = new ArrayList<SqlExprAggregator>();
//		SqlGroup result = new SqlGroup(a, sqlAggregators);
//
//		for(ExprAggregator item : exprAggregator) {
//
//			//SqlExprAggregator sqlExprAggregator = PushDown.
//			Expr expr = item.getExpr();
//			
//			SqlExprAggregator sqlExpr = (SqlExprAggregator)forcePushDown(expr, substitutor);
//			sqlAggregators.add(sqlExpr);
//			
//			String newColAlias = colGenerator.next();
//
//			
//			Expr exprVar = new ExprVar(Var.alloc(newColAlias));
//			Expr rdfTerm = E_RdfTerm.createTypedLiteral(exprVar, NodeValue.makeNode(sqlExpr.getDatatype().getXsd()));
//			
//			result.getSparqlVarToExprs().put(item.getVar(), new VarDef(rdfTerm));
//			result.getAliasToColumn().put(newColAlias, sqlExpr);
//
//			//System.out.println(sqlExpr);
//		}
//		
//		//for(SqlExprAggregator item : sqlAggregators) {
//		//}
//		
//		
//		//throw new RuntimeException("Implement me");
//		return result;
//	}
//
//	/**
//	 * Replaces the projection to refer to a new alias
//	 * 
//	 * @param a
//	 * @param newAlias
//	 * @param generator
//	 */
//	public static void updateProjection(SqlNodeOld a, String newAlias, ColRelGenerator generator) {
//		SqlNodeOld newProj = createNewAlias(newAlias, a, generator);
//		a = new SqlProjection(newAlias, a);
//		a.getAliasToColumn().putAll(newProj.getAliasToColumn());
//		a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//	}
//	
//	// NOTE Does in place transformation!
//
//	
//	public static SqlNodeOld project(SqlNodeOld a, List<Var> vars, ColRelGenerator generator) {
//		//return projectInPlace(a, vars, generator);
//		return projectWrap(a, vars, generator);
//	}
//	
//	public static SqlNodeOld projectInPlace(SqlNodeOld result, List<Var> vars, ColRelGenerator generator) {
//		
//		Set<String> referencedColumns = new HashSet<String>();
//		
//		
//		// Track all columns that contribute to the construction of SQL variables
//		for(Var var : vars) {
//			for(VarDef def : result.getSparqlVarToExprs().get(var)) {
//				for(Var item : def.getExpr().getVarsMentioned()) {
//					referencedColumns.add(item.getName());
//				}
//			}
//		}
//		
//		
//	
//		result.getAliasToColumn().keySet().retainAll(referencedColumns);
//		result.getSparqlVarToExprs().keySet().retainAll(vars);
//		
//		return result;
//		
//	}
//	
//	
//	public static SqlNodeOld projectWrap(SqlNodeOld a, List<Var> vars, ColRelGenerator generator) {
//
//		/*
//		if(a.getAliasName() == null) {
//
//			if(a instanceof SqlJoin || a instanceof SqlMyRestrict || a instanceof SqlProjection) {				
//				SqlNode newProj = new SqlProjection(newAlias, a);
//				newProj.getAliasToColumn().putAll(a.getAliasToColumn());
//				newProj.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
//				a = newProj;
//			} else {
//				SqlNode newProj = createNewAlias(newAlias, a, generator);
//				a = new SqlProjection(newAlias, a);
//				a.getAliasToColumn().putAll(newProj.getAliasToColumn());
//				a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//			}
//			//a = new Projection() 
//			//a = wrapWithProjection(generator.next(), a, generator);
//			//replaceAlias(alias, node, columnNameColRelGenerator)
//			
//			SqlNode newProj = createNewAlias(newAlias, a, generator);
//			a = new SqlProjection(newAlias, a);
//			a.getAliasToColumn().putAll(newProj.getAliasToColumn());
//			a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//		}
//		*/
//		String newAlias = generator.nextRelation();
//
//		
//		SqlNodeOld newProj = createNewAlias(newAlias, a, generator);
//		SqlProjection result = new SqlProjection(newAlias, a);
//		result.getAliasToColumn().putAll(newProj.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
//		
//		/*
//		SqlProjection result = new SqlProjection(a.getAliasName(), a);
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		*/
//		//createNewAlias(a.getAliasName(), result, a);
//		
//		
//		Set<String> referencedColumns = new HashSet<String>();
//		
//		
//		// Track all columns that contribute to the construction of SQL variables
//		for(Var var : vars) {
//			for(VarDef def : result.getSparqlVarToExprs().get(var)) {
//				for(Var item : def.getExpr().getVarsMentioned()) {
//					referencedColumns.add(item.getName());
//				}
//			}
//		}
//		
//		
//	
//		result.getAliasToColumn().keySet().retainAll(referencedColumns);
//		result.getSparqlVarToExprs().keySet().retainAll(vars);
//		
//		return result;
//	}
//	
//	
//	public static SqlNodeOld filter(SqlNodeOld a, ExprList exprs, ColRelGenerator generator) {
//
//		SqlExprList sqlExprs = fullPush(exprs, a);
//
//		SqlNodeOld result;
//
//		/*
//		if(a.getAliasName() == null) {
//			String newAlias = generator.next();
//			SqlNode newProj = new SqlProjection(newAlias, a);
//			newProj.getAliasToColumn().putAll(a.getAliasToColumn());
//			newProj.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
//			a = newProj;
//		}
//		*/
//
//		
//		if(a instanceof SqlNodeEmpty || !isSatisfiable(sqlExprs)) {
//			fullPush(exprs, a);
//			result = new SqlNodeEmpty();
//		} else {
//
//			SqlMyRestrict tmp;
//			if(a instanceof SqlMyRestrict) {
//				tmp = (SqlMyRestrict)a;
//			}
//			else {				
//				//tmp = new SqlMyRestrict(a.getAliasName(), a);
//				tmp = new SqlMyRestrict(null, a);
//			}
//
//			tmp.getConditions().addAll(sqlExprs);
//		
//			result = tmp;
//		}		
//
//		
//		//SqlExprToExpr.convert(expr)
//		
//		//NodeExprSubstitutor expander = createSparqlVarExpander(a);
//
//
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
//
//		return result;
//	}
//
//	
//	/**
//	 * 
//	 * 
//	 * Each variable may have multiple expressions attached to it.
//	 * As a consequence, each combination of expressions has to be
//	 * evaluated.
//	 * 
//	 * @param a
//	 * @param exprs
//	 * @return
//	 */
//	/*
//	public static SqlNode filter(SqlNode a, ExprList exprs) {
//
//		SqlMyRestrict result;
//		if(a instanceof SqlMyRestrict) {
//			result = (SqlMyRestrict)a;
//		}
//		else {
//			result = new SqlMyRestrict(a.getAliasName(), a);
//		}
//		
//		//NodeExprSubstitutor expander = createSparqlVarExpander(a);
//		NodeExprSubstitutor substitutor = createSubstitutor(a);
//
//		for (Expr expr : exprs) {
//			
//			List<Var> vars = new ArrayList<Var>(expr.getVarsMentioned());
//			List<List<Expr>> cartesianBase = new ArrayList<List<Expr>>();
//			
//			for(Var var : vars) {
//				Collection<Expr> varExprs = a.getSparqlVarToExprs().get(var);
//				cartesianBase.add(new ArrayList<Expr>(varExprs));
//			}
//			
//			CartesianProduct<Expr> cartesian = new CartesianProduct<Expr>(cartesianBase);
//			
//
//			List<SqlExpr> ors = new ArrayList<SqlExpr>();
//			for(List<Expr> items : cartesian) {
//				
//				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
//				for(int i = 0; i < vars.size(); ++i) {
//					Var var = vars.get(i);
//					Expr item = items.get(i);
//					
//					expanderMap.put(var, item);
//				}
//
//				NodeExprSubstitutor expander = new NodeExprSubstitutor(expanderMap);
//				
//				
//				
//				/*
//				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
//				for(Entry<Node, Expr> entry : node.getSparqlVarToExprs().entrySet()) {
//					expanderMap.put(Var.alloc(entry.getKey()), entry.getValue());
//				}* /
//
//				
//				// Expand variables in the filter expression
//				// Example: Given ?r = term(...) and Filter(regex(?r...))
//				//          We will get Filter(regex(term(...)))
//				Expr functionExpand = FunctionExpander.transform(expr);
//				Expr constantExpand = ConstantExpander.transform(functionExpand);
//				Expr expand = expander.transformMM(constantExpand);
//				Expr simplified = SqlExprTranslator.optimizeMM(expand);
//				
//				// Expr tmp = sub.transformMM(expr);
//				// TODO Do the variable transformation
//				Expr subbed = substitutor.transformMM(simplified); 
//				Expr pushed = PushDown.pushDownMM(subbed);
//
//				SqlExpr sqlExpr = null;
//				if(pushed instanceof ExprSqlBridge) {
//					sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
//				} else {
//					throw new RuntimeException("Could not push expressions");
//				}
//
//				ors.add(sqlExpr);			
//			}
//			
//			SqlExpr orified = SqlExprUtils.orifyBalanced(ors);
//			result.getConditions().add(orified);
//		}
//
//		result.getAliasToColumn().putAll(a.getAliasToColumn());
//		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
//
//		return result;
//	}
//*/
//	
//	
//	/**
//	 * Given two expressions, walk them in a top down fashion and return: .) the
//	 * expression that is common to both, where the leaves where there were
//	 * differences contain helper variables
//	 * 
//	 * .) For both expression, a mapping of helper variable to the sub
//	 * expression which would in combination with the common part yield the
//	 * original expression again.
//	 * 
//	 * i = f(g(h(a, b)) j = f(g(h(c, d))
//	 * 
//	 * becomes: common = f(g(h(x, y))
//	 * 
//	 * a.x = a a.y = b
//	 * 
//	 * b.x = c b.y = d
//	 * 
//	 * Note: A helper variable correspond to a single sql colum
//	 * 
//	 * Ideally, if we had a union of the same uris generated in the same way
//	 * from different relations, this would allow us to end up with a simple
//	 * union:
//	 * 
//	 * 
//	 * 
//	 * 
//	 * @param a
//	 * @param b
//	 */
//	public void unifyCommonExpression(Expr a, Expr b) {
//
//	}
//
//	/**
//	 * New approach:
//	 * Only combine columns if they have the same datatype AND result in the same sparql var expression.
//	 * 
//	 * In the case of constants we add a discriminator column.
//	 * 
//	 * 
//	 * 
//	 * 
//	 * 
//	 * Old approach: (Top-Down factoring out)
//	 * 
//	 * Problem: We want to create an SQL statement corresponding to the union of
//	 * two SqlNodeBindings:
//	 * 
//	 * Select id From A; ?a = Uri(Concat("prefix", id)) Select c1, c2 From B; ?a
//	 * = Uri(Concat(?c1, ?c2))
//	 * 
//	 * This requires aligning the RDF variables:
//	 * 
//	 * Select Concat("prefix") As a_lex From (Select id From A); ?a =
//	 * Uri(unionAlias.a_lex) Select Concat(c1, c2) As a_lex From (Select c1, c2
//	 * From B); ?a = Uri(unionAlias.a_lex)
//	 * 
//	 * -> Select ... As a_lex ... Union All Select ... As a_lex
//	 * 
//	 * Union(Union(Union(Filter(A), Filter(B)), Filter(C))
//	 * 
//	 * 
//	 * We are trying to keep the size of the projections minimal: . Only select
//	 * non-static data from the underlying relation, for constants, keep them in
//	 * the ?a = RdfTerm(const, ...) expression. . Factor out common sub
//	 * expressions for common variables: Example: Assuming there is a union C of
//	 * A and B, ?A.x = expr(a.id) and ?B.x = expr'(b.id) So we are looking for
//	 * expressions that are equalTo each other, based on a given mapping of
//	 * variables
//	 * 
//	 * We can keep the binding ?C.x = expr(c.id), so there is no need to
//	 * translate the expr to sql. TODO We must consider datatypes when doing
//	 * this!!!
//	 *
//	 * 
//	 * Dealing with datatypes:
//	 * A sparql variable may get its value assigned from different expressions.
//	 * 
//	 * Example:
//	 * ?p = Uri(concat(?prefixColumn, ?id))
//	 * ?p = PlainLiteral(?name)
//	 * 
//	 * ?prefixColumn string
//	 * id int
//	 * name int
//	 * 
//	 * 
//	 * a) Group expressions by variables,
//	 * b) For each variable: group the datatypes they make use of.
//	 * 
//	 * But then we have to find common expressions within each datatype group.
//	 * An unoptimized version would be: for each expression make sure that
//	 * it references a unique set of columns
//	 * 
//	 * So... we group the expressions by datatype, and within the group we check
//	 * what we can factor out.
//	 * 
//	 * 
//	 * 
//	 * 
//	 * @param a
//	 * @param b
//	 * @return
//	 */
//	public static SqlNodeOld unionNew(ColRelGenerator generator, List<SqlNodeOld> sqlNodes) {
//
//		// Prepare the data structures from which the
//		// result node will be created
//		Multimap<Var, VarDef> commons = HashMultimap.create();
//		
//		// For each union member, prepare a datastructe for its new projection
//		List<Multimap<Var, VarDef>> projections = new ArrayList<Multimap<Var, VarDef>>();
//		for (int i = 0; i < sqlNodes.size(); ++i) {
//			Multimap<Var, VarDef> tmp = HashMultimap.create();
//			projections.add(tmp);
//		}
//
//		// Now we can start with the actual work				
//		Multimap<Var, Integer> varToSqlNode = HashMultimap.create();
//
//		// Map each variable to the set of corresponding nodes
//		for (int i = 0; i < sqlNodes.size(); ++i) {
//			SqlNodeOld sqlNode = sqlNodes.get(i);
//			for (Node var : sqlNode.getSparqlVarsMentioned()) {
//				varToSqlNode.put((Var)var, i);
//			}
//		}
//
//		
//		// If a variable maps to a constant, than the mapping does not apply to any union member
//		// that does not define the constant.
//		// This means we have to introduce a column for discrimination, which contains NULL for
//		// all union members where to constaint is not applicable 		
//		Generator aliasGen = Gensym.create("c");
//		ExprCommonFactor factorizer = new ExprCommonFactor(aliasGen);
//
//		
//		Map<String, SqlDatatype> allColumnsToDatatype = new HashMap<String, SqlDatatype>();
//		
//		
//		// For each variable, cluster the corresponding expressions
//		for(Entry<Var, Collection<Integer>> entry : varToSqlNode.asMap().entrySet()) {
//			Var var = entry.getKey();
//			
//			
//			// TODO Just clustering by hash may result in clashes!!!
//			// For each hash we have to keep a list an explicitly compare for structural equivalence
//			Multimap<Integer, ArgExpr> cluster = HashMultimap.create();
//
//			//IBiSetMultimap<Integer, Integer> exprToOrigin = new BiHashMultimap<Integer, Integer>();
//			//Multimap<Integer, Integer> exprToOrigin = HashMultimap.create();
//			
//			for (int index : entry.getValue()) {
//				SqlNodeOld sqlNode = sqlNodes.get(index);
//
//				Collection<VarDef> exprsForVar = sqlNode.getSparqlVarToExprs().get(var);
//				
//				for(VarDef def : exprsForVar) {
//				
//					Map<String, SqlDatatype> columnToDatatype = SqlNodeUtil.getColumnToDatatype(sqlNode);
//					//Integer hash = ExprStructuralHash.hash(def.getExpr(), columnToDatatype);
//					Integer hash = ExprDatatypeHash.hash(def.getExpr(), columnToDatatype);
//								
//					cluster.put(hash, new ArgExpr(def.getExpr(), index));
//				}				
//			}
//			
//		
//			// Process the clusters we just created
//
//			
//			// First, we build a list of exprs of the cluster and
//			// a map for mapping the clustered exprs back to their nodes
//			for(Entry<Integer, Collection<ArgExpr>> clusterEntry : cluster.asMap().entrySet()) {
//				Collection<ArgExpr> argExprs = clusterEntry.getValue();
//					
//				List<Expr> exprs = new ArrayList<Expr>();
//				Map<Integer, Integer> exprToNode = new HashMap<Integer, Integer>();
//
//				int i = 0;
//				for(ArgExpr argExpr : argExprs) {
//					exprs.add(argExpr.getExpr());
//					exprToNode.put(i, argExpr.getIndex());
//					
//					++i;
//				}
//				
//
//				// Now we can finally factor the cluster
//				List<Map<Var, Expr>> partialProjections = new ArrayList<Map<Var, Expr>>();
//				Expr common = factorizer.transform(exprs, partialProjections);
//
//				
//				// For our current variable, we can set up the projection of the result...
//				commons.put(var, new VarDef(common));
//
//				// ... and now we adjust the projections of the children accordingly
//				for (int j = 0; j < partialProjections.size(); ++j) {
//					int originalIndex = exprToNode.get(j);
//
//					//SqlNode tmp = sqlNodes.get(originalIndex);
//					Multimap<Var, VarDef> projection = projections.get(originalIndex);
//					
//					Map<Var, Expr> partialProjection = partialProjections.get(j);
//					
//					for(Entry<Var, Expr> ppEntry : partialProjection.entrySet()) {
//						projection.put(ppEntry.getKey(), new VarDef(ppEntry.getValue()));
//					}					
//				}
//			}			
//		}
//
//		// Build the final result from the information we gathered
//		
//		for (int i = 0; i < projections.size(); ++i) {
//			SqlNodeOld tmp = sqlNodes.get(i);
//			Multimap<Var, VarDef> projection = projections.get(i);
//
//			// Projection.Var becomes the new column alias
//			// Projection.Expr is pushed down to an sqlExpr
//			// Projection.Expr's vars are replaced with the original column defs
//			
//			NodeExprSubstitutor substitutor = createSubstitutor(tmp);
//			Map<String, SqlExpr> subbedProj = new HashMap<String, SqlExpr>();
//			for(Entry<Var, VarDef> entry : projection.entries()) {
//				Expr subbed = substitutor.transformMM(entry.getValue().getExpr());
//				Expr pushed = PushDown.pushDownMM(subbed);
//				
//				if(!(pushed instanceof ExprSqlBridge)) {
//					throw new RuntimeException("Could not push down common sub expression");
//				}
// 
//				SqlExpr sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
//				
//				subbedProj.put(entry.getKey().getName(), sqlExpr);
//				
//				allColumnsToDatatype.put(entry.getKey().getName(), sqlExpr.getDatatype());
//			}
//
//			// Update the projection
//			tmp.getAliasToColumn().clear();
//			tmp.getAliasToColumn().putAll(subbedProj);
//
//			// Fill up missing columns with null
//			//Set<Var> referencedColumns = new HashSet<Var>();
//			
//			//Set<Var> unreferenedColumns = Sets.difference(allColumnsToDatatype.keySet(), expr.getVarsMentioned());
//
//			
//			tmp.getSparqlVarToExprs().clear();
//			tmp.getSparqlVarToExprs().putAll(commons);
//		}
//
//
//		for(SqlNodeOld sqlNode : sqlNodes) {
//			Set<String> unboundColumns = Sets.difference(allColumnsToDatatype.keySet(), sqlNode.getAliasToColumn().keySet());
//		
//			for(String columnName : unboundColumns) {
//				
//				SqlDatatype datatype = allColumnsToDatatype.get(columnName);
//				
//				sqlNode.getAliasToColumn().put(columnName, SqlExprValue.createNull(datatype));
//			}
//		}
//
//		String unionAlias = generator.nextRelation();
//		SqlNodeOld result = new SqlUnionN(unionAlias, sqlNodes);
//
//		result.getSparqlVarToExprs().putAll(commons);
//		
//		
//		for(Entry<String, SqlDatatype> entry : allColumnsToDatatype.entrySet()) {
//			String columnName = entry.getKey();
//			SqlDatatype datatype = entry.getValue();
//			
//			//XXX WAS NULL
//			result.getAliasToColumn().put(columnName, new SqlExprColumn(unionAlias, columnName, datatype));
//		}
//		
//
//		return result;
//	}
//
//	
//	public static SqlNodeOld union(ColRelGenerator generator, List<SqlNodeOld> sqlNodes) {
//
//		
//		if(sqlNodes.isEmpty()) {
//			return new SqlNodeEmpty();
//		}
//		
//		if(sqlNodes.size() == 1) {
//			return sqlNodes.get(0);
//		}
//		
//		// Prepare the data structures from which the
//		// result node will be created
//		Multimap<Var, VarDef> commons = HashMultimap.create();
//		
//		List<Multimap<Var, VarDef>> projections = new ArrayList<Multimap<Var, VarDef>>();
//		for (int i = 0; i < sqlNodes.size(); ++i) {
//			Multimap<Var, VarDef> tmp = HashMultimap.create();
//			projections.add(tmp);
//		}
//
//		// Now we can start with the actual work				
//		Multimap<Var, Integer> varToSqlNode = HashMultimap.create();
//
//		Generator aliasGen = Gensym.create("c");
//
//		// Push constants into columns
//		for (int i = 0; i < sqlNodes.size(); ++i) {
//			SqlNodeOld sqlNode = sqlNodes.get(i);
//			Set<Var> vars = new HashSet<Var>(sqlNode.getSparqlVarsMentioned()); // FIXME possible redundant hashset
//			for (Var var : vars) {
//				
//				
//				List<VarDef> termDefs = new ArrayList<VarDef>(sqlNode.getSparqlVarToExprs().get(var)); 
//				
//				for(VarDef termDef : termDefs) {
//					Expr expr = termDef.getExpr();
//					
//					
//					if(termDef.getExpr().isConstant()) {
//						sqlNode.getSparqlVarToExprs().remove(var, termDef);
//						
//						NodeValue nv = null; // TODO Fix package of: ExprUtils.eval(expr);
//						Object o = NodeValueUtils.getValue(nv);
//						
//						SqlExprValue sv = new SqlExprValue(o);
//						
//						String columnAlias = aliasGen.next();
//						
//						// FIXME Assumes a type constructor here - which it should alway be
//						List<Expr> newArgs = new ArrayList<Expr>(expr.getFunction().getArgs());
//						newArgs.set(1, new ExprVar(columnAlias));
//						
//						Expr newExpr = ExprCopy.getInstance().copy(expr, newArgs);
//						
//						VarDef newTermDef = new VarDef(newExpr);
//						sqlNode.getSparqlVarToExprs().put(var, newTermDef);
//						sqlNode.getAliasToColumn().put(columnAlias, sv);
//						
//						sqlNode.getSparqlVarToExprs().put(var, newTermDef);
//					}
//
//				}
//				
//				
//			}
//		}
//
//		
//		// Map each variable to the set of corresponding nodes
//		for (int i = 0; i < sqlNodes.size(); ++i) {
//			SqlNodeOld sqlNode = sqlNodes.get(i);
//			for (Node var : sqlNode.getSparqlVarsMentioned()) {
//				varToSqlNode.put((Var)var, i);
//			}
//		}
//		
//
//		// TODO Delete the commented out code below if the pushing into columns works 
//		// A set of variables that have bindings to constants
//		//Set<Var> varConstant = new HashSet<Var>();
//		/*
//		Map<Var, TermDef> varToConstant = new HashMap<Var, TermDef>();
//		for(Entry<Var, Collection<Integer>> entry : varToSqlNode.asMap().entrySet()) {
//			Var var = entry.getKey();
//						
//			for (int index : entry.getValue()) {
//				SqlNode sqlNode = sqlNodes.get(index);
//				
//				sqlNode.
//				Collection<TermDef> exprsForVar = sqlNode.getSparqlVarToExprs().get(var);
//				
//
//				for(TermDef def : exprsForVar) {
//					if(def.getExpr().isConstant()) {
//						//varConstant.add(var);
//						varToConstant.put(var, def);
//					}			
//				}
//			}
//		}*/
//
//		
//		// For each var that maps to a constant, add a NULL mapping for
//		// every union member which does not define the variable as a contstant
//		/*
//		for(Entry<Var, TermDef> entry : varToConstant.entrySet()) {
//			Var var = entry.getKey();
//			TermDef baseTermDef = entry.getValue();
//			
//			for (int i = 0; i < sqlNodes.size(); ++i) {
//				SqlNode sqlNode = sqlNodes.get(i);
//				
//				Multimap<Var, TermDef> varDefs = sqlNode.getSparqlVarToExprs();
//				
//				boolean hasConstant = false;
//				for(TermDef termDef : varDefs.get(var)) {
//					if(termDef.getExpr().isConstant()) {
//						hasConstant = true;
//						continue;
//					}
//				}
//				
//				if(!hasConstant) {
//					ExprList exprs = new ExprList();
//					List<Expr> args = baseTermDef.getExpr().getFunction().getArgs();
//					//System.out.println("Args: " + args.size());
//					for(int j = 0; j < args.size(); ++j) {
//						Expr expr = j == 1 ? NodeValue.makeString(""): args.get(j);
//						
//						exprs.add(expr);
//					}
//					
//					Expr newExpr = ExprCopy.getInstance().copy(baseTermDef.getExpr(), exprs); 
//					
//					varToSqlNode.put((Var)var, i);
//					varDefs.put(var, new TermDef(newExpr));
//				}				
//			}
//		}
//		*/
//
//
//		
//		
//		ExprCommonFactor factorizer = new ExprCommonFactor(aliasGen);
//
//		
//		Map<String, SqlDatatype> allColumnsToDatatype = new HashMap<String, SqlDatatype>();
//		
//
//
//		
//		// For each variable, cluster the corresponding expressions
//		for(Entry<Var, Collection<Integer>> entry : varToSqlNode.asMap().entrySet()) {
//			Var var = entry.getKey();
//			
//			
//			Multimap<Integer, ArgExpr> cluster = HashMultimap.create();
//
//			//IBiSetMultimap<Integer, Integer> exprToOrigin = new BiHashMultimap<Integer, Integer>();
//			//Multimap<Integer, Integer> exprToOrigin = HashMultimap.create();
//			
//			RestrictionSet restrictionsForVar = new RestrictionSet(false);
//			for (int index : entry.getValue()) {
//				SqlNodeOld sqlNode = sqlNodes.get(index);
//
//				Collection<VarDef> exprsForVar = sqlNode.getSparqlVarToExprs().get(var);
//				
//				
//				
//				
//				
//				for(VarDef def : exprsForVar) {
//					restrictionsForVar.addAlternatives(def.getRestrictions());
//					
//					Map<String, SqlDatatype> columnToDatatype = SqlNodeUtil.getColumnToDatatype(sqlNode);
//
//					// TODO This is hacky - we are using a hash for determining structural equivalence
//					// So the hash is ok, but still we need to check for collisions
//					Integer hash = ExprDatatypeHash.hash(def.getExpr(), columnToDatatype);
//								 
//					cluster.put(hash, new ArgExpr(def.getExpr(), index));
//				}				
//			}
//			
//		
//			// Process the sets (clusters) of structurally equivalent expressions we just created
//
//			
//			// First, we build a list of exprs of the cluster and
//			// a map for mapping the clustered exprs back to their nodes
//			for(Entry<Integer, Collection<ArgExpr>> clusterEntry : cluster.asMap().entrySet()) {
//				Collection<ArgExpr> argExprs = clusterEntry.getValue();
//					
//				List<Expr> exprs = new ArrayList<Expr>();
//				Map<Integer, Integer> exprToNode = new HashMap<Integer, Integer>();
//
//				int i = 0;
//				for(ArgExpr argExpr : argExprs) {
//					exprs.add(argExpr.getExpr());
//					exprToNode.put(i, argExpr.getIndex());
//					
//					++i;
//				}
//
//				/*
//				if(exprs.size() == 1) {
//					Expr expr = exprs.get(0);
//					if(expr.isConstant()) {
//						System.out.println("constant expr: " + expr);
//					}
//				}
//				*/
//
//				// Now we can finally factor the cluster
//				List<Map<Var, Expr>> partialProjections = new ArrayList<Map<Var, Expr>>();
//				Expr common = factorizer.transform(exprs, partialProjections);
//
//				
//				// The common restriction is the disjunction of all participating restrictions
//				
//				
//				
//				// For our current variable, we can set up the projection of the result...
//				commons.put(var, new VarDef(common, restrictionsForVar));
//
//				// ... and now we adjust the projections of the children accordingly
//				for (int j = 0; j < partialProjections.size(); ++j) {
//					int originalIndex = exprToNode.get(j);
//
//					//SqlNode tmp = sqlNodes.get(originalIndex);
//					Multimap<Var, VarDef> projection = projections.get(originalIndex);
//					
//					Map<Var, Expr> partialProjection = partialProjections.get(j);
//					
//					for(Entry<Var, Expr> ppEntry : partialProjection.entrySet()) {
//						projection.put(ppEntry.getKey(), new VarDef(ppEntry.getValue()));
//					}					
//				}
//			}			
//		}
//
//		// Build the final result from the information we gathered
//		
//		for (int i = 0; i < projections.size(); ++i) {
//			SqlNodeOld tmp = sqlNodes.get(i);
//			Multimap<Var, VarDef> projection = projections.get(i);
//
//			// Projection.Var becomes the new column alias
//			// Projection.Expr is pushed down to an sqlExpr
//			// Projection.Expr's vars are replaced with the original column defs
//			
//			NodeExprSubstitutor substitutor = createSubstitutor(tmp);
//			Map<String, SqlExpr> subbedProj = new HashMap<String, SqlExpr>();
//			for(Entry<Var, VarDef> entry : projection.entries()) {
//				Expr subbed = substitutor.transformMM(entry.getValue().getExpr());
//				Expr pushed = PushDown.pushDownMM(subbed);
//				
//				if(!(pushed instanceof ExprSqlBridge)) {
//					throw new RuntimeException("Could not push down common sub expression");
//				}
// 
//				SqlExpr sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
//				
//				subbedProj.put(entry.getKey().getName(), sqlExpr);
//				
//				allColumnsToDatatype.put(entry.getKey().getName(), sqlExpr.getDatatype());
//			}
//
//			// Update the projection
//			tmp.getAliasToColumn().clear();
//			tmp.getAliasToColumn().putAll(subbedProj);
//
//			// Fill up missing columns with null
//			//Set<Var> referencedColumns = new HashSet<Var>();
//			
//			//Set<Var> unreferenedColumns = Sets.difference(allColumnsToDatatype.keySet(), expr.getVarsMentioned());
//
//			
//			tmp.getSparqlVarToExprs().clear();
//			tmp.getSparqlVarToExprs().putAll(commons);
//		}
//
//
//		for(SqlNodeOld sqlNode : sqlNodes) {
//			Set<String> unboundColumns = Sets.difference(allColumnsToDatatype.keySet(), sqlNode.getAliasToColumn().keySet());
//		
//			for(String columnName : unboundColumns) {
//				
//				SqlDatatype datatype = allColumnsToDatatype.get(columnName);
//				
//				sqlNode.getAliasToColumn().put(columnName, SqlExprValue.createNull(datatype));
//			}
//		}
//
//		String unionAlias = generator.nextRelation();
//		SqlNodeOld result = new SqlUnionN(unionAlias, sqlNodes);
//
//		result.getSparqlVarToExprs().putAll(commons);
//		
//		
//		for(Entry<String, SqlDatatype> entry : allColumnsToDatatype.entrySet()) {
//			String columnName = entry.getKey();
//			SqlDatatype datatype = entry.getValue();
//			
//			//XXX WAS NULL
//			result.getAliasToColumn().put(columnName, new SqlExprColumn(unionAlias, columnName, datatype));
//		}
//		
//
//		return result;
//	}
//		
//	
//	
//	
///*
//		Map<Var, Expr> commons = new HashMap<Var, Expr>();
//		List<Map<Var, Expr>> projections = new ArrayList<Map<Var, Expr>>();
//		for (int i = 0; i < sqlNodes.size(); ++i) {
//			projections.add(new HashMap<Var, Expr>());
//		}
//* /
//		if (sqlNodes.size() == 1) {
//			return sqlNodes.get(0);
//		}
//
//		// Map all variables to the bindings where they are used
//		Multimap<Node, Integer> varToBinding = HashMultimap.create();
//
//		for (int i = 0; i < sqlNodes.size(); ++i) {
//			SqlNode binding = sqlNodes.get(i);
//			for (Node var : binding.getSparqlVarsMentioned()) {
//				varToBinding.put(var, i);
//			}
//		}
//
//		
//		ColRelGenerator aliasGen = Gensym.create("h");
//		// Align the Bindings for each variable
//		// For each variable, for each union's child, collect all expressions
//		// Then cluster them based on their hash (structural equivalence)
//		for(Entry<Node, Collection<Integer>> entry : varToBinding.asMap()
//				.entrySet()) {
//
//
//			// Map each expression to an expression and its argument index
//			Multimap<Integer, ArgExpr> hashToExpr = HashMultimap.create();
//			for (int index : entry.getValue()) {
//				SqlNode sqlNode = sqlNodes.get(index).getSparqlVarToExprs();
//				
//				Map<String, SqlDatatype> columnToDatatype = SqlNodeUtil.getColumnToDatatype(tmp);
//				Integer hash = ExprDatatypeHash.hash(sqlNode, columnToDatatype);
//				
//			}
//
//			//List<Expr> exprs = new ArrayList<Expr>();
//
//			
//			for (SqlNode tmp : bindings) {
//				// Map the columns to their datatype
//
//				
//				exprs.addAll(tmp.getSparqlVarToExprs().get(var));
//			}
//
//			ExprCommonFactor factorizer = new ExprCommonFactor(aliasGen);
//			List<Map<Var, Expr>> partialProjections = new ArrayList<Map<Var, Expr>>();
//			Expr common = factorizer.transform(exprs, partialProjections);
//
//			commons.put((Var)var, common);
//			//result.getSparqlVarToExpr().put(var, common);
//
//			// Merge the local projections into the global ones
//
//			int i = 0;
//			for (int index : entry.getValue()) {
//				// SqlNodeBinding tmp = bindings.get(index);
//				projections.get(index).putAll(partialProjections.get(i));
//				++i;
//			}
//
//			// Map<Var, Expr> partialProjection = partialProjections.get(i);
//
//			// FIXME We assume that there is no overlap in helper variables
//			// Maybe we could do a merge method like Map merge(Map a, Map b)
//			// This method would add all entries of b to a, and rename those
//			// keys
//			// that already exist in a. The result is a map of renamings.
//			// result.getSqlVarToExpr().putAll(partialProjections);
//
//			// FIXME Maybe create an OpProjection rather than doing it in place
//			//tmp.getSqlVarToExpr().putAll(partialProjection);
//			/*
//			tmp.getSparqlVarToExpr().put(var, common);
//
//			String alias = "a" + globalAliasId++;
//			SqlProjection opProj = new SqlProjection(alias, tmp.getSqlNode(), partialProjection);
//			* /
//		}
//
//		for (int i = 0; i < projections.size(); ++i) {
//			SqlNode tmp = sqlNodes.get(i);
//			Map<Var, Expr> projection = projections.get(i);
//
//			// Projection.Var becomes the new column alias
//			// Projection.Expr is pushed down to an sqlExpr
//			// Projection.Expr's vars are replaced with the original column defs
//			
//			NodeExprSubstitutor substitutor = createSubstitutor(tmp);
//			Map<String, SqlExpr> subbedProj = new HashMap<String, SqlExpr>();
//			for(Entry<Var, Expr> entry : projection.entrySet()) {
//				Expr subbed = substitutor.transformMM(entry.getValue());
//				Expr pushed = PushDown.pushDownMM(subbed);
//				
//				if(!(pushed instanceof ExprSqlBridge)) {
//					throw new RuntimeException("Could not push down common sub expression");
//				}
//
//				SqlExpr sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
//				
//				subbedProj.put(entry.getKey().getName(), sqlExpr);
//			}
//
//			// Update the projection
//			tmp.getAliasToColumn().clear();
//			tmp.getAliasToColumn().putAll(subbedProj);
//
//			tmp.getSparqlVarToExprs().clear();
//			tmp.getSparqlVarToExprs().putAll(commons);
//			
//		}
//
//
//
//		String unionAlias = generator.next();
//		SqlNode result = new SqlUnionN(unionAlias, sqlNodes);
//
//		result.getSparqlVarToExprs().putAll(commons);
//		
//		Set<String> columns = new HashSet<String>();
//		for(Expr exprs : commons.values()) {
//			for(Var var : exprs.getVarsMentioned()) {
//				columns.add(var.getName());
//			}
//		}
//		
//		for(String columnName : columns) {
//			result.getAliasToColumn().put(columnName, new SqlExprColumn(null, columnName));
//		}
//		
//
//		return result;
//	}
//
//	/**
//	 * Aligns the layout of a sparql variable on the SQL level.
//	 * 
//	 * For instance, if a sparql variable is defined as ?o = GeoLit(?geom) then
//	 * it will introduce helper columns on SQL such as:
//	 * 
//	 * ?o_lexicalValue = ST_AsText(?geom) ?o_type = 1 // Literal ?o_dataType =
//	 * "http://.../geometry"
//	 * 
//	 * which are used to intruduce new columns in the SQL query: Select
//	 * ST_AsText(?geom) as o_lexicalValue, 1 as o_type...
//	 * 
//	 * and the rewritten definition of ?o as ?o = RdfTerm(?o_type,
//	 * ?o_lexicalValue, null, ?o_dataType)
//	 * 
//	 * This is needed in order to e.g. allow a union such as { ?s geom ?o }
//	 * union {?s hasGeometry ?o }, wheres the second ?o is not based on a
//	 * GeoLiteral column in which case the different "?o"s have to be aligned in
//	 * their structure
//	 * 
//	 * 
//	 */
//	public void alignSql() {
//		// TODO implement
//	}
//}
//
//
//
///*
//
//
////boolean isPickDefSatisfiable = defs.isEmpty();
//for(RestrictedExpr pickDef : result.definitionExprs) {
//	Set<RestrictedExpr> newPickDefs = new HashSet<RestrictedExpr>();
//
//	// The restrictions that apply to the picked variable.
//	// initialized only if there are no defs (init means true, i.e. no restriction)
//	RestrictionSet pickVarRestrictions = defs.isEmpty() ? new RestrictionSet() : null;
//
//
//	for(RestrictedExpr def : defs) {
//		
//		
//		RestrictedExpr vdEquals = VariableDefinitionOps.equals(pickDef, def);
//		
//		Expr sqlExpr = translateSql(vdEquals, sqlTranslator);
//		
//		if(sqlExpr.equals(SqlExprValue.FALSE)) {
//			continue;
//		}
//
//		if(pickVarRestrictions == null) {
//			pickVarRestrictions = new RestrictionSet(); 
//		}
//		
//		pickVarRestrictions.addAlternatives(vdEquals.getRestrictions());
//
//		
//		result.constraintExpr.add(sqlExpr);
//	}
//
//	// Only if there is a non-unsatisfiable restriction...
//	if(pickVarRestrictions != null) {
//		RestrictedExpr newRestrictedExpr = new RestrictedExpr(pickDef.getExpr(), pickVarRestrictions);
//		newPickDefs.add(newRestrictedExpr);
//	}
//	
//	result.definitionExprs = newPickDefs;
//}
//*/



