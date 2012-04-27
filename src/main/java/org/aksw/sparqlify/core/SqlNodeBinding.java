package org.aksw.sparqlify.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mapping.ExprCommonFactor;
import mapping.ExprCopy;

import org.aksw.commons.collections.CartesianProduct;
import org.aksw.commons.util.Pair;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.expr.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sparql.transform.ConstantExpander;
import org.aksw.sparqlify.algebra.sparql.transform.ExprDatatypeHash;
import org.aksw.sparqlify.algebra.sparql.transform.FunctionExpander;
import org.aksw.sparqlify.algebra.sparql.transform.NodeExprSubstitutor;
import org.aksw.sparqlify.algebra.sparql.transform.SqlExprUtils;
import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprBase;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.algebra.sql.exprs.SqlSortCondition;
import org.aksw.sparqlify.algebra.sql.nodes.SqlAlias;
import org.aksw.sparqlify.algebra.sql.nodes.SqlDistinct;
import org.aksw.sparqlify.algebra.sql.nodes.SqlGroup;
import org.aksw.sparqlify.algebra.sql.nodes.SqlJoin;
import org.aksw.sparqlify.algebra.sql.nodes.SqlMyRestrict;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNode;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeEmpty;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOrder;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeUtil;
import org.aksw.sparqlify.algebra.sql.nodes.SqlProjection;
import org.aksw.sparqlify.algebra.sql.nodes.SqlQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlSlice;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.algebra.sql.nodes.SqlUnionN;
import org.aksw.sparqlify.algebra.sql.nodes.TermDef;
import org.aksw.sparqlify.compile.sparql.PushDown;
import org.aksw.sparqlify.compile.sparql.SqlAlgebraToString;
import org.aksw.sparqlify.compile.sparql.SqlExprOptimizer;
import org.aksw.sparqlify.compile.sparql.SqlSelectBlockCollector;
import org.aksw.sparqlify.expr.util.NodeValueUtils;
import org.aksw.sparqlify.restriction.Restriction;
import org.aksw.sparqlify.restriction.RestrictionSet;
import org.aksw.sparqlify.views.transform.SqlExprToExpr;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparql.DnfUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.SortCondition;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.ExprUtils;


class ArgExpr {
	private Expr expr;
	private int index;
	
	public ArgExpr(Expr expr, int index)
	{
		this.expr = expr;
		this.index = index;
	}
	
	public Expr getExpr() {
		return expr;
	}
	
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "ArgExpr [expr=" + expr + ", index=" + index + "]";
	}
}



/**
 * DEPRECATED
 * 
 * The attributios of this class have been added to SqlNodeBase
 * 
 * 
 * An sql node and a mapping of the sql-columns to sparql variables
 * 
 * Well to be precise:
 * 
 * This class represents an relational algebra operation.
 * 
 * Basically it is used for joins:
 * 
 * There is a set of sparql variables that are constructed from expressions over
 * the sql-columns. These variables might be equated to each other, which
 * results in join expressions
 * 
 * 
 * 
 * 
 * @author raven
 * 
 */
@Deprecated
public class SqlNodeBinding {
	private static final Logger logger = LoggerFactory.getLogger(SqlNodeBinding.class);
	
	// Whether the whole expression represented by this node has an alias
	// Technically, if an alias exists, the underlying SQL statement will be
	// wrapped such as in:
	// Select {columns} From (underyling sql statement) As {alias}
	private String alias;

	// FIXME The alias id should managed in some context
	// So for each query the alias count starts at zero
	// Now the count keeps increasing for each now query
	public static int globalAliasId = 0;

	private Map<Node, Expr> sparqlVarToExpr = new HashMap<Node, Expr>();

	// Helper columns - these variables become aliases for
	// expressions on the underlying columns
	// The helper columns can be referred to by the sparqlVar expressions.
	private Map<Var, Expr> sqlVarToExpr = new HashMap<Var, Expr>();

	// private Map<Var, SqlNode> sqlVarToExpr = new HashMap<Var, Expr>();

	// Jena's SqlRestrict class is unfortunately no longer used, and
	// all methods were made private
	// Therefore we collect conditions here
	// Note: We are using a set here in order to naturally merge duplicate
	// constraints
	// private Set<SqlExpr> conditions = new HashSet<SqlExpr>();

	// SqlExpression corresponding to a relation
	private SqlExpr sqlNode;

	public SqlExpr getSqlNode() {
		return sqlNode;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setSqlNode(SqlExpr sqlNode) {
		this.sqlNode = sqlNode;
	}

	public Map<Node, Expr> getSparqlVarToExpr() {
		return sparqlVarToExpr;
	}

	public Map<Var, Expr> getSqlVarToExpr() {
		return sqlVarToExpr;
	}

	public Set<Node> getSparqlVarsMentioned() {
		return sparqlVarToExpr.keySet();
	}

	/*
	 * public Set<SqlExpr> getConditions() { return conditions; }
	 */

	public SqlNodeBinding() {
	}

	public static E_RdfTerm expandConstant(Node node) {
		int type;
		Object lex = "";
		String lang = "";
		String dt = "";
		
		if(node.isBlank()) {
			type = 0;
			lex = node.getBlankNodeId().getLabelString();
		} else if(node.isURI()) {
			type = 1;
			lex = node.getURI();
		} else if(node.isLiteral()) {
			
			lex = node.getLiteral().getValue();
			
			//lex = node.getLiteralLexicalForm();

			String datatype = node.getLiteralDatatypeURI();
			if(datatype == null || datatype.isEmpty()) {
				type = 2;
				lang = node.getLiteralLanguage();
			} else {
				type = 3;
				dt = node.getLiteralDatatypeURI();
			}
		} else {
			throw new RuntimeException("Should not happen");
		}

		return new E_RdfTerm(
				NodeValue.makeDecimal(type), NodeValue.makeNode(lex.toString(), lang, dt),
				NodeValue.makeString(lang), NodeValue.makeString(dt));
		
		/*
		return new E_Function(SparqlifyConstants.rdfTermLabel, SparqlSubstitute.makeExprList(
				NodeValue.makeDecimal(type), NodeValue.makeNode(lex.toString(), lang, dt),
				NodeValue.makeString(lang), NodeValue.makeString(dt)));
		*/

	}
	
	
	
	/**
	 * Creates an sql node from a view instance
	 * 
	 * 
	 * @param generator
	 * @param viewInstance
	 * @return
	 */
	public static SqlNode create(ColRelGenerator generator, RdfViewInstance viewInstance) {
		//SqlNode result = new SqlNode();

		// Instantiate the sqlNode:
		// Assign it an alias, and update the sql references accordingly.
		SqlNode sqlNode = viewInstance.getParent().getSqlNode();
		SqlNode result;
		
		String alias = generator.nextRelation();

		if (sqlNode instanceof SqlTable) {
			SqlTable tmp = (SqlTable) sqlNode;
			result = new SqlTable(alias, tmp.getTableName());
		} else if (sqlNode instanceof SqlQuery) {

			String outerAlias = generator.nextRelation();

			SqlQuery tmp = (SqlQuery) sqlNode;
			result = new SqlQuery(outerAlias, tmp.getQueryString(), alias);
			
			alias = outerAlias;
		} else {
			throw new NotImplementedException();
		}

		//result.setAlias(alias);
		//result.setSqlNode(sqlRelation);

		//Multimap<Var, Expr> tmpBinding = viewInstance.getSqlBinding();
		Multimap<Var, TermDef> sqlBinding = viewInstance.getSqlBinding(); //HashMultimap.create();

		
		/*
		for(Entry<Var, Expr> entry : tmpBinding.entries()) {
			Var var = entry.getKey();
			
			//Restriction r = viewInstance.getParent().getRestrictions().getRestriction(var);
			
			sqlBinding.put(entry.getKey(), new TermDef(entry.getValue(), r));
		}*/

		// Add the constants
		// 2011 Nov 1: Not sure if below is still correct. In the meanwhile constants are
		// replaced with variables and corresponding filters.
		// TODO URGENT: This is wrong here:
		// We can't just add constants; we need to check whether we need
		// to create filter statements
		// SOLUTION: Here we replace with the defining expression, while there is a wrapping
		// OpFilter that constrains the expr.
		for(Entry<Var, Node> entry : viewInstance.getBinding().getEquiMap().getKeyToValue().entrySet()) {
			Var var = entry.getKey();
			
			if(!sqlBinding.containsKey(entry.getKey())) {

				
				Expr definingExpr = viewInstance.getDefiningExpr(var);
				Restriction r = viewInstance.getParent().getRestrictions().getRestriction(var);
				
				Expr expand = expandConstant(entry.getValue());
				//sqlBinding.put(entry.getKey(), expand);

				if(definingExpr == null) {
					sqlBinding.put(var, new TermDef(expand, new Restriction(entry.getValue())));
				} else {
					sqlBinding.put(var, new TermDef(definingExpr, r));
				}
			}
		}
		
		
		
		
		
		// Keep things separated: Perform the renaming of view variables to
		// SQL column references (alias.columnName) here,
		// and do the more complicated processing later
		/* We do not replace variables with column references here
		RenamerVars renamer = new RenamerVars(new HashSet<Var>(), alias + ".");
		for (Entry<Node, Collection<Expr>> entry : tmpBinding.asMap()
				.entrySet()) {

			for (Expr expr : entry.getValue()) {
				
				// FIXME: Not sure if this is the best place for performing
				// simple substitutions such as beef:uri -> beef:rdfTerm
				Expr substituted = SparqlSubstitute.substituteExpr(expr);
				
				Expr renamed = substituted.applyNodeTransform(renamer);
				sqlBinding.put(entry.getKey(), renamed);
			}
		}*/
		
		Set<Var> vars = new HashSet<Var>();
		for(Entry<Var, Collection<TermDef>> entry : sqlBinding.asMap().entrySet()) {
			for (TermDef expr : entry.getValue()) {
				vars.addAll(expr.getExpr().getVarsMentioned());
			}
		}

		//RenamerVars renamer = new RenamerVars(new HashSet<Var>(), alias + ".");
		for(Var var : vars) {
			
			SqlDatatype datatype = viewInstance.getParent().getColumnToDatatype().get(var.getName());
			
			if(datatype == null) {
				throw new RuntimeException("Datatype is null - no mapping for column named '" + var.getName() + "' in view " + viewInstance.getParent().getName());
			}
			
			SqlExprColumn column = new SqlExprColumn(alias, var.getName(), datatype);
			result.getAliasToColumn().put(var.getName(), column);
		}

		// Check for the variables that have been stated equal in the equimap, whether
		// combining their corresponding patterns is satisfiable
		
		
		
		/*
		for (Entry<Node, Collection<Expr>> entry : sqlBinding.asMap()
				.entrySet()) {

			for
		RdfTermPattern pattern = null;
*/

		
		// result.setSqlNode(sqlNode);

		// TODO A query variable may be mapped to by multiple view-variables
		// This means that these underlying columns must be made equal in
		// the sql query; Example:
		// View : {?s knows ?o } From Table(id, foreign_id)
		// Query: {?x knows ?x }
		
		
		List<SqlExpr> selfConditions = new ArrayList<SqlExpr>();
		for (Entry<Var, Collection<TermDef>> entry : sqlBinding.asMap()
				.entrySet()) {

			// In case that multiple view variables are bound to the same query
			// variable, we need to create equality conditions:
			// Example:
			// ?o = {Uri(colX), Uri(colY)} then we infer the condition colX =
			// colY
			// and pick just one of the expressions for ?o, since they are all
			// equal.
			// Currently we pick the first expression
			// TODO Investigate whether a clever picking/equating strategy
			// could improve query execution performance

			/*
			 * 			RdfTermPattern pattern = null;
					// Check if the constraints are still satisfiable,
					// given the equality of a and b.
					pattern = RdfTermPattern.merge(pattern, viewInstance.getParent().getConstraints().getPattern(a));
					
					if(!pattern.isSatisfiable()) {
						// Indicate empty rewrite
					}

			 */
			
			
			NodeExprSubstitutor substitutor = createSubstitutor(result.getAliasToColumn());

			
			// Create a restriction that equates all the different variables
			Expr a = null;
			Expr b = null;
			
			for (TermDef def : entry.getValue()) {

				b = def.getExpr();
				
				if (a != null) {
					// Optimize the expression
					Expr tmp = SqlExprOptimizer.optimizeMM(new E_Equals(a, b));
					
					
					if (tmp.equals(NodeValue.FALSE)) {
						// TODO Somehow indicate an empty relation
					}

					// For the translation to sql we need the sparql expression
					// and the mapping of sparql variables to sql columns
					//SqlExpr sqlExpr = SqlExprOptimizer.translateMM(tmp);

					SqlExpr sqlExpr = forcePushDown(tmp, substitutor);

					//if(true) { throw new RuntimeException("Add support for discriminator column"); }
					/*
					Expr substituted = substitutor.transformMM(tmp);

					
					Expr x = PushDown.pushDownMM(substituted);
					if(!(x instanceof ExprSqlBridge)) {
						throw new RuntimeException("Failed to push down '" + tmp + "'");
					}
					SqlExpr sqlExpr = ((ExprSqlBridge)x).getSqlExpr();
					*/
					
					selfConditions.add(sqlExpr);
				}
				a = b;
			}
			
			// Pick the first expr
			result.getSparqlVarToExprs().put(entry.getKey(),
					entry.getValue().iterator().next());
		}
		

		if(!selfConditions.isEmpty()) {
			SqlMyRestrict sqlRestrict;
			sqlRestrict = new SqlMyRestrict(result
					.getAliasName(), result);

			sqlRestrict.getConditions().addAll(selfConditions);
			sqlRestrict.getSparqlVarToExprs().putAll(result.getSparqlVarToExprs());
			sqlRestrict.getAliasToColumn().putAll(result.getAliasToColumn());
		
			result = sqlRestrict;
		}
		
		// TODO Add the constant restrictions


		return result;
	}
	
	
	// Pushes down an Expr object that should be interpreted as an SQL expression
	public static SqlExpr forcePushDown(Expr expr, NodeExprSubstitutor substitutor) {
		Expr substituted = substitutor.transformMM(expr);

		
		Expr x = PushDown.pushDownMM(substituted);
		if(!(x instanceof ExprSqlBridge)) {
			throw new RuntimeException("Failed to push down '" + expr + "'");
		}
		SqlExpr result = ((ExprSqlBridge)x).getSqlExpr();

		return result;
	}
	
	public static SqlExprList forcePushDown(ExprList exprs, SqlNode node) {
		SqlExprList result = new SqlExprList();
		for(Expr expr : exprs) {
			SqlExpr sqlExpr = forcePushDown(expr, node.getAliasToColumn());
			result.add(sqlExpr);
		}
		
		return result;
	}



	public static SqlExpr forcePushDown(Expr expr, Map<String, SqlExpr> aliasToColumn) {
		NodeExprSubstitutor substitutor = createSubstitutor(aliasToColumn);
		
		return forcePushDown(expr, substitutor);
	}

	public static SqlExpr forceShallowPushDown(Expr expr, Map<String, SqlExpr> aliasToColumn) {

		Map<String, SqlExpr> shallow = createShallowAliasToColumn(aliasToColumn);

		NodeExprSubstitutor substitutor = createSubstitutor(shallow);
		
		return forcePushDown(expr, substitutor);
	}
	

	
	public static SqlExprOptimizer sqlTranslator = new SqlExprOptimizer();

	/**
	 * Replace all occurrences of variables within expr with those of the
	 * binding
	 * 
	 * @param a
	 * @param expr
	 * @return
	 */
	public static SqlExpr rewriteExpr(SqlNodeBinding a, Expr expr) {
		// Expr tmp = expr.applyNodeTransform(new Renamer())

		// Expr tmp = new E_Equals(a.getSparqlVarToExpr().get(var),
		// b.getSparqlVarToExpr().get(var));
		// tmp = SqlExprTranslator.optimizeMM(tmp);

		// SqlExpr joinExpr = SqlExprTranslator.translateMM(tmp);

		return null;
	}

	
	public static String generateNextFreeId(String base, Set<String> used) {
		if(!used.contains(base)) {
			return base;
		}
		
		for(int i = 1; ; ++i) {
			String id = base + i;
			if(!used.contains(id)) {
				return id;
			}
		}
	}
	
	
	/**
	 * Calculates the projection for the given join by renaming 
	 * columns with same names on both sides of the join.
	 * 
	 * @param generator
	 * @param left
	 * @param right
	 * @return
	 */
	public static SqlNode doJoinRename(ColRelGenerator generator, SqlNode left, String leftAlias, SqlNode right, String rightAlias)
	{
		SqlNode result = new SqlNodeEmpty();

		if(leftAlias != null && leftAlias.equals(rightAlias)) {
			throw new RuntimeException("Two aliases equal - should not happen");
		}
		
		Set<String> colsA = left.getAliasToColumn().keySet();
		Set<String> colsB = right.getAliasToColumn().keySet();
		
		Set<String> intersection = new HashSet<String>(Sets.intersection(colsA, colsB));
		Set<String> union = Sets.union(colsA, colsB);

		Map<String, String> colRefRenames = new HashMap<String, String>();

		for(String common : intersection) {
			SqlExpr sqlExpr = right.getAliasToColumn().get(common);
			
			String idBase = rightAlias + "_" + common;			
			String id = generateNextFreeId(idBase, union);
			
			colRefRenames.put(common, id);
		}
		
		Map<String, SqlExpr> newBMap = new HashMap<String, SqlExpr>();
		for(String colB : colsB) {
			String rename = colRefRenames.get(colB);
			if(rename == null) {
				rename = colB;
			}
			
			SqlExpr sqlExpr = right.getAliasToColumn().get(colB);
			
			newBMap.put(rename, sqlExpr);
		}
		//b.getAliasToColumn().clear();
		//b.getAliasToColumn().putAll(newBMap);
		result.getAliasToColumn().putAll(newBMap);
		
		
		Map<Node, Expr> exprMap = new HashMap<Node, Expr>();
		for(Entry<String, String> entry : colRefRenames.entrySet()) {
			exprMap.put(Var.alloc(entry.getKey()), new ExprVar(entry.getValue()));
		}
		
		// Substitute the column references in b
		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(exprMap);

		Multimap<Var, TermDef> newSparqlMap = HashMultimap.create();
		for(Entry<Var, TermDef> entry : right.getSparqlVarToExprs().entries()) {
			TermDef before = entry.getValue();
			TermDef after = new TermDef(substitutor.transformMM(before.getExpr()), before.getRestrictions());
			
			newSparqlMap.put(entry.getKey(), after);
		}
		//b.getSparqlVarToExprs().clear();
		//b.getSparqlVarToExprs().putAll(newSparqlMap);
		result.getSparqlVarToExprs().putAll(newSparqlMap);
		
		return result;
	}


	/**
	 * Creates a substitutor object, that can replace the sparql variables
	 * with their their expression
	 * 
	 * TODO Does not work that way with the datatype support
	 * 
	 * @param node
	 * @return
	 */
/*
	public static NodeExprSubstitutor createSparqlVarExpander(SqlNode node) {
		Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
		for(Entry<Node, Expr> entry : node.getSparqlVarToExprs().entrySet()) {
			expanderMap.put(Var.alloc(entry.getKey()), entry.getValue());
		}
		
		NodeExprSubstitutor result = new NodeExprSubstitutor(expanderMap);

		return result;
	}
*/
	
	/**
	 * Create an object that can replace column references
	 * with their expression.
	 * 
	 * Example:
	 * concat('prefix', id) AS col1
	 * 
	 * The substitutor can replace sparql var references to col1 with an
	 * SqlBridge to the concat expression. 
	 * 
	 * 
	 * @param node
	 * @return
	 */
	public static NodeExprSubstitutor createSubstitutor(Map<String, SqlExpr> aliasToColumn) {
		Map<Var, Expr> sqlSubstitutionMap = new HashMap<Var, Expr>();
		for(Entry<String, SqlExpr> entry : aliasToColumn.entrySet()) {
			sqlSubstitutionMap.put(Var.alloc(entry.getKey()), new ExprSqlBridge(entry.getValue()));
		}
		
		NodeExprSubstitutor result = new NodeExprSubstitutor(sqlSubstitutionMap);

		return result;	
	}	
	
	
	
	
	public static NodeExprSubstitutor createSubstitutor(SqlNode node) {
		return createSubstitutor(node.getAliasToColumn());
	}


	public static Pair<SqlNode, SqlNode> createJoinAlias(SqlNode node, ColRelGenerator generator) {
		if(node instanceof SqlJoin || node.getAliasName() != null) {
			// If the node is a join, then all the components already
			// have aliases
			return Pair.create(node, node);
		} else {
			// Deal with cases where the join argument does not yet
			// have an alias, e.g. Join(a, Filter(...))
			// In this case, the second argument will eventually become a sub-select.
			// So we need to assign a new alias (subselect AS x), and update the projection
			// accordingly: aliasToColumn must refer to columns of x
			
			
			// The projection must reference the columns via the old alias
			// However, the join result must refer to them with the new alias
			
			String newAlias = generator.nextRelation();
			/*
			SqlNode result = new SqlProjection(newAlias, node);
			result.getAliasToColumn().putAll(node.getAliasToColumn());
			result.getSparqlVarToExprs().putAll(node.getSparqlVarToExprs());
			*/
			
			SqlNode proj = createNewAlias(newAlias, node, generator);
			
			return Pair.create(node, proj);
			
			
			/*
			SqlNode newProj = createNewAlias(newAlias, node, generator);
			SqlNode result= new SqlProjection(newAlias, node);
			result.getAliasToColumn().putAll(newProj.getAliasToColumn());
			result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
			*/
		}
	}

	/*
	public static SqlNode createJoinProj(SqlNode node, ColRelGenerator generator) {
		
	}*/
	
	public static SqlNode join(ColRelGenerator generator, SqlNode _a, SqlNode _b,
			JoinType joinType) {

		/*
		if(joinType == JoinType.LEFT) {
			System.out.println("debug");
		}*/
		
		
		// Generate fresh aliases for the members of the join
		// (If any of the join members does not have an alias yet, it will become a
		// sub expression)
		// (SqlNode) JOIN (SqlNode) --> (SqlNode) r1 JOIN (SqlNode) r2
		/*
		Pair<SqlNode, SqlNode> pairA = createJoinAlias(_a, generator);
		Pair<SqlNode, SqlNode> pairB = createJoinAlias(_b, generator);

		
		SqlNode a = pairA.getValue();
		SqlNode b = pairB.getValue();
		
		_a = pairA.getKey();
		_b = pairB.getKey();
		
		String leftAlias = a.getAliasName();
		String rightAlias = b.getAliasName();
		*/
		
		SqlNode a = _a;
		SqlNode b = _b;
		

		//if(a.getAliasName() == null && !(a instanceof SqlJoin) || a instanceof SqlUnionN) {
		if(!(a instanceof SqlJoin  || a instanceof SqlTable || a instanceof SqlQuery) || a instanceof SqlUnionN) {
			a = createNewAlias(generator.nextRelation(), a, generator);

			
			
			/*
			a.getSparqlVarToExprs().putAll(_a.getSparqlVarToExprs());
			a.getAliasToColumn().putAll(_a.getAliasToColumn());
			*/
		}
		
		//if(b.getAliasName() == null && !(b instanceof SqlJoin)  || b instanceof SqlUnionN) {
		if(!(b instanceof SqlJoin || b instanceof SqlTable || b instanceof SqlQuery)  || b instanceof SqlUnionN) {
			b = createNewAlias(generator.nextRelation(), b, generator);

			/*
			b = new SqlAlias(generator.nextRelation(), _b);
			b.getSparqlVarToExprs().putAll(_b.getSparqlVarToExprs());
			b.getAliasToColumn().putAll(_b.getAliasToColumn());
			*/
		}
		

		
		
		//
		
		SqlNode c = doJoinRename(generator, a, a.getAliasName(), b, b.getAliasName());

		/*
		if(a instanceof SqlUnionN || b instanceof SqlUnionN) {
			System.out.println("debug");
		}
		
		if(!c.getAliasToColumn().equals(b.getAliasToColumn())) {
			System.out.println("debug");			
		}*/

		/*
		if(a instanceof SqlTable && b instanceof SqlTable) {
			SqlTable x = (SqlTable)a;
			SqlTable y = (SqlTable)b;
			

			if(x.getTableName().equals(y.getTableName())) {
				System.out.println("Same table");
				System.out.println(a.getSparqlVarToExprs());
				System.out.println(b.getSparqlVarToExprs());
			}
		}
		*/
		
		
		SqlJoin result = SqlJoin.create(joinType, a, b);

		
		result.getAliasToColumn().putAll(a.getAliasToColumn());
		result.getAliasToColumn().putAll(c.getAliasToColumn());
		
		// TODO Take filters into account for further join conditions

		
		// Check if there is an overlap in the columns being used for the join
		// if that is the case, rename the columns
		// e.g. A(id, name) Join B(id, name) will result in
		// a.id as a_id, b.id as b_id

		
		
		// Do we need an alias?

		// Update the projection:
		// First use all variables from a, then use those that are exclusive to b
		Set<Var> cVars = Sets.difference(c.getSparqlVarsMentioned(),
				a.getSparqlVarsMentioned());

		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
		//result.getSqlVarToExpr().putAll(a.getSqlVarToExpr());

		// TODO
		// Given Join(A(id, name), B(id, name)), and the bindingis
		// ?o = Literal(name)            ?t = Literal(name)
		// In that case we need to rename 'name':
		// Select a.name as renamed1, b.name as renamed2
		for (Var var : cVars) {
			Collection<TermDef> exprC = c.getSparqlVarToExprs().get(var);
			result.getSparqlVarToExprs().putAll(var, exprC);
		}
		
		
		// Determine join conditions based on variables common to both join
		// arguments
		Set<Var> commons = Sets.intersection(a.getSparqlVarsMentioned(),
				c.getSparqlVarsMentioned());


		
		/**
		 * Build a map var -> sql expr
		 */
		/*
		Map<Var, Expr> sqlSubstitutionMap = new HashMap<Var, Expr>();
		for(Entry<String, SqlExpr> entry : result.getColumnToSqlExpr().entrySet()) {
			sqlSubstitutionMap.put(Var.alloc(entry.getKey()), new ExprSqlBridge(entry.getValue()));
		}
		
		NodeExprSubstitutor substitutor = new NodeExprSubstitutor(sqlSubstitutionMap);
		*/
		
		
		/*
		Map<String, SqlExpr> joinSub = new HashMap<String, SqlExpr>();
		joinSub.putAll(result.getAliasToColumn());
		
		for(Entry<String, SqlExpr> entry : b.getAliasToColumn().entrySet()) {
			
			joinSub.put(entry.getKey(), new SqlExprColumn(b.getAliasName(), entry.getKey(), entry.getValue().getDatatype()));
		}
		NodeExprSubstitutor substitutor = createSubstitutor(joinSub);
		*/
		
		//throw new RuntimeException("Fix me");
		
		NodeExprSubstitutor substitutor = createSubstitutor(result);
		
		
		// Outerloop: For each variable in common 
		// Innerloop: For each of the variables associated expression
		//List<SqlExpr> ands = new ArrayList<SqlExpr>();
		for (Var var : commons) {
			List<SqlExpr> ors = new ArrayList<SqlExpr>();
			
			
			// For each combination of expressions derive the join condition
			// The outer loop or-ifies, the inner one and-ifies
			// a1 - b1
			// a2 - b1
			Collection<TermDef> newTermDefs = new ArrayList<TermDef>();
			Collection<TermDef> ebs = c.getSparqlVarToExprs().get(var);
			boolean foundSatisfiableJoinCondition = false; //ebs.isEmpty();
			RestrictionSet ras = new RestrictionSet(false);

			for(TermDef ea : a.getSparqlVarToExprs().get(var)) {
				
				
				// If there are no expressions to join on, then we retain the variable
				RestrictionSet ra = ea.getRestrictions();
				
				//Restriction ra = (Restriction)ea.getRestriction().clone();
				
				for(TermDef eb : ebs) {

					// Check if the constraints associated with the termDefs are unsatisfiable
					RestrictionSet rc = ra.clone();
					RestrictionSet rb = eb.getRestrictions();

					rc.stateRestriction(rb);

					if(rc.isUnsatisfiable()) {
						continue;
					}
					
					ras.addAlternatives(rc);
					
					// TODO Update restriction
					//result.getSparqlVarToExprs().put(var, new TermDef(ea.getExpr(), ra));

					
					// substitute renamed variables in exprb
					
					// For common variables we need to chose one expression for the
					// projection.
					// Here we always use the first			
					Expr tmp = new E_Equals(ea.getExpr(), eb.getExpr());
					
					
					// Essentially add ... AND(EQUALS(ea.getDiscriminatorColumnName(), ea.getDCValue()) 
					//if(true) { throw new RuntimeException("Add support for discriminator column"); }
					
					
					
					/*
					Expr functionExpand = FunctionExpander.transform(tmp);
					Expr constantExpand = ConstantExpander.transform(functionExpand);
					Expr expand = expander.transformMM(constantExpand);
					Expr optimized = SqlExprOptimizer.optimizeMM(constantExpand);
					*/


					// Substitute all variables with sql bridges
					// TODO Do the variable transformation
					// Then push down
					
					//Expr tmp = sub.transformMM(expr);
					Expr optimized = SqlExprOptimizer.optimizeMM(tmp);					
					Expr substituted = substitutor.transformMM(optimized);
					Expr pushedExpr = PushDown.pushDownMM(substituted);

					SqlExpr sqlExpr = null;
					if(pushedExpr instanceof ExprSqlBridge) {
						sqlExpr = ((ExprSqlBridge) pushedExpr).getSqlExpr();
					} else {
						throw new RuntimeException("Could not push an expression");
					}
					
					if(isSatisfiable(sqlExpr)) {
						ors.add(sqlExpr);
						foundSatisfiableJoinCondition = true;
					}
				}
				
				newTermDefs.add(new TermDef(ea.getExpr(), ras));
			}

			result.getSparqlVarToExprs().putAll(var, newTermDefs);

			// For the given variable there is no satisfiable join
			// so the join is empty
			if(!foundSatisfiableJoinCondition) {
				SqlNode x = new SqlNodeEmpty();
				x.getSparqlVarToExprs().putAll(result.getSparqlVarToExprs());
				x.getAliasToColumn().putAll(result.getAliasToColumn());
				return x;
			}
			
			
			// Or-ify the expressions
			if(!ors.isEmpty()) {
				SqlExpr joinExpr = SqlExprUtils.orifyBalanced(ors);
				
				result.addCondition(joinExpr);
			}
		}			

		// Check for unsatisfiable node
		// TODO We need to consider the join condition and the filters
		// as a unit (andify them); Example:
		// join ... on(a = b) where (a != b)
		// Treating the filter and the join condition separately causes us
		// to miss the unsatisfiability in this case
		if(!isSatisfiable(result.getConditions())) {
			SqlNode x = new SqlNodeEmpty();
			x.getSparqlVarToExprs().putAll(result.getSparqlVarToExprs());
			x.getAliasToColumn().putAll(result.getAliasToColumn());
			return x;
		}
		
		// Of the helper variables, only copy those over, which are still
		// referenced

		return result;
	}

	public static SqlNode distinct(SqlNode a) {
		SqlDistinct result = new SqlDistinct(a.getAliasName(), a);

		result.getAliasToColumn().putAll(a.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());

		return result;
	}
	
	public static SqlNode slice(SqlNode a, ColRelGenerator generator, long start, long length) {

		SqlSlice result = new SqlSlice(a, start, length);
		result.getAliasToColumn().putAll(a.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
		
		/*
		String aliasName = a.getAliasName() != null
				? a.getAliasName()
				: generator.next();

		SqlSlice result = new SqlSlice(aliasName, a, start, length);

		if(a.getAliasName() == null) {
			SqlNode newProj = createNewAlias(aliasName, a, generator);
			result.getAliasToColumn().putAll(newProj.getAliasToColumn());
			result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());

		} else {
			result.getAliasToColumn().putAll(a.getAliasToColumn());
			result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
		}
		*/
		
		
		return result;
	}

	
	
	
	/**
	 * 
	 * Select b.i i, b.j j From (
	 *     Select a.i i, a.j j From ( ... ) a
	 * ) b
	 * 
	 * 
	 * Updates all references in the target node.
	 * The sparqlVarsToExpr map just needs to be copied
	 * For the 
	 * 
	 * @param alias
	 * @param target
	 * @param source
	 */
	public static void createNewAlias(String alias, SqlNode target, SqlNode source) {
		
		target.getSparqlVarToExprs().putAll(source.getSparqlVarToExprs());

		
		for(Entry<String, SqlExpr> entry : source.getAliasToColumn().entrySet()) {
			target.getAliasToColumn().put(entry.getKey(), new SqlExprColumn(source.getAliasName(), entry.getKey(), entry.getValue().getDatatype()));
		}
	}
	

	
	public static void replaceAlias(String alias, SqlNode node, ColRelGenerator columnNameColRelGenerator) {
		SqlNode tmp = createNewAlias(alias, node, columnNameColRelGenerator);
		
		node.getAliasToColumn().clear();
		node.getSparqlVarToExprs().clear();
		
		node.getAliasToColumn().putAll(tmp.getAliasToColumn());
		node.getSparqlVarToExprs().putAll(tmp.getSparqlVarToExprs());
	}


	public static SqlAlias createNewAlias(String alias, SqlNode node, ColRelGenerator generator) {
		SqlAlias result = new SqlAlias(alias, node);
		
		Map<Var, Expr> varRename = new HashMap<Var, Expr>();
    	for(Entry<String, SqlExpr> col : node.getAliasToColumn().entrySet()) {
    		String newColumnName = col.getKey();//generator.nextColumn();
    		
    		varRename.put(Var.alloc(col.getKey()), new ExprVar(newColumnName));
    		result.getAliasToColumn().put(newColumnName, new SqlExprColumn(alias, col.getKey(), col.getValue().getDatatype()));
    	}
    	
    	NodeExprSubstitutor substitutor = new NodeExprSubstitutor(varRename);

    	for(Entry<Var, TermDef> entry : node.getSparqlVarToExprs().entries()) {
    		Expr newExpr = substitutor.transformMM(entry.getValue().getExpr());
    		
    		result.getSparqlVarToExprs().put(entry.getKey(), new TermDef(newExpr, entry.getValue().getRestrictions()));
    	}
		
    	
    	return result;
	}


	public static SqlProjection wrapWithProjection(String newAlias, SqlNode tmp, ColRelGenerator generator) {
		
		SqlProjection node = new SqlProjection(newAlias, tmp);
		node.getAliasToColumn().putAll(tmp.getAliasToColumn());
		node.getSparqlVarToExprs().putAll(tmp.getSparqlVarToExprs());
		
		SqlProjection result = new SqlProjection(newAlias, node);

		SqlAlias newProj = createNewAlias(newAlias, node, generator);
		
		result.getAliasToColumn().putAll(newProj.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
		
		/*
		Map<Var, Expr> varRename = new HashMap<Var, Expr>();
    	for(Entry<String, SqlExpr> col : node.getAliasToColumn().entrySet()) {
    		String newColumnName = generator.next();
    		
    		varRename.put(Var.alloc(col.getKey()), new ExprVar(newColumnName));
    		result.getAliasToColumn().put(newColumnName, new SqlExprColumn(newAlias, col.getKey(), col.getValue().getDatatype()));
    	}
    	
    	NodeExprSubstitutor substitutor = new NodeExprSubstitutor(varRename);

    	for(Entry<Var, TermDef> entry : node.getSparqlVarToExprs().entries()) {
    		Expr newExpr = substitutor.transformMM(entry.getValue().getExpr());
    		
    		result.getSparqlVarToExprs().put(entry.getKey(), new TermDef(newExpr, entry.getValue().getRestrictions()));
    	}
    	*/

    	
    	return result;
	}

	
	/**
	 * Ordering requires some wrapping:
	 * 
	 * Given Order(subNode), we transform to SqlAlias(SqlProjection(Order(Alias(subNode)))
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * First, we add a projection to whatever node we have
	 * Then, in this projection we group by the variables we are sorting
	 * Then we wrap this projection
	 * Finally, we create an SqlOrderBy node with the order by expressions
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param a
	 * @param conditions
	 * @param generator
	 * @return
	 */
	public static SqlNode order(SqlNode a, List<SortCondition> conditions, ColRelGenerator generator) {
		List<SqlSortCondition> sqlConditions = new ArrayList<SqlSortCondition>();


		/*
		if(a.getAliasName() == null) {
			//a = wrapWithProjection(generator.next(), a, generator);
			String newAlias = generator.nextRelation();
			SqlNode newProj = createNewAlias(newAlias, a, generator);
			a = new SqlProjection(newAlias, a);
			a.getAliasToColumn().putAll(newProj.getAliasToColumn());
			a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());

		}*/
		

		// If the node does not have an alias yet, create one
		//if(a.getAliasName() == null) {
		//}
		
			
		SqlNode subSelect = createNewAlias(generator.nextRelation(), a, generator);

		// Wrap whatever we have with a new projection (-> sub select)
		// This new projection is the basis for the group by
		/*
		SqlProjection groupByNode = new SqlProjection(generator.nextRelation(), projection);
    	SqlSelectBlockCollector.copyProjection(groupByNode, tmp);
    	*/
		SqlNode groupByNode = subSelect;

		//SqlNode projection = subSelect;

    	SqlNode tmp = createNewAlias(groupByNode.getAliasName(), subSelect, generator);

		
		// Transform the conditions
		Generator genG = Gensym.create("s");
    	for(SortCondition condition : conditions) {

    		
    		// SOMEWHAT HACKY: We need to avoid name clashes when grouping
    		// A really hard to find bug was, the when e.g. grouping c_{5-10}, it might have
    		// redefined e.g. c_1 
    		// Group by the variables
    		for(Var var : condition.getExpression().getVarsMentioned()) {
    			SqlAlgebraToString.groupBy(var, groupByNode, tmp, genG); //generator.forColumn());
    		}
    	}
    	

    	/*
    	if(true) {
    		return groupByNode;
    	}*/
    	
    	// TODO Somewhere in the below code it happens, that the projection gets messed up (resulting in e.g. <1> being returned as a property)
    	
    	// Now we have created the "group by projection"
    	// However, now we need to wrap it with another projection, in which we can add any expressions appearing in the order by
		SqlProjection orderByNode = new SqlProjection(generator.nextRelation(), groupByNode);
    	SqlNode tmp2 = createNewAlias(orderByNode.getAliasName(), groupByNode, generator);
    	SqlSelectBlockCollector.copyProjection(orderByNode, tmp2);
    	
    	
    		// order by ...  str(?o)
    		// with ?o = uri(concat())
    		
    	
    	// Create a new projection

    	/*
    	String orderAlias = generator.nextRelation();
    	SqlProjection orderNode = new SqlProjection(orderAlias, projection);
    	createNewAlias(projection.getAliasName(), orderNode, projection);
    	*/
    	//SqlSelectBlockCollector.copyProjection(orderNode, projection);

    	
    	
    	//SqlNode orderNode = wrapWithProjection(projection, generator); //new SqlProjection(projection.getAliasName(), projection);
    	

    	SqlNode result = new SqlNodeOrder(orderByNode.getAliasName(), orderByNode, sqlConditions);
	

		Generator gen = Gensym.create("o");

		// Build the sort conditions for our current node
    	for(SortCondition condition : conditions) {
    		
    		SqlExprList pushed;

    		if(condition.getExpression().isVariable()) {
    			Var v = condition.getExpression().asVar();
    			
    			Collection<TermDef> exprs = orderByNode.getSparqlVarToExprs().get(v);
    			
    			
    			if(exprs.isEmpty()) {
    				logger.warn("Variable does not exist for sorting");
    				continue;
    			} else if(exprs.size() > 1) {
    				throw new RuntimeException("Should not happen"); // because we grouped by the var
    			}
    			
    			TermDef def = exprs.iterator().next();
    			
    			Expr expr = def.getExpr();
    			if(expr instanceof E_RdfTerm) {
    				E_RdfTerm term = (E_RdfTerm)expr;

    				pushed = new SqlExprList();
    				for(int i = 0; i < 4; ++i) {
    					Expr arg = term.getArgs().get(i);
    					SqlExpr sqlExpr = forceShallowPushDown(arg, orderByNode.getAliasToColumn());
    					//SqlExpr sqlExpr = fullPush(arg, a);
    					
    					//SqlExpr sqlExpr = shallowPush(arg, a);
    					
    					pushed.add(sqlExpr);
    				}
    			} else {
    				throw new RuntimeException("Should not happen");
    			}
    		}
    		else {
 				pushed = shallowPush(new ExprList(condition.getExpression()), orderByNode);
    			//pushed = fullPush(new ExprList(condition.getExpression()), a);
 			}
    		
    		//SqlExprList pushed = forcePushDown(new ExprList(condition.getExpression()), a);
    		
    		for(SqlExpr sqlExpr : pushed) {
        		// Don't sort by constant expression
    			Set<SqlExprColumn> columnsMentioned = SqlExprBase.getColumnsMentioned(sqlExpr); 
    			if(columnsMentioned.isEmpty()) {
        			continue;
        		}
        		
    			boolean allowExprsInOrderByClause = false;
    			// Sql cannot order by columns that are not selected;
    			// Therefore, any order expression becomes part of the projection
    			// TODO Implement this properly!!!
    			if(allowExprsInOrderByClause) {
    				sqlConditions.add(new SqlSortCondition(sqlExpr, condition.getDirection()));
    			} else {
    				String dummyColumn = gen.next(); //generator.nextColumn();

    				orderByNode.getAliasToColumn().put(dummyColumn, sqlExpr);
    				
    				sqlConditions.add(new SqlSortCondition(new SqlExprColumn(null, dummyColumn, sqlExpr.getDatatype()), condition.getDirection()));
    				
    				//result.getAliasToColumn().put(dummyColumn, new SqlExprColumn(projection.getAliasName(), dummyColumn, sqlExpr.getDatatype()));
    			}
    		}
    	}
    		
    		/*
    		for(Var var : condition.getExpression().getVarsMentioned()) {
    			for(Expr expr : a.getSparqlVarToExprs().asMap().get(var)) {
    				for(Var columnName : expr.getVarsMentioned()) {
    					
    					
    					
    					
    					SqlExpr sqlExpr = a.getAliasToColumn().get(columnName.getName());
    					
    					
    					
    					String exprStr = sqlExprSerializer.serialize(sqlExpr);
    					
    					if(dirStr != null) {
    						//exprStr = dirStr + "(" + exprStr + ")";
    						exprStr = exprStr + " " + dirStr;
    					}
    					
    					sortColumnExprStrs.add(exprStr);
    				}
    			}
    		}
    	}
    	*/
		
		/*
		result.getAliasToColumn().putAll(a.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
		 */

		
		//SqlProjection r2 = new SqlProjection(aliasName, orderNode);
		//createNewAlias(aliasName, r2, orderNode);

		result.getAliasToColumn().putAll(orderByNode.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(orderByNode.getSparqlVarToExprs());
		return result;

		

		/*
		SqlProjection wrap = new SqlProjection(generator.nextRelation(), result);
    	SqlNode tmp3 = createNewAlias(wrap.getAliasName(), result, generator);
    	SqlSelectBlockCollector.copyProjection(wrap, tmp3);

    	return wrap;
    	*/
	}
	
		
	/*
	public static SqlExpr fullPush(Expr expr, SqlNode node) {
		return fullPush(new ExprList(exprs), node.getAliasToColumn(), node.getSparqlVarToExprs());
	}
	
	public static SqlExprList fullPush(ExprList exprs, Map<String, SqlExpr> aliasToColumn, Multimap<Node, Expr> sparqlVarToExprs) {
	}*/
	
	
	
	public static SqlExpr shallowPush(Expr expr, SqlNode node) {
		return shallowPush(new ExprList(expr), node).get(0);
	}
	
	
	public static Map<String, SqlExpr> createShallowAliasToColumn(Map<String, SqlExpr> aliasToColumn) {
		
		Map<String, SqlExpr> result = new HashMap<String, SqlExpr>();
		for(Entry<String, SqlExpr> entry : aliasToColumn.entrySet()) {
			result.put(entry.getKey(), new SqlExprColumn(null, entry.getKey(), entry.getValue().getDatatype()));
		}

		return result;
	}
	
	/**
	 * Like a full push, except it does not replace sql-columns with their definition
	 * 
	 * @param exprs
	 * @param node
	 * @return
	 */	
	public static SqlExprList shallowPush(ExprList exprs, SqlNode node) {
		
		Map<String, SqlExpr> aliasToColumn = createShallowAliasToColumn(node.getAliasToColumn());
		
		return fullPush(exprs, aliasToColumn, node.getSparqlVarToExprs());
	}
	
	
	public static SqlExpr fullPush(Expr expr, SqlNode node) {
		// Could be a bit more efficient...
		SqlExprList tmp = fullPush(new ExprList(expr), node);
		if(tmp.isEmpty()) {
			throw new RuntimeException("Should not happen");
		}
		
		return tmp.get(0);
	}
	
	public static SqlExprList fullPush(ExprList exprs, SqlNode node) {
		return fullPush(exprs, node.getAliasToColumn(), node.getSparqlVarToExprs());
	}
	
	
	
	public static SqlExprList fullPush(ExprList exprs, Map<String, SqlExpr> aliasToColumn, Multimap<Var, TermDef> sparqlVarToExprs) {
		
		NodeExprSubstitutor substitutor = aliasToColumn == null
				? null
			    : createSubstitutor(aliasToColumn);

		SqlExprList result = new SqlExprList();
		
		for (Expr expr : exprs) {
			
			List<Var> vars = new ArrayList<Var>(expr.getVarsMentioned());
			List<List<TermDef>> cartesianBase = new ArrayList<List<TermDef>>();
			
			// Substitute each sparql variable with its set of definitions on the sql level
			for(Var var : vars) {
				Collection<TermDef> varExprs = sparqlVarToExprs.get(var);
				cartesianBase.add(new ArrayList<TermDef>(varExprs));
			}
			
			CartesianProduct<TermDef> cartesian = new CartesianProduct<TermDef>(cartesianBase);
			

			List<SqlExpr> ors = new ArrayList<SqlExpr>();
			for(List<TermDef> items : cartesian) {
				
				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
				for(int i = 0; i < vars.size(); ++i) {
					Var var = vars.get(i);
					TermDef item = items.get(i);
					
					expanderMap.put(var, item.getExpr());
				}

				NodeExprSubstitutor expander = new NodeExprSubstitutor(expanderMap);
				
				
				
				/*
				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
				for(Entry<Node, Expr> entry : node.getSparqlVarToExprs().entrySet()) {
					expanderMap.put(Var.alloc(entry.getKey()), entry.getValue());
				}*/

				/*
				if(expr instanceof E_LessThan) {
					System.out.println("lessthan here");
				}
				*/
				
				// Expand variables in the filter expression
				// Example: Given ?r = term(...) and Filter(regex(?r...))
				//          We will get Filter(regex(term(...)))
				
				
				Expr functionExpand = FunctionExpander.transform(expr);
				Expr constantExpand = ConstantExpander.transform(functionExpand);
				Expr expand = expander.transformMM(constantExpand);
				Expr simplified = SqlExprOptimizer.optimizeMM(expand);
				
				// Expr tmp = sub.transformMM(expr);
				// TODO Do the variable transformation

				Expr subbed = (substitutor == null)
						? simplified
						: substitutor.transformMM(simplified);
						
				Expr pushed = PushDown.pushDownMM(subbed);

				SqlExpr sqlExpr = null;
				if(pushed instanceof ExprSqlBridge) {
					sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
				} else {
					throw new RuntimeException("Could not push expressions");
				}

				ors.add(sqlExpr);			
			}
			
			SqlExpr orified = SqlExprUtils.orifyBalanced(ors);
			// TODO Should we assume false or true?
			if(orified != null) {
				result.add(orified);
			}
		}

		// FIXME We could bail out earlier
		if(!isSatisfiable(result)) {
			return new SqlExprList(SqlExprValue.FALSE);
		}

		
		// Remove all 'true' from the result
		// In Postgres 9.1, a "WHERE TRUE" in a union prevents the union to become an append-relation
		// As a result, in LinkedGeoData "Select { ?s dc:modified ?o . } Order By ?s" won't work (efficiently)
		// FIXME Although it makes sense to already get rid of unnecessary exprs here,
		// it might be good to have a full SQL-level optimization at some later stage
		Iterator<SqlExpr> it = result.iterator();
		while(it.hasNext()) {
			SqlExpr item = it.next();
			if(item.equals(SqlExprValue.TRUE)) {
				it.remove();
			}
		}
		
		
		return result;
	}

	
	public static boolean isSatisfiable(SqlExpr sqlExpr)
	{
		Expr expr = SqlExprToExpr.convert(sqlExpr);
		if(expr.equals(SqlExprToExpr.UNKNOWN)) {
			return true;
		}
		
		
		Set<Set<Expr>> dnf = DnfUtils.toSetDnf(expr);
		
		return DnfUtils.isSatisfiable(dnf);
	}
	
	public static boolean isSatisfiable(SqlExprList sqlExprs) {
		if(sqlExprs.isEmpty()) {
			return true;
		}
		
		for(SqlExpr sqlExpr : sqlExprs) {
			if(!isSatisfiable(sqlExpr)) {
				return false;
			}
		}
		
		return true;
	}

	public static SqlNode group(SqlNode a, VarExprList groupVars, List<ExprAggregator> exprAggregator) {
		
		NodeExprSubstitutor substitutor = createSubstitutor(a.getAliasToColumn());

		
		SqlGroup result = new SqlGroup(a);

		for(ExprAggregator item : exprAggregator) {
			Expr expr = item.getExpr();
			
			SqlExpr sqlExpr = forcePushDown(expr, substitutor);
			//System.out.println(sqlExpr);
		}
		
		throw new RuntimeException("Implement me");
		//return result;
	}

	/**
	 * Replaces the projection to refer to a new alias
	 * 
	 * @param a
	 * @param newAlias
	 * @param generator
	 */
	public static void updateProjection(SqlNode a, String newAlias, ColRelGenerator generator) {
		SqlNode newProj = createNewAlias(newAlias, a, generator);
		a = new SqlProjection(newAlias, a);
		a.getAliasToColumn().putAll(newProj.getAliasToColumn());
		a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
	}
	
	// NOTE Does in place transformation!

	
	public static SqlNode project(SqlNode a, List<Var> vars, ColRelGenerator generator) {
		//return projectInPlace(a, vars, generator);
		return projectWrap(a, vars, generator);
	}
	
	public static SqlNode projectInPlace(SqlNode result, List<Var> vars, ColRelGenerator generator) {
		
		Set<String> referencedColumns = new HashSet<String>();
		
		
		// Track all columns that contribute to the construction of SQL variables
		for(Var var : vars) {
			for(TermDef def : result.getSparqlVarToExprs().get(var)) {
				for(Var item : def.getExpr().getVarsMentioned()) {
					referencedColumns.add(item.getName());
				}
			}
		}
		
		
	
		result.getAliasToColumn().keySet().retainAll(referencedColumns);
		result.getSparqlVarToExprs().keySet().retainAll(vars);
		
		return result;
		
	}
	
	
	public static SqlNode projectWrap(SqlNode a, List<Var> vars, ColRelGenerator generator) {

		/*
		if(a.getAliasName() == null) {

			if(a instanceof SqlJoin || a instanceof SqlMyRestrict || a instanceof SqlProjection) {				
				SqlNode newProj = new SqlProjection(newAlias, a);
				newProj.getAliasToColumn().putAll(a.getAliasToColumn());
				newProj.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
				a = newProj;
			} else {
				SqlNode newProj = createNewAlias(newAlias, a, generator);
				a = new SqlProjection(newAlias, a);
				a.getAliasToColumn().putAll(newProj.getAliasToColumn());
				a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
			}
			//a = new Projection() 
			//a = wrapWithProjection(generator.next(), a, generator);
			//replaceAlias(alias, node, columnNameColRelGenerator)
			
			SqlNode newProj = createNewAlias(newAlias, a, generator);
			a = new SqlProjection(newAlias, a);
			a.getAliasToColumn().putAll(newProj.getAliasToColumn());
			a.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
		}
		*/
		String newAlias = generator.nextRelation();

		
		SqlNode newProj = createNewAlias(newAlias, a, generator);
		SqlProjection result = new SqlProjection(newAlias, a);
		result.getAliasToColumn().putAll(newProj.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(newProj.getSparqlVarToExprs());
		
		/*
		SqlProjection result = new SqlProjection(a.getAliasName(), a);
		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());
		result.getAliasToColumn().putAll(a.getAliasToColumn());
		*/
		//createNewAlias(a.getAliasName(), result, a);
		
		
		Set<String> referencedColumns = new HashSet<String>();
		
		
		// Track all columns that contribute to the construction of SQL variables
		for(Var var : vars) {
			for(TermDef def : result.getSparqlVarToExprs().get(var)) {
				for(Var item : def.getExpr().getVarsMentioned()) {
					referencedColumns.add(item.getName());
				}
			}
		}
		
		
	
		result.getAliasToColumn().keySet().retainAll(referencedColumns);
		result.getSparqlVarToExprs().keySet().retainAll(vars);
		
		return result;
	}
	
	
	public static SqlNode filter(SqlNode a, ExprList exprs, ColRelGenerator generator) {

		SqlExprList sqlExprs = fullPush(exprs, a);

		SqlNode result;

		/*
		if(a.getAliasName() == null) {
			String newAlias = generator.next();
			SqlNode newProj = new SqlProjection(newAlias, a);
			newProj.getAliasToColumn().putAll(a.getAliasToColumn());
			newProj.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());			
			a = newProj;
		}
		*/

		
		if(a instanceof SqlNodeEmpty || !isSatisfiable(sqlExprs)) {
			fullPush(exprs, a);
			result = new SqlNodeEmpty();
		} else {

			SqlMyRestrict tmp;
			if(a instanceof SqlMyRestrict) {
				tmp = (SqlMyRestrict)a;
			}
			else {				
				//tmp = new SqlMyRestrict(a.getAliasName(), a);
				tmp = new SqlMyRestrict(null, a);
			}

			tmp.getConditions().addAll(sqlExprs);
		
			result = tmp;
		}		

		
		//SqlExprToExpr.convert(expr)
		
		//NodeExprSubstitutor expander = createSparqlVarExpander(a);


		result.getAliasToColumn().putAll(a.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());

		return result;
	}

	
	/**
	 * 
	 * 
	 * Each variable may have multiple expressions attached to it.
	 * As a consequence, each combination of expressions has to be
	 * evaluated.
	 * 
	 * @param a
	 * @param exprs
	 * @return
	 */
	/*
	public static SqlNode filter(SqlNode a, ExprList exprs) {

		SqlMyRestrict result;
		if(a instanceof SqlMyRestrict) {
			result = (SqlMyRestrict)a;
		}
		else {
			result = new SqlMyRestrict(a.getAliasName(), a);
		}
		
		//NodeExprSubstitutor expander = createSparqlVarExpander(a);
		NodeExprSubstitutor substitutor = createSubstitutor(a);

		for (Expr expr : exprs) {
			
			List<Var> vars = new ArrayList<Var>(expr.getVarsMentioned());
			List<List<Expr>> cartesianBase = new ArrayList<List<Expr>>();
			
			for(Var var : vars) {
				Collection<Expr> varExprs = a.getSparqlVarToExprs().get(var);
				cartesianBase.add(new ArrayList<Expr>(varExprs));
			}
			
			CartesianProduct<Expr> cartesian = new CartesianProduct<Expr>(cartesianBase);
			

			List<SqlExpr> ors = new ArrayList<SqlExpr>();
			for(List<Expr> items : cartesian) {
				
				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
				for(int i = 0; i < vars.size(); ++i) {
					Var var = vars.get(i);
					Expr item = items.get(i);
					
					expanderMap.put(var, item);
				}

				NodeExprSubstitutor expander = new NodeExprSubstitutor(expanderMap);
				
				
				
				/*
				Map<Var, Expr> expanderMap = new HashMap<Var, Expr>();
				for(Entry<Node, Expr> entry : node.getSparqlVarToExprs().entrySet()) {
					expanderMap.put(Var.alloc(entry.getKey()), entry.getValue());
				}* /

				
				// Expand variables in the filter expression
				// Example: Given ?r = term(...) and Filter(regex(?r...))
				//          We will get Filter(regex(term(...)))
				Expr functionExpand = FunctionExpander.transform(expr);
				Expr constantExpand = ConstantExpander.transform(functionExpand);
				Expr expand = expander.transformMM(constantExpand);
				Expr simplified = SqlExprTranslator.optimizeMM(expand);
				
				// Expr tmp = sub.transformMM(expr);
				// TODO Do the variable transformation
				Expr subbed = substitutor.transformMM(simplified); 
				Expr pushed = PushDown.pushDownMM(subbed);

				SqlExpr sqlExpr = null;
				if(pushed instanceof ExprSqlBridge) {
					sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
				} else {
					throw new RuntimeException("Could not push expressions");
				}

				ors.add(sqlExpr);			
			}
			
			SqlExpr orified = SqlExprUtils.orifyBalanced(ors);
			result.getConditions().add(orified);
		}

		result.getAliasToColumn().putAll(a.getAliasToColumn());
		result.getSparqlVarToExprs().putAll(a.getSparqlVarToExprs());

		return result;
	}
*/
	
	
	/**
	 * Given two expressions, walk them in a top down fashion and return: .) the
	 * expression that is common to both, where the leaves where there were
	 * differences contain helper variables
	 * 
	 * .) For both expression, a mapping of helper variable to the sub
	 * expression which would in combination with the common part yield the
	 * original expression again.
	 * 
	 * i = f(g(h(a, b)) j = f(g(h(c, d))
	 * 
	 * becomes: common = f(g(h(x, y))
	 * 
	 * a.x = a a.y = b
	 * 
	 * b.x = c b.y = d
	 * 
	 * Note: A helper variable correspond to a single sql colum
	 * 
	 * Ideally, if we had a union of the same uris generated in the same way
	 * from different relations, this would allow us to end up with a simple
	 * union:
	 * 
	 * 
	 * 
	 * 
	 * @param a
	 * @param b
	 */
	public void unifyCommonExpression(Expr a, Expr b) {

	}

	/**
	 * New approach:
	 * Only combine columns if they have the same datatype AND result in the same sparql var expression.
	 * 
	 * In the case of constants we add a discriminator column.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * Old approach: (Top-Down factoring out)
	 * 
	 * Problem: We want to create an SQL statement corresponding to the union of
	 * two SqlNodeBindings:
	 * 
	 * Select id From A; ?a = Uri(Concat("prefix", id)) Select c1, c2 From B; ?a
	 * = Uri(Concat(?c1, ?c2))
	 * 
	 * This requires aligning the RDF variables:
	 * 
	 * Select Concat("prefix") As a_lex From (Select id From A); ?a =
	 * Uri(unionAlias.a_lex) Select Concat(c1, c2) As a_lex From (Select c1, c2
	 * From B); ?a = Uri(unionAlias.a_lex)
	 * 
	 * -> Select ... As a_lex ... Union All Select ... As a_lex
	 * 
	 * Union(Union(Union(Filter(A), Filter(B)), Filter(C))
	 * 
	 * 
	 * We are trying to keep the size of the projections minimal: . Only select
	 * non-static data from the underlying relation, for constants, keep them in
	 * the ?a = RdfTerm(const, ...) expression. . Factor out common sub
	 * expressions for common variables: Example: Assuming there is a union C of
	 * A and B, ?A.x = expr(a.id) and ?B.x = expr'(b.id) So we are looking for
	 * expressions that are equalTo each other, based on a given mapping of
	 * variables
	 * 
	 * We can keep the binding ?C.x = expr(c.id), so there is no need to
	 * translate the expr to sql. TODO We must consider datatypes when doing
	 * this!!!
	 *
	 * 
	 * Dealing with datatypes:
	 * A sparql variable may get its value assigned from different expressions.
	 * 
	 * Example:
	 * ?p = Uri(concat(?prefixColumn, ?id))
	 * ?p = PlainLiteral(?name)
	 * 
	 * ?prefixColumn string
	 * id int
	 * name int
	 * 
	 * 
	 * a) Group expressions by variables,
	 * b) For each variable: group the datatypes they make use of.
	 * 
	 * But then we have to find common expressions within each datatype group.
	 * An unoptimized version would be: for each expression make sure that
	 * it references a unique set of columns
	 * 
	 * So... we group the expressions by datatype, and within the group we check
	 * what we can factor out.
	 * 
	 * 
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static SqlNode unionNew(ColRelGenerator generator, List<SqlNode> sqlNodes) {

		// Prepare the data structures from which the
		// result node will be created
		Multimap<Var, TermDef> commons = HashMultimap.create();
		
		// For each union member, prepare a datastructe for its new projection
		List<Multimap<Var, TermDef>> projections = new ArrayList<Multimap<Var, TermDef>>();
		for (int i = 0; i < sqlNodes.size(); ++i) {
			Multimap<Var, TermDef> tmp = HashMultimap.create();
			projections.add(tmp);
		}

		// Now we can start with the actual work				
		Multimap<Var, Integer> varToSqlNode = HashMultimap.create();

		// Map each variable to the set of corresponding nodes
		for (int i = 0; i < sqlNodes.size(); ++i) {
			SqlNode sqlNode = sqlNodes.get(i);
			for (Node var : sqlNode.getSparqlVarsMentioned()) {
				varToSqlNode.put((Var)var, i);
			}
		}

		
		// TODO
		// If a variable maps to a constant, than the mapping does not apply to any union member
		// that does not define the constant.
		// This means we have to introduce a column for discrimination, which contains NULL for
		// all union members where to constaint is not applicable
 
		
		Generator aliasGen = Gensym.create("c");
		ExprCommonFactor factorizer = new ExprCommonFactor(aliasGen);

		
		Map<String, SqlDatatype> allColumnsToDatatype = new HashMap<String, SqlDatatype>();
		
		
		// For each variable, cluster the corresponding expressions
		for(Entry<Var, Collection<Integer>> entry : varToSqlNode.asMap().entrySet()) {
			Var var = entry.getKey();
			
			
			// TODO Just clustering by hash may result in clashes!!!
			// For each hash we have to keep a list an explicitly compare for structural equivalence
			Multimap<Integer, ArgExpr> cluster = HashMultimap.create();

			//IBiSetMultimap<Integer, Integer> exprToOrigin = new BiHashMultimap<Integer, Integer>();
			//Multimap<Integer, Integer> exprToOrigin = HashMultimap.create();
			
			for (int index : entry.getValue()) {
				SqlNode sqlNode = sqlNodes.get(index);

				Collection<TermDef> exprsForVar = sqlNode.getSparqlVarToExprs().get(var);
				
				for(TermDef def : exprsForVar) {
				
					Map<String, SqlDatatype> columnToDatatype = SqlNodeUtil.getColumnToDatatype(sqlNode);
					//Integer hash = ExprStructuralHash.hash(def.getExpr(), columnToDatatype);
					Integer hash = ExprDatatypeHash.hash(def.getExpr(), columnToDatatype);
								
					cluster.put(hash, new ArgExpr(def.getExpr(), index));
				}				
			}
			
		
			// Process the clusters we just created

			
			// First, we build a list of exprs of the cluster and
			// a map for mapping the clustered exprs back to their nodes
			for(Entry<Integer, Collection<ArgExpr>> clusterEntry : cluster.asMap().entrySet()) {
				Collection<ArgExpr> argExprs = clusterEntry.getValue();
					
				List<Expr> exprs = new ArrayList<Expr>();
				Map<Integer, Integer> exprToNode = new HashMap<Integer, Integer>();

				int i = 0;
				for(ArgExpr argExpr : argExprs) {
					exprs.add(argExpr.getExpr());
					exprToNode.put(i, argExpr.getIndex());
					
					++i;
				}
				

				// Now we can finally factor the cluster
				List<Map<Var, Expr>> partialProjections = new ArrayList<Map<Var, Expr>>();
				Expr common = factorizer.transform(exprs, partialProjections);

				
				// For our current variable, we can set up the projection of the result...
				commons.put(var, new TermDef(common));

				// ... and now we adjust the projections of the children accordingly
				for (int j = 0; j < partialProjections.size(); ++j) {
					int originalIndex = exprToNode.get(j);

					//SqlNode tmp = sqlNodes.get(originalIndex);
					Multimap<Var, TermDef> projection = projections.get(originalIndex);
					
					Map<Var, Expr> partialProjection = partialProjections.get(j);
					
					for(Entry<Var, Expr> ppEntry : partialProjection.entrySet()) {
						projection.put(ppEntry.getKey(), new TermDef(ppEntry.getValue()));
					}					
				}
			}			
		}

		// Build the final result from the information we gathered
		
		for (int i = 0; i < projections.size(); ++i) {
			SqlNode tmp = sqlNodes.get(i);
			Multimap<Var, TermDef> projection = projections.get(i);

			// Projection.Var becomes the new column alias
			// Projection.Expr is pushed down to an sqlExpr
			// Projection.Expr's vars are replaced with the original column defs
			
			NodeExprSubstitutor substitutor = createSubstitutor(tmp);
			Map<String, SqlExpr> subbedProj = new HashMap<String, SqlExpr>();
			for(Entry<Var, TermDef> entry : projection.entries()) {
				Expr subbed = substitutor.transformMM(entry.getValue().getExpr());
				Expr pushed = PushDown.pushDownMM(subbed);
				
				if(!(pushed instanceof ExprSqlBridge)) {
					throw new RuntimeException("Could not push down common sub expression");
				}
 
				SqlExpr sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
				
				subbedProj.put(entry.getKey().getName(), sqlExpr);
				
				allColumnsToDatatype.put(entry.getKey().getName(), sqlExpr.getDatatype());
			}

			// Update the projection
			tmp.getAliasToColumn().clear();
			tmp.getAliasToColumn().putAll(subbedProj);

			// Fill up missing columns with null
			//Set<Var> referencedColumns = new HashSet<Var>();
			
			//Set<Var> unreferenedColumns = Sets.difference(allColumnsToDatatype.keySet(), expr.getVarsMentioned());

			
			tmp.getSparqlVarToExprs().clear();
			tmp.getSparqlVarToExprs().putAll(commons);
		}


		for(SqlNode sqlNode : sqlNodes) {
			Set<String> unboundColumns = Sets.difference(allColumnsToDatatype.keySet(), sqlNode.getAliasToColumn().keySet());
		
			for(String columnName : unboundColumns) {
				
				SqlDatatype datatype = allColumnsToDatatype.get(columnName);
				
				sqlNode.getAliasToColumn().put(columnName, SqlExprValue.createNull(datatype));
			}
		}

		String unionAlias = generator.nextRelation();
		SqlNode result = new SqlUnionN(unionAlias, sqlNodes);

		result.getSparqlVarToExprs().putAll(commons);
		
		
		for(Entry<String, SqlDatatype> entry : allColumnsToDatatype.entrySet()) {
			String columnName = entry.getKey();
			SqlDatatype datatype = entry.getValue();
			
			//XXX WAS NULL
			result.getAliasToColumn().put(columnName, new SqlExprColumn(unionAlias, columnName, datatype));
		}
		

		return result;
	}

	
	public static SqlNode union(ColRelGenerator generator, List<SqlNode> sqlNodes) {

		
		if(sqlNodes.isEmpty()) {
			return new SqlNodeEmpty();
		}
		
		if(sqlNodes.size() == 1) {
			return sqlNodes.get(0);
		}
		
		// Prepare the data structures from which the
		// result node will be created
		Multimap<Var, TermDef> commons = HashMultimap.create();
		
		List<Multimap<Var, TermDef>> projections = new ArrayList<Multimap<Var, TermDef>>();
		for (int i = 0; i < sqlNodes.size(); ++i) {
			Multimap<Var, TermDef> tmp = HashMultimap.create();
			projections.add(tmp);
		}

		// Now we can start with the actual work				
		Multimap<Var, Integer> varToSqlNode = HashMultimap.create();

		Generator aliasGen = Gensym.create("c");

		// Push constants into columns
		for (int i = 0; i < sqlNodes.size(); ++i) {
			SqlNode sqlNode = sqlNodes.get(i);
			Set<Var> vars = new HashSet<Var>(sqlNode.getSparqlVarsMentioned()); // FIXME possible redundant hashset
			for (Var var : vars) {
				
				
				List<TermDef> termDefs = new ArrayList<TermDef>(sqlNode.getSparqlVarToExprs().get(var)); 
				
				for(TermDef termDef : termDefs) {
					Expr expr = termDef.getExpr();
					
					
					if(termDef.getExpr().isConstant()) {
						sqlNode.getSparqlVarToExprs().remove(var, termDef);
						
						NodeValue nv = ExprUtils.eval(expr);
						Object o = NodeValueUtils.getValue(nv);
						
						SqlExprValue sv = new SqlExprValue(o);
						
						String columnAlias = aliasGen.next();
						
						// FIXME Assumes a type constructor here - which it should alway be
						List<Expr> newArgs = new ArrayList<Expr>(expr.getFunction().getArgs());
						newArgs.set(1, new ExprVar(columnAlias));
						
						Expr newExpr = ExprCopy.getInstance().copy(expr, newArgs);
						
						TermDef newTermDef = new TermDef(newExpr);
						sqlNode.getSparqlVarToExprs().put(var, newTermDef);
						sqlNode.getAliasToColumn().put(columnAlias, sv);
						
						sqlNode.getSparqlVarToExprs().put(var, newTermDef);
					}

				}
				
				
			}
		}

		
		// Map each variable to the set of corresponding nodes
		for (int i = 0; i < sqlNodes.size(); ++i) {
			SqlNode sqlNode = sqlNodes.get(i);
			for (Node var : sqlNode.getSparqlVarsMentioned()) {
				varToSqlNode.put((Var)var, i);
			}
		}
		

		// TODO Delete the commented out code below if the pushing into columns works 
		// A set of variables that have bindings to constants
		//Set<Var> varConstant = new HashSet<Var>();
		/*
		Map<Var, TermDef> varToConstant = new HashMap<Var, TermDef>();
		for(Entry<Var, Collection<Integer>> entry : varToSqlNode.asMap().entrySet()) {
			Var var = entry.getKey();
						
			for (int index : entry.getValue()) {
				SqlNode sqlNode = sqlNodes.get(index);
				
				sqlNode.
				Collection<TermDef> exprsForVar = sqlNode.getSparqlVarToExprs().get(var);
				

				for(TermDef def : exprsForVar) {
					if(def.getExpr().isConstant()) {
						//varConstant.add(var);
						varToConstant.put(var, def);
					}			
				}
			}
		}*/

		
		// For each var that maps to a constant, add a NULL mapping for
		// every union member which does not define the variable as a contstant
		/*
		for(Entry<Var, TermDef> entry : varToConstant.entrySet()) {
			Var var = entry.getKey();
			TermDef baseTermDef = entry.getValue();
			
			for (int i = 0; i < sqlNodes.size(); ++i) {
				SqlNode sqlNode = sqlNodes.get(i);
				
				Multimap<Var, TermDef> varDefs = sqlNode.getSparqlVarToExprs();
				
				boolean hasConstant = false;
				for(TermDef termDef : varDefs.get(var)) {
					if(termDef.getExpr().isConstant()) {
						hasConstant = true;
						continue;
					}
				}
				
				if(!hasConstant) {
					ExprList exprs = new ExprList();
					List<Expr> args = baseTermDef.getExpr().getFunction().getArgs();
					//System.out.println("Args: " + args.size());
					for(int j = 0; j < args.size(); ++j) {
						Expr expr = j == 1 ? NodeValue.makeString(""): args.get(j);
						
						exprs.add(expr);
					}
					
					Expr newExpr = ExprCopy.getInstance().copy(baseTermDef.getExpr(), exprs); 
					
					varToSqlNode.put((Var)var, i);
					varDefs.put(var, new TermDef(newExpr));
				}				
			}
		}
		*/


		
		
		ExprCommonFactor factorizer = new ExprCommonFactor(aliasGen);

		
		Map<String, SqlDatatype> allColumnsToDatatype = new HashMap<String, SqlDatatype>();
		


		
		// For each variable, cluster the corresponding expressions
		for(Entry<Var, Collection<Integer>> entry : varToSqlNode.asMap().entrySet()) {
			Var var = entry.getKey();
			
			
			Multimap<Integer, ArgExpr> cluster = HashMultimap.create();

			//IBiSetMultimap<Integer, Integer> exprToOrigin = new BiHashMultimap<Integer, Integer>();
			//Multimap<Integer, Integer> exprToOrigin = HashMultimap.create();
			
			RestrictionSet restrictionsForVar = new RestrictionSet(false);
			for (int index : entry.getValue()) {
				SqlNode sqlNode = sqlNodes.get(index);

				Collection<TermDef> exprsForVar = sqlNode.getSparqlVarToExprs().get(var);
				
				
				
				
				
				for(TermDef def : exprsForVar) {
					restrictionsForVar.addAlternatives(def.getRestrictions());
					
					Map<String, SqlDatatype> columnToDatatype = SqlNodeUtil.getColumnToDatatype(sqlNode);

					// TODO This is hacky - we are using a hash for determining structural equivalence
					// So the hash is ok, but still we need to check for collisions
					Integer hash = ExprDatatypeHash.hash(def.getExpr(), columnToDatatype);
								 
					cluster.put(hash, new ArgExpr(def.getExpr(), index));
				}				
			}
			
		
			// Process the sets (clusters) of structurally equivalent expressions we just created

			
			// First, we build a list of exprs of the cluster and
			// a map for mapping the clustered exprs back to their nodes
			for(Entry<Integer, Collection<ArgExpr>> clusterEntry : cluster.asMap().entrySet()) {
				Collection<ArgExpr> argExprs = clusterEntry.getValue();
					
				List<Expr> exprs = new ArrayList<Expr>();
				Map<Integer, Integer> exprToNode = new HashMap<Integer, Integer>();

				int i = 0;
				for(ArgExpr argExpr : argExprs) {
					exprs.add(argExpr.getExpr());
					exprToNode.put(i, argExpr.getIndex());
					
					++i;
				}

				/*
				if(exprs.size() == 1) {
					Expr expr = exprs.get(0);
					if(expr.isConstant()) {
						System.out.println("constant expr: " + expr);
					}
				}
				*/

				// Now we can finally factor the cluster
				List<Map<Var, Expr>> partialProjections = new ArrayList<Map<Var, Expr>>();
				Expr common = factorizer.transform(exprs, partialProjections);

				
				// The common restriction is the disjunction of all participating restrictions
				
				
				
				// For our current variable, we can set up the projection of the result...
				commons.put(var, new TermDef(common, restrictionsForVar));

				// ... and now we adjust the projections of the children accordingly
				for (int j = 0; j < partialProjections.size(); ++j) {
					int originalIndex = exprToNode.get(j);

					//SqlNode tmp = sqlNodes.get(originalIndex);
					Multimap<Var, TermDef> projection = projections.get(originalIndex);
					
					Map<Var, Expr> partialProjection = partialProjections.get(j);
					
					for(Entry<Var, Expr> ppEntry : partialProjection.entrySet()) {
						projection.put(ppEntry.getKey(), new TermDef(ppEntry.getValue()));
					}					
				}
			}			
		}

		// Build the final result from the information we gathered
		
		for (int i = 0; i < projections.size(); ++i) {
			SqlNode tmp = sqlNodes.get(i);
			Multimap<Var, TermDef> projection = projections.get(i);

			// Projection.Var becomes the new column alias
			// Projection.Expr is pushed down to an sqlExpr
			// Projection.Expr's vars are replaced with the original column defs
			
			NodeExprSubstitutor substitutor = createSubstitutor(tmp);
			Map<String, SqlExpr> subbedProj = new HashMap<String, SqlExpr>();
			for(Entry<Var, TermDef> entry : projection.entries()) {
				Expr subbed = substitutor.transformMM(entry.getValue().getExpr());
				Expr pushed = PushDown.pushDownMM(subbed);
				
				if(!(pushed instanceof ExprSqlBridge)) {
					throw new RuntimeException("Could not push down common sub expression");
				}
 
				SqlExpr sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
				
				subbedProj.put(entry.getKey().getName(), sqlExpr);
				
				allColumnsToDatatype.put(entry.getKey().getName(), sqlExpr.getDatatype());
			}

			// Update the projection
			tmp.getAliasToColumn().clear();
			tmp.getAliasToColumn().putAll(subbedProj);

			// Fill up missing columns with null
			//Set<Var> referencedColumns = new HashSet<Var>();
			
			//Set<Var> unreferenedColumns = Sets.difference(allColumnsToDatatype.keySet(), expr.getVarsMentioned());

			
			tmp.getSparqlVarToExprs().clear();
			tmp.getSparqlVarToExprs().putAll(commons);
		}


		for(SqlNode sqlNode : sqlNodes) {
			Set<String> unboundColumns = Sets.difference(allColumnsToDatatype.keySet(), sqlNode.getAliasToColumn().keySet());
		
			for(String columnName : unboundColumns) {
				
				SqlDatatype datatype = allColumnsToDatatype.get(columnName);
				
				sqlNode.getAliasToColumn().put(columnName, SqlExprValue.createNull(datatype));
			}
		}

		String unionAlias = generator.nextRelation();
		SqlNode result = new SqlUnionN(unionAlias, sqlNodes);

		result.getSparqlVarToExprs().putAll(commons);
		
		
		for(Entry<String, SqlDatatype> entry : allColumnsToDatatype.entrySet()) {
			String columnName = entry.getKey();
			SqlDatatype datatype = entry.getValue();
			
			//XXX WAS NULL
			result.getAliasToColumn().put(columnName, new SqlExprColumn(unionAlias, columnName, datatype));
		}
		

		return result;
	}
		
	
	
	
/*
		Map<Var, Expr> commons = new HashMap<Var, Expr>();
		List<Map<Var, Expr>> projections = new ArrayList<Map<Var, Expr>>();
		for (int i = 0; i < sqlNodes.size(); ++i) {
			projections.add(new HashMap<Var, Expr>());
		}
* /
		if (sqlNodes.size() == 1) {
			return sqlNodes.get(0);
		}

		// Map all variables to the bindings where they are used
		Multimap<Node, Integer> varToBinding = HashMultimap.create();

		for (int i = 0; i < sqlNodes.size(); ++i) {
			SqlNode binding = sqlNodes.get(i);
			for (Node var : binding.getSparqlVarsMentioned()) {
				varToBinding.put(var, i);
			}
		}

		
		ColRelGenerator aliasGen = Gensym.create("h");
		// Align the Bindings for each variable
		// For each variable, for each union's child, collect all expressions
		// Then cluster them based on their hash (structural equivalence)
		for(Entry<Node, Collection<Integer>> entry : varToBinding.asMap()
				.entrySet()) {


			// Map each expression to an expression and its argument index
			Multimap<Integer, ArgExpr> hashToExpr = HashMultimap.create();
			for (int index : entry.getValue()) {
				SqlNode sqlNode = sqlNodes.get(index).getSparqlVarToExprs();
				
				Map<String, SqlDatatype> columnToDatatype = SqlNodeUtil.getColumnToDatatype(tmp);
				Integer hash = ExprDatatypeHash.hash(sqlNode, columnToDatatype);
				
			}

			//List<Expr> exprs = new ArrayList<Expr>();

			
			for (SqlNode tmp : bindings) {
				// Map the columns to their datatype

				
				exprs.addAll(tmp.getSparqlVarToExprs().get(var));
			}

			ExprCommonFactor factorizer = new ExprCommonFactor(aliasGen);
			List<Map<Var, Expr>> partialProjections = new ArrayList<Map<Var, Expr>>();
			Expr common = factorizer.transform(exprs, partialProjections);

			commons.put((Var)var, common);
			//result.getSparqlVarToExpr().put(var, common);

			// Merge the local projections into the global ones

			int i = 0;
			for (int index : entry.getValue()) {
				// SqlNodeBinding tmp = bindings.get(index);
				projections.get(index).putAll(partialProjections.get(i));
				++i;
			}

			// Map<Var, Expr> partialProjection = partialProjections.get(i);

			// FIXME We assume that there is no overlap in helper variables
			// Maybe we could do a merge method like Map merge(Map a, Map b)
			// This method would add all entries of b to a, and rename those
			// keys
			// that already exist in a. The result is a map of renamings.
			// result.getSqlVarToExpr().putAll(partialProjections);

			// FIXME Maybe create an OpProjection rather than doing it in place
			//tmp.getSqlVarToExpr().putAll(partialProjection);
			/*
			tmp.getSparqlVarToExpr().put(var, common);

			String alias = "a" + globalAliasId++;
			SqlProjection opProj = new SqlProjection(alias, tmp.getSqlNode(), partialProjection);
			* /
		}

		for (int i = 0; i < projections.size(); ++i) {
			SqlNode tmp = sqlNodes.get(i);
			Map<Var, Expr> projection = projections.get(i);

			// Projection.Var becomes the new column alias
			// Projection.Expr is pushed down to an sqlExpr
			// Projection.Expr's vars are replaced with the original column defs
			
			NodeExprSubstitutor substitutor = createSubstitutor(tmp);
			Map<String, SqlExpr> subbedProj = new HashMap<String, SqlExpr>();
			for(Entry<Var, Expr> entry : projection.entrySet()) {
				Expr subbed = substitutor.transformMM(entry.getValue());
				Expr pushed = PushDown.pushDownMM(subbed);
				
				if(!(pushed instanceof ExprSqlBridge)) {
					throw new RuntimeException("Could not push down common sub expression");
				}

				SqlExpr sqlExpr = ((ExprSqlBridge)pushed).getSqlExpr();
				
				subbedProj.put(entry.getKey().getName(), sqlExpr);
			}

			// Update the projection
			tmp.getAliasToColumn().clear();
			tmp.getAliasToColumn().putAll(subbedProj);

			tmp.getSparqlVarToExprs().clear();
			tmp.getSparqlVarToExprs().putAll(commons);
			
		}



		String unionAlias = generator.next();
		SqlNode result = new SqlUnionN(unionAlias, sqlNodes);

		result.getSparqlVarToExprs().putAll(commons);
		
		Set<String> columns = new HashSet<String>();
		for(Expr exprs : commons.values()) {
			for(Var var : exprs.getVarsMentioned()) {
				columns.add(var.getName());
			}
		}
		
		for(String columnName : columns) {
			result.getAliasToColumn().put(columnName, new SqlExprColumn(null, columnName));
		}
		

		return result;
	}

	/**
	 * Aligns the layout of a sparql variable on the SQL level.
	 * 
	 * For instance, if a sparql variable is defined as ?o = GeoLit(?geom) then
	 * it will introduce helper columns on SQL such as:
	 * 
	 * ?o_lexicalValue = ST_AsText(?geom) ?o_type = 1 // Literal ?o_dataType =
	 * "http://.../geometry"
	 * 
	 * which are used to intruduce new columns in the SQL query: Select
	 * ST_AsText(?geom) as o_lexicalValue, 1 as o_type...
	 * 
	 * and the rewritten definition of ?o as ?o = RdfTerm(?o_type,
	 * ?o_lexicalValue, null, ?o_dataType)
	 * 
	 * This is needed in order to e.g. allow a union such as { ?s geom ?o }
	 * union {?s hasGeometry ?o }, wheres the second ?o is not based on a
	 * GeoLiteral column in which case the different "?o"s have to be aligned in
	 * their structure
	 * 
	 * 
	 */
	public void alignSql() {
		// TODO implement
	}
}